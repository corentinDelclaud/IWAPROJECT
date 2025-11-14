#!/bin/bash
# Stop all User Microservice services

echo "🛑 Stopping User Microservice services..."
echo ""

# Navigate to the directory where the script is located
cd "$(dirname "$0")"

echo "Stopping Keycloak..."
docker compose -f docker-compose.keycloak.yml down

echo "Stopping PostgreSQL..."
docker compose -f docker-compose.yml down

echo ""
echo "✅ All services stopped!"
echo ""
echo "💡 Tips:"
echo "   - Data is preserved in Docker volumes"
echo "   - To remove all data, run: ./start-services.sh --clean"
echo "   - To restart, run: ./start-services.sh"
echo ""
