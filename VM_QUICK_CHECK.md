# Quick Check - VM Backup System

## Your Current .env File

Based on your configuration, you have the correct variables:

✅ `POSTGRES_DB=slms`
✅ `POSTGRES_USER=slms`
✅ `POSTGRES_PASSWORD=slms`
✅ `POSTGRES_PORT=5432`

## What You Need to Add

Add this line to your `/home/sousahugo/actio/.env` file:

```bash
# Backup Configuration
MAX_BACKUPS=3
```

## Quick VM Commands

### 1. Add MAX_BACKUPS to .env

```bash
cd ~/actio
echo "MAX_BACKUPS=3" >> infra/.env
```

### 2. Deploy/Restart Services

```bash
cd ~/actio
docker compose -f infra/docker-compose.yaml up -d --build
```

### 3. Verify Backup Service is Running

```bash
docker compose -f infra/docker-compose.yaml ps backup
```

Expected output:
```
NAME           IMAGE              STATUS
slms-backup    ...                Up X seconds
```

### 4. Check Backup Service Logs

```bash
docker compose -f infra/docker-compose.yaml logs backup
```

Should see:
```
Backup service started. Automatic backups scheduled daily at 2:00 AM
Keeping last 3 backups only
```

### 5. Test Manual Backup (Optional)

```bash
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh
```

### 6. List Backups

```bash
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups
```

### 7. Check Cron is Running

```bash
docker compose -f infra/docker-compose.yaml exec backup ps aux | grep crond
```

Should see a `crond` process running.

### 8. View Backup Logs

```bash
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log
```

## Environment Variables Used by Backup Service

From your `.env`, the backup service will use:

- `POSTGRES_HOST=postgres` (set in docker-compose.yaml)
- `POSTGRES_DB=slms` ✅ (from your .env)
- `POSTGRES_USER=slms` ✅ (from your .env)
- `POSTGRES_PASSWORD=slms` ✅ (from your .env)
- `POSTGRES_PORT=5432` ✅ (from your .env)
- `MAX_BACKUPS=3` (needs to be added)

## Complete Setup Commands

Run these on your VM:

```bash
# Go to project directory
cd ~/actio

# Add backup configuration
echo "MAX_BACKUPS=3" >> infra/.env

# Rebuild and restart services
docker compose -f infra/docker-compose.yaml up -d --build

# Wait a few seconds
sleep 5

# Check backup service
docker compose -f infra/docker-compose.yaml ps backup

# Test manual backup
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh

# List backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups
```

## Expected Output

After running the manual backup test, you should see:

```
==========================================
Starting database backup
==========================================
Host: postgres
Database: slms
Timestamp: 20241029_HHMMSS
==========================================
Waiting for database...
Database available. Creating backup...
==========================================
✓ Backup created successfully!
File: /backups/backup_slms_20241029_HHMMSS.sql.gz
Size: XX.XM
==========================================
Cleaning old backups (keeping last 3)...
==========================================
Available backups: 1
==========================================
-rw-r--r--    1 root     root        XX.XM Oct 29 HH:MM backup_slms_20241029_HHMMSS.sql.gz
==========================================
Backup completed successfully!
```

## Troubleshooting

### If backup service doesn't start:

```bash
# Check logs
docker compose -f infra/docker-compose.yaml logs backup

# Check if image was built
docker images | grep backup

# Rebuild
docker compose -f infra/docker-compose.yaml build backup
docker compose -f infra/docker-compose.yaml up -d backup
```

### If database connection fails:

```bash
# Check PostgreSQL is running
docker compose -f infra/docker-compose.yaml ps postgres

# Test connection from backup container
docker compose -f infra/docker-compose.yaml exec backup \
  pg_isready -h postgres -U slms -d slms
```

### If permission issues:

```bash
# Check volume
docker volume inspect backup-data

# If needed, recreate volume
docker compose -f infra/docker-compose.yaml down
docker volume rm backup-data
docker compose -f infra/docker-compose.yaml up -d
```

## Verify Automatic Backups

After 24 hours (or at 2:00 AM), check:

```bash
# See if new backup was created
docker compose -f infra/docker-compose.yaml exec backup ls -lht /backups

# Check logs
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log
```

## Download Backup to Local Machine

From your local machine (not VM):

```bash
# SSH to VM and copy backup
scp sousahugo@deti-engsoft-14.ua.pt:~/actio/backup_slms_*.sql.gz ./

# Or use docker cp via SSH
ssh sousahugo@deti-engsoft-14.ua.pt "docker cp slms-backup:/backups/backup_slms_20241029_HHMMSS.sql.gz /tmp/"
scp sousahugo@deti-engsoft-14.ua.pt:/tmp/backup_slms_20241029_HHMMSS.sql.gz ./
```

## Summary

1. ✅ Your `.env` has all required PostgreSQL variables
2. ⚠️ Need to add `MAX_BACKUPS=3` to `.env`
3. 🚀 Deploy/restart with `docker compose up -d --build`
4. ✅ Test with manual backup
5. 📅 Automatic backups run daily at 2:00 AM
