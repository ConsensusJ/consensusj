package com.msgilligan.bitcoinj.rpc

import spock.lang.Specification

/**
 * Check URI constants for correctness
 */
class RpcURISpec extends Specification {
    def "test URI creation methods" () {
        expect:
        RpcURI.defaultMainNetURI == "http://127.0.0.1:8332/".toURI()
        RpcURI.defaultTestNetURI == "http://127.0.0.1:18332/".toURI()
        RpcURI.defaultRegTestURI == "http://127.0.0.1:18443/".toURI()
    }
}
