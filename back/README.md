# Backend - API Gateway Standalone

## Vue d'ensemble

Ce projet contient uniquement une API Gateway Spring Boot qui peut servir de point d'entrée unique pour votre architecture. L'API Gateway peut être utilisée pour router les requêtes, gérer les CORS, et servir de proxy intelligent.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │
│   (React/RN)    │◄──►│   (Port: 8080)  │
└─────────────────┘    └─────────────────┘
```

### Technologies utilisées
- **Spring Boot 2.7.14** - Framework principal
- **Spring Cloud Gateway** - API Gateway réactive
- **Maven** - Gestion des dépendances

## Prérequis

- **Java 11** ou supérieur
- **Maven 3.6+**
- **Git**

## Structure du projet

```
back/
├── pom.xml                     # POM parent
└── api-gateway/                # API Gateway standalone
    ├── pom.xml
    └── src/main/
        ├── java/com/iwaproject/gateway/
        │   ├── ApiGatewayApplication.java
        │   ├── config/         # Configuration CORS et routes
        │   ├── controller/     # Endpoints de santé
        │   └── filter/         # Filtres de logging et authentification
        └── resources/
            ├── application.yml # Configuration des routes
            └── bootstrap.yml
```

## Guide de démarrage

### Étape 1: Compilation du projet

```bash
cd back
mvn clean install
```

### Étape 2: Démarrage de l'API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

L'API Gateway sera accessible sur http://localhost:8080

## Configuration

### API Gateway (Port: 8080)
- **URL** : http://localhost:8080
- **Health Check** : http://localhost:8080/actuator/health
- **Routes configurées** : http://localhost:8080/actuator/gateway/routes

## Endpoints disponibles

### Endpoints de santé et monitoring
```bash
# Santé de l'API Gateway
GET http://localhost:8080/actuator/health

# Information sur les routes configurées
GET http://localhost:8080/actuator/gateway/routes

# Métriques
GET http://localhost:8080/actuator/metrics
```

## Configuration des routes

L'API Gateway peut être configurée pour router vers d'autres services en modifiant le fichier `api-gateway/src/main/resources/application.yml`.

Exemple de configuration de routes :
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: exemple-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/exemple/**
```

## Développement

### Ajout de nouvelles routes

Pour ajouter de nouvelles routes, éditez le fichier `api-gateway/src/main/resources/application.yml` :

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: nouveau-service
          uri: http://localhost:8085
          predicates:
            - Path=/api/nouveau/**
          filters:
            - name: LoggingFilter  # Optionnel : ajout de logs
```

### Configuration CORS

La configuration CORS est déjà présente dans `api-gateway/src/main/java/com/iwaproject/gateway/config/CorsConfig.java` et permet les appels depuis :
- `http://localhost:*`
- `exp://*` (pour Expo/React Native)
- `https://*.expo.dev`

### Filtres disponibles

L'API Gateway inclut plusieurs filtres préconfiguré :
- **LoggingFilter** : Trace toutes les requêtes avec des IDs uniques
- **AuthenticationFilter** : Prêt pour l'authentification JWT (à configurer)

## Monitoring et Debug

### Vérification de l'état du service
```bash
# Vérifier que le port est utilisé
netstat -an | findstr :8080

# Tester la connectivité
curl http://localhost:8080/actuator/health
```

### Logs
- L'API Gateway logge au niveau DEBUG
- Chaque requête est tracée avec un ID unique pour faciliter le debugging

## Dépannage

### Problèmes courants

#### Port déjà utilisé
```bash
# Identifier le processus utilisant le port 8080
netstat -ano | findstr :8080
```

#### Service ne démarre pas
1. Vérifier que Java 11+ est installé : `java -version`
2. Vérifier que Maven est installé : `mvn -version`
3. Recompiler le service : `mvn clean compile`
4. Vérifier les logs de démarrage

## Utilisation en tant que Proxy

L'API Gateway peut servir de proxy vers d'autres services. Exemples d'utilisation :

### Proxy vers des APIs externes
```yaml
- id: external-api
  uri: https://api.externe.com
  predicates:
    - Path=/external/**
  filters:
    - RewritePath=/external/(?<segment>.*), /${segment}
```

### Proxy vers des microservices locaux
```yaml
- id: microservice-local
  uri: http://localhost:3000
  predicates:
    - Path=/api/local/**
```

## Évolution du projet

Cette API Gateway peut facilement évoluer pour :
1. **Ajouter l'authentification** : Intégrer JWT, OAuth2, etc.
2. **Ajouter des microservices** : Router vers de nouveaux services
3. **Ajouter du load balancing** : Distribuer la charge entre plusieurs instances
4. **Ajouter de la sécurité** : Rate limiting, validation des requêtes, etc.

### Prochaines étapes possibles
- Intégration avec un système d'authentification (Keycloak, Auth0, etc.)
- Ajout de microservices backend
- Configuration d'une base de données
- Déploiement sur le cloud

Cette architecture minimaliste est **idéale pour** :
- Un projet en phase de démarrage
- Une API Gateway standalone
- Un point d'entrée simple pour des services externes
- Un environnement de développement léger
