# 🚀 Guide de démarrage - Architecture Microservices IWA

Ce guide vous explique comment démarrer tous les services et tester l'architecture complète.

## 📋 Prérequis

- Java 21
- Maven 3.x
- Docker & Docker Compose
- PostgreSQL 16
- Keycloak 26.0.7
- Un terminal bash

## 🏗️ Architecture

```
Frontend (Expo)
       │
       │ Port 8080 UNIQUEMENT
       │
       ▼
┌──────────────────┐
│  API Gateway     │ :8080 (PUBLIC)
│  - JWT Filter    │
│  - CORS          │
│  - Routing       │
└────┬─────────────┘
     │
     ├─────────────┬─────────────┐
     │             │             │
     ▼             ▼             ▼
┌─────────┐  ┌──────────┐  ┌──────────┐
│Keycloak │  │Auth Svc  │  │User Svc  │
│  :8080  │  │  :8082   │  │  :8081   │
│(intern) │  │(internal)│  │(internal)│
└─────────┘  └──────────┘  └──────────┘
     │                          │
     └──────────────────────────┘
              PostgreSQL :5432
```

## 🔧 Étape 1 : Démarrer PostgreSQL et Keycloak

### Option A : Avec Docker Compose (Recommandé)

```bash
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
./start-services.sh
```

Ou manuellement :
```bash
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
docker-compose up -d postgres keycloak
```

### Option B : Sans Docker

Si vous préférez installer PostgreSQL et Keycloak localement, suivez les instructions officielles.

### Vérification

```bash
# PostgreSQL (via Docker)
docker exec -it iwa-user-postgres psql -U postgres -d iwa_users -c "SELECT 1"

# Ou si psql est installé localement
# psql -h localhost -U postgres -d iwa_users -c "SELECT 1"

# Keycloak
curl http://localhost:8080/realms/IWA_NextLevel
```

**Keycloak Admin Console** : http://localhost:8080/admin
- Username: `admin`
- Password: `admin`

## 🚀 Étape 2 : Démarrer les microservices

Ouvrez **3 terminaux** (ou utilisez tmux/screen) :

### Terminal 1 : Auth Service (Port 8082)

```bash
cd ./back/auth-service
mvn clean install
mvn spring-boot:run
```

**Vérifications** :
```bash
# Via Gateway (devrait fonctionner)
curl http://localhost:8080/api/auth/health

# Direct (devrait échouer - Connection refused)
curl http://localhost:8082/api/auth/health
```

### Terminal 2 : User Service (Port 8081)

```bash
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
mvn clean install
mvn spring-boot:run
```

**Vérifications** :
```bash
# Direct (devrait échouer - Connection refused)
curl http://localhost:8081/api/users/profile
```

### Terminal 3 : API Gateway (Port 8080)

```bash
cd /home/etienne/Documents/IWAPROJECT/back/api-gateway
mvn clean install
mvn spring-boot:run
```

**Vérifications** :
```bash
# Health check Gateway
curl http://localhost:8080/actuator/health

# Liste des routes configurées
curl http://localhost:8080/actuator/gateway/routes | jq
```

## 📊 Étape 3 : Accéder aux interfaces Swagger

### 🔹 API Gateway
- **URL** : http://localhost:8080/swagger-ui.html
- **Status** : À configurer (pas encore implémenté)

### 🔹 Auth Service
**⚠️ Important** : Auth Service est en mode interne (`server.address=127.0.0.1`), donc Swagger n'est pas accessible directement.

Pour le développement, vous pouvez temporairement changer :

```yaml
# auth-service/src/main/resources/application.yml
server:
  port: 8082
  address: 0.0.0.0  # ⚠️ Uniquement pour développement
```

Puis redémarrer et accéder à :
- **URL** : http://localhost:8082/swagger-ui.html

**⚠️ N'oubliez pas de remettre `127.0.0.1` après les tests !**

### 🔹 User Service
**URL** : http://localhost:8081/swagger-ui.html

Temporairement changer aussi :
```properties
# user-microservice/src/main/resources/application.properties
server.address=0.0.0.0  # ⚠️ Uniquement pour développement
```

## 🧪 Étape 4 : Tests fonctionnels

### Test 1 : Vérifier l'isolation des services

```bash
# ✅ Gateway accessible
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# ⚠️ Auth Service accessible en LOCAL (server.address=127.0.0.1)
curl http://localhost:8082/api/auth/health
# Expected: {"status":"UP","service":"auth-service"}
# Note: C'est NORMAL! 127.0.0.1 signifie "localhost uniquement", pas "réseau externe"

# ⚠️ User Service accessible en LOCAL (server.address=127.0.0.1)
curl http://localhost:8081/api/users/profile
# Expected: Erreur 401 ou 500 (pas de token)
# Note: Le service répond, mais c'est uniquement depuis localhost
```

**💡 Important**: `server.address=127.0.0.1` signifie:
- ✅ **Accessible depuis la même machine** (localhost)
- ❌ **NON accessible depuis le réseau externe** (autres machines, internet)

Pour une **vraie isolation réseau**, utilisez Docker avec des réseaux internes séparés (voir section Production).

