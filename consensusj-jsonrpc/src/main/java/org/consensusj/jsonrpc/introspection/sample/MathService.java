package org.consensusj.jsonrpc.introspection.sample;

import org.consensusj.jsonrpc.JsonRpcError;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.JsonRpcResponse;
import org.consensusj.jsonrpc.JsonRpcService;
import org.consensusj.jsonrpc.introspection.AbstractJsonRpcService;
import org.consensusj.jsonrpc.introspection.JsonRpcServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Simple service and command-line tool that contains a "math" JsonRpcService
 * Allows testing method dispatch independent of any server code or framework
 * (including compiling with Graal `native-image` and running as a native tool with SubstrateVM)
 */
public class MathService extends AbstractJsonRpcService {
    private static Logger log = LoggerFactory.getLogger(MathService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());

    public MathService() {
        super(methods);
    }

    public static void main(String[] args) {
        JsonRpcService service = new MathService();
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(1, 2));
        JsonRpcResponse<Object> response = null;
        try {
            response = service.call(req).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Integer sum = (Integer) response.getResult();
        JsonRpcError error = response.getError();
        if (sum == null) {
            System.err.println("Error = " + response.getError().getMessage());
            System.exit(-1);
        }
        System.out.println("Sum is: " + sum);
    }

    public Integer add(Integer a, Integer b) {
        log.info("MathService: add {} + {}",a,b);
        return a + b;
    }

    public Integer subtract(Integer a, Integer b) {
        log.info("MathService: subtract {} - {}",a,b);
        return a - b;
    }
}
