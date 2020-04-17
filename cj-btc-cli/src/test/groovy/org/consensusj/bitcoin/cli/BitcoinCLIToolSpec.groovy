package org.consensusj.bitcoin.cli

import org.consensusj.jsonrpc.JsonRpcException
import org.consensusj.jsonrpc.cli.GenericJsonRpcTool
import spock.lang.Specification

import java.util.spi.ToolProvider

/**
 *
 */
class BitcoinCLIToolSpec extends Specification {

    def "Can instantiate via ToolProvider"() {
        when:
        def tool = ToolProvider.findFirst("cj-bitcoin-cli").get()

        then:
        tool instanceof BitcoinCLITool
    }

    def "getServerURI works"() {
        given:
        def tool = createInstance()

        when:
        URI expectedURI = "http://localhost:18332".toURI()
        BitcoinCLITool.BitcoinCLICall call = (BitcoinCLITool.BitcoinCLICall) tool.createCall(System.out, System.err, "-regtest", "getblockcount")
        def client = call.rpcClient()
        def serverURI = client.getServerURI()
        def config = call.getRPCConfig()

        then:
        serverURI == expectedURI
        config != null
    }

    private BitcoinCLITool createInstance() {
        return new BitcoinCLITool()
    }

}
