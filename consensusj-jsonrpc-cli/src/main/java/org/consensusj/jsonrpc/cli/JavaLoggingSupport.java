package org.consensusj.jsonrpc.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * GraalVM-compatible Support for using Java Logging
 * See: https://github.com/oracle/graal/blob/master/substratevm/LOGGING.md
 *
 * The default log-level for command-line tools configured in logging.properties
 * should be `WARNING`. The `-v` command-line switch should set the level to `FINE`.
 * Request-logging in RpcClient is at the `FINE` (slf4j `debug`) level.
 *
 * TODO: Create a command-line option to set finer-grained log levels
 */
public class JavaLoggingSupport {
    private final static String loggingPropertiesResource = "/logging.properties";
    private static String loggerName;

    /**
     * Configure logging.
     * Should be one of the first things called in `main()`
     */
    public static void configure(String loggerName) {
        InputStream inputStream = JsonRpcTool.class.getResourceAsStream(loggingPropertiesResource);
        if (inputStream != null) {
            try {
                LogManager.getLogManager().readConfiguration(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("JavaLoggingSupport: failed to process " + loggingPropertiesResource);
            }
        } else {
            System.err.println("JavaLoggingSupport: failed to load " + loggingPropertiesResource);
        }
        JavaLoggingSupport.loggerName = loggerName;
    }

    /**
     * Change log level (eg. as a result of `-v` command-line option)
     */
    public static void setVerbose() {
        final Logger app = Logger.getLogger(loggerName);
        app.setLevel(Level.FINE);
    }
}
