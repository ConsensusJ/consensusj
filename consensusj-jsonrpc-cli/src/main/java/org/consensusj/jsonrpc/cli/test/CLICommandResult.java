package org.consensusj.jsonrpc.cli.test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Holds all the results (status code, stdout, stderr) of a CLI command run by a test
 */
public record CLICommandResult(int status, String output, String error) {
    static final Charset charset = StandardCharsets.UTF_8;

    public CLICommandResult(int status, ByteArrayOutputStream os, ByteArrayOutputStream es)  {
        this(status, os.toString(charset), es.toString(charset));
    }
}
