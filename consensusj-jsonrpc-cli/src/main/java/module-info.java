/**
 * Java Module Declaration
 */
module org.consensusj.jsonrpc.cli {
    requires java.logging;
    requires org.slf4j;

    requires transitive org.apache.commons.cli;
    requires transitive org.consensusj.jsonrpc;
    requires transitive com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.toml;

    exports org.consensusj.jsonrpc.cli;
    exports org.consensusj.jsonrpc.cli.config;
    exports org.consensusj.jsonrpc.cli.test;
}
