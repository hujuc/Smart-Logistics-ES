# Database Backup System - Quick Summary

## What's Implemented

A simple automatic backup system for your PostgreSQL database using **cron**.

### Key Features

- **Automatic backups** run daily at 2:00 AM using cron (reliable scheduling)
- **Keeps only 3 backups** - automatically deletes older ones
- **Compressed** with gzip to save space
- **Zero maintenance** - just works
- **Independent of CI/CD** - keeps running during deployments

### Files Created/Modified

1. **`infra/docker/backup/Dockerfile`** - Backup service container with cron
2. **`infra/scripts/backup.sh`** - Backup script
3. **`infra/docker-compose.yaml`** - Added backup service
4. **`infra/.env.example`** - Added `MAX_BACKUPS=3` configuration
5. **`infra/docker/backup/README.md`** - Basic documentation
6. **`infra/BACKUP_CICD.md`** - CI/CD integration guide
7. **`infra/VM_SETUP.md`** - VM setup instructions

### How It Works with CI/CD

```
┌─────────────────────────────────────────────────┐
│  GIT PUSH → CI/CD → Deploy                      │
│                      ↓                           │
│           Updates app containers                 │
│                      ↓                           │
│           Backup container keeps running         │
│           (independent, using cron)              │
└─────────────────────────────────────────────────┘
```

**The backup container is NOT triggered by pushes** - it runs on a schedule!

### VM Setup (deti-engsoft-14.ua.pt)

**Automated Setup** - Run this on your VM:

```bash
cd ~/actio
bash setup-backup.sh
```

This script will:
- Add `MAX_BACKUPS=3` to your `.env`
- Build and start the backup service
- Run a test backup
- Verify everything is working

**Manual Setup** (if you prefer):

```bash
cd ~/actio
echo "MAX_BACKUPS=3" >> infra/.env
docker compose -f infra/docker-compose.yaml up -d --build
```

Your existing `.env` already has all the required PostgreSQL variables! ✅

### How to Use

Just start your infrastructure normally:

```bash
cd ~/actio
docker compose -f infra/docker-compose.yaml up -d
```

The backup service will:
- Start automatically
- Use cron to run backups at 2:00 AM daily
- Keep only the last 3 backups
- Clean up old backups automatically
- Keep running even during deployments

### Viewing Backups

```bash
# List backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# Check backup logs
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log

# Check if cron is running
docker compose -f infra/docker-compose.yaml exec backup ps | grep crond
```

### Manual Backup (Optional)

If you want to create a backup immediately (e.g., before a risky deployment):

```bash
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh
```

### If You Need to Restore

```bash
# 1. See available backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# 2. Stop services
docker compose -f infra/docker-compose.yaml stop backend admin-console customer-portal driver-pwa

# 3. Restore (replace YYYYMMDD_HHMMSS with your backup timestamp)
docker compose -f infra/docker-compose.yaml exec backup sh -c '
  gunzip -c /backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz | PGPASSWORD=$POSTGRES_PASSWORD psql -h postgres -U $POSTGRES_USER -d $POSTGRES_DB
'

# 4. Restart services
docker compose -f infra/docker-compose.yaml start backend admin-console customer-portal driver-pwa
```

### Download a Backup

```bash
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

## Configuration

In `infra/.env`:

```bash
# How many backups to keep (default: 3)
MAX_BACKUPS=3

# Optional: Set timezone for correct scheduling
TZ=Europe/Lisbon
```

## Permissions

No `sudo` needed in the container! Docker handles permissions automatically for volumes.

Only need `sudo` on the VM host if your CI/CD user isn't in the docker group yet.

## That's It!

No manual backups needed. The system handles everything automatically using cron.
