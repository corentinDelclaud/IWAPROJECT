#!/bin/bash

# =============================================================================
# Test Script for Keycloak Webhook User Synchronization
# =============================================================================

echo "======================================"
echo "Testing Keycloak Webhook Integration"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# =============================================================================
# STEP 1: Check all services are running
# =============================================================================
echo "STEP 1: Checking Docker containers status..."
echo "--------------------------------------"
docker ps --filter "name=iwa-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# =============================================================================
# STEP 2: Verify Webhook Extension is loaded in Keycloak
# =============================================================================
echo "STEP 2: Verifying webhook extension loaded in Keycloak..."
echo "--------------------------------------"
if docker logs iwa-keycloak 2>&1 | grep -q "WebhookEventListenerProviderFactory initialized"; then
    echo -e "${GREEN}✓ Webhook extension loaded successfully${NC}"
    docker logs iwa-keycloak 2>&1 | grep "WebhookEventListenerProviderFactory initialized"
else
    echo -e "${RED}✗ Webhook extension NOT loaded${NC}"
    echo "Run: docker restart iwa-keycloak"
fi
echo ""

# =============================================================================
# STEP 3: Check webhook endpoint health
# =============================================================================
echo "STEP 3: Testing webhook endpoint health..."
echo "--------------------------------------"
WEBHOOK_HEALTH=$(curl -s http://localhost:8081/api/webhooks/health)
if [ "$WEBHOOK_HEALTH" = "Webhook endpoint is healthy" ]; then
    echo -e "${GREEN}✓ Webhook endpoint is healthy${NC}"
else
    echo -e "${RED}✗ Webhook endpoint not responding correctly${NC}"
    echo "Response: $WEBHOOK_HEALTH"
fi
echo ""

# =============================================================================
# STEP 4: Check current database state
# =============================================================================
echo "STEP 4: Checking current database state..."
echo "--------------------------------------"
echo "Users in Keycloak database:"
docker exec iwa-postgres-keycloak psql -U keycloak -d keycloak -c "SELECT id, username, email, first_name, last_name FROM user_entity ORDER BY username;" 2>/dev/null
echo ""

echo "Users in user-microservice database:"
docker exec iwa-postgres-users psql -U postgres -d iwa_users -c "SELECT id, username, email, first_name, last_name FROM users ORDER BY username;" 2>/dev/null
echo ""

# =============================================================================
# STEP 5: Get admin token
# =============================================================================
echo "STEP 5: Getting admin token from Keycloak..."
echo "--------------------------------------"
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8085/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" != "null" ] && [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✓ Admin token obtained successfully${NC}"
else
    echo -e "${RED}✗ Failed to get admin token${NC}"
    exit 1
fi
echo ""

# =============================================================================
# STEP 6: Create a test user via Keycloak API
# =============================================================================
echo "STEP 6: Creating test user via Keycloak API..."
echo "--------------------------------------"
TIMESTAMP=$(date +%s)
TEST_USERNAME="webhooktest_$TIMESTAMP"

CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8085/admin/realms/IWA_NextLevel/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "'$TEST_USERNAME'",
    "email": "'$TEST_USERNAME'@example.com",
    "firstName": "Webhook",
    "lastName": "Test",
    "enabled": true,
    "emailVerified": true
  }')

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ User created successfully in Keycloak${NC}"
    echo "Username: $TEST_USERNAME"
    echo "Email: ${TEST_USERNAME}@example.com"
else
    echo -e "${RED}✗ Failed to create user (HTTP $HTTP_CODE)${NC}"
    echo "$CREATE_RESPONSE"
fi
echo ""

# Wait a moment for webhook to process
echo "Waiting 2 seconds for webhook to process..."
sleep 2
echo ""

# =============================================================================
# STEP 7: Verify user was synced to user-microservice database
# =============================================================================
echo "STEP 7: Verifying user synchronization..."
echo "--------------------------------------"
echo "Users in user-microservice database after creation:"
docker exec iwa-postgres-users psql -U postgres -d iwa_users -c "SELECT id, username, email, first_name, last_name, created_at FROM users WHERE username = '$TEST_USERNAME';" 2>/dev/null

