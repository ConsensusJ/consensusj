package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import com.msgilligan.bitcoinj.json.conversion.RpcServerModule;
import com.msgilligan.bitcoinj.proxy.authext.BasicAuthCallFactory;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;
import ratpack.retrofit.RatpackRetrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Guice Module to create handlers and client for JSON-RPC Proxy
 */
public class BitcoinRpcProxyModule extends AbstractModule {
    private static final String RPCServerURLName = "RPC Server URL";

    @Override
    protected void configure() {
        bind(NetworkParameters.class).toInstance(RegTestParams.get());
        bind(String.class)
                .annotatedWith(Names.named(RPCServerURLName))
                .toInstance("http://localhost:18332");
        bind(RpcProxyHandler.class);
        bind(ChainStatusHandler.class);
        bind(GenerateHandler.class);
    }

    @Provides
    RPCConfig provideRPCConfig(NetworkParameters netParams, @Named(RPCServerURLName) String serverURL) throws URISyntaxException {
        return new RPCConfig(netParams, new URI(serverURL), "bitcoinrpc", "pass");
    }

    @Provides
    ObjectMapper provideObjectMapper(NetworkParameters netParams) {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new RpcClientModule(netParams))
                .registerModule(new RpcServerModule());
    }

    @Provides
    JacksonConverterFactory provideJacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Provides
    JsonRpcClient provideJsonRpcClient(JacksonConverterFactory converterFactory,
                                       okhttp3.Call.Factory callFactory,
                                       @Named(RPCServerURLName) String serverURL) {
        return RatpackRetrofit
                .client(serverURL)
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
