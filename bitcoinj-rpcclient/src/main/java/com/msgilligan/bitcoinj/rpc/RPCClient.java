package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * JSON-RPC Client
 */
public class RPCClient {
    private static final Logger log = LoggerFactory.getLogger(RPCClient.class);
    private URI serverURI;
    private String username;
    private String password;
    protected ObjectMapper mapper;
    private static final boolean disableSslVerification = true;

    static {
        if (disableSslVerification) {
            // Disable checks that prevent using a self-signed SSL certificate
            // TODO: Should checks be enabled by default for security reasons?
            disableSslVerification();
        }
    }

    public RPCClient(RPCConfig config) {
        this(config.getURI(), config.getUsername(), config.getPassword(), null);
    }

    public RPCClient(URI server, final String rpcuser, final String rpcpassword) {
        this(server, rpcuser, rpcpassword, null);

    }

    /**
     *
     * @param server server URI should not contain username/password
     * @param rpcuser username for the RPC HTTP connection
     * @param rpcpassword password for the RPC HTTP connection
     * @param mapper Jackson object Mapper or null to use default mapper
     */
    public RPCClient(URI server, final String rpcuser, final String rpcpassword, ObjectMapper mapper) {
        this.serverURI = server;
        this.username = rpcuser;
        this.password = rpcpassword;
        this.mapper = (mapper == null) ? new ObjectMapper() : mapper;
    }

    public URI getServerURI() {
        return serverURI;
    }

    /**
     * Send a JSON-RPC request to the server and return a JSON-RPC response.
     *
     * @param request JSON-RPC request
     * @param responseType Response type to deserialize to
     * @return JSON-RPC response
     * @throws IOException when thrown by the underlying HttpURLConnection
     * @throws JsonRPCStatusException when the HTTP response code is other than 200
     */
    private <R> JsonRpcResponse<R> send(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRPCStatusException {
        HttpURLConnection connection = openConnection();

        // TODO: Make sure HTTP keep-alive will work
        // See: http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html
        // http://developer.android.com/reference/java/net/HttpURLConnection.html
        // http://android-developers.blogspot.com/2011/09/androids-http-clients.html

        if (log.isDebugEnabled()) {
            log.debug("Req json: {}", mapper.writeValueAsString(request));
        }

        OutputStream requestStream = connection.getOutputStream();
        mapper.writeValue(requestStream, request);
        requestStream.close();

        int responseCode = connection.getResponseCode();
        log.debug("Response code: {}", responseCode);

        if (responseCode != 200) {
            handleBadResponseCode(responseCode, connection);
        }

        JsonRpcResponse<R> responseJson = mapper.readValue(connection.getInputStream(), responseType);
        log.debug("Resp json: {}", responseJson);
        connection.disconnect();
        return responseJson;
    }

    // Prepare and throw JsonRPCStatusException with all relevant info
    private void handleBadResponseCode(int responseCode, HttpURLConnection connection) throws IOException, JsonRPCStatusException {
        String responseMessage = connection.getResponseMessage();
        String exceptionMessage = responseMessage;
        int jsonRPCCode = 0;
        JsonRpcResponse bodyJson = null;    // Body as JSON if available
        String bodyString = null;               // Body as String if not JSON
        if (connection.getContentType().equals("application/json")) {
            // We got a JSON error response, parse it
            bodyJson = mapper.readValue(connection.getErrorStream(), JsonRpcResponse.class);
            JsonRpcError error = bodyJson.getError();
            if (error != null) {
                // If there's a more specific message in the JSON use it instead.
                exceptionMessage = error.getMessage();
                jsonRPCCode = error.getCode();
            }
        } else {
            // No JSON, read response body as string
            InputStream errorStream = connection.getErrorStream();
            bodyString = new Scanner(errorStream,"UTF-8").useDelimiter("\\A").next();
            errorStream.close();
        }
        throw new JsonRPCStatusException(exceptionMessage, responseCode, responseMessage, jsonRPCCode, bodyString, bodyJson);
    }

    /**
     * JSON-RPC remote method call that returns the 'result'
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param <R> Expected return type -- will match type of variable method result is assigned to
     * @return the 'result' field of the JSON RPC response
     */
    protected <R> R send(String method, List<Object> params, Class<R> resultType) throws IOException, JsonRPCStatusException {
        JsonRpcRequest request = new JsonRpcRequest(method, params);
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametrizedType(JsonRpcResponse.class, JsonRpcResponse.class, resultType);
        JsonRpcResponse<R> response =  send(request, responseType);

//        assert response != null;
//        assert response.getJsonrpc() != null;
//        assert response.getJsonrpc().equals("2.0");
//        assert response.getId() != null;
//        assert response.getId().equals(request.getId());

        return response.getResult();
    }

    protected <R> R send(String method, List<Object> params, JavaType resultType) throws IOException, JsonRPCStatusException {
        JsonRpcRequest request = new JsonRpcRequest(method, params);
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametrizedType(JsonRpcResponse.class, JsonRpcResponse.class, resultType);
        JsonRpcResponse<R> response =  send(request, responseType);

        return response.getResult();
    }

    protected <R> R send(String method, List<Object> params) throws IOException, JsonRPCStatusException {
        return (R) send(method, params, (Class<R>) Object.class);
    }

    /**
     * CLI-style send
     *
     * Useful for:
     * * Simple (not client-side validated) command line utilities
     * * Functional tests that need to send incorrect types to the server to test error handling
     *
     * @param method Allows RPC method to be passed as a stream
     * @param params variable number of untyped objects
     * @return The 'result' element from the returned JSON RPC response
     */
    public Object cliSend(String method, Object... params) throws IOException, JsonRPCException {
        return send(method, createParamList(params));
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection =  (HttpURLConnection) serverURI.toURL().openConnection();
        connection.setDoOutput(true); // For writes
        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
//        connection.setRequestProperty("Content-Type", " application/json;charset=" + StandardCharsets.UTF_8.toString());
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json;charset=" +  "UTF-8");
        connection.setRequestProperty("Connection", "close");   // Avoid EOFException: http://stackoverflow.com/questions/19641374/android-eofexception-when-using-httpurlconnection-headers

        String auth = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());
        connection.setRequestProperty ("Authorization", basicAuth);

        return connection;
    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a mutable param list (so send() can remove null parameters)
     * @param parameters  A variable number of parameters as varargs or array
     * @return A mutable list of the same parameters
     */
    protected List<Object> createParamList(Object... parameters) {
        return new ArrayList<Object>(Arrays.asList(parameters));
    }
}
