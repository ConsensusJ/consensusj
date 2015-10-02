package com.msgilligan.bitcoinj.daemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot application container for **bitcoinj daemon**
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages="com.msgilligan.bitcoinj.daemon")
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setShowBanner(false);
        ApplicationContext ctx = app.run();
    }
}
