import org.jspecify.annotations.NullMarked;

/**
 * Java Module Declaration
 */
@NullMarked
module org.consensusj.jrpc {
    requires java.logging;

    requires org.slf4j;
    requires com.fasterxml.jackson.dataformat.toml;

    requires org.consensusj.jsonrpc.cli;
    requires org.jspecify;
}
