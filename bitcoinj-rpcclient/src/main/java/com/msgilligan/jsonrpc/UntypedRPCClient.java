package com.msgilligan.jsonrpc;

import java.io.IOException;
import java.util.List;

/**
 * Interface for untyped (or dynamic, via Groovy) RPC calls
 */
public interface UntypedRPCClient {
    <R> R send(String method, List<Object> params) throws IOException, JsonRPCStatusException;

    <R> R send(String method, Object... params) throws IOException, JsonRPCStatusException;
}
