package org.consensusj.decentralizedid

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Spock test specification for Did class
 */
class DidSpec extends Specification {
    static final List<String> validDids = [
            "did:example:123456789abcdefghi",
            "did:example2:123456789abcdefghi"
    ];
    static final List<String> invalidUris = [
            "{}",
            "[]"
    ];
    static final List<String> invalidDids = [
            "abc:example:123456789abcdefghi",
            "def:example:123456789abcdefghi"
    ];

    @Unroll
    def "Can construct valid DID: #string" (String string) {
        when:
        def did = Did.of(string)

        then:
        did.toString() == string
        
        where:
        string << validDids
    }

    @Unroll
    def "Invalid URI throws URISyntaxException: #string" (String string) {
        when:
        def did = Did.of(string)

        then:
        URISyntaxException ex = thrown()

        where:
        string << invalidUris
    }

    @Unroll
    def "Invalid DID throws IllegalArgumentException: #string" (String string) {
        when:
        def did = Did.of(string)

        then:
        IllegalArgumentException ex = thrown()

        where:
        string << invalidDids
    }
}
