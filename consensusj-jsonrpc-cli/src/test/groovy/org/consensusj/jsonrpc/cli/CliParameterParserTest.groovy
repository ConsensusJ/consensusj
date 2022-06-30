package org.consensusj.jsonrpc.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import spock.lang.Shared
import spock.lang.Specification

/**
 * 
 */
class CliParameterParserTest extends Specification  {
    @Shared ObjectMapper mapper
    @Shared CliParameterParser parser

    def setup() {
        mapper = new ObjectMapper()
        parser = new CliParameterParser(mapper)
    }

    def "can parse String param to type #clazz"(String param, Class<?> clazz, String expectedSerialization) {
        when:
        var req = parser.parse(["method", param])
        var pojo = req.params[0]
        var serialized = mapper.writeValueAsString(pojo);

        then:
        req.method == "method"
        pojo.class == clazz
        serialized == expectedSerialization

        where:
        param               | clazz             | expectedSerialization
        'world'             | String.class      | '"world"'
        '"world"'           | TextNode.class    | '"world"'
        '1'                 | IntNode.class     | '1'
        'true'              | BooleanNode.class | 'true'
        '{"key":"value"}'   | ObjectNode.class  | '{"key":"value"}'
        '[]'                | ArrayNode.class   | '[]'
    }
}
