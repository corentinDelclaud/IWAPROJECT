# Guide Rapide - Configuration et Tests

## 🚀 Démarrage Rapide

### API Gateway

```bash
cd back/api-gateway
mvn spring-boot:run
```

### Keycloak

Utiliser un docker compose pour lancer Keycloak et sa base de données

```bash
cd back/keycloak-service
docker-compose up
```

Accéder à la console administrateur :
http://localhost:8085
admin/admin

### Auth microservice

Lance le microservice d'authentification

```bash
cd back/auth-service
mvn spring-boot:run
```

Accéder au swagger-ui :
http://localhost:8082/swagger-ui/index.html

### User microservice

Lance le microservice User et sa base de données

```bash
cd back/user-service
docker-compose up -d
mvn spring-boot:run
```

Accéder au swagger-ui :
http://localhost:8081/swagger-ui/index.html
