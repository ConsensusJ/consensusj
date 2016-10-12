package com.msgilligan.bitcoinj.rpc;

/**
 *  Marker interface that must be implemented by enums that declare
 *  known RPC method names
 */
public interface JSONRPCMethod {
    String name();
}
