package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msgilligan.bitcoinj.rpc.JsonRpcRequest;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Base class for JSON RPC handlers (until we have a better solution for config and common code)
 */
public abstract class AbstractJsonRpcHandler {
    protected static final String jsonType = "application/json";
    protected final URI remoteURI;
    protected final String remoteUserName;
    protected final String remotePassword;
    protected final ObjectMapper mapper = new ObjectMapper();

    protected AbstractJsonRpcHandler() throws URISyntaxException {
        remoteURI = new URI("http://localhost:18332");
//        remoteUserName = null;
//        remotePassword = null;
        remoteUserName = "bitcoinrpc";
        remotePassword = "pass";
    }

    // TODO: Surely this method isn't necessary with Ratpack
    protected String requestToString(JsonRpcRequest request) {
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
