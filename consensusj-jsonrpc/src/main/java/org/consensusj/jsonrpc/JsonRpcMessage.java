package org.consensusj.jsonrpc;

/**
 * In the future this may be a superclass for {@link JsonRpcRequest} and {@link JsonRpcResponse}.
 * For now it just contains the {@code Version enum}.
 */
public class JsonRpcMessage {
    public enum Version {
        V1("1.0"),
        V2("2.0");

        private final String jsonrpc;

        /**
         * Constructor with value for the {@code jsonrpc} message field.
         *
         * @param jsonrpc The value of the {@code jsonrpc} field in a JSON-RPC message
         */
        Version(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        /**
         * Get the value for the message field.
         *
         * @return the value for the {@code jsonrpc} message field
         */
        public String jsonrpc() {
            return jsonrpc;
        }

        public String toString() {
            return jsonrpc;
        }
    }
}
