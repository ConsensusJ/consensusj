package org.consensusj.jrpc;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

// These tests require a local Bitcoin server running in Regtest mode
public class RegTestServerTests {
    static final URI expectedURI = URI.create("http://bitcoinrpc:pass@localhost:18443/");
    static final String[] testArgs = {"--url", expectedURI.toString(), "getblockcount"};
    static final String[] helpArgs = {"--url", expectedURI.toString(), "help"};

    @Test
    void callBitcoinRegTest() {
        var tool = new JRpc();
        int result = tool.run(System.out, System.err, testArgs);

        assertEquals(0, result);
    }

    @Test
    void callBitcoinRegTestHelp() {
        var tool = new JRpc();
        int result = tool.run(System.out, System.err, helpArgs);

        assertEquals(0, result);
    }
}
