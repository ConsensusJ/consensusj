package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import static ratpack.jackson.Jackson.fromJson;
import ratpack.http.client.HttpClient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * JsonRPC proxy handler
 * Relay allowed RPC methods to a URI
 * (defaults to localhost with regtest port)
 */
public class RpcProxyHandler implements Handler {
    private static final String jsonType = "application/json";
    private final URI remoteURI;
    private final String remoteUserName;
    private final String remotePassword;
    private final List<String> allowedMethods =  Arrays.asList("getblockcount", "setgenerate");
    private final ObjectMapper mapper = new ObjectMapper();

    public RpcProxyHandler() throws Exception {
        remoteURI = new URI("http://localhost:18332");
        remoteUserName = null;
        remotePassword = null;
    }

    public RpcProxyHandler(URI server, String userName, String password) throws Exception {
        remoteURI = server;
        remoteUserName = userName;
        remotePassword = password;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.parse(fromJson(JsonRpcRequest.class)).then(rpcReq -> {
            if (allowedMethods.contains(rpcReq.getMethod())) {
                ctx.get(HttpClient.class).requestStream(remoteURI, requestSpec -> {
                    requestSpec.post();
                    requestSpec.body(body ->
                            body.type(jsonType).text(requestToString(rpcReq)));
                    requestSpec.redirects(0);
                    if (remoteUserName != null) {
                        requestSpec.basicAuth(remoteUserName, remotePassword);
                    }
                }).then(responseStream -> {
                    responseStream.forwardTo(ctx.getResponse());
                });
            } else {
                // Should we send a JsonRpcResponse here?
                ctx.getResponse().status(403).send("JSON-RPC method not allowed by proxy");
            }
        });
    }

    // TODO: Surely this method isn't necessary with Ratpack
    private String requestToString(JsonRpcRequest request) {
        String result;
        try {
            result = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result = "proxy jackson error";
        }
        return result;
    }
}
