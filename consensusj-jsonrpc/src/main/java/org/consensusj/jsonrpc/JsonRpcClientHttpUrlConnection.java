package org.consensusj.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * JSON-RPC Client using {@link HttpURLConnection} formerly named{@code RpcClient}.
 * <p>
 * This is a concrete class with generic JSON-RPC functionality, it implements the abstract
 * method {@link AbstractRpcClient#sendRequestForResponseAsync(JsonRpcRequest, JavaType)} using {@link HttpURLConnection}.
 * <p>
 * Uses strongly-typed POJOs representing {@link JsonRpcRequest} and {@link JsonRpcResponse}. The
 * response object uses a parameterized type for the object that is the actual JSON-RPC `result`.
 * Using strong types and Jackson to serialize/deserialize to/from strongly-typed POJO's without
 * using intermediate `Map` or `JsonNode` types.
 *
 */
public class JsonRpcClientHttpUrlConnection extends AbstractRpcClient {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcClientHttpUrlConnection.class);
    private final URI serverURI;
    private final String username;
    private final String password;
    private static final String UTF8 = StandardCharsets.UTF_8.name();
    private final SSLSocketFactory sslSocketFactory;

    /**
     * @deprecated Use {@link JsonRpcClientHttpUrlConnection#JsonRpcClientHttpUrlConnection(SSLContext, JsonRpcMessage.Version, URI, String, String)}
     */
    @Deprecated
    public JsonRpcClientHttpUrlConnection(SSLSocketFactory socketFactory, JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        super(jsonRpcVersion);
        this.sslSocketFactory = socketFactory;
        log.debug("Constructing JSON-RPC client for: {}", server);
        this.serverURI = server;
        this.username = rpcUser;
        this.password = rpcPassword;
    }

    public JsonRpcClientHttpUrlConnection(SSLContext sslContext, JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        super(jsonRpcVersion);
        this.sslSocketFactory = sslContext.getSocketFactory();
        log.debug("Constructing JSON-RPC client for: {}", server);
        this.serverURI = server;
        this.username = rpcUser;
        this.password = rpcPassword;
    }

    /**
     * Construct a JSON-RPC client from URI, username, and password
     *
     * @param jsonRpcVersion version for {@code jsonrpc} field in messages
     * @param server server URI should not contain username/password
     * @param rpcUser username for the RPC HTTP connection
     * @param rpcPassword password for the RPC HTTP connection
     */
    public JsonRpcClientHttpUrlConnection(JsonRpcMessage.Version jsonRpcVersion, URI server, final String rpcUser, final String rpcPassword) {
        this(getDefaultSSLContext(), jsonRpcVersion, server, rpcUser, rpcPassword);
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
    public <R> JsonRpcResponse<R> sendRequestForResponse(JsonRpcRequest request, JavaType responseType) throws IOException, JsonRpcStatusException {
        HttpURLConnection connection = openConnection();

        // TODO: Make sure HTTP keep-alive will work
        // See: http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html
        // http://developer.android.com/reference/java/net/HttpURLConnection.html
        // http://android-developers.blogspot.com/2011/09/androids-http-clients.html

        if (log.isDebugEnabled()) {
            log.debug("JsonRpcRequest: {}", mapper.writeValueAsString(request));
        }
        
        try (OutputStream requestStream = connection.getOutputStream()) {
            mapper.writeValue(requestStream, request);
        }

        int responseCode = connection.getResponseCode();
        log.debug("HTTP Response code: {}", responseCode);

        if (responseCode != 200) {
            handleBadResponseCode(responseCode, connection);
        }

        JsonRpcResponse<R> responseJson = responseFromStream(connection.getInputStream(), responseType);
        connection.disconnect();
        return responseJson;
    }

    @Override
    public <R> CompletableFuture<JsonRpcResponse<R>> sendRequestForResponseAsync(JsonRpcRequest request, JavaType responseType) {
        return supplyAsync(() -> this.sendRequestForResponse(request, responseType));
    }

    private <R> JsonRpcResponse<R> responseFromStream(InputStream inputStream, JavaType responseType) throws IOException {
        JsonRpcResponse<R> responseJson;
        try {
            if (log.isDebugEnabled()) {
                // If logging enabled, copy InputStream to string and log
                String responseBody = convertStreamToString(inputStream);
                log.debug("Response Body: {}", responseBody);
                responseJson = mapper.readValue(responseBody, responseType);
            } else {
                // Otherwise convert directly to responseType
                responseJson = mapper.readValue(inputStream, responseType);
            }
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: ", e);
            // TODO: Map to some kind of JsonRPC exception similar to JsonRPCStatusException
            throw e;
        }
        return responseJson;
    }

    /**
     * Prepare and throw JsonRPCStatusException with all relevant info
     * @param responseCode Non-success response code
     * @param connection the current connection
     * @throws IOException IO Error
     * @throws JsonRpcStatusException An exception containing the HTTP status code and a message
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
                JavaType genericResponseType = mapper.getTypeFactory().
                        constructParametricType(JsonRpcResponse.class, JsonNode.class);
                // We got a JSON error response -- try to parse it as a JsonRpcResponse
                bodyJson = responseFromStream(errorStream, genericResponseType);
                JsonRpcError error = bodyJson.getError();
                if (error != null) {
                    // If there's a more specific message in the JSON use it instead.
                    exceptionMessage = error.getMessage();
                    jsonRpcCode = error.getCode();
                    // Since this is an error at the JSON level, let's log it with `debug` level and
                    // let the higher-level software decide whether to log it as `error` or not.
                    // i.e. The higher-level software can set error level on this module to `warn` and then
                    // decide whether to log this "error" not based upon the JsonRpcStatusException thrown.
                    // An example occurs in Bitcoin when a client is waiting for a server to initialize
                    // and returns 'Still scanning.. at block 530006 of 548850'
                    log.debug("json error code: {}, message: {}", jsonRpcCode, exceptionMessage);
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
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(this.sslSocketFactory);
        }
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
}
