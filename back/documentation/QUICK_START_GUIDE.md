# Guide Rapide - Configuration et Tests

## 🚀 Démarrage Rapide

### 🐳 Option 1 : Docker (Recommandé - Tout en une commande)

Démarrer **tous les services** avec Docker Compose :

```bash
cd back
./start-docker.sh
```

Cette commande va démarrer :
- ✅ Tous les microservices (API Gateway, Auth, User, Catalog)
- ✅ Keycloak (serveur d'authentification)
- ✅ Toutes les bases de données PostgreSQL

Pour arrêter :
```bash
cd back
./stop-docker.sh
```

📖 **Documentation complète** : Voir [DOCKER_README.md](./DOCKER_README.md)

---

### 🔧 Option 2 : Mode Développement (Services individuels avec Maven)

#### API Gateway

```bash
cd back/api-gateway
mvn spring-boot:run
```

#### Keycloak

Utiliser un docker compose pour lancer Keycloak et sa base de données

```bash
cd back/keycloak-service
docker-compose up
```

Accéder à la console administrateur :
http://localhost:8085
admin/admin

#### Auth microservice

Lance le microservice d'authentification

```bash
cd back/auth-service
mvn spring-boot:run
```

Accéder au swagger-ui :
http://localhost:8082/swagger-ui/index.html

#### User microservice

Lance le microservice User et sa base de données

```bash
cd back/user-microservice
docker-compose up -d
mvn spring-boot:run
```

Accéder au swagger-ui :
http://localhost:8081/swagger-ui/index.html

#### Service Catalog

Lance le microservice Catalog

```bash
cd back/service-catalog
mvn spring-boot:run
```

Accéder au swagger-ui :
http://localhost:8083/swagger-ui/index.html

---

## 📊 Services disponibles

| Service | URL | Swagger |
|---------|-----|---------|
| **Keycloak** | http://localhost:8085 | - |
| **API Gateway** | http://localhost:8080 | - |
| **Auth Service** | http://localhost:8082 | [Swagger](http://localhost:8082/swagger-ui/index.html) |
| **User Microservice** | http://localhost:8081 | [Swagger](http://localhost:8081/swagger-ui/index.html) |
| **Service Catalog** | http://localhost:8083 | [Swagger](http://localhost:8083/swagger-ui/index.html) |
