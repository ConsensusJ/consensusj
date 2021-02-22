package org.consensusj.jsonrpc.cli;


import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Command-line options for JSON-RPC tool
 */
public class JsonRpcToolOptions extends Options {
    public JsonRpcToolOptions() {
        super();
        this.addOption("?", "help", false, "This help message")
            .addOption((Option.builder("v")
                        .longOpt("verbose")
                        .desc("Print verbose output")
                        .build()))
            .addOption(Option.builder()
                    .longOpt("url")
                    .desc("URL for the JSON-RPC endpoint")
                    .hasArg()
                    .argName("url")
                    .build())
            // Bitcoin-CLI style options
            .addOption(Option.builder()
                    .longOpt("rpcconnect")
                    .desc("Send commands to node running on <ip> (default: 127.0.0.1)")
                    .hasArg()
                    .argName("ip")
                    .build())
            .addOption(Option.builder()
                    .longOpt("rpcport")
                    .desc("Connect to JSON-RPC on <port> (default: 8080)")
                    .hasArg()
                    .argName("port")
                    .build())
            .addOption(Option.builder()
                    .longOpt("rpcuser")
                    .desc("Username for JSON-RPC connections")
                    .hasArg()
                    .argName("user")
                    .build())
            .addOption(Option.builder()
                    .longOpt("rpcpassword")
                    .desc("Password for JSON-RPC connections")
                    .hasArg()
                    .argName("pw")
                    .build())
            .addOption(null, "rpcssl", false, "Use https for JSON-RPC connections");
    }

}
