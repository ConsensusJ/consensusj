package com.msgilligan.bitcoinj.dsljs;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;

/**
 *
 */
public class ScriptRunner {
    private ScriptEngine engine;

    public ScriptRunner() {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        engine = factory.getScriptEngine("-scripting");
    }

    public Object evalResource(String resourcePath) throws ScriptException {
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(resourcePath));
        return engine.eval(reader);
    }
}
