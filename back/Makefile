# ==============================================================================
# Makefile - IWA PROJECT
# ==============================================================================
# Commandes simplifiées pour gérer l'infrastructure Docker
# ==============================================================================

.PHONY: help start stop restart logs build clean health ps

# Couleurs pour l'affichage
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m # No Color

help: ## Affiche cette aide
	@echo "$(BLUE)╔═══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║         IWA PROJECT - Commandes disponibles               ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-15s$(NC) %s\n", $$1, $$2}'
	@echo ""

start: ## Démarre tous les services
	@echo "$(BLUE)🚀 Démarrage de tous les services...$(NC)"
	@./start-docker.sh

stop: ## Arrête tous les services
	@echo "$(BLUE)🛑 Arrêt de tous les services...$(NC)"
	@docker-compose down

restart: ## Redémarre tous les services
	@echo "$(YELLOW)🔄 Redémarrage de tous les services...$(NC)"
	@docker-compose restart

logs: ## Affiche les logs de tous les services
	@docker-compose logs -f

build: ## Reconstruit toutes les images Docker
	@echo "$(BLUE)🔨 Reconstruction des images...$(NC)"
	@docker-compose build

rebuild: ## Reconstruit et redémarre tous les services
	@echo "$(BLUE)🔨 Reconstruction et redémarrage...$(NC)"
	@docker-compose up -d --build

clean: ## Arrête et supprime tout (conteneurs, volumes, images)
	@echo "$(YELLOW)⚠️  Nettoyage complet...$(NC)"
	@docker-compose down -v
	@docker rmi iwa-api-gateway iwa-auth-service iwa-user-microservice iwa-service-catalog 2>/dev/null || true
	@echo "$(GREEN)✅ Nettoyage terminé$(NC)"

health: ## Vérifie la santé de tous les services
	@./check-health.sh

ps: ## Affiche l'état de tous les conteneurs
	@docker-compose ps

# Commandes spécifiques par service
logs-gateway: ## Logs de l'API Gateway
	@docker-compose logs -f api-gateway

logs-auth: ## Logs du Auth Service
	@docker-compose logs -f auth-service

logs-user: ## Logs du User Microservice
	@docker-compose logs -f user-microservice

logs-catalog: ## Logs du Service Catalog
	@docker-compose logs -f service-catalog

logs-keycloak: ## Logs de Keycloak
	@docker-compose logs -f keycloak

# Commandes pour les bases de données
db-users: ## Se connecte à la base Users
	@docker exec -it iwa-postgres-users psql -U postgres -d iwa_users

db-catalog: ## Se connecte à la base Catalog
	@docker exec -it iwa-postgres-catalog psql -U postgres -d iwa_catalog

db-keycloak: ## Se connecte à la base Keycloak
	@docker exec -it iwa-postgres-keycloak psql -U keycloak -d keycloak
