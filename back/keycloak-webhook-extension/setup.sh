#!/bin/bash
# Setup script for Keycloak Webhook Extension

set -e

echo "🚀 Setting up Keycloak Webhook Extension"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Step 1: Build extension
echo -e "${BLUE}📦 Step 1: Building Keycloak extension...${NC}"
cd ./IWAPROJECT/back/keycloak-webhook-extension

if mvn clean package -q; then
    echo -e "${GREEN}✅ Extension built successfully!${NC}"
    echo "   JAR: target/keycloak-webhook-extension-1.0.0.jar"
else
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi
echo ""

# Step 2: Check if Keycloak is running
echo -e "${BLUE}🔍 Step 2: Checking if Keycloak is running...${NC}"
if docker ps | grep -q iwa-keycloak; then
    echo -e "${YELLOW}⚠️  Keycloak is running. It needs to be restarted to load the extension.${NC}"
    read -p "   Stop and restart Keycloak now? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd ./IWAPROJECT/back/user-microservice
        docker compose -f docker-compose.keycloak.yml down
        echo -e "${GREEN}✅ Keycloak stopped${NC}"
    fi
else
    echo -e "${GREEN}✅ Keycloak is not running${NC}"
fi
echo ""

# Step 3: Update docker-compose
echo -e "${BLUE}📝 Step 3: Updating docker-compose.keycloak.yml...${NC}"
COMPOSE_FILE="./IWAPROJECT/back/user-microservice/docker-compose.keycloak.yml"

if grep -q "keycloak-webhook-extension" "$COMPOSE_FILE"; then
    echo -e "${GREEN}✅ docker-compose.keycloak.yml already configured${NC}"
else
    echo -e "${YELLOW}⚠️  Need to update docker-compose.keycloak.yml manually${NC}"
    echo "   Add this volume to the keycloak service:"
    echo "   - ../keycloak-webhook-extension/target/keycloak-webhook-extension-1.0.0.jar:/opt/keycloak/providers/keycloak-webhook-extension-1.0.0.jar"
fi
echo ""

# Step 4: Start Keycloak
echo -e "${BLUE}🚀 Step 4: Starting Keycloak with extension...${NC}"
read -p "   Start Keycloak now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd ./IWAPROJECT/back/user-microservice
    docker compose -f docker-compose.keycloak.yml up -d
    
    echo -e "${YELLOW}⏳ Waiting for Keycloak to start (30 seconds)...${NC}"
    sleep 30
    
    # Check if extension loaded
    if docker logs iwa-keycloak 2>&1 | grep -q "Webhook Event Listener initialized"; then
        echo -e "${GREEN}✅ Extension loaded successfully!${NC}"
        docker logs iwa-keycloak 2>&1 | grep "Webhook Event Listener" | tail -3
    else
        echo -e "${RED}❌ Extension may not have loaded. Check logs:${NC}"
        echo "   docker logs iwa-keycloak | grep -i webhook"
    fi
fi
echo ""

# Step 5: Configure in Keycloak
echo -e "${BLUE}⚙️  Step 5: Configure Event Listener in Keycloak${NC}"
echo ""
echo "Now you need to enable the event listener in Keycloak:"
echo ""
echo "1. Go to: ${YELLOW}http://localhost:8080/admin${NC}"
echo "2. Login: ${YELLOW}admin${NC} / ${YELLOW}admin${NC}"
echo "3. Select realm: ${YELLOW}IWA_NextLevel${NC}"
echo "4. Go to: ${YELLOW}Realm Settings → Events tab${NC}"
echo "5. Scroll to: ${YELLOW}Event Listeners${NC}"
echo "6. Add: ${YELLOW}webhook-event-listener${NC}"
echo "7. Click: ${YELLOW}Save${NC}"
echo ""
read -p "Press Enter when you've configured the event listener..."
echo ""

# Step 6: Test
echo -e "${BLUE}🧪 Step 6: Testing the extension${NC}"
echo ""
echo "Create a test user in Keycloak:"
echo "1. Go to: ${YELLOW}http://localhost:8080/admin${NC}"
echo "2. Go to: ${YELLOW}Users → Create new user${NC}"
echo "3. Username: ${YELLOW}webhooktest${NC}"
echo "4. Email: ${YELLOW}webhook@test.com${NC}"
echo "5. Save, then set password in Credentials tab"
echo ""
read -p "Press Enter when you've created the test user..."
echo ""

# Check database
echo "Checking database for the new user..."
if docker exec iwa-user-postgres psql -U postgres -d iwa_users -t -c "SELECT COUNT(*) FROM users WHERE username='webhooktest';" 2>/dev/null | grep -q "1"; then
    echo -e "${GREEN}🎉 SUCCESS! User was automatically added to the database!${NC}"
    docker exec iwa-user-postgres psql -U postgres -d iwa_users -c "SELECT username, email, created_at FROM users WHERE username='webhooktest';"
else
    echo -e "${YELLOW}⚠️  User not found in database yet. Check:${NC}"
    echo "   1. Microservice logs for webhook messages"
    echo "   2. Keycloak logs: docker logs iwa-keycloak | grep webhook"
    echo "   3. Event listener is enabled in Keycloak Admin Console"
fi
echo ""

echo -e "${GREEN}✅ Setup complete!${NC}"
echo ""
echo "📚 Next steps:"
echo "   - Register users in Keycloak - they'll be synced automatically"
echo "   - Check logs: docker logs -f iwa-keycloak"
echo "   - Check database: docker exec iwa-user-postgres psql -U postgres -d iwa_users -c 'SELECT * FROM users;'"
