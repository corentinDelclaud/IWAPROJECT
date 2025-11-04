#!/bin/bash

# Script to manage Keycloak service

KEYCLOAK_CONTAINER_NAME="keycloak-service"
POSTGRES_CONTAINER_NAME="keycloak-postgres"

function start() {
    echo "Starting Keycloak and PostgreSQL containers..."
    docker-compose up -d
    echo "Waiting for PostgreSQL to be ready..."
    until docker exec $POSTGRES_CONTAINER_NAME pg_isready -U keycloak; do
        sleep 2
    done
    echo "Starting Keycloak..."
    docker exec $KEYCLOAK_CONTAINER_NAME /opt/keycloak/bin/kc.sh start-dev
}

function stop() {
    echo "Stopping Keycloak and PostgreSQL containers..."
    docker-compose down
}

function restart() {
    stop
    start
}

function status() {
    echo "Checking status of Keycloak and PostgreSQL containers..."
    docker ps -f name=$KEYCLOAK_CONTAINER_NAME
    docker ps -f name=$POSTGRES_CONTAINER_NAME
}

function logs() {
    echo "Fetching logs for Keycloak..."
    docker logs $KEYCLOAK_CONTAINER_NAME
}

function backup_realm() {
    echo "Backing up Keycloak realm..."
    docker exec $KEYCLOAK_CONTAINER_NAME /opt/keycloak/bin/kc.sh export --realm your-realm-name --dir /tmp/export
    docker cp $KEYCLOAK_CONTAINER_NAME:/tmp/export/your-realm-name-realm.json ./keycloak-config/realms/
}

function backup_db() {
    echo "Backing up PostgreSQL database..."
    docker exec $POSTGRES_CONTAINER_NAME pg_dump -U keycloak keycloak > ./backups/keycloak_backup.sql
}

function info() {
    echo "Keycloak Admin URL: http://localhost:8080/auth/admin"
    echo "Keycloak User URL: http://localhost:8080/auth"
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    backup-realm)
        backup_realm
        ;;
    backup-db)
        backup_db
        ;;
    info)
        info
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs|backup-realm|backup-db|info}"
        exit 1
esac

exit 0