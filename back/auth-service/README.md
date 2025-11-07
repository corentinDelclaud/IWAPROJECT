# Auth Service

Service d'authentification séparé pour gérer toutes les opérations liées à Keycloak.

## 🎯 Responsabilités

Ce service gère :
- ✅ **Login** - Authentification des utilisateurs
- ✅ **Register** - Inscription de nouveaux utilisateurs
- ✅ **Token Refresh** - Rafraîchissement des tokens JWT
- ✅ **Logout** - Déconnexion des utilisateurs

## 🚀 Architecture

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│                 │      │                  │      │                 │
│   Front-End     │─────▶│   Auth Service   │─────▶│    Keycloak     │
│  (Expo/React)   │      │   (Port 8082)    │      │   (Port 8080)   │
│                 │      │                  │      │                 │
└─────────────────┘      └──────────────────┘      └─────────────────┘
                                  │
                                  │ (Webhook notification)
                                  │
                         ┌────────▼──────────┐
                         │                   │
                         │   User Service    │
                         │   (Port 8081)     │
                         │                   │
                         └───────────────────┘
```

## 📡 API Endpoints

### 1. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "Password123!"
}
```

**Réponse:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshExpiresIn": 1800,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com"
}
```

### 2. Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Réponse:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com",
  "message": "User registered successfully"
}
```

### 3. Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI..."
}
```

**Réponse:** (même format que login)

### 4. Logout
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI..."
}
```

**Réponse:**
```json
{
  "message": "Logout successful"
}
```

### 5. Health Check
```http
GET /api/auth/health
```

**Réponse:**
```json
{
  "status": "UP",
  "service": "auth-service",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## ⚙️ Configuration

Le service se configure via `application.yml`:

```yaml
server:
  port: 8082

keycloak:
  server-url: http://localhost:8080
  realm: IWA_NextLevel
  client-id: iwa-client
  admin-username: admin
  admin-password: admin

user-service:
  url: http://localhost:8081
```

## 🔧 Build et Run

### Avec Maven
```bash
cd back/auth-service
mvn clean install
mvn spring-boot:run
```

### Avec Docker (à venir)
```bash
docker-compose up auth-service
```

## 🔐 Sécurité

- ✅ Tous les endpoints `/api/auth/**` sont publics (pas de JWT requis)
- ✅ Les mots de passe sont validés (min 8 caractères)
- ✅ Les emails sont validés
- ✅ Les tokens sont gérés par Keycloak
- ✅ Les refresh tokens ont une durée de vie limitée

## 🔄 Workflow d'inscription

1. **Client** → POST `/api/auth/register` → **Auth Service**
2. **Auth Service** → Crée l'utilisateur dans **Keycloak**
3. **Keycloak** → Déclenche l'événement REGISTER
4. **Webhook Extension** → POST `/api/webhooks/keycloak/user-registered` → **User Service**
5. **User Service** → Crée l'utilisateur dans **PostgreSQL**

## 📊 Dépendances

- Spring Boot 3.5.6
- Keycloak Admin Client
- Spring Security OAuth2 Resource Server
- Spring Validation
- Lombok
- SLF4J Logger

## 🐛 Troubleshooting

### Service ne démarre pas
```bash
# Vérifier que Keycloak est accessible
curl http://localhost:8080/health

# Vérifier les logs
mvn spring-boot:run
```

### Erreur "Invalid credentials"
- Vérifier que le realm est correct (`IWA_NextLevel`)
- Vérifier que le client-id est correct (`iwa-client`)
- Vérifier que l'utilisateur existe dans Keycloak

### Erreur "Username already exists"
- L'utilisateur existe déjà dans Keycloak
- Choisir un autre username ou supprimer l'ancien

## 📝 TODO

- [ ] Ajouter validation email par code
- [ ] Ajouter gestion des rôles
- [ ] Ajouter rate limiting
- [ ] Ajouter métriques Prometheus
- [ ] Créer Dockerfile
- [ ] Ajouter tests unitaires
- [ ] Ajouter tests d'intégration
