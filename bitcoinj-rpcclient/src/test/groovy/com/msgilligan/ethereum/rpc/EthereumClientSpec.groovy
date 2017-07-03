package com.msgilligan.ethereum.rpc

import spock.lang.Ignore
import spock.lang.Specification

/**
 * Test the few implemented static methods
 */
@Ignore("Should be an integration test")
class EthereumClientSpec extends Specification {

    def "constructor works correctly" () {
        when:
        def client = new EthereumClient()

        then:
        client.serverURI == EthereumClient.DEFAULT_LOCALHOST
    }

    def "can check eth version" () {
        given:
        def client = new EthereumClient()

        when:
        def version = client.ethProtocolVersion()

        then:
        version == "63"
    }

    def "can check eth block number" () {
        given:
        def client = new EthereumClient()

        when:
        long blockNumber = client.ethBlockNumber()

        then:
        blockNumber >= 0
    }

    @Ignore("not supported in Parity")
    def "can start mining" () {
        given:
        def client = new EthereumClient()

        when:
        def result = client.minerStart(3)

        then:
        result == true
    }

    @Ignore("not supported in Parity")
    def "can stop mining" () {
        given:
        def client = new EthereumClient()

        when:
        def result = client.minerStop()

        then:
        result == true
    }
}
