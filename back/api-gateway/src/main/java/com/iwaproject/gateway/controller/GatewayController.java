package com.iwaproject.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Autowired
    // private RouteLocator routeLocator;

    /**
     * Endpoint de santé de la Gateway
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "api-gateway");
        health.put("version", "1.0.0");

        return Mono.just(ResponseEntity.ok(health));
    }

    /**
     * Information sur les routes configurées
     */
    @GetMapping("/routes")
    public Mono<ResponseEntity<Map<String, Object>>> getRoutes() {
        Map<String, Object> routeInfo = new HashMap<>();
        routeInfo.put("message", "Routes actives dans la Gateway");
        routeInfo.put("timestamp", LocalDateTime.now());

        // Liste des services disponibles
        Map<String, String> services = new HashMap<>();
        services.put("service-auth", "Service d'authentification");
        services.put("service-user", "Service de gestion des utilisateurs");
        services.put("service-data", "Service de données métier");
        services.put("service-common", "Service de ressources communes");

        routeInfo.put("services", services);

        return Mono.just(ResponseEntity.ok(routeInfo));
    }

    /**
     * Endpoint pour tester la connectivité
     */
    @GetMapping("/ping")
    public Mono<ResponseEntity<Map<String, Object>>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        response.put("gateway", "api-gateway");

        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Information sur la configuration de la Gateway
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "IWA Project API Gateway");
        info.put("description", "Passerelle API pour l'architecture microservices");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now());

        Map<String, Object> features = new HashMap<>();
        features.put("cors", "Activé pour React Native");
        features.put("authentication", "JWT Bearer Token");
        features.put("logging", "Requêtes et réponses tracées");
        features.put("load_balancing", "Eureka Discovery");

        info.put("features", features);

        return Mono.just(ResponseEntity.ok(info));
    }
}
