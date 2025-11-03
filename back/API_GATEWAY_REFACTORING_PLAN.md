# 🏗️ ARCHITECTURE REFACTORING - API Gateway Pattern

## 🎯 Problème actuel

L'architecture actuelle permet au frontend de communiquer directement avec les microservices, ce qui n'est pas conforme au pattern API Gateway.

### ❌ Architecture incorrecte (actuelle)
```
┌─────────────┐
│  Frontend   │
│  (Expo)     │
└──────┬──────┘
       │
       ├──────────────┐──────────────┐──────────────┐
       │              │              │              │
       ▼              ▼              ▼              ▼
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│Keycloak  │   │Auth Svc  │   │User Svc  │   │Other Svc │
│  :8080   │   │  :8082   │   │  :8081   │   │  :808X   │
└──────────┘   └──────────┘   └──────────┘   └──────────┘
```

**Problèmes** :
- ❌ Frontend expose tous les ports des microservices
- ❌ Pas de point d'entrée unique
- ❌ Difficile de gérer l'authentification
- ❌ Pas de rate limiting centralisé
- ❌ CORS à configurer sur chaque service

## ✅ Architecture correcte (cible)

```
┌─────────────┐
│  Frontend   │
│  (Expo)     │
└──────┬──────┘
       │
       │ (Port 8080 uniquement)
       │
       ▼
┌──────────────────────────────────┐
│      API Gateway :8080           │
│  - Routing                       │
│  - Authentication                │
│  - Rate Limiting                 │
│  - CORS                          │
│  - Load Balancing                │
└──────┬───────────────────────────┘
       │
       ├──────────────┬──────────────┬──────────────┐
       │              │              │              │
       ▼              ▼              ▼              ▼
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│Keycloak  │   │Auth Svc  │   │User Svc  │   │Other Svc │
│ (intern) │   │(internal)│   │(internal)│   │(internal)│
└──────────┘   └──────────┘   └──────────┘   └──────────┘
    ▲
    │ (Admin API only)
    └── Auth Service
```

**Avantages** :
- ✅ Point d'entrée unique sur le port 8080
- ✅ Microservices non exposés publiquement
- ✅ Authentification centralisée
- ✅ Configuration CORS unique
- ✅ Logs et monitoring centralisés
- ✅ Rate limiting par endpoint

## 📋 Plan de refactoring

### Phase 1 : ✅ Mise à jour API Gateway

#### 1.1 Configuration des routes
Mettre à jour `GatewayConfig.java` pour router vers les microservices :

```java
@Bean
public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        // ==================== AUTH SERVICE ====================
        .route("auth-login", r -> r
            .path("/api/auth/login")
            .uri("http://localhost:8082"))
        
        .route("auth-register", r -> r
            .path("/api/auth/register")
            .uri("http://localhost:8082"))
        
        .route("auth-refresh", r -> r
            .path("/api/auth/refresh")
            .uri("http://localhost:8082"))
        
        .route("auth-logout", r -> r
            .path("/api/auth/logout")
            .uri("http://localhost:8082"))
        
        .route("auth-health", r -> r
            .path("/api/auth/health")
            .uri("http://localhost:8082"))
        
        // ==================== USER SERVICE ====================
        .route("user-profile", r -> r
            .path("/api/users/profile")
            .filters(f -> f.filter(jwtAuthenticationFilter))
            .uri("http://localhost:8081"))
        
        .route("user-all", r -> r
            .path("/api/users/**")
            .filters(f -> f.filter(jwtAuthenticationFilter))
            .uri("http://localhost:8081"))
        
        .route("user-webhook", r -> r
            .path("/api/webhooks/**")
            .uri("http://localhost:8081"))
        
        .build();
}
```

#### 1.2 Ajouter JWT Authentication Filter
Créer un filtre pour valider les tokens JWT :

```java
@Component
public class JwtAuthenticationGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = extractToken(exchange.getRequest());
            
            if (token == null || !validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            return chain.filter(exchange);
        };
    }
}
```

#### 1.3 Mettre à jour `application.yml`

```yaml
server:
  port: 8080  # Point d'entrée unique

spring:
  application:
    name: api-gateway
  
  # Configuration OAuth2 pour validation JWT
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/IWA_NextLevel

# Services backend (internes)
services:
  auth:
    url: http://localhost:8082
  user:
    url: http://localhost:8081
  keycloak:
    url: http://localhost:8080

# Rate Limiting
spring:
  cloud:
    gateway:
      redis-rate-limiter:
        replenish-rate: 10
        burst-capacity: 20
```

### Phase 2 : 🔧 Mise à jour des microservices

