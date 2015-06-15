package com.msgilligan.peerlist.config;

import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * User: sean
 * Date: 2/22/14
 * Time: 11:22 PM
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcAutoConfiguration {
}
