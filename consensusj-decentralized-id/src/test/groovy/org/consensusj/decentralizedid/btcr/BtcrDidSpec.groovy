package org.consensusj.decentralizedid.btcr

import org.consensusj.decentralizedid.DidSpec
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test spec for BtcrDid
 */
class BtcrDidSpec extends Specification {
    static final List<String> validBtcrDids = [
            "did:btcr:xkyt-fzgq-qq87-xnhn",
            "did:btcr:123456789abcdefghi"  // TODO: This is not really valid, just has valid method
    ]
    static final List<String> invalidUris = DidSpec.invalidUris
    static final List<String> invalidBtcrDids = DidSpec.invalidDids + // Invalid DIDs are also invalid BTCR DIDs
            [   // List of valid DIDs that are invalid BTCR DIDs
            "did:example:123456789abcdefghi",
            "did:example2:123456789abcdefghi"
    ]

    @Unroll
    def "Can construct valid BTCR DID: #string" (String string) {
        when:
        def did = BtcrDid.of(string)

        then:
        did.toString() == string

        where:
        string << validBtcrDids
    }

    @Unroll
    def "Invalid URI throws URISyntaxException: #string" (String string) {
        when:
        def did = BtcrDid.of(string)

        then:
        URISyntaxException ex = thrown()

        where:
        string << invalidUris
    }

    @Unroll
    def "Invalid BTCR DID throws IllegalArgumentException: #string" (String string) {
        when:
        def did = BtcrDid.of(string)

        then:
        IllegalArgumentException ex = thrown()

        where:
        string << invalidBtcrDids
    }
}
