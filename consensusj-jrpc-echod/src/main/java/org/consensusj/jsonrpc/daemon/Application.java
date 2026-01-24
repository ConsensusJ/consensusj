package org.consensusj.jsonrpc.daemon;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import org.consensusj.jsonrpc.JsonRpcShutdownService;

@Factory
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }

    @Singleton
    @Bean(preDestroy = "close")
    EchoJsonRpcService echoJsonRpcService(JsonRpcShutdownService shutdownService) {
        return new EchoJsonRpcService(shutdownService);
    }
}
