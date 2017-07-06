package com.msgilligan.ethereum.rpc

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Test the few implemented static methods
 */
@Ignore("Should be an integration test")
class EthereumClientSpec extends Specification {

    @Shared EthereumClient client

    void setup() {
        client = new EthereumClient()
    }

    def "constructor works correctly" () {
        expect:
        client.serverURI == EthereumClient.DEFAULT_LOCALHOST
    }

    def "can check eth version" () {
        when:
        def version = client.ethProtocolVersion()

        then:
        version == "63"
    }

    def "can check eth block number" () {
        when:
        long blockNumber = client.ethBlockNumber()

        then:
        blockNumber >= 0
    }

    def "can get balance" () {
        given:
        def testAddr = "0x48c80F1f4D53D5951e5D5438B54Cba84f29F32a5"
        def expectedBalance = 0.0 * (10**18)
        
        when:
        def balance = client.ethGetBalance(testAddr)

        then:
        balance == expectedBalance
    }

    //@Ignore
    def "can make an eth call (check ERC-20 balance)" () {
        given:
        //def methodHash = client.web3Sha3('balanceOf(address)'.getBytes(StandardCharsets.UTF_8).encodeHex().toString())
        // data = methodHash + left-zero-filled address to query
        def data = "0x70a08231000000000000000000000000ab11204cfeaccffa63c2d23aef2ea9accdb0a0d5"
        def callObject = new EthTxCallObject(null,
                "0x48c80F1f4D53D5951e5D5438B54Cba84f29F32a5",  // REP (Augur)
                null,
                null,
                null,
                data )
        when:
        String resultStr = client.ethCall(callObject)
        BigInteger balance = client.quantityToInt(resultStr)

        then:
        balance >= 10000206669166472531012
    }

    def "can get client version (web3)" () {
        when:
        String version = client.web3ClientVersion()

        then:
        version == "Parity//v1.6.8-beta-c396229-20170608/x86_64-macos/rustc1.17.0"
    }

    def "can do a Keccak-256  hash (web3)" () {
        given: "data to hash"
        def dataToHash = "0x68656c6c6f20776f726c64"

        when:
        String hashed = client.web3Sha3(dataToHash)

        then:
        hashed == "0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"
    }

    @Ignore("not supported in Parity")
    def     "can start mining" () {
        when:
        def result = client.minerStart(3)

        then:
        result == true
    }

    @Ignore("not supported in Parity")
    def "can stop mining" () {
        when:
        def result = client.minerStop()

        then:
        result == true
    }

}
