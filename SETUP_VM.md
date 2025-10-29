# 🚀 Setup Instructions for VM (deti-engsoft-14.ua.pt)

## Quick Start

On your VM, run:

```bash
cd ~/actio
bash setup-backup.sh
```

That's it! The script will set up everything automatically.

## What It Does

1. ✅ Adds `MAX_BACKUPS=3` to your `infra/.env`
2. ✅ Builds and starts the backup container
3. ✅ Tests that backups work
4. ✅ Shows you the results

## Your Current .env Status

✅ **Already configured** (no changes needed):
- `POSTGRES_DB=slms`
- `POSTGRES_USER=slms`
- `POSTGRES_PASSWORD=slms`
- `POSTGRES_PORT=5432`

⚠️ **Will be added** by setup script:
- `MAX_BACKUPS=3`

## After Setup

The backup system will:
- 📅 Run automatically every day at **2:00 AM**
- 💾 Keep only the **last 3 backups**
- 🗑️ Delete older backups automatically
- 🔄 Keep running even during deployments

## Useful Commands

### View backups
```bash
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups
```

### Check backup logs
```bash
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log
```

### Manual backup (if needed)
```bash
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh
```

### Download a backup
```bash
docker cp slms-backup:/backups/backup_slms_20241029_HHMMSS.sql.gz ./
```

## Troubleshooting

### If setup script fails

Check Docker permissions:
```bash
docker ps
```

If you get "permission denied":
```bash
sudo usermod -aG docker sousahugo
# Then log out and back in
```

### Check if backup service is running

```bash
docker compose -f infra/docker-compose.yaml ps backup
```

Should show "Up".

### View detailed logs

```bash
docker compose -f infra/docker-compose.yaml logs backup
```

## Documentation

- **Quick check**: See `VM_QUICK_CHECK.md`
- **Full docs**: See `infra/docker/backup/README.md`
- **CI/CD info**: See `infra/BACKUP_CICD.md`

## Questions?

The backup system is **independent** of CI/CD:
- It's **NOT triggered** by Git pushes
- It runs on a **schedule** (cron)
- It keeps running during deployments

Perfect for keeping your database safe! 🛡️
