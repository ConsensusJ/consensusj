package org.consensusj.bitcoin.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Command-line options for tools that communicate with Bitcoin RPC
 */
public class CliOptions extends Options {


    public CliOptions() {
        super();
        this.addOption("?", null, false, "This help message")
// 'conf' and 'datadir' aren't implemented yet.
//            .addOption("c", "conf", true, "Specify configuration file (default: bitcoin.conf)")
//            .addOption("d", "datadir", true, "Specify data directory")
            .addOptionGroup(new OptionGroup()
                    .addOption(new Option(null, "testnet", false, "Use the test network"))
                    .addOption(new Option(null, "regtest", false, "Enter regression test mode")))
            .addOption(Option.builder().longOpt("rpcconnect")
                    .desc("Send commands to node running on <ip> (default: 127.0.0.1)")
                    .hasArg()
                    .argName("ip")
                    .build())
                .addOption(Option.builder().longOpt("rpcport")
                        .desc("Connect to JSON-RPC on <port> (default: 8332 or testnet: 18332)")
                        .hasArg()
                        .argName("port")
                        .build())
                .addOption(null, "rpcwait", false, "Wait for RPC server to start")
                .addOption(Option.builder().longOpt("rpcuser")
                        .desc("Username for JSON-RPC connections")
                        .hasArg()
                        .argName("user")
                        .build())
                .addOption(Option.builder().longOpt("rpcpassword")
                        .desc("Password for JSON-RPC connections")
                        .hasArg()
                        .argName("pw")
                        .build())
            .addOption(null, "rpcssl", false, "Use https for JSON-RPC connections");
    }

}
