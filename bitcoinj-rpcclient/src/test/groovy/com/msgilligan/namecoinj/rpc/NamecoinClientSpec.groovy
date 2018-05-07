package com.msgilligan.namecoinj.rpc

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * Integration smoke test for Namecoin Client
 * Assumes localhost Namecoin with config conf file in standard location
 */
@Ignore("Should be an integration test")
class NamecoinClientSpec extends Specification{
    @Shared NamecoinClient client

    void setup() {
        client = new NamecoinClient(NamecoinClient.readConfig())
    }

    def "constructor works correctly" () {
        expect:
        client
    }

    def "can check NMC block number" () {
        when:
        long blockNumber = client.getBlockCount()

        then:
        blockNumber >= 0
    }

}
