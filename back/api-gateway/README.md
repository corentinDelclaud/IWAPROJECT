# API Gateway# API Gateway - IWA Project



Point d'entrée unique pour toute l'architecture microservices IWA Project.## Vue d'ensemble



## 🎯 RôleL'API Gateway est un service Spring Boot qui sert de point d'entrée unique pour votre application. Elle gère le routage des requêtes, la configuration CORS, et peut servir de proxy intelligent vers d'autres services.



L'API Gateway est le **seul point d'accès** pour le frontend. Elle gère :## Architecture



- ✅ **Routing** : Redirection des requêtes vers les microservices appropriés```

- ✅ **Authentication** : Validation des JWT tokens┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐

- ✅ **CORS** : Configuration centralisée des origines autorisées│   Frontend      │    │   API Gateway   │    │   Services      │

- ✅ **Rate Limiting** : Protection contre les abus (à venir)│   (React/RN)    │◄──►│   (Port: 8080)  │◄──►│   Externes      │

- ✅ **Logging** : Traçabilité de toutes les requêtes└─────────────────┘    └─────────────────┘    └─────────────────┘

- ✅ **Retry Logic** : Réessayer automatiquement en cas d'erreur temporaire```

- ✅ **Circuit Breaker** : Protection contre les services défaillants (à venir)

## Technologies utilisées

## 🏗️ Architecture

- **Spring Boot 2.7.14** - Framework principal

```- **Spring Cloud Gateway** - API Gateway réactive

┌─────────────┐- **Netty** - Serveur web réactif

│  Frontend   │- **Maven** - Gestion des dépendances

│  (Expo)     │

└──────┬──────┘## Structure du projet

       │

       │ Port 8080 UNIQUEMENT```

       │api-gateway/

       ▼├── pom.xml                                    # Configuration Maven

┌──────────────────────────────────┐├── src/main/

│      API Gateway :8080           ││   ├── java/com/iwaproject/gateway/

│  ┌────────────────────────────┐  ││   │   ├── ApiGatewayApplication.java         # Classe principale

│  │  Routes Configuration      │  ││   │   ├── config/

