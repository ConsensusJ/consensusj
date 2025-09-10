package org.consensusj.jsonrpc.cli;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON-RPC Command-line tool with logging support.
 */
public class GenericJsonRpcTool extends BaseJsonRpcTool {
    public final static String NAME = "jrpc";
    /**
     * @deprecated Use {@link #NAME}
     */
    public final static String commandName = NAME;
    private final static Logger log = LoggerFactory.getLogger(GenericJsonRpcTool.class);

    /**
     * main method for {@code jrpc} tool.
     * <p>
     * See {@link JsonRpcToolOptions} for options and your server's documentation or help
     * for the methods and parameters. Users can use `-?` to get help on the tool or (usually) {@code help <command>} to get help
     * from the server on a specific JSON-RPC method.
     *
     * @param args options, JSON-RPC method, JSON-RPC parameters
     */
    public static void main(String[] args) {
        JavaLoggingSupport.configure("org.consensusj.jsonrpc");
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
        return NAME;
    }

    @Override
    public Options options() {
        return new JsonRpcToolOptions();
    }
}
