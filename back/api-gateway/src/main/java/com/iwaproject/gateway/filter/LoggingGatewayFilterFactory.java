// back/api-gateway/src/main/java/com/iwaproject/gateway/filter/LoggingGatewayFilterFactory.java
package com.iwaproject.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGatewayFilterFactory.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();
            String timestamp = LocalDateTime.now().format(formatter);
            String requestId = generateRequestId();

            logger.info("=== REQUÊTE ENTRANTE [{}] ===", requestId);
            logger.info("Timestamp: {}", timestamp);
            logger.info("Méthode: {}", exchange.getRequest().getMethod().name());
            logger.info("URI: {}", exchange.getRequest().getURI());
            logger.info("Path: {}", exchange.getRequest().getPath());
            logger.info("Query Params: {}", exchange.getRequest().getQueryParams());
            logger.info("Headers: {}", exchange.getRequest().getHeaders());
            logger.info("Remote Address: {}", exchange.getRequest().getRemoteAddress());

            return chain.filter(exchange)
                    .doOnSuccess(aVoid -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.info("=== RÉPONSE SORTANTE [{}] ===", requestId);
                        logger.info("Status: {}", exchange.getResponse().getStatusCode());
                        logger.info("Duration: {}ms", duration);
                        logger.info("Response Headers: {}", exchange.getResponse().getHeaders());
                    })
                    .doOnError(throwable -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.error("=== ERREUR [{}] ===", requestId);
                        logger.error("Duration: {}ms", duration);
                        logger.error("Error: {}", throwable.getMessage(), throwable);
                    });
        };
    }

    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" +
               new java.util.Random().nextInt(1000);
    }
}
