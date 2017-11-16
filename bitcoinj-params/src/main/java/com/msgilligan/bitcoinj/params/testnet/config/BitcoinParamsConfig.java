package com.msgilligan.bitcoinj.params.testnet.config;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Bitcoin Testnet params
 */
@Configuration
public class BitcoinParamsConfig {
    @Bean
    public NetworkParameters networkParameters() {
        return TestNet3Params.get();
    }
}
