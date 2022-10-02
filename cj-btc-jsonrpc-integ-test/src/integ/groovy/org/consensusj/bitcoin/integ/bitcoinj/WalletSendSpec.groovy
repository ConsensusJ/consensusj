package org.consensusj.bitcoin.integ.bitcoinj

import com.google.common.util.concurrent.ListenableFuture
import groovy.util.logging.Slf4j
import org.consensusj.bitcoin.json.pojo.WalletTransactionInfo
import org.bitcoinj.core.TransactionBroadcast
import org.bitcoinj.core.TransactionConfidence
import org.bitcoinj.script.Script
import org.consensusj.jsonrpc.JsonRpcStatusException
import org.bitcoinj.core.Address
import org.bitcoinj.core.BlockChain
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.utils.BriefLogFormatter
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Interoperability tests between a bitcoinj {@link Wallet} and a Bitcoin Core RPC server in RegTest mode.
 *
 * This is a {@link Stepwise} Spock {@link spock.lang.Specification} meaning that the tests
 * are always run in the order they appear in the source file. {@link Shared} variables are initialized
 * in {@link WalletSendSpec#setupSpec}. Since these are integration tests (not pure unit tests) and
 * communicate with the stateful Bitcoin blockchain, the {@code Stepwise} approach is helpful.
 */
@Slf4j
@Stepwise
class WalletSendSpec extends BaseRegTestSpec {
    /**
     * See bitcoinj Issue #2050 https://github.com/bitcoinj/bitcoinj/issues/2050
     * Currently in these tests it seems it's ok to just assume the transaction was sent properly
     * and rely on waitForUnconfirmedTransaction to tell us it was received by the server.
     */
    static final workaroundBitcoinJ_015_8_Issue = true

    @Shared
    NetworkParameters params
    @Shared
    Wallet wallet
    @Shared
    PeerGroup peerGroup

    void setupSpec() {
        BriefLogFormatter.init()
        params = getNetParams()

        wallet = Wallet.createDeterministic(params, Script.ScriptType.P2PKH)
        def store = new MemoryBlockStore(params)
        def chain = new BlockChain(params,wallet,store)
        peerGroup = new PeerGroup(params, chain)
        peerGroup.start()
        peerGroup.downloadBlockChain()
    }

    def "Wait for bitcoinj wallet to sync with RegTest chain"() {
        when: "we wait for the bitcoinj wallet to sync"
        //client.generateBlocks(1)   // This RPC call is necessary when I run locally, though I don't think it should be
        waitForWalletSync()

        then: "the bitcoinj wallet has the same height as the RPC server"
        noExceptionThrown()
    }

    def "Send mined coins from the Bitcoin Core (server) wallet to fund the bitcoinj wallet"() {
        when: "we mine coins with createFundedAddress()"
        Coin amount = 10.btc
        Coin extra = 0.1.btc
        Coin fundingAmount = amount + extra
        Address fundingAddress = createFundedAddress(fundingAmount)

        and: "we send coins from the Bitcoin Core wallet to an address in the bitcoinj wallet"
        Address bitcoinjWalletAddress = wallet.currentReceiveAddress()
        client.sendToAddress(bitcoinjWalletAddress, amount)

        and: "a block is generated"
        client.generateBlocks(1)

        and: "the bitcoinj wallet sees the new block"
        waitForWalletSync()

        then: "the coins arrive"
        wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE) == amount
    }

    def "Send from bitcoinj wallet back to the Bitcoin Core wallet using Wallet::sendCoins"() {
        when: "we send coins from bitcoinj"
        Coin startAmount = 10.btc
        Coin amount = 1.btc
        Address serverWalletAddress = client.getNewAddress()
        log.info("Sending ${amount} coins to ${serverWalletAddress}")
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, serverWalletAddress, amount)
        // Wait for broadcast complete
        log.info("Waiting for broadcast of {} to complete", sendResult.tx)
        Transaction sentTx = (workaroundBitcoinJ_015_8_Issue) ? sendResult.tx : sendResult.broadcastComplete.get()
        log.warn("Broadcast complete, tx = ${sentTx}")

        and: "wait for the unconfirmed transaction to be received by the server"
        log.info("Waiting for unconfirmed transaction to appear on server...")
        waitForUnconfirmedTransaction(sentTx.getTxId())
        log.info("... unconfirmed transaction found on server.")

        and: "a block is generated"
        log.info("Generating a block")
        client.generateBlocks(1)

        and: "Wait for the bitcoinj wallet to get confirmation of the transaction"
        // Wait for wallet to get confirmation of the transaction
        ListenableFuture<TransactionConfidence> depthFuture = sentTx.getConfidence().getDepthFuture(1)
        do {
            log.warn("Waiting for bitcoinj wallet to get a confirmation of the transaction...")
            sleep(100)
        } while (!depthFuture.isDone())

        then: "the server address has a balance of amount"
        client.getReceivedByAddress(serverWalletAddress) == amount

        and: "the bitcoinj wallet's balance has decreased by amount + fee"
        wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE) == startAmount - amount - sentTx.getFee()
    }

    def "Send from bitcoinj wallet back to the Bitcoin Core wallet using PeerGroup::broadcastTransaction"() {
        when: "we create a transaction using bitcoinj"
        Coin amount = 1.btc
        Address serverWalletAddress = client.getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, serverWalletAddress)
        SendRequest request = SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)

        and: "we send it using the PeerGroup"
        log.info("Sending Tx: {}", tx)
        TransactionBroadcast broadcast = peerGroup.broadcastTransaction(request.tx)
        log.info("Waiting for completion of broadcast for txid: {}", broadcast.tx.getTxId())
        Transaction sentTx = (workaroundBitcoinJ_015_8_Issue) ? request.tx : broadcast.future().get()
        waitForUnconfirmedTransaction(tx.txId)  // Wait for tx to show up on server as unconfirmed

        and: "a block is generated"
        client.generateBlocks(1)

        then: "the new address has a balance of amount"
        getReceivedByAddress(serverWalletAddress) == amount  // Verify serverWalletAddress balance
    }

    def "Create a raw transaction using bitcoinj and send with `sendrawtransaction` RPC"() {
        when: "we create a transaction using the bitcoinj wallet"
        Coin amount = 1.btc
        Address rpcAddress = client.getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, rpcAddress)
        SendRequest request = SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)

        and: "we send it to the server with the `sendrawtransaction` RPC"
        Sha256Hash txid = client.sendRawTransaction(tx)

        and: "we generate a block"
        client.generateBlocks(1)
        WalletTransactionInfo confirmedTx = client.getTransaction(txid, false, true)

        then: "the transaction is confirmed"
        confirmedTx.confirmations == 1

        and: "the new address has the correct balance"
        client.getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

    /**
     * Wait for a transaction to show up on the server (as unconfirmed)
     * (e.g. to make sure Transaction is fully received before generating a block)
     *
     * @param txid Transaction ID (hash) of transaction we're waiting for
     */
    void waitForUnconfirmedTransaction(Sha256Hash txid) {
        Transaction pendingTx = null
        while (pendingTx == null) {
            try {
                pendingTx = client.getRawTransaction(txid)
            } catch (JsonRpcStatusException e) {
                if (e.message == "No such mempool or blockchain transaction. Use gettransaction for wallet transactions.") {
                    log.warn("ignoring JsonRpcStatusException: {}, {}", e.jsonRpcCode, e.message)
                    Thread.sleep(100)
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * Wait for the bitcoinj wallet to sync with the Bitcoin Core blockchain
     */
    void waitForWalletSync() {
        int walletHeight, serverHeight
        while ( (walletHeight = wallet.getLastBlockSeenHeight()) < (serverHeight = client.getBlockCount()) ) {
            // TODO: Figure out a way to do this without polling and sleeping
            println "walletHeight < serverHeight: ${walletHeight} < ${serverHeight} -- Waiting..."
            Thread.sleep(100)
        }
        log.warn "walletHeight: ${walletHeight}, serverHeight: ${serverHeight}"
    }
}
