package org.consensusj.bitcoin.rpc.wallet

import org.consensusj.bitcoin.jsonrpc.test.WalletTestUtil
import org.consensusj.bitcoin.test.BaseRegTestSpec
import spock.lang.Requires

/**
 * Test Spec for {@code createwallet}
 */
class CreateWalletSpec extends BaseRegTestSpec {

    @Requires({ instance.clientInstance.getServerVersion() >= 210000})
    def "create and unload new non-descriptor wallet"() {
        given: "A random wallet name and a client instance for that wallet URL"
        var walletName = WalletTestUtil.randomWalletName()
        var walletClient = client.withWallet(walletName, rpcTestUser, rpcTestPassword)

        when: "we create a wallet"
        var createResult = walletClient.createWallet(walletName, null, null, null, null)

        then: "creation was successful"
        createResult.name == walletName
        createResult.warning == null || createResult.warning == ""

        when: "we unload the wallet"
        var unloadResult = walletClient.unloadWallet()

        then: "it works"
        unloadResult.warning == null || unloadResult.warning == ""
    }

    @Requires({ instance.clientInstance.getServerVersion() >= 230000})
    def "create and unload new descriptor wallet"() {
        given: "A random wallet name and a client instance for that wallet URL"
        var walletName = WalletTestUtil.randomWalletName()
        var walletClient = client.withWallet(walletName, rpcTestUser, rpcTestPassword)

        when: "we create a wallet"
        var createResult = walletClient.createWallet(walletName, null, null, null, null, true, null, null)

        then: "creation was successful"
        createResult.name == walletName
        createResult.warning == null || createResult.warning == ""

        when: "we unload the wallet"
        var unloadResult = walletClient.unloadWallet()

        then: "it works"
        unloadResult.warning == null || unloadResult.warning == ""
    }
}
