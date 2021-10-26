package org.consensusj.bitcoin.rpc.bitcoind

import spock.lang.Specification


/**
 *
 */
class BitcoinConfFileSpec extends Specification {
    static final testFilePath = "org/consensusj/bitcoin/rpc/bitcoind/"
    static final testFileNames = ["bitcoin.conf"]

    def "can read typical bitcoin.conf file"() {
        given:
        def path = testFilePath + testFileNames[0]
        def file = new File(ClassLoader.getSystemResource(path).toURI())
        def confFile = new BitcoinConfFile(file)

        when:
        def conf = confFile.read()

        then:
        conf.get("rpcconnect") == "127.0.0.1"
        conf.get("rpcport") == "8332"
        conf.get("rpcuser") == "bitcoinrpc"
        conf.get("rpcpassword") == "pass"
    }

    def "File not found returns defaults"() {
        given:
        def file = new File("bitcoinxxx.conf")
        def confFile = new BitcoinConfFile(file)

        when:
        def conf = confFile.readWithFallback()

        then:
        conf.get("rpcconnect") == "127.0.0.1"
        conf.get("rpcport") == "8332"
        conf.get("rpcuser") == ""
        conf.get("rpcpassword") == ""
    }

}