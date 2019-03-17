package org.consensusj.jsonrpc.cli

import org.consensusj.jsonrpc.JsonRpcException
import spock.lang.Specification

/**
 *
 */
class CliCommandTest extends Specification {
    static final expectedURI = URI.create('http://localhost:8080/freebird')
    static final String[] dummyArgs = ['-url', expectedURI].toArray()

    def "smoke"() {
        given:
        CliCommand cli = new CliCommand("dummyName", "dummyUsage", new CliOptions(), dummyArgs) {
            @Override
            protected Integer runImpl() throws IOException, JsonRpcException {
                return null
            }
        }

        when:
        def client = cli.getClient()

        then:
        client.getServerURI() == expectedURI
    }
}
