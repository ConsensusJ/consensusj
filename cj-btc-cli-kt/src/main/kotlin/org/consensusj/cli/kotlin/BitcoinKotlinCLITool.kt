package org.consensusj.cli.kotlin

import org.consensusj.bitcoin.cli.CliCommand
import org.consensusj.bitcoin.cli.CliOptions
import org.consensusj.jsonrpc.JsonRpcException

import java.io.IOException
import java.util.ArrayList

const val commandName = "cj-bitcoin-cli-kotlin"

/**
 * main method for BitcoinKotlinCLITool.
 *
 * See [CliOptions] for options and https://bitcoin.org/en/developer-reference#bitcoin-core-apis[Bitcoin Core JSON-RPC API]
 * for the methods and parameters. Users can use `-?` to get general help or `help <command>` to get help
 * on a specific command.
 *
 * @param args options, JSON-RPC method, JSON-RPC parameters
 */
fun main(args: Array<String>) {
    val status = BitcoinKotlinCLITool(args).run()
    System.exit(status)
}

/**
 * An attempt at cloning the bitcoin-cli tool, but using Kotlin and ConsensusJ
 */
class BitcoinKotlinCLITool(args: Array<String>) : CliCommand(commandName, CliOptions(), args) {

    @Throws(IOException::class)
    public override fun runImpl(): Int? {
        val args = line.argList
        if (args.size == 0) {
            printError("jsonrpc method required")
            printHelp()
            return 1
        }
        val method = args[0]
        args.removeAt(0) // remove method from list
        val typedArgs = convertParameters(method, args)
        val result: Any?
        try {
            result = client.send<Any>(method, typedArgs)
        } catch (e: JsonRpcException) {
            e.printStackTrace()
            return 1
        }

        if (result != null) {
            pwout.println(result.toString())
        }
        return 0
    }

    /**
     * Convert params from strings to Java types that will map to correct JSON types
     *
     * TODO: Make this better and complete
     *
     * @param method the JSON-RPC method
     * @param params Params with String type
     * @return Params with correct Java types for JSON
     */
    protected fun convertParameters(method: String, params: List<String>): List<Any> {
        val typedParams = ArrayList<Any>()
        when (method) {
            "setgenerate" -> typedParams.add(java.lang.Boolean.parseBoolean(params[0]))
            // Default (for now) is to leave them all as strings
            else -> params.forEach { typedParams.add(it) }
        }
        return typedParams
    }
}

