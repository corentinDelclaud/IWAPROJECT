# Guide Rapide - Test du Logging Service

## 🚀 Démarrage

```bash
cd /home/etienne/Documents/IWAPROJECT/back
docker-compose up -d
```

Attendre ~30 secondes que tous les services démarrent.

---

## 📝 Créer un utilisateur de test (génère un log)

### Méthode 1 : Via Webhook Keycloak

```bash
curl -X POST http://localhost:8081/api/webhooks/keycloak/user-registered \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-001",
    "username": "testuser001",
    "email": "test001@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Réponse attendue :**
```json
{
  "id": "test-user-001",
  "username": "testuser001",
  "email": "test001@example.com",
  ...
}
```

### Méthode 2 : Appeler l'endpoint stats (génère aussi un log)

```bash
curl http://localhost:8081/api/users/stats
```

**Réponse attendue :**
```json
{
  "totalActiveUsers": 1,
  "totalDeletedUsers": 0,
  "usersLastMonth": 1
}
```

---

## 🔍 Lire les logs

### 1. Voir tous les logs récents

```bash
curl -s "http://localhost:8087/api/logs/service/user-microservice?page=0&size=10" | python3 -m json.tool
```

### 2. Voir uniquement les 5 derniers logs (simple)

```bash
curl -s "http://localhost:8087/api/logs/service/user-microservice?size=5&sortDir=DESC" | python3 -m json.tool | grep -A5 '"message"'
```

### 3. Voir les statistiques des logs

```bash
curl -s "http://localhost:8087/api/logs/stats/user-microservice"
```

**Réponse :**
```json
{
  "ERROR": 0,
  "INFO": 2,
  "DEBUG": 0,
  "WARN": 0
}
```

### 4. Voir uniquement les logs d'erreur

```bash
curl -s "http://localhost:8087/api/logs/errors?size=10" | python3 -m json.tool
```

### 5. Voir les logs par niveau

```bash
# Logs INFO
curl -s "http://localhost:8087/api/logs/level/INFO?size=10" | python3 -m json.tool

# Logs ERROR
curl -s "http://localhost:8087/api/logs/level/ERROR?size=10" | python3 -m json.tool

# Logs WARN
curl -s "http://localhost:8087/api/logs/level/WARN?size=10" | python3 -m json.tool
```

---

## 📊 Workflow complet de test

```bash
# 1. Nettoyer les logs existants (optionnel)
docker exec iwa-postgres-logs psql -U postgres -d iwa_logs -c "TRUNCATE TABLE log_entries RESTART IDENTITY CASCADE;"

# 2. Créer un utilisateur (génère un log de création)
curl -X POST http://localhost:8081/api/webhooks/keycloak/user-registered \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "demo-user",
    "username": "demouser",
    "email": "demo@example.com",
    "firstName": "Demo",
    "lastName": "User"
  }'

# 3. Appeler stats (génère un log de stats)
curl http://localhost:8081/api/users/stats

# 4. Attendre 2 secondes que Kafka traite les messages
sleep 2

# 5. Lire les logs créés
curl -s "http://localhost:8087/api/logs/service/user-microservice?size=10&sortDir=DESC" | python3 -m json.tool

# 6. Voir les statistiques
curl -s "http://localhost:8087/api/logs/stats/user-microservice"
```

---

## 🧹 Commandes de nettoyage

### Nettoyer tous les logs de la base de données

```bash
docker exec iwa-postgres-logs psql -U postgres -d iwa_logs -c "TRUNCATE TABLE log_entries RESTART IDENTITY CASCADE;"
```

### Vérifier que les logs sont supprimés

```bash
curl -s "http://localhost:8087/api/logs/service/user-microservice?size=10"
# Devrait retourner: "content": [], "totalElements": 0
```

---

## 📦 Utiliser Postman

1. Importer le fichier : `/home/etienne/Documents/IWAPROJECT/back/logging-service/postman-collection.json`
2. Toutes les requêtes sont pré-configurées
3. Utiliser "Get Logs by Service - User Microservice" pour voir les logs

---

## 🐛 Dépannage

### Les logs n'apparaissent pas ?

```bash
# Vérifier que le user-microservice est démarré
docker ps | grep user-microservice

# Vérifier que le logging-service est démarré
docker ps | grep logging-service

# Vérifier les logs du logging-service
docker logs iwa-logging-service --tail 50

# Vérifier Kafka
docker logs iwa-kafka --tail 30
```

### Redémarrer un service spécifique

```bash
cd /home/etienne/Documents/IWAPROJECT/back
docker-compose restart user-microservice
docker-compose restart logging-service
```

---

## 📍 Endpoints disponibles

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/logs/health` | GET | Santé du service |
| `/api/logs/service/{serviceName}` | GET | Logs par service |
| `/api/logs/level/{level}` | GET | Logs par niveau (INFO, ERROR, etc.) |
| `/api/logs/errors` | GET | Tous les ERROR et WARN |
| `/api/logs/stats/{serviceName}` | GET | Statistiques des logs |
| `/api/logs/time-range` | GET | Logs dans une période |
| `/api/logs/correlation/{id}` | GET | Logs par correlation ID |

---

## ✨ Exemples de logs générés

### Log de création d'utilisateur
```json
{
  "serviceName": "user-microservice",
  "logLevel": "INFO",
  "message": "New user created - ID: test-user-001, Username: testuser001, Email: test001@example.com",
  "timestamp": "2025-11-28T14:29:31.762718",
  "userId": "test-user-001"
}
```

### Log de statistiques
```json
{
  "serviceName": "user-microservice",
  "logLevel": "INFO",
  "message": "User statistics requested - Total: 5, Deleted: 0, Recent: 5",
  "timestamp": "2025-11-28T14:28:54.425274"
}
```
