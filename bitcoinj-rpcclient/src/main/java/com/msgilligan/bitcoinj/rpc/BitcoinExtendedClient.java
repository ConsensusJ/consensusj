package com.msgilligan.bitcoinj.rpc;

import com.msgilligan.bitcoinj.json.pojo.Outpoint;
import com.msgilligan.bitcoinj.json.pojo.SignedRawTransaction;
import com.msgilligan.bitcoinj.json.pojo.UnspentOutput;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.RegTestParams;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * = Extended Bitcoin JSON-RPC Client with added convenience methods
 *
 * This class adds extra methods that aren't 1:1 mappings to standard
 * Bitcoin API RPC methods, but are useful for many common use cases -- specifically
 * the ones we ran into while building integration tests.
 */
public class BitcoinExtendedClient extends BitcoinClient {
    private final NetworkParameters netParams = RegTestParams.get();
    public final Coin stdTxFee = Coin.valueOf(10000);
    public final Coin stdRelayTxFee = Coin.valueOf(1000);
    public final Integer defaultMaxConf = 9999999;
    public final long stdTxFeeSatoshis = stdTxFee.getValue();

    public BitcoinExtendedClient(URI server, String rpcuser, String rpcpassword) {
        super(server, rpcuser, rpcpassword);
    }

    public BitcoinExtendedClient(RPCConfig config) {
        super(config);
    }

    /**
     * Creates a raw transaction, spending from a single address, whereby no new change address is created, and
     * remaining amounts are returned to {@code fromAddress}.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param fromAddress The source to spend from
     * @param outputs The destinations and amounts to transfer
     * @return The hex-encoded raw transaction
     */
    public String createRawTransaction(Address fromAddress, Map<Address, Coin> outputs) throws JsonRPCException, IOException {
        // Get unspent outputs via RPC
        List<UnspentOutput> unspentOutputs = listUnspent(0, defaultMaxConf, Collections.singletonList(fromAddress));

        // Gather inputs
        List<Outpoint> inputs = new ArrayList<>();
        for (UnspentOutput input : unspentOutputs) {
            inputs.add(new Outpoint(input.getTxid(), input.getVout()));
        }

        // Calculate change
        long amountIn = 0;
        long amountOut = 0;
        for (UnspentOutput it : unspentOutputs) {
            amountIn += it.getAmount().value;
        }
        for (Coin it : outputs.values()) {
            amountOut += it.value;
        }
        Coin amountChange = Coin.valueOf(amountIn - amountOut - stdTxFee.value);
        if (amountIn < (amountOut + stdTxFee.value)) {
            System.out.println("Insufficient funds"); // + ": ${amountIn} < ${amountOut + stdTxFee}"
        }
        // Copy the Map (which may be immutable) and add change output if needed.
        Map<Address,Coin> outputsWithChange = new HashMap<>(outputs);
        if (amountChange.value > 0) {
            outputsWithChange.put(fromAddress, amountChange);
        }

        return createRawTransaction(inputs, outputsWithChange);
    }

    /**
     * Creates a raw transaction, sending {@code amount} from a single address to a destination, whereby no new change
     * address is created, and remaining amounts are returned to {@code fromAddress}.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param fromAddress The source to spent from
     * @param toAddress The destination
     * @param amount The amount
     * @return The hex-encoded raw transaction
     */
    public String createRawTransaction(Address fromAddress, Address toAddress, Coin amount) throws JsonRPCException, IOException {
        Map<Address, Coin> outputs = Collections.singletonMap(toAddress, amount);
        return createRawTransaction(fromAddress, outputs);
    }



    /**
     * Returns the Bitcoin balance of an address.
     *
     * @param address The address
     * @return The balance
     */
    public Coin getBitcoinBalance(Address address) throws JsonRPCException, IOException {
        // NOTE: because null is currently removed from the argument lists passed via RPC, using it here for default
        // values would result in the RPC call "listunspent" with arguments [["address"]], which is invalid, similar
        // to a call with arguments [null, null, ["address"]], as expected arguments are either [], [int], [int, int]
        // or [int, int, array]
        return getBitcoinBalance(address, 1, defaultMaxConf);
    }

    /**
     * Returns the Bitcoin balance of an address where spendable outputs have at least {@code minConf} confirmations.
     *
     * @param address The address
     * @param minConf Minimum amount of confirmations
     * @return The balance
     */
    public Coin getBitcoinBalance(Address address, Integer minConf) throws JsonRPCException, IOException {
        return getBitcoinBalance(address, minConf, defaultMaxConf);
    }

    /**
     * Returns the Bitcoin balance of an address where spendable outputs have at least {@code minConf} and not more
     * than {@code maxConf} confirmations.
     *
     * @param address The address
     * @param minConf Minimum amount of confirmations
     * @param maxConf Maximum amount of confirmations
     * @return The balance
     */
    public Coin getBitcoinBalance(Address address, Integer minConf, Integer maxConf) throws JsonRPCException, IOException {
        long btcBalance = 0;
        List<UnspentOutput> unspentOutputs = listUnspent(minConf, maxConf, Collections.singletonList(address));

        for (UnspentOutput unspentOutput : unspentOutputs) {
            btcBalance += unspentOutput.getAmount().value;
        }

        return Coin.valueOf(btcBalance);
    }

