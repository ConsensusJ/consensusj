package org.consensusj.bitcoin.signing;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;

/**
 * Stupid simple fee calculator for testnet
 */
public class TestnetFeeCalculator implements FeeCalculator {

    @Override
    public Coin calculateFee(SigningRequest proposedTx) {
        long messageSize =  2048;   // TODO: Size calculation
        long fee = (messageSize * Transaction.DEFAULT_TX_FEE.toSat()) / 1024;
        return Coin.valueOf(fee);
    }
}
