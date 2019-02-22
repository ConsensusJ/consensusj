package org.consensusj.decentralizedid;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Representation of a W3C DID
 *
 * Based on java.net.URI with some inspiration from OkHttp.HttpUrl and use of Java 8-style `.of()` methods.
 * 
 */
public class Did {
    public static String DID_SCHEME = "did";
    protected final URI didUri;

    private Did(String didString) throws URISyntaxException {
        this(new URI(didString));
    }

    protected Did(URI didUri) {
        String scheme = didUri.getScheme();
        if (scheme != null && !scheme.equals(DID_SCHEME)) {
            fail(didUri, "DID Scheme Required");
        }
        this.didUri = didUri;
    }

    private static void fail(URI candidateUri, String reason) throws IllegalArgumentException {
        throw new IllegalArgumentException(candidateUri.toString() + " is not a DID URI (" + reason + ")");
    }

    /**
     *
     * @param uriString A string representing a DID URI
     * @return a valid DID
     * @throws  URISyntaxException
     *          If the URI string constructed from the given components
     *          violates RFC&nbsp;2396
     */
    public static Did of(String uriString) throws URISyntaxException {
        return new Did(uriString);
    }

    /**
     *
     * @param didUri a DID URI
     * @return a valid DID
     * 
     */
    public static Did of(URI didUri) {
        return new Did(didUri);
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
    public static Did create(String uriString) {
        return new Did(URI.create(uriString));
    }

    public URI toURI() {
        return didUri;
    }

    public String toString() {
        return didUri.toString();
    }
}
