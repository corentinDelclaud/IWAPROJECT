# Guide Rapide - Configuration et Tests

## 🚀 Démarrage Rapide

### 1. Redémarrer les services avec les corrections

```bash
cd /home/etienne/Documents/IWAPROJECT/back

# Arrêter les services existants
docker-compose -f docker-compose.production.yml down

# Redémarrer avec les nouvelles configurations
docker-compose -f docker-compose.production.yml up --build -d

# Attendre que tous les services démarrent (30-60 secondes)
sleep 30

# Vérifier le statut
./test-services.sh
```

## 🔍 Commandes de Test des Microservices

### Test rapide de tous les services
```bash
cd /home/etienne/Documents/IWAPROJECT/back
./test-services.sh
```

### Tests individuels

```bash
# 1. Vérifier tous les conteneurs
docker ps --filter "name=iwa-"

# 2. API Gateway (Port 8080)
curl http://localhost:8080

# 3. Keycloak Admin (Port 9090)
curl http://localhost:9090/realms/IWA_NextLevel

# 4. PostgreSQL
docker exec iwa-user-postgres pg_isready -U postgres

# 5. User Service via Gateway
curl http://localhost:8080/users/actuator/health

# 6. Auth Service via Gateway
curl http://localhost:8080/auth/actuator/health
```

## 🍪 Solutions pour le problème de Cookies Keycloak

### Changements appliqués dans docker-compose.production.yml

✅ **Ajout de la configuration des cookies** dans la section Keycloak:
- `--spi-login-protocol-openid-connect-legacy-logout-redirect-uri=true`
- `KC_SPI_STICKY_SESSION_ENCODER_INFINISPAN_SHOULD_ATTACH_ROUTE: "false"`

### Configuration Client Keycloak (À faire manuellement)

1. **Accéder à la console admin Keycloak**
   - URL: http://localhost:9090
   - Username: `admin`
   - Password: `admin`

2. **Aller dans le realm IWA_NextLevel**
   - Cliquez sur le menu déroulant en haut à gauche
   - Sélectionnez "IWA_NextLevel"

3. **Configurer le client `user-microservice`**
   - Menu: **Clients** → **user-microservice**
   - Onglet **Settings**:

   **Valid redirect URIs** (ajouter si manquant):
   ```
   http://localhost:19000/*
   http://localhost:*/*
   exp://*
   exp://localhost:19000/--/*
   exp://192.168.*.*:19000/--/*
   ```

   **Valid post logout redirect URIs**:
   ```
   http://localhost:19000/*
   http://localhost:*/*
   +
   ```

   **Web origins**:
   ```
   http://localhost:19000
   http://localhost:*
   +
   ```

4. **Autres paramètres importants**:
   - ✅ **Public Client**: ON
   - ✅ **Standard Flow Enabled**: ON
   - ✅ **Direct Access Grants Enabled**: ON
   - ✅ **Implicit Flow Enabled**: OFF (sécurité)

5. **Sauvegarder** en bas de page

### Configuration Frontend

Mettre à jour `/home/etienne/Documents/IWAPROJECT/front/config/keycloak.ts`:

```typescript
export const keycloakConfig = {
  url: 'http://localhost:9090',  // URL Keycloak
  realm: 'IWA_NextLevel',
  clientId: 'user-microservice',  // ⚠️ Nom correct du client
};
```

## 🧪 Tests après corrections

### Test 1: Vérifier Keycloak
```bash
# Le realm doit être accessible
curl http://localhost:9090/realms/IWA_NextLevel

# Doit retourner les configurations du realm
```

### Test 2: Tester l'inscription (depuis le navigateur)

1. Ouvrir: http://localhost:9090/realms/IWA_NextLevel/protocol/openid-connect/auth?client_id=user-microservice&redirect_uri=http://localhost:19000&response_type=code&scope=openid

2. Cliquer sur "Register"

3. Remplir le formulaire

4. ✅ Si ça marche: vous serez redirigé vers localhost:19000

