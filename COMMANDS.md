# Quick Reference - Backup Commands

## Setup (Run Once)
```bash
cd ~/actio
bash setup-backup.sh
```

## Daily Use

### List backups
```bash
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups
```

### Check logs
```bash
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log
```

### Check service status
```bash
docker compose -f infra/docker-compose.yaml ps backup
```

### Manual backup
```bash
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh
```

## Download Backup

### Copy to VM home directory
```bash
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ~/
```

### Download to local machine (from your laptop)
```bash
scp sousahugo@deti-engsoft-14.ua.pt:~/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

## Restore Backup

### 1. List available backups
```bash
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups
```

### 2. Stop application services
```bash
docker compose -f infra/docker-compose.yaml stop backend admin-console customer-portal driver-pwa
```

### 3. Restore (replace YYYYMMDD_HHMMSS with actual timestamp)
```bash
docker compose -f infra/docker-compose.yaml exec backup sh -c '
  gunzip -c /backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz | \
  PGPASSWORD=$POSTGRES_PASSWORD psql -h postgres -U slms -d slms
'
```

### 4. Restart services
```bash
docker compose -f infra/docker-compose.yaml start backend admin-console customer-portal driver-pwa
```

## Monitoring

### Check cron is running
```bash
docker compose -f infra/docker-compose.yaml exec backup ps aux | grep crond
```

### Check disk space
```bash
docker compose -f infra/docker-compose.yaml exec backup du -sh /backups
```

### View container logs
```bash
docker compose -f infra/docker-compose.yaml logs --tail=50 backup
```

## Troubleshooting

### Restart backup service
```bash
docker compose -f infra/docker-compose.yaml restart backup
```

### Rebuild backup service
```bash
docker compose -f infra/docker-compose.yaml build backup
docker compose -f infra/docker-compose.yaml up -d backup
```

### Check database connection
```bash
docker compose -f infra/docker-compose.yaml exec backup pg_isready -h postgres -U slms -d slms
```

## Important Notes

- ⏰ Automatic backups run at **2:00 AM** daily
- 💾 Only **last 3 backups** are kept
- 🔄 Backup service keeps running during deployments
- 🚫 **NOT triggered** by Git pushes (runs on schedule)

---

**Your VM**: deti-engsoft-14.ua.pt  
**Project Path**: ~/actio  
**User**: sousahugo
