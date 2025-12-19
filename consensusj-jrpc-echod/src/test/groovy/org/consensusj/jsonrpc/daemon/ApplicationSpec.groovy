package org.consensusj.jsonrpc.daemon

import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.consensusj.jsonrpc.JsonRpcMessage
import org.consensusj.jsonrpc.groovy.DynamicRpcClient
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * Basic Integration test of the JsonRpc echo daemon
 */
@MicronautTest
class ApplicationSpec extends Specification {
    @Inject
    EmbeddedServer server

    void 'test it works'() {
        expect:
        server.running
        server.URI.getScheme() == "http"
        server.URI.getHost() == "localhost" || server.URI.getHost().startsWith("runner") // "runner" is GitlabCI
    }

    void 'hit it with a JSON-RPC request'() {
        given:
        def testString = 'Hello jrpc-echod!'
        def endpoint = URI.create(server.URI.toString()+"/")
        def client = new DynamicRpcClient(endpoint, "", "")

        when:
        String result = client.echo(testString)

        then:
        result == testString
    }

}
