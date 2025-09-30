# API Gateway - IWA Project

## Vue d'ensemble

L'API Gateway est un service Spring Boot qui sert de point d'entrée unique pour votre application. Elle gère le routage des requêtes, la configuration CORS, et peut servir de proxy intelligent vers d'autres services.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Services      │
│   (React/RN)    │◄──►│   (Port: 8080)  │◄──►│   Externes      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Technologies utilisées

- **Spring Boot 2.7.14** - Framework principal
- **Spring Cloud Gateway** - API Gateway réactive
- **Netty** - Serveur web réactif
- **Maven** - Gestion des dépendances

## Structure du projet

```
api-gateway/
├── pom.xml                                    # Configuration Maven
├── src/main/
│   ├── java/com/iwaproject/gateway/
│   │   ├── ApiGatewayApplication.java         # Classe principale
│   │   ├── config/
│   │   │   ├── CorsConfig.java               # Configuration CORS
│   │   │   └── GatewayConfig.java            # Configuration Gateway
│   │   ├── controller/
│   │   │   └── GatewayController.java        # Contrôleur principal
│   │   └── filter/
│   │       └── LoggingGatewayFilterFactory.java # Filtre de logging
│   └── resources/
│       ├── application.yml                    # Configuration principale
│       └── bootstrap.yml                     # Configuration bootstrap
└── target/                                   # Répertoire de build
```

## Configuration

### Port et application
- **Port** : 8080
- **Nom** : api-gateway

### CORS
Configuration CORS prête pour React Native/Expo :
- `http://localhost:*` - Applications locales
- `http://127.0.0.1:*` - Applications locales (IP)
- `exp://*` - Applications Expo en développement
- `https://*.expo.dev` - Applications Expo publiées

### Méthodes HTTP supportées
- GET, POST, PUT, DELETE, PATCH, OPTIONS

## Démarrage

### Prérequis
- Java 11 ou supérieur
- Maven 3.6+

### Compilation
```bash
mvn clean compile
```

### Démarrage en développement
```bash
mvn spring-boot:run
```

### Build pour production
```bash
mvn clean package
java -jar target/api-gateway-1.0-SNAPSHOT.jar
```

## Endpoints disponibles

### Santé et monitoring
```bash
# Vérification de l'état du service
GET http://localhost:8080/actuator/health

# Information détaillée sur l'état
GET http://localhost:8080/actuator/info

# Routes configurées
GET http://localhost:8080/actuator/gateway/routes

# Métriques du service
GET http://localhost:8080/actuator/metrics
```

### Réponses typiques

#### Health Check
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

#### Routes Gateway
```json
[
  {
    "predicate": "Paths: [/api/exemple/**], match trailing slash: true",
    "route_id": "exemple-service",
    "filters": ["[[LoggingFilter], order = 1]"],
    "uri": "http://localhost:8081",
    "order": 0
  }
]
```

## Configuration des routes

