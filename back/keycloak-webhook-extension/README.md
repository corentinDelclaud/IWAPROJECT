# Keycloak Webhook Extension for User Synchronization

This extension synchronizes users between Keycloak and the user-microservice database automatically.

## Features

- **Automatic User Sync**: When a user is created in Keycloak, it's automatically created in the user-microservice database
- **Real-time Updates**: User updates in Keycloak are immediately reflected in the user-microservice
- **Soft Delete**: User deletions in Keycloak trigger a soft delete (timestamp) in the user-microservice
- **Keycloak ID Tracking**: The user-microservice stores the Keycloak user ID as the primary key

## Architecture

```
Keycloak Admin Event
        ↓
WebhookEventListenerProvider (SPI)
        ↓
HTTP Request to user-microservice
        ↓
UserWebhookController
        ↓
UserWebhookService
        ↓
User Database
```

## Installation

### 1. Build the Extension

```bash
cd /home/etienne/Documents/IWAPROJECT/back/keycloak-webhook-extension
chmod +x build-and-deploy.sh
./build-and-deploy.sh
```

### 2. Restart Keycloak

```bash
docker restart iwa-keycloak
```

Wait for Keycloak to fully start (check logs):
```bash
docker logs -f iwa-keycloak
```

### 3. Enable the Extension in Keycloak

1. Open Keycloak Admin Console: http://localhost:8085/admin
2. Login with admin/admin
3. Select the realm: **IWA_NextLevel**
4. Go to: **Realm Settings** → **Events** tab
5. In the **Event Listeners** section, click the field and add: `user-webhook-sync`
6. Click **Save**

### 4. Test the Integration

Create a test user in Keycloak:

```bash
# Access Keycloak Admin Console
# http://localhost:8085/admin
# Go to Users > Add User
```

Then verify the user was created in the user-microservice database:

```bash
docker exec iwa-postgres-users psql -U postgres -d iwa_users -c "SELECT id, username, email, first_name, last_name FROM users;"
```

## How It Works

### Keycloak Side

The extension implements the Keycloak `EventListenerProvider` SPI (Service Provider Interface):

1. **WebhookEventListenerProviderFactory**: Registers the extension with Keycloak
2. **WebhookEventListenerProvider**: Listens to admin events (CREATE, UPDATE, DELETE) for users
3. Sends HTTP requests to the user-microservice webhook endpoints

### User-Microservice Side

The microservice receives webhook events:

1. **UserWebhookController**: Exposes webhook endpoints
2. **UserWebhookService**: Handles the user synchronization logic
3. **User Entity**: Stores the Keycloak user ID as the primary key

### Webhook Endpoints

- `POST /api/webhooks/users` - Create user
- `PUT /api/webhooks/users/{userId}` - Update user
- `DELETE /api/webhooks/users/{userId}` - Soft delete user
- `GET /api/webhooks/health` - Health check

## Configuration

The webhook URL is configured in the extension and defaults to:
```
http://user-microservice:8081
```

This uses Docker's internal network to communicate between containers.

## User Entity Structure

The `User` entity in the user-microservice now stores:

```java
@Id
private String id; // Keycloak user ID (UUID)
private String username;
private String email;
private String firstName;
private String lastName;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
private LocalDateTime deletedAt; // For soft delete
```

## Troubleshooting

### Check Extension is Loaded

```bash
docker logs iwa-keycloak | grep -i webhook
```

You should see:
```
WebhookEventListenerProviderFactory initialized with webhookUrl: http://user-microservice:8081
```

### Check Webhook Requests

```bash
docker logs iwa-user-microservice | grep -i webhook
```

### Test Webhook Manually

```bash
curl -X POST http://localhost:8081/api/webhooks/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-id-123",
    "username": "testuser",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Re-sync Existing Users

If you have existing users in Keycloak that need to be synced, you can manually trigger the sync by updating each user in the Keycloak Admin Console (just change and save any field).

## Development

### Rebuild After Changes

```bash
./build-and-deploy.sh
docker restart iwa-keycloak
```

### Maven Commands

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# View dependencies
mvn dependency:tree
```

## Security Note

The webhook endpoints (`/api/webhooks/**`) are configured as public in the SecurityConfig because they're called by Keycloak from within the Docker network. In production, consider adding:

- Shared secret validation
- IP whitelist
- Network isolation
