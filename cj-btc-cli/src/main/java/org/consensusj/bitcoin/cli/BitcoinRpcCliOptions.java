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
package org.consensusj.bitcoin.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Command-line options for tools that communicate with Bitcoin RPC
 */
public class BitcoinRpcCliOptions extends Options {
    public BitcoinRpcCliOptions() {
        super();
        this.addOption("?", "help", false, "This help message")
            .addOption((Option.builder("v")
                        .longOpt("verbose")
                        .desc("Print verbose output")
                        .get()))
// 'conf' and 'datadir' aren't implemented yet.
//            .addOption("c", "conf", true, "Specify configuration file (default: bitcoin.conf)")
//            .addOption("d", "datadir", true, "Specify data directory")
            .addOptionGroup(new OptionGroup()
                    .addOption(new Option(null, "testnet", false, "Use the test network"))
                    .addOption(new Option(null, "regtest", false, "Enter regression test mode")))
            .addOption(Option.builder()
                    .longOpt("rpcconnect")
                    .desc("Send commands to node running on <ip> (default: 127.0.0.1)")
                    .hasArg()
                    .argName("ip")
                    .get())
            .addOption(Option.builder()
                    .longOpt("rpcport")
                    .desc("Connect to JSON-RPC on <port> (default: 8332 or testnet: 18332)")
                    .hasArg()
                    .argName("port")
                    .get())
            .addOption(null, "rpcwait", false, "Wait for RPC server to start")
            .addOption(Option.builder()
                    .longOpt("rpcuser")
                    .desc("Username for JSON-RPC connections")
                    .hasArg()
                    .argName("user")
                    .get())
            .addOption(Option.builder()
                    .longOpt("rpcpassword")
                    .desc("Password for JSON-RPC connections")
                    .hasArg()
                    .argName("pw")
                    .get())
            .addOption(null, "rpcssl", false, "Use https for JSON-RPC connections");
    }

}
