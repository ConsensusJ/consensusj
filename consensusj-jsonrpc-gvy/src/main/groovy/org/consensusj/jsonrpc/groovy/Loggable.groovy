package org.consensusj.jsonrpc.groovy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Adds an slf4j logger to a class. This was useful because we couldn't use the {@code @Slf4j} annotation on traits.
 * @deprecated We're deprecating most of our Groovy traits, so either use the {@code @Slf4j} annotation or define
 * your own {@code trait}.
 */
@Deprecated
trait Loggable {
    private final Logger logger = LoggerFactory.getLogger( this.getClass() )

    Logger getLog() {
        return logger
    }
}