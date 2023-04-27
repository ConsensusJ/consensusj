package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

/**
 * Test JsonRpcResponse
 */
class JsonRpcResponseSpec extends Specification {

    def "can create from JSON "() {
        given:
        var mapper = new ObjectMapper()
        var json = """
{"result":null,"error":{"code":-8,"message":"Address not found"},"id":1}
"""

        when:
        JsonRpcResponse<JsonNode> response = mapper.readValue(json, JsonRpcResponse.class)

        then:
        response != null
    }
}
