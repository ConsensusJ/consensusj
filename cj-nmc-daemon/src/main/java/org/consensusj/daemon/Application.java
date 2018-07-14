package org.consensusj.daemon;

import org.consensusj.daemon.config.NamecoinConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Spring Boot application container for ** namecoinj daemon**
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses={NamecoinConfig.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
