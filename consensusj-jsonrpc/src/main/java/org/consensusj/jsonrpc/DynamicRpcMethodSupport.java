package org.consensusj.jsonrpc;

import java.io.IOException;
import java.util.List;

/**
 * Basic JSON-RPC remote call interface for RPC calls
 *
 * The parameter list is "untyped" (declared as `List<Object>`) and implementations are responsible
 * for converting each Java object parameter to a valid and correctly-typed (for the method) JSON object.
 *
 * This is used to implement the `DynamicRpcMethodFallback` trait in Groovy which is applied
 * to various Groovy RPC client implementations that typically inherit statically-dispatched
 * methods from Java classes, but use `methodMissing()` to add JSON-RPC methods dynamically.
 * This may be useful in other Dynamic JVM languages, as well.
 *
 */
public interface DynamicRpcMethodSupport {
    /**
     * Call an RPC method and return default object type.
     *
     * Caller should cast returned object to the correct type.
     *
     * @param method JSON RPC method call to send
     * @param params JSON RPC parameters using types that are convertible to JSON
     * @param pass:[<R>] Type of result object
     * @return the `response.result` field of the JSON-RPC response cast to type R
     * @throws IOException network error
     * @throws JsonRpcStatusException JSON RPC status error
     */
    <R> R send(String method, List<Object> params) throws IOException, JsonRpcStatusException;
}
