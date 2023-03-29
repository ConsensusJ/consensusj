package org.consensusj.namecoin.jsonrpc;

import org.bitcoinj.base.Network;
import org.bitcoinj.utils.AppDataDirectory;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.jsonrpc.JsonRpcStatusException;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile;
import org.consensusj.namecoin.jsonrpc.core.NMCMainNetParams;
import org.consensusj.namecoin.jsonrpc.core.NameCoinNetwork;
import org.consensusj.namecoin.jsonrpc.pojo.NameData;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Namecoin RPC client
 * See: https://wiki.namecoin.org/index.php?title=Client_API
 */
public class NamecoinClient extends BitcoinClient {
    public NamecoinClient(Network network, URI server, String rpcuser, String rpcpassword) {
        super(network, server, rpcuser, rpcpassword);
    }

    public NamecoinClient(RpcConfig config) {
        this(config.network(), config.getURI(), config.getUsername(), config.getPassword());
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
        File file = AppDataDirectory.getPath("Namecoin").resolve("namecoin.conf").toFile();
        BitcoinConfFile conf = new BitcoinConfFile(file);
        RpcConfig config = conf.readWithFallback().getRPCConfig();
        // Since config is immutable we have to make a new one with NameCoin parameters
        config = new RpcConfig(NameCoinNetwork.MAINNET,
                config.getURI(),
                config.getUsername(),
                config.getPassword());
        return config;
    }

}
