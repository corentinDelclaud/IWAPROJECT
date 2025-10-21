# 🚀 User Microservice - Quick Start Guide

Complete guide to start and test the User Microservice with Keycloak authentication.

---

## 📋 Prerequisites

- Docker and Docker Compose installed
- Java 21 installed
- Maven installed (or use `./mvnw`)
- Terminal access

---

## 🔧 Step 1: Start Infrastructure Services

### Start PostgreSQL

```bash
cd back/user-microservice
docker compose -f docker-compose.yml up -d postgres
```

**Verify it's running:**
```bash
docker ps | grep iwa-user-postgres
```

### Start Keycloak

```bash
docker compose -f docker-compose.keycloak.yml up -d keycloak
```

**Wait for Keycloak to be ready (takes ~30 seconds):**
```bash
# This will retry until Keycloak responds
for i in {1..30}; do 
  curl -s http://localhost:8080/realms/IWA_NextLevel/.well-known/openid-configuration >/dev/null && echo "✅ Keycloak is ready!" && break
  echo "⏳ Waiting for Keycloak... ($i/30)"
  sleep 2
done
```

**Verify services are running:**
```bash
docker ps
# You should see:
# - iwa-user-postgres (port 5432)
# - iwa-keycloak (port 8080)
```

---

## 🔑 Step 2: Configure Keycloak (First Time Only)

### Access Keycloak Admin Console

Open: **http://localhost:8080**

- Username: `admin`
- Password: `admin`

### Verify Realm

The realm `IWA_NextLevel` should already be imported. You should see it in the realm dropdown (top left).

### Create Client (if not exists)

1. Select realm: `IWA_NextLevel`
2. Go to: **Clients** → **Create client**
3. Fill in:
   - **Client ID**: `user-microservice`
   - **Name**: User Microservice
   - Click **Next**
4. **Capability config**:
   - ✅ **Client authentication**: OFF (public client)
   - ✅ **Authorization**: OFF
   - ✅ **Authentication flow**: ✅ Standard flow, ✅ Direct access grants
   - Click **Next**
5. **Login settings**:
   - **Root URL**: `http://localhost:8081`
   - **Valid redirect URIs**: `http://localhost:8081/*`
   - **Web origins**: `+`
   - Click **Save**

### Create Test User (if not exists)

1. Go to: **Users** → **Create new user**
2. Fill in:
   - **Username**: `testuser`
   - **Email**: `test@example.com`
   - **First name**: `Test`
   - **Last name**: `User`
   - ✅ **Email verified**: ON
   - Click **Create**
3. Go to **Credentials** tab:
   - Click **Set password**
   - **Password**: `user`
   - **Password confirmation**: `user`
   - ✅ **Temporary**: OFF
   - Click **Save**

---

## 🗄️ Step 3: Initialize Database (First Time Only)

### Create the users table

```bash
docker exec -i iwa-user-postgres psql -U postgres -d iwa_users <<'SQL'
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
SQL
```

### Seed test user

```bash
# Get the user ID from JWT
ACCESS_TOKEN=$(curl -s \
  -d 'client_id=user-microservice' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=user' \
  http://localhost:8080/realms/IWA_NextLevel/protocol/openid-connect/token | jq -r .access_token)

# Extract user ID (sub claim)
USER_ID=$(python3 -c "import sys, json, base64; token = sys.argv[1]; payload = token.split('.')[1] + '=='; print(json.loads(base64.urlsafe_b64decode(payload).decode())['sub'])" "$ACCESS_TOKEN")

echo "User ID: $USER_ID"

# Insert user into database
docker exec -i iwa-user-postgres psql -U postgres -d iwa_users -c \
  "INSERT INTO users (id, username, email, first_name, last_name, created_at) 
   VALUES ('$USER_ID', 'testuser', 'test@example.com', 'Test', 'User', NOW()) 
   ON CONFLICT (id) DO NOTHING;"
```

---

## ▶️ Step 4: Start the Microservice

### From the command line

```bash
cd back/user-microservice
./mvnw spring-boot:run
```

Or if Maven is installed globally:
```bash
mvn spring-boot:run
```

### From your IDE

Run the main class: `iwaproject.user_microservice.UserMicroserviceApplication`

### Verify it's running

```bash
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP"}
```

---

## 🧪 Step 5: Test the API

### Get an Access Token

```bash
ACCESS_TOKEN=$(curl -s \
  -d 'client_id=user-microservice' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=user' \
  http://localhost:8080/realms/IWA_NextLevel/protocol/openid-connect/token | jq -r .access_token)

echo "Token: $ACCESS_TOKEN"
```

### Test Protected Endpoint - Get Your Profile

```bash
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8081/api/users/profile | jq
```

**Expected response:**
```json
{
  "id": "f6c967f7-94ba-4366-ad8b-5e69e39ce0e8",
  "username": "testuser",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "createdAt": "2025-10-20T17:30:31.005507",
  "updatedAt": null
}
```

### Test Protected Endpoint - Update Your Profile

```bash
curl -X PUT \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "firstName": "Updated",
    "lastName": "Name"
  }' \
  http://localhost:8081/api/users/profile | jq
```

### Test Protected Endpoint - Delete Your Profile

```bash
curl -X DELETE \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8081/api/users/profile
# Expected: 204 No Content
```

