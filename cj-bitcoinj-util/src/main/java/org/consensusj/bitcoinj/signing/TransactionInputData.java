package org.consensusj.bitcoinj.signing;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

/**
 * Immutable data for a transaction input.
 * See also {@link RawTransactionSigningRequest.RawInput} which can hold an incomplete transaction input, like
 * those sent in the {@code signtransactionwithwallet} JSON-RPC request.
 */
public interface TransactionInputData {
    Coin amount();

    /**
     * prevOut ScriptPubKey
     */

    Script script();

    /**
     * This probably shouldn't be here but is needed for proper operation with bitcoinj
     * @return A Transaction "outpoint" pointing to the utxo this input will spend.
     */
    TransactionOutPoint toOutPoint();

    Utxo toUtxo();

    /**
     * Create from a UTXO that paid to an address the intended signer can redeem
     */
    static TransactionInputData of(Utxo.Signable utxo, Address address) {
        return new TransactionInputDataUtxo(utxo.txId(), utxo.index(), utxo.amount(), ScriptBuilder.createOutputScript(address));
    }

    /**
     * Create from a UTXO that paid to a script the intended signer can redeem
     * @param utxo Complete UTXO info
     * @return input ready for signing
     */
    static TransactionInputData of(Utxo.Complete utxo) {
        return new TransactionInputDataUtxo(utxo.txId(), utxo.index(), utxo.amount(), utxo.scriptPubKey());
    }

    /**
     * Create an unsigned transaction input from an unspent transaction output that the signer can redeem
     * @param out An unspent transaction output
     * @return transaction input data
     */
    static TransactionInputData fromTxOut(TransactionOutput out) {
        return new TransactionInputDataUtxo(out.getParentTransactionHash(), out.getIndex(), out.getValue(), out.getScriptPubKey());
    }

    /**
     * Create an unsigned transaction input from an bitcoinj (unsigned) TransactionInput.
     * @param input A transaction input (typically/currently unsigned)
     * @return transaction input data
     */
    static TransactionInputData fromTxInput(TransactionInput input) {
        return new TransactionInputDataUtxo(
                input.getOutpoint().getHash(),
                (int) input.getOutpoint().getIndex(),
                input.getValue(),
                input.getScriptSig());
    }
}
