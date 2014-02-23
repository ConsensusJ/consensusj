package com.msgilligan.peerlist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * User: sean
 * Date: 2/22/14
 * Time: 11:22 PM
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() //TODO Refactor login form
                .authorizeRequests()
                .antMatchers("/style/**").permitAll()
                .antMatchers("/lib/**").permitAll()
//                .antMatchers("/stomp/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutSuccessUrl("/login.html?logout")
                .logoutUrl("/logout.html")
                .permitAll()
                .and()
                .formLogin()
                .defaultSuccessUrl("/index.html")
                .loginPage("/login.html")
                .failureUrl("/login.html?error")
                .permitAll();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("sean").password("sean").roles("ADMIN","USER").and()
                .withUser("taariq").password("taariq").roles("USER").and()
                .withUser("kevin").password("kevin").roles("USER");
    }
}