package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.NetworkParameters;

import java.util.List;

/**
 * Simple implementation of SigningRequest
 */
public class DefaultSigningRequest implements SigningRequest {
    private final NetworkParameters netParams;
    private final List<TransactionInputData> inputs;
    private final List<TransactionOutputData> outputs;


    public DefaultSigningRequest(String networkId, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this.netParams = NetworkParameters.fromID(networkId);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public String networkId() {
        return netParams.getId();
    }

    @Override
    public List<TransactionInputData> inputs() {
        return inputs;
    }

    @Override
    public List<TransactionOutputData> outputs() {
        return outputs;
    }
}
