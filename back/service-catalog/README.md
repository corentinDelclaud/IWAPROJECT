# Service Catalog - Microservice de Catalogue

## Description

Microservice Spring Boot pour la gestion du catalogue des services gaming (boosting, coaching, revente de comptes).

## Structure de la base de données

### Entité Service
```
- idService: int (PK)
- game: enum (League of Legends, Teamfight Tactics, Rocket League, Valorant, Other)
- serviceType: enum (Boosting, Coaching, Account resaling, Other)
- description: string
- price: float
- unique: boolean (service unique ou multiple)
- isAvailable: boolean
- idProvider: int (FK vers Provider)
```

## Technologies

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (développement)
- **PostgreSQL** (production)
- **Lombok**
- **Maven**

## Démarrage rapide

### 1. Compiler le projet
```bash
cd back/service-catalog
mvn clean install
```

### 2. Lancer le microservice
```bash
mvn spring-boot:run
```

Le service démarre sur **http://localhost:8083**

### 3. Accéder à la console H2
- URL : http://localhost:8083/h2-console
- JDBC URL : `jdbc:h2:mem:catalogdb`
- Username : `sa`
- Password : (vide)

## API Endpoints

### Récupérer les services

#### Tous les services
```http
GET http://localhost:8083/api/catalog
```

#### Services disponibles uniquement
```http
GET http://localhost:8083/api/catalog?available=true
```

#### Service par ID
```http
GET http://localhost:8083/api/catalog/{id}
```

#### Filtrer par jeu
```http
GET http://localhost:8083/api/catalog/game/VALORANT
GET http://localhost:8083/api/catalog/game/LEAGUE_OF_LEGENDS
```

Valeurs possibles : `LEAGUE_OF_LEGENDS`, `TEAMFIGHT_TACTICS`, `ROCKET_LEAGUE`, `VALORANT`, `OTHER`

#### Filtrer par type
```http
GET http://localhost:8083/api/catalog/type/COACHING
GET http://localhost:8083/api/catalog/type/BOOSTING
```

Valeurs possibles : `BOOSTING`, `COACHING`, `ACCOUNT_RESALING`, `OTHER`

#### Filtrer par jeu ET type
```http
GET http://localhost:8083/api/catalog/filter?game=VALORANT&type=COACHING
```

#### Services d'un provider
```http
GET http://localhost:8083/api/catalog/provider/{idProvider}
```

### Créer un service

```http
POST http://localhost:8083/api/catalog
Content-Type: application/json

{
  "game": "VALORANT",
  "serviceType": "COACHING",
  "description": "Coaching personnalisé pour atteindre Radiant",
  "price": 30.0,
  "unique": false,
  "isAvailable": true,
  "idProvider": 1
}
```

### Mettre à jour un service

```http
PUT http://localhost:8083/api/catalog/{id}
Content-Type: application/json

{
  "game": "VALORANT",
  "serviceType": "COACHING",
  "description": "Description mise à jour",
  "price": 35.0,
  "unique": false,
  "isAvailable": true,
  "idProvider": 1
}
```

### Supprimer un service

```http
DELETE http://localhost:8083/api/catalog/{id}
```

### Changer la disponibilité

```http
PATCH http://localhost:8083/api/catalog/{id}/toggle-availability
```

## Exemples de réponses

### Liste des services
```json
[
  {
    "idService": 1,
    "game": "Valorant",
    "serviceType": "Coaching",
    "description": "Coaching personnalisé Valorant",
    "price": 30.0,
    "unique": false,
    "isAvailable": true,
    "idProvider": 1
  }
]
```

## Données de test

Au démarrage, 5 services de test sont automatiquement créés :
1. Coaching Valorant (30€)
2. Boosting LoL (20€)
3. Boosting Valorant (45€)
4. Compte LoL (25€)
5. Coaching Rocket League (35€)

## Configuration Production

Pour utiliser PostgreSQL en production, modifier `application.properties` :

```properties
# Commenter la config H2
#spring.datasource.url=jdbc:h2:mem:catalogdb

# Décommenter la config PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/catalogdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Architecture

```
service-catalog/
├── src/main/java/com/iwaproject/catalog/
│   ├── controller/          # REST Controllers
│   │   └── CatalogController.java
│   ├── service/             # Business Logic
│   │   └── CatalogService.java
│   ├── repository/          # Data Access Layer
│   │   └── ServiceRepository.java
│   ├── model/              # Entities & Enums
│   │   ├── Service.java
│   │   ├── Game.java
│   │   └── ServiceType.java
│   ├── dto/                # Data Transfer Objects
│   │   ├── ServiceDTO.java
│   │   └── CreateServiceRequest.java
│   ├── DataInitializer.java    # Données de test
│   └── CatalogServiceApplication.java
└── src/main/resources/
    └── application.properties
```

## Intégration avec l'API Gateway

Le microservice est conçu pour être appelé via l'API Gateway sur le port 8080.

Configuration Gateway à ajouter :
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-catalog
          uri: http://localhost:8083
          predicates:
            - Path=/api/catalog/**
```

## Tests

```bash
# Lancer les tests
mvn test

# Avec couverture
mvn clean test jacoco:report
```

## Logs

Les logs sont configurés en mode DEBUG pour le développement.
Visible dans la console lors du démarrage et des requêtes.

## Support

Pour toute question, consulter la documentation Spring Boot ou contacter l'équipe de développement.

