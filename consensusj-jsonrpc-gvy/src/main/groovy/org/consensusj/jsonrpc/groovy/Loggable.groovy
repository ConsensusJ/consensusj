package org.consensusj.jsonrpc.groovy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Adds an slf4j logger to a class. This is useful because we can't use the {@code @Slf4j} annotation on traits.
 */
trait Loggable {
    private final Logger logger = LoggerFactory.getLogger( this.getClass() )

    Logger getLog() {
        return logger
    }
}