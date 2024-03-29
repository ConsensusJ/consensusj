package org.consensusj.daemon.micronaut

import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * Test the ability to stop the server remotely
 */
@MicronautTest
class ServerStopSpec extends Specification {
    @Inject
    EmbeddedServer server

    @Shared
    BitcoinExtendedClient client

    def setup() {
        client = new BitcoinExtendedClient(server.URI, "", "")
    }

    @Ignore("For some reason this test fails with Micronaut 4.2.0")
    void 'stop'() {
        when:
        String message = client.stop()

        then:
        message == "cjbitcoind stopping"
    }
}
