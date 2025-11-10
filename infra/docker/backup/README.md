# Database Backup System

Automatic PostgreSQL backup system for the SLMS database.

## Features

- ✅ **Automatic backups** on every push to `main` (pre- and post-deploy)
- ✅ **Automatic cleanup** – only the last 4 dumps are kept
- ✅ **Compressed backups** (gzip) to save space
- ✅ **Persistent storage** in a Docker volume

## Configuration

The backup system is configured via environment variables in `infra/.env`:

```bash
# Maximum number of backups to keep
MAX_BACKUPS=4

# Run manual backups the same way the CI does
```

## How It Works

1. **Backup Service**: the `slms-backup` container runs idle (a `sleep` process).
2. **Workflow**: when there is a push to `main`, the `Deploy project` job runs the script twice (pre- and post-deploy) using `docker compose exec backup /usr/local/bin/backup.sh`.
3. **Storage**: Backups are stored in the `backup-data` volume and are additionally copied to `./backups` on the runner.
4. **Cleanup**: After each run only the last 4 files are retained.
5. **Naming**: `backup_<db>_<timestamp>[_pre|post-<sha>].sql.gz`.

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

