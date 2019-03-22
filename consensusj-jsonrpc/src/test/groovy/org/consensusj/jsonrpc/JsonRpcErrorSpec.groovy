package org.consensusj.jsonrpc

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 */
class JsonRpcErrorSpec extends Specification {

    def "constructor works correctly" () {
        given:
        def code = 0
        def message = "Message 1"
        def data = new Object()

        when:
        def error = new JsonRpcError(code, message, data)

        then:
        error.code == code
        error.message == message
        error.data == data
    }
    
    def "can deserialize from JSON" () {
        given:
        def jsonString = """
{"code": 0, "message": "Message 1", "data": {}} 
"""
        def mapper = new ObjectMapper()


        when:
        def error = mapper.readValue(jsonString, JsonRpcError.class)

        then:
        error.code == 0
        error.message == "Message 1"
        error.data instanceof Object
    }

}
