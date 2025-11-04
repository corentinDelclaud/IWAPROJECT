#!/bin/bash

# Script de démarrage complet de l'environnement IWA avec Keycloak

set -e

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  Démarrage de l'environnement IWA${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# 1. Vérifier Docker
echo -e "${BLUE}[1/5]${NC} Vérification de Docker..."
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}Docker n'est pas en cours d'exécution. Démarrage...${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker OK${NC}"
echo ""

# 2. Build des images si nécessaire
echo -e "${BLUE}[2/5]${NC} Build des images Docker..."
docker-compose -f docker-compose.production.yml build
echo -e "${GREEN}✓ Images construites${NC}"
echo ""

# 3. Démarrage des bases de données
echo -e "${BLUE}[3/5]${NC} Démarrage des bases de données..."
docker-compose -f docker-compose.production.yml up -d postgres keycloak-postgres
echo "Attente de la disponibilité des bases de données (20s)..."
sleep 20
echo -e "${GREEN}✓ Bases de données démarrées${NC}"
echo ""

# 4. Démarrage de Keycloak
echo -e "${BLUE}[4/5]${NC} Démarrage de Keycloak..."
docker-compose -f docker-compose.production.yml up -d keycloak
echo "Attente du démarrage de Keycloak (30s)..."
sleep 30
echo -e "${GREEN}✓ Keycloak démarré${NC}"
echo ""

# 5. Démarrage des services applicatifs
echo -e "${BLUE}[5/5]${NC} Démarrage des services applicatifs..."
docker-compose -f docker-compose.production.yml up -d auth-service user-service api-gateway
echo "Attente du démarrage des services (15s)..."
sleep 15
echo -e "${GREEN}✓ Services démarrés${NC}"
echo ""

# Affichage du statut
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}  Environnement IWA démarré avec succès!${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo "📊 Statut des services:"
docker-compose -f docker-compose.production.yml ps
echo ""
echo "🔗 URLs disponibles:"
echo "  - API Gateway:       http://localhost:8080"
echo "  - Keycloak Admin:    http://localhost:8085/admin"
echo "  - Keycloak Realm:    http://localhost:8085/realms/IWA_NextLevel"
echo ""
echo "🔐 Credentials Keycloak:"
echo "  - Username: admin"
echo "  - Password: admin"
echo ""
echo "📝 Commandes utiles:"
echo "  - Voir les logs:     docker-compose -f docker-compose.production.yml logs -f [service]"
echo "  - Arrêter:           docker-compose -f docker-compose.production.yml down"
echo "  - Redémarrer:        docker-compose -f docker-compose.production.yml restart [service]"
echo ""
