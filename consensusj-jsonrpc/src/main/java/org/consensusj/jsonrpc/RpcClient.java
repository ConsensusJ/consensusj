package org.consensusj.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.consensusj.jsonrpc.util.Base64;
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
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * = JSON-RPC Client
 *
 * This is a concrete class with basic JSON-RPC functionality. Abstract `sendRequestForResponse` method is implemented
 * using `HttpURLConnection`.
 *
 * Uses strongly-typed POJOs representing {@link JsonRpcRequest} and {@link JsonRpcResponse}. The
 * response object uses a parameterized type for the object that is the actual JSON-RPC `result`.
 * Using strong types and Jackson to serialize/deserialize to/from strongly-typed POJO's without
 * using intermediate `Map` or `JsonNode` types.
 *
 */
public class RpcClient extends AbstractRpcClient {
    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);
    private URI serverURI;
    private String username;
    private String password;
    private static final boolean disableSslVerification = false;
    private static final String UTF8 = StandardCharsets.UTF_8.name();

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
     * @param server server URI should not contain username/password
     * @param rpcUser username for the RPC HTTP connection
     * @param rpcPassword password for the RPC HTTP connection
     */
    public RpcClient(URI server, final String rpcUser, final String rpcPassword) {
        super();
        this.serverURI = server;
        this.username = rpcUser;
        this.password = rpcPassword;
    }

    /**
     * Get the URI of the server this client connects to
     * @return Server URI
     */
    @Override
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
     * @throws JsonRpcStatusException when the HTTP response code is other than 200
     */
    @Override
    protected <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        HttpURLConnection connection = openConnection();

        // TODO: Make sure HTTP keep-alive will work
        // See: http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html
        // http://developer.android.com/reference/java/net/HttpURLConnection.html
        // http://android-developers.blogspot.com/2011/09/androids-http-clients.html

        if (log.isDebugEnabled()) {
            log.debug("Req json: {}", mapper.writeValueAsString(request));
        }
        
        try (OutputStream requestStream = connection.getOutputStream()) {
            mapper.writeValue(requestStream, request);
        }

        int responseCode = connection.getResponseCode();
        log.debug("Response code: {}", responseCode);

        if (responseCode != 200) {
            handleBadResponseCode(responseCode, connection);
        }

        JsonRpcResponse<R> responseJson;
        try {
            if (log.isDebugEnabled()) {
                // If logging enabled, copy InputStream to string and log
                String responseBody = convertStreamToString(connection.getInputStream());
                log.debug("responseBody: {}", responseBody);
                responseJson = mapper.readValue(responseBody, responseType);
            } else {
                // Otherwise convert directly to responseType
                responseJson = mapper.readValue(connection.getInputStream(), responseType);
            }
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: {}", e);
            // TODO: Map to some kind of JsonRPC exception similar to JsonRPCStatusException
            throw e;
        }
        log.debug("Resp json: {}", responseJson);
        connection.disconnect();
        return responseJson;
    }

    /**
     * Prepare and throw JsonRPCStatusException with all relevant info
     * @param responseCode Non-success response code
     * @param connection the current connection
     * @throws IOException IO Error
     * @throws JsonRpcStatusException An exception containing the HTTP status coe and a message
     */
    private void handleBadResponseCode(int responseCode, HttpURLConnection connection) throws IOException, JsonRpcStatusException
    {
        String responseMessage = connection.getResponseMessage();
        String exceptionMessage = responseMessage;
        int jsonRpcCode = 0;
        JsonRpcResponse bodyJson = null;    // Body as JSON if available
        String bodyString = null;           // Body as String if not JSON
        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
            if (connection.getContentType().equals("application/json")) {
                // We got a JSON error response, parse it
                bodyJson = mapper.readValue(errorStream, JsonRpcResponse.class);
                JsonRpcError error = bodyJson.getError();
                if (error != null) {
                    // If there's a more specific message in the JSON use it instead.
                    exceptionMessage = error.getMessage();
                    jsonRpcCode = error.getCode();
                    // Since this is an error at the JSON level, let's log it with `info` level and
                    // let the higher-level software decide whether to log it as `error` or not.
                    // i.e. The higher-level software can set error level on this module to `warn` and then
                    // decide whether to log this "error" not based upon the JsonRpcStatusException thrown.
                    // An example occurs in Bitcoin when a client is waiting for a server to initialize
                    // and returns 'Still scanning.. at block 530006 of 548850'
                    log.info("json error code: {}, message: {}", jsonRpcCode, exceptionMessage);
                }
            } else {
                // No JSON, read response body as string
                bodyString = convertStreamToString(errorStream);
                log.error("error string: {}", bodyString);
                errorStream.close();
            }
        }
        throw new JsonRpcStatusException(exceptionMessage, responseCode, responseMessage, jsonRpcCode, bodyString, bodyJson);
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, UTF8).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection =  (HttpURLConnection) serverURI.toURL().openConnection();
        connection.setDoOutput(true); // For writes
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset", UTF8);
        connection.setRequestProperty("Content-Type", "application/json;charset=" +  UTF8);
        connection.setRequestProperty("Connection", "close");   // Avoid EOFException: http://stackoverflow.com/questions/19641374/android-eofexception-when-using-httpurlconnection-headers

        String auth = username + ":" + password;
        String basicAuth = "Basic " + base64Encode(auth);
        connection.setRequestProperty ("Authorization", basicAuth);

        return connection;
    }

    /**
     * Encode username password as Base64 for basic authentication
     *
     * We're using an internal `Base64` utility class here (copied from Android) in order
     * to have working, consistent behavior on JavaSE and Android. Prior to Android 8.0,
     * Android has it's own implementation that differs from the JavaSE version.
     * 
     * @param authString An authorization string of the form `username:password`
     * @return A compliant Base64 encoding of `authString`
     */
    protected static String base64Encode(String authString) {
        return Base64.encodeToString(authString.getBytes(),Base64.NO_WRAP).trim();
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
