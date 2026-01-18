package org.consensusj.daemon.micronaut;

import io.micronaut.context.ApplicationContext;
import org.bitcoinj.base.BitcoinNetwork;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitcoinDaemonConfigTest {
    private ApplicationContext ctx;

    @Test
    void testDefaultBitcoinDaemonConfiguration() {
        ctx = ApplicationContext.run(ApplicationContext.class);
        var config = ctx.getBean(BitcoinDaemonConfig.class);

        assertEquals("walletd", config.walletBaseName());
        assertEquals("regtest", config.networkId());
        assertEquals(BitcoinNetwork.REGTEST, config.network());
        assertEquals(8080, config.serverPort());
    }

    @AfterEach
    void cleanup() {
        ctx.close();
    }
}
