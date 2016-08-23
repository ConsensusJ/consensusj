package com.msgilligan.ethereum.rpc;

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException;
import com.msgilligan.bitcoinj.rpc.RPCClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * A partial implementation of an Ethereum RPC Client
 *
 * How to mine just a little for "reg test mode" in Eth:
 * https://github.com/ethereum/go-ethereum/wiki/bitchin-tricks
 * See also:
 * https://github.com/ethereum/go-ethereum/wiki/Management-APIs
 */
public class EthereumClient extends RPCClient {
    public static URI DEFAULT_LOCALHOST;

    static {
        try {
            DEFAULT_LOCALHOST = new URI("http://localhost:8545");
        } catch (URISyntaxException e) {
            DEFAULT_LOCALHOST = null;
        }
    }

    /**
     * Construct a JSON-RPC client from URI, username, and password
     *
     * @param server      server URI should not contain username/password
     * @param rpcuser     username for the RPC HTTP connection
     * @param rpcpassword password for the RPC HTTP connection
     */
    private EthereumClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword);
    }

    public EthereumClient() {
        super(DEFAULT_LOCALHOST, null, null);
    }

    public String ethProtocolVersion() throws IOException, JsonRPCStatusException {
        return this.send("eth_protocolVersion");
    }

    public long ethBlockNumber() throws IOException, JsonRPCStatusException {
        String blockNumString = this.send("eth_blockNumber");
        long blockNum = Long.decode(blockNumString);
        return blockNum;
    }

    public boolean minerStart(int numberOfThreads) throws IOException, JsonRPCStatusException {
        return this.send("miner_start", "0x" + Integer.toHexString(numberOfThreads));
    }

    public boolean minerStop() throws IOException, JsonRPCStatusException {
        return this.send("miner_stop");
    }

}
