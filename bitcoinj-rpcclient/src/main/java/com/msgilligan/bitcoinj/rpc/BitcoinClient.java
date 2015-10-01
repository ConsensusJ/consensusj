package com.msgilligan.bitcoinj.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msgilligan.bitcoinj.json.conversion.HexUtil;
import com.msgilligan.bitcoinj.json.pojo.ChainTip;
import com.msgilligan.bitcoinj.json.pojo.Outpoint;
import com.msgilligan.bitcoinj.json.pojo.ReceivedByAddressInfo;
import com.msgilligan.bitcoinj.json.pojo.ServerInfo;
import com.msgilligan.bitcoinj.json.pojo.SignedRawTransaction;
import com.msgilligan.bitcoinj.json.pojo.TxOutInfo;
import com.msgilligan.bitcoinj.json.pojo.UnspentOutput;
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * = JSON-RPC Client for *Bitcoin Core*
 *
 * A strongly-typed wrapper for the
 * https://bitcoin.org/en/developer-reference#bitcoin-core-apis[Bitcoin Core JSON-RPC API].
 * *bitcoinj* types are used where appropriate.
 * For example, requesting a block hash will return a {@link org.bitcoinj.core.Sha256Hash}:
 *
 * [source,java]
 * --
 * Sha256Hash hash = client.getBlockHash(342650);
 * --
 *
 * Requesting a Bitcoin balance will return the amount as a {@link org.bitcoinj.core.Coin}:
 * [source,java]
 * --
 * Coin balance = client.getBalance();
 * --
 *
 * NOTE: This is still a work-in-progress and the API will change. High on the priority list is making
 * better use of https://github.com/FasterXML/jackson[Jackson] to replace some of the current `Map`-based types.
 *
 */
public class BitcoinClient extends RPCClient {
    private static final Logger log = LoggerFactory.getLogger(BitcoinClient.class);

    private static final int SECOND_IN_MSEC = 1000;
    private static final int RETRY_SECONDS = 1;
    private static final int MESSAGE_SECONDS = 10;

    protected final Context context;

