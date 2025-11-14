#!/bin/bash

# ==============================================================================
# Script d'arrêt - IWA PROJECT
# ==============================================================================
# Ce script arrête tous les services Docker
# ==============================================================================

set -e

echo "🛑 Arrêt de l'infrastructure IWA Project..."

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Se déplacer dans le répertoire parent (où se trouve docker-compose.yml)
cd "$(dirname "$0")/.."

# Arrêter et supprimer les conteneurs
docker-compose down

echo ""
echo -e "${GREEN}✅ Tous les services ont été arrêtés !${NC}"
echo ""
echo "Pour supprimer également les volumes (données) :"
echo "  docker-compose down -v"
