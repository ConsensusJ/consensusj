package org.consensusj.jsonrpc.ratpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Provides;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import org.consensusj.jsonrpc.util.Base64;
import ratpack.exec.Execution;
import ratpack.func.Factory;
import ratpack.guice.ConfigurableModule;
import ratpack.handling.Context;
import ratpack.http.client.HttpClient;
import ratpack.retrofit.RatpackRetrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Create an asynchronous JSON RPC Client using RatpackRetrofit
 * TODO: Remove specific dependencies on Bitcoin
 */
public class JsonRpcClientModule extends ConfigurableModule<RpcConfig> {
    @Override
    protected void configure() {
    }

    @Provides
    ObjectMapper provideObjectMapper(RpcConfig config) {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new RpcClientModule(config.getNetParams()))
                .registerModule(new RpcServerModule(config.getNetParams()));
    }
    
    @Provides
    JacksonConverterFactory provideJacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Provides
    JsonRpcClient provideJsonRpcClient(Factory<? extends HttpClient> httpClientFactory,
                                       JacksonConverterFactory converterFactory,
                                       RpcConfig rpcConfig) {
        return RatpackRetrofit
                .client(rpcConfig.getURI())
                .httpClient(httpClientFactory)
                .configure(b -> {
                    b.addConverterFactory(converterFactory);
                })
                .build(JsonRpcClient.class);
    }

    /**
     * Return an HttpClient Factory that will create a client that adds an Authorization header to HTTP requests
     *
     * @param rpcConfig JSON-RPC Configuration with username and password
     * @return A factory that will produce an HttpClient tthat sets the "Authorization" header
     */
    @Provides
    Factory<? extends HttpClient> authClientFactory(RpcConfig rpcConfig) {
        final String auth = authString(rpcConfig.getUsername(), rpcConfig.getPassword());
        return () -> clientFactory
                .create()
                .copyWith(
                        spec -> spec.requestIntercept(
                                requestSpec -> requestSpec.headers(
                                        mutableHeaders ->
                                                mutableHeaders.add("Authorization", auth)
                                )
                        )
                );
    }
    
    /**
     * HttpClient Factory borrowed from the private lambda in RatpackRetrofit.java
     */
    private Factory<? extends HttpClient> clientFactory = () -> {
        Execution exec = Execution.current();
        return exec
                .maybeGet(HttpClient.class)
                .orElseGet(() ->
                    exec.get(Context.class).get(HttpClient.class)
                );
    };

    private static String authString(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.encodeToString(auth.getBytes(),Base64.NO_WRAP).trim();
    }
}
