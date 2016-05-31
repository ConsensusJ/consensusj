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
 * = JSON-RPC Client
 *
 * This is a concrete class with basic JSON-RPC functionality. In theory it could be used to implement
 * other JSON-RPC clients, but as this is a Bitcoin-focused project you probably want to look at
 * {@link BitcoinClient} and its subclasses.
 *
 * This client uses strongly-typed POJOs representing {@link JsonRpcRequest} and {@link JsonRpcResponse}. The
 * response object uses a type parameter to specify the object that is the actual JSON-RPC `result`.
 * Early versions of this client were http://c2.com/cgi/wiki?StringlyTyped[stringly-typed], but
 * these strong types allows us to use Jackson to deserialize
 * directly to strongly-typed POJO's without using intermediate `Map` or `JsonNode` types.
 *
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

    /**
     * Construct a JSON-RPC client from URI, username, and password
     *
     * Typically you'll want to use {@link BitcoinClient} or one of its subclasses
     * @param server server URI should not contain username/password
     * @param rpcuser username for the RPC HTTP connection
     * @param rpcpassword password for the RPC HTTP connection
     */
    public RPCClient(URI server, final String rpcuser, final String rpcpassword) {
        this.serverURI = server;
        this.username = rpcuser;
        this.password = rpcpassword;
        this.mapper = new ObjectMapper();
    }

    /**
     * Get the URI of the server this client connects to
     * @return Server URI
     */
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

    @Deprecated
    protected <R> R send(String method, List<Object> params, Class<R> resultType) throws IOException, JsonRPCStatusException {
        return send(method, resultType, params);
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, Class<R> resultType, Object... params) throws IOException, JsonRPCStatusException {
        return send(method, resultType, Arrays.asList(params));
    }

    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param pass:[<R>] Type of result object
     * @param resultType desired result type as a Java class object
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    protected <R> R send(String method, Class<R> resultType, List<Object> params) throws IOException, JsonRPCStatusException {
        JsonRpcRequest request = new JsonRpcRequest(method, params);
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametrizedType(JsonRpcResponse.class, JsonRpcResponse.class, resultType);
        JsonRpcResponse<R> response = send(request, responseType);

//        assert response != null;
//        assert response.getJsonrpc() != null;
//        assert response.getJsonrpc().equals("2.0");
//        assert response.getId() != null;
//        assert response.getId().equals(request.getId());

        return response.getResult();
    }

    @Deprecated
    protected <R> R send(String method, List<Object> params, JavaType resultType) throws IOException, JsonRPCStatusException {
        return send(method, resultType, params);
    }

    /**
     * Varargs version
     */
    protected <R> R send(String method, JavaType resultType, Object... params) throws IOException, JsonRPCStatusException {
        return send(method, resultType, Arrays.asList(params));
    }

    /**
     * JSON-RPC remote method call that returns 'response.result`
     *
     * @param pass:[<R>] Type of result object
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param resultType desired result type as a Jackson JavaType object
     * @return the 'response.result' field of the JSON RPC response converted to type R
     */
    protected <R> R send(String method, JavaType resultType, List<Object> params) throws IOException, JsonRPCStatusException {
        JsonRpcRequest request = new JsonRpcRequest(method, params);
        // Construct a JavaType object so we can tell Jackson what type of result we are expecting.
        // (We can't use R because of type erasure)
        JavaType responseType = mapper.getTypeFactory().
                constructParametrizedType(JsonRpcResponse.class, JsonRpcResponse.class, resultType);
        JsonRpcResponse<R> response =  send(request, responseType);

        return response.getResult();
    }

    /**
     * Call an RPC method and return default object type.
     *
     * Caller should cast returned object to the correct type.
     *
     * Useful for:
     * * Simple (not client-side validated) command line utilities
     * * Functional tests that need to send incorrect types to the server to test error handling
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC params
     * @param pass:[<R>] Type of result object
     * @return the 'response.result' field of the JSON RPC response cast to type R
     * @throws IOException
     * @throws JsonRPCStatusException
     */
    public <R> R send(String method, Object... params) throws IOException, JsonRPCStatusException {
        return send(method, Arrays.asList(params));
    }

    public <R> R send(String method, List<Object> params) throws IOException, JsonRPCStatusException {
        return (R) send(method, (Class<R>) Object.class, params);
    }

    /**
     * CLI-style send
     *
     * Now that we've pushed most of the parameter conversion into Jackson serializers,
     * there's little difference between this method and the `send()` that it calls.
     * After some refactoring it is now being eliminated.
     *
     * @param method Allows RPC method to be passed as a stream
     * @param params variable number of untyped objects
     * @return The 'result' element from the returned JSON RPC response
     * @deprecated There is now a send() method with the same calling conventions and behavior
     */
    @Deprecated
    public Object cliSend(String method, Object... params) throws IOException, JsonRPCException {
        return send(method, params);
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

    // TODO: Allow for self-signed certificates without disabling all verification
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
        } catch (NoSuchAlgorithmException | KeyManagementException e ) {
            log.error("Exception in disableSslVerification{}", e);
            e.printStackTrace();
        }
    }
}
