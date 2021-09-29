package com.msgilligan.bitcoinj.dsljs;

import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import com.msgilligan.bitcoinj.rpc.RpcURI;
import com.msgilligan.bitcoinj.rpc.test.TestServers;
import com.msgilligan.bitcoinj.test.RegTestEnvironment;
import com.msgilligan.bitcoinj.test.RegTestFundingSource;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * ScriptRunner that places a JSON-RPC client and Bitcoin utility functions inside
 * a script context so they can be called from JavaScript (currently Nashorn, but in
 * the future this may be Graal Javascript)
 */
public class ScriptRunner {
    private static final Logger log = LoggerFactory.getLogger(ScriptRunner.class);
    private final ScriptEngine engine;

    public ScriptRunner() throws ScriptException {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        engine = factory.getScriptEngine("-scripting");
        BitcoinExtendedClient client = new BitcoinExtendedClient(MainNetParams.get(),
                RpcURI.getDefaultMainNetURI(),
                TestServers.getInstance().getRpcTestUser(),
                TestServers.getInstance().getRpcTestPassword());
        RegTestEnvironment env = new RegTestEnvironment(client);
        RegTestFundingSource funder = new RegTestFundingSource(client);
        engine.put("client", client);
        engine.put("env", env);
        engine.put("funder", funder);
        engine.put("satoshi", (Function<Number, Coin>) satoshis -> Coin.valueOf(satoshis.longValue()));
        engine.put("btc", (Function<Number, Coin>) btc -> Coin.valueOf(btc.longValue() * Coin.COIN.longValue()));
        engine.put("coin", (BiFunction<Number, Number, Coin>) (btc, cents) -> Coin.valueOf(btc.intValue(), cents.intValue()));
        engine.put("getBlockCount", (Supplier<Integer>)() -> {
            try { return client.getBlockCount(); }
            catch (Exception e) { log.error("Exception: ", e); throw new RuntimeException(e); }
        });
    }

    public Object evalResource(String resourcePath) throws ScriptException {
        log.info("Running resource: {}", resourcePath);
        InputStreamReader reader =
                new InputStreamReader(getClass().getResourceAsStream(resourcePath), StandardCharsets.UTF_8);
        return engine.eval(reader);
    }
}
