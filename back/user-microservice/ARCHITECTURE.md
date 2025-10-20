# Architecture du User Microservice

## 📐 Structure du Projet

```
user-microservice/
├── src/main/java/iwaproject/user_microservice/
│   ├── UserMicroserviceApplication.java    # Point d'entrée Spring Boot
│   ├── config/
│   │   ├── SecurityConfig.java             # Configuration OAuth2/Keycloak
│   │   └── OpenAPIConfig.java              # Configuration Swagger
│   ├── controller/
│   │   └── UserController.java             # REST endpoints
│   ├── dto/
│   │   ├── UserProfileDTO.java             # Profil complet (avec email)
│   │   ├── UserPublicDTO.java              # Profil public (sans email)
│   │   └── UpdateProfileDTO.java           # DTO pour mise à jour
│   ├── entity/
│   │   └── User.java                       # Entité JPA
│   ├── exception/
│   │   ├── ErrorResponse.java              # Structure de réponse d'erreur
│   │   ├── GlobalExceptionHandler.java     # Gestion centralisée des erreurs
│   │   ├── UserNotFoundException.java
│   │   ├── UserAlreadyExistsException.java
│   │   └── UserDeletedException.java
│   ├── kafka/
│   │   ├── consumer/
│   │   │   └── KeycloakEventConsumer.java  # Écoute événements Keycloak
│   │   ├── producer/
│   │   │   └── UserEventProducer.java      # Publie événements User
│   │   └── event/
│   │       ├── KeycloakUserEvent.java      # Event Keycloak
│   │       └── UserEvent.java              # Event User
│   ├── repository/
│   │   └── UserRepository.java             # Spring Data JPA
│   └── service/
│       └── UserService.java                # Logique métier
└── src/main/resources/
    ├── application.properties              # Configuration
    └── schema.sql                          # Script SQL (optionnel)
```

## 🔄 Flux de données

### 1. Création d'utilisateur (via Keycloak)
```
User → Keycloak Registration
     ↓
Keycloak Event Listener → Kafka Topic: keycloak-events
     ↓
KeycloakEventConsumer → UserService.createUser()
     ↓
User Entity saved to PostgreSQL
     ↓
UserEventProducer → Kafka Topic: user-events (USER_CREATED)
```

### 2. Consultation de profil
```
Client → GET /api/users/profile (avec JWT)
     ↓
SecurityConfig valide JWT Keycloak
     ↓
UserController extrait userId du JWT (claim 'sub')
     ↓
UserService.getUserProfile(userId)
     ↓
UserRepository.findById()
     ↓
PostgreSQL
     ↓
UserProfileDTO retourné au client
```

### 3. Mise à jour de profil
```
Client → PUT /api/users/profile (avec JWT + UpdateProfileDTO)
     ↓
SecurityConfig valide JWT
     ↓
@Valid validation
     ↓
UserService.updateProfile()
     ↓
PostgreSQL update
     ↓
UserEventProducer → Kafka Topic: user-events (USER_UPDATED)
     ↓
UserProfileDTO retourné
```

## 🗄️ Modèle de données

### Table: users

| Colonne     | Type          | Description                          |
|-------------|---------------|--------------------------------------|
| id          | VARCHAR(255)  | PK - UUID de Keycloak (sub claim)    |
| username    | VARCHAR(50)   | Unique - Pseudo de l'utilisateur     |
| email       | VARCHAR(255)  | Unique - Email (lecture seule)       |
| first_name  | VARCHAR(100)  | Prénom                               |
| last_name   | VARCHAR(100)  | Nom                                  |
| created_at  | TIMESTAMP     | Date de création                     |
| updated_at  | TIMESTAMP     | Date de dernière modification        |
| deleted_at  | TIMESTAMP     | Date de suppression (soft delete)    |

## 🔐 Sécurité

### Configuration OAuth2
- **Type**: Resource Server
- **Provider**: Keycloak
- **Token**: JWT
- **Validation**: Via JWK Set URI

### Extraction de l'utilisateur
```java
@AuthenticationPrincipal Jwt jwt
String userId = jwt.getSubject(); // Extrait le 'sub' claim
```

## 📨 Événements Kafka

### Topics

#### Consommé: `keycloak-events`
```json
{
  "eventType": "REGISTER",
  "userId": "uuid-from-keycloak",
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "timestamp": 1697789400000
}
```

#### Produit: `user-events`
```json
{
  "eventType": "USER_CREATED|USER_UPDATED|USER_DELETED",
  "userId": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "timestamp": "2025-10-20T10:30:00"
}
```

## 🌐 Endpoints API

| Méthode | Endpoint              | Auth | Description                     |
|---------|----------------------|------|---------------------------------|
| GET     | /api/users/profile    | ✅   | Profil complet (soi-même)       |
| PUT     | /api/users/profile    | ✅   | Mettre à jour son profil        |
| DELETE  | /api/users/profile    | ✅   | Supprimer son compte            |
| GET     | /api/users/{userId}   | ❌   | Profil public (n'importe qui)   |

## 🧪 Tests recommandés

1. **Tests unitaires** (UserService)
   - getUserProfile()
   - updateProfile()
   - deleteProfile()
   - createUser()

2. **Tests d'intégration** (UserController)
   - Endpoints REST
   - Validation JWT
   - Gestion des erreurs

3. **Tests Kafka**
   - Consumer Keycloak events
   - Producer User events

## 📦 Dépendances clés

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter OAuth2 Resource Server
- Spring Boot Starter Validation
- Spring Kafka
- PostgreSQL Driver
- Lombok
- SpringDoc OpenAPI (Swagger)
