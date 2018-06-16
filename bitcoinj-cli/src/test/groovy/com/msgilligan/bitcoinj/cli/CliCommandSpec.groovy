package com.msgilligan.bitcoinj.cli

import org.consensusj.jsonrpc.JsonRPCException
import spock.lang.Specification

/**
 *
 */
class CliCommandSpec extends Specification {
    String[] dummyArgs = [].toArray()

    def "getServerURI works"() {
        given:
        def cli = createInstance()

        when:
        URI fallbackURI = "http://localhost:8332".toURI()

        def uri = cli.getServerURI(fallbackURI)

        then:
        uri == fallbackURI
    }

    def "getRPCConfig works"() {
        given:
        def cli = createInstance()

        when:
        URI fallbackURI = "http://localhost:8332".toURI()

        def config = cli.getRPCConfig()

        then:
        config != null
    }

    private CliCommand createInstance() {
        return new CliCommand("dummyName", "dummyUsage", new CliOptions(), dummyArgs) {
            @Override
            protected Integer runImpl() throws IOException, JsonRPCException {
                return null
            }
        }
    }

}
