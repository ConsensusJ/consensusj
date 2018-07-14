package com.msgilligan.bitcoinj.params.config;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Bitcoin Mainnet params
 */
@Configuration
public class BitcoinParamsConfig {
    @Bean
    public NetworkParameters networkParameters() {
        return MainNetParams.get();
    }
}
