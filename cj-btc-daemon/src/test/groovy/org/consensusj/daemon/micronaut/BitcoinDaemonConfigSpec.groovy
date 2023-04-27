package org.consensusj.daemon.micronaut

import io.micronaut.context.ApplicationContext
import org.bitcoinj.base.BitcoinNetwork
import spock.lang.Specification

/**
 * Test the configuration record. It should be loaded from {@code resources/application-test.toml}.
 */
class BitcoinDaemonConfigSpec extends Specification {
    void "test default BitcoinDaemon configuration"() {
        given:
        var ctx = ApplicationContext.run(ApplicationContext)

        when:
        var config = ctx.getBean(BitcoinDaemonConfig)

        then:
        config.walletBaseName() == "CJBitcoinDaemon"
        config.networkId() == "regtest"
        config.network() == BitcoinNetwork.REGTEST
        config.serverPort() == 8080

        cleanup:
        ctx.close()
    }
}
