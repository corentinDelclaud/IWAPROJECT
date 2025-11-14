# 🚀 IWA Project - Backend

Architecture microservices complète avec Spring Boot, Keycloak et PostgreSQL.

## 📋 Table des matières

- [Démarrage rapide](#-démarrage-rapide)
- [Architecture](#-architecture)
- [Services disponibles](#-services-disponibles)
- [Documentation](#-documentation)
- [Développement](#-développement)

---

## 🎯 Démarrage rapide

### Option 1 : Docker (Recommandé) 🐳

**Tout démarrer en une commande :**

```bash
./start-docker.sh
```

**Vérifier que tout fonctionne :**

```bash
./check-health.sh
```

**Arrêter tous les services :**

```bash
./stop-docker.sh
```

### Option 2 : Avec Make

```bash
make help    # Afficher toutes les commandes disponibles
make start   # Démarrer tous les services
make health  # Vérifier la santé des services
make logs    # Voir les logs
make stop    # Arrêter tous les services
```

### Option 3 : Développement manuel (Maven)

Voir le [QUICK_START_GUIDE.md](./QUICK_START_GUIDE.md)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway :8080                    │
│              (Point d'entrée unique)                    │
└──────────────────┬──────────────────────────────────────┘
                   │
        ┌──────────┼──────────┬──────────────┐
        │          │          │              │
        ▼          ▼          ▼              ▼
   ┌────────┐ ┌────────┐ ┌────────┐   ┌──────────┐
   │  Auth  │ │  User  │ │Catalog │   │ Keycloak │
   │ :8082  │ │ :8081  │ │ :8083  │   │  :8085   │
   └────┬───┘ └───┬────┘ └───┬────┘   └─────┬────┘
        │         │          │              │
        ▼         ▼          ▼              ▼
   ┌────────────────────────────────────────────┐
   │        Bases de données PostgreSQL         │
   │  • Users DB      (port 5433)              │
   │  • Catalog DB    (port 5434)              │
   │  • Keycloak DB   (port 5435)              │
   └────────────────────────────────────────────┘
```

---

## 📊 Services disponibles

| Service | Port | Description | Swagger | Health |
|---------|------|-------------|---------|--------|
| **API Gateway** | 8080 | Point d'entrée unique | - | [Health](http://localhost:8080/actuator/health) |
| **Auth Service** | 8082 | Authentification | [Swagger](http://localhost:8082/swagger-ui/index.html) | [Health](http://localhost:8082/actuator/health) |
| **User Microservice** | 8081 | Gestion utilisateurs | [Swagger](http://localhost:8081/swagger-ui/index.html) | [Health](http://localhost:8081/actuator/health) |
| **Service Catalog** | 8083 | Gestion produits | [Swagger](http://localhost:8083/swagger-ui/index.html) | [Health](http://localhost:8083/actuator/health) |
| **Keycloak** | 8085 | Serveur d'auth | [Admin Console](http://localhost:8085) | [Health](http://localhost:8085/health/ready) |

### Identifiants Keycloak
- **URL**: http://localhost:8085
- **Username**: `admin`
- **Password**: `admin`

---

## 📚 Documentation

### Guides principaux
- 🐳 **[DOCKER_README.md](./DOCKER_README.md)** - Guide complet Docker
- 🚀 **[QUICK_START_GUIDE.md](./QUICK_START_GUIDE.md)** - Guide de démarrage rapide
- 📦 **[CONTAINERISATION_SUMMARY.md](./CONTAINERISATION_SUMMARY.md)** - Résumé de la containerisation

### Documentation par service
- [API Gateway README](./api-gateway/README.md)
- [Auth Service README](./auth-service/README.md)
- [User Microservice README](./user-microservice/README.md)
- [Service Catalog README](./service-catalog/README.md)
- [Keycloak Service README](./keycloak-service/README.md)

---

## 🛠️ Développement

### Prérequis

- **Java** 21
- **Maven** 3.9+
- **Docker** 20.10+
- **Docker Compose** 2.0+

### Structure du projet

```
back/
├── api-gateway/          # API Gateway (Spring Cloud Gateway)
├── auth-service/         # Service d'authentification
├── user-microservice/    # Gestion des utilisateurs
├── service-catalog/      # Catalogue de produits
├── keycloak-service/     # Configuration Keycloak
├── docker-compose.yml    # Orchestration complète
├── start-docker.sh       # Script de démarrage
├── stop-docker.sh        # Script d'arrêt
├── check-health.sh       # Vérification de santé
├── test-integration.sh   # Tests d'intégration
└── Makefile             # Commandes simplifiées
```

### Commandes Docker

```bash
# Démarrer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f api-gateway

# Arrêter tous les services
docker-compose down

# Reconstruire les images
docker-compose build

# Reconstruire et redémarrer
docker-compose up -d --build

# Voir l'état des conteneurs
docker-compose ps
```

### Commandes Maven (développement local)

```bash
# Depuis la racine du projet
mvn clean install

# Depuis un service spécifique
cd api-gateway
mvn spring-boot:run
```

### Tests

```bash
# Tests unitaires
mvn test

# Tests d'intégration (avec Docker)
./test-integration.sh

# Vérification de santé
./check-health.sh
```

---

## 🔧 Configuration

### Variables d'environnement

Copiez `.env.example` vers `.env` et modifiez selon vos besoins :

```bash
cp .env.example .env
```

### Ports utilisés

| Port | Service |
|------|---------|
| 8080 | API Gateway |
| 8081 | User Microservice |
| 8082 | Auth Service |
| 8083 | Service Catalog |
| 8085 | Keycloak |
| 5433 | PostgreSQL Users |
| 5434 | PostgreSQL Catalog |
| 5435 | PostgreSQL Keycloak |

---

## 🐛 Dépannage

### Les services ne démarrent pas

```bash
# Vérifier les logs
docker-compose logs -f

# Vérifier l'état
docker-compose ps

# Reconstruire proprement
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### Port déjà utilisé

Modifiez le port dans `docker-compose.yml` :

```yaml
ports:
  - "8081:8080"  # Utiliser 8081 au lieu de 8080
```

### Problèmes de connexion à Keycloak

```bash
# Vérifier que Keycloak est démarré
docker-compose logs keycloak

# Attendre le démarrage complet (peut prendre 1-2 minutes)
./check-health.sh
```

---

## 📦 Build et Déploiement

### Build des images Docker

```bash
# Build de tous les services
docker-compose build

# Build d'un service spécifique
docker-compose build api-gateway
```

### Tag et Push (pour un registry)

```bash
# Tagging
docker tag iwa-api-gateway:latest your-registry/iwa-api-gateway:1.0.0

# Push
docker push your-registry/iwa-api-gateway:1.0.0
```

---

## 🤝 Contribution

1. Créer une branche pour votre fonctionnalité
2. Faire vos modifications
3. Tester avec `./test-integration.sh`
4. Créer une Pull Request

---

## 📄 Licence

[À définir]

---

## 👥 Équipe

[À compléter]

---

## 🔗 Liens utiles

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Docker Documentation](https://docs.docker.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Version** : 1.0  
**Dernière mise à jour** : 10 novembre 2025
