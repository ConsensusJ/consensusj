package org.consensusj.bitcoin.cli

import org.consensusj.bitcoin.jsonrpc.test.TestServers
import org.consensusj.jsonrpc.cli.test.CLICommandResult
import org.consensusj.jsonrpc.cli.test.CLITestSupport
import spock.lang.Specification

/**
 * Integration Test Spec for BitcoinCLITool
 * Assumes bitcoind running on localhost in RegTest mode.
 *
 * TODO: We should probably check the command output (eventually)
 */
class BitcoinCLIToolIntegrationSpec extends Specification implements CLITestSupport {
    static final String rpcUser = TestServers.getInstance().getRpcTestUser();
    static final String rpcPassword = TestServers.getInstance().getRpcTestPassword();

    def "help option"() {
        when:
        def result = command '-?'

        then:
        result.status == 0
        result.output.length() == 0
        result.error.length() > 0
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

    def "generate a block"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait generatetoaddress 1 moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP"

        then:
        result.status == 0
        result.output.length() >= 0  // length == 0 on bitcoin core 0.9.x, length  > 0 on 0.10.x
        result.error.length() == 0
    }

    def "get server info"() {
        when:
        def result = command "-regtest -rpcuser=${rpcUser} -rpcpassword=${rpcPassword} -rpcwait getblockchaininfo"

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
        BitcoinCLITool tool = new BitcoinCLITool()
        return runTool(tool, args)
    }

}