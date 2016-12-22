package com.msgilligan.bitcoinj.proxy;

import com.msgilligan.bitcoinj.rpc.RPCConfig;
import ratpack.guice.Guice;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;

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
                    .get(ctx -> ctx.getResponse().send("Hello world! (Not RPC)"))
                )
        );
    }
}
