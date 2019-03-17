package org.consensusj.jsonrpc.introspection.sample;

import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.introspection.JsonRpcServerWrapper;
import org.consensusj.jsonrpc.introspection.JsonRpcServerWrapperGraal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class SumTool extends JsonRpcServerWrapperGraal {
    private static final Map<String, MethodHandle> methods = JsonRpcServerWrapper.reflect(MethodHandles.lookup().lookupClass());

    public SumTool(Map<String, MethodHandle> methods) {
        super(methods);
    }

    public static void main(String[] args) {
        SumTool service = new SumTool(methods);
        List<Object> params = Arrays.asList(1, 2);
        JsonRpcRequest req = new JsonRpcRequest("sum", params);
        JsonRpcResponse<Object> response;
        try {
            response = service.call(req).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


    public Integer sum(Integer a, Integer b) {
        return a + b;
    }
}