### Test 2 : Inscription d'un nouvel utilisateur

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Réponse attendue** :
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "message": "User registered successfully"
}
```

**Vérifier dans la base de données** :
```bash
# Via Docker
docker exec -it iwa-user-postgres psql -U postgres -d iwa_users -c "SELECT * FROM users WHERE username='john_doe';"

# Ou si psql est installé localement
# psql -h localhost -U postgres -d iwa_users -c "SELECT * FROM users WHERE username='john_doe';"
```

**Vérifier dans Keycloak** :
1. Ouvrir http://localhost:8080/admin
2. Login : admin / admin
3. Realm : IWA_NextLevel
4. Users → Chercher "john_doe"

### Test 3 : Connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'
```

**Réponse attendue** :
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkI...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkI...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshExpiresIn": 1800,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com"
}
```

**💾 Sauvegarder le token pour les tests suivants** :
```bash
export TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkI..."
```

### Test 4 : Accès au profil utilisateur (Route protégée)

```bash
# Sans token (devrait échouer - 401)
curl -v http://localhost:8080/api/users/profile

# Avec token (devrait réussir - 200)
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Réponse attendue** :
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "keycloakId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2025-11-03T10:00:00Z",
  "updatedAt": "2025-11-03T10:00:00Z"
}
```

### Test 5 : Rafraîchir le token

```bash
export REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkI..."

curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

**Réponse attendue** : Nouveaux tokens (même format que login)

### Test 6 : Déconnexion

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

**Réponse attendue** :
```json
{
  "message": "Logout successful"
}
```

### Test 7 : Webhook (Keycloak → User Service)

Ce test simule l'appel que fait l'extension Keycloak après une inscription :

```bash
curl -X POST http://localhost:8080/api/webhooks/keycloak/user-registered \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "username": "webhook_test",
    "email": "webhook@example.com",
    "firstName": "Webhook",
    "lastName": "Test"
  }'
```

**Vérifier dans la DB** :
```bash
# Via Docker
docker exec -it iwa-user-postgres psql -U postgres -d iwa_users -c "SELECT * FROM users WHERE username='webhook_test';"

# Ou si psql est installé localement
# psql -h localhost -U postgres -d iwa_users -c "SELECT * FROM users WHERE username='webhook_test';"
```

## 📊 Étape 5 : Monitoring et Logs

### API Gateway Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health | jq

# Liste des routes
curl http://localhost:8080/actuator/gateway/routes | jq

# Métriques
curl http://localhost:8080/actuator/metrics | jq

