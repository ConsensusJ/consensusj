package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;

import java.util.Objects;

/**
 *  UTXO info for transaction signing. Immutable and all fields non-nullable.
 *  <p>Non-segwit can be signed without amount, but we're focusing on modern Bitcoin, so
 *  we're going to simplify things by making {@link Utxo.Signable} mandatory in many methods.
 */
public interface Utxo {
    Sha256Hash txId();
    int index();

    static Basic of(Sha256Hash txId, int index) {
        return new Basic(txId, index);
    }

    static Signable of(Sha256Hash txId, int index, Coin amount) {
        return new Signable(txId, index, amount);
    }

    static Complete of(Sha256Hash txId, int index, Coin amount, Script scriptPubKey) {
        return new Complete(txId, index, amount, scriptPubKey);
    }

    static Utxo of(TransactionOutPoint outPoint) {
        if (outPoint.getConnectedOutput() == null || outPoint.getConnectedOutput().getValue() == null) {
            throw new IllegalArgumentException("outpoint is not complete enough: no value ");
        }
        if (outPoint.getConnectedOutput().getScriptPubKey() == null) {
            return new Signable(outPoint.getHash(), (int) outPoint.getIndex(), outPoint.getConnectedOutput().getValue());
        } else {
            return new Signable(outPoint.getHash(), (int) outPoint.getIndex(), outPoint.getConnectedOutput().getValue());
        }
    }

    /**
     * Bare minimum UTXO info: txId and index
     */
    class Basic implements Utxo {
        private final Sha256Hash txId;
        private final int index;

        public Basic(Sha256Hash txId, int index) {
            this.txId = Objects.requireNonNull(txId);
            this.index = index;
        }

        @Override
        public Sha256Hash txId() {
            return txId;
        }

        @Override
        public int index() {
            return index;
        }

        Signable addAmount(Coin amount) {
            return Utxo.of(txId, index, amount);
        }
    }

    /**
     * UTXO with "amount" which is necessary for signing a segwit transaction.
     */
    class Signable implements Utxo {
        private final Sha256Hash txId;
        private final int index;
        private final Coin amount;

        public Signable(Sha256Hash txId, int index, Coin amount) {
            this.txId = Objects.requireNonNull(txId);
            this.index = index;
            this.amount = Objects.requireNonNull(amount);
        }

        @Override
        public Sha256Hash txId() {
            return txId;
        }

        @Override
        public int index() {
            return index;
        }

        public Coin amount() {
            return amount;
        }
    }

    /**
     * UTXO with {@code amount} and {@code scriptPubKey}. Everything needed to build a transaction input with
     * {@code scriptSig} to spend this output.
     */
    class Complete implements Utxo {
        private final Sha256Hash txId;
        private final int index;
        private final Coin amount;
        private final Script scriptPubKey;

        public Complete(Sha256Hash txId, int index, Coin amount, Script scriptPubKey) {
            this.txId = Objects.requireNonNull(txId);
            this.index = index;
            this.amount = Objects.requireNonNull(amount);
            this.scriptPubKey = Objects.requireNonNull(scriptPubKey);
        }

        @Override
        public Sha256Hash txId() {
            return txId;
        }

        @Override
        public int index() {
            return index;
        }

        public Coin amount() {
            return amount;
        }

        public Script scriptPubKey() {
            return scriptPubKey;
        }
    }
}
