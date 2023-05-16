package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: This probably shouldn't have the addXyz methods that create a new, modified instance.
// A SigningRequest should be immutable *and* complete/ready-to-sign. There should be some kind of SigningRequestBuilder
// to build a SigningRequest.
/**
 * A transaction signing request with immutable data specifying the transaction.
 * <p>
 * This is an experiment as to what immutable transactions might look like in <b>bitcoinj</b>.
 * At some point in the future I would like to propose some refactoring in bitcoinj to implement
 * immutable transactions in a mostly-compatible way with the existing transaction classes.
 */
public interface SigningRequest {
    /**
     * This property is only here because bitcoinj {@link org.bitcoinj.core.Transaction} currently requires
     * this information for construction. This will be removed in the future when bitcoinj is updated.
     * @return the id string for the network
     */
    @Deprecated
    String networkId();
    Network network();
    List<TransactionInputData> inputs();
    List<TransactionOutputData> outputs();

    static SigningRequest of(Network network, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        return new DefaultSigningRequest(network, inputs, outputs);
    }

    static SigningRequest of(Network network, List<TransactionInputData> inputs, Map<Address, Coin> outputMap) {
        List<TransactionOutputData> outs = outputMap.entrySet().stream()
                .map(e -> new TransactionOutputAddress(e.getValue(), e.getKey()))
                .collect(Collectors.toList());
        return SigningRequest.of(network, inputs, outs);
    }

    /**
     * bitcoinj signing (currently) uses mutable {@link Transaction} objects, this convenience method will create
     * a completed, unsigned bitcoinj {@code Transaction} if you want to use bitcoinj to sign this request.
     * @return an unsigned bitcoinj transaction
     */
    Transaction toUnsignedTransaction();
}
