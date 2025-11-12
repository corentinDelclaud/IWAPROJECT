#!/bin/bash

# ==============================================================================
# Script de vérification de santé - IWA PROJECT
# ==============================================================================
# Vérifie que tous les services sont accessibles
# ==============================================================================

# Se déplacer dans le répertoire parent (où se trouve docker-compose.yml)
cd "$(dirname "$0")/.."

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🏥 Vérification de la santé des services..."
echo ""

# Fonction pour vérifier un service
check_service() {
    local name=$1
    local url=$2
    local timeout=5
    
    if curl -s --max-time $timeout "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $name${NC} - Accessible"
        return 0
    else
        echo -e "${RED}❌ $name${NC} - Non accessible ($url)"
        return 1
    fi
}

# Fonction pour vérifier un service avec health endpoint
check_health() {
    local name=$1
    local url=$2
    local timeout=5
    
    response=$(curl -s --max-time $timeout "$url" 2>&1)
    if [[ $? -eq 0 ]] && [[ $response == *"UP"* || $response == *"200"* || $response == *"ready"* ]]; then
        echo -e "${GREEN}✅ $name${NC} - Healthy"
        return 0
    else
        echo -e "${RED}❌ $name${NC} - Unhealthy ou non accessible"
        return 1
    fi
}

total=0
success=0

# Vérifier Keycloak
echo -e "${BLUE}🔐 Keycloak${NC}"
((total++))
if check_health "Keycloak" "http://localhost:8085/health/ready"; then
    ((success++))
fi
echo ""

# Vérifier API Gateway
echo -e "${BLUE}🚪 API Gateway${NC}"
((total++))
if check_service "API Gateway" "http://localhost:8080/actuator/health"; then
    ((success++))
fi
echo ""

# Vérifier Auth Service
echo -e "${BLUE}🔑 Auth Service${NC}"
((total++))
if check_service "Auth Service" "http://localhost:8082/actuator/health"; then
    ((success++))
fi
echo ""

# Vérifier User Microservice
echo -e "${BLUE}👤 User Microservice${NC}"
((total++))
if check_service "User Microservice" "http://localhost:8081/actuator/health"; then
    ((success++))
fi
echo ""

# Vérifier Service Catalog
echo -e "${BLUE}📦 Service Catalog${NC}"
((total++))
if check_service "Service Catalog" "http://localhost:8083/actuator/health"; then
    ((success++))
fi
echo ""

# Résumé
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $success -eq $total ]; then
    echo -e "${GREEN}🎉 Tous les services sont opérationnels ! ($success/$total)${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️  Certains services ne sont pas disponibles ($success/$total)${NC}"
    echo ""
    echo "Suggestions :"
    echo "  - Vérifiez les logs : docker-compose logs -f"
    echo "  - Attendez quelques secondes et réessayez"
    echo "  - Redémarrez les services : docker-compose restart"
    exit 1
fi
