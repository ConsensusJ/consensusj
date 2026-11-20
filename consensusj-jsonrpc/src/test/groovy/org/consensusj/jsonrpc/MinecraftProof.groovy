package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Ignore
import spock.lang.Specification

@Ignore("Integration test -- needs a local Minecraft server")
class MinecraftProof extends Specification {

    private URI testMineCraftServer = URI.create("ws://localhost:44000");
    private String bearerToken = "0123456789012345678901234567890123456789";
    private final DefaultRpcClient.TransportFactory transportFactory = (ObjectMapper m) -> new JsonRpcClientWebSocket(m, testMineCraftServer, bearerToken)

    def "get server status as JsonRpcResponse" () {
        when:
        var client = new DefaultRpcClient(transportFactory, JsonRpcMessage.Version.V2)
        var node = client.send("minecraft:server/status", JsonNode.class)

        then:
        node != null
    }

}
