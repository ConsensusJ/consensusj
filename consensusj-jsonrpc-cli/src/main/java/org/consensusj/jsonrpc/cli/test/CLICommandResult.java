package org.consensusj.jsonrpc.cli.test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Holds all the results (status code, stdout, stderr) of a CLI command run by a test
 */
public record CLICommandResult(int status, String output, String error) {
    static final String charset = StandardCharsets.UTF_8.toString();

    public CLICommandResult(int status, ByteArrayOutputStream os, ByteArrayOutputStream es) throws UnsupportedEncodingException {
        this(status, os.toString(charset), es.toString(charset));
    }
}
