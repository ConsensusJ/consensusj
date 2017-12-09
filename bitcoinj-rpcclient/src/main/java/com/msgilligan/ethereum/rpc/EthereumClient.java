package com.msgilligan.ethereum.rpc;

import org.consensusj.jsonrpc.JsonRPCStatusException;
import org.consensusj.jsonrpc.JsonRpcRequest;
import org.consensusj.jsonrpc.RPCClient;

import java.io.IOException;
import java.math.BigInteger;
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

    /**
     * Override to send JSON RPC version "2.0"
     *
     */
    @Override
    protected JsonRpcRequest buildJsonRequest(String method, List<Object> params) {
        return new JsonRpcRequest(method, params, JsonRpcRequest.JSON_RPC_VERSION_2);
    }

    public String ethProtocolVersion() throws IOException, JsonRPCStatusException {
        return this.send("eth_protocolVersion");
    }

    public long ethBlockNumber() throws IOException, JsonRPCStatusException {
        String blockNumString = this.send("eth_blockNumber");
        long blockNum = Long.decode(blockNumString);
        return blockNum;
    }

    public BigInteger ethGetBalance(String address) throws IOException, JsonRPCStatusException {
        String weiAsHexString = this.send("eth_getBalance", address);
        return quantityToInt(weiAsHexString);
    }

    public String ethCall(EthTxCallObject callObject) throws IOException, JsonRPCStatusException {
        String data = this.send("eth_call", callObject);
        return data;
    }

    public String web3ClientVersion() throws IOException, JsonRPCStatusException {
        return this.send("web3_clientVersion");
    }

    /**
     * Returns Keccak-256 (not the standardized SHA3-256) of the given data
     * @param dataToHash
     * @return
     * @throws IOException
     * @throws JsonRPCStatusException
     */
    public String web3Sha3(String dataToHash) throws IOException, JsonRPCStatusException {
        return this.send("web3_sha3", dataToHash);
    }

    public boolean minerStart(int numberOfThreads) throws IOException, JsonRPCStatusException {
        return this.send("miner_start", "0x" + Integer.toHexString(numberOfThreads));
    }

    public boolean minerStop() throws IOException, JsonRPCStatusException {
        return this.send("miner_stop");
    }

    private BigInteger quantityToInt(String weiAsHexString) {
        return new BigInteger(weiAsHexString.substring(2), 16);
    }
}
