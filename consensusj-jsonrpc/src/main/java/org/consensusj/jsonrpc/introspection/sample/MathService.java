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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Simple service and command-line tool that contains a "math" JsonRpcService
 * Allows testing method dispatch independent of any server code or framework
 * (including compiling with Graal `native-image` and running as a native tool with SubstrateVM)
 * This will be moved or removed in a future release.
 */
public class MathService extends AbstractJsonRpcService {
    private static final Logger log = LoggerFactory.getLogger(MathService.class);
    private static final Map<String, Method> methods = JsonRpcServiceWrapper.reflect(MethodHandles.lookup().lookupClass());

    /**
     * Constructor that calls {@link AbstractJsonRpcService#AbstractJsonRpcService(Map)} with a private, statically-initialized
     * {@link Map} of methods generated with {@link JsonRpcServiceWrapper#reflect(Class)}  }.
     */
    public MathService() {
        super(methods);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        JsonRpcService service = new MathService();
        JsonRpcRequest req = new JsonRpcRequest("add", Arrays.asList(1, 2));
        JsonRpcResponse<Object> response =  service.call(req).get();

        Integer sum = (Integer) response.getResult();
        if (sum != null) {
            System.out.println("Sum is: " + sum);
        } else {
            String message = response.getError() != null
                    ? response.getError().getMessage()
                    : "error was null";
            System.err.println("Error = " + message);
            System.exit(-1);
        }
    }

    public CompletableFuture<Integer> add(Integer a, Integer b) {
        log.info("MathService: add {} + {}",a,b);
        return result(a + b);
    }

    public CompletableFuture<Integer> subtract(Integer a, Integer b) {
        log.info("MathService: subtract {} - {}",a,b);
        return result(a - b);
    }

    @Override
    public void close() throws Exception {
    }
}
