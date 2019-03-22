package org.consensusj.jsonrpc.cli;

import org.consensusj.jsonrpc.JsonRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-RPC Command-line tool with logging support.
 */
public class JsonRpcTool extends CliCommand {
    public final static String commandName = "jsonrpc";
    // For a GraalVM command-line tool we muse configure Java Logging in main
    // before initializing this Logger object
    private static Logger log;


    public JsonRpcTool(String[] args) {
        super(commandName, new CliOptions(), args);
    }

    /**
     * main method for bitcoinj-cli tool.
     *
     * See {@link CliOptions} for options and https://bitcoin.org/en/developer-reference#bitcoin-core-apis[Bitcoin Core JSON-RPC API]
     * for the methods and parameters. Users can use `-?` to get general help or `help <command>` to get help
     * on a specific command.
     *
     * @param args options, JSON-RPC method, JSON-RPC parameters
     */
    public static void main(String[] args) {
        JavaLoggingSupport.configure("org.consensusj.jsonrpc");
        log = LoggerFactory.getLogger(JsonRpcTool.class);
        JsonRpcTool command = new JsonRpcTool(args);
        log.trace("About to run command object");
        int status = command.run();
        log.trace("Command object completed with status: {}", status);
        System.exit(status);
    }

    @Override
    public Integer runImpl() throws IOException {
        List<String> args = line.getArgList();
        if (args.size() == 0) {
            printError("jsonrpc method required");
            printHelp();
            return(1);
        }
        String method = args.get(0);
        args.remove(0); // remove method from list
        List<Object> typedArgs = convertParameters(method, args);
        Object result;
        try {
            result = client.send(method, typedArgs);
        } catch (JsonRpcException e) {
            e.printStackTrace();
            return 1;
        }
        if (result != null) {
            pwout.println(result.toString());
        }
        return 0;
    }

    /**
     * Convert params from strings to Java types that will map to correct JSON types
     *
     * TODO: Make this better and complete
     *
     * @param method the JSON-RPC method
     * @param params Params with String type
     * @return Params with correct Java types for JSON
     */
    protected List<Object> convertParameters(String method, List<String> params) {
        // Default (for now) is to leave them all as strings
        return new ArrayList<>(params);
    }

}
