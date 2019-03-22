package org.consensusj.jsonrpc.introspection.sample;

import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Simple command-line tool that contains a JsonRpcService
 * Allows testing method dispatch independent of any server code or framework
 * (including compiling with Graal `native-image` and running as a native tool with SubstrateVM)
 */
public class MathTool extends AbstractJsonRpcService {
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());

    public MathTool() {
        super(methods);
    }

    public static void main(String[] args) {
        JsonRpcService service = new MathTool();
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(1, 2));
        JsonRpcResponse<Object> response = null;
        try {
            response = service.call(req).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Integer sum = (Integer) response.getResult();

        System.out.println("Sum is: " + sum);
    }

    public Integer add(Integer a, Integer b) {
        return a + b;
    }

    public Integer subtract(Integer a, Integer b) {
        return a - b;
    }
}
