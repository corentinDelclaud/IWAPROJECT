#!/bin/bash

# ==============================================================================
# Script de démarrage complet - IWA PROJECT
# ==============================================================================
# Ce script démarre tous les services avec Docker Compose
# ==============================================================================

set -e

echo "🚀 Démarrage de l'infrastructure IWA Project..."
echo ""

# Couleurs pour l'affichage
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Se déplacer dans le répertoire parent (où se trouve docker-compose.yml)
cd "$(dirname "$0")/.."

# Vérifier que Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Veuillez installer Docker d'abord."
    exit 1
fi

# Vérifier que Docker Compose est installé
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé. Veuillez installer Docker Compose d'abord."
    exit 1
fi

echo -e "${BLUE}📦 Construction des images Docker...${NC}"
docker-compose build

echo ""
echo -e "${BLUE}🐳 Démarrage des conteneurs...${NC}"
docker-compose up -d

echo ""
echo -e "${YELLOW}⏳ Attente du démarrage des services (cela peut prendre 1-2 minutes)...${NC}"
sleep 10

echo ""
echo -e "${GREEN}✅ Tous les services sont en cours de démarrage !${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}📋 Services disponibles :${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${BLUE}🔐 Keycloak (Auth Server)${NC}"
echo "   URL:  http://localhost:8085"
echo "   User: admin / admin"
echo ""
echo -e "${BLUE}🚪 API Gateway${NC}"
echo "   URL:  http://localhost:8080"
echo ""
echo -e "${BLUE}🔑 Auth Service${NC}"
echo "   URL:      http://localhost:8082"
echo "   Swagger:  http://localhost:8082/swagger-ui/index.html"
echo ""
echo -e "${BLUE}👤 User Microservice${NC}"
echo "   URL:      http://localhost:8081"
echo "   Swagger:  http://localhost:8081/swagger-ui/index.html"
echo ""
echo -e "${BLUE}📦 Service Catalog${NC}"
echo "   URL:      http://localhost:8083"
echo "   Swagger:  http://localhost:8083/swagger-ui/index.html"
echo ""
echo -e "${BLUE}🗄️  Bases de données PostgreSQL${NC}"
echo "   Users DB:    localhost:5433"
echo "   Catalog DB:  localhost:5434"
echo "   Keycloak DB: localhost:5435"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${YELLOW}📊 Commandes utiles :${NC}"
echo "  - Voir les logs:           docker-compose logs -f"
echo "  - Voir les logs d'un service: docker-compose logs -f <service>"
echo "  - Arrêter les services:    docker-compose down"
echo "  - Redémarrer:              docker-compose restart"
echo "  - Statut des services:     docker-compose ps"
echo ""
echo -e "${GREEN}🎉 Démarrage terminé !${NC}"
