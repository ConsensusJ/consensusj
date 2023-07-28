package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.InsufficientMoneyException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public interface SigningUtils {
    /* private */ Coin MIN_NONDUST_OUTPUT = Coin.valueOf(546);
    static TransactionOutputData createDustOutput(Address address) {
        TransactionOutputData test = new TransactionOutputAddress(Coin.ZERO, address);
        Coin dustAmount = SigningUtils.getMinNonDustValue(test);
        return new TransactionOutputAddress(dustAmount, address);
    }

    static Coin getMinNonDustValue(TransactionOutputData data) {
        return MIN_NONDUST_OUTPUT.times(3);  // TODO: Fix this
    }

    static SigningRequest addChange(SigningRequest request, Address changeAddress, FeeCalculator calculator) throws InsufficientMoneyException {
        Coin fee = calculator.calculateFee(addOutput(request, changeAddress, Coin.ZERO));
        long change = sumInputSats(request.inputs()) - sumOutputSats(request.outputs()) - fee.value;
        if (change < 0) {
            throw new InsufficientMoneyException(Coin.ofSat(-change));
        }
        return (change > 0) ? addOutput(request, changeAddress, Coin.ofSat(change)) : request;
    }

    private static SigningRequest addOutput(SigningRequest req, Address address, Coin amount) {
        List<TransactionOutputData> outs = append(req.outputs(),
                new TransactionOutputAddress(amount, address));
        return SigningRequest.of(req.inputs(), outs);
    }

    private static <T> List<T> append(List<T> list, T element) {
        List<T> modifiable = new ArrayList<>(list);
        modifiable.add(element);
        return Collections.unmodifiableList(modifiable);
    }

    /**
     *
     * @param request
     * @return The fee that will be paid from a set of inputs and outputs
     */
    static Coin getFee(SigningRequest request) {
        return getFee(request.inputs(), request.outputs());
    }

    /**
     *
     * @param inputs
     * @param outputs
     * @return The fee that will be paid from a set of inputs and outputs
     */
    static Coin getFee(Collection<TransactionInputData> inputs, Collection<TransactionOutputData> outputs) {
        return Coin.ofSat(sumInputSats(inputs) - sumOutputSats(outputs));
    }

    /**
     * Calculate the total value of a collection of transaction inputs.
     *
     * @param inputs list of transaction outputs to total
     * @return total value in satoshis
     */
    static Coin sumInputs(Collection<TransactionInputData> inputs) {
        return Coin.ofSat(sumInputSats(inputs));
    }

    /**
     * Calculate the total value of a collection of transaction outputs.
     *
     * @param outputs list of transaction outputs to total
     * @return total value in satoshis
     */
    static Coin sumOutputs(Collection<TransactionOutputData> outputs) {
        return Coin.ofSat(sumOutputSats(outputs));
    }

    static /* private */ long sumInputSats(Collection<TransactionInputData> inputs) {
        return inputs.stream()
                .mapToLong(input -> input.amount().toSat())
                .sum();
    }

    static /* private */ long sumOutputSats(Collection<TransactionOutputData> outputs) {
        return outputs.stream()
                .mapToLong(output -> output.amount().toSat())
                .sum();
    }
}
