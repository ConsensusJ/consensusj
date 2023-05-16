package org.consensusj.bitcoin.integ.services

import groovy.util.logging.Slf4j
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.params.BitcoinNetworkParams
import org.consensusj.bitcoin.json.pojo.UnspentOutput
import org.consensusj.bitcoin.services.WalletAppKitService
import org.consensusj.bitcoin.services.WalletSigningService
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoinj.signing.RawTransactionSigningRequest
import org.consensusj.bitcoinj.signing.SigningRequest
import org.consensusj.bitcoinj.signing.TransactionInputData
import org.consensusj.bitcoinj.signing.Utxo
import spock.lang.Shared

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * RegTest Integration test of WalletSigningService using WalletAppKitService
 */
@Slf4j
class WalletSigningServiceRegTestSpec extends BaseRegTestSpec {
    /** The WalletAppKitService that provides UTXOs for testing */
    @Shared WalletAppKitService appKitService
    @Shared WalletSigningService signingService;
    @Shared Address spvWalletAddress
    @Shared UnspentOutput unspentOutput
    
    def setupSpec() {
        appKitService = WalletAppKitService.createTemporary(BitcoinNetwork.REGTEST, ScriptType.P2PKH, "cj-btc-services-unittest")
        appKitService.start()
        signingService = appKitService.signingService   // access private member using Groovy reflection
        assert appKitService.network() == BitcoinNetwork.REGTEST
        spvWalletAddress = appKitService.getnewaddress().join()
        log.info("spvWalletAddress: {}", spvWalletAddress)

        var txId = requestBitcoin(spvWalletAddress, 2.btc)
        assert txId != null
        generateBlocks(1)
        waitForWalletSync()

        var balance = appKitService.getbalance().join()
        assert balance >= 2.btc

        var list = appKitService.listunspent(null, null, List.of(spvWalletAddress.toString()), false).join()
        unspentOutput = list.get(0)
    }

    def "serialize, deserialize and sign a transaction - similar to sendrawtransactionwithwallet"() {
        given: "a UTXO to spend, a toAddress, and a desired amount to send"
        var utxo = Utxo.of(unspentOutput.txid, unspentOutput.vout, unspentOutput.amount, unspentOutput.scriptPubKey)
        var toAddr = new ECKey().toAddress(ScriptType.P2PKH, network)
        var sendAmount = 1.5.btc

        when: "we build an unsigned tx via a SigningRequest"
        var changeAmount = utxo.amount() - (sendAmount + 0.1.btc)
        var signingRequest = SigningRequest.of(network,
                [TransactionInputData.of(utxo)],
                [(toAddr): sendAmount,
                 (spvWalletAddress): changeAmount]  // Change
        )
        var uTx = signingRequest.toUnsignedTransaction()

        then: "it is correctly built"
        uTx.getOutputSum() >= 0.9.btc

        when: "we serialize it"
        byte[] serialized = uTx.bitcoinSerialize()
        var buf = ByteBuffer.wrap(serialized).order(ByteOrder.LITTLE_ENDIAN)

        then: "it is serialized properly"
        buf.getInt() == 1                  // Version
        buf.get()    == (byte) 1           // Input count
        // input 1
        // TODO: simple parsing and testing illustrating raw buffer format

        // TODO: Maybe we need more an INPUT BUILDER more than a transaction builder!!


        when: "we deserialize it"
        var raw = ByteBuffer.wrap(serialized);
        var dTx = new Transaction(NetworkParameters.of(network), raw)
        var req = RawTransactionSigningRequest.ofTransaction(network, dTx)

        then: "it has all the information we need to prepare it for signing"
        req != null

        when: "we complete it"
        var in0 = req.inputs().get(0)
        var dUtxo = signingService.findUtxo(in0.toUtxo()).get()
        var finalInput = TransactionInputData.of(dUtxo)
        var completeTx = SigningRequest.of(appKitService.network(), List.of(finalInput), req.outputs())
        var sTx = signingService.signTransaction(completeTx).join()

        then: "it is complete"
        sTx != null
   }

    /**
     * Wait for the bitcoinj wallet to sync with the Bitcoin Core blockchain
     */
    void waitForWalletSync() {
        int walletHeight, serverHeight
        while ( (walletHeight = appKitService.getblockcount().join()) < (serverHeight = client.getBlockCount()) ) {
            // TODO: Figure out a way to do this without polling and sleeping
            println "walletHeight < serverHeight: ${walletHeight} < ${serverHeight} -- Waiting..."
            Thread.sleep(100)
        }
        println "walletHeight: ${walletHeight}, serverHeight: ${serverHeight}"
    }
}
