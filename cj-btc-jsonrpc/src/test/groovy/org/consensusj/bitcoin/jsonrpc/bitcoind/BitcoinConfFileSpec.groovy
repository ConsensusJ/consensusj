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
package org.consensusj.bitcoin.jsonrpc.bitcoind

import org.consensusj.bitcoin.jsonrpc.bitcoind.BitcoinConfFile
import spock.lang.Specification


/**
 *
 */
class BitcoinConfFileSpec extends Specification {
    static final testFilePath = "org/consensusj/bitcoin/jsonrpc/bitcoind/"
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