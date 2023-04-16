package org.consensusj.daemon.micronaut

import io.micronaut.context.ApplicationContext
import org.bitcoinj.base.BitcoinNetwork
import spock.lang.Specification

/**
 * Test the configuration record
 */
class BitcoinDaemonConfigSpec extends Specification {
    void "test default BitcoinDaemon configuration"() {
        given:
        var ctx = ApplicationContext.run(ApplicationContext)

        when:
        var config = ctx.getBean(BitcoinDaemonConfig)

        then:
        config.walletBaseName() == "CJBitcoinDaemon"
        config.networkId() == "testnet"
        config.network() == BitcoinNetwork.TESTNET
        config.serverPort() == 8080

        cleanup:
        ctx.close()
    }
}
