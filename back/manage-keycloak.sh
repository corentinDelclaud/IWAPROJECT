#!/bin/bash

# Script de gestion de Keycloak avec base de données PostgreSQL
# Ce script facilite le démarrage, l'arrêt et la gestion de Keycloak

set -e

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
COMPOSE_FILE="docker-compose.production.yml"
PROJECT_NAME="iwa"

# Fonction pour afficher les messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Fonction pour vérifier si Docker est en cours d'exécution
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker n'est pas en cours d'exécution. Veuillez démarrer Docker."
        exit 1
    fi
    print_success "Docker est en cours d'exécution"
}

# Fonction pour démarrer Keycloak et ses dépendances
start_keycloak() {
    print_info "Démarrage de Keycloak avec PostgreSQL..."
    
    # Démarrer la base de données Keycloak en premier
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d keycloak-postgres
    
    print_info "Attente du démarrage de la base de données Keycloak..."
    sleep 10
    
    # Vérifier que la base de données est prête
    until docker exec iwa-keycloak-postgres pg_isready -U keycloak -d keycloak > /dev/null 2>&1; do
        print_info "En attente de la base de données Keycloak..."
        sleep 2
    done
    
    print_success "Base de données Keycloak prête"
    
    # Démarrer Keycloak
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d keycloak
    
    print_info "Attente du démarrage de Keycloak..."
    sleep 15
    
    # Vérifier que Keycloak est prêt
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker exec iwa-keycloak curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; then
            print_success "Keycloak est prêt et opérationnel!"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_error "Keycloak n'a pas démarré correctement après $max_attempts tentatives"
            print_info "Vérifiez les logs avec: docker logs iwa-keycloak"
            exit 1
        fi
        
        print_info "Tentative $attempt/$max_attempts - En attente de Keycloak..."
        sleep 5
        ((attempt++))
    done
    
    print_success "Keycloak est accessible à: http://localhost:8085"
    print_info "Console d'administration: http://localhost:8085/admin"
    print_info "Credentials: admin / admin (changez-les en production!)"
}

# Fonction pour arrêter Keycloak
stop_keycloak() {
    print_info "Arrêt de Keycloak et de sa base de données..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME stop keycloak keycloak-postgres
    print_success "Keycloak et sa base de données sont arrêtés"
}

# Fonction pour redémarrer Keycloak
restart_keycloak() {
    print_info "Redémarrage de Keycloak..."
    stop_keycloak
    start_keycloak
}

# Fonction pour afficher les logs
show_logs() {
    local service=${1:-keycloak}
    print_info "Affichage des logs de $service..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f $service
}

# Fonction pour afficher le statut
show_status() {
    print_info "Statut des services Keycloak:"
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME ps keycloak keycloak-postgres
    
    echo ""
    print_info "Santé de Keycloak:"
    if docker exec iwa-keycloak curl -sf http://localhost:8080/health > /dev/null 2>&1; then
        print_success "Keycloak: Healthy"
    else
        print_error "Keycloak: Not healthy"
    fi
    
    echo ""
    print_info "Santé de la base de données:"
    if docker exec iwa-keycloak-postgres pg_isready -U keycloak -d keycloak > /dev/null 2>&1; then
        print_success "PostgreSQL (Keycloak): Healthy"
    else
        print_error "PostgreSQL (Keycloak): Not healthy"
    fi
}

# Fonction pour créer une sauvegarde du realm
backup_realm() {
    local backup_dir="./backups"
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$backup_dir/keycloak_realm_backup_$timestamp.json"
    
    mkdir -p $backup_dir
    
    print_info "Création d'une sauvegarde du realm..."
    
    # Export du realm via l'API admin de Keycloak
    docker exec iwa-keycloak /opt/keycloak/bin/kc.sh export \
        --dir /tmp/export \
        --realm IWA_NextLevel \
        --users realm_file
    
    docker cp iwa-keycloak:/tmp/export/IWA_NextLevel-realm.json $backup_file
    
    print_success "Sauvegarde créée: $backup_file"
}

# Fonction pour sauvegarder la base de données
backup_database() {
    local backup_dir="./backups"
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="$backup_dir/keycloak_db_backup_$timestamp.sql"
    
    mkdir -p $backup_dir
    
    print_info "Création d'une sauvegarde de la base de données..."
    
    docker exec iwa-keycloak-postgres pg_dump -U keycloak -d keycloak > $backup_file
    
    print_success "Sauvegarde de la base de données créée: $backup_file"
}

# Fonction pour nettoyer les volumes (ATTENTION: supprime toutes les données)
clean_volumes() {
    print_warning "ATTENTION: Cette action supprimera TOUTES les données de Keycloak!"
    read -p "Êtes-vous sûr de vouloir continuer? (yes/no): " confirmation
    
    if [ "$confirmation" = "yes" ]; then
        print_info "Arrêt des services..."
        docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down
        
        print_info "Suppression des volumes..."
        docker volume rm ${PROJECT_NAME}_keycloak_postgres_data ${PROJECT_NAME}_keycloak_data 2>/dev/null || true
        
        print_success "Volumes supprimés"
    else
        print_info "Opération annulée"
    fi
}

# Fonction pour afficher les informations de connexion
show_info() {
    echo ""
    print_info "=== Informations de connexion Keycloak ==="
    echo ""
    echo "URL Console Admin: http://localhost:8085/admin"
    echo "URL Realm IWA_NextLevel: http://localhost:8085/realms/IWA_NextLevel"
    echo "Admin Username: admin"
    echo "Admin Password: admin (changez en production!)"
    echo ""
    print_info "=== Base de données Keycloak ==="
    echo ""
    echo "Host: keycloak-postgres (interne) / localhost:5432 (externe si exposé)"
    echo "Database: keycloak"
    echo "Username: keycloak"
    echo "Password: keycloak_secure_password (changez en production!)"
    echo ""
    print_warning "IMPORTANT: Changez tous les mots de passe par défaut en production!"
    echo ""
}

# Fonction pour afficher l'aide
show_help() {
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commandes disponibles:"
    echo "  start           - Démarrer Keycloak et sa base de données"
    echo "  stop            - Arrêter Keycloak et sa base de données"
    echo "  restart         - Redémarrer Keycloak"
    echo "  logs [service]  - Afficher les logs (keycloak ou keycloak-postgres)"
    echo "  status          - Afficher le statut des services"
    echo "  backup-realm    - Créer une sauvegarde du realm"
    echo "  backup-db       - Créer une sauvegarde de la base de données"
    echo "  clean           - Supprimer tous les volumes (DESTRUCTIF!)"
    echo "  info            - Afficher les informations de connexion"
    echo "  help            - Afficher cette aide"
    echo ""
}

# Menu principal
main() {
    check_docker
    
    case "${1:-}" in
        start)
            start_keycloak
            show_info
            ;;
        stop)
            stop_keycloak
            ;;
        restart)
            restart_keycloak
            show_info
            ;;
        logs)
            show_logs ${2:-keycloak}
            ;;
        status)
            show_status
            ;;
        backup-realm)
            backup_realm
            ;;
        backup-db)
            backup_database
            ;;
        clean)
            clean_volumes
            ;;
        info)
            show_info
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Commande invalide: ${1:-}"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
