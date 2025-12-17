/**
 * Java Module Declaration
 */
module org.consensusj.jsonrpc.groovy {
    requires org.slf4j;
    requires io.reactivex.rxjava3;

    requires org.consensusj.jsonrpc;

    exports org.consensusj.rx.jsonrpc;
}