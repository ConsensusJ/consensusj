package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.script.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A transaction signing request with immutable data specifying the transaction.
 * <p>
 * This is an experiment as to what immutable transactions might look like in <b>bitcoinj</b>.
 * At some point in the future I would like to propose some refactoring in bitcoinj to implement
 * immutable transactions in a mostly-compatible way with the existing transaction classes.
 */
public interface SigningRequest {
    String networkId();
    List<TransactionInputData> inputs();
    List<TransactionOutputData> outputs();


    default SigningRequest addInput(Address address, Coin amount, Sha256Hash txId, long index) {
        TransactionInputData in = new TransactionInputDataImpl(networkId(), txId, index, amount, address);
        return addInput(in);
    }

    default SigningRequest addInput(Script script, Coin amount, Sha256Hash txId, long index) {
        TransactionInputData in = new TransactionInputDataImpl(networkId(), txId, index, amount, script);
        return addInput(in);
    }

    default SigningRequest addInput(byte[] script, Coin amount, Sha256Hash txId, long index) {
        TransactionInputData in = new TransactionInputDataImpl(networkId(), txId, index, amount, script);
        return addInput(in);
    }

    default SigningRequest addInput(TransactionInputData input) {
        List<TransactionInputData> ins = append(inputs(), input);
        return new DefaultSigningRequest(networkId(), ins, outputs());
    }

    default SigningRequest addOutput(Address address, Coin amount) {
        List<TransactionOutputData> outs = append(outputs(),
                new TransactionOutputAddress(amount, address));
        return new DefaultSigningRequest(networkId(), inputs(), outs);
    }

    default SigningRequest addDustOutput(Address address) {
        TransactionOutputData test = new TransactionOutputAddress(Coin.ZERO, address);
        Coin dustAmount = SigningUtils.getMinNonDustValue(test);
        List<TransactionOutputData> testOuts = append(outputs(),
                new TransactionOutputAddress(dustAmount, address));
        return new DefaultSigningRequest(networkId(), inputs(), testOuts);
    }

    static <T> List<T> append(List<T> list, T element) {
        List<T> modifiable = new ArrayList<>(list);
        modifiable.add(element);
        return Collections.unmodifiableList(modifiable);
    }
}
