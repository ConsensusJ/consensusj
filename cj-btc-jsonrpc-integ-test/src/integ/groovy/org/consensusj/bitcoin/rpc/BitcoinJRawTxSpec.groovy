package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.test.BaseRegTestSpec
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.core.Transaction
import spock.lang.Shared
import spock.lang.Stepwise


/**
 * Test building raw transactions with BitcoinJ and validate with RPC/RegTest
 *
 * Note: Original plan was to create an unsigned bitcoinj Transaction, then sign it,
 * then send it. The commented out code hints at this attempt. I hope to solve this later.
 *
 */
@Stepwise
class BitcoinJRawTxSpec extends BaseRegTestSpec {
    final static Coin fundingAmount = 10.btc
    final static Coin sendingAmount = 1.btc

    @Shared
    Address fundingAddress

    @Shared
    Address destinationAddress

    @Shared
    Transaction tx

//    @Shared
//    TransactionSigner signer;
//
//    @Shared
//    TestKeyBag keyBag;
//    KeyChainGroup keyBag;
//    KeyBag keyBag;

//    def setupSpec() {
//        keyBag = new TestKeyBag()
//        keyBag = new KeyChainGroup(netParams)
//        signer = new LocalTransactionSigner()
//    }

    def "Fund address"() {
        when: "a new address is created and a funding transaction is sent"
        fundingAddress = createFundedAddress(fundingAmount)

        and: "a block is recorded"
        generateBlocks(1)
        def balance = getBitcoinBalance(fundingAddress)

        then: "the address should have that balance"
        balance == fundingAmount
    }

    def "Create Signed raw transaction"() {
        given: "a newly created address as destination"
        destinationAddress = getNewAddress("destinationAddress")

        when: "we get the signing key from the server"
        def key = dumpPrivKey(fundingAddress)

        and: "we create an signed bitcoinj transaction, spending from fundingAddress to destinationAddress"
        tx = createSignedTransaction(key, destinationAddress, sendingAmount)

        then: "there should be a valid signed transaction"
        tx != null
        tx.outputs.size() > 0
        tx.inputs.size() > 0
    }

//    def "Sign unsigned raw transaction"() {
//        given: "the private key is in the keybag used by the signer"
//        def key = dumpPrivKey(fundingAddress)
////        keyBag.add(key)
////        keyBag.importKeys(key)
//        keyBag = Wallet.fromKeys(netParams, [key])
//
//        when: "the transaction is signed"
//        def proposedTx = new TransactionSigner.ProposedTransaction(tx);
//        def signed = signer.signInputs(proposedTx, keyBag)
//
//        then: "all inputs should be signed"
//        signed == true
//        tx.inputs.every { it.getScriptSig() != null }
//    }

    def "Broadcast signed raw transaction"() {
        when: "the transaction is sent"
        def txid = sendRawTransaction(tx)

        then: "there should be a transaction hash"
        txid != null

        when: "a new block is mined"
        generateBlocks(1)

        and: "we get info about the transaction"
        def broadcastedTransaction = getRawTransactionInfo(txid)

        then: "the transaction should have 1 confirmation"
        broadcastedTransaction.confirmations == 1
        broadcastedTransaction.txid == txid

        and: "#fundingAddress has a remainder of coins minus transaction fees"
        def balanceRemaining = getBitcoinBalance(fundingAddress)
        balanceRemaining == fundingAmount - sendingAmount - stdTxFee

        and: "#destinationAddress has a balance matching the spent amount"
        def balanceDestination = getBitcoinBalance(destinationAddress)
        balanceDestination == sendingAmount
    }
}