package org.consensusj.bitcoin.services;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.consensusj.bitcoin.json.rpc.BitcoinJsonRpc;
import org.consensusj.bitcoinj.service.SignTransactionService;
import org.consensusj.bitcoinj.signing.DefaultSigningRequest;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.HDKeychainSigner;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.SigningUtils;
import org.consensusj.bitcoinj.signing.TestnetFeeCalculator;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.signing.TransactionOutputAddress;
import org.consensusj.bitcoinj.signing.TransactionOutputData;
import org.consensusj.bitcoinj.signing.Utxo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Transaction completion and signing service that uses a bitcoinj {@link Wallet}. This
 * service can find available UTXOs, build and sign transactions.
 */
public class WalletSigningService implements SignTransactionService {
    private final Wallet wallet;
    private final HDKeychainSigner signer;
    private final FeeCalculator feeCalculator = new TestnetFeeCalculator();

    public WalletSigningService(Wallet wallet) {
        this.wallet = wallet;
        signer = new HDKeychainSigner(wallet.getActiveKeyChain());
    }

    @Override
    public CompletableFuture<Transaction> signTransaction(SigningRequest request) {
        return signer.signTransaction(request);
    }

    /**
     * Create and sign a transaction to send coins to the specified address. Implements the transaction-building
     * and signing portion of `sendtoaddress` RPC.
     * @param toAddress destination address
     * @param amount amount to send
     * @return a future signed transaction
     */
    @Override
    public CompletableFuture<Transaction> signSendToAddress(Address toAddress, Coin amount) throws IOException, InsufficientMoneyException {
        List<TransactionInputData> utxos = getInputs();
        TransactionOutputData outputData = new TransactionOutputAddress(amount, toAddress);
        SigningRequest bitcoinSendReq = createBitcoinSigningRequest(wallet.network(), utxos, List.of(outputData), wallet.currentChangeAddress());
        return signer.signTransaction(bitcoinSendReq);
    }
    
    @Override
    public SigningRequest createBitcoinSigningRequest(Network network, List<? super TransactionInputData> inputUtxos, List<TransactionOutputData> outputs, Address changeAddress) throws InsufficientMoneyException {
        SigningRequest request = new DefaultSigningRequest(network, (List<TransactionInputData>) inputUtxos, outputs);
        // TODO: see Wallet.calculateFee
        return SigningUtils.addChange(request, changeAddress, feeCalculator);
    }

    List<TransactionInputData> getInputs() {
        List<TransactionOutput> spendCandidates = findUnspentOutputs(1, BitcoinJsonRpc.DEFAULT_MAX_CONF, List.of());
        List<? extends TransactionInputData> utxos  = spendCandidates.stream()
                .map(TransactionInputData::fromTxOut)
                .toList();
        return (List<TransactionInputData>) utxos;
    }

    /**
     */
    public Optional<Utxo.Complete> findUtxo(Utxo utxo) {
        List<TransactionOutput> candidates = wallet.calculateAllSpendCandidates();
        return candidates.stream()
                .filter(out -> out.getParentTransactionHash().equals(utxo.txId()) &&
                        out.getIndex() == utxo.index())
                .findFirst()
                .map(out -> new Utxo.Complete(out.getParentTransactionHash(),
                        out.getIndex(),
                        out.getValue(),
                        out.getScriptPubKey()));
    }

    /**
     * @param txId txid
     * @param vout output index
     * @return  list of matching transaction outputs (bitcoinj objects)
     */
    public Optional<TransactionOutput> findUnspentOutput(Sha256Hash txId, int vout) {
        return wallet.calculateAllSpendCandidates().stream()
                .filter(out -> out.getParentTransactionDepthInBlocks() >= 1 &&
                        out.getParentTransactionHash().equals(txId) &&
                        out.getIndex() == vout)
                .findFirst();
    }

    /**
     * @param minConf minimum confirmations
     * @param maxConf maximum confirmations
     * @param addresses List of wallet addresses these outputs should belong to (empty list is allowed matches all addresses)
     * @return  list of matching transaction outputs (bitcoinj objects)
     */
    List<TransactionOutput> findUnspentOutputs(int minConf, int maxConf, Collection<Address> addresses) {
        return wallet.calculateAllSpendCandidates().stream()
                .filter(out -> out.getParentTransactionDepthInBlocks() >= minConf &&
                                out.getParentTransactionDepthInBlocks() <= maxConf &&
                                matchesAddresses(out, addresses))
                .toList();
    }

    // empty collection is wildcard
    private boolean matchesAddresses(TransactionOutput out, Collection<Address> addresses) {
        return addresses.size() == 0 || addresses.stream().anyMatch(a -> matchesAddress(out, a));
    }

    private boolean matchesAddress(TransactionOutput out, Address address) {
        return out.getScriptPubKey().getToAddress(wallet.getParams()).equals(address);
    }
}
