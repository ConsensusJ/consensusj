package com.msgilligan.bitcoinj.dsljs;

import javax.script.ScriptException;

/**
 * Demo of using bitcoinj DSL for JavaScript
 */
public class Demo {
    public static void main(String[] args) throws ScriptException {
        ScriptRunner runner = new ScriptRunner();
        runner.evalResource("/javascript/sendBitcoin.js");
    }
}
