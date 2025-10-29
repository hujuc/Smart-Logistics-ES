# VM Setup Guide - Backup System

Quick setup guide for the backup system on your production VM.

## One-Time Setup on VM

### 1. Give CI/CD User Docker Permissions

If your CI/CD pipeline needs to run Docker commands:

```bash
# Replace 'your-user' with your actual username
sudo usermod -aG docker your-user

# Log out and back in for changes to take effect
# Or restart the Docker service
sudo systemctl restart docker
```

### 2. Verify Docker Volume

The backup volume is created automatically, but you can verify:

```bash
docker volume ls | grep backup-data
```

### 3. Set Timezone (Optional but Recommended)

To ensure backups run at the correct local time, add to `infra/.env`:

```bash
# Add this line
TZ=Europe/Lisbon
```

Then update the docker-compose.yaml backup service:

```yaml
services:
  backup:
    environment:
      TZ: ${TZ:-UTC}
```

## Deployment

### First Deployment

```bash
# Clone repository
git clone <your-repo>
cd group-project-es2526_304

# Configure environment
cd infra
cp .env.example .env
nano .env  # Edit as needed

# Start all services
docker compose up -d

# Verify backup service is running
docker compose ps backup
```

### Subsequent Deployments (CI/CD)

Your CI/CD pipeline should:

```bash
# Pull latest code
git pull

# Rebuild and restart services
docker compose -f infra/docker-compose.yaml up -d --build

# The backup container will keep running and maintain its schedule
```

## Verification

### Check Backup Service Status

```bash
# Is the container running?
docker compose -f infra/docker-compose.yaml ps backup

# Check cron is running inside container
docker compose -f infra/docker-compose.yaml exec backup ps | grep crond

# View backup logs
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log
```

### Test Backup Manually

```bash
# Run backup script manually to test
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh

# List backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups
```

## Common Issues

### Issue: "Permission denied" when running Docker commands

**Solution**: Add user to docker group (see step 1 above)

### Issue: Backups not running at scheduled time

**Solution**: Check timezone settings

```bash
# Inside backup container, check current time
docker compose -f infra/docker-compose.yaml exec backup date

# Should match your local time
```

### Issue: Can't see backup files from host

**Solution**: Backups are in a Docker volume, access via container:

```bash
# List backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# Copy to host if needed
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

### Issue: Backup container stops after deployment

**Solution**: Check restart policy

```bash
# Should show "unless-stopped"
docker inspect slms-backup | grep -A 5 RestartPolicy
```

## Monitoring

### Daily Health Check

Add this to your monitoring or create a cron job on the VM:

```bash
#!/bin/bash
# /usr/local/bin/check-backups.sh

# Check if backup container is running
if ! docker ps | grep slms-backup > /dev/null; then
    echo "ERROR: Backup container is not running!"
    exit 1
fi

# Check if we have recent backups (within last 48 hours)
LATEST=$(docker exec slms-backup find /backups -name "backup_*.sql.gz" -mtime -2 | wc -l)
if [ "$LATEST" -eq 0 ]; then
    echo "WARNING: No recent backups found!"
    exit 1
fi

echo "OK: Backup system is healthy"
exit 0
```

Schedule it:

```bash
# Add to crontab
crontab -e

# Add this line (runs daily at 9 AM)
0 9 * * * /usr/local/bin/check-backups.sh
```

## Backup Logs

View backup logs:

```bash
# Last 50 lines of backup log
docker compose -f infra/docker-compose.yaml exec backup tail -50 /var/log/backup.log

# Follow logs in real-time
docker compose -f infra/docker-compose.yaml exec backup tail -f /var/log/backup.log
```

## External Backup Storage (Recommended for Production)

Consider adding automatic upload to external storage:

```bash
# Example: Upload to S3 after each backup
# Add to backup.sh or create a separate script

# Install AWS CLI in container (add to Dockerfile)
RUN apk add --no-cache aws-cli

# In backup script
aws s3 cp "$BACKUP_FILE" s3://your-bucket/backups/
```

## Summary Checklist

- [ ] CI/CD user added to docker group
- [ ] Timezone configured in .env
- [ ] Services deployed and running
- [ ] Backup container is up
- [ ] Cron is running inside backup container
- [ ] Test backup runs successfully
- [ ] Can list and access backups
- [ ] (Optional) Monitoring script set up
- [ ] (Optional) External backup storage configured
