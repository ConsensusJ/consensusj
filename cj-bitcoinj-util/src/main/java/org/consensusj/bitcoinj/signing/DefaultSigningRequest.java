package org.consensusj.bitcoinj.signing;

import org.bitcoinj.core.NetworkParameters;

import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of SigningRequest
 */
public class DefaultSigningRequest implements SigningRequest {
    private final NetworkParameters netParams;
    private final List<TransactionInputData> inputs;
    private final List<TransactionOutputData> outputs;


    public DefaultSigningRequest(String networkId, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this(NetworkParameters.fromID(networkId), inputs, outputs);
    }

    public DefaultSigningRequest(NetworkParameters netParams, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this.netParams = netParams;
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
    }

    public DefaultSigningRequest(NetworkParameters netParams, TransactionInputData input) {
        this(netParams, Collections.singletonList(input), Collections.emptyList());
    }

    public DefaultSigningRequest(NetworkParameters netParams) {
        this(netParams, Collections.emptyList(), Collections.emptyList());
    }

    public DefaultSigningRequest(String networkId) {
        this(NetworkParameters.fromID(networkId), Collections.emptyList(), Collections.emptyList());
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
