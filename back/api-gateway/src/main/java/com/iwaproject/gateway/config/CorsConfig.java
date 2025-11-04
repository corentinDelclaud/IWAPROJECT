package com.iwaproject.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permettre les credentials (cookies, headers d'authentification)
        config.setAllowCredentials(true);

        // Origines autorisées (React Native + développement local)
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "exp://*",  // Expo development
            "https://*.expo.dev",  // Expo production
            "capacitor://*"  // Si utilisation de Capacitor
        ));

        // Méthodes HTTP autorisées
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers autorisés
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Auth-Token"
        ));

        // Headers exposés au client
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Auth-Token",
            "X-Total-Count"
        ));

        // Durée de cache pour les requêtes preflight
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
