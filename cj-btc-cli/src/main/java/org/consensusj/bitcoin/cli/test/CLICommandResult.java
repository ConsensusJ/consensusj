package org.consensusj.bitcoin.cli.test;

/**
 * Holds all the results (status code, stdout, stderr) of a CLI command run by a test
 */
public class CLICommandResult {
    public Integer status;
    public String  output;
    public String  error;
}
