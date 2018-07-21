package com.msgilligan.bitcoinj.integ

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
import org.bitcoinj.wallet.AllowUnconfirmedCoinSelector
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Various interoperability tests between RPC server and bitcoinj wallets.
 */
//@Ignore("'Send mined coins' intermittently fails because transaction is still pending")
@Stepwise
class WalletSendSpec extends BaseRegTestSpec {
    @Shared
    NetworkParameters params
    @Shared
    Wallet wallet
    @Shared
    PeerGroup peerGroup


    void setupSpec() {
//        BriefLogFormatter.initVerbose();
        BriefLogFormatter.initWithSilentBitcoinJ();
        params = getNetParams()

        wallet = new Wallet(params)
        wallet.setCoinSelector(new AllowUnconfirmedCoinSelector())
        def store = new MemoryBlockStore(params)
        def chain = new BlockChain(params,wallet,store)
        peerGroup = new PeerGroup(params, chain)
        peerGroup.addWallet(wallet)
        peerGroup.start()
    }

    def "Send mined coins to fund a new BitcoinJ wallet"() {
        given:
        def fundingAmount = 20.btc
        def fundingAddress = createFundedAddress(fundingAmount)
        def walletAddr = getNewAddress()
        def walletKey = dumpPrivKey(walletAddr)
        wallet.importKey(walletKey)
        def amount = 10.btc

        when: "we send coins to the wallet and write a block"
        client.sendToAddress(walletAddr, amount)
        client.generate()
        Integer walletHeight, rpcHeight
        while ( (walletHeight = wallet.getLastBlockSeenHeight()) < (rpcHeight = client.getBlockCount()) ) {
            // TODO: Figure out a way to do this without polling and sleeping
            println "WalletHeight < rpcHeight: ${walletHeight} < ${rpcHeight} -- Waiting..."
            Thread.sleep(100)
        }
        println "WalletHeight: ${walletHeight} == RPC Height: ${rpcHeight}"
        // Is it safe to assume that if walletHeight == rpcHeight then our transaction has been processed?

        then: "the coins arrive"
        client.getReceivedByAddress(walletAddr) == amount
        wallet.getBalance() == amount
    }

    def "Send from BitcoinJ wallet to the Bitcoin Core wallet"() {
        when: "we send coins from BitcoinJ and write a block"
        Coin startAmount = 10.btc
        Coin amount = 1.btc
        Address rpcAddress = getNewAddress()
        // Send it with BitcoinJ
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup,rpcAddress,amount)
        // Wait for broadcast complete
        Transaction sentTx = sendResult.broadcastComplete.get()
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.hash)
        // Once server has pending transaction, generate a block
        generate()
        // Wait for wallet to get confirmation of the transaction
        sentTx.getConfidence().getDepthFuture(1).get()

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount
        wallet.getBalance() == startAmount - amount - sentTx.getFee()
    }

    def "create and send a transaction from BitcoinJ using wallet.completeTx"() {
        when:
        Coin amount = 1.btc
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, rpcAddress)
        SendRequest request = SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        Transaction sentTx = peerGroup.broadcastTransaction(request.tx).future().get();
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.hash)
        generate()

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

    def "create a raw transaction using BitcoinJ but send with an RPC"() {
        when:
        Coin amount = 1.btc
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(amount, rpcAddress)
        SendRequest request = SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        def txid = client.sendRawTransaction(tx)
        generate()
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
        Transaction pendingTx = null;
        while (pendingTx == null) {
            try {
                pendingTx = getRawTransaction(txid)
            } catch (JsonRpcStatusException e) {
                if (e.message != "No information available about transaction") {
                    throw e;
                }
            }
        }
    }
}
