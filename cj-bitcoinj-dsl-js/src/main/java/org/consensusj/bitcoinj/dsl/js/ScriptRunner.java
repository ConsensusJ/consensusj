package org.consensusj.bitcoinj.dsl.js;

import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.consensusj.bitcoin.jsonrpc.RpcURI;
import org.consensusj.bitcoin.jsonrpc.test.TestServers;
import org.consensusj.bitcoin.jsonrpc.test.RegTestEnvironment;
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource;
import org.consensusj.jsonrpc.AsyncSupport;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * ScriptRunner that places a JSON-RPC client and Bitcoin utility functions inside
 * a script context so they can be called from JavaScript (currently Nashorn, but in
 * the future this may be Graal Javascript)
 */
public class ScriptRunner {
    private static final Logger log = LoggerFactory.getLogger(ScriptRunner.class);
    private final ScriptEngine engine;

    public ScriptRunner(RpcConfig rpcConfig) {
        engine = new NashornScriptEngineFactory().getScriptEngine("-scripting");
        var client = new BitcoinExtendedClient(rpcConfig);
        var env = new RegTestEnvironment(client);
        var funder = new RegTestFundingSource(client);
        engine.put("client", client);
        engine.put("env", env);
        engine.put("funder", funder);
        engine.put("satoshi", (Function<Number, Coin>) satoshis -> Coin.valueOf(satoshis.longValue()));
        engine.put("btc", (Function<Number, Coin>) btc -> Coin.valueOf(btc.longValue() * Coin.COIN.longValue()));
        engine.put("coin", (BiFunction<Number, Number, Coin>) (btc, cents) -> Coin.valueOf(btc.intValue(), cents.intValue()));
        engine.put("getBlockCount", (AsyncSupport.ThrowingSupplier<Integer>) client::getBlockCount);
    }

    public ScriptRunner() {
        this(new RpcConfig(MainNetParams.get(), RpcURI.getDefaultMainNetURI(),
                TestServers.getInstance().getRpcTestUser(), TestServers.getInstance().getRpcTestPassword()));
    }

    public Object evalResource(String resourcePath) throws ScriptException {
        log.info("Running resource: {}", resourcePath);
        var reader = new InputStreamReader(getClass().getResourceAsStream(resourcePath), StandardCharsets.UTF_8);
        return engine.eval(reader);
    }

    public Object evalFile(File scriptFile) throws ScriptException, FileNotFoundException {
        log.info("Running file: {}", scriptFile);
        var reader = new InputStreamReader(new FileInputStream(scriptFile), StandardCharsets.UTF_8);
        return engine.eval(reader);
    }
}
