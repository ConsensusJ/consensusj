package com.msgilligan.bitcoinj.proxy;

import static com.msgilligan.bitcoinj.rpc.BitcoinClientMethod.*;
import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import static ratpack.jackson.Jackson.fromJson;
import ratpack.http.client.HttpClient;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * JsonRPC proxy handler
 * Relay allowed RPC methods to a URI
 * (defaults to localhost with regtest port)
 */
public class RpcProxyHandler extends AbstractJsonRpcHandler implements Handler {
    private final List<String> allowedMethods =
            Stream.of(getblockcount, setgenerate)
                .map(Enum::name).collect(Collectors.toList());

    protected RpcProxyHandler() throws URISyntaxException {
        super();
    }


//    public RpcProxyHandler(URI server, String userName, String password) throws Exception {
//        remoteURI = server;
//        remoteUserName = userName;
//        remotePassword = password;
//    }

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

}
