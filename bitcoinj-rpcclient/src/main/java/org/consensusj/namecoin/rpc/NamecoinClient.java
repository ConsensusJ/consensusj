package org.consensusj.namecoin.rpc;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import com.msgilligan.bitcoinj.rpc.bitcoind.AppDataDirectory;
import com.msgilligan.bitcoinj.rpc.bitcoind.BitcoinConfFile;
import org.consensusj.namecoin.core.NMCMainNetParams;
import org.consensusj.namecoin.pojo.NameData;
import org.bitcoinj.core.NetworkParameters;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Namecoin RPC client
 * See: https://wiki.namecoin.org/index.php?title=Client_API
 */
public class NamecoinClient extends BitcoinClient {
    public NamecoinClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword);
    }

    public NamecoinClient(RpcConfig config) {
        this(config.getNetParams(), config.getURI(), config.getUsername(), config.getPassword());
    }

    /**
     * name_show
     *
     * @param identifier namespace/name, e.g. 'd/beelin'
     * @return Data object
     * @throws IOException
     * @throws JsonRpcStatusException
     */
    public NameData nameShow(String identifier) throws IOException, JsonRpcStatusException {
        return send("name_show", NameData.class, identifier);
    }

    /**
     * Read Namecoin conf file. Namecoin conf file uses same format as bitcoin.conf
     * @return object containing RPC endpoint information
     */
    public static RpcConfig readConfig() {
        File file = new File(AppDataDirectory.forAppName("Namecoin"), "namecoin.conf");
        BitcoinConfFile conf = new BitcoinConfFile(file);
        RpcConfig config = conf.readWithFallback().getRPCConfig();
        // Since config is immutable we have to make a new one with NameCoin parameters
        config = new RpcConfig(NMCMainNetParams.get(),
                config.getURI(),
                config.getUsername(),
                config.getPassword());
        return config;
    }

}
