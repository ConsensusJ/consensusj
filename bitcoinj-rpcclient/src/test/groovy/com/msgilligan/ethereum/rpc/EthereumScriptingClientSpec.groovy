package com.msgilligan.ethereum.rpc

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * Test dynamic calls
 */
@Ignore("Should be an integration test")
class EthereumScriptingClientSpec extends Specification {

    @Shared EthereumScriptingClient client

    void setup() {
        client = new EthereumScriptingClient(InfuraHosts.INFURA_MAINNET_HOST, null,null)
    }


    def "check if geth is mining" () {
        when:
        def mining = client.eth_mining()  // Dynamic Method

        then:
        mining == false
    }

}
