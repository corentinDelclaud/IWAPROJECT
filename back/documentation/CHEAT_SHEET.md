# 📝 Cheat Sheet - IWA Project

Guide rapide de toutes les commandes utiles.

## 🚀 Démarrage / Arrêt

```bash
# Démarrer tout (méthode 1 - script)
./start-docker.sh

# Démarrer tout (méthode 2 - make)
make start

# Démarrer tout (méthode 3 - docker-compose)
docker-compose up -d

# Arrêter tout
./stop-docker.sh
# ou
make stop
# ou
docker-compose down

# Arrêter et supprimer les volumes (⚠️ perte de données)
docker-compose down -v
```

## 🔍 Monitoring

```bash
# Vérifier la santé des services
./check-health.sh
# ou
make health

# Voir l'état des conteneurs
docker-compose ps
# ou
make ps

# Voir les logs de tous les services
docker-compose logs -f
# ou
make logs

# Logs d'un service spécifique
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
docker-compose logs -f user-microservice
docker-compose logs -f service-catalog
docker-compose logs -f keycloak

# ou avec Make
make logs-gateway
make logs-auth
make logs-user
make logs-catalog
make logs-keycloak

# Voir les dernières 100 lignes
docker-compose logs --tail=100 api-gateway
```

## 🔄 Redémarrage

```bash
# Redémarrer tous les services
docker-compose restart
# ou
make restart

# Redémarrer un service spécifique
docker-compose restart api-gateway

# Arrêter un service
docker-compose stop api-gateway

# Démarrer un service
docker-compose start api-gateway
```

## 🔨 Build / Rebuild

```bash
# Construire toutes les images
docker-compose build
# ou
make build

# Construire une image spécifique
docker-compose build api-gateway

# Construire sans cache (build propre)
docker-compose build --no-cache

# Reconstruire et redémarrer
docker-compose up -d --build
# ou
make rebuild
```

## 🧪 Tests

```bash
# Tests d'intégration
./test-integration.sh

# Tests unitaires Maven
cd <service>
mvn test

# Tests avec Maven depuis la racine
mvn clean test
```

## 🗄️ Bases de données

```bash
# Se connecter à la base Users
docker exec -it iwa-postgres-users psql -U postgres -d iwa_users
# ou
make db-users

# Se connecter à la base Catalog
docker exec -it iwa-postgres-catalog psql -U postgres -d iwa_catalog
# ou
make db-catalog

# Se connecter à la base Keycloak
docker exec -it iwa-postgres-keycloak psql -U keycloak -d keycloak
# ou
make db-keycloak

# Backup d'une base
docker exec iwa-postgres-users pg_dump -U postgres iwa_users > backup.sql

# Restaurer une base
docker exec -i iwa-postgres-users psql -U postgres iwa_users < backup.sql

# Voir les tables (une fois connecté au psql)
\dt

# Quitter psql
\q
```

## 🧹 Nettoyage

```bash
# Nettoyage complet (conteneurs + volumes + images)
make clean

# Ou manuellement :
docker-compose down -v
docker rmi iwa-api-gateway iwa-auth-service iwa-user-microservice iwa-service-catalog

# Supprimer toutes les images inutilisées
docker image prune -a

# Supprimer tous les volumes non utilisés
docker volume prune

# Nettoyage complet Docker (⚠️ attention)
docker system prune -a --volumes
```

## 🔧 Développement local (sans Docker)

```bash
# API Gateway
cd api-gateway
mvn spring-boot:run

# Auth Service
cd auth-service
mvn spring-boot:run

# User Microservice
cd user-microservice
mvn spring-boot:run

# Service Catalog
cd service-catalog
mvn spring-boot:run

# Build complet
mvn clean install

# Skip tests
mvn clean install -DskipTests
```

## 📊 Inspection

```bash
# Inspecter un conteneur
docker inspect iwa-api-gateway

# Voir les processus d'un conteneur
docker top iwa-api-gateway

# Statistiques en temps réel
docker stats

# Voir l'utilisation des volumes
docker volume ls
docker volume inspect back_postgres_users_data

# Voir les réseaux
docker network ls
docker network inspect back_iwa-network
```

## 🐚 Shell / Exec

