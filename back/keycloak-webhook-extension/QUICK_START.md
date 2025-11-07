# 🎯 INSTANT USER SYNC - Quick Setup

Users will be **automatically added to PostgreSQL** when they register in Keycloak!

## 🚀 Setup (5 Minutes)

### Step 1: Build the Keycloak Extension

```bash
cd ./IWAPROJECT/back/keycloak-webhook-extension
mvn clean package
```

This creates the extension JAR file.

### Step 2: Restart Keycloak

```bash
cd ./IWAPROJECT/back/user-microservice

# Stop Keycloak
docker compose -f docker-compose.keycloak.yml down

# Start with the extension
docker compose -f docker-compose.keycloak.yml up -d

# Wait for it to start (30 seconds)
sleep 30
```

### Step 3: Verify Extension Loaded

```bash
docker logs iwa-keycloak | grep -i "webhook"
```

You should see:
```
🚀 Webhook Event Listener initialized
   Webhook URL: http://host.docker.internal:8081/api/webhooks/keycloak/user-registered
```

### Step 4: Enable in Keycloak Admin Console

1. Go to **http://localhost:8080/admin**
2. Login: `admin` / `admin`
3. Select realm: **IWA_NextLevel** (top-left dropdown)
4. Go to: **Realm Settings** → **Events** tab
5. Scroll down to **Event Listeners** section
6. Click in the field and select **webhook-event-listener**
7. Click **Save**

### Step 5: Test It!

**Make sure your microservice is running:**
```bash
cd ./IWAPROJECT/back/user-microservice
mvn spring-boot:run  # If not already running
```

**Create a test user:**
1. Go to **http://localhost:8080/admin**
2. **Users** → **Create new user**
3. Username: `instanttest`
4. Email: `instant@test.com`
5. First name: `Instant`
6. Last name: `Test`
7. Click **Create**
8. Go to **Credentials** tab → **Set password** → `test123` (Temporary: OFF)

**Check the database:**
```bash
docker exec iwa-user-postgres psql -U postgres -d iwa_users \
  -c "SELECT username, email, created_at FROM users WHERE username='instanttest';"
```

🎉 **The user should appear immediately!**

## ✨ How It Works

```
User registers in Keycloak
        ↓
Keycloak Extension triggers
        ↓
Calls webhook endpoint
        ↓
User added to PostgreSQL
        ↓
DONE! (All automatic)
```

## 🔍 Troubleshooting

### Extension not loaded?

```bash
# Check if JAR exists
docker exec iwa-keycloak ls -la /opt/keycloak/providers/

# Should show: keycloak-webhook-extension-1.0.0.jar
```

### Webhook not being called?

1. **Check event listener is enabled** in Admin Console (Step 4)
2. **Check Keycloak logs:**
   ```bash
   docker logs -f iwa-keycloak
   ```
3. **Test webhook directly:**
   ```bash
   curl -X POST http://localhost:8081/api/webhooks/keycloak/user-registered \
     -H "Content-Type: application/json" \
     -d '{"userId":"test123","username":"testwebhook","email":"test@test.com","firstName":"Test","lastName":"User"}'
   ```

### User not appearing in database?

1. **Check microservice logs** - should see "Received user registration webhook"
2. **Check microservice is running** on port 8081
3. **Check PostgreSQL is running**

## 📊 Verify Everything Works

```bash
# Watch Keycloak logs
docker logs -f iwa-keycloak

# In another terminal, create a user in Keycloak
# You should see in the logs:
# "User registration event detected: userId=..."
# "Sending webhook for user: ..."
# "✅ Successfully sent webhook for user: ... (HTTP 201)"
```

## 🎓 What You Get

✅ **Instant sync** - Users appear in DB immediately when they register  
✅ **Zero manual work** - No more manual database insertion  
✅ **Production ready** - Proper error handling and logging  
✅ **Works everywhere** - Admin-created users OR self-registration  

## 🔄 Auto-Setup Script (Alternative)

Or use the automated setup script:

```bash
cd ./IWAPROJECT/back/keycloak-webhook-extension
./setup.sh
```

This will guide you through all the steps automatically.

---

**That's it!** From now on, every user registered in Keycloak will automatically appear in PostgreSQL! 🚀
