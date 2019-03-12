package org.consensusj.bitcoin.cli.test;

import com.msgilligan.bitcoinj.rpc.test.TestServers;
import org.consensusj.jsonrpc.cli.CliCommand;
import org.consensusj.jsonrpc.cli.test.CLICommandResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 *  Support functions for testing command-line tools
 */
public interface CLITestSupport {
    String rpcUser = TestServers.getInstance().getRpcTestUser();
    String rpcPassword = TestServers.getInstance().getRpcTestPassword();

    default String[] parseCommandLine(String line) {
        String[] args = line.split(" ");     // (Overly?) simple parsing of string into args[]
        return args;
    }

    /**
     * Run a command and capture status and output
     *
     * @param command Command object instance to run
     * @return Object containing status, stdout, stderr
     */
    default CLICommandResult runCommand(CliCommand command) throws UnsupportedEncodingException {
        // Setup CommandResult to capture status and streams
        CLICommandResult result = new CLICommandResult();
        InputStream is = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream cos = new PrintStream(bos);
        ByteArrayOutputStream bes = new ByteArrayOutputStream();
        PrintStream ces = new PrintStream(bes);

        // Run the command
        result.status = command.run(is, cos, ces);

        // Put output and error streams in strings
        String charset = StandardCharsets.UTF_8.toString();
        result.output = bos.toString(charset);
        result.error = bes.toString(charset);

        return result;
    }

}
