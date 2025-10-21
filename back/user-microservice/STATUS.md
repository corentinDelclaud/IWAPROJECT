# ✅ User Microservice - DÉMARRÉ AVEC SUCCÈS !

## 🎉 État actuel

Le microservice User est **opérationnel** et fonctionne correctement !

### ✅ Services actifs

1. **PostgreSQL** (Docker) - Port 5432
   - Base de données : `iwa_users`
   - Username : `postgres`
   - Password : `postgres`

2. **Kafka + Zookeeper** (Docker) - Ports 9092 / 2181
   - Bootstrap servers : `localhost:9092`
   - Topics : `user-events`, `keycloak-events`

3. **Kafka UI** (Docker) - Port 8090
   - URL : http://localhost:8090

4. **User Microservice** - Port 8081
   - API : http://localhost:8081
   - Swagger : http://localhost:8081/swagger-ui.html
   - Actuator Health : http://localhost:8081/actuator/health

---

## 🔧 Configuration appliquée

### Problem résolu : Conflit de port PostgreSQL

**Problème initial :** Port 5432 déjà utilisé par PostgreSQL local  
**Solution :** Docker PostgreSQL sur port **5432**

**Fichiers modifiés :**
- `docker-compose.yml` : Port PostgreSQL → 5432
- `application.properties` : JDBC URL → `localhost:5432`

---

## 📚 Endpoints disponibles

### API REST

| Méthode | URL | Auth | Description |
|---------|-----|------|-------------|
| GET | `/api/users/profile` | JWT ✅ | Mon profil complet |
| PUT | `/api/users/profile` | JWT ✅ | Mettre à jour mon profil |
| DELETE | `/api/users/profile` | JWT ✅ | Supprimer mon compte |
| GET | `/api/users/{userId}` | Public ❌ | Profil public d'un user |

### Documentation & Monitoring

| URL | Description |
|-----|-------------|
| http://localhost:8081/swagger-ui.html | Documentation API interactive |
| http://localhost:8081/api-docs | OpenAPI JSON |
| http://localhost:8081/actuator/health | Health check |
| http://localhost:8090 | Kafka UI |

---

## 🧪 Tester l'API

### 1. Health Check (sans authentification)

```bash
curl http://localhost:8081/actuator/health
```

**Résultat attendu :**
```json
{"status":"UP"}
```

### 2. Voir un profil public (sans authentification)

```bash
curl http://localhost:8081/api/users/test-user-id
```

**Résultat attendu :** 404 (normal, aucun user créé pour l'instant)

### 3. Avec JWT Token (nécessite Keycloak)

**Obtenir un token :**
```bash
curl -X POST "http://localhost:8080/realms/IWA_NextLevel/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=your-client" \
  -d "username=your-user" \
  -d "password=your-password"
```

**Utiliser le token :**
```bash
curl -X GET "http://localhost:8081/api/users/profile" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🛑 Arrêter les services

### Arrêter le microservice
Dans le terminal Spring Boot : `Ctrl+C`

### Arrêter Docker (PostgreSQL, Kafka, etc.)
```bash
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
docker-compose down
```

### Arrêter et supprimer les données
```bash
docker-compose down -v  # ⚠️ Supprime les données PostgreSQL
```

---

## 🚀 Redémarrer les services

### Démarrer les dépendances (PostgreSQL + Kafka)
```bash
cd /home/etienne/Documents/IWAPROJECT/back/user-microservice
docker-compose up -d
```

### Démarrer le microservice
```bash
./mvnw spring-boot:run
```

Ou avec chemin absolu :
```bash
/home/etienne/Documents/IWAPROJECT/back/user-microservice/mvnw \
  -f /home/etienne/Documents/IWAPROJECT/back/user-microservice/pom.xml \
  spring-boot:run
```

---

## 📊 Vérifier les logs

### Logs du microservice
Visibles directement dans le terminal Spring Boot

### Logs Docker
```bash
# Tous les conteneurs
docker-compose logs -f

# PostgreSQL seulement
docker-compose logs -f postgres

# Kafka seulement
docker-compose logs -f kafka
```

---

## 🔍 Accès aux bases de données

### PostgreSQL (via Docker)
```bash
docker exec -it iwa-user-postgres psql -U postgres -d iwa_users
```

Commandes psql utiles :
```sql
\dt              -- Lister les tables
\d users         -- Structure de la table users
SELECT * FROM users;  -- Voir tous les users
\q               -- Quitter
```

### Kafka UI
Ouvrir http://localhost:8090 dans le navigateur

---

## ✅ Checklist de démarrage

- [x] Docker Compose lancé (`docker-compose up -d`)
- [x] PostgreSQL accessible sur port 5432
- [x] Kafka accessible sur port 9092
- [x] Microservice démarré sur port 8081
- [x] Swagger UI accessible : http://localhost:8081/swagger-ui.html
- [x] Health check OK : http://localhost:8081/actuator/health

---

## 📝 Notes importantes

1. **PostgreSQL Local vs Docker**  
   Votre PostgreSQL local (port 5432) reste intact et utilisable  
   Le Docker PostgreSQL utilise le port 5432

2. **Keycloak requis pour l'authentification**  
   Les endpoints protégés nécessitent un JWT valide de Keycloak  
   Configuration : `http://localhost:8080/realms/IWA_NextLevel`

3. **Création automatique des users**  
   Quand un user s'inscrit dans Keycloak, un profil est créé automatiquement  
   via le Kafka Consumer (topic : `keycloak-events`)

4. **Soft Delete**  
   Les users supprimés ne sont jamais effacés physiquement  
   Ils sont marqués avec `deleted_at`

---

## 🎯 Prochaines étapes

1. ✅ ~~Configurer PostgreSQL~~ - **FAIT**
2. ✅ ~~Démarrer le microservice~~ - **FAIT**
3. ⏳ Configurer Keycloak (realm + client + event listener)
4. ⏳ Tester les endpoints avec un JWT valide
5. ⏳ Configurer Spring Cloud Gateway
6. ⏳ Développer les autres microservices

---

**🎉 Félicitations ! Votre User Microservice est opérationnel ! 🎉**
