package org.consensusj.jsonrpc.ratpack;

import com.fasterxml.jackson.databind.JsonNode;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import ratpack.exec.Promise;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 *  Async JsonRpcClient for Ratpack using Retrofit and Jackson
 */
public interface JsonRpcClient {
    @POST("/")
    Promise<JsonRpcResponse<JsonNode>> call(@Body JsonRpcRequest request);
}
