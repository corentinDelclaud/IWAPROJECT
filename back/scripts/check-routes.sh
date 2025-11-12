#!/bin/bash

# Script pour lister et tester toutes les routes de l'API Gateway
# Usage: ./check-routes.sh

echo "======================================"
echo "Routes disponibles dans l'API Gateway"
echo "======================================"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}=== Routes Keycloak (Publiques) ===${NC}"
echo "GET  http://localhost:8080/realms/IWA_NextLevel"
echo "GET  http://localhost:8080/resources/**"
echo ""

echo -e "${BLUE}=== Routes Auth Service (Publiques) ===${NC}"
echo "POST http://localhost:8080/api/auth/login"
echo "POST http://localhost:8080/api/auth/register"
echo "POST http://localhost:8080/api/auth/refresh"
echo "POST http://localhost:8080/api/auth/logout"
echo "GET  http://localhost:8080/api/auth/health"
echo "GET  http://localhost:8080/api/auth/actuator/health"
echo ""

echo -e "${BLUE}=== Routes User Service (Protégées JWT) ===${NC}"
echo "GET  http://localhost:8080/api/users/profile"
echo "GET  http://localhost:8080/api/users/{userId}"
echo "PUT  http://localhost:8080/api/users/profile"
echo ""

echo -e "${BLUE}=== Routes User Service (Publiques - Actuator) ===${NC}"
echo "GET  http://localhost:8080/api/users/actuator/health"
echo ""

echo -e "${BLUE}=== Routes Webhook (Internes - Keycloak) ===${NC}"
echo "POST http://localhost:8080/api/webhooks/**"
echo ""

echo "======================================"
echo "Test des routes publiques"
echo "======================================"
echo ""

# Fonction de test
test_route() {
    local method=$1
    local url=$2
    local description=$3
    
    echo -n "Testing $description... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    else
        response=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" 2>/dev/null)
    fi
    
    if [ "$response" = "200" ] || [ "$response" = "302" ]; then
        echo -e "${GREEN}✓ OK${NC} (HTTP $response)"
    elif [ "$response" = "401" ]; then
        echo -e "${YELLOW}⚠ Protected${NC} (HTTP $response - JWT required)"
    elif [ "$response" = "404" ]; then
        echo -e "${RED}✗ NOT FOUND${NC} (HTTP $response)"
    else
        echo -e "${YELLOW}⚠ Response${NC} (HTTP $response)"
    fi
}

# Test Keycloak
test_route "GET" "http://localhost:8080/realms/IWA_NextLevel" "Keycloak Realm via Gateway"

# Test Auth Service
test_route "GET" "http://localhost:8080/api/auth/health" "Auth Health (custom endpoint)"
test_route "GET" "http://localhost:8080/api/auth/actuator/health" "Auth Actuator Health"

# Test User Service (actuator public)
test_route "GET" "http://localhost:8080/api/users/actuator/health" "User Actuator Health"

# Test User Service (routes protégées - devrait renvoyer 401)
test_route "GET" "http://localhost:8080/api/users/profile" "User Profile (protected)"

echo ""
echo "======================================"
echo "Routes Gateway Management"
echo "======================================"
echo ""

echo "Vérifier les routes configurées dans Spring Cloud Gateway:"
echo "  curl http://localhost:8080/actuator/gateway/routes | jq"
echo ""

echo "Informations sur l'API Gateway:"
echo "  curl http://localhost:8080/actuator/health"
echo ""

echo "Tester manuellement une route protégée avec JWT:"
echo "  1. D'abord, obtenir un token:"
echo '     curl -X POST http://localhost:8080/api/auth/login \'
echo '       -H "Content-Type: application/json" \'
echo '       -d '\''{"username":"test","password":"password"}'\'''
echo ""
echo "  2. Ensuite, utiliser le token:"
echo '     curl http://localhost:8080/api/users/profile \'
echo '       -H "Authorization: Bearer YOUR_TOKEN"'
echo ""
