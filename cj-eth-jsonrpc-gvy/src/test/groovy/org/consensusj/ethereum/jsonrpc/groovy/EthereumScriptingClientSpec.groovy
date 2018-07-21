package org.consensusj.ethereum.jsonrpc.groovy

import org.consensusj.ethereum.jsonrpc.InfuraHosts
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * Test static and dynamic calls
 */
@Ignore("Should be an integration test")
class EthereumScriptingClientSpec extends Specification {

    @Shared EthereumScriptingClient client

    void setup() {
        client = new EthereumScriptingClient(InfuraHosts.INFURA_MAINNET_HOST, null,null)
    }


    def "can check eth version" () {
        when:
        def version = client.ethProtocolVersion()  // Static method

        then:
        version == "0x3f"
    }

    def "check if geth is mining" () {
        when:
        def mining = client.eth_mining()  // Dynamic Method

        then:
        mining == false
    }

}
