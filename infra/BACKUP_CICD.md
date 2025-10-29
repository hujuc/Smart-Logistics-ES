# Database Backup in CI/CD Pipeline

## How It Works

### In Production (VM)

The backup system runs **independently** of CI/CD:

1. **Docker Container**: `slms-backup` container runs continuously as a daemon
2. **Cron Schedule**: Uses cron to execute backups daily at 2:00 AM
3. **Automatic Cleanup**: Keeps only the last 3 backups
4. **Persistent Storage**: Backups are stored in a Docker volume that persists across deployments

### Relationship with CI/CD

The backup system is **NOT triggered by Git pushes**. Here's why:

```
┌─────────────────────────────────────────────────────────────┐
│                                                               │
│  GIT PUSH → CI/CD Pipeline → Deploy Application              │
│                                    ↓                          │
│                          Updates containers                   │
│                          (backend, frontend, etc)             │
│                                    ↓                          │
│                          Backup container keeps running       │
│                          (unchanged, independent)             │
│                                                               │
└─────────────────────────────────────────────────────────────┘

Meanwhile, in the background:
┌─────────────────────────────────────────────────────────────┐
│  Backup Container (always running)                            │
│  └─ Cron: 2:00 AM daily → backup.sh → Save to volume        │
└─────────────────────────────────────────────────────────────┘
```

### Why This Design?

✅ **Advantages**:
- Backups run even without deployments
- No risk of missing backups if CI/CD is slow
- Simpler to maintain
- Works independently of code changes

❌ **Disadvantages**:
- Not triggered by deployments
- Requires container to always run

## Permissions in CI/CD

### Docker User Permissions

The backup container runs as root inside the container, but Docker handles permissions:

```yaml
volumes:
  - backup-data:/backups  # Docker-managed volume, no permission issues
```

### On the VM Host

If your CI/CD pipeline needs to:
- Access backups from the host
- Copy backups to external storage
- Run backup commands

You may need to add the CI/CD user to the `docker` group:

```bash
# On the VM (one-time setup)
sudo usermod -aG docker <ci-cd-user>
```

### Example CI/CD Integration

If you want to **download backups** after deployment:

```yaml
# .github/workflows/deploy.yml or similar
steps:
  # ... deployment steps ...
  
  - name: Backup database after deployment
    run: |
      # Wait for services to be ready
      sleep 30
      
      # Trigger a backup (optional, for post-deployment safety)
      docker compose -f infra/docker-compose.yaml exec -T backup /usr/local/bin/backup.sh
      
      # Download latest backup to CI/CD artifacts
      LATEST_BACKUP=$(docker compose -f infra/docker-compose.yaml exec -T backup ls -t /backups | head -1)
      docker cp slms-backup:/backups/$LATEST_BACKUP ./backup-latest.sql.gz
  
  - name: Upload backup to artifact storage (optional)
    run: |
      # Upload to S3, Google Cloud Storage, etc.
      # aws s3 cp ./backup-latest.sql.gz s3://your-bucket/backups/
```

## Recommended Setup

### Option 1: Current Setup (Simplest)
- ✅ Backup container runs independently
- ✅ Cron schedules at 2:00 AM
- ✅ No CI/CD integration needed
- **Best for**: Most projects

### Option 2: Post-Deployment Backup (Safer)
Add to your CI/CD pipeline:

```yaml
# After successful deployment
- name: Create post-deployment backup
  run: |
    docker compose -f infra/docker-compose.yaml exec -T backup /usr/local/bin/backup.sh
```

This creates a backup immediately after each deployment.

### Option 3: External Backup Storage
Add to backup script or CI/CD:

```bash
# In backup.sh or as a separate CI/CD step
# Upload to cloud storage
aws s3 cp /backups/backup_slms_*.sql.gz s3://your-bucket/backups/

# Or use rclone, gsutil, etc.
```

## Common CI/CD Issues and Solutions

### Issue 1: Permission Denied

```bash
# Error: Got permission denied while trying to connect to Docker daemon
# Solution: Add user to docker group
sudo usermod -aG docker $USER
```

### Issue 2: Volume Not Persisting

```yaml
# Make sure volume is declared in docker-compose.yaml
volumes:
  backup-data:  # This line is important!
```

### Issue 3: Container Stops After Deployment

```yaml
# Ensure restart policy is set
services:
  backup:
    restart: unless-stopped  # Important!
```

### Issue 4: Timezone Issues

```yaml
# Add timezone to backup container
services:
  backup:
    environment:
      TZ: Europe/Lisbon  # Adjust for your timezone
```

## Monitoring in CI/CD

Add health checks to your CI/CD:

```bash
# Check if backup service is running
docker compose -f infra/docker-compose.yaml ps backup | grep "Up"

# Check last backup date
docker compose -f infra/docker-compose.yaml exec -T backup ls -lht /backups | head -n 2

# Verify cron is running
docker compose -f infra/docker-compose.yaml exec -T backup ps | grep crond
```

## Example: Complete CI/CD Integration

```yaml
name: Deploy with Backup Safety

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v2
      
      - name: Setup SSH
        # ... your SSH setup ...
      
      - name: Pre-deployment backup
        run: |
          ssh user@vm 'cd /path/to/project && docker compose -f infra/docker-compose.yaml exec -T backup /usr/local/bin/backup.sh'
      
      - name: Deploy application
        run: |
          ssh user@vm 'cd /path/to/project && git pull && docker compose -f infra/docker-compose.yaml up -d --build'
      
      - name: Verify backup service
        run: |
          ssh user@vm 'docker compose -f /path/to/project/infra/docker-compose.yaml ps backup | grep Up'
      
      - name: Download backup (optional)
        run: |
          LATEST=$(ssh user@vm 'docker compose -f /path/to/project/infra/docker-compose.yaml exec -T backup ls -t /backups | head -1')
          scp user@vm:/path/to/project/infra/backups/$LATEST ./
```

## Summary

- ✅ Backup container runs **independently** using cron
- ✅ **Not triggered** by Git pushes (runs on schedule)
- ✅ Docker handles permissions automatically
- ✅ Add `sudo usermod -aG docker <user>` if CI/CD needs Docker access
- ✅ Optionally add backup steps to CI/CD for extra safety
- ✅ Consider external backup storage for production