    /**
     * Sends BTC from an address to a destination, whereby no new change address is created, and any leftover is
     * returned to the sending address.
     *
     * @param fromAddress The source to spent from
     * @param toAddress   The destination address
     * @param amount      The amount to transfer
     * @return The transaction hash
     */
    public Sha256Hash sendBitcoin(Address fromAddress, Address toAddress, Coin amount) throws JsonRPCException, IOException {
        Map<Address, Coin> outputs = Collections.singletonMap(toAddress, amount);
        return sendBitcoin(fromAddress, outputs);
    }

    /**
     * Sends BTC from an address to the destinations, whereby no new change address is created, and any leftover is
     * returned to the sending address.
     *
     * @param fromAddress The source to spent from
     * @param outputs     The destinations and amounts to transfer
     * @return The transaction hash
     */
    public Sha256Hash sendBitcoin(Address fromAddress, Map<Address, Coin> outputs) throws JsonRPCException, IOException {
        String unsignedTxHex = createRawTransaction(fromAddress, outputs);
        SignedRawTransaction signingResult = signRawTransaction(unsignedTxHex);

        Boolean complete = signingResult.isComplete();
        assert complete;

        String signedTxHex = signingResult.getHex();
        Sha256Hash txid = sendRawTransaction(signedTxHex);

        return txid;
    }

    public Transaction createSignedTransaction(ECKey fromKey, List<TransactionOutput> outputs) throws JsonRPCException, IOException {
        Address fromAddress = fromKey.toAddress(netParams);
        Transaction tx = new Transaction(netParams);

        List<TransactionOutput> unspentOutputs = listUnspentJ(fromAddress);

        // Add outputs to the transaction
        for (TransactionOutput it : outputs) {
            tx.addOutput(it);
        }

        // Calculate change (units are satoshis)
//        long amountIn     = (long) unspentOutputs.sum { TransactionOutput it -> it.value.longValue() }
        long amountIn = 0;
        for (TransactionOutput it : unspentOutputs) {
            amountIn += it.getValue().value;
        }
//        long amountOut    = (long) outputs.sum { TransactionOutput it -> it.value.longValue() }
        long amountOut = 0;
        for (TransactionOutput it : outputs) {
            amountOut += it.getValue().value;
        }
        long amountChange = amountIn - amountOut - stdTxFeeSatoshis;
        if (amountChange < 0) {
            // TODO: Throw Exception
            System.out.println("Insufficient funds"); // + ": ${amountIn} < ${amountOut + stdTxFeeSatoshis}"
        }
        if (amountChange > 0) {
            // Add a change output
            tx.addOutput(Coin.valueOf(amountChange), fromAddress);
        }

        // Add all UTXOs for fromAddress as inputs
        for (TransactionOutput it : unspentOutputs) {
            tx.addSignedInput(it, fromKey);
        }
        return tx;
    }

    public Transaction createSignedTransaction(ECKey fromKey, Address toAddress, Coin amount) throws JsonRPCException, IOException {
        List<TransactionOutput> outputs = Collections.singletonList(
                new TransactionOutput(netParams, null, amount, toAddress));
        return createSignedTransaction(fromKey, outputs);
    }

    /**
     * Build a list of bitcoinj <code>TransactionOutput</code>s using <code>listUnspent</code>
     * and <code>getRawTransaction</code> RPCs
     *
     * @param fromAddress Address to get UTXOs for
     * @return All unspent TransactionOutputs for fromAddress
     */
    public List<TransactionOutput> listUnspentJ(Address fromAddress) throws JsonRPCException, IOException {
        List<Address> addresses = Collections.singletonList(fromAddress);
        List<UnspentOutput> unspentOutputsRPC = listUnspent(0, defaultMaxConf, addresses); // RPC UnspentOutput objects
        List<TransactionOutput> unspentOutputsJ = new ArrayList<TransactionOutput>();
        for (UnspentOutput it : unspentOutputsRPC) {
            unspentOutputsJ.add(getRawTransaction(it.getTxid()).getOutput(it.getVout()));
        }
        return unspentOutputsJ;
    }

    public List<TransactionOutPoint> listUnspentOutPoints(Address fromAddress) throws JsonRPCException, IOException {
        List<Address> addresses = Collections.singletonList(fromAddress);
        List<UnspentOutput> unspentOutputsRPC = listUnspent(0, defaultMaxConf, addresses); // RPC UnspentOutput objects
        List<TransactionOutPoint> unspentOutPoints = new ArrayList<TransactionOutPoint>();
        for (UnspentOutput it : unspentOutputsRPC) {
            unspentOutPoints.add(new TransactionOutPoint(netParams, it.getVout(), it.getTxid()));
        }
        return unspentOutPoints;
    }
}
