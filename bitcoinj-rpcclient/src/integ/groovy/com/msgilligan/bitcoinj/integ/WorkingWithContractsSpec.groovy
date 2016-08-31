package com.msgilligan.bitcoinj.integ

import com.google.common.collect.ImmutableList
import com.msgilligan.bitcoinj.BaseRegTestSpec
import org.bitcoinj.core.BlockChain
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.Script;
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.wallet.AllowUnconfirmedCoinSelector
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise


/**
 * Integration test spec based on https://bitcoinj.github.io/working-with-contracts
 */
@Ignore("not working yet")
@Stepwise
class WorkingWithContractsSpec extends BaseRegTestSpec {
    static final params = RegTestParams.get()
    static final walletStartAmount = 10.btc
    static final txAmount = 0.5.btc

    @Shared
    PeerGroup peerGroup

    @Shared blockChain
    BlockChain chain

    @Shared
    Wallet wallet

    @Shared
    ECKey clientKey

    @Shared
    ECKey serverKey

    @Shared
    Transaction broadcastTx

    @Shared
    TransactionSignature serverSignature

    def "Client has a Bitcoinj Wallet with some funds"() {
        when: "Create a bitcoinj wallet"
        wallet = new Wallet(params)
        wallet.setCoinSelector(new AllowUnconfirmedCoinSelector())
        def store = new MemoryBlockStore(params)
        chain = new BlockChain(params,wallet,store)
        peerGroup = new PeerGroup(params, chain)
        peerGroup.addWallet(wallet)
        peerGroup.start()

        then: "Wallet is ready"
        wallet != null

    }

    def "Send mined coins to fund a new BitcoinJ wallet"() {
        given:
        def fundingAddress = createFundedAddress(walletStartAmount + 0.1.btc)
        def walletKey = new ECKey()
        def walletAddr = walletKey.toAddress(params)
        wallet.importKey(walletKey)

        when: "we send coins to the wallet and write a block"
        client.sendToAddress(walletAddr, walletStartAmount)
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
        wallet.getBalance() == walletStartAmount
    }


    def "Server generates a key pair" () {
        when:
        serverKey = new ECKey()

        then:
        serverKey != null
        serverKey.hasPrivKey()
    }

    def "Client creates multi-signature outputs" () {
        when: "Generate a key pair"
        clientKey = new ECKey();

        and: "Prepare a template for the contract."
        Transaction contract = new Transaction(params)
        List<ECKey> keys = ImmutableList.of(clientKey, serverKey)
        // Create a 2-of-2 multisig output script.
        Script script = ScriptBuilder.createMultiSigOutputScript(2, keys)
        // Now add an output for 0.50 bitcoins that uses that script.
        Coin amount = txAmount
        contract.addOutput(amount, script)

        and: "Use a Wallet to add inputs"
        // We have said we want to make 0.5 coins controlled by us and them.
        // But it's not a valid tx yet because there are no inputs.
        SendRequest req = Wallet.SendRequest.forTx(contract)
        wallet.completeTx(req)   // Could throw InsufficientMoneyException

        then:
        req != null

        when: "Broadcast"
        // Broadcast and wait for it to propagate across the network.
        // It should take a few seconds unless something went wrong.
        broadcastTx = peerGroup.broadcastTransaction(req.tx).broadcast().get()

        then:
        broadcastTx != null
        broadcastTx.outputs.size() >= 1
    }

    def "Server partially signs the transaction" () {
        given:
        // Assume we get the multisig transaction we're trying to spend from
        // somewhere, like a network connection.
        Transaction contract = broadcastTx

        when: "Transaction is partially signed"

        TransactionOutput multisigOutput = contract.getOutput(0);
        Script multisigScript = multisigOutput.getScriptPubKey();
        // Is the output what we expect?
        //checkState(multisigScript.isSentToMultiSig());
        Coin value = multisigOutput.getValue();

        // OK, now build a transaction that spends the money back to the client.
        Transaction spendTx = new Transaction(params);
        spendTx.addOutput(value, clientKey);
        spendTx.addInput(multisigOutput);

        // It's of the right form. But the wallet can't sign it. So, we have to
        // do it ourselves.
        Sha256Hash sighash = spendTx.hashForSignature(0, multisigScript, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature signature = serverKey.sign(sighash);
        // We have calculated a valid signature, so send it back to the client:
        serverSignature = new TransactionSignature(signature, Transaction.SigHash.ALL, false)
        //sendToClientApp(signature);

        then:
        serverSignature != null
    }

    def "Client assembles complete transaction" () {
        given:
        def multisigOutput = broadcastTx.outputs.find { it.scriptPubKey.isSentToMultiSig() }
        def multisigScript = multisigOutput.getScriptPubKey()

        when:
        // Client side code.
        Transaction spendTx = new Transaction(params);
        spendTx.addOutput(txAmount, clientKey);
        TransactionInput input = spendTx.addInput(multisigOutput);
        Sha256Hash sighash = spendTx.hashForSignature(0, multisigScript, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature mySignature = clientKey.sign(sighash)
        TransactionSignature myTxSig = new TransactionSignature(mySignature, Transaction.SigHash.ALL, false);

        then:
        mySignature != null

        when:
        // Create the script that spends the multi-sig output.
        Script inputScript = ScriptBuilder.createMultiSigInputScript(
                        ImmutableList.of(myTxSig, serverSignature))
//        Script inputScript = ScriptBuilder.createMultiSigInputScriptBytes(
//                ImmutableList.of(mySignature.encodeToDER(), serverSignature.toCanonicalised().encodeToDER()))
        // Add it to the input.
        input.setScriptSig(inputScript);

        then:
        // We can now check the server provided signature is correct, of course...
        println multisigOutput
        println input
        println multisigScript
        println inputScript
        input.verify(multisigOutput)  // Throws an exception if the script doesn't run.

        when: "Broadcast"
        // Broadcast and wait for it to propagate across the network.
        // It should take a few seconds unless something went wrong.
        broadcastTx = peerGroup.broadcastTransaction(spendTx).broadcast().get()

        then:
        broadcastTx != null

        when: "Block is recorded"
        generate()

        then: "the amount is returned to our wallet"
        wallet.getBalance() == walletStartAmount

    }

}