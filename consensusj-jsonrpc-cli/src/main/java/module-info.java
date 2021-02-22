/**
 * Module information
 */
module org.consensusj.jsonrpc.cli {
    requires java.logging;
    requires org.slf4j;

    requires /* transitive */ org.apache.commons.cli;    // Non-modular, relies on processing by 'extra-java-module-info' plugin
    requires /* transitive */ org.consensusj.jsonrpc;    // Automatic module from sibling subproject, not handled correctly by IntelliJ
    requires com.fasterxml.jackson.databind;

    exports org.consensusj.jsonrpc.cli;
    exports org.consensusj.jsonrpc.cli.test;
}
