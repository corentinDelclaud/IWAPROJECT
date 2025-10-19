# Keycloak Realm Configuration Quick Reference

## Realm: iwa-project

### Clients Configuration

#### 1. api-gateway (Backend - Confidential Client)

```
Client ID: api-gateway
Client Protocol: openid-connect
Access Type: confidential
Standard Flow Enabled: ON
Direct Access Grants Enabled: ON
Valid Redirect URIs: http://localhost:8080/*
Web Origins: http://localhost:8080
```

**Client Secret:** Available in Credentials tab (save this!)

#### 2. mobile-app (Frontend - Public Client)

```
Client ID: mobile-app
Client Protocol: openid-connect
Access Type: public
Standard Flow Enabled: ON
Direct Access Grants Enabled: ON
Valid Redirect URIs:
  - exp://localhost:8081
  - exp://*
  - myapp://*
  - http://localhost:8081
Web Origins: *
PKCE Code Challenge Method: S256
```

---

### Roles

| Role Name | Description |
|-----------|-------------|
| user      | Standard user with basic access |
| admin     | Administrator with full access |
| seller    | Can list and manage products |
| buyer     | Can browse and purchase products |

---

### Test Users

| Username    | Password  | Email            | Roles         |
|-------------|-----------|------------------|---------------|
| testadmin   | Test123!  | <admin@test.com>   | admin, user   |
| testseller  | Test123!  | <seller@test.com>  | seller, user  |
| testbuyer   | Test123!  | <buyer@test.com>   | buyer, user   |

---

### Token Endpoints

```bash
# Well-known configuration
http://localhost:8180/realms/iwa-project/.well-known/openid-configuration

# Token endpoint
http://localhost:8180/realms/iwa-project/protocol/openid-connect/token

# Authorization endpoint
http://localhost:8180/realms/iwa-project/protocol/openid-connect/auth

# UserInfo endpoint
http://localhost:8180/realms/iwa-project/protocol/openid-connect/userinfo

# Logout endpoint
http://localhost:8180/realms/iwa-project/protocol/openid-connect/logout

# JWKS (public keys for token verification)
http://localhost:8180/realms/iwa-project/protocol/openid-connect/certs
```

---

### Example Token Request (Password Grant - for testing only)

```bash
curl -X POST 'http://localhost:8180/realms/iwa-project/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=mobile-app' \
  --data-urlencode 'username=testadmin' \
  --data-urlencode 'password=Test123!' \
  --data-urlencode 'grant_type=password'
```

---

### Example Authorization Code Flow (for production)

1. **Authorization Request:**

```
http://localhost:8180/realms/iwa-project/protocol/openid-connect/auth?
  client_id=mobile-app
  &redirect_uri=exp://localhost:8081
  &response_type=code
  &scope=openid profile email
  &code_challenge=<PKCE_CHALLENGE>
  &code_challenge_method=S256
```

2. **Token Exchange:**

```bash
curl -X POST 'http://localhost:8180/realms/iwa-project/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=mobile-app' \
  --data-urlencode 'grant_type=authorization_code' \
  --data-urlencode 'code=<AUTHORIZATION_CODE>' \
  --data-urlencode 'redirect_uri=exp://localhost:8081' \
  --data-urlencode 'code_verifier=<PKCE_VERIFIER>'
```

---

### JWT Token Claims

The access token will contain:

```json
{
  "exp": 1234567890,
  "iat": 1234567890,
  "jti": "token-id",
  "iss": "http://localhost:8180/realms/iwa-project",
  "aud": "account",
  "sub": "user-uuid",
  "typ": "Bearer",
  "azp": "mobile-app",
  "session_state": "session-id",
  "realm_access": {
    "roles": ["user", "admin"]
  },
  "resource_access": {
    "account": {
      "roles": ["manage-account", "view-profile"]
    }
  },
  "scope": "openid profile email",
  "email_verified": true,
  "preferred_username": "testadmin",
  "email": "admin@test.com"
}
```

---

### Refresh Token Request

```bash
curl -X POST 'http://localhost:8180/realms/iwa-project/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=mobile-app' \
  --data-urlencode 'grant_type=refresh_token' \
  --data-urlencode 'refresh_token=<REFRESH_TOKEN>'
```

---

### Logout Request

```bash
curl -X POST 'http://localhost:8180/realms/iwa-project/protocol/openid-connect/logout' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=mobile-app' \
  --data-urlencode 'refresh_token=<REFRESH_TOKEN>'
```

---

## Configuration for Backend (Spring Boot)

Add to `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/iwa-project
          jwk-set-uri: http://localhost:8180/realms/iwa-project/protocol/openid-connect/certs
      client:
        registration:
          keycloak:
            client-id: api-gateway
            client-secret: <YOUR_CLIENT_SECRET_FROM_KEYCLOAK>
            authorization-grant-type: authorization_code
            scope: openid,profile,email
        provider:
          keycloak:
            issuer-uri: http://localhost:8180/realms/iwa-project
```

---

## Configuration for Frontend (React Native)

```typescript
const keycloakConfig = {
  issuer: 'http://localhost:8180/realms/iwa-project',
  clientId: 'mobile-app',
  redirectUri: 'exp://localhost:8081',
  scopes: ['openid', 'profile', 'email'],
};
```

---

## Quick Commands

### Start Keycloak

```bash
docker-compose up -d
```

### View Logs

```bash
docker-compose logs -f keycloak
```

### Stop Keycloak

```bash
docker-compose down
```

### Reset Database (⚠️ Deletes all data)

```bash
docker-compose down -v
docker-compose up -d
```

### Export Realm Configuration

```bash
docker exec -it iwa-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/export \
  --realm iwa-project

docker cp iwa-keycloak:/opt/keycloak/data/export/iwa-project-realm.json ./keycloak-backup.json
```
