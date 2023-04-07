package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;

import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of SigningRequest
 */
public class DefaultSigningRequest implements SigningRequest {
    private final Network network;
    private final List<TransactionInputData> inputs;
    private final List<TransactionOutputData> outputs;


    public DefaultSigningRequest(String networkId, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this(BitcoinNetwork.fromIdString(networkId).orElseThrow(IllegalArgumentException::new), inputs, outputs);
    }

    public DefaultSigningRequest(Network network, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this.network = network;
        this.inputs = Collections.unmodifiableList(inputs);
        this.outputs = Collections.unmodifiableList(outputs);
    }

    public DefaultSigningRequest(Network network, TransactionInputData input) {
        this(network, Collections.singletonList(input), Collections.emptyList());
    }

    public DefaultSigningRequest(Network network) {
        this(network, Collections.emptyList(), Collections.emptyList());
    }

    @Deprecated
    public DefaultSigningRequest(NetworkParameters netParams, List<TransactionInputData> inputs, List<TransactionOutputData> outputs) {
        this(netParams.network(), inputs, outputs);
    }

    @Deprecated
    public DefaultSigningRequest(NetworkParameters netParams, TransactionInputData input) {
        this(netParams, Collections.singletonList(input), Collections.emptyList());
    }

    @Deprecated
    public DefaultSigningRequest(NetworkParameters netParams) {
        this(netParams.network(), Collections.emptyList(), Collections.emptyList());
    }

    public DefaultSigningRequest(String networkId) {
        this(BitcoinNetwork.fromIdString(networkId).orElseThrow(IllegalArgumentException::new), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public String networkId() {
        return network.id();
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