#### 2.1 Auth Service (port 8082)
**Changements** :
- ✅ Garder sur port 8082 (interne)
- ✅ Ajouter configuration pour communiquer avec Keycloak
- ✅ Retirer CORS (géré par Gateway)

```yaml
# application.yml
server:
  port: 8082
  # Écouter uniquement sur localhost (non public)
  address: 127.0.0.1

keycloak:
  server-url: http://localhost:8080
  realm: IWA_NextLevel
  client-id: iwa-client
```

#### 2.2 User Service (port 8081)
**Changements** :
- ✅ Garder sur port 8081 (interne)
- ✅ Retirer CORS (géré par Gateway)
- ✅ Validation JWT uniquement (pas d'auth logic)

```yaml
# application.yml
server:
  port: 8081
  # Écouter uniquement sur localhost (non public)
  address: 127.0.0.1

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/IWA_NextLevel
```

### Phase 3 : 🌐 Keycloak Routing

#### 3.1 Ajouter routes Keycloak dans Gateway
Le frontend doit pouvoir accéder à Keycloak pour certaines opérations :

```java
// Routes Keycloak (nécessaires pour le frontend)
.route("keycloak-realms", r -> r
    .path("/realms/**")
    .uri("http://localhost:8080"))  // Même port mais routé

.route("keycloak-resources", r -> r
    .path("/resources/**")
    .uri("http://localhost:8080"))
```

#### 3.2 Configuration Keycloak
**URL publique** : `http://localhost:8080` (via Gateway)
**URL admin** : Accessible uniquement par Auth Service

### Phase 4 : 📱 Mise à jour Frontend

#### 4.1 Configuration API Base URL
Mettre à jour `front/services/api.ts` :

```typescript
// AVANT (plusieurs URLs)
const AUTH_SERVICE_URL = 'http://localhost:8082';
const USER_SERVICE_URL = 'http://localhost:8081';
const KEYCLOAK_URL = 'http://localhost:8080';

// APRÈS (une seule URL)
const API_BASE_URL = 'http://localhost:8080';  // API Gateway uniquement

// Tous les endpoints passent par la Gateway
export const authApi = {
  login: () => `${API_BASE_URL}/api/auth/login`,
  register: () => `${API_BASE_URL}/api/auth/register`,
  refresh: () => `${API_BASE_URL}/api/auth/refresh`,
};

export const userApi = {
  profile: () => `${API_BASE_URL}/api/users/profile`,
  updateProfile: () => `${API_BASE_URL}/api/users/profile`,
};
```

#### 4.2 Keycloak Config
Mettre à jour `front/config/keycloak.ts` :

```typescript
export const keycloakConfig = {
  url: 'http://localhost:8080',  // Via Gateway
  realm: 'IWA_NextLevel',
  clientId: 'iwa-client',
};
```

### Phase 5 : 🐳 Docker Configuration

#### 5.1 Network isolation
Créer des networks Docker séparés :

```yaml
# docker-compose.yml
networks:
  # Network public (Gateway seulement)
  public:
    driver: bridge
  
  # Network interne (microservices)
  internal:
    driver: bridge
    internal: true  # Pas d'accès externe

services:
  api-gateway:
    ports:
      - "8080:8080"  # Seul port exposé
    networks:
      - public
      - internal
  
  auth-service:
    # Pas de ports exposés
    networks:
      - internal
  
  user-service:
    # Pas de ports exposés
    networks:
      - internal
  
  keycloak:
    # Accessible via Gateway uniquement
    networks:
      - internal
  
  postgres:
    # Base de données interne
    networks:
      - internal
```

#### 5.2 Variables d'environnement
Mettre à jour les URLs pour Docker :

```yaml
# docker-compose.yml
services:
  api-gateway:
    environment:
      - SERVICES_AUTH_URL=http://auth-service:8082
      - SERVICES_USER_URL=http://user-service:8081
      - KEYCLOAK_URL=http://keycloak:8080
  
  auth-service:
    environment:
      - KEYCLOAK_SERVER_URL=http://keycloak:8080
      - SERVER_ADDRESS=0.0.0.0  # Écouter sur toutes interfaces dans Docker
  
  user-service:
    environment:
      - SERVER_ADDRESS=0.0.0.0
```

## 🔐 Sécurité

### Matrice d'accès

| Service | Port | Accessible depuis |
|---------|------|-------------------|
| **API Gateway** | 8080 | Internet/Frontend (public) |
| **Auth Service** | 8082 | API Gateway uniquement |
| **User Service** | 8081 | API Gateway uniquement |
| **Keycloak** | 8080 | Auth Service + Gateway (routes proxy) |
| **PostgreSQL** | 5432 | Services backend uniquement |

### Flow d'authentification sécurisé

```
1. Frontend → Gateway:8080/api/auth/login
           ↓
2. Gateway → Auth Service:8082/api/auth/login
           ↓
3. Auth Service → Keycloak (direct)
           ↓
4. Keycloak → JWT token
           ↓
5. Auth Service → Gateway → Frontend (JWT)
           ↓
6. Frontend → Gateway:8080/api/users/profile (+ JWT Header)
           ↓
7. Gateway → Valide JWT
           ↓
8. Gateway → User Service:8081/api/users/profile (+ JWT)
           ↓
9. User Service → Valide JWT → Response
           ↓
10. Gateway → Frontend (Response)
```

## 📊 Routes complètes

### Routes publiques (sans JWT)
```
POST   /api/auth/login
POST   /api/auth/register
POST   /api/auth/refresh
GET    /api/auth/health
GET    /realms/IWA_NextLevel/**  (Keycloak resources)
```

### Routes protégées (avec JWT)
```
GET    /api/users/profile
PUT    /api/users/profile
GET    /api/users/{id}
POST   /api/orders
GET    /api/orders
... (tous les autres endpoints métier)
```

### Routes internes (webhook, non exposées au frontend)
```
POST   /api/webhooks/keycloak/user-registered  (appelé par Keycloak extension)
```

## 🧪 Tests

### Test 1: Gateway accessible
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Test 2: Microservices non accessibles directement
```bash
curl http://localhost:8081/api/users/profile
# Expected: Connection refused ou timeout
```

### Test 3: Login via Gateway
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"Test123!"}'
# Expected: JWT tokens
```

### Test 4: User profile via Gateway avec JWT
```bash
TOKEN="eyJhbG..."  # Token from login
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN"
# Expected: User profile data
```

## 📝 Checklist de migration

### API Gateway
- [ ] Ajouter dépendance Spring Security OAuth2 Resource Server
- [ ] Créer JwtAuthenticationFilter
- [ ] Mettre à jour GatewayConfig avec toutes les routes
- [ ] Configurer application.yml avec URLs des services
- [ ] Ajouter rate limiting
- [ ] Configurer CORS global
- [ ] Ajouter logging des requêtes

### Auth Service
- [ ] Changer `server.address: 127.0.0.1` (ou 0.0.0.0 dans Docker)
- [ ] Retirer configuration CORS
- [ ] Tester communication avec Keycloak
- [ ] Vérifier que le service n'est pas accessible directement

### User Service
- [ ] Changer `server.address: 127.0.0.1` (ou 0.0.0.0 dans Docker)
- [ ] Retirer configuration CORS
- [ ] Garder uniquement validation JWT
- [ ] Webhook endpoint reste accessible (pour Keycloak)
- [ ] Vérifier que le service n'est pas accessible directement

### Frontend
- [ ] Mettre à jour toutes les URLs vers `http://localhost:8080`
- [ ] Retirer références directes aux microservices
- [ ] Tester login flow
- [ ] Tester protected endpoints
- [ ] Tester refresh token

