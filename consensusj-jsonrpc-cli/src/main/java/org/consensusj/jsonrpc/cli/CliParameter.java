/*
 * Copyright 2014-2026 ConsensusJ Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.consensusj.jsonrpc.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.stream.Stream;

/**
 * JSON-RPC method parameter, parsed from the command-line. There are two implementations: {@link Valid} and {@link Invalid}.
 * A {@code Valid} contains an object that can be serialized to JSON via a converter/mapper like Jackson. An {@code Invalid}
 * contains the {@link Exception} that occurred while parsing.
 */
public sealed interface CliParameter {
    /**
     * Return the source (pre-deserialization) JSON string
     * @return source string
     */
    String source();

    default boolean valid() {
        return this instanceof CliParameter.Valid;
    }

    default boolean invalid() {
        return !valid();
    }

    /**
     * Stream zero or 1 valid objects.
     * <p>This allows {@code .flatMap(CliParameter::stream)} to filter invalid and unwrap valid objects.
     * @return A stream of zero or one valid objects.
     */
    Stream<Object> stream();

    /**
     * Create a {@code Valid} object
     * @param source The source CLI string
     * @param object The resulting object
     * @return a valid parameter
     */
    static Valid valid(String source, Object object) {
        return new Valid(source, object);
    }

    /**
     * Create a {@code Invalid} object
     * @param source The source CLI string
     * @param error The error returned from the parser
     * @return a valid parameter
     */
    static Invalid invalid(String source, Exception error) {
        return new Invalid(source, error);
    }

    /**
     * Parse a string returning either a valid or invalid {@code CliParameter}
     * @param source the CLI parameter string to parse
     * @param parseFunction a parsing function
     * @return wraps either parsed, serializable object or an {@code Exception}
     */
    static CliParameter parse(String source, Parser parseFunction) {
        try {
            return new CliParameter.Valid(source, parseFunction.apply(source));
        } catch (Exception e) {
            return new CliParameter.Invalid(source, e);
        }
    }

    /**
     * Implementation for Valid objects
     * @param source source JSON string
     * @param object parsed object
     */
    record Valid(String source, Object object) implements CliParameter {
        public Stream<Object> stream() { return Stream.of(object); }
    }

    /**
     * Implementation for Invalid source strings
     * @param source source JSON string
     * @param error parsing exception
     */
    record Invalid(String source, Exception error) implements CliParameter {
        public Stream<Object> stream() { return Stream.empty(); }
    }

    /**
     * Functional interface for parsing a {@link String} to an {@link Object} possibly
     * throwing an {@link Exception}.
     */
    @FunctionalInterface
    interface Parser {
        JsonNode apply(String s) throws JsonProcessingException;
    }
}
