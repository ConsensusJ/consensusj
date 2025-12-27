package org.consensusj.jsonrpc.cli.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.spi.ToolProvider;

/**
 *  Support functions for testing command-line tools
 */
public interface CLITestSupport {
    /**
     * Run a command and capture status and output
     *
     * @param tool Command object instance to run
     * @return Object containing status, stdout, stderr
     */
    static CLICommandResult runTool(ToolProvider tool, String... args) {
        // Setup to capture output streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream bes = new ByteArrayOutputStream();
        PrintStream pos = new PrintStream(bos);
        PrintStream pes = new PrintStream(bes);

        // Run the command
        int status = tool.run(pos, pes, args);

        return new CLICommandResult(status, bos, bes);
    }
}
