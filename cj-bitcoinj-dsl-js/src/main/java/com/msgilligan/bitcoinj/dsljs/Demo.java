package com.msgilligan.bitcoinj.dsljs;

import com.msgilligan.bitcoinj.rpc.RpcConfig;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.net.URI;

/**
 * Demo of using bitcoinj DSL for JavaScript
 */
public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws ScriptException {
        var rpcConfig = new RpcConfig(MainNetParams.get(),  // Network to connect to
                URI.create("http://localhost:8332"),        // RPC Server URL
                "bitcoinrpc",                      // RPC Server username
                "pass");                           // RPC Server password
        var runner = new ScriptRunner(rpcConfig);
        runner.evalResource("/javascript/demo.js");
    }
}
