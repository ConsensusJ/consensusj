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
package org.consensusj.bitcoinj.dsl.js;

import org.bitcoinj.base.BitcoinNetwork;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

/**
 * Demo of using bitcoinj DSL for JavaScript
 */
public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws ScriptException, FileNotFoundException {
        if (args.length < 1) {
            System.err.println("Error: No JavaScript file specified");
            System.err.println("USAGE: scriptname.js");
            System.exit(-1);
        }
        var file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("File not found: " + file);
            System.exit(-1);
        }

        var rpcConfig = new RpcConfig(BitcoinNetwork.MAINNET,  // Network to connect to
                URI.create("http://localhost:8332"),        // RPC Server URL
                "bitcoinrpc",                      // RPC Server username
                "pass");                           // RPC Server password
        var runner = new ScriptRunner(rpcConfig);
        runner.evalFile(file);
    }
}
