package org.consensusj.bitcoin.rpc.bitcoind;

import org.consensusj.bitcoin.rpc.RpcConfig;
import org.consensusj.bitcoin.rpc.RpcURI;
import org.bitcoinj.core.NetworkParameters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Bitcoin configuration from bitcoin.conf
 * First cut is just a map, but future versions may be strongly typed
 */
public class BitcoinConf extends HashMap<String, String> {
    public final String rpcconnect = "rpconnect";
    public final String rpcport = "rpcport";
    public final String RPCCONNECT_DEFAULT = "localhost";
    public final String RPCPORT_DEFAULT = Integer.toString(RpcURI.RPCPORT_MAINNET);

    /**
     *  Create a BitcoinConf with a default RPCConfig configuration.
     */
    public BitcoinConf() {
        super();
        this.put(rpcconnect, RPCCONNECT_DEFAULT);
        this.put(rpcport, RPCPORT_DEFAULT);
    }

    public RpcConfig getRPCConfig() {
        URI uri;
        try {
            uri = new URI("http://" + get(rpcconnect) + ":" + get(rpcport));
        } catch (URISyntaxException e) {
            uri = RpcURI.getDefaultMainNetURI(); // TODO: Throw exception on failure, rather than fall back to default
        }
        String netWorkId = getNetworkId();

        RpcConfig config = new RpcConfig(netWorkId, uri, get("rpcuser"), get("rpcpassword"));
        return config;
    }

    private String getNetworkId() {
        String isTestNetString = get("testnet");
        String isRegTestString = get("regtest");
        boolean isTestNet = (isTestNetString != null && isTestNetString.equals("1"));
        boolean isRegTest = (isRegTestString != null && isRegTestString.equals("1"));

        if (isRegTest && isTestNet) {
            throw new RuntimeException("Invalid config file, both 'testnet' and 'regtest' are set!");
        }

        if (isRegTest) {
            return NetworkParameters.ID_REGTEST;
        } else if (isTestNet) {
            return NetworkParameters.ID_TESTNET;
        } else {
            return NetworkParameters.ID_MAINNET;
        }
    }
}
