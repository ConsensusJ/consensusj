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
package org.consensusj.jsonrpc.cli.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.spi.ToolProvider;

/**
 *  Support functions for testing command-line tools
 */
public interface CLITestSupport {
    /**
     * Run a command and capture status and output
     *
     * @param tool Command object instance to run
     * @return Object containing status, stdout, stderr
     */
    static CLICommandResult runTool(ToolProvider tool, String... args) {
        // Setup to capture output streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream bes = new ByteArrayOutputStream();
        PrintStream pos = new PrintStream(bos);
        PrintStream pes = new PrintStream(bes);

        // Run the command
        int status = tool.run(pos, pes, args);

        return new CLICommandResult(status, bos, bes);
    }
}
