package org.consensusj.bitcoin.rpc

import org.consensusj.bitcoin.json.pojo.MethodHelpEntry
import org.consensusj.bitcoin.test.BaseRegTestSpec

/**
 * Integration test of {@code help} RPC command
 */
class HelpSpec extends BaseRegTestSpec {

    def "can call help"() {
        when:
        String helpString = client.help()

        then:
        helpString != null;
        helpString.contains("getblock")
        helpString.contains("getblockcount")
    }


    def "can call help for getblockcount"() {
        when:
        String helpString = client.help("getblockcount")

        then:
        helpString != null
        helpString.contains("getblockcount")
    }

    def "help for unknown command returns correct message"() {
        when:
        String helpString = client.help("idontexist")

        then:
        helpString.startsWith("help: unknown command: ")
    }

    def "can call help (for output)"() {
        when:
        List<String> helpLines = client.helpAsLines()

        then:
        helpLines != null;

        cleanup:
        helpLines.forEach(line -> println line)
    }

    def "get help as method help entries works"() {
        when:
        List<MethodHelpEntry> commands = client.helpAsMethodEntries()

        then:
        commands != null
        commands.size() > 40
        commands.find { it.methodName == "getblock"}.methodParametersHelp.contains("\"blockhash\" ( verbosity )")
        commands.find { it.methodName == "getblockcount"}.methodParametersHelp == ""
    }

    def "get commands works"() {
        when:
        List<String> commands = client.getCommands()

        then:
        commands != null
        commands.size() > 40
        commands.contains("getblock")
        commands.contains("getblockcount")
    }

    def "commandExists works for known command"() {
        when:
        boolean exists = client.commandExists("getblockcount")

        then:
        exists
    }

    def "commandExists works for unknown command"() {
        when:
        boolean exists = client.commandExists("idontexist")

        then:
        !exists
    }
}
