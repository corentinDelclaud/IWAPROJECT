#!/bin/bash
# Startup script for User Microservice with Keycloak

set -e

echo "🚀 Starting User Microservice Infrastructure..."
echo ""

# Navigate to the directory where the script is located
cd "$(dirname "$0")"

echo "📦 Step 1: Starting PostgreSQL..."
docker compose -f docker-compose.yml up -d postgres
echo "✅ PostgreSQL started on port 5432"
echo ""

echo "🔑 Step 2: Starting Keycloak..."
docker compose -f docker-compose.keycloak.yml up -d keycloak
echo "⏳ Waiting for Keycloak to be ready (this may take 30-60 seconds)..."
echo ""

# Wait for Keycloak to be ready
for i in {1..40}; do 
  if curl -s http://localhost:8080/realms/IWA_NextLevel/.well-known/openid-configuration >/dev/null 2>&1; then
    echo "✅ Keycloak is ready!"
    break
  fi
  if [ $i -eq 40 ]; then
    echo "⚠️  Keycloak is taking longer than expected. Check logs with: docker logs iwa-keycloak"
    exit 1
  fi
  echo -n "."
  sleep 2
done
echo ""

echo "🏥 Step 3: Checking health..."
docker ps | grep -E "(iwa-user-postgres|iwa-keycloak)"
echo ""

echo "✅ All services are running!"
echo ""
echo "📍 Service URLs:"
echo "   - Keycloak Admin: http://localhost:8080 (admin/admin)"
echo "   - Keycloak Realm: IWA_NextLevel"
echo "   - PostgreSQL: localhost:5432 (postgres/postgres)"
echo ""
echo "🎯 Next steps:"
echo "   1. Start the microservice:"
echo "      ./mvnw spring-boot:run"
echo ""
echo "   2. Or follow the complete guide:"
echo "      cat START_HERE.md"
echo ""
