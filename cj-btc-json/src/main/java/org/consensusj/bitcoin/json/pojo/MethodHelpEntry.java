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