│  │  - /api/auth/**           │  ││   │   │   ├── CorsConfig.java               # Configuration CORS

│  │  - /api/users/**          │  ││   │   │   └── GatewayConfig.java            # Configuration Gateway

│  │  - /realms/**             │  ││   │   ├── controller/

│  └────────────────────────────┘  ││   │   │   └── GatewayController.java        # Contrôleur principal

│  ┌────────────────────────────┐  ││   │   └── filter/

│  │  JWT Validation Filter     │  ││   │       └── LoggingGatewayFilterFactory.java # Filtre de logging

│  │  (pour routes protégées)   │  ││   └── resources/

│  └────────────────────────────┘  ││       ├── application.yml                    # Configuration principale

│  ┌────────────────────────────┐  ││       └── bootstrap.yml                     # Configuration bootstrap

│  │  CORS Configuration        │  │└── target/                                   # Répertoire de build

│  └────────────────────────────┘  │```

└──────┬───────────────────────────┘

       │## Configuration

       ├──────────────┬──────────────┬──────────────┐

       │              │              │              │### Port et application

       ▼              ▼              ▼              ▼- **Port** : 8080

┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐- **Nom** : api-gateway

│Keycloak  │   │Auth Svc  │   │User Svc  │   │Other Svc │

│(internal)│   │  :8082   │   │  :8081   │   │  :808X   │### CORS

│          │   │(internal)│   │(internal)│   │(internal)│Configuration CORS prête pour React Native/Expo :

└──────────┘   └──────────┘   └──────────┘   └──────────┘- `http://localhost:*` - Applications locales

```- `http://127.0.0.1:*` - Applications locales (IP)

- `exp://*` - Applications Expo en développement

## 📡 Routes configurées- `https://*.expo.dev` - Applications Expo publiées



### Routes publiques (pas de JWT requis)### Méthodes HTTP supportées

- GET, POST, PUT, DELETE, PATCH, OPTIONS

| Méthode | Chemin | Destination | Description |

|---------|--------|-------------|-------------|## Démarrage

| POST | `/api/auth/login` | Auth Service :8082 | Authentification |

| POST | `/api/auth/register` | Auth Service :8082 | Inscription |### Prérequis

| POST | `/api/auth/refresh` | Auth Service :8082 | Refresh token |- Java 11 ou supérieur

| POST | `/api/auth/logout` | Auth Service :8082 | Déconnexion |- Maven 3.6+

| GET | `/api/auth/health` | Auth Service :8082 | Health check |

| GET | `/realms/**` | Keycloak :8080 | Resources Keycloak |### Compilation

| GET | `/resources/**` | Keycloak :8080 | Static resources |```bash

| POST | `/api/webhooks/**` | User Service :8081 | Webhooks internes |mvn clean compile

```

### Routes protégées (JWT requis)

### Démarrage en développement

| Méthode | Chemin | Destination | Description |```bash

|---------|--------|-------------|-------------|mvn spring-boot:run

| GET | `/api/users/profile` | User Service :8081 | Profil utilisateur |```

| PUT | `/api/users/profile` | User Service :8081 | Mise à jour profil |

| GET | `/api/users/**` | User Service :8081 | Autres opérations user |### Build pour production

```bash

## 🔐 Validation JWTmvn clean package

java -jar target/api-gateway-1.0-SNAPSHOT.jar

Le filtre `JwtAuthenticationGatewayFilterFactory` valide les tokens JWT pour les routes protégées :```



### Validations effectuées :## Endpoints disponibles

1. ✅ Présence du header `Authorization: Bearer <token>`

2. ✅ Structure JWT valide (3 parties : header.payload.signature)### Santé et monitoring

3. ✅ Présence des claims requis (`sub`, `exp`)```bash

4. ✅ Token non expiré# Vérification de l'état du service

GET http://localhost:8080/actuator/health

### Réponse en cas d'erreur :

```json# Information détaillée sur l'état

{GET http://localhost:8080/actuator/info

  "error": "Unauthorized",

  "message": "Missing or invalid Authorization header"# Routes configurées

}GET http://localhost:8080/actuator/gateway/routes

```

# Métriques du service

**Status code**: `401 Unauthorized`GET http://localhost:8080/actuator/metrics

```

## ⚙️ Configuration

### Réponses typiques

### Variables d'environnement

#### Health Check

```yaml```json

# application.yml{

server:  "status": "UP",

  port: 8080  "components": {

    "diskSpace": {"status": "UP"},

services:    "ping": {"status": "UP"}

  auth:  }

    url: http://localhost:8082      # Auth Service}

  user:```

    url: http://localhost:8081      # User Service

  keycloak:#### Routes Gateway

    url: http://localhost:8080      # Keycloak```json

```[

  {

### Pour Docker    "predicate": "Paths: [/api/exemple/**], match trailing slash: true",

    "route_id": "exemple-service",

```yaml    "filters": ["[[LoggingFilter], order = 1]"],

# docker-compose.yml    "uri": "http://localhost:8081",

services:    "order": 0

  api-gateway:  }

    environment:]

      - SERVICES_AUTH_URL=http://auth-service:8082```

      - SERVICES_USER_URL=http://user-service:8081

      - SERVICES_KEYCLOAK_URL=http://keycloak:8080## Configuration des routes

```

### Ajouter de nouvelles routes

## 🚀 DémarrageÉditez `src/main/resources/application.yml` :



### En local```yaml

spring:

```bash  cloud:

cd back/api-gateway    gateway:

mvn clean install      routes:

mvn spring-boot:run        - id: mon-service

```          uri: http://localhost:8081

          predicates:

La Gateway sera accessible sur `http://localhost:8080`            - Path=/api/mon-service/**

          filters:

### Avec Docker            - name: LoggingFilter

        - id: service-externe

```bash          uri: https://api.exemple.com

docker-compose up api-gateway          predicates:

```            - Path=/external/**

          filters:

## 🧪 Tests            - RewritePath=/external/(?<segment>.*), /${segment}

```

### 1. Health check

```bash### Types de prédicats disponibles

curl http://localhost:8080/actuator/health- **Path** : `/api/users/**`

```- **Method** : `GET,POST`

- **Header** : `X-Request-Id, \d+`

**Réponse attendue** :- **Query** : `version, v1`

```json- **Host** : `*.exemple.com`

{

  "status": "UP"### Filtres disponibles

}- **LoggingFilter** : Trace les requêtes avec des IDs uniques

```- **RewritePath** : Réécrit le chemin de la requête

- **AddRequestHeader** : Ajoute des headers à la requête

### 2. Lister les routes- **AddResponseHeader** : Ajoute des headers à la réponse

```bash

curl http://localhost:8080/actuator/gateway/routes## Logging

```

### Configuration des logs

### 3. Test login (route publique)Le niveau de logging est configurable dans `application.yml` :

```bash

curl -X POST http://localhost:8080/api/auth/login \```yaml

  -H "Content-Type: application/json" \logging:

  -d '{  level:

    "username": "testuser",    com.iwaproject.gateway: DEBUG

    "password": "Test123!"    org.springframework.cloud.gateway: INFO

  }'```

```

### Format des logs

**Réponse attendue** : JWT tokens```

2025-09-29 11:10:30 [main] INFO  c.i.gateway.ApiGatewayApplication - Started ApiGatewayApplication in 10.386 seconds

### 4. Test user profile (route protégée)```

```bash

# Sans token (doit échouer)### Filtre de logging

curl http://localhost:8080/api/users/profileChaque requête est tracée avec un ID unique :

```

# Avec token (doit réussir)=== REQUÊTE ENTRANTE [REQ-1696012345678-123] ===

TOKEN="eyJhbGc..."Timestamp: 2025-09-29 11:10:30

curl http://localhost:8080/api/users/profile \Méthode: GET

  -H "Authorization: Bearer $TOKEN"URI: http://localhost:8080/api/users/profile

``````



### 5. Test accès direct aux microservices (doit échouer)## Utilisation avec React Native

```bash

# Essayer d'accéder directement au auth-service### Configuration Expo

curl http://localhost:8082/api/auth/health```javascript

# ❌ Connection refused (si server.address=127.0.0.1)// Dans votre fichier de configuration Expo

const config = {

# Essayer d'accéder directement au user-service  extra: {

curl http://localhost:8081/api/users/profile    apiUrl: __DEV__ 

# ❌ Connection refused (si server.address=127.0.0.1)      ? 'http://localhost:8080' 

```      : 'https://votre-api-gateway.com'

  }

## 📊 Monitoring};

```

### Endpoints actuator disponibles

### Appels API

- `/actuator/health` - État de santé```javascript

- `/actuator/info` - Informations sur l'application// Exemple d'appel API depuis React Native

- `/actuator/gateway/routes` - Liste des routes configuréesconst apiCall = async () => {

- `/actuator/metrics` - Métriques  try {

- `/actuator/prometheus` - Métriques format Prometheus    const response = await fetch('http://localhost:8080/api/users/profile', {

      method: 'GET',

### Logs      headers: {

        'Content-Type': 'application/json',

Les logs incluent :        // Ajoutez vos headers d'authentification ici

- Requêtes entrantes avec méthode et path      },

- Validation JWT (succès/échec)    });

- Routing vers les microservices    const data = await response.json();

- Erreurs et timeouts    return data;

  } catch (error) {

Exemple :    console.error('Erreur API:', error);

```  }

2025-11-03 10:30:00 [reactor-http-nio-2] DEBUG c.i.g.f.JwtAuthenticationGatewayFilterFactory - Valid JWT token for path: /api/users/profile};

2025-11-03 10:30:01 [reactor-http-nio-3] WARN  c.i.g.f.JwtAuthenticationGatewayFilterFactory - Missing or invalid Authorization header for path: /api/users/profile```

```

## Exemples de configuration

## 🔧 Fonctionnalités avancées

### Proxy vers un service local

### Retry Logic```yaml

- id: backend-service

Configuré pour réessayer automatiquement :  uri: http://localhost:3000

- **Retries** : 3 tentatives  predicates:

- **Statuses** : BAD_GATEWAY, GATEWAY_TIMEOUT    - Path=/api/backend/**

- **Methods** : GET, POST  filters:

- **Backoff** : Exponentiel (10ms → 20ms → 40ms)    - RewritePath=/api/backend/(?<segment>.*), /${segment}

```

### CORS

### Load balancing

Configuration globale pour toutes les routes :```yaml

- **Origins** : localhost:*, 127.0.0.1:*, exp://*, *.expo.dev- id: load-balanced-service

- **Methods** : GET, POST, PUT, DELETE, PATCH, OPTIONS  uri: lb://mon-service

- **Headers** : Tous  predicates:

- **Credentials** : Autorisés    - Path=/api/service/**

- **Max Age** : 3600 secondes```



## 🐛 Troubleshooting### Ajout d'authentification

```yaml

### Gateway ne démarre pas- id: protected-service

  uri: http://localhost:8081

```bash  predicates:

# Vérifier si le port 8080 est libre    - Path=/api/protected/**

lsof -i :8080  filters:

    - AddRequestHeader=X-Gateway-Auth, true

# Changer le port si nécessaire```

SERVER_PORT=8090 mvn spring-boot:run

```## Monitoring et métriques



### Routes ne fonctionnent pas### Métriques disponibles

- Nombre de requêtes par route

```bash- Temps de réponse moyen

# Lister les routes actives- Codes de statut HTTP

curl http://localhost:8080/actuator/gateway/routes | jq- Utilisation mémoire et CPU



# Vérifier que les microservices sont accessibles### Intégration avec des outils de monitoring

curl http://localhost:8082/api/auth/healthL'API Gateway expose des métriques compatibles avec :

curl http://localhost:8081/actuator/health- **Prometheus** : `/actuator/prometheus`

```- **Micrometer** : `/actuator/metrics`



### JWT validation échoue## Sécurité



```bash### CORS

# Vérifier le format du tokenLa configuration CORS est adaptée pour le développement. En production, restreignez les origines :

echo "eyJhbGc..." | cut -d '.' -f 2 | base64 -d

```yaml

# Vérifier l'expirationglobalcors:

curl http://localhost:8080/api/auth/refresh \  cors-configurations:

  -H "Content-Type: application/json" \    '[/**]':

  -d '{"refreshToken":"..."}'      allowedOriginPatterns:

```        - "https://votre-domaine.com"

      allowedMethods: [GET, POST]

### Erreur CORS```



```bash### Headers de sécurité

# Vérifier la configuration CORSAjoutez des headers de sécurité :

curl -v -X OPTIONS http://localhost:8080/api/auth/login \

  -H "Origin: http://localhost:8081" \```yaml

  -H "Access-Control-Request-Method: POST"filters:

```  - AddResponseHeader=X-Content-Type-Options, nosniff

  - AddResponseHeader=X-Frame-Options, DENY

## 📝 TODO```



- [ ] Implémenter rate limiting avec Redis## Déploiement

- [ ] Ajouter circuit breaker avec Resilience4j

- [ ] Implémenter cache pour réduire les appels aux microservices### Variables d'environnement

- [ ] Ajouter authentification par API Key pour les services internes```bash

- [ ] Implémenter request/response logging complet# Port du serveur

- [ ] Ajouter métriques custom (temps de réponse par route, etc.)SERVER_PORT=8080

- [ ] Implémenter health checks pour les microservices backend

- [ ] Ajouter support pour WebSockets# Niveau de logging

- [ ] Créer dashboard de monitoringLOGGING_LEVEL_COM_IWAPROJECT_GATEWAY=INFO



## 📚 Documentation# URL des services backend

BACKEND_SERVICE_URL=https://api.backend.com

- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)```

- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)

- [Architecture complète](../API_GATEWAY_REFACTORING_PLAN.md)### Docker

```dockerfile

## ✅ Checklist de déploiementFROM openjdk:11-jre-slim

COPY target/api-gateway-1.0-SNAPSHOT.jar app.jar

- [x] Dépendances Maven configuréesEXPOSE 8080

- [x] Routes configuréesENTRYPOINT ["java", "-jar", "/app.jar"]

- [x] JWT validation implémentée```

- [x] CORS configuré

- [x] Logging configuré## Dépannage

- [x] Retry logic configuré

- [x] Health checks activés### Problèmes courants

- [ ] Rate limiting configuré

- [ ] Circuit breaker configuré#### Port déjà utilisé

- [ ] Tests end-to-end```bash

- [ ] Documentation API (Swagger)# Vérifier quel processus utilise le port 8080

- [ ] Dockerfile créénetstat -ano | findstr :8080

- [ ] CI/CD pipeline configuré```



---#### Service ne démarre pas

1. Vérifiez la version de Java : `java -version`

**Version**: 1.0.0  2. Nettoyez et recompilez : `mvn clean compile`

**Port**: 8080  3. Vérifiez les logs de démarrage

**Status**: ✅ Prêt pour production locale

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
