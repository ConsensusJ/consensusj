/**
 * Java Module Declaration
 */
module org.consensusj.jsonrpc {
    requires java.net.http;

    requires org.slf4j;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.jspecify;

    exports org.consensusj.jsonrpc;
    exports org.consensusj.jsonrpc.introspection;
    exports org.consensusj.jsonrpc.internal to com.fasterxml.jackson.databind;
    opens org.consensusj.jsonrpc to com.fasterxml.jackson.databind;
}
