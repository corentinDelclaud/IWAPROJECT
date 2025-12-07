package com.iwaproject.gateway.config;

import com.iwaproject.gateway.filter.JwtAuthenticationGatewayFilterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationGatewayFilterFactory jwtAuthFilter;

    @Value("${services.auth.url:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${services.user.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.product.url:http://localhost:8083}")
    private String productServiceUrl;

    @Value("${services.transaction.url:http://localhost:8084}")
    private String transactionServiceUrl;

    @Value("${services.keycloak.url:http://localhost:8080}")
    private String keycloakUrl;
    
        @Value("${services.stripe.url:http://localhost:8090}")
        private String stripeServiceUrl;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // ==================== KEYCLOAK ROUTES ====================
                .route("keycloak-realms", r -> r
                        .path("/realms/**")
                        .filters(f -> f
                                .removeRequestHeader("Cookie")
                                .preserveHostHeader())
                        .uri(keycloakUrl))
                
                .route("keycloak-resources", r -> r
                        .path("/resources/**")
                        .uri(keycloakUrl))
                
                // ==================== AUTH SERVICE ROUTES ====================
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri(authServiceUrl))
                
                .route("auth-register", r -> r
                        .path("/api/auth/register")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri(authServiceUrl))
                
                .route("auth-refresh", r -> r
                        .path("/api/auth/refresh")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri(authServiceUrl))
                
                .route("auth-logout", r -> r
                        .path("/api/auth/logout")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri(authServiceUrl))
                
                .route("auth-health", r -> r
                        .path("/api/auth/health")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri(authServiceUrl))
                
                .route("auth-actuator", r -> r
                        .path("/api/auth/actuator/**")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/${segment}"))
                        .uri(authServiceUrl))
                
                // ==================== USER SERVICE ROUTES ====================
                .route("user-actuator", r -> r
                        .order(1)
                        .path("/api/users/actuator/**")
                        .filters(f -> f.rewritePath("/api/users/(?<segment>.*)", "/${segment}"))
                        .uri(userServiceUrl))
                
                .route("user-profile", r -> r
                        .order(2)
                        .path("/api/users/profile")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri(userServiceUrl))
                
                .route("user-management", r -> r
                        .order(3)
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri(userServiceUrl))
                
                // ==================== CATALOG/PRODUCT SERVICE ROUTES ====================
                .route("catalog-get-all", r -> r
                        .order(1)
                        .path("/api/products")
                        .and().method("GET")
                        .uri(productServiceUrl))
                
                .route("catalog-get-by-id", r -> r
                        .order(2)
                        .path("/api/products/{id}")
                        .and().method("GET")
                        .uri(productServiceUrl))
                
                .route("catalog-search", r -> r
                        .order(3)
                        .path("/api/products/search")
                        .and().method("GET")
                        .uri(productServiceUrl))
                
                .route("catalog-by-game", r -> r
                        .order(4)
                        .path("/api/products/game/**")
                        .and().method("GET")
                        .uri(productServiceUrl))
                
                .route("catalog-by-type", r -> r
                        .order(5)
                        .path("/api/products/type/**")
                        .and().method("GET")
                        .uri(productServiceUrl))
                
                .route("catalog-by-provider", r -> r
                        .order(6)
                        .path("/api/products/provider/**")
                        .and().method("GET")
                        .uri(productServiceUrl))
                
                .route("catalog-create", r -> r
                        .order(7)
                        .path("/api/products")
                        .and().method("POST")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri(productServiceUrl))
                
                .route("catalog-update", r -> r
                        .order(8)
                        .path("/api/products/{id}")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri(productServiceUrl))
                
                .route("catalog-delete", r -> r
                        .order(9)
                        .path("/api/products/{id}")
                        .and().method("DELETE")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri(productServiceUrl))
                
                .route("catalog-toggle", r -> r
                        .order(10)
                        .path("/api/products/{id}/toggle-availability")
                        .and().method("PATCH")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri(productServiceUrl))
                
                // ==================== WEBHOOK ROUTES ====================
                .route("webhooks", r -> r
                        .path("/api/webhooks/**")
                        .filters(f -> f.rewritePath("/api/webhooks/(?<segment>.*)", "/api/webhooks/${segment}"))
                        .uri(userServiceUrl))
                
                // ==================== STRIPE SERVICE ROUTES ====================
                .route("stripe-service", r -> r
                        .path("/api/stripe/**")
                        .filters(f -> f.rewritePath("/api/stripe/(?<segment>.*)", "/api/stripe/${segment}"))
                        .uri(stripeServiceUrl))
                // ==================== TRANSACTION SERVICE ROUTES ====================
                .route("transaction-my", r -> r
                        .order(0)
                        .path("/api/transactions/my")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/api/transactions/my", "/transaction/my"))
                        .uri(transactionServiceUrl))
                
                .route("transaction-create", r -> r
                        .order(1)
                        .path("/api/transactions")
                        .and().method("POST")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/api/transactions", "/transaction"))
                        .uri(transactionServiceUrl))
                
                .route("transaction-get", r -> r
                        .order(2)
                        .path("/api/transactions/{id}")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/api/transactions/(?<segment>.*)", "/transaction/${segment}"))
                        .uri(transactionServiceUrl))
                
                .route("transaction-update-state", r -> r
                        .order(3)
                        .path("/api/transactions/{id}/state")
                        .and().method("PUT", "PATCH")  // Accepter les deux méthodes
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/api/transactions/(?<segment>.*)", "/transaction/${segment}"))
                        .uri(transactionServiceUrl))
                
                // SSE endpoint pour les transactions
                .route("transaction-sse-single", r -> r
                        .order(4)
                        .path("/api/transactions/sse/{transactionId}")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/api/transactions/sse/(?<segment>.*)", "/transaction/sse/${segment}"))
                        .uri(transactionServiceUrl))
                
                .route("transaction-sse-user", r -> r
                        .order(5)
                        .path("/api/transactions/sse/user")
                        .filters(f -> f
                                .filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config()))
                                .rewritePath("/api/transactions/sse/user", "/transaction/sse/user"))
                        .uri(transactionServiceUrl))
                
                .route("transaction-swagger", r -> r
                        .order(6)
                        .path("/api/transactions/swagger-ui/**", "/api/transactions/v3/api-docs/**")
                        .filters(f -> f.rewritePath("/api/transactions/(?<segment>.*)", "/${segment}"))
                        .uri(transactionServiceUrl))
                
                .build();
    }
}
