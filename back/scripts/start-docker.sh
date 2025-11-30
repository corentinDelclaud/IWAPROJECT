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

# Se déplacer dans le répertoire parent (où se trouve docker compose.yml)
cd "$(dirname "$0")/.."

# Vérifier que Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Veuillez installer Docker d'abord."
    exit 1
fi

# Vérifier que Docker Compose est installé
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé. Veuillez installer Docker Compose d'abord."
    exit 1
fi

echo -e "${BLUE}📦 Construction des images Docker...${NC}"
docker compose build

echo ""
echo -e "${BLUE}🐳 Démarrage des conteneurs...${NC}"
docker compose up -d

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
echo "  - Voir les logs:           docker compose logs -f"
echo "  - Voir les logs d'un service: docker compose logs -f <service>"
echo "  - Arrêter les services:    docker compose down"
echo "  - Redémarrer:              docker compose restart"
echo "  - Statut des services:     docker compose ps"
echo ""
echo -e "${GREEN}🎉 Démarrage terminé !${NC}"

TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwdE1FcEs2VDhPR21UTTJXSTZmYmhiM0ZMOXFmbWhqb29TcTQ5LVd3YXFrIn0.eyJleHAiOjE3NjQ1MjI5NjcsImlhdCI6MTc2NDUyMjY2NywianRpIjoiMmE4NDE2YjUtMTBjNS00YWQzLTk5OGYtYmRmYjZjYjQxNmQyIiwiaXNzIjoiaHR0cDovLzE5Mi4xNjguMS4xNjE6ODA4NS9yZWFsbXMvSVdBX05leHRMZXZlbCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJmNmFhOTBmYS1kNDcxLTQ1NmUtYWM1OS1kYTUyZWFiMWVkOGMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhdXRoLXNlcnZpY2UiLCJzaWQiOiI0YTI1NWY5NS0zNTg5LTRlNmMtYmE4ZC1iZWIzN2JlYjNiN2QiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwiZGVmYXVsdC1yb2xlcy1pd2FfbmV4dGxldmVsIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJzdHJpbmczIHN0cmluZzMiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzdHJpbmczIiwiZ2l2ZW5fbmFtZSI6InN0cmluZzMiLCJmYW1pbHlfbmFtZSI6InN0cmluZzMiLCJlbWFpbCI6InN0cmluZzNAbWFpbC5jb20ifQ.b4hMpVgiX9CSpndcBUjoR02c2gPyqMPm5XW8D-IE0YIuOjPbQccVyolNk5_d37H1Nyr1-Q3UO6WpSZp9wQ8_Ke_BfExZfITNa5iUOqv1iePSxUP5HWqCXGxTI0iW88CZoTYf7_Nt-EffXVFmqEOrojOKMAXwUWhpbm6oMxIL1BX7sXG1db10PPpzahouXwpVfQcr3zuvw0HhpjSsGzwXqfgHCwHNORbMuy6NBmVkJZMOX1hBZNI-3QkNwUO10TP2VY-qv1dR2JDcw0jZ1kv_f3oPI_sHHuMeBEPgBLCmUS1h2EtA-XXkrvYQjC5T33-Bl4KNa5gYi0KlIe4w9QWJVw"

curl -v -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"serviceId": 3, "directRequest": true}'