#!/bin/bash

# Script pour appliquer toutes les corrections Keycloak
# Usage: ./apply-keycloak-fixes.sh

echo "======================================"
echo "Application des corrections Keycloak"
echo "======================================"
echo ""

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Vérifier qu'on est dans le bon répertoire
if [ ! -f "docker-compose.production.yml" ]; then
    echo "Erreur: Ce script doit être exécuté depuis le répertoire /back"
    exit 1
fi

echo -e "${BLUE}Étape 1: Arrêt des services${NC}"
docker-compose -f docker-compose.production.yml down
echo ""

echo -e "${BLUE}Étape 2: Reconstruction des images${NC}"
docker-compose -f docker-compose.production.yml build
echo ""

echo -e "${BLUE}Étape 3: Démarrage des services${NC}"
docker-compose -f docker-compose.production.yml up -d
echo ""

echo -e "${YELLOW}Attente du démarrage de tous les services (60 secondes)...${NC}"
for i in {60..1}; do
    echo -ne "\rTemps restant: $i secondes "
    sleep 1
done
echo ""
echo ""

echo -e "${BLUE}Étape 4: Vérification des services${NC}"
./test-services.sh
echo ""

echo -e "${GREEN}======================================"
echo "Configuration terminée !"
echo "======================================${NC}"
echo ""
echo "Prochaines étapes manuelles:"
echo ""
echo "1. Accédez à la console Keycloak:"
echo "   http://localhost:9090"
echo "   Username: admin"
echo "   Password: admin"
echo ""
echo "2. Vérifiez la configuration du client:"
echo "   - Allez dans le realm 'IWA_NextLevel'"
echo "   - Menu: Clients → user-microservice"
echo "   - Vérifiez que les Redirect URIs incluent:"
echo "     • http://localhost:19000/*"
echo "     • http://localhost:*/*"
echo "     • exp://*"
echo ""
echo "3. Testez l'authentification depuis votre frontend:"
echo "   http://localhost:19000"
echo ""
echo "4. Si problème de cookies persiste, consultez:"
echo "   - FRONTEND_KEYCLOAK_FIX.md (Solution via API Gateway)"
echo "   - QUICK_START_GUIDE.md (Guide complet)"
echo ""
