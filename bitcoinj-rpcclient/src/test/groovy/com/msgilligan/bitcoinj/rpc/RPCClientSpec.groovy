package com.msgilligan.bitcoinj.rpc

import spock.lang.Specification

/**
 * Test Spec for RPCClient
 */
class RPCClientSpec extends Specification {

    def "constructor works correctly" () {
        when:
        def client = new RPCClient("http://localhost:8080".toURI(), "user", "pass")

        then:
        client.serverURI == "http://localhost:8080".toURI()
    }

}