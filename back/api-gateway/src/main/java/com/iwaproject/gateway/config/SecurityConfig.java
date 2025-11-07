package com.iwaproject.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration de sécurité pour l'API Gateway
 * La Gateway ne valide pas les JWT elle-même (c'est fait par le filtre custom)
 * mais elle autorise tous les endpoints publics
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Endpoints publics (actuator, health)
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers("/realms/**").permitAll()
                .pathMatchers("/resources/**").permitAll()
                .pathMatchers("/api/webhooks/**").permitAll()
                
                // Tous les autres endpoints sont gérés par le JWT filter custom
                .anyExchange().permitAll()
            );
        
        return http.build();
    }
}
