package com.iwaproject.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Routes d'authentification - pas de filtre d'auth nécessaire
                .route("auth-login", r -> r.path("/api/auth/login")
                        .uri("lb://service-auth"))
                .route("auth-register", r -> r.path("/api/auth/register")
                        .uri("lb://service-auth"))
                .route("auth-refresh", r -> r.path("/api/auth/refresh")
                        .uri("lb://service-auth"))

                // Routes utilisateur - authentification requise
                .route("user-profile", r -> r.path("/api/users/profile/**")
                        .uri("lb://service-user"))
                .route("user-management", r -> r.path("/api/users/**")
                        .uri("lb://service-user"))

                // Routes de données - authentification requise
                .route("data-products", r -> r.path("/api/data/products/**")
                        .uri("lb://service-data"))
                .route("data-orders", r -> r.path("/api/data/orders/**")
                        .uri("lb://service-data"))
                .route("data-marketplace", r -> r.path("/api/data/marketplace/**")
                        .uri("lb://service-data"))

                // Routes communes - publiques
                .route("common-health", r -> r.path("/api/common/health")
                        .uri("lb://service-common"))
                .route("common-config", r -> r.path("/api/common/config/**")
                        .uri("lb://service-common"))

                .build();
    }
}
