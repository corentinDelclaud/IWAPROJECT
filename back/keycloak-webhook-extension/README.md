# Keycloak Webhook Extension

This extension automatically calls your microservice webhook when users register in Keycloak, syncing them to PostgreSQL **immediately** without waiting for first API access.

## 📋 What It Does

- Listens for user registration events in Keycloak
- Automatically calls `POST /api/webhooks/keycloak/user-registered` 
- User is added to PostgreSQL database immediately
- No need to wait for first API access

## 🔨 Build the Extension

```bash
cd ./IWAPROJECT/back/keycloak-webhook-extension
mvn clean package
```

This creates: `target/keycloak-webhook-extension-1.0.0.jar`

## 📦 Deploy to Keycloak

### Option 1: Update docker-compose.keycloak.yml (Recommended)

Edit your `user-microservice/docker-compose.keycloak.yml`:

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:26.0.7
    container_name: iwa-keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_HTTP_PORT: 8080
      KC_HOSTNAME_STRICT: false
      KC_HOSTNAME_STRICT_HTTPS: false
      KC_HTTP_ENABLED: true
    ports:
      - "8080:8080"
    volumes:
      - ./keycloak-data:/opt/keycloak/data
      - ../../keycloak-config/realm-export.json:/opt/keycloak/data/import/realm-export.json
      # Mount the webhook extension
      - ../keycloak-webhook-extension/target/keycloak-webhook-extension-1.0.0.jar:/opt/keycloak/providers/keycloak-webhook-extension-1.0.0.jar
    command:
      - start-dev
      - --import-realm
    networks:
      - iwa-network

networks:
  iwa-network:
    driver: bridge
```

### Option 2: Copy JAR manually

```bash
docker cp target/keycloak-webhook-extension-1.0.0.jar iwa-keycloak:/opt/keycloak/providers/
docker restart iwa-keycloak
```

## ⚙️ Configure in Keycloak

1. Go to **http://localhost:8080/admin**
2. Login: `admin` / `admin`
3. Select realm: **IWA_NextLevel**
4. Go to: **Realm Settings** → **Events** tab
5. Scroll to **Event Listeners**
6. Add `webhook-event-listener` to the list
7. Click **Save**

## 🧪 Test It

### 1. Make sure your microservice is running

```bash
cd ./IWAPROJECT/back/user-microservice
mvn spring-boot:run
```

### 2. Make sure Keycloak is running with the extension

```bash
cd ./IWAPROJECT/back/user-microservice
docker compose -f docker-compose.keycloak.yml up -d
```

### 3. Check Keycloak logs to verify extension loaded

```bash
docker logs iwa-keycloak | grep -i webhook
```

You should see:
```
🚀 Webhook Event Listener initialized
   Webhook URL: http://host.docker.internal:8081/api/webhooks/keycloak/user-registered
```

### 4. Register a new user

**Via Admin Console:**
1. http://localhost:8080/admin
2. Users → Create new user
3. Username: `testwebhook`
4. Email: `webhook@test.com`
5. Save, then set password in Credentials tab

**Or via Self-Registration:**
1. http://localhost:8080/realms/IWA_NextLevel/account
2. Click Register
3. Fill form and submit

### 5. Check the results

**Check microservice logs:**
```bash
# Should see: "Received user registration webhook for user: testwebhook"
```

**Check database:**
```bash
docker exec iwa-user-postgres psql -U postgres -d iwa_users \
  -c "SELECT username, email, created_at FROM users ORDER BY created_at DESC LIMIT 5;"
```

You should see the new user immediately! 🎉

## 🐛 Troubleshooting

### Extension not loaded

```bash
# Check if JAR exists in container
docker exec iwa-keycloak ls -la /opt/keycloak/providers/

# Check Keycloak logs
docker logs iwa-keycloak | grep -i "webhook"
```

### Webhook not being called

1. **Check event listener is enabled:**
   - Admin Console → Realm Settings → Events → Event Listeners
   - Should have `webhook-event-listener` in the list

2. **Check Keycloak logs:**
   ```bash
   docker logs -f iwa-keycloak
   ```
   Register a user and watch for webhook messages

3. **Test webhook directly:**
   ```bash
   curl -X POST http://localhost:8081/api/webhooks/keycloak/user-registered \
     -H "Content-Type: application/json" \
     -d '{"userId":"test","username":"test","email":"test@test.com","firstName":"Test","lastName":"User"}'
   ```

### Connection refused

- Make sure you're using `host.docker.internal` in the webhook URL (not `localhost`)
- This allows Docker containers to reach services on the host machine

### Rebuild after changes

```bash
cd ./IWAPROJECT/back/keycloak-webhook-extension
mvn clean package
docker restart iwa-keycloak
```

## 🔒 Configuration

You can customize the webhook URL by setting environment variable in `docker-compose.keycloak.yml`:

```yaml
environment:
  KC_SPI_EVENTS_LISTENER_WEBHOOK_EVENT_LISTENER_WEBHOOK_URL: "http://host.docker.internal:8081/api/webhooks/keycloak/user-registered"
```

## 📚 How It Works

1. User registers in Keycloak
2. Keycloak fires `REGISTER` event
3. Extension catches the event
4. Extension calls webhook: `POST /api/webhooks/keycloak/user-registered`
5. Microservice creates user in PostgreSQL
6. Done! User is synced immediately

## ✅ Benefits

- ✅ Users synced **immediately** on registration
- ✅ Works even if user never uses the API
- ✅ Clean separation of concerns
- ✅ Reliable with proper error handling
- ✅ Detailed logging for debugging

## 🎓 Files

```
keycloak-webhook-extension/
├── pom.xml                                         # Maven configuration
├── README.md                                       # This file
└── src/main/
    ├── java/com/iwaproject/keycloak/
    │   ├── WebhookEventListenerProvider.java      # Main event handler
    │   └── WebhookEventListenerProviderFactory.java # Provider factory
    └── resources/META-INF/services/
        └── org.keycloak.events.EventListenerProviderFactory # SPI registration
```
