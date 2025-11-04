#!/bin/bash

# This script restores Keycloak data from a backup file.

# Usage: ./restore-keycloak.sh <backup-file>

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <backup-file>"
    exit 1
fi

BACKUP_FILE=$1

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Backup file not found: $BACKUP_FILE"
    exit 1
fi

# Restore the Keycloak database
echo "Restoring Keycloak database from $BACKUP_FILE..."

# Replace the following command with the actual command to restore your database
# Example for PostgreSQL:
# docker exec -i <keycloak_postgres_container_name> psql -U <db_user> -d <db_name> < "$BACKUP_FILE"

echo "Keycloak database restored successfully."