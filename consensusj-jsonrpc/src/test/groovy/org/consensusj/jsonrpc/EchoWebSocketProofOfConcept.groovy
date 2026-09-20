package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Ignore
import spock.lang.Specification

@Ignore("Integration test -- needs a running echod instance")
class EchoWebSocketProofOfConcept extends Specification {

    private URI echodServer = URI.create("ws://localhost:8080/ws");
    private String bearerToken = "unused";
    private final DefaultRpcClient.TransportFactory transportFactory = (ObjectMapper m) -> new JsonRpcClientWebSocket(m, echodServer, bearerToken)

    def "send and receive and echo as a JsonRpcResponse" () {
        given:
        String expectedEcho = "Hello WebSocket!"

        when:
        var client = new DefaultRpcClient(transportFactory, JsonRpcMessage.Version.V2)
        String result = client.send("echo", String.class, List.of(expectedEcho))

        then:
        result != null
        result == expectedEcho
    }

}
