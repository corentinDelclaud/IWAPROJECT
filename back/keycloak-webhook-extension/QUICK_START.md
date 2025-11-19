# Quick Start Guide - Keycloak User Synchronization

## Overview
This guide will help you set up automatic user synchronization between Keycloak and the user-microservice.

## Prerequisites
- Docker and Docker Compose running
- All services started with `docker-compose up -d`
- Maven installed (for building the extension)

## Step-by-Step Setup

### Step 1: Build and Deploy the Extension

```bash
cd /home/etienne/Documents/IWAPROJECT/back/keycloak-webhook-extension
./build-and-deploy.sh
```

Expected output:
```
Building Keycloak Webhook Extension
Build successful! JAR created at: target/keycloak-webhook-extension.jar
Copying JAR to Keycloak container...
Extension deployed successfully!
```

### Step 2: Restart Keycloak

```bash
docker restart iwa-keycloak
```

Wait 30-60 seconds for Keycloak to fully restart. Monitor the logs:

```bash
docker logs -f iwa-keycloak
```

Look for this line (press Ctrl+C when you see it):
```
WebhookEventListenerProviderFactory initialized with webhookUrl: http://user-microservice:8081
```

### Step 3: Enable the Event Listener in Keycloak

1. Open Keycloak Admin Console: http://localhost:8085/admin
2. Login:
   - Username: `admin`
   - Password: `admin`
3. Select the realm: **IWA_NextLevel** (dropdown at top left)
4. Click **Realm Settings** in the left menu
5. Click the **Events** tab
6. Scroll down to **Event Listeners**
7. Click in the Event Listeners field
8. Select or type: `user-webhook-sync`
9. Click **Save** at the bottom

### Step 4: Verify Setup

Check that both databases are empty:

```bash
# Check user-microservice database
docker exec iwa-postgres-users psql -U postgres -d iwa_users -c "SELECT id, username, email FROM users;"

# Check Keycloak database
docker exec iwa-postgres-keycloak psql -U keycloak -d keycloak -c "SELECT id, username, email FROM user_entity ORDER BY username;"
```

### Step 5: Test the Synchronization

#### Option A: Create a User via Keycloak Admin UI

1. In Keycloak Admin Console: http://localhost:8085/admin
2. Click **Users** in the left menu
3. Click **Create new user**
4. Fill in the form:
   - Username: `synctest`
   - Email: `synctest@example.com`
   - First name: `Sync`
   - Last name: `Test`
5. Click **Create**
6. Go to the **Credentials** tab
7. Click **Set password**
8. Enter a password (e.g., `password123`)
9. Turn OFF "Temporary"
10. Click **Save**

#### Option B: Create a User via API

```bash
# First, get an admin token
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8085/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

# Create a user
curl -X POST http://localhost:8085/admin/realms/IWA_NextLevel/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apitest",
    "email": "apitest@example.com",
    "firstName": "API",
    "lastName": "Test",
    "enabled": true,
    "credentials": [{
      "type": "password",
      "value": "password123",
      "temporary": false
    }]
  }'
```

### Step 6: Verify Synchronization

```bash
# Check user-microservice database (should now have users!)
docker exec iwa-postgres-users psql -U postgres -d iwa_users -c "SELECT id, username, email, first_name, last_name FROM users ORDER BY username;"
```

Expected result:
```
                  id                  | username | email               | first_name | last_name
--------------------------------------+----------+---------------------+------------+-----------
 <keycloak-uuid>                      | synctest | synctest@example.com| Sync       | Test
```

### Step 7: Test Your JWT Token

Now create a token and test the profile endpoint:

```bash
# Get a token for your new user
TOKEN=$(curl -s -X POST http://localhost:8085/realms/IWA_NextLevel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=synctest" \
  -d "password=password123" \
  -d "grant_type=password" \
  -d "client_id=iwa-client" | jq -r '.access_token')

# Test the profile endpoint
curl -X GET http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json"
```

## Troubleshooting

### Extension Not Loading

Check Keycloak logs:
```bash
docker logs iwa-keycloak 2>&1 | grep -i webhook
```

If you don't see "WebhookEventListenerProviderFactory initialized", rebuild and redeploy.

### Webhook Not Being Called

1. Check that the event listener is enabled in Keycloak (Step 3)
2. Check user-microservice logs:
   ```bash
   docker logs iwa-user-microservice | grep -i webhook
   ```

### User Not Syncing

1. Check if the webhook endpoint is accessible:
   ```bash
   curl http://localhost:8081/api/webhooks/health
   ```
   Should return: "Webhook endpoint is healthy"

2. Test the webhook manually:
   ```bash
   curl -X POST http://localhost:8081/api/webhooks/users \
     -H "Content-Type: application/json" \
     -d '{
       "id": "test-123",
       "username": "manualtest",
       "email": "manual@test.com",
       "firstName": "Manual",
       "lastName": "Test"
     }'
   ```

3. Check the database:
   ```bash
   docker exec iwa-postgres-users psql -U postgres -d iwa_users -c "SELECT * FROM users WHERE username = 'manualtest';"
   ```

## Sync Existing Users

If you have existing users in Keycloak that need to be synced:

1. Go to each user in Keycloak Admin Console
2. Make a small change (e.g., add/update a field)
3. Click Save
4. This will trigger an UPDATE event and sync the user

## Next Steps

- Set up a password for existing users (admin, testuser)
- Test the authentication flow
- Create additional users as needed
- Verify all microservice endpoints work with JWT tokens

## Security Considerations

In production, you should:
- Add webhook authentication (shared secret)
- Restrict webhook endpoints to internal network only
- Enable HTTPS for all communication
- Use proper Keycloak client credentials
