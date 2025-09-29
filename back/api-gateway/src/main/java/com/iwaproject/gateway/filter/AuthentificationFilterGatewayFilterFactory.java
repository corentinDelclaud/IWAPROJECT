package com.iwaproject.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthentificationFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthentificationFilterGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthentificationFilterGatewayFilterFactory.class);

    public AuthentificationFilterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Laisse passer le preflight CORS
            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                return chain.filter(exchange);
            }

            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                logger.debug("[AuthentificationFilter] Jeton manquant ou invalide");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // TODO: valider réellement le jeton (JWT, appel vers service\-auth, etc.)
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Placeholders pour une configuration future si nécessaire
    }
}
