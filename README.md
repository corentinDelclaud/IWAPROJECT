# 🎮 IWA PROJECT

> Plateforme de marketplace mobile avec architecture microservices

Une application mobile complète de marketplace construite avec React Native (Expo) et une architecture backend microservices basée sur Spring Boot, avec authentification Keycloak et intégration Stripe.

---

## 📑 Table des matières

- [✨ Fonctionnalités](#-fonctionnalités)
- [🏗️ Architecture](#️-architecture)
- [🚀 Démarrage rapide](#-démarrage-rapide)
- [🛠️ Stack technique](#️-stack-technique)
- [📂 Structure du projet](#-structure-du-projet)
- [🔧 Configuration](#-configuration)
- [📚 Documentation](#-documentation)
- [👥 Contribution](#-contribution)

---

## ✨ Fonctionnalités

### 🎯 Fonctionnalités principales
- **Marketplace** : Consultation et achat de services/produits
- **Messagerie temps réel** : Chat entre utilisateurs via SSE (Server-Sent Events)
- **Authentification sécurisée** : OAuth2/OIDC avec Keycloak
- **Paiements** : Intégration Stripe pour les transactions
- **Multi-langue** : Support FR, EN, DE
- **Gestion de profil** : Édition des informations utilisateur

### 🔐 Sécurité
- Authentification Keycloak (SSO)
- Gestion des rôles et permissions
- Tokens JWT pour les API
- Validation côté serveur et client

---

## 🏗️ Architecture

### Backend - Microservices

```
┌─────────────────┐
│   API Gateway   │ (Port 8080)
│   (Spring Cloud)│
└────────┬────────┘
         │
    ┌────┴────┬────────────┬──────────────┬─────────────┐
    │         │            │              │             │
┌───▼──┐  ┌──▼───┐  ┌─────▼──────┐  ┌───▼────┐  ┌────▼─────┐
│ Auth │  │ User │  │  Catalog   │  │ Stripe │  │Transaction│
│ 8082 │  │ 8081 │  │   8083     │  │ 8084   │  │   8086    │
└──────┘  └──┬───┘  └─────┬──────┘  └───┬────┘  └────┬──────┘
             │            │              │            │
         ┌───▼────────────▼──────────────▼────────────▼───┐
         │           PostgreSQL Databases                 │
         │  (Users, Catalog, Transactions, Keycloak)      │
         └────────────────────────────────────────────────┘
```

### Services
- **API Gateway** (8080) : Point d'entrée unique, routage des requêtes
- **Auth Service** (8082) : Authentification et gestion des tokens
- **User Service** (8081) : Gestion des utilisateurs et profils
- **Catalog Service** (8083) : Gestion des produits/services
- **Stripe Service** (8084) : Paiements et onboarding marchands
- **Transaction Service** (8086) : Historique des transactions
- **Keycloak** (8085) : Serveur d'identité (IAM)

---

## 🚀 Démarrage rapide

### Prérequis

- **Docker** & **Docker Compose** (recommandé)
- **Node.js** 18+ et **npm**
- **Java 21** (pour développement backend)
- **Maven 3.8+** (pour développement backend)

### 🎬 Démarrage de l'application

#### 1️⃣ Configuration Backend

**Avant de démarrer le backend, configurer le fichier `.env` :**

```bash
cd back
cp .env.example .env
# Éditer .env et remplacer VOTRE_IP par votre adresse IP locale
```

Dans `/back/.env`, modifier la ligne :
```env
API_HOST=VOTRE_IP  # Par exemple: 192.168.1.12
```

> ⚠️ **Important** : L'IP dans le backend doit être la **même** que celle utilisée pour le frontend !

**Puis démarrer les services :**

```bash
docker-compose up --build -d
```

Les services démarrent sur :
- API Gateway : http://localhost:8080
- Keycloak : http://localhost:8085
- Auth Service : http://localhost:8082
- User Service : http://localhost:8081
- Catalog Service : http://localhost:8083
- Stripe Service : http://localhost:8084
- Transaction Service : http://localhost:8086

#### 2️⃣ Configuration Frontend

**Sur Linux/macOS :**
```bash
cd front
cp .env.example .env
# Éditer .env et remplacer VOTRE_IP par votre adresse IP locale
EXPO_PUBLIC_API_HOST="VOTRE_IP" npm start
```

**Sur Windows (PowerShell) :**
```powershell
cd front
Copy-Item .env.example .env
# Éditer .env et remplacer VOTRE_IP par votre adresse IP locale
$env:EXPO_PUBLIC_API_HOST="VOTRE_IP"; npm start
```

> 💡 **Trouver votre IP** :
> - Linux : `hostname -I | awk '{print $1}'`
> - macOS : `ipconfig getifaddr en0`
> - Windows : `ipconfig` (chercher l'adresse IPv4)

#### 3️⃣ Configuration Keycloak

1. Ouvrir http://localhost:8085 dans votre navigateur
2. Se connecter avec :
   - **Username** : `admin`
   - **Password** : `admin`
3. Passer du realm **master** à **IWAPROJECT** (menu déroulant en haut à gauche)
4. Aller dans **Clients** → Cliquer sur **auth-service**
5. Dans **Valid redirect URIs**, ajouter l'URL de votre Expo :
   ```
   exp://cfxwv_8-anonymous-19000.exp.direct/--/*
   ```
   (Remplacer par l'URL affichée dans votre terminal Expo)
6. Sauvegarder

#### 4️⃣ Lancer l'application mobile

- Scanner le QR code avec l'app **Expo Go**
- Ou appuyer sur `w` pour ouvrir dans le navigateur
- Ou appuyer sur `a` pour Android / `i` pour iOS

---

## 🛠️ Stack technique

### Frontend
- **Framework** : React Native avec Expo (~54.0)
- **Navigation** : Expo Router (file-based routing)
- **État** : React Context API
- **Authentification** : expo-auth-session, expo-web-browser
- **Internationalisation** : i18next (FR, EN, DE)
- **UI** : Expo Vector Icons, React Native Reanimated
- **Temps réel** : react-native-sse (Server-Sent Events)

### Backend
- **Framework** : Spring Boot 3.4.11
- **Java** : 21 (LTS)
- **Architecture** : Microservices avec Spring Cloud
- **API Gateway** : Spring Cloud Gateway
- **Base de données** : PostgreSQL 16
- **Authentification** : Keycloak 26.0.7 (OAuth2/OIDC)
- **Paiements** : Stripe API
- **Conteneurisation** : Docker & Docker Compose

### DevOps
- **Orchestration** : Docker Compose
- **CI/CD** : GitHub Actions 
- **Tests** : JUnit, Maven

---

## 📂 Structure du projet

```
IWAPROJECT/
├── back/                          # Backend microservices
│   ├── api-gateway/              # Point d'entrée (8080)
│   ├── auth-service/             # Authentification (8082)
│   ├── user-microservice/        # Gestion users (8081)
│   ├── service-catalog/          # Catalogue produits (8083)
│   ├── stripe-service/           # Paiements Stripe (8084)
│   ├── microservice-transaction/ # Transactions (8086)
│   ├── keycloak-service/         # Config Keycloak (8085)
│   ├── docker-compose.yml        # Orchestration Docker
│   └── scripts/                  # Scripts utilitaires
│
├── front/                        # Application mobile React Native
│   ├── app/                      # Écrans (file-based routing)
│   │   ├── (tabs)/              # Navigation par onglets
│   │   ├── login.tsx            # Écran de connexion
│   │   ├── conversation/        # Chat/messagerie
│   │   └── product/             # Détails produit
│   ├── components/              # Composants réutilisables
│   ├── services/                # Appels API
│   ├── context/                 # Gestion état global
│   ├── locales/                 # Traductions (i18n)
│   └── config/                  # Configuration
│
└── README.md                     # Ce fichier
```

---

## 🔧 Configuration

### Variables d'environnement Backend

Créer un fichier `.env` dans `/back` :

```env
# API Configuration
API_HOST=localhost

# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# Stripe
STRIPE_API_KEY=votre_clé_stripe
STRIPE_WEBHOOK_SECRET=votre_secret_webhook
```

### Variables d'environnement Frontend

Créer un fichier `.env` dans `/front` :

```env
EXPO_PUBLIC_API_HOST=192.168.1.XX  # Votre IP locale
EXPO_PUBLIC_API_PORT=8080
EXPO_PUBLIC_KEYCLOAK_URL=http://192.168.1.XX:8085
```

---

## 📚 Documentation

### Documentation Backend
- [Guide de démarrage rapide](back/documentation/QUICK_START_GUIDE.md)
- [Architecture détaillée](back/documentation/ARCHITECTURE_DIAGRAM.md)
- [Guide Docker](back/documentation/DOCKER_README.md)
- [Cheat Sheet](back/documentation/CHEAT_SHEET.md)

### Documentation Services
- [API Gateway](back/api-gateway/README.md)
- [Auth Service](back/auth-service/README.md)
- [User Service](back/user-microservice/README.md)
- [Stripe Service](back/stripe-service/README.md)
- [Keycloak Service](back/keycloak-service/README.md)

### Tutoriels
- [SSE avec React Native](tuto_sse_react_native.txt)
- [SSE avec Spring](tuto_sse_spring.txt)

---

## 🧪 Tests et débogage

### Vérifier l'état des services

```bash
cd back
./scripts/check-health.sh
```

### Tester les endpoints

```bash
cd back
./scripts/test-services.sh
```

### Logs Docker

```bash
# Tous les services
docker-compose logs -f

# Service spécifique
docker-compose logs -f user-microservice
```

---

## 🛑 Arrêt de l'application

### Backend
```bash
cd back
docker-compose down
```

### Frontend
Appuyer sur `Ctrl+C` dans le terminal où Expo tourne

---

## 📝 Scripts utiles

### Backend
- `make start` : Démarrer tous les services
- `make stop` : Arrêter tous les services
- `make logs` : Voir les logs
- `make clean` : Nettoyer les volumes Docker

### Frontend
- `npm start` : Démarrer Expo
- `npm run android` : Lancer sur Android
- `npm run ios` : Lancer sur iOS
- `npm run web` : Lancer dans le navigateur

---

## 👥 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📄 Licence

Ce projet est développé dans un cadre éducatif.

---

## 🆘 Support

En cas de problème :
1. Vérifier que tous les services Docker tournent : `docker-compose ps`
2. Consulter les logs : `docker-compose logs -f`
3. Vérifier la configuration Keycloak
4. S'assurer que l'IP dans **les deux fichiers `.env`** (backend ET frontend) est correcte et identique

---

## ⚡ Démarrage rapide (TL;DR)

```bash
# 1. Configurer le Backend
cd back
cp .env.example .env
# Éditer back/.env → API_HOST=VOTRE_IP

# 2. Démarrer le Backend
docker-compose up -d

# 3. Configurer le Frontend (dans un autre terminal)
cd front
cp .env.example .env
# Éditer front/.env → EXPO_PUBLIC_API_HOST=VOTRE_IP (la même IP !)

# 4. Démarrer le Frontend
npm start

# 5. Configurer Keycloak
# → http://localhost:8085 (admin/admin)
# → Realm: IWAPROJECT
# → Client: auth-service
# → Ajouter redirect URI Expo

# 6. Scanner le QR code et profiter ! 🎉
```