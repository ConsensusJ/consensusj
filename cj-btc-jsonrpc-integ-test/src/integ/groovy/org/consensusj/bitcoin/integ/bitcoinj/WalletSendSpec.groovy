package org.consensusj.bitcoin.integ.bitcoinj

import groovy.util.logging.Slf4j
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.consensusj.bitcoin.json.pojo.WalletTransactionInfo
import org.bitcoinj.core.TransactionBroadcast
import org.bitcoinj.core.TransactionConfidence
import org.consensusj.jsonrpc.JsonRpcStatusException
import org.bitcoinj.base.Address
import org.bitcoinj.core.BlockChain
import org.bitcoinj.base.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.utils.BriefLogFormatter
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
@IgnoreIf({ System.getProperty("regTestUseLegacyWallet") != "true" })
class WalletSendSpec extends BaseRegTestSpec {
    @Shared
    Wallet wallet

    void setupSpec() {
        BriefLogFormatter.init()
    }

    // TODO: Pull request to bitcoinj to make downloadBlockChain() work on 0-block RegTest?
    def "Make sure there's at least one block in the blockchain"() {
        // TODO: Have generateBlocks verify/create the server-side regtest wallet before mining
        when: "First make sure there is a server-side RegTest wallet for mining"
        var fundingSource = fundingSource()

        and: "Then make sure at least one block is mined"
        var height = getBlockCount()
        if (height < 1) {
            generateBlocks(1)
        }
        var newHeight = getBlockCount()

        then:
        newHeight >= 1
    }

    def "Create a bitcoinj Wallet and download the blockchain"() {
        when:
        wallet = Wallet.createDeterministic(network, ScriptType.P2PKH)
        var store = new MemoryBlockStore(NetworkParameters.of(network).getGenesisBlock())
        var chain = new BlockChain(network,wallet,store)
        PeerGroup peerGroup = new PeerGroup(network, chain)
        peerGroup.addWallet(wallet)
        peerGroup.start()
        peerGroup.downloadBlockChain()

        then:
        peerGroup.getMostCommonChainHeight() > 0
    }

    def "Wait for bitcoinj wallet to sync with RegTest chain"() {
        when: "we wait for the bitcoinj wallet to sync"
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
        given: "A starting amount, an amount to send and a newly-created destination address"
        Coin startAmount = 10.btc
        Coin amount = 1.btc
        Address serverWalletAddress = client.getNewAddress()

        when: "we send coins from bitcoinj"
        log.info("Sending ${amount} coins to ${serverWalletAddress}")
        SendRequest sendRequest = SendRequest.to(serverWalletAddress, amount)
        TransactionBroadcast broadcast = wallet.sendCoins(sendRequest).getBroadcast()
        Transaction tx = broadcast.transaction()    // Get the completed transaction
        // Wait for broadcast complete
        log.info("Waiting for broadcast of {} to complete", tx)
        // Wait for transaction to be acknowledged as sent, since we are in Regtest mode we do not call `awaitRelayed()`
        // I don't thinik we'll get an INV message from our single peer. We can use JSON-RPC to ensure
        // the server has seen the transaction. See https://github.com/bitcoinj/bitcoinj/issues/2050
        broadcast.awaitSent().get()
        log.warn("Broadcast complete, tx = ${tx}")

        and: "wait for the unconfirmed transaction to be received by the server"
        log.info("Waiting for unconfirmed transaction to appear on server...")
        waitForUnconfirmedTransaction(tx.txId)      // Using JSON-RPC, Wait for tx to show up on server as unconfirmed
        log.info("... unconfirmed transaction found on server.")
        // This code will never execute since we are always on REGTEST, but is provided for completeness
        if (wallet.network() != BitcoinNetwork.REGTEST) {
            log.info("Waiting for indication the transaction was relayed")
            broadcast.awaitRelayed();
        }

        and: "a block is generated"
        log.info("Generating a block")
        client.generateBlocks(1)

        and: "Wait for the bitcoinj wallet to get confirmation of the transaction"
        // Wait for wallet to get confirmation of the transaction
        CompletableFuture<TransactionConfidence> depthFuture = tx.getConfidence().getDepthFuture(1)
        do {
            log.warn("Waiting for bitcoinj wallet to get a confirmation of the transaction...")
            sleep(100)
        } while (!depthFuture.isDone())

        then: "the server address has a balance of amount"
        client.getReceivedByAddress(serverWalletAddress) == amount

        and: "the bitcoinj wallet's balance has decreased by amount + fee"
        wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE) == startAmount - amount - tx.getFee()
    }

    def "Send from bitcoinj wallet back to the Bitcoin Core wallet using Wallet::sendTransaction"() {
        given: "An amount to send and a newly-created destination address"
        Coin amount = 1.btc
        Address serverWalletAddress = client.getNewAddress()

        when: "we create a transaction using bitcoinj"
        // This could be done more simply using Wallet.sendCoins(), but this code shows how to build a transaction
        // manually and then use a SendRequest to have the wallet complete (find UTXOs, create a change output) it.
        Transaction tx = new Transaction()
        tx.addOutput(amount, serverWalletAddress)
        SendRequest request = SendRequest.forTx(tx)

        and: "we complete and send it using the Wallet"
        log.info("Sending Tx: {}", tx)
        // sendTransaction will complete, commit (store in the wallet), and send the transaction.
        // sendTransaction returns a CompletableFuture that completes when the transaction is sent to
        // the required number of peers (in this case 1.)
        // Since we are in Regtest mode we do not call `awaitRelayed()` as we won't get an INV message
        // from our single peer. Instead, we will use JSON-RPC to ensure
        // the server has seen the transaction. See https://github.com/bitcoinj/bitcoinj/issues/2050
        log.info("Waiting for completion of broadcast for txid: {}", tx.getTxId())
        TransactionBroadcast broadcast = wallet.sendTransaction(request).get(1, TimeUnit.SECONDS)
        log.info("Peers were found and transaction was broadcast to them")
        waitForUnconfirmedTransaction(tx.txId)      // Using JSON-RPC, Wait for tx to show up on server as unconfirmed
        log.info("Unconfirmed transaction seen by server")
        // This code will never execute since we are always on REGTEST, but is provided for completeness
        if (wallet.network() != BitcoinNetwork.REGTEST) {
            log.info("Waiting for indication the transaction was relayed")
            broadcast.awaitRelayed();
        }
        var confidence = tx.getConfidence()

        then: "we have a tx confidence object, showing depth of 0"
        confidence.depthInBlocks == 0

        when: "a block is generated"
        client.generateBlocks(1)

        then: "the new address has a balance of amount"
        tx == broadcast.transaction()
        getReceivedByAddress(serverWalletAddress) == amount  // Verify serverWalletAddress balance
        // confidence.depthInBlocks == 1   # This isn't reliably received by bitcoinj in the test environment
    }

    def "Create a raw transaction using bitcoinj and send with `sendrawtransaction` RPC"() {
        when: "we create a transaction using the bitcoinj wallet"
        Coin amount = 1.btc
        Address rpcAddress = client.getNewAddress()
        Transaction tx = new Transaction()
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
