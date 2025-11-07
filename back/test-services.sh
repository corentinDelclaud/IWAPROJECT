#!/bin/bash

# Script de test des microservices IWA
# Usage: ./test-services.sh

echo "======================================"
echo "Test des Microservices IWA"
echo "======================================"
echo ""

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour tester un endpoint
test_endpoint() {
    local name=$1
    local url=$2
    
    echo -n "Testing $name... "
    
    if response=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 "$url" 2>/dev/null); then
        if [ "$response" = "200" ] || [ "$response" = "404" ] || [ "$response" = "302" ]; then
            echo -e "${GREEN}✓ OK${NC} (HTTP $response)"
            return 0
        else
            echo -e "${YELLOW}⚠ Warning${NC} (HTTP $response)"
            return 1
        fi
    else
        echo -e "${RED}✗ FAILED${NC} (No response)"
        return 2
    fi
}

# Vérifier Docker
echo "1. Vérification des conteneurs Docker"
echo "--------------------------------------"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter "name=iwa-"
echo ""

# Tester les services
echo "2. Test des endpoints de santé"
echo "--------------------------------------"

# API Gateway
test_endpoint "API Gateway (8080)" "http://localhost:8080"

# Keycloak
test_endpoint "Keycloak Admin (9090)" "http://localhost:9090"
test_endpoint "Keycloak Realm" "http://localhost:9090/realms/IWA_NextLevel"

# PostgreSQL via Docker
echo -n "Testing PostgreSQL... "
if docker exec iwa-user-postgres pg_isready -U postgres >/dev/null 2>&1; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
fi

echo ""

# Tester les endpoints via API Gateway
echo "3. Test des microservices via API Gateway"
echo "--------------------------------------"

# Test routes
test_endpoint "Auth Service Health (via Gateway)" "http://localhost:8080/api/auth/health"
test_endpoint "Auth Service Actuator (via Gateway)" "http://localhost:8080/api/auth/actuator/health"
test_endpoint "User Service Actuator (via Gateway)" "http://localhost:8080/api/users/actuator/health"

echo ""

# Informations supplémentaires
echo "4. Informations supplémentaires"
echo "--------------------------------------"
echo "Console Admin Keycloak: http://localhost:9090"
echo "  - Username: admin"
echo "  - Password: admin"
echo ""
echo "API Gateway: http://localhost:8080"
echo "Frontend attendu: http://localhost:19000"
echo ""

# Vérifier les logs d'erreur récents
echo "5. Dernières erreurs dans les logs"
echo "--------------------------------------"
echo "Keycloak:"
docker logs iwa-keycloak --tail 5 2>&1 | grep -i "error\|exception\|warn" | tail -n 3
echo ""
echo "API Gateway:"
docker logs iwa-api-gateway --tail 5 2>&1 | grep -i "error\|exception\|warn" | tail -n 3
echo ""

echo "======================================"
echo "Test terminé"
echo "======================================"
echo ""
echo "Pour voir les logs en temps réel:"
echo "  docker-compose -f docker-compose.production.yml logs -f"
echo ""
echo "Pour redémarrer un service:"
echo "  docker-compose -f docker-compose.production.yml restart <service-name>"
echo ""
