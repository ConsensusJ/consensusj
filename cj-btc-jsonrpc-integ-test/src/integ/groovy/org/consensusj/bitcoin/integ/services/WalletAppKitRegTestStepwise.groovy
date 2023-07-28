package org.consensusj.bitcoin.integ.services

import groovy.util.logging.Slf4j
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.core.Transaction
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.script.ScriptBuilder
import org.consensusj.bitcoin.services.WalletAppKitService
import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.consensusj.bitcoinj.signing.SigningRequest
import org.consensusj.bitcoinj.signing.TransactionInputData
import org.consensusj.bitcoinj.signing.Utxo
import spock.lang.Shared
import spock.lang.Stepwise

import java.nio.ByteBuffer

/**
 * Test WalletAppKitService on RegTest through a series of transactions.
 */
@Slf4j
@Stepwise
class WalletAppKitRegTestStepwise extends BaseRegTestSpec {
    private final hexFormatter = HexFormat.of();
    
    /** The WalletAppKitService under test */
    @Shared WalletAppKitService appKitService;

    @Shared Address spvWalletAddress

    def setupSpec() {
        appKitService = WalletAppKitService.createTemporary(BitcoinNetwork.REGTEST, ScriptType.P2PKH, "cj-btc-services-unittest")
        appKitService.start()
    }

    def 'loaded correctly'() {
        expect:
        appKitService != null
        appKitService.network() == BitcoinNetwork.REGTEST
    }

    def 'get an address'() {
        when:
        spvWalletAddress = appKitService.getnewaddress().join()

        then:
        spvWalletAddress != null
    }

    def 'get some coins and send money to an address in appKitService'() {
        when:
        log.warn("spvWalletAddress: {}", spvWalletAddress)
        var txId = requestBitcoin(spvWalletAddress, 2.btc)
        generateBlocks(1)
        waitForWalletSync()

        then:
        txId != null

        when:
        var balance = appKitService.getbalance().join()

        then:
        balance >= 1.btc

        when:
        var list = appKitService.listunspent(null, null, List.of(spvWalletAddress.toString()), false).join()

        then:
        list != null
        list.size() == 1
        list[0].amount == 2.btc
    }

    def 'build and sign a raw transaction using appKitService'() {
        given:
        var toAddr = new ECKey().toAddress(ScriptType.P2PKH, network)

        when: "we list unspent outputs to get a utxo"
        var list = appKitService.listunspent(null, null, List.of(spvWalletAddress.toString()), false).join()

        then:
        list != null
        list.size() == 1
        list[0].amount == 2.btc
        list[0].scriptPubKey.getProgram() == ScriptBuilder.createP2PKHOutputScript(spvWalletAddress.hash).getProgram()

        when: "we build an unsigned tx"
        var unspent = list[0]
        var utxo = Utxo.of(unspent.txid, unspent.vout, unspent.amount, unspent.scriptPubKey)
        var funds = utxo.amount()
        var sendAmount = 0.5.btc
        var signingRequest = SigningRequest.of(network,
                [TransactionInputData.of(utxo)],
                [(toAddr): sendAmount,
                 (spvWalletAddress): funds - (sendAmount + 0.1.btc)]  // Change
        )
        var utx = signingRequest.toUnsignedTransaction()

        then:
        utx.getOutputSum() >= 0.9.btc

        when: "we send the utx to appKitService for signing"
        var result = appKitService.signrawtransactionwithwallet(hexFormatter.formatHex(utx.bitcoinSerialize())).join()

        then:
        result != null
        result.isComplete()
        result.getHex() != null

        when: "we deserialize the signed tx"
        var signed = Transaction.read(ByteBuffer.wrap(hexFormatter.parseHex(result.getHex())))

        then:
        signed != null
        signed.getOutputSum() >= 0.9.btc
    }

    def 'create raw transaction using appKitService.createrawtransaction'() {
        given:
        var toAddr = new ECKey().toAddress(ScriptType.P2PKH, network)

        when: "we list unspent outputs go get a utxo"
        var list = appKitService.listunspent(null, null, List.of(spvWalletAddress.toString()), false).join()

        then:
        list != null
        list.size() == 1
        list[0].amount >= 1.btc
        list[0].scriptPubKey == ScriptBuilder.createP2PKHOutputScript(spvWalletAddress.hash)

        when: "we build an unsigned tx"
        var utxo = list[0]
        var funds = utxo.amount
        var sendAmount = 0.5.btc
        Map <String, Object> inp = Map.of("txid", list[0].getTxid().toString(), "vout", list[0].getVout())
        Map<String, String> toOutput = Map.of(toAddr.toString(), sendAmount.toPlainString())
        Map<String, String> changeOutput = Map.of(spvWalletAddress.toString(), (funds - (sendAmount + 0.1.btc)).toPlainString());
        var hex = appKitService.createrawtransaction(List.of(inp), List.of(toOutput, changeOutput)).join()
        var utx = Transaction.read(ByteBuffer.wrap(hexFormatter.parseHex(hex)));

        then:
        utx.getOutputSum() >= 0.9.btc

        when: "we send the utx to appKitService for signing"
        var result = appKitService.signrawtransactionwithwallet(hexFormatter.formatHex(utx.bitcoinSerialize())).join()

        then:
        result != null
        result.isComplete()
        result.getHex() != null

        when: "we deserialize the signed tx"
        var signed = Transaction.read(ByteBuffer.wrap(hexFormatter.parseHex(result.getHex())))

        then:
        signed != null
        signed.getOutputSum() >= 0.9.btc
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
