#!/bin/bash

# =============================================================================
# Enable Webhook Event Listener in Keycloak
# =============================================================================

echo "======================================"
echo "Enabling Webhook Event Listener"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Get admin token
echo "Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8085/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}✗ Failed to get admin token${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Admin token obtained${NC}"
echo ""

# Get current realm configuration
echo "Getting current realm configuration..."
REALM_CONFIG=$(curl -s -X GET http://localhost:8085/admin/realms/IWA_NextLevel \
  -H "Authorization: Bearer $ADMIN_TOKEN")

# Extract current event listeners
CURRENT_LISTENERS=$(echo "$REALM_CONFIG" | jq -r '.eventsListeners // []')
echo "Current event listeners: $CURRENT_LISTENERS"
echo ""

# Check if our listener is already enabled
if echo "$CURRENT_LISTENERS" | grep -q "user-webhook-sync"; then
    echo -e "${GREEN}✓ user-webhook-sync is already enabled!${NC}"
    exit 0
fi

# Add our listener to the list
echo "Adding user-webhook-sync to event listeners..."
NEW_LISTENERS=$(echo "$CURRENT_LISTENERS" | jq '. + ["user-webhook-sync"]')

# Update realm with new event listeners
UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT http://localhost:8085/admin/realms/IWA_NextLevel \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"eventsListeners\": $NEW_LISTENERS,
    \"eventsEnabled\": true,
    \"adminEventsEnabled\": true,
    \"adminEventsDetailsEnabled\": true
  }")

HTTP_CODE=$(echo "$UPDATE_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "204" ] || [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Event listener enabled successfully!${NC}"
    echo ""
    echo "======================================"
    echo "Configuration Complete!"
    echo "======================================"
    echo ""
    echo "Event Listeners: $NEW_LISTENERS"
    echo ""
    echo "You can now test the webhook:"
    echo "  ./test-webhook.sh"
    echo ""
else
    echo -e "${RED}✗ Failed to enable event listener (HTTP $HTTP_CODE)${NC}"
    echo "$UPDATE_RESPONSE"
    exit 1
fi
