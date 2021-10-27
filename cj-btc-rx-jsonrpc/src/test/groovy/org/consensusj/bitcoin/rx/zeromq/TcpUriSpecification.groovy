package org.consensusj.bitcoin.rx.zeromq

import spock.lang.Specification

/**
 *
 */
class TcpUriSpecification extends Specification {
    def "can parse as URI"() {
        given:
        def uri = URI.create("tcp://192.168.1.1:38332")

        expect:
        uri.getScheme() == "tcp"
        uri.getHost() == "192.168.1.1"
        uri.getPort() == 38332
    }
}
