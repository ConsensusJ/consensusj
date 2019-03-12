package org.consensusj.bitcoin.cli

import org.consensusj.jsonrpc.JsonRpcException
import org.consensusj.jsonrpc.cli.CliCommand
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

    private BitcoinCliCommand createInstance() {
        return new BitcoinCliCommand("dummyName", "dummyUsage", new CliOptions(), dummyArgs) {
            @Override
            protected Integer runImpl() throws IOException, JsonRpcException {
                return null
            }
        }
    }

}
