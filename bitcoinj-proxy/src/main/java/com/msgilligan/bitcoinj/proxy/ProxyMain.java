package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import com.msgilligan.bitcoinj.rpc.RPCConfig;
import org.bitcoinj.params.RegTestParams;
import ratpack.guice.Guice;
import ratpack.retrofit.RatpackRetrofit;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Main class for a Bitcoin RPC reverse proxy server
 */
public class ProxyMain {
    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
            .serverConfig(config -> config
                            .port(5050)
                            .baseDir(BaseDir.find())
                            .json("proxy-config.json")
                            .require("/rpcclient", RPCConfig.class))
            .registry(Guice.registry(b -> b.module(BitcoinRpcProxyModule.class)))
            .handlers(chain -> chain
                    .post("rpc", RpcProxyHandler.class)
                    .get("status", ChainStatusHandler.class)
                    .get("gen", GenerateHandler.class)
                    .get(context -> context.getResponse().send("Hello world! (Not RPC)"))
                )
        );
    }
}
