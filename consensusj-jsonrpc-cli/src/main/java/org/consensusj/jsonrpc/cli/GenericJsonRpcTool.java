package org.consensusj.jsonrpc.cli;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON-RPC Command-line tool with logging support.
 */
public class GenericJsonRpcTool extends BaseJsonRpcTool {
    public final static String commandName = "jsonrpc";

    // For a GraalVM command-line tool we must configure Java Logging in main
    // before initializing this Logger object
    private static Logger log;

    /**
     * main method for jsonrpc tool.
     *
     * See {@link JsonRpcToolOptions} for options and https://bitcoin.org/en/developer-reference#bitcoin-core-apis[Bitcoin Core JSON-RPC API]
     * for the methods and parameters. Users can use `-?` to get general help or {@code help <command>} to get help
     * on a specific command.
     *
     * @param args options, JSON-RPC method, JSON-RPC parameters
     */
    public static void main(String[] args) {
        JavaLoggingSupport.configure("org.consensusj.jsonrpc");
        log = LoggerFactory.getLogger(GenericJsonRpcTool.class);
        GenericJsonRpcTool tool = new GenericJsonRpcTool();
        log.trace("About to run command object");
        int status = tool.run(System.out, System.err, args);
        log.trace("Command object completed with status: {}", status);
        System.exit(status);
    }

    public GenericJsonRpcTool() {
        super();
    }

    @Override public String name() {
        return commandName;
    }

    @Override
    public Options options() {
        return new JsonRpcToolOptions();
    }
}