### Test Public Endpoint - Get Any User's Public Profile

```bash
# Replace with actual user ID
curl http://localhost:8081/api/users/f6c967f7-94ba-4366-ad8b-5e69e39ce0e8 | jq
```

**Expected response (no email):**
```json
{
  "id": "f6c967f7-94ba-4366-ad8b-5e69e39ce0e8",
  "username": "testuser",
  "firstName": "Test",
  "lastName": "User"
}
```

---

## 📚 Step 6: Use Swagger UI (Interactive Testing)

### Open Swagger

Open in browser: **http://localhost:8081/swagger-ui.html**

### Authenticate

1. Click **Authorize** button (top right)
2. Paste your access token (from Step 5)
3. Click **Authorize**
4. Click **Close**

### Try Endpoints

Now you can test all endpoints interactively:
- Click on an endpoint
- Click **Try it out**
- Fill in parameters (if needed)
- Click **Execute**

---

## 🛑 Stop Services

### Stop the microservice

Press `Ctrl+C` in the terminal where it's running.

### Stop Docker containers

```bash
cd back/user-microservice

# Stop Keycloak
docker compose -f docker-compose.keycloak.yml down

# Stop PostgreSQL
docker compose -f docker-compose.yml down
```

### Keep data (recommended)

The above commands preserve your database data.

### Clean everything (if needed)

```bash
# Remove containers and volumes (deletes all data!)
docker compose -f docker-compose.keycloak.yml down -v
docker compose -f docker-compose.yml down -v
```

---

## 🔄 Restart Services Later

### Quick restart (everything already configured)

```bash
# 1. Start services
cd back/user-microservice
docker compose -f docker-compose.yml up -d postgres
docker compose -f docker-compose.keycloak.yml up -d keycloak

# 2. Wait for Keycloak (30 seconds)
sleep 30

# 3. Start microservice
./mvnw spring-boot:run
```

### If you cleaned everything

Follow the full guide from Step 1.

---

## 📊 Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Keycloak Admin | http://localhost:8080 | admin / admin |
| User Microservice | http://localhost:8081 | - |
| Swagger UI | http://localhost:8081/swagger-ui.html | - |
| Health Check | http://localhost:8081/actuator/health | - |
| PostgreSQL | localhost:5432 | postgres / postgres |

---

## 🐛 Troubleshooting

### "Connection refused" to Keycloak

**Problem**: Microservice can't reach Keycloak.

**Solution**: Wait 30-60 seconds for Keycloak to fully start.

```bash
curl http://localhost:8080/realms/IWA_NextLevel/.well-known/openid-configuration
```

### "401 Unauthorized" on protected endpoints

**Problem**: Token is invalid or expired.

**Solution**: Get a fresh token (tokens expire after 5 minutes by default).

```bash
ACCESS_TOKEN=$(curl -s \
  -d 'client_id=user-microservice' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=user' \
  http://localhost:8080/realms/IWA_NextLevel/protocol/openid-connect/token | jq -r .access_token)
```

### "404 User not found" on profile endpoint

**Problem**: User exists in Keycloak but not in the database.

**Solution**: Re-run Step 3 to seed the user.

### Port already in use

**Problem**: 8080, 8081, or 5432 is already used.

**Solution**: 
```bash
# Find what's using the port
lsof -i :8080
lsof -i :8081
lsof -i :5432

# Stop the conflicting service or change ports in application.properties
```

### Kafka warnings in logs

**Problem**: Kafka is not running but the microservice tries to connect.

**Solution**: This is normal and doesn't affect functionality. Kafka is disabled by default (`spring.kafka.enabled=false`).

---

## 📝 Notes

- **Kafka**: Currently disabled. Enable with `spring.kafka.enabled=true` when ready.
- **Database**: Data persists in Docker volume `postgres_data`.
- **Tokens**: JWT tokens expire after 5 minutes. Get a fresh token for each test session.
- **Keycloak**: Realm `IWA_NextLevel` is auto-imported on first start.

---

## 🎯 All-in-One Test Script

Save this as `test-api.sh` in `back/user-microservice/`:

```bash
#!/bin/bash
set -e

echo "🔑 Getting access token..."
ACCESS_TOKEN=$(curl -s \
  -d 'client_id=user-microservice' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=user' \
  http://localhost:8080/realms/IWA_NextLevel/protocol/openid-connect/token | jq -r .access_token)

echo "✅ Token obtained!"
echo ""

echo "📋 GET /api/users/profile"
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8081/api/users/profile | jq
echo ""

echo "✏️ PUT /api/users/profile"
curl -X PUT \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "firstName": "Updated",
    "lastName": "Test"
  }' \
  http://localhost:8081/api/users/profile | jq
echo ""

echo "🌍 GET /api/users/{userId} (public)"
USER_ID=$(python3 -c "import sys, json, base64; token = sys.argv[1]; payload = token.split('.')[1] + '=='; print(json.loads(base64.urlsafe_b64decode(payload).decode())['sub'])" "$ACCESS_TOKEN")
curl http://localhost:8081/api/users/$USER_ID | jq
echo ""

echo "✅ All tests completed!"
```

Make it executable:
```bash
chmod +x test-api.sh
./test-api.sh
```

---

**Last Updated**: October 20, 2025  
**Version**: 1.0  
**Status**: ✅ Ready for Development