### Docker
- [ ] Créer network public/internal
- [ ] Exposer uniquement port 8080 (Gateway)
- [ ] Configurer variables d'environnement
- [ ] Tester communication inter-services
- [ ] Vérifier isolation réseau

## 🎯 Ordre d'exécution

1. **Jour 1** : API Gateway
   - Ajouter JWT filter
   - Configurer routes
   - Tester en local

2. **Jour 2** : Microservices
   - Mettre à jour Auth Service
   - Mettre à jour User Service
   - Tester via Gateway

3. **Jour 3** : Frontend
   - Mettre à jour toutes les URLs
   - Tester tous les flows
   - Fix bugs

4. **Jour 4** : Docker
   - Dockeriser API Gateway
   - Configurer networks
   - Tests end-to-end

5. **Jour 5** : Documentation & Polish
   - Mettre à jour READMEs
   - Diagrammes d'architecture
   - Guide de déploiement

## 📚 Ressources

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)

## ✅ Résultat attendu

**Frontend** :
- ✅ Communique uniquement avec `localhost:8080`
- ✅ Ne connaît pas l'existence des microservices
- ✅ Configuration simple et claire

**Backend** :
- ✅ Point d'entrée unique (Gateway)
- ✅ Microservices isolés et non exposés
- ✅ Authentification centralisée
- ✅ Logs et monitoring centralisés
- ✅ Architecture scalable et maintenable

---

**Status**: 📋 Plan prêt - Prêt pour implémentation
**Prochaine étape**: Commencer par Phase 1 - API Gateway
