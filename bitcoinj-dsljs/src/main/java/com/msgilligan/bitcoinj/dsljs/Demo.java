package com.msgilligan.bitcoinj.dsljs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;

/**
 * Demo of using bitcoinj DSL for JavaScript
 */
public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws ScriptException {
        ScriptRunner runner = new ScriptRunner();
        runner.evalResource("/javascript/sendBitcoin.js");
    }
}