### Ajouter de nouvelles routes
Éditez `src/main/resources/application.yml` :

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: mon-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/mon-service/**
          filters:
            - name: LoggingFilter
        - id: service-externe
          uri: https://api.exemple.com
          predicates:
            - Path=/external/**
          filters:
            - RewritePath=/external/(?<segment>.*), /${segment}
```

### Types de prédicats disponibles
- **Path** : `/api/users/**`
- **Method** : `GET,POST`
- **Header** : `X-Request-Id, \d+`
- **Query** : `version, v1`
- **Host** : `*.exemple.com`

### Filtres disponibles
- **LoggingFilter** : Trace les requêtes avec des IDs uniques
- **RewritePath** : Réécrit le chemin de la requête
- **AddRequestHeader** : Ajoute des headers à la requête
- **AddResponseHeader** : Ajoute des headers à la réponse

## Logging

### Configuration des logs
Le niveau de logging est configurable dans `application.yml` :

```yaml
logging:
  level:
    com.iwaproject.gateway: DEBUG
    org.springframework.cloud.gateway: INFO
```

### Format des logs
```
2025-09-29 11:10:30 [main] INFO  c.i.gateway.ApiGatewayApplication - Started ApiGatewayApplication in 10.386 seconds
```

### Filtre de logging
Chaque requête est tracée avec un ID unique :
```
=== REQUÊTE ENTRANTE [REQ-1696012345678-123] ===
Timestamp: 2025-09-29 11:10:30
Méthode: GET
URI: http://localhost:8080/api/users/profile
```

## Utilisation avec React Native

### Configuration Expo
```javascript
// Dans votre fichier de configuration Expo
const config = {
  extra: {
    apiUrl: __DEV__ 
      ? 'http://localhost:8080' 
      : 'https://votre-api-gateway.com'
  }
};
```

### Appels API
```javascript
// Exemple d'appel API depuis React Native
const apiCall = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/users/profile', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        // Ajoutez vos headers d'authentification ici
      },
    });
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Erreur API:', error);
  }
};
```

## Exemples de configuration

### Proxy vers un service local
```yaml
- id: backend-service
  uri: http://localhost:3000
  predicates:
    - Path=/api/backend/**
  filters:
    - RewritePath=/api/backend/(?<segment>.*), /${segment}
```

### Load balancing
```yaml
- id: load-balanced-service
  uri: lb://mon-service
  predicates:
    - Path=/api/service/**
```

### Ajout d'authentification
```yaml
- id: protected-service
  uri: http://localhost:8081
  predicates:
    - Path=/api/protected/**
  filters:
    - AddRequestHeader=X-Gateway-Auth, true
```

## Monitoring et métriques

### Métriques disponibles
- Nombre de requêtes par route
- Temps de réponse moyen
- Codes de statut HTTP
- Utilisation mémoire et CPU

### Intégration avec des outils de monitoring
L'API Gateway expose des métriques compatibles avec :
- **Prometheus** : `/actuator/prometheus`
- **Micrometer** : `/actuator/metrics`

## Sécurité

### CORS
La configuration CORS est adaptée pour le développement. En production, restreignez les origines :

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOriginPatterns:
        - "https://votre-domaine.com"
      allowedMethods: [GET, POST]
```

### Headers de sécurité
Ajoutez des headers de sécurité :

```yaml
filters:
  - AddResponseHeader=X-Content-Type-Options, nosniff
  - AddResponseHeader=X-Frame-Options, DENY
```

## Déploiement

### Variables d'environnement
```bash
# Port du serveur
SERVER_PORT=8080

# Niveau de logging
LOGGING_LEVEL_COM_IWAPROJECT_GATEWAY=INFO

# URL des services backend
BACKEND_SERVICE_URL=https://api.backend.com
```

### Docker
```dockerfile
FROM openjdk:11-jre-slim
COPY target/api-gateway-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Dépannage

### Problèmes courants

#### Port déjà utilisé
```bash
# Vérifier quel processus utilise le port 8080
netstat -ano | findstr :8080
```

#### Service ne démarre pas
1. Vérifiez la version de Java : `java -version`
2. Nettoyez et recompilez : `mvn clean compile`
3. Vérifiez les logs de démarrage

#### CORS ne fonctionne pas
1. Vérifiez la configuration dans `application.yml`
2. Assurez-vous que l'origine est incluse dans `allowedOriginPatterns`
3. Vérifiez que la méthode HTTP est autorisée

#### Routes ne fonctionnent pas
1. Vérifiez la configuration des routes dans `application.yml`
2. Consultez les routes actives : `GET /actuator/gateway/routes`
3. Vérifiez les logs du filtre de logging

### Logs de débogage
```bash
# Activer les logs détaillés
java -jar api-gateway.jar --logging.level.org.springframework.cloud.gateway=DEBUG
```

## Extension du projet

### Ajouter l'authentification
1. Intégrer JWT ou OAuth2
2. Créer un filtre d'authentification personnalisé
3. Configurer les routes protégées

### Ajouter la limitation de débit
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

### Intégrer des services externes
```yaml
- id: external-api
  uri: https://jsonplaceholder.typicode.com
  predicates:
    - Path=/api/posts/**
  filters:
    - RewritePath=/api/posts/(?<segment>.*), /posts/${segment}
```

Cette API Gateway est conçue pour être facilement extensible selon les besoins de votre projet.

## Fonctionnement technique détaillé de l'API Gateway

### 1. Rôle et fonctionnement

L’API Gateway (basée sur **Spring Cloud Gateway**) agit comme un point d’entrée unique pour toutes les requêtes HTTP venant du frontend (React, React Native, etc.) ou d’autres clients. Elle centralise :
- Le routage des requêtes vers les microservices
- La gestion CORS
- Le logging
- L’application de règles de sécurité ou de transformation

**Flux typique :**
1. Le frontend envoie une requête à la gateway (`http://localhost:8080`).
2. La gateway applique les règles globales (CORS, logs, etc.).
3. Elle route la requête vers le microservice approprié selon la configuration.
4. Le microservice répond à la gateway.
5. La gateway renvoie la réponse au frontend.

### 2. Ajout de routes et connexion aux microservices

Les routes sont définies dans `src/main/resources/application.yml` :

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - name: LoggingFilter
        - id: product-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/products/**
```

- **id** : identifiant unique de la route
- **uri** : URL du microservice cible
- **predicates** : conditions d’activation (ex : chemin)
- **filters** : filtres appliqués (ex : logs, réécriture d’URL)

Pour connecter un nouveau microservice, il suffit d’ajouter une nouvelle entrée dans la section `routes` avec le bon chemin et l’URL du service.

### 3. Connexion au frontend

Le frontend (React, React Native) communique uniquement avec la gateway. Exemple d’appel API :

```javascript
const response = await fetch('http://localhost:8080/api/users/profile', {
  method: 'GET',
  headers: { 'Content-Type': 'application/json' }
});
```

La configuration CORS (dans `CorsConfig.java` ou `application.yml`) permet d’accepter les requêtes du frontend.

### 4. Structure du code de la gateway

- `ApiGatewayApplication.java` : classe principale qui démarre l’application
- `config/GatewayConfig.java` : configuration des routes (optionnel, sinon via YAML)
- `config/CorsConfig.java` : configuration CORS
- `filter/LoggingGatewayFilterFactory.java` : filtre de logging personnalisé

**Exemple de classe principale :**
```java
package com.iwaproject.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

**Exemple de configuration CORS:**
```java
package com.iwaproject.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

**Exemple de filtre de logging :**
```java
package com.iwaproject.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    private static final Logger logger = LoggerFactory.getLogger(LoggingGatewayFilterFactory.class);

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            logger.info("=== REQUÊTE ENTRANTE ===\nMéthode: {}\nURI: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI());
            return chain.filter(exchange);
        };
    }
}
```

### 5. Résumé

- La gateway centralise tous les accès et applique des règles globales.
- Les routes sont configurables dynamiquement dans `application.yml`.
- Le frontend ne connaît que la gateway, ce qui simplifie la sécurité et l’architecture.
- Pour ajouter un microservice, il suffit d’ajouter une route.
