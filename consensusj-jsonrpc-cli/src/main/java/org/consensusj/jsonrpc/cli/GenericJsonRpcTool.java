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

    // For a GraalVM command-line tool we muse configure Java Logging in main
    // before initializing this Logger object
    private static Logger log;

    /**
     * main method for jsonrpc tool.
     *
     * See {@link JsonRpcToolOptions} for options and https://bitcoin.org/en/developer-reference#bitcoin-core-apis[Bitcoin Core JSON-RPC API]
     * for the methods and parameters. Users can use `-?` to get general help or `help <command>` to get help
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



    /**
     * Convert params from strings to Java types that will map to correct JSON types
     *
     * TODO: Make this better and complete
     *
     * @param method the JSON-RPC method
     * @param params Params with String type
     * @return Params with correct Java types for JSON
     */
    @Override
    protected List<Object> convertParameters(String method, List<String> params) {
        List<Object> converted = new ArrayList<>();
        for (String param : params) {
            Object convertedParam = convertParam(param);
            converted.add(convertedParam);
        }
        return converted;
    }

    /**
     * Convert a single param from a command-line option {@code String} to a type more appropriate
     * for Jackson/JSON-RPC.
     * 
     * @param param A string parameter to convert
     * @return The input parameter, possibly converted to a different type
     */
    private Object convertParam(String param) {
        Object result;
        Long l = toLong(param);
        if (l != null) {
            // If the param is a valid Long, return a Long
            result = l;
        } else {
            // Else, return a Boolean or String
            switch (param) {
                case "false":
                    result = Boolean.FALSE;
                    break;
                case "true":
                    result = Boolean.TRUE;
                    break;
                default:
                    result = param;
            }
        }
        return result;
    }

    // Convert to Long (if possible), else return null
    private static Long toLong(String strNum) {
        try {
            Long l = Long.parseLong(strNum);
            return l;
        } catch (NumberFormatException nfe) {
            return null;
        }
    }
}
