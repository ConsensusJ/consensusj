package com.msgilligan.ethereum.rpc

import spock.lang.Ignore
import spock.lang.Specification

/**
 * Test dynamic calls
 */
@Ignore("Should be an integration test")
class EthereumScriptingClientSpec extends Specification {


    def "check if geth is mining" () {
        given:
        def client = new EthereumScriptingClient()

        when:
        def mining = client.eth_mining()

        then:
        mining == false
    }

}
