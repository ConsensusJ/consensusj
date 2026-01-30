/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
