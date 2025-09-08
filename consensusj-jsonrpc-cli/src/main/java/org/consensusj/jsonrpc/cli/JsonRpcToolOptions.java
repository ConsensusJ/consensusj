package org.consensusj.jsonrpc.cli;


import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
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
                    .desc("Print verbose output (log-level = 3/INFO)")
                    .get()))
            .addOption((Option.builder()
                    .longOpt("log")
                    .desc("log-level (0-5: OFF, SEVERE, WARNING, INFO, FINE, ALL)")
                    .hasArg()
                    .argName("level")
                    .get()))
            .addOption(Option.builder()
                    .longOpt("V1")
                    .desc("Send '1.0' in the request 'jsonrpc' field as the JSON-RPC version")
                    .get())
            .addOptionGroup(new OptionGroup()
                    .addOption(Option.builder("c")
                            .longOpt("config-id")
                            .desc("ID/nickname in ~/.config/jsonrpc/config.toml")
                            .hasArg()
                            .argName("config-id")
                            .get())
                    .addOption(Option.builder("u")
                            .longOpt("url")
                            .desc("URL for the JSON-RPC endpoint")
                            .hasArg()
                            .argName("url")
                            .get()))
            .addOption(Option.builder()
                    .longOpt("add-truststore")
                    .desc("Additional truststore")
                    .hasArg()
                    .argName("path")
                    .get())
            .addOption(Option.builder()
                    .longOpt("alt-truststore")
                    .desc("Alternate truststore (password must be 'changeit')")
                    .hasArg()
                    .argName("path")
                    .get())
            .addOption(Option.builder()
                    .longOpt("response")
                    .desc("Output entire JsonRpcResponse, not just the result")
                    .get())
            // Bitcoin-CLI style options
            .addOption(Option.builder()
                    .longOpt("rpcconnect")
                    .desc("Send commands to node running on <ip> (default: 127.0.0.1)")
                    .hasArg()
                    .argName("ip")
                    .get())
            .addOption(Option.builder()
                    .longOpt("rpcport")
                    .desc("Connect to JSON-RPC on <port> (default: 8080)")
                    .hasArg()
                    .argName("port")
                    .get())
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
