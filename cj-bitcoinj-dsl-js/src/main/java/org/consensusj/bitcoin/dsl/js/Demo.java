package org.consensusj.bitcoin.dsl.js;

import com.msgilligan.bitcoinj.rpc.RpcConfig;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

/**
 * Demo of using bitcoinj DSL for JavaScript
 */
public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws ScriptException, FileNotFoundException {
        var file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("File not found: " + file);
            System.exit(-1);
        }

        var rpcConfig = new RpcConfig(MainNetParams.get(),  // Network to connect to
                URI.create("http://localhost:8332"),        // RPC Server URL
                "bitcoinrpc",                      // RPC Server username
                "pass");                           // RPC Server password
        var runner = new ScriptRunner(rpcConfig);
        runner.evalFile(file);
    }
}
