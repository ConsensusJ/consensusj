package org.consensusj.ethereum.jsonrpc;

import org.consensusj.jsonrpc.DefaultRpcClient;
import org.consensusj.jsonrpc.JsonRpcMessage;
import org.consensusj.jsonrpc.JsonRpcStatusException;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;

/**
 * A partial implementation of an Ethereum RPC Client
 *
 * How to mine just a little for "reg test mode" in Eth:
 * https://github.com/ethereum/go-ethereum/wiki/bitchin-tricks
 * See also:
 * https://github.com/ethereum/go-ethereum/wiki/Management-APIs
 */
public class EthereumClient extends DefaultRpcClient {
    public static final URI DEFAULT_LOCALHOST = URI.create("http://localhost:8545");
    
    /**
     * Construct a JSON-RPC client from URI, username, and password
     *
     * @param server      server URI should not contain username/password
     * @param rpcuser     username for the RPC HTTP connection
     * @param rpcpassword password for the RPC HTTP connection
     */
    public EthereumClient(URI server, String rpcuser, String rpcpassword) {
        super(JsonRpcMessage.Version.V2, server, rpcuser, rpcpassword);
    }

    public EthereumClient() {
        this(DEFAULT_LOCALHOST, null, null);
    }
    
    public String ethProtocolVersion() throws IOException, JsonRpcStatusException {
        return this.send("eth_protocolVersion");
    }

    public long ethBlockNumber() throws IOException, JsonRpcStatusException {
        String blockNumString = this.send("eth_blockNumber");
        long blockNum = Long.decode(blockNumString);
        return blockNum;
    }

    public BigInteger ethGetBalance(String address, String block) throws IOException, JsonRpcStatusException {
        String weiAsHexString = this.send("eth_getBalance", address, block);
        return quantityToInt(weiAsHexString);
    }

    public String ethCall(EthTxCallObject callObject, String block) throws IOException, JsonRpcStatusException {
        String data = this.send("eth_call", callObject, block);
        return data;
    }

    public String web3ClientVersion() throws IOException, JsonRpcStatusException {
        return this.send("web3_clientVersion");
    }

    /**
     * Returns Keccak-256 (not the standardized SHA3-256) of the given data
     * @param dataToHash
     * @return Keccak-256 hash of the data
     * @throws IOException
     * @throws JsonRpcStatusException
     */
    public String web3Sha3(String dataToHash) throws IOException, JsonRpcStatusException {
        return this.send("web3_sha3", dataToHash);
    }

    public boolean minerStart(int numberOfThreads) throws IOException, JsonRpcStatusException {
        return this.send("miner_start", "0x" + Integer.toHexString(numberOfThreads));
    }

    public boolean minerStop() throws IOException, JsonRpcStatusException {
        return this.send("miner_stop");
    }

    private BigInteger quantityToInt(String weiAsHexString) {
        return new BigInteger(weiAsHexString.substring(2), 16);
    }
}