USER_COUNT=$(docker exec iwa-postgres-users psql -U postgres -d iwa_users -t -c "SELECT COUNT(*) FROM users WHERE username = '$TEST_USERNAME';" 2>/dev/null | tr -d ' ')

if [ "$USER_COUNT" = "1" ]; then
    echo -e "${GREEN}✓ User successfully synced to user-microservice database!${NC}"
else
    echo -e "${RED}✗ User NOT synced to user-microservice database${NC}"
    echo "Check webhook logs:"
    echo "  docker logs iwa-user-microservice | grep -i webhook"
fi
echo ""

# =============================================================================
# STEP 8: Check webhook logs
# =============================================================================
echo "STEP 8: Checking webhook logs..."
echo "--------------------------------------"
echo "Recent webhook activity in user-microservice:"
docker logs iwa-user-microservice 2>&1 | grep -i "webhook" | tail -n 5
echo ""

# =============================================================================
# STEP 9: Get user ID from Keycloak and set password
# =============================================================================
echo "STEP 9: Setting password for test user..."
echo "--------------------------------------"
USER_ID=$(curl -s -X GET "http://localhost:8085/admin/realms/IWA_NextLevel/users?username=$TEST_USERNAME" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

if [ "$USER_ID" != "null" ] && [ -n "$USER_ID" ]; then
    echo "User ID: $USER_ID"
    
    # Set password
    curl -s -X PUT "http://localhost:8085/admin/realms/IWA_NextLevel/users/$USER_ID/reset-password" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "type": "password",
        "value": "Test123!",
        "temporary": false
      }'
    
    echo -e "${GREEN}✓ Password set to: Test123!${NC}"
else
    echo -e "${RED}✗ Could not find user ID${NC}"
fi
echo ""

# =============================================================================
# STEP 10: Test authentication with new user
# =============================================================================
echo "STEP 10: Testing authentication with new user..."
echo "--------------------------------------"
USER_TOKEN=$(curl -s -X POST http://localhost:8085/realms/IWA_NextLevel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$TEST_USERNAME" \
  -d "password=Test123!" \
  -d "grant_type=password" \
  -d "client_id=iwa-client" | jq -r '.access_token')

if [ "$USER_TOKEN" != "null" ] && [ -n "$USER_TOKEN" ]; then
    echo -e "${GREEN}✓ Successfully authenticated${NC}"
    echo "Token (first 50 chars): ${USER_TOKEN:0:50}..."
else
    echo -e "${YELLOW}⚠ Authentication failed or client 'iwa-client' not configured${NC}"
    echo "You may need to create the client in Keycloak first"
fi
echo ""

# =============================================================================
# STEP 11: Test user profile endpoint
# =============================================================================
if [ "$USER_TOKEN" != "null" ] && [ -n "$USER_TOKEN" ]; then
    echo "STEP 11: Testing user profile endpoint..."
    echo "--------------------------------------"
    PROFILE_RESPONSE=$(curl -s -X GET http://localhost:8081/api/users/profile \
      -H "Authorization: Bearer $USER_TOKEN" \
      -H "Accept: application/json")
    
    echo "Profile response:"
    echo "$PROFILE_RESPONSE" | jq '.' 2>/dev/null || echo "$PROFILE_RESPONSE"
    echo ""
fi

# =============================================================================
# SUMMARY
# =============================================================================
echo "======================================"
echo "Test Summary"
echo "======================================"
echo ""
echo "✓ Tests completed!"
echo ""
echo "To manually test webhook, create users in Keycloak Admin Console:"
echo "  http://localhost:8085/admin"
echo ""
echo "To check if event listener is enabled:"
echo "  1. Go to Keycloak Admin Console"
echo "  2. Select realm: IWA_NextLevel"
echo "  3. Go to: Realm Settings > Events"
echo "  4. Check 'user-webhook-sync' is in Event Listeners list"
echo ""
echo "Created test user:"
echo "  Username: $TEST_USERNAME"
echo "  Password: Test123!"
echo "  Email: ${TEST_USERNAME}@example.com"
echo ""
