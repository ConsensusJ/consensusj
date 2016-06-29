package com.msgilligan.bitcoinj.dsljs;

import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import com.msgilligan.bitcoinj.rpc.JsonRPCException;
import com.msgilligan.bitcoinj.rpc.RPCURI;
import com.msgilligan.bitcoinj.rpc.test.TestServers;
import com.msgilligan.bitcoinj.test.RegTestEnvironment;
import com.msgilligan.bitcoinj.test.RegTestFundingSource;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.bitcoinj.core.Coin;
import org.bitcoinj.params.RegTestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 *
 */
public class ScriptRunner {
    private static final Logger log = LoggerFactory.getLogger(ScriptRunner.class);
    private ScriptEngine engine;

    public ScriptRunner() throws ScriptException {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        engine = factory.getScriptEngine("-scripting");
        BitcoinExtendedClient client = new BitcoinExtendedClient(RegTestParams.get(),
                RPCURI.getDefaultRegTestURI(),
                TestServers.getInstance().getRpcTestUser(),
                TestServers.getInstance().getRpcTestPassword());
        RegTestEnvironment env = new RegTestEnvironment(client);
        RegTestFundingSource funder = new RegTestFundingSource(client);
        engine.put("client", client);
        engine.put("env", env);
        engine.put("funder", funder);
        engine.put("satoshi", (Function<Number, Coin>) (Number n) -> Coin.valueOf(n.longValue()));
        engine.put("btc", (Function<Number, Coin>) (Number n) -> Coin.valueOf(n.longValue() * Coin.COIN.value));
        engine.put("getBlockCount", (Supplier<Integer>)() -> {
            try { return client.getBlockCount(); }
            catch (Exception e) { log.error("Exception: {}", e); throw new RuntimeException(e); }
        });
    }

    public Object evalResource(String resourcePath) throws ScriptException {
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(resourcePath));
        return engine.eval(reader);
    }
}
