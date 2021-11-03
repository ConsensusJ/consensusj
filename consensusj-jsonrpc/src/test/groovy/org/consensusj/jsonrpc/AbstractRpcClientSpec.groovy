package org.consensusj.jsonrpc

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Basic test of our copied Base64 class.
 */
class AbstractRpcClientSpec extends Specification {

    @Unroll
    def "Base64 Basic Auth Test #myInt" (myInt, expectedResult) {
        given:
        def auth = "myuser" + ":" + "mypass" + myInt;

        when:
        def basicAuth = "Basic " + AbstractRpcClient.base64Encode(auth);

        then:
        basicAuth == expectedResult

        where:
        myInt | expectedResult
        1     | "Basic bXl1c2VyOm15cGFzczE="
        2     | "Basic bXl1c2VyOm15cGFzczI="
        3     | "Basic bXl1c2VyOm15cGFzczM="
        4     | "Basic bXl1c2VyOm15cGFzczQ="
        5     | "Basic bXl1c2VyOm15cGFzczU="
    }
}
