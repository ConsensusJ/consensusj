package org.consensusj.jsonrpc.cli.test;

import org.consensusj.jsonrpc.cli.BaseJsonRpcTool;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 *  Support functions for testing command-line tools
 */
public interface CLITestSupport {

    default String[] parseCommandLine(String line) {
        return line.split(" ");
    }

    /**
     * Run a command and capture status and output
     *
     * @param tool Command object instance to run
     * @return Object containing status, stdout, stderr
     */
    default CLICommandResult runTool(BaseJsonRpcTool tool, String... args) throws UnsupportedEncodingException {
        // Setup to capture output streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream bes = new ByteArrayOutputStream();
        PrintStream cos = new PrintStream(bos);
        PrintStream ces = new PrintStream(bes);

        // Run the command
        int status = tool.run(cos, ces, args);

        return new CLICommandResult(status, bos, bes);
    }
}
