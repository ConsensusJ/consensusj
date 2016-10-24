package com.msgilligan.bitcoinj.proxy

import com.google.common.io.Resources
import spock.lang.Specification

/**
 * Configuration resource loading test
 */
class ConfigSpec extends Specification {

    def "can find resource marker"() {
        when:
        def resource = this.getClass().getResource("/.ratpack")

        then:
        resource != null
    }
}
