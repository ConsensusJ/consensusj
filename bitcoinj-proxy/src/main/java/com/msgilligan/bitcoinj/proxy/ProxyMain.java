package com.msgilligan.bitcoinj.proxy;

import com.msgilligan.bitcoinj.rpc.RPCConfig;
import org.consensusj.jsonrpc.ratpack.RpcProxyHandler;
import ratpack.guice.Guice;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

/**
 * Main class for a Bitcoin RPC reverse proxy server
 */
public class ProxyMain {
    public static void main(String... args) throws Exception {
        ServerConfig serverConfig = ServerConfig.of(config -> config
                .port(5050)
                .baseDir(BaseDir.find())
                .json("proxy-config.json")
                .require("/rpcclient", RPCConfig.class)
        );
        RatpackServer.start (server -> server
            .serverConfig(serverConfig)
            .registry(Guice.registry(b -> b.
                    moduleConfig(BitcoinRpcProxyModule.class,
                            serverConfig.get("/rpcclient", RPCConfig.class))))
            .handlers(chain -> chain
                    .post("rpc", RpcProxyHandler.class)
                    .get("status", ChainStatusHandler.class)
                    .get("gen", GenerateHandler.class)
                    .get(ctx -> ctx.getResponse().send("Hello world! (Not RPC)"))
                )
        );
    }
}
