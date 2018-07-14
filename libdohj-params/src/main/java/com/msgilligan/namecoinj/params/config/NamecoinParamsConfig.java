package com.msgilligan.namecoinj.params.config;

import org.bitcoinj.core.NetworkParameters;
import org.libdohj.params.NamecoinMainNetParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Namecoin Mainnet params
 */
@Configuration
public class NamecoinParamsConfig {
    @Bean
    public NetworkParameters networkParameters() {
        return NamecoinMainNetParams.get();
    }
}
