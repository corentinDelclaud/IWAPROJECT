#!/bin/bash

# ==============================================================================
# Script de test d'intégration - IWA PROJECT
# ==============================================================================
# Teste les endpoints principaux de tous les services
# ==============================================================================

set -e

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🧪 Tests d'intégration - IWA Project"
echo ""

# Compteurs
total_tests=0
passed_tests=0

# Fonction de test
test_endpoint() {
    local name=$1
    local url=$2
    local expected_code=$3
    
    ((total_tests++))
    
    echo -n "Testing $name... "
    
    response_code=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    
    if [ "$response_code" -eq "$expected_code" ]; then
        echo -e "${GREEN}✅ PASS${NC} (HTTP $response_code)"
        ((passed_tests++))
        return 0
    else
        echo -e "${RED}❌ FAIL${NC} (Expected $expected_code, got $response_code)"
        return 1
    fi
}

# Attendre que les services soient prêts
echo -e "${YELLOW}⏳ Attente du démarrage des services...${NC}"
sleep 5
echo ""

# ==============================================================================
# TESTS
# ==============================================================================

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🔐 Tests Keycloak${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "Keycloak Health" "http://localhost:8085/health/ready" 200
test_endpoint "Keycloak Realms" "http://localhost:8085/realms/IWA_NextLevel" 200
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🚪 Tests API Gateway${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "API Gateway Health" "http://localhost:8080/actuator/health" 200
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🔑 Tests Auth Service${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "Auth Service Health" "http://localhost:8082/actuator/health" 200
test_endpoint "Auth Service Swagger" "http://localhost:8082/swagger-ui/index.html" 200
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}👤 Tests User Microservice${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "User Service Health" "http://localhost:8081/actuator/health" 200
test_endpoint "User Service Swagger" "http://localhost:8081/swagger-ui/index.html" 200
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📦 Tests Service Catalog${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
test_endpoint "Catalog Service Health" "http://localhost:8083/actuator/health" 200
test_endpoint "Catalog Service Swagger" "http://localhost:8083/swagger-ui/index.html" 200
echo ""

# ==============================================================================
# RÉSUMÉ
# ==============================================================================

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📊 Résumé des tests${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Total de tests : $total_tests"
echo "Tests réussis  : $passed_tests"
echo "Tests échoués  : $((total_tests - passed_tests))"
echo ""

if [ $passed_tests -eq $total_tests ]; then
    echo -e "${GREEN}🎉 Tous les tests sont passés avec succès !${NC}"
    exit 0
else
    echo -e "${RED}❌ Certains tests ont échoué${NC}"
    echo ""
    echo "Suggestions :"
    echo "  - Vérifiez les logs : docker-compose logs -f"
    echo "  - Attendez quelques secondes et réessayez"
    exit 1
fi
