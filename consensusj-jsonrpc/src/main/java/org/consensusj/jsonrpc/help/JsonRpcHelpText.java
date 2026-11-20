package org.consensusj.jsonrpc.help;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide help documentation for JSON-RPC methods.
 * <p>
 * This annotation can be used to document JSON-RPC method implementations
 * with both a brief summary and detailed help text that can be exposed
 * through a help/documentation system.
 * </p>
 *
 * Example usage:
 * <pre>
 * {@code
 * @JsonRpcHelp(
 *     summary = "Adds two numbers together",
 *     details = "This method takes two numeric parameters and returns their sum.\n" +
 *               "Parameters:\n" +
 *               "  - a: First number\n" +
 *               "  - b: Second number\n" +
 *               "Returns: The sum of a and b"
 * )
 * public int add(int a, int b) {
 *     return a + b;
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonRpcHelpText {

    /**
     * A brief one-line summary of what the JSON-RPC method does.
     * This should be concise and descriptive.
     *
     * @return the summary help text
     */
    String summary();

    /**
     * Detailed multi-line help documentation for the JSON-RPC method.
     * This can include information about parameters, return values,
     * examples, exceptions, and any other relevant details.
     *
     * @return the detailed help text
     */
    String details() default "";
}