# Métriques spécifiques
curl http://localhost:8080/actuator/metrics/http.server.requests | jq
```

### Logs en temps réel

```bash
# Auth Service logs
tail -f /home/etienne/Documents/IWAPROJECT/back/auth-service/logs/*.log

# User Service logs
tail -f /home/etienne/Documents/IWAPROJECT/back/user-microservice/logs/*.log

# API Gateway logs
tail -f /home/etienne/Documents/IWAPROJECT/back/api-gateway/logs/*.log

# Keycloak logs
docker logs -f keycloak
```

## 🔍 Étape 6 : Tests avec Postman/Insomnia

### Collection Postman

Créez une collection avec ces endpoints :

#### 1. Variables d'environnement
```
BASE_URL = http://localhost:8080
ACCESS_TOKEN = {{accessToken}}
REFRESH_TOKEN = {{refreshToken}}
```

#### 2. Auth - Register
```
POST {{BASE_URL}}/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123!",
  "firstName": "Test",
  "lastName": "User"
}
```

#### 3. Auth - Login
```
POST {{BASE_URL}}/api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "Test123!"
}

# Script Post-Request (pour sauvegarder les tokens) :
pm.environment.set("accessToken", pm.response.json().accessToken);
pm.environment.set("refreshToken", pm.response.json().refreshToken);
```

#### 4. User - Get Profile
```
GET {{BASE_URL}}/api/users/profile
Authorization: Bearer {{ACCESS_TOKEN}}
```

#### 5. Auth - Refresh Token
```
POST {{BASE_URL}}/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{REFRESH_TOKEN}}"
}
```

#### 6. Auth - Logout
```
POST {{BASE_URL}}/api/auth/logout
Content-Type: application/json

{
  "refreshToken": "{{REFRESH_TOKEN}}"
}
```

## 🐛 Dépannage

### Problème : Gateway ne démarre pas

**Erreur** : `Port 8080 already in use`

**Solution** :
```bash
# Trouver le processus
lsof -i :8080

# Arrêter le processus
kill -9 <PID>

# Ou arrêter Keycloak si c'est lui
docker-compose down keycloak
```

### Problème : Auth/User Service non accessibles via Gateway

**Vérifier** :
```bash
# Services démarrés ?
ps aux | grep java

# Logs Gateway
cat /home/etienne/Documents/IWAPROJECT/back/api-gateway/logs/*.log
```

**Solution** : Vérifier que les URLs dans `api-gateway/src/main/resources/application.yml` sont correctes :
```yaml
services:
  auth:
    url: http://localhost:8082
  user:
    url: http://localhost:8081
```

### Problème : JWT validation échoue

**Erreur** : `401 Unauthorized` pour route protégée

**Vérifier** :
```bash
# Token expiré ?
echo $TOKEN | cut -d '.' -f 2 | base64 -d | jq .exp

# Comparer avec timestamp actuel
date +%s
```

**Solution** : Refaire un login pour obtenir un nouveau token

### Problème : Base de données inaccessible

**Erreur** : `Connection refused` ou `FATAL: database "iwa_users" does not exist`

**Solution** :
```bash
# Vérifier PostgreSQL
docker ps | grep postgres

# Créer la base si nécessaire (via Docker)
docker exec -it iwa-user-postgres psql -U postgres -c "CREATE DATABASE iwa_users;"

# Ou si psql est installé localement
# psql -h localhost -U postgres -c "CREATE DATABASE iwa_users;"

# Vérifier les credentials dans application.properties
cat /home/etienne/Documents/IWAPROJECT/back/user-microservice/src/main/resources/application.properties
```

### Problème : Keycloak non accessible

**Solution** :
```bash
# Vérifier Keycloak
docker ps | grep keycloak

# Redémarrer Keycloak
docker-compose restart keycloak

# Vérifier les logs
docker logs keycloak
```

## 📝 Commandes utiles

### Arrêter tous les services

```bash
# Arrêter les services Java (Ctrl+C dans chaque terminal)

# Arrêter Docker
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
./stop-services.sh

# Ou manuellement
docker-compose down
```

### Nettoyer et reconstruire

```bash
# API Gateway
cd /home/etienne/Documents/IWAPROJECT/back/api-gateway
mvn clean install

# Auth Service
cd /home/etienne/Documents/IWAPROJECT/back/auth-service
mvn clean install

# User Service
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
mvn clean install
```

### Vérifier les bases de données

```bash
# Se connecter à PostgreSQL (via Docker)
docker exec -it iwa-user-postgres psql -U postgres -d iwa_users

# Ou si psql est installé localement
# psql -h localhost -U postgres -d iwa_users

# Lister les tables
\dt

# Voir les utilisateurs
SELECT * FROM users;

# Compter les utilisateurs
SELECT COUNT(*) FROM users;

# Quitter
\q
```

## 🎯 Checklist de démarrage rapide

- [ ] PostgreSQL et Keycloak démarrés
- [ ] Auth Service démarré (8082)
- [ ] User Service démarré (8081)
- [ ] API Gateway démarré (8080)
- [ ] Vérification isolation (curl vers 8081/8082 = Connection refused)
- [ ] Test inscription via Gateway
- [ ] Test login via Gateway
- [ ] Test profil utilisateur avec JWT
- [ ] Vérification logs
- [ ] Tests Postman/Insomnia

## 📚 Documentation complémentaire

- [Auth Service README](./README.md) - Documentation Auth Service
- [API Gateway README](../api-gateway/README.md) - Documentation Gateway
- [User Service README](../user-microservice/README.md) - Documentation User Service
- [Architecture Refactoring Plan](../API_GATEWAY_REFACTORING_PLAN.md) - Plan d'architecture
- [Keycloak Setup](../../keycloak-config/README.md) - Configuration Keycloak

## 🎉 Félicitations !

Si tous les tests passent, votre architecture microservices est complètement fonctionnelle ! 🚀

Vous avez maintenant :
- ✅ Une API Gateway comme point d'entrée unique
- ✅ Des microservices isolés (localhost uniquement)
- ✅ Une authentification JWT centralisée
- ✅ Une séparation claire des responsabilités
- ✅ Un système prêt pour la production

## 🐳 Pour la production: Isolation réseau complète avec Docker

Pour une **vraie isolation réseau** (services inaccessibles même en localhost), utilisez Docker Compose:

### Architecture Docker recommandée

```yaml
# docker-compose.production.yml
networks:
  public:
    driver: bridge
  internal:
    driver: bridge
    internal: true  # ⚠️ Pas d'accès externe!

services:
  api-gateway:
    networks:
      - public   # Accessible de l'extérieur
      - internal # Communique avec les microservices
    ports:
      - "8080:8080"
  
  auth-service:
    networks:
      - internal # ⚠️ Uniquement réseau interne!
    environment:
      - SERVER_ADDRESS=0.0.0.0  # Écoute sur toutes les interfaces du réseau interne
  
  user-service:
    networks:
      - internal # ⚠️ Uniquement réseau interne!
    environment:
      - SERVER_ADDRESS=0.0.0.0
  
  keycloak:
    networks:
      - internal # ⚠️ Accessible uniquement via Gateway!
```

Avec cette configuration:
- ✅ Gateway accessible sur `http://localhost:8080` (réseau public)
- ❌ Auth/User services **vraiment inaccessibles** directement (réseau internal)
- ❌ Keycloak **vraiment inaccessible** directement (réseau internal)
- ✅ Gateway peut router vers tous les services (membre des 2 réseaux)

---

**Besoin d'aide ?** Consultez les logs ou la documentation dans chaque service.
