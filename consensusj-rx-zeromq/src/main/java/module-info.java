/**
 * Java Module Declaration
 */
module org.consensusj.rx.zeromq {
    requires org.slf4j;
    requires io.reactivex.rxjava3;
    requires org.zeromq.jeromq;

    exports org.consensusj.rx.zeromq;
}