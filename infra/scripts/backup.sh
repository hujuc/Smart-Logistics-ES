#!/bin/bash

# Automatic PostgreSQL Backup Script
# Creates database backups and keeps only the last N backups

set -e

# Environment variables with defaults
POSTGRES_HOST=${POSTGRES_HOST:-postgres}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_DB=${POSTGRES_DB:-slms}
POSTGRES_USER=${POSTGRES_USER:-postgres}
BACKUP_DIR=${BACKUP_DIR:-/backups}
MAX_BACKUPS=${MAX_BACKUPS:-4}

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Generate backup filename with timestamp and optional label
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LABEL_SUFFIX=""
SANITIZED_LABEL=""
if [ -n "${BACKUP_LABEL:-}" ]; then
    SANITIZED_LABEL=$(printf '%s' "$BACKUP_LABEL" | tr -c 'A-Za-z0-9_-.' '-')
    if [ -n "$SANITIZED_LABEL" ]; then
        LABEL_SUFFIX="_$SANITIZED_LABEL"
    fi
fi
BACKUP_FILE="$BACKUP_DIR/backup_${POSTGRES_DB}_${TIMESTAMP}${LABEL_SUFFIX}.sql.gz"

echo "=========================================="
echo "Starting database backup"
echo "=========================================="
echo "Host: $POSTGRES_HOST"
echo "Database: $POSTGRES_DB"
echo "Timestamp: $TIMESTAMP"
if [ -n "$SANITIZED_LABEL" ]; then
    echo "Label: $SANITIZED_LABEL"
else
    echo "Label: (none)"
fi
echo "=========================================="

# Wait for database to be available
echo "Waiting for database..."
until PGPASSWORD=$POSTGRES_PASSWORD pg_isready -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" > /dev/null 2>&1; do
    echo "Database not ready. Retrying in 5 seconds..."
    sleep 5
done

echo "Database available. Creating backup..."

# Create compressed backup
PGPASSWORD=$POSTGRES_PASSWORD pg_dump \
    -h "$POSTGRES_HOST" \
    -p "$POSTGRES_PORT" \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
    --no-owner \
    --no-acl \
    --format=plain \
    | gzip > "$BACKUP_FILE"

# Check if backup was created successfully
if [ -f "$BACKUP_FILE" ]; then
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "=========================================="
    echo "✓ Backup created successfully!"
    echo "File: $BACKUP_FILE"
    echo "Size: $BACKUP_SIZE"
    echo "=========================================="
else
    echo "=========================================="
    echo "✗ Backup failed!"
    echo "=========================================="
    exit 1
fi

# Keep only the last MAX_BACKUPS backups
echo "Cleaning old backups (keeping last $MAX_BACKUPS)..."
ls -t "$BACKUP_DIR"/backup_${POSTGRES_DB}_*.sql.gz 2>/dev/null | tail -n +$((MAX_BACKUPS + 1)) | xargs -r rm -f

# List available backups
BACKUP_COUNT=$(ls -1 "$BACKUP_DIR"/backup_${POSTGRES_DB}_*.sql.gz 2>/dev/null | wc -l)
echo "=========================================="
echo "Available backups: $BACKUP_COUNT"
echo "=========================================="
ls -lh "$BACKUP_DIR"/backup_${POSTGRES_DB}_*.sql.gz 2>/dev/null || echo "No backups found."
echo "=========================================="

echo "Backup completed successfully!"
