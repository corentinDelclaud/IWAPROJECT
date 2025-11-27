package com.iwaproject.gateway.filter;

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
import java.util.Objects;

/**
 * Gateway filter pour valider les JWT tokens
 */
@Component
@Slf4j
public class JwtAuthenticationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Extract Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", request.getPath());
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            String token = authHeader.substring(7);
            
            // Validate token
            if (!isValidToken(token)) {
                log.warn("Invalid JWT token for path: {}", request.getPath());
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
            
            log.debug("Valid JWT token for path: {}", request.getPath());
            
            // Continue with the request
            return chain.filter(exchange);
        };
    }

    /**
     * Validate JWT token
     * Basic validation: check structure and expiration
     */
    private boolean isValidToken(String token) {
        try {
            // Split token into parts
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT structure");
                return false;
            }
            
            // Decode payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Check if token contains required claims
            if (!payload.contains("\"sub\"") || !payload.contains("\"exp\"")) {
                log.warn("Missing required claims in JWT");
                return false;
            }
            
            // Extract expiration time
            String expStr = payload.replaceAll(".*\"exp\":(\\d+).*", "$1");
            long exp = Long.parseLong(expStr);
            long now = System.currentTimeMillis() / 1000;
            
            if (exp < now) {
                log.warn("JWT token has expired");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            return false;
        }
    }

    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String errorMessage = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
                status.getReasonPhrase(), message);
        
        byte[] bytes = Objects.requireNonNull(errorMessage.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Objects.requireNonNull(Mono.just(response.bufferFactory().wrap(bytes))));
    }


    @Data
    public static class Config {
        // Configuration properties if needed
        private boolean validateExpiration = true;
        private boolean validateSignature = false; // Can be enabled with proper key configuration
    }
}
