package com.msgilligan.bitcoinj.daemon.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring Web configuration
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcAutoConfiguration {
}
