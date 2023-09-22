package org.consensusj.jsonrpc.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

// TODO: Create a command-line option to set finer-grained log levels
/**
 * GraalVM-compatible support for using Java Logging.
 * <p>
 * See: <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/guides/add-logging-to-native-executable.md">add-logging-to-native-executable.md"LOGGING.md</a>
 * <p>
 * The default log-level for command-line tools configured in {@code logging.properties} should be {@link Level#WARNING}.
 * The {@code -v} command-line switch should set the level to {@link Level#FINE}.
 * Request-logging in {@link org.consensusj.jsonrpc.JsonRpcClientHttpUrlConnection} is at the {@link Level#FINE} (slf4j {@code debug}) level.
 */
public class JavaLoggingSupport {
    private final static String loggingPropertiesResource = "/logging.properties";
    private static String loggerName = "";

    /**
     * Configure logging.
     * Should be one of the first things called in `main()`
     */
    public static void configure(String loggerName) {
        InputStream inputStream = GenericJsonRpcTool.class.getResourceAsStream(loggingPropertiesResource);
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
     * This sets the level to {@link Level#FINE}
     */
    public static void setVerbose() {
        setLogLevel(Level.FINE);
    }

    /**
     * Change log level (eg. as a result of `-log=level` command-line option)
     * @param level j.u.logging log level
     */
    public static void setLogLevel(Level level) {
        final Logger app = Logger.getLogger(loggerName);
        app.setLevel(level);
    }

}
