package com.msgilligan.bitcoinj.rpc.bitcoind;

import com.msgilligan.bitcoinj.rpc.RPCConfig;
import org.bitcoinj.params.MainNetParams;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Bitcoin configuration from bitcoin.conf
 * First cut is just a map, but future versions may be strongly typed
 */
public class BitcoinConf extends HashMap<String, String> {

    public RPCConfig getRPCConfig() {
        URI uri = null;
        try {
            uri = new URI("http://" + get("rpcconnect") + ":" + get("rpcport"));
        } catch (URISyntaxException e) {
            try {
                uri = new URI("http://127.0.0.1:8332");
            } catch (URISyntaxException e1) {
                throw new RuntimeException("Error creating RPC URI", e1);
            }
        }
        // TODO: Determine MainNet, TestNet, or RegTest from contents of .conf file
        RPCConfig config = new RPCConfig(MainNetParams.get(), uri,
                get("rpcuser"), get("rpcpassword"));
        return config;
    }
}
