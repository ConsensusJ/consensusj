package com.msgilligan.bitcoinj.daemon;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.msgilligan.bitcoinj.daemon.config.BitcoinConfig;
import com.msgilligan.bitcoinj.params.config.BitcoinParamsConfig;

/**
 * Spring Boot application container for **bitcoinj daemon**
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses={BitcoinParamsConfig.class, BitcoinConfig.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
