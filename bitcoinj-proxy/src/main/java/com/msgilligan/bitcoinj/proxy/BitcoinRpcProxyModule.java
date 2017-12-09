package com.msgilligan.bitcoinj.proxy;

import org.consensusj.jsonrpc.ratpack.JsonRpcClientModule;
import org.consensusj.jsonrpc.ratpack.RpcProxyHandler;

/**
 * Guice Module to create handlers and client for JSON-RPC Proxy
 * Adds a few handlers to the JsonRpcClientModule
 */
public class BitcoinRpcProxyModule extends JsonRpcClientModule {

    @Override
    protected void configure() {
        super.configure();
        bind(RpcProxyHandler.class);
        bind(ChainStatusHandler.class);
        bind(GenerateHandler.class);
    }
}