### Test 3: Tester depuis votre application

```bash
# Depuis votre frontend sur localhost:19000
# Le bouton Register/Login devrait fonctionner
```

## 🐛 Debugging si le problème persiste

### 1. Vérifier les logs Keycloak
```bash
docker logs iwa-keycloak --tail 50
docker logs iwa-keycloak 2>&1 | grep -i "cookie\|error"
```

### 2. Vérifier les cookies dans le navigateur

1. Ouvrir DevTools (F12)
2. Onglet **Application** → **Cookies**
3. Vérifier `http://localhost:9090`
4. Chercher: `AUTH_SESSION_ID`, `KC_RESTART`, `KEYCLOAK_SESSION`

### 3. Tester avec cURL complet
```bash
# Récupérer la page de login avec cookies
curl -v -c cookies.txt http://localhost:9090/realms/IWA_NextLevel/protocol/openid-connect/auth?client_id=user-microservice&redirect_uri=http://localhost:19000&response_type=code&scope=openid

# Vérifier les cookies
cat cookies.txt
```

### 4. Options de navigateur

Si vous utilisez **Chrome/Edge**:
- Allez dans `chrome://settings/cookies`
- Autorisez temporairement tous les cookies

Si vous utilisez **Firefox**:
- Allez dans `about:preferences#privacy`
- Paramètres des cookies: "Standard" ou "Personnalisé"

### 5. Désactiver les extensions de navigateur
- AdBlock, Privacy Badger, etc. peuvent bloquer les cookies

## 📊 Monitoring continu

### Voir tous les logs en temps réel
```bash
docker-compose -f docker-compose.production.yml logs -f
```

### Voir les logs d'un service spécifique
```bash
docker-compose -f docker-compose.production.yml logs -f keycloak
docker-compose -f docker-compose.production.yml logs -f api-gateway
docker-compose -f docker-compose.production.yml logs -f user-service
```

### Redémarrer un service spécifique
```bash
docker-compose -f docker-compose.production.yml restart keycloak
docker-compose -f docker-compose.production.yml restart api-gateway
```

## 🔥 En cas de problème persistant

### Solution radicale: Reset complet

```bash
# ⚠️ ATTENTION: Supprime toutes les données
cd /home/etienne/Documents/IWAPROJECT/back

# Arrêter et supprimer tout
docker-compose -f docker-compose.production.yml down -v

# Nettoyer les images
docker-compose -f docker-compose.production.yml build --no-cache

# Redémarrer
docker-compose -f docker-compose.production.yml up -d

# Attendre que tout démarre
sleep 60

# Tester
./test-services.sh
```

## 📝 Checklist de vérification

- [ ] Tous les conteneurs Docker sont en cours d'exécution
- [ ] Keycloak est accessible sur http://localhost:9090
- [ ] Le realm IWA_NextLevel existe
- [ ] Le client `user-microservice` est configuré avec les bonnes redirections
- [ ] Le frontend utilise le bon `clientId`: `user-microservice`
- [ ] Les cookies sont autorisés dans le navigateur
- [ ] Pas d'extensions qui bloquent les cookies
- [ ] Les logs Keycloak ne montrent pas d'erreurs

## 🎯 URLs importantes

- **Console Admin Keycloak**: http://localhost:9090 (admin/admin)
- **API Gateway**: http://localhost:8080
- **Frontend**: http://localhost:19000
- **Realm Config**: http://localhost:9090/realms/IWA_NextLevel/.well-known/openid-configuration

## 💡 Alternative: Utiliser l'API Gateway comme proxy

Si le problème persiste, vous pouvez faire passer Keycloak par l'API Gateway:

1. Modifier `api-gateway/src/main/resources/application.yml` pour ajouter une route Keycloak
2. Utiliser `http://localhost:8080/keycloak` dans le frontend au lieu de `http://localhost:9090`

Cela résoudra définitivement les problèmes de CORS et de cookies car tout sera sur le même port.

Voulez-vous que je vous aide à mettre en place cette solution ?
