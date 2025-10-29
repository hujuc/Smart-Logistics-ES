# Database Backup System

Automatic PostgreSQL backup system for SLMS database.

## Features

- ✅ **Automatic daily backups** at 2:00 AM
- ✅ **Automatic cleanup** - keeps only the last 3 backups
- ✅ **Compressed backups** (gzip) to save space
- ✅ **Persistent storage** in Docker volume

## Configuration

The backup system is configured via environment variables in `infra/.env`:

```bash
# Maximum number of backups to keep
MAX_BACKUPS=3

# Optional: Set timezone for correct backup scheduling
# TZ=Europe/Lisbon
```

## How It Works

1. **Backup Service**: A Docker container (`slms-backup`) runs continuously as a daemon
2. **Cron Scheduler**: Uses cron to run backups at 2:00 AM daily (more reliable than while loops)
3. **Storage**: Backups are stored in a Docker volume (`backup-data`) that persists across deployments
4. **Cleanup**: After each backup, old backups are automatically deleted, keeping only the last 3
5. **Naming**: Backups are named `backup_slms_YYYYMMDD_HHMMSS.sql.gz`


## Viewing Backups

To see available backups:

```bash
# List all backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# Check backup logs
docker compose -f infra/docker-compose.yaml logs backup
```

## Restoring a Backup

If you need to restore from a backup:

```bash
# 1. List available backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# 2. Stop services that use the database
docker compose -f infra/docker-compose.yaml stop backend admin-console customer-portal driver-pwa

# 3. Restore the backup
docker compose -f infra/docker-compose.yaml exec backup sh -c '
  BACKUP_FILE=backup_slms_YYYYMMDD_HHMMSS.sql.gz
  gunzip -c /backups/$BACKUP_FILE | PGPASSWORD=$POSTGRES_PASSWORD psql -h postgres -U $POSTGRES_USER -d $POSTGRES_DB
'

# 4. Restart services
docker compose -f infra/docker-compose.yaml start backend admin-console customer-portal driver-pwa
```

## Download Backup

To download a backup to your local machine:

```bash
# Copy backup from container to local machine
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

## Disaster Recovery

If the VM is lost:

1. Set up a new VM with Docker
2. Clone the repository
3. Copy your backup file to the new VM
4. Start the infrastructure: `docker compose -f infra/docker-compose.yaml up -d`
5. Copy backup to container: `docker cp backup_slms_YYYYMMDD_HHMMSS.sql.gz slms-backup:/backups/`
6. Restore using the commands above

## Monitoring

Check backup service status:

```bash
# Check if backup service is running
docker compose -f infra/docker-compose.yaml ps backup

# View recent logs
docker compose -f infra/docker-compose.yaml logs --tail=50 backup

# Check disk space used by backups
docker compose -f infra/docker-compose.yaml exec backup du -sh /backups
```

## Troubleshooting

### Backup service not running

```bash
# Check logs
docker compose -f infra/docker-compose.yaml logs backup

# Restart service
docker compose -f infra/docker-compose.yaml restart backup
```

### Database connection issues

```bash
# Test database connection
docker compose -f infra/docker-compose.yaml exec backup \
  pg_isready -h postgres -U $POSTGRES_USER -d $POSTGRES_DB
```

### Check backup integrity

```bash
# Verify a backup file is not corrupted
docker compose -f infra/docker-compose.yaml exec backup \
  gunzip -t /backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz
```

## Important Notes

- Backups are stored in a Docker volume and persist even if containers are removed
- Only the last 3 backups are kept to save disk space
- For production, consider also storing backups externally (cloud storage, external drive, etc.)
- The backup process does not lock the database - the application continues to run
- Backups include the complete database schema and data
