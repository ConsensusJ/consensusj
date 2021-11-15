package org.consensusj.bitcoin.json.pojo;

/**
 * An entry for a JSON-RPC method in a {@code help} response.
 */
public class MethodHelpEntry {
    private final String methodName;
    private final String methodParametersHelp;

    /**
     * Create an entry from a text line in the help response
     * @param entryLine A text line with the method name optionally followed by a space and parameter help info
     */
    public MethodHelpEntry(String entryLine) {
        this(entryLine.split(" ", 2));
    }

    /**
     * Create an entry from an array of 1 or 2 strings
     * @param components method name in {@code components[0]}, if {@code length == 2}, parameter help in {@code components[1]}
     */
    public MethodHelpEntry(String[] components) {
        this(components[0], components.length >= 2 ? components[1] : "");
    }

    /**
     * Create an entry from method name and parameter help
     * @param methodName name of RPC method
     * @param methodParametersHelp help for method parameters or {@code ""}
     */
    public MethodHelpEntry(String methodName, String methodParametersHelp) {
        this.methodName = methodName;
        this.methodParametersHelp = methodParametersHelp;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodParametersHelp() {
        return methodParametersHelp;
    }
}
