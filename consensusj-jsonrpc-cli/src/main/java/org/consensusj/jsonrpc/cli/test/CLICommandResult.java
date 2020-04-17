package org.consensusj.jsonrpc.cli.test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Holds all the results (status code, stdout, stderr) of a CLI command run by a test
 */
public class CLICommandResult {
    static final String charset = StandardCharsets.UTF_8.toString();

    public final int status;
    public final String output;
    public final String error;

    public CLICommandResult(int status, String output, String error) {
        this.status = status;
        this.output = output;
        this.error = error;
    }

    public CLICommandResult(int status, ByteArrayOutputStream os, ByteArrayOutputStream es) throws UnsupportedEncodingException {
        this(status, os.toString(charset), es.toString(charset));
    }

}
