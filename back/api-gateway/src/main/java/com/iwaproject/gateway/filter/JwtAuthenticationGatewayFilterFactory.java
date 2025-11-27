package com.iwaproject.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway filter pour valider les JWT tokens
 */
@Component
@Slf4j
public class JwtAuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    @Value("${keycloak.auth-server-url:http://keycloak:8085}")
    private String keycloakUrl;

    @Value("${keycloak.realm:IWA_NextLevel}")
    private String realm;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
        this.webClient = WebClient.builder().build();
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
            
            // Validation avec Keycloak
            return validateTokenWithKeycloak(token)
                    .flatMap(claims -> {
                        // Ajouter les claims en tant que headers pour les microservices
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", claims.get("sub").asText())
                                .header("X-User-Username", claims.get("preferred_username").asText())
                                .header("X-User-Email", claims.path("email").asText())
                                .header("X-User-Roles", getRolesAsString(claims))
                                .build();
                        
                        log.debug("Valid JWT token for user: {} on path: {}", 
                                claims.get("preferred_username").asText(), request.getPath());
                        
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(e -> {
                        log.error("Token validation failed: {}", e.getMessage());
                        return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                    });
        };
    }

    /**
     * Valide le token via l'endpoint userinfo de Keycloak
     * Cela vérifie automatiquement la signature et l'expiration
     */
    private Mono<JsonNode> validateTokenWithKeycloak(String token) {
        String userinfoUrl = String.format("%s/realms/%s/protocol/openid-connect/userinfo", 
                keycloakUrl, realm);
        
        return webClient.get()
                .uri(userinfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        return Mono.just(objectMapper.readTree(response));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse userinfo response", e));
                    }
                })
                .doOnError(e -> log.error("Keycloak validation failed: {}", e.getMessage()));
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
        private boolean validateWithKeycloak = true;
    }
}
