package com.ecommerce.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/payments/create").permitAll()
                .requestMatchers("/api/payments/vnpay/callback").permitAll()
                .requestMatchers("/api/payments/*").permitAll()
                .requestMatchers("/api/payments/order/*").permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {});

        return http.build();
    }
}