```bash
# Se connecter au shell d'un conteneur
docker exec -it iwa-api-gateway sh

# Exécuter une commande dans un conteneur
docker exec iwa-api-gateway ls -la /app

# Voir les variables d'environnement
docker exec iwa-api-gateway env

# Vérifier la version Java
docker exec iwa-api-gateway java -version
```

## 🌐 Accès aux services

```bash
# Tester un endpoint
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8085/health/ready

# Avec formatage JSON (jq requis)
curl -s http://localhost:8080/actuator/health | jq

# Tester avec authentification
TOKEN="your-jwt-token"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/users
```

## 🔐 Keycloak

```bash
# Accéder à l'admin console
open http://localhost:8085

# Credentials par défaut
Username: admin
Password: admin

# Realm
IWA_NextLevel

# Obtenir un token (exemple)
curl -X POST http://localhost:8085/realms/IWA_NextLevel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser" \
  -d "password=testpass" \
  -d "grant_type=password" \
  -d "client_id=your-client-id"
```

## 📈 Performance / Debugging

```bash
# Voir l'utilisation CPU/Mémoire
docker stats

# Limiter la mémoire d'un service (dans docker-compose.yml)
services:
  api-gateway:
    mem_limit: 512m
    mem_reservation: 256m

# Voir les logs avec horodatage
docker-compose logs -f --timestamps

# Suivre les logs en temps réel avec grep
docker-compose logs -f | grep ERROR
```

## 🔄 Docker Compose avancé

```bash
# Démarrer un seul service et ses dépendances
docker-compose up -d api-gateway

# Mise à l'échelle (scaling)
docker-compose up -d --scale api-gateway=3

# Voir la configuration compilée
docker-compose config

# Valider le fichier docker-compose.yml
docker-compose config --quiet
```

## 📦 Images

```bash
# Lister les images
docker images

# Supprimer une image
docker rmi iwa-api-gateway:latest

# Tagger une image
docker tag iwa-api-gateway:latest iwa-api-gateway:1.0.0

# Sauvegarder une image
docker save iwa-api-gateway:latest > api-gateway.tar

# Charger une image
docker load < api-gateway.tar

# Push vers un registry
docker tag iwa-api-gateway:latest registry.example.com/iwa-api-gateway:latest
docker push registry.example.com/iwa-api-gateway:latest
```

## 🆘 Dépannage

```bash
# Service ne démarre pas
docker-compose logs <service>
docker inspect <container>

# Port déjà utilisé
sudo lsof -i :<port>
sudo netstat -tulpn | grep :<port>

# Problème de réseau
docker network inspect back_iwa-network

# Problème de volume
docker volume inspect back_postgres_users_data

# Reset complet
docker-compose down -v
docker system prune -a --volumes
./start-docker.sh
```

## 📝 Variables d'environnement

```bash
# Créer un fichier .env
cp .env.example .env

# Docker Compose chargera automatiquement .env

# Ou spécifier un fichier différent
docker-compose --env-file .env.production up -d

# Voir les variables d'un service
docker exec iwa-api-gateway env
```

## 🎯 Makefile

```bash
# Voir toutes les commandes Make
make help

# Commandes principales
make start      # Démarrer
make stop       # Arrêter
make restart    # Redémarrer
make logs       # Voir logs
make build      # Construire
make rebuild    # Reconstruire et redémarrer
make clean      # Nettoyage complet
make health     # Vérifier santé
make ps         # Statut conteneurs
```

## 🔗 URLs rapides

```bash
# Ouvrir dans le navigateur
open http://localhost:8080  # API Gateway
open http://localhost:8081  # User Service
open http://localhost:8082  # Auth Service
open http://localhost:8083  # Service Catalog
open http://localhost:8085  # Keycloak

# Swagger
open http://localhost:8081/swagger-ui/index.html  # User
open http://localhost:8082/swagger-ui/index.html  # Auth
open http://localhost:8083/swagger-ui/index.html  # Catalog
```

## 💡 Tips

```bash
# Raccourcis bash utiles (ajoutez à votre .bashrc)
alias dc='docker-compose'
alias dcu='docker-compose up -d'
alias dcd='docker-compose down'
alias dcl='docker-compose logs -f'
alias dcp='docker-compose ps'

# Fonction pour logs d'un service
dclog() { docker-compose logs -f "$1"; }

# Usage
dc ps
dcu
dclog api-gateway
```

---

**💾 Sauvegardez ce fichier** - Vous y reviendrez souvent !