    /**
     * Construct a BitcoinClient from URI, user name, and password.
     * @param server URI of the Bitcoin RPC server
     * @param rpcuser Username (if required)
     * @param rpcpassword Password (if required)
     */
    public BitcoinClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword);
        this.context = new Context(RegTestParams.get());
        mapper.registerModule(new RpcClientModule(context.getParams()));
    }

    /**
     * Construct a BitcoinClient from an RPCConfig data object.
     * @param config Contains URI, user name, and password
     */
    public BitcoinClient(RPCConfig config) {
        this(config.getURI(), config.getUsername(), config.getPassword());
    }

    /**
     * Wait until the server is available.
     *
     * Keep trying, ignoring (and logging) a known list of exception conditions that may occur while waiting for
     * a `bitcoind` server to start up. This is similar to the behavior enabled by the `-rpcwait`
     * option to the `bitcoin-cli` command-line tool.
     *
     * @param timeout Timeout in seconds
     * @return true if ready, false if timeout
     */
    public Boolean waitForServer(int timeout) throws JsonRPCException {

        log.debug("Waiting for server RPC ready...");

        String status;          // Status message for logging
        String statusLast = null;
        int seconds = 0;
        while (seconds < timeout) {
            try {
                Integer block = this.getBlockCount();
                if (block != null) {
                    log.debug("RPC Ready.");
                    return true;
                }
                status = "getBlock returned null";
            } catch (SocketException se) {
                // These are expected exceptions while waiting for a server
                if (se.getMessage().equals("Unexpected end of file from server") ||
                        se.getMessage().equals("Connection reset") ||
                        se.getMessage().equals("Connection refused") ||
                        se.getMessage().equals("recvfrom failed: ECONNRESET (Connection reset by peer)")) {
                    status = se.getMessage();
                } else {
                    throw new JsonRPCException("Unexpected exception in waitForServer", se) ;
                }

            } catch (java.io.EOFException ignored) {
                /* Android exception, ignore */
                // Expected exceptions on Android, RoboVM
                status = ignored.getMessage();
            } catch (IOException e) {
                status = e.getMessage();
            } catch (JsonRPCStatusException e) {
                // If server is in "warmup" mode, e.g. validating/parsing the blockchain...
                if (e.jsonRPCCode == -28) {
                    // ...then grab text message for status logging
                    status = e.getMessage();
                } else {
                    throw e;
                }
            } catch (JsonRPCException e) {
                    throw e;
            }
            try {
                // Log status messages only once, if new or updated
                if (!status.equals(statusLast)) {
                    log.info("RPC Status: " + status);
                    statusLast = status;
                }
                Thread.sleep(RETRY_SECONDS * SECOND_IN_MSEC);
                seconds += RETRY_SECONDS;
            } catch (InterruptedException e) {
                log.error(e.toString());
            }
        }

        log.error("waitForServer() timed out after {} seconds.", timeout);
        return false;
    }

    /**
     * Wait for RPC server to reach specified block height.
     *
     * @param blockHeight Block height to wait for
     * @param timeout     Timeout in seconds
     * @return True if blockHeight reached, false if timeout
     */
    public Boolean waitForBlock(int blockHeight, int timeout) throws JsonRPCException, IOException {

        log.info("Waiting for server to reach block " + blockHeight);

        int seconds = 0;
        while (seconds < timeout) {
            Integer block = this.getBlockCount();
            if (block >= blockHeight) {
                log.info("Server is at block " + block + " returning 'true'.");
                return true;
            } else {
                try {
                    if (seconds % MESSAGE_SECONDS == 0) {
                        log.debug("Server at block " + block);
                    }
                    Thread.sleep(RETRY_SECONDS * SECOND_IN_MSEC);
                    seconds += RETRY_SECONDS;
                } catch (InterruptedException e) {
                    log.error(e.toString());
                }
            }
        }

        log.error("Timeout waiting for block " + blockHeight);
        return false;
    }

    /**
     * Returns the number of blocks in the longest block chain.
     *
     * @return The current block count
     */
    public Integer getBlockCount() throws JsonRPCException, IOException {
        return send("getblockcount", null);
    }

    /**
     * Returns the hash of block in best-block-chain at index provided.
     *
     * @param index The block index
     * @return The block hash
     */
    public Sha256Hash getBlockHash(Integer index) throws JsonRPCException, IOException {
        List<Object> params = createParamList(index);
        return send("getblockhash", params, Sha256Hash.class);
    }

    /**
     * Returns information about a block with the given block hash.
     *
     * @param hash The block hash
     * @return The information about the block
     */
    public Map<String, Object> getBlock(Sha256Hash hash) throws JsonRPCException, IOException {
        // Use "verbose = true"
        List<Object> params = createParamList(hash, true);
        Map<String, Object> json = send("getblock", params);
        return json;
    }

    public Block getRawBlock(Sha256Hash hash) throws JsonRPCException, IOException {
        // Use "verbose = false"
        List<Object> params = createParamList(hash, false);
        Block block = send("getblock", params, Block.class);
        return block;
    }

    /**
     * Returns information about a block at index provided.
     *
     * @param index The block index
     * @return The information about the block
     */
    public Block getBlock(Integer index) throws JsonRPCException, IOException {
        Sha256Hash blockHash = getBlockHash(index);
        return getRawBlock(blockHash);
    }

    /**
     * Turn generation on/off or, if in RegTest mode, generate blocks
     *
     * @param generate        turn generation on or off
     * @param genproclimit    Generation is limited to [genproclimit] processors, -1 is unlimited
     *                        in regtest mode genproclimit is number of blocks to generate immediately
     * @return List<Sha256Hash>  Bitcoin 0.10.0+: list containing  block header hashes of the generated blocks or empty list
     *                        if no blocks were generated
     *                        Bitcoin 0.9.x: Empty list
     *
     */
    public List<Sha256Hash> setGenerate(Boolean generate, Long genproclimit) throws JsonRPCException, IOException {
        List<Object> params = createParamList(generate, genproclimit);
        // TODO: When we drop support for Bitcoin 0.9.x we can use automatic Jackson mapping here
        List<Sha256Hash> hashes = new ArrayList<Sha256Hash>();
        Object result = send("setgenerate", params);
        if (result != null) {
            List<String> stringList = (List<String>) result;
            for (String hashString : stringList) {
                hashes.add(Sha256Hash.wrap(hashString));
            }
        }
        return hashes;
    }

    /**
     * Convenience method for generating a single block when in RegTest mode
     * @see BitcoinClient#generateBlocks(Long blocks)
     */
    public List<Sha256Hash> generateBlock() throws JsonRPCException, IOException {
        return generateBlocks(1L);
    }

    /**
     * Convenience method for generating blocks when in RegTest mode
     *
     * @param blocks number of blocks to generate
     * @see BitcoinClient#setGenerate(Boolean, Long)
     */
    public List<Sha256Hash> generateBlocks(Long blocks) throws JsonRPCException, IOException {
        return setGenerate(true, blocks);
    }

    /**
     * Creates a new Bitcoin address for receiving payments, linked to the default account "".
     *
     * @return A new Bitcoin address
     */
    public Address getNewAddress() throws JsonRPCException, IOException {
        return getNewAddress(null);
    }

    /**
     * Creates a new Bitcoin address for receiving payments.
     *
     * @param account The account name for the address to be linked to.
     * @return A new Bitcoin address
     */
    public Address getNewAddress(String account) throws JsonRPCException, IOException {
        List<Object> params = createParamList(account);
        Address address = send("getnewaddress", params, Address.class);
        return address;
    }

    /**
     * Returns the Bitcoin address linked to the given account.
     *
     * @param account The account name linked to the address.
     * @return The Bitcoin address
     */
    public Address getAccountAddress(String account) throws JsonRPCException, IOException {
        List<Object> params = createParamList(account);
        Address address = send("getaccountaddress", params, Address.class);
        return address;
    }

    /**
     * Return the private key from the server.
     *
     * Note: must be in wallet mode with unlocked or unencrypted wallet.
     *
     * @param address Address corresponding to the private key to return
     * @return The private key
     */
    public ECKey dumpPrivKey(Address address) throws IOException, JsonRPCStatusException {
        List<Object> params = createParamList(address);
        ECKey key = send("dumpprivkey", params, ECKey.class);
        return key;
    }

    /**
     * Move a specified amount from one account in your wallet to another.
     *
     * @param fromaccount The name of the account to move funds from, which may be the default account using ""
     * @param toaccount   The name of the account to move funds to, which may be the default account using ""
     * @param amount      The amount to move
     * @param minconf     Only use funds with at least this many confirmations
     * @param comment     An optional comment, stored in the wallet only
     * @return True, if successful, and false otherwise
     */
    public Boolean moveFunds(Address fromaccount, Address toaccount, Coin amount, Integer minconf, String comment)
            throws JsonRPCException,
            IOException {
        List<Object> params = createParamList(fromaccount, toaccount, amount, minconf, comment);
        return send("move", params);
    }

    /**
     * Creates a raw transaction spending the given inputs to the given destinations.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param inputs  The outpoints to spent
     * @param outputs The destinations and amounts to transfer
     * @return The hex-encoded raw transaction
     */
    public String createRawTransaction(List<Outpoint> inputs, Map<Address, Coin> outputs)
            throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList(inputs, outputs);
        String transactionHex = send("createrawtransaction", params);
        return transactionHex;
    }

    /**
     * Signs inputs of a raw transaction.
     *
     * @param unsignedTransaction The hex-encoded raw transaction
     * @return The signed transaction and information whether it has a complete set of signature
     */
    public SignedRawTransaction signRawTransaction(String unsignedTransaction) throws IOException, JsonRPCException {
        List<Object> params = createParamList(unsignedTransaction);
        SignedRawTransaction signedTransaction = send("signrawtransaction", params, SignedRawTransaction.class);
        return signedTransaction;
    }

    public Object getRawTransaction(Sha256Hash txid, Boolean verbose) throws JsonRPCException, IOException {
        Object result;
        if (verbose) {
            result = getRawTransactionMap(txid);    // Verbose means JSON
        } else {
            result = getRawTransaction(txid);  // Not-verbose is bitcoinj Transaction
        }
        return result;
    }

    /**
     *  Return a BitcoinJ Transaction type
     */
    public Transaction getRawTransaction(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid);
        String hexEncoded = send("getrawtransaction", params);
        byte[] raw = HexUtil.hexStringToByteArray(hexEncoded);
        // Hard-code RegTest for now
        // TODO: All RPC client connections should have a BitcoinJ NetworkParameters object?
        Transaction tx = new Transaction(RegTestParams.get(), raw);
        return tx;
    }

    /* TODO: Return a stronger type than an a Map? Or maybe we always/only return bitcoinj Transaction? */
    public Map<String, Object> getRawTransactionMap(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid, 1);
        Map<String, Object> json = send("getrawtransaction", params);
        return json;
    }

    public Sha256Hash sendRawTransaction(Transaction tx) throws JsonRPCException, IOException {
        return sendRawTransaction(tx, null);
    }

    public Sha256Hash sendRawTransaction(String hexTx) throws JsonRPCException, IOException {
        return sendRawTransaction(hexTx, null);
    }

    public Sha256Hash sendRawTransaction(Transaction tx, Boolean allowHighFees) throws JsonRPCException, IOException {
        List<Object> params = createParamList(tx, allowHighFees);
        Sha256Hash hash = send("sendrawtransaction", params, Sha256Hash.class);
        return hash;
    }

    public Sha256Hash sendRawTransaction(String hexTx, Boolean allowHighFees) throws JsonRPCException, IOException {
        List<Object> params = createParamList(hexTx, allowHighFees);
        Sha256Hash hash = send("sendrawtransaction", params, Sha256Hash.class);
        return hash;
    }

    public Coin getReceivedByAddress(Address address) throws JsonRPCException, IOException {
        return getReceivedByAddress(address, 1);   // Default to 1 or more confirmations
    }

    /**
     * get total amount received by an address.
     *
     * @param address Address to query
     * @param minConf minimum number of confirmations
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     * @throws JsonRPCException
     * @throws IOException
     */
    public Coin getReceivedByAddress(Address address, Integer minConf) throws JsonRPCException, IOException {
        List<Object> params = createParamList(address, minConf);
        return send("getreceivedbyaddress", params, Coin.class);
    }

    public List<ReceivedByAddressInfo> listReceivedByAddress(Integer minConf, Boolean includeEmpty)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(minConf, includeEmpty);
        List<ReceivedByAddressInfo> addresses = send("listreceivedbyaddress", params,
                mapper.getTypeFactory().constructCollectionType(List.class, ReceivedByAddressInfo.class));
        return addresses;
    }

    /**
     * Returns a list of unspent transaction outputs with at least one confirmation.
     *
     * @return The unspent transaction outputs
     */
    public List<UnspentOutput> listUnspent() throws JsonRPCException, IOException {
        return listUnspent(null, null, null);
    }

    /**
     * Returns a list of unspent transaction outputs with at least {@code minConf} and not more than {@code maxConf}
     * confirmations.
     *
     * @param minConf The minimum confirmations to filter
     * @param maxConf The maximum confirmations to filter
     * @return The unspent transaction outputs
     */
    public List<UnspentOutput> listUnspent(Integer minConf, Integer maxConf)
            throws JsonRPCException, IOException {
        return listUnspent(minConf, maxConf, null);
    }

    /**
     * Returns a list of unspent transaction outputs with at least {@code minConf} and not more than {@code maxConf}
     * confirmations, filtered by a list of addresses.
     *
     * @param minConf The minimum confirmations to filter
     * @param maxConf The maximum confirmations to filter
     * @param filter  Include only transaction outputs to the specified addresses
     * @return The unspent transaction outputs
     */
    public List<UnspentOutput> listUnspent(Integer minConf, Integer maxConf, Iterable<Address> filter)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(minConf, maxConf, filter);
        List<UnspentOutput> unspent = send("listunspent", params,
                mapper.getTypeFactory().constructCollectionType(List.class, UnspentOutput.class));
        return unspent;
    }

    /**
     * Returns details about an unspent transaction output.
     *
     * @param txid The transaction hash
     * @param vout The transaction output index
     * @return Details about an unspent output or nothing, if the output was already spent
     */
    public TxOutInfo getTxOut(Sha256Hash txid, Integer vout) throws JsonRPCException, IOException {
        return getTxOut(txid, vout, null);
    }

    /**
     * Returns details about an unspent transaction output.
     *
     * @param txid              The transaction hash
     * @param vout              The transaction output index
     * @param includeMemoryPool Whether to included the memory pool
     * @return Details about an unspent output or nothing, if the output was already spent
     */
    public TxOutInfo getTxOut(Sha256Hash txid, Integer vout, Boolean includeMemoryPool)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid, vout, includeMemoryPool);
        TxOutInfo txout = send("gettxout", params, TxOutInfo.class);
        return txout;
    }

    /**
     * Get the balance for a the default Bitcoin "account"
     *
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     */
    public Coin getBalance() throws JsonRPCException, IOException {
        return getBalance(null, null);
    }

    /**
     * Get the balance for a Bitcoin "account"
     *
     * @param account A Bitcoin "account". (Be wary of using this feature.)
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     */
    public Coin getBalance(String account) throws JsonRPCException, IOException {
        return getBalance(account, null);
    }

    /**
     * Get the balance for a Bitcoin "account"
     *
     * @param account A Bitcoin "account". (Be wary of using this feature.)
     * @param minConf minimum number of confirmations
     * @return Is now returning `Coin`, if you need to convert use `BitcoinMath.btcToCoin(result)`
     */
    public Coin getBalance(String account, Integer minConf) throws JsonRPCException, IOException {
        List<Object> params = createParamList(account, minConf);
        return send("getbalance", params, Coin.class);
    }

    public Sha256Hash sendToAddress(Address address, Coin amount) throws JsonRPCException, IOException {
        return sendToAddress(address, amount, null, null);
    }

    public Sha256Hash sendToAddress(Address address, Coin amount, String comment, String commentTo)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(address, amount, comment, commentTo);
        return send("sendtoaddress", params, Sha256Hash.class);
    }

    public Sha256Hash sendFrom(String account, Address address, Coin amount)
            throws JsonRPCException, IOException {
        List<Object> params = createParamList(account, address, amount);
        return send("sendfrom", params, Sha256Hash.class);
    }

    public Sha256Hash sendMany(String account, Map<Address, Coin> amounts) throws JsonRPCException, IOException {
        List<Object> params = Arrays.asList(account, amounts);
        return send("sendmany", params, Sha256Hash.class);
    }

    /**
     * Set the transaction fee per kB.
     *
     * @param amount The transaction fee in BTC/kB rounded to the nearest 0.00000001.
     * @return True if successful
     */
    public Boolean setTxFee(Coin amount) throws JsonRPCException, IOException {
        List<Object> params = createParamList(amount);
        return send("settxfee", params);
    }

    public Map<String, Object> getTransaction(Sha256Hash txid) throws JsonRPCException, IOException {
        List<Object> params = createParamList(txid);
        Map<String, Object> transaction = send("gettransaction", params);
        return transaction;
    }

    public ServerInfo getInfo() throws JsonRPCException, IOException {
        ServerInfo result = send("getinfo", null, ServerInfo.class);
        return result;
    }

    /**
     * Returns a human readable list of available commands.
     * <p>
     * Bitcoin Core 0.9 returns an alphabetical list of commands, and Bitcoin Core 0.10 returns a categorized list of
     * commands.
     *
     * @return The list of commands as string
     */
    public String help() throws JsonRPCException, IOException {
        return help(null);
    }

    /**
     * Returns helpful information for a specific command.
     *
     * @param command The name of the command to get help for
     * @return The help text
     */
    public String help(String command) throws JsonRPCException, IOException {
        List<Object> params = createParamList(command);
        return send("help", params);
    }

    /**
     * Returns a list of available commands.
     *
     * Commands which are unavailable will not be listed, such as wallet RPCs, if wallet support is disabled.
     *
     * @return The list of commands
     */
    public List<String> getCommands() throws JsonRPCException, IOException {
        List<String> commands = new ArrayList<String>();
        for (String entry : help().split("\n")) {
            if (!entry.isEmpty() && !entry.matches("== (.+) ==")) {
                String command = entry.split(" ")[0];
                commands.add(command);
            }
        }
        return commands;
    }

    /**
     * Checks whether a command exists.
     *
     * This is done indirectly, by using {help(String) help} to get information about the command, and if information
     * about the command is available, then the command exists. The absence of information does not necessarily imply
     * the non-existence of a command.
     *
     * @param command The name of the command to check
     * @return True if the command exists
     */
    public Boolean commandExists(String command) throws JsonRPCException, IOException {
        return !help(command).contains("help: unknown command");
    }

    /**
     * Permanently marks a block as invalid, as if it violated a consensus rule.
     *
     * @param hash The block hash
     * @since Bitcoin Core 0.10
     */
    public void invalidateBlock(Sha256Hash hash) throws JsonRPCException, IOException {
        List<Object> params = createParamList(hash);
        send("invalidateblock", params);
    }

    /**
     * Removes invalidity status of a block and its descendants, reconsider them for activation.
     * <p>
     * This can be used to undo the effects of {link invalidateBlock(Sha256Hash) invalidateBlock}.
     *
     * @param hash The hash of the block to reconsider
     * @since Bitcoin Core 0.10
     */
    public void reconsiderBlock(Sha256Hash hash) throws JsonRPCException, IOException {
        List<Object> params = createParamList(hash);
        send("reconsiderblock", params);
    }

    /**
     * Return information about all known tips in the block tree, including the main chain as well as orphaned branches.
     *
     * @return A list of chain tip information
     * @since Bitcoin Core 0.10
     */
    public List<ChainTip> getChainTips() throws JsonRPCException, IOException {
        List<ChainTip> tips = send("getchaintips", null,
                mapper.getTypeFactory().constructCollectionType(List.class, ChainTip.class));
        return tips;
    }

    /**
     * Clears the memory pool and returns a list of the removed transactions.
     *
     * Note: this is a customized command, which is currently not part of Bitcoin Core.
     * See https://github.com/OmniLayer/OmniJ/pull/72[Pull Request #72] on GitHub
     *
     * @return A list of transaction hashes of the removed transactions
     */
    public List<Sha256Hash> clearMemPool() throws JsonRPCException, IOException {
        List<Sha256Hash> hashes = send("clearmempool", null,
                mapper.getTypeFactory().constructCollectionType(List.class, Sha256Hash.class));
        return hashes;
    }
}