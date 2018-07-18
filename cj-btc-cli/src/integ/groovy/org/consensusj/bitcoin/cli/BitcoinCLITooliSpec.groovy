package org.consensusj.bitcoin.cli

import org.consensusj.bitcoin.cli.test.CLICommandResult
import org.consensusj.bitcoin.cli.test.CLITestSupport
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Integration Test Spec for BitcoinCLITool
 * Assumes bitcoind running on localhost in RegTest mode.
 *
 * TODO: We should probably check the command output (eventually)
 */
class BitcoinCLITooliSpec extends Specification implements CLITestSupport {

    def "help option"() {
        when:
        def result = command '-?'

        then:
        result.status == 1
        result.output.length() > 0
        result.error.length() == 0
    }

    def "get block count"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait getblockcount"

        then:
        result.status == 0
        result.output.length() > 0
        result.output[0..-2].toInteger() >= 0    // blockcount is a valid integer 0 or greater (trim '\n')
        result.error.length() == 0
    }

    @Ignore("The parameter 1 is being passed as a string and the server is rejecting it -- need to fix")
    def "generate a block"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait generate 1"

        then:
        result.status == 0
        result.output.length() >= 0  // length == 0 on bitcoin core 0.9.x, length  > 0 on 0.10.x
        result.error.length() == 0
    }

    def "get server info"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait getinfo"

        then:
        result.status == 0
        result.output.length() > 0  // Should be a JSON serialized string here, validate?
        result.error.length() == 0
    }

    /**
     * Helper method to create and run a command
     *
     * @param line The command args in a single string, separated by spaces
     * @return  status and output streams in Strings
     */
    protected CLICommandResult command(String line) {
        String[] args = parseCommandLine(line)     // Parse line into separate args

        // Run the command
        BitcoinCLITool cli = new BitcoinCLITool(args)
        return runCommand(cli)
    }

}