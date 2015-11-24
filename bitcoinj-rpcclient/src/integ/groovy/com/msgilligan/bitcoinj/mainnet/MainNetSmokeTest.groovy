package com.msgilligan.bitcoinj.mainnet

import com.msgilligan.bitcoinj.BaseMainNetTestSpec
import spock.lang.Specification


/**
 *
 */
class MainNetSmokeTest extends BaseMainNetTestSpec {
    def "return basic info" () {
        when: "we request info"
        def info = getInfo()

        then: "we get back some basic information"
        info != null
        info.version >= 90100
        info.protocolversion >= 70002
    }

}