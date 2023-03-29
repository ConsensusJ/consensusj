package org.consensusj.bitcoin.json.pojo

import com.fasterxml.jackson.databind.ObjectMapper
import org.bitcoinj.base.BitcoinNetwork
import org.consensusj.bitcoin.json.conversion.RpcClientModule
import spock.lang.Shared
import spock.lang.Specification

/**
 * Make sure we can correctly parse AddressInfo for multiple version of Bitcoin Core
 */
class AddressInfoSpec extends Specification {
    static String json19 = """
    {
      "address": "n1ozC6KGsH8VqjgvQtxbDAMjhaZ6siGXXs",
      "scriptPubKey": "76a914de9b2bb5ab122eb4e27dddc71313dcca13ee5ef388ac",
      "ismine": true,
      "solvable": true,
      "desc": "pkh([de9b2bb5]0409013cc94bc5bc6c9f28bc82cd8ea3ddaec3dc55771a55efc5779fe25dd4be38f635cbd1882822d978a9bc5da0578db432a5d5ddf9dc0f3830c071fccea8f14f)#5clvu8pg",
      "iswatchonly": false,
      "isscript": false,
      "iswitness": false,
      "pubkey": "0409013cc94bc5bc6c9f28bc82cd8ea3ddaec3dc55771a55efc5779fe25dd4be38f635cbd1882822d978a9bc5da0578db432a5d5ddf9dc0f3830c071fccea8f14f",
      "iscompressed": false,
      "label": "AddressInfoSpec",
      "ischange": false,
      "timestamp": 1,
      "labels": [
            {   "name" : "AddressInfoSpec", "purpose" : "xyz" }
      ]
    }
"""

    static String json20 = """
    {
      "address": "n1ozC6KGsH8VqjgvQtxbDAMjhaZ6siGXXs",
      "scriptPubKey": "76a914de9b2bb5ab122eb4e27dddc71313dcca13ee5ef388ac",
      "ismine": true,
      "solvable": true,
      "desc": "pkh([de9b2bb5]0409013cc94bc5bc6c9f28bc82cd8ea3ddaec3dc55771a55efc5779fe25dd4be38f635cbd1882822d978a9bc5da0578db432a5d5ddf9dc0f3830c071fccea8f14f)#5clvu8pg",
      "iswatchonly": false,
      "isscript": false,
      "iswitness": false,
      "pubkey": "0409013cc94bc5bc6c9f28bc82cd8ea3ddaec3dc55771a55efc5779fe25dd4be38f635cbd1882822d978a9bc5da0578db432a5d5ddf9dc0f3830c071fccea8f14f",
      "iscompressed": false,
      "ischange": false,
      "timestamp": 1,
      "labels": [
        "AddressInfoSpec"
      ]
    }
"""

    @Shared
    ObjectMapper mapper;

    def "can parse 0.19 format"() {
        given:

        when:
        AddressInfo addressInfo = mapper.readValue(json19, AddressInfo.class)

        then:
        addressInfo.address.toString() == "n1ozC6KGsH8VqjgvQtxbDAMjhaZ6siGXXs"
    }

    def "can parse 0.20 format"() {
        given:

        when:
        AddressInfo addressInfo = mapper.readValue(json20, AddressInfo.class)

        then:
        addressInfo.address.toString() == "n1ozC6KGsH8VqjgvQtxbDAMjhaZ6siGXXs"
    }

    void setup() {
        mapper = new ObjectMapper()
        mapper.registerModule(new RpcClientModule(BitcoinNetwork.REGTEST))
    }
}
