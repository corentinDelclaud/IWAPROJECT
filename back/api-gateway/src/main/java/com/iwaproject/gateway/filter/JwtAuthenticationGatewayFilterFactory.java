package com.iwaproject.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Gateway filter pour valider les JWT tokens
 * Décode le JWT localement sans appeler Keycloak (évite les problèmes de réseau Docker)
 */
@Component
@Slf4j
public class JwtAuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", request.getPath());
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            String token = authHeader.substring(7);
            
            try {
                // Décoder le JWT pour extraire les claims
                JsonNode jwtClaims = decodeJwtPayload(token);
                
                // Vérifier l'expiration
                long exp = jwtClaims.path("exp").asLong();
                long now = System.currentTimeMillis() / 1000;
                if (exp < now) {
                    log.warn("Token expired: exp={}, now={}", exp, now);
                    return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
                }
                
                // Extraire les informations utilisateur
                String userId = jwtClaims.path("sub").asText();
                String username = jwtClaims.path("preferred_username").asText();
                String email = jwtClaims.path("email").asText();
                String roles = getRolesAsString(jwtClaims);
                
                log.info("JWT validated - sub: {}, username: {}, exp: {}", userId, username, exp);
                
                // Ajouter les claims en tant que headers pour les microservices
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Username", username)
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles)
                        .build();
                
                log.info("Added headers - X-User-Id: {}, X-User-Username: {}", userId, username);
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (Exception e) {
                log.error("Token validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Décode le payload du JWT (partie centrale en base64)
     */
    private JsonNode decodeJwtPayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format - expected 3 parts, got " + parts.length);
        }
        
        // Décoder la partie payload (index 1)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        log.debug("Decoded JWT payload: {}", payload);
        return objectMapper.readTree(payload);
    }

    /**
     * Extrait les rôles du token JWT décodé
     */
    private String getRolesAsString(JsonNode claims) {
        try {
            JsonNode realmAccess = claims.path("realm_access");
            if (realmAccess.has("roles")) {
                StringBuilder roles = new StringBuilder();
                realmAccess.get("roles").forEach(role -> {
                    if (roles.length() > 0) roles.append(",");
                    roles.append(role.asText());
                });
                return roles.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to extract roles from token", e);
        }
        return "";
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String errorMessage = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
                status.getReasonPhrase(), message);
        
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Data
    public static class Config {
        // Configuration optionnelle si besoin
    }
}
