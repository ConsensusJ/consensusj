package com.msgilligan.bitcoinj.integ

import com.msgilligan.bitcoinj.json.pojo.NetworkInfo
import org.bitcoinj.core.TransactionBroadcast
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
import com.msgilligan.bitcoinj.BaseRegTestSpec
import spock.lang.Ignore
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
@Stepwise
//@Ignore("Hangs on Github Actions and likely elsewhere")
class WalletSendSpec extends BaseRegTestSpec {
    static NetworkInfo networkInfo // networkInfo.version for Assumptions (e.g. server version)
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

        networkInfo = client.getNetworkInfo()   // store networkInfo for Assumptions (e.g. server version)
    }

    def "Wait for bitcoinj wallet to sync with RegTest chain"() {
        when:
        wallet.addWatchedAddress(client.getRegTestMiningAddress()) 
        client.generateBlocks(1)   // This RPC call is necessary when I run locally, I don't think it should be
        Integer walletHeight, rpcHeight
        while ( (walletHeight = wallet.getLastBlockSeenHeight()) < (rpcHeight = client.getBlockCount()) ) {
            // TODO: Figure out a way to do this without polling and sleeping
            println "walletHeight < rpcHeight: ${walletHeight} < ${rpcHeight} -- Waiting..."
            Thread.sleep(100)
        }
        log.warn "walletHeight: ${walletHeight}, rpcHeight: ${rpcHeight}"

        then:
        walletHeight == rpcHeight
    }

    def "Send mined coins to fund the bitcoinj wallet"() {
        given:
        def fundingAmount = 10.1.btc
        def fundingAddress = createFundedAddress(fundingAmount)
        def walletAddr = wallet.currentReceiveAddress()
        def amount = 10.btc

        when: "we send coins to the wallet and write a block"
        client.sendToAddress(walletAddr, amount)
        client.generateBlocks(1)
        Integer walletHeight, rpcHeight
        while ( (walletHeight = wallet.getLastBlockSeenHeight()) < (rpcHeight = client.getBlockCount()) ) {
            println "WalletHeight < rpcHeight: ${walletHeight} < ${rpcHeight} -- Waiting..."
            Thread.sleep(100)
        }
        println "WalletHeight: ${walletHeight} == RPC Height: ${rpcHeight}"
        // Is it safe to assume that if walletHeight == rpcHeight then our transaction has been processed?

        then: "the coins arrive"
        wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE) == amount
    }

    def "Send from bitcoinj wallet to the Bitcoin Core wallet using Wallet::sendCoins"() {
        when: "we send coins from BitcoinJ and write a block"
        Coin startAmount = 10.btc
        Coin amount = 1.btc
        Address rpcAddress = getNewAddress()
        // Send it with BitcoinJ
        log.info("Sending ${amount} coins to ${rpcAddress}")
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup,rpcAddress,amount)
        // Wait for broadcast complete
        log.info("Waiting for broadcast of {} to complete", sendResult.tx)
        Transaction sentTx = (workaroundBitcoinJ_015_8_Issue) ? sendResult.tx : sendResult.broadcastComplete.get()
        log.warn("Broadcast complete, tx = ${sentTx}")
        // Wait for it to show up on server as unconfirmed
        log.info("Waiting for unconfirmed transaction to appear on server...")
        waitForUnconfirmedTransaction(sentTx.getTxId())
        log.info("... unconfirmed transaction found on server.")
        // Once server has pending transaction, generate a block
        log.info("Generating a block")
        generateBlocks(1)
        // Wait for wallet to get confirmation of the transaction
        def depthFuture = sentTx.getConfidence().getDepthFuture(1)
        do {
            log.warn("Waiting for bitcoinj wallet to get a confirmation of the transaction...")
            // TODO: I don't think we should have to wait 1 second and generate additional blocks here.
            sleep(1_000)
            generateBlocks(1)
        } while (!depthFuture.isDone())

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount
        wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE) == startAmount - amount - sentTx.getFee()
    }

    def "create and send a transaction from bitcoinj using PeerGroup::broadcastTransaction"() {
        when:
        Coin amount = 1.btc
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, rpcAddress)
        SendRequest request = SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        log.info("Sending Tx: {}", tx)
        TransactionBroadcast broadcast = peerGroup.broadcastTransaction(request.tx)
        log.info("Waiting for completion of broadcast for txid: {}", broadcast.tx.getTxId())
        Transaction sentTx = (workaroundBitcoinJ_015_8_Issue) ? request.tx : broadcast.future().get()
        waitForUnconfirmedTransaction(tx.txId)  // Wait for tx to show up on server as unconfirmed
        generateBlocks(1)

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

    def "create a raw transaction using bitcoinj and send with sendRawTransaction RPC"() {
        when:
        Coin amount = 1.btc
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, rpcAddress)
        SendRequest request = SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        def txid = client.sendRawTransaction(tx)
        generateBlocks(1)
        def confirmedTx = getTransaction(txid)

        then: "the transaction is confirmed"
        confirmedTx.confirmations == 1

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
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
                pendingTx = getRawTransaction(txid)
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
}
