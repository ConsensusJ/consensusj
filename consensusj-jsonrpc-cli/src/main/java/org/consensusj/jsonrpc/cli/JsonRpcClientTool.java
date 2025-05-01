package org.consensusj.jsonrpc.cli;

/**
 * This interface was a mistake. It should be phased out.
 */
interface JsonRpcClientTool {
    class ToolException extends RuntimeException {
        public final int resultCode;

        public ToolException(int resultCode, String resultMessage) {
            super(resultMessage);
            this.resultCode = resultCode;
        }
    }

    enum OutputObject {
        RESPONSE,
        RESULT
    }

    enum OutputFormat {
        JSON,
        JAVA
    }

    enum OutputStyle {
        DEFAULT,
        PRETTY
    }
}
