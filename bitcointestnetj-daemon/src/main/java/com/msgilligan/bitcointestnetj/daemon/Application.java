package com.msgilligan.bitcointestnetj.daemon;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot application container for **bitcoinj daemon**
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages={"com.msgilligan.bitcoinj.params.testnet", "com.msgilligan.bitcoinj.daemon.config"})
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
