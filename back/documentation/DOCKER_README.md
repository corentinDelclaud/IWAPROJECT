# 🐳 Guide Docker - IWA Project

## 📋 Vue d'ensemble

Ce projet utilise Docker et Docker Compose pour containeriser l'ensemble de l'architecture microservices. Tous les services peuvent être démarrés avec **une seule commande**.

## 🏗️ Architecture

L'infrastructure complète comprend :

### Microservices
- **API Gateway** (port 8080) - Point d'entrée unique
- **Auth Service** (port 8082) - Service d'authentification
- **User Microservice** (port 8081) - Gestion des utilisateurs
- **Service Catalog** (port 8083) - Gestion des produits

### Services d'infrastructure
- **Keycloak** (port 8085) - Serveur d'authentification
- **PostgreSQL Users** (port 5433) - Base de données utilisateurs
- **PostgreSQL Catalog** (port 5434) - Base de données produits
- **PostgreSQL Keycloak** (port 5435) - Base de données Keycloak

## 🚀 Démarrage rapide

### Prérequis

- Docker >= 20.10
- Docker Compose >= 2.0

### Démarrer tous les services

```bash
# Option 1 : Avec le script (recommandé)
./start-docker.sh

# Option 2 : Directement avec docker-compose
docker-compose up -d
```

Le script `start-docker.sh` va :
1. ✅ Construire toutes les images Docker
2. ✅ Démarrer tous les conteneurs
3. ✅ Afficher les URLs de tous les services

### Arrêter tous les services

```bash
# Option 1 : Avec le script
./stop-docker.sh

# Option 2 : Directement avec docker-compose
docker-compose down
```

## 📊 Commandes utiles

### Gestion des services

```bash
# Voir tous les conteneurs en cours d'exécution
docker-compose ps

# Voir les logs de tous les services
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
docker-compose logs -f user-microservice
docker-compose logs -f service-catalog
docker-compose logs -f keycloak

# Redémarrer un service spécifique
docker-compose restart api-gateway

# Redémarrer tous les services
docker-compose restart

# Arrêter un service spécifique
docker-compose stop api-gateway

# Démarrer un service spécifique
docker-compose start api-gateway
```

### Reconstruction des images

```bash
# Reconstruire toutes les images
docker-compose build

# Reconstruire une image spécifique
docker-compose build api-gateway

# Reconstruire et redémarrer
docker-compose up -d --build
```

### Nettoyage

```bash
# Arrêter et supprimer les conteneurs
docker-compose down

# Arrêter et supprimer les conteneurs + volumes (⚠️ supprime les données)
docker-compose down -v

# Supprimer les images inutilisées
docker image prune -a
```

## 🔗 URLs des services

Une fois tous les services démarrés :

| Service | URL | Documentation |
|---------|-----|---------------|
| **Keycloak** | http://localhost:8085 | admin / admin |
| **API Gateway** | http://localhost:8080 | - |
| **Auth Service** | http://localhost:8082 | [Swagger](http://localhost:8082/swagger-ui/index.html) |
| **User Microservice** | http://localhost:8081 | [Swagger](http://localhost:8081/swagger-ui/index.html) |
| **Service Catalog** | http://localhost:8083 | [Swagger](http://localhost:8083/swagger-ui/index.html) |

## 🗄️ Bases de données

Connexion aux bases PostgreSQL :

```bash
# Base Users
docker exec -it iwa-postgres-users psql -U postgres -d iwa_users

# Base Catalog
docker exec -it iwa-postgres-catalog psql -U postgres -d iwa_catalog

# Base Keycloak
docker exec -it iwa-postgres-keycloak psql -U keycloak -d keycloak
```

Ou via un client externe :
- **Users**: `localhost:5433` (user: postgres, password: postgres)
- **Catalog**: `localhost:5434` (user: postgres, password: postgres)
- **Keycloak**: `localhost:5435` (user: keycloak, password: keycloak_password)

## 🔍 Dépannage

### Les services ne démarrent pas

```bash
# Vérifier les logs
docker-compose logs

# Vérifier l'état de tous les conteneurs
docker-compose ps

# Reconstruire les images
docker-compose build --no-cache
docker-compose up -d
```

### Ports déjà utilisés

Si un port est déjà utilisé, modifiez le mapping dans `docker-compose.yml` :

```yaml
ports:
  - "8080:8080"  # Changer le premier port (host) si nécessaire
```

### Problèmes de connexion entre services

Les services communiquent via le réseau Docker `iwa-network`. Utilisez les noms de services (pas localhost) :
- ✅ `http://keycloak:8085`
- ❌ `http://localhost:8085`

### Réinitialiser complètement

```bash
# Tout arrêter et supprimer (y compris les volumes)
docker-compose down -v

# Supprimer les images
docker rmi iwa-api-gateway iwa-auth-service iwa-user-microservice iwa-service-catalog

# Redémarrer
./start-docker.sh
```

## 📁 Structure des Dockerfiles

Chaque microservice utilise un build multi-stage :

```dockerfile
# Stage 1 : Build avec Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
# ... compilation du JAR

# Stage 2 : Runtime avec JRE uniquement
FROM eclipse-temurin:21-jre-alpine
# ... exécution du JAR
```

Avantages :
- ✅ Images finales légères (JRE seulement)
- ✅ Build reproductible
- ✅ Sécurité améliorée

## 🔒 Sécurité

⚠️ **Important pour la production** :
- Changez tous les mots de passe par défaut
- Utilisez des secrets Docker ou variables d'environnement sécurisées
- Activez HTTPS/TLS
- Limitez l'exposition des ports

## 🆚 Docker vs Développement local

| Aspect | Docker | Local (mvn) |
|--------|--------|-------------|
| Setup | `./start-docker.sh` | Démarrer chaque service manuellement |
| Isolation | Complète | Partielle |
| Ports | Configurables | Fixes |
| Hot reload | ❌ (rebuild requis) | ✅ |
| CI/CD | ✅ Idéal | ❌ |

**Recommandation** : 
- 🔧 **Développement** : Mode local avec `mvn spring-boot:run`
- 🧪 **Tests d'intégration** : Docker
- 🚀 **Production** : Docker

## 📚 Ressources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
