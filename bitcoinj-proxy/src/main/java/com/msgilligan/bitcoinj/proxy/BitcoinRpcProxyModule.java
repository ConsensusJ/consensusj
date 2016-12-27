package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Provides;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import com.msgilligan.bitcoinj.proxy.authext.BasicAuthCallFactory;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.guice.ConfigurableModule;
import ratpack.retrofit.RatpackRetrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Guice Module to create handlers and client for JSON-RPC Proxy
 */
public class BitcoinRpcProxyModule extends ConfigurableModule<RPCConfig> {

    @Override
    protected void configure() {
        bind(RpcProxyHandler.class);
        bind(ChainStatusHandler.class);
        bind(GenerateHandler.class);
    }

    @Provides
    ObjectMapper provideObjectMapper(RPCConfig config) {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new RpcClientModule(config.getNetParams()))
                .registerModule(new RpcServerModule());
    }

    @Provides
    JacksonConverterFactory provideJacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Provides
    JsonRpcClient provideJsonRpcClient(JacksonConverterFactory converterFactory,
                                       okhttp3.Call.Factory callFactory,
                                       RPCConfig rpcConfig) {
        return RatpackRetrofit
                .client(rpcConfig.getURI())
                .configure(b -> {
                    b.addConverterFactory(converterFactory);
                    b.callFactory(callFactory);
                    })
                .build(JsonRpcClient.class);
    }

    @Provides
    okhttp3.Call.Factory provideCallFactory(RPCConfig config) {
        return new BasicAuthCallFactory(config.getUsername(), config.getPassword());
    }

}
