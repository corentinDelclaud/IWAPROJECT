#!/bin/bash

# Build and Deploy Keycloak Webhook Extension
# This script builds the extension JAR and deploys it to the Keycloak container

set -e

echo "======================================"
echo "Building Keycloak Webhook Extension"
echo "======================================"

# Build the extension
echo "Building JAR with Maven..."
mvn clean package

# Check if build was successful
if [ ! -f "target/keycloak-webhook-extension.jar" ]; then
    echo "ERROR: Build failed - JAR file not found"
    exit 1
fi

echo "Build successful! JAR created at: target/keycloak-webhook-extension.jar"

# Check if Keycloak container is running
if ! docker ps | grep -q iwa-keycloak; then
    echo "ERROR: Keycloak container is not running"
    echo "Start it with: docker-compose up -d keycloak"
    exit 1
fi

echo ""
echo "======================================"
echo "Deploying to Keycloak"
echo "======================================"

# Copy JAR to Keycloak container
echo "Copying JAR to Keycloak container..."
docker cp target/keycloak-webhook-extension.jar iwa-keycloak:/opt/keycloak/providers/

echo "Extension deployed successfully!"
echo ""
echo "======================================"
echo "Next Steps:"
echo "======================================"
echo "1. Restart Keycloak container:"
echo "   docker restart iwa-keycloak"
echo ""
echo "2. Wait for Keycloak to start (check logs):"
echo "   docker logs -f iwa-keycloak"
echo ""
echo "3. Configure the extension in Keycloak Admin Console:"
echo "   - Login to http://localhost:8085/admin"
echo "   - Go to: Realm Settings > Events > Event Listeners"
echo "   - Add 'user-webhook-sync' to the Event Listeners list"
echo "   - Save"
echo ""
echo "4. Test the webhook by creating a new user in Keycloak"
echo "======================================"
