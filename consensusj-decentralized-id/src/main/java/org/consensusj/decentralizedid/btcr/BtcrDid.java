package org.consensusj.decentralizedid.btcr;

import org.consensusj.decentralizedid.Did;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * BTCR DID
 */
public class BtcrDid extends Did {
    public static final String METHOD = "btcr";

    protected BtcrDid(URI uri) {
        super(uri);
        String ssp = uri.getSchemeSpecificPart();
        if (!ssp.startsWith(METHOD + ":")) {
            fail(uri, "Method is not 'btcr'");
        }
    }

    protected BtcrDid(String uriString) throws URISyntaxException {
        this(new URI(uriString));
    }


    private static void fail(URI candidateUri, String reason) throws IllegalArgumentException {
        throw new IllegalArgumentException(candidateUri.toString() + " is not a valid DID:BTCR URI (" + reason + ")");
    }

    /**
     *
     * @param uriString A string representing a DID URI
     * @return a valid DID
     * @throws  URISyntaxException
     *          If the URI string constructed from the given components
     *          violates RFC&nbsp;2396
     */
    public static BtcrDid of(String uriString) throws URISyntaxException {
        return new BtcrDid(uriString);
    }

    /**
     *
     * @param didUri a DID URI
     * @return a valid DID
     *
     */
    public static BtcrDid of(URI didUri) {
        return new BtcrDid(didUri);
    }

    /**
     * Create a DID from a known good string
     *
     * Similar to `java.net.url#create`
     *
     * @param uriString
     * @return a valid DID
     *
     * @throws  NullPointerException
     *          If {@code str} is {@code null}
     *
     * @throws  IllegalArgumentException
     *          If the given string violates RFC&nbsp;2396
     */
    public static BtcrDid create(String uriString) {
        return new BtcrDid(URI.create(uriString));
    }

}
