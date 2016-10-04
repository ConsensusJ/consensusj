package com.msgilligan.bitcoinj.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import org.bitcoinj.params.RegTestParams;
import ratpack.server.RatpackServer;

/**
 * Main class for a Bitcoin RPC reverse proxy server
 * TODO: Allow configuration of bitcoind URI, NetParams, user, password
 */
public class ProxyMain {
    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                    .registryOf(r -> r
                            .add(new ObjectMapper()
                                    .registerModule(new Jdk8Module())
                                    .registerModule(new RpcClientModule(RegTestParams.get()))
                    ))
                    .handlers(chain -> chain
                            .post("rpc", new RpcProxyHandler())
                            .get("status", new ChainStatusHandler())
                            .get("gen", new GenerateHandler())
                            .get(context -> context.getResponse().send("Hello world! (Not RPC)"))
                )
        );
    }
}
