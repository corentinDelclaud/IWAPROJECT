#!/bin/bash

# Backup Keycloak data

# Set variables
KEYCLOAK_CONTAINER="keycloak-service"
BACKUP_DIR="./backups"
TIMESTAMP=$(date +"%Y%m%d%H%M%S")
BACKUP_FILE="$BACKUP_DIR/keycloak_backup_$TIMESTAMP.sql"

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Execute the backup command
docker exec $KEYCLOAK_CONTAINER pg_dump -U keycloak -d keycloak > $BACKUP_FILE

# Check if the backup was successful
if [ $? -eq 0 ]; then
  echo "Backup successful! Backup file: $BACKUP_FILE"
else
  echo "Backup failed!"
fi