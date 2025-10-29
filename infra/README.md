# Infrastructure Setup

## Quick Start

### Development Environment

```bash
# From project root
make dev-up        # Start services
make dev-logs      # View logs
make dev-down      # Stop services
```

### Production Environment

```bash
# From project root 
cp infra/.env.example infra/.env  # Configure environment
make prod-up       # Start services
make prod-logs     # View logs
make prod-down     # Stop services
```

## Environment Configuration

### Backend (`src/backend/.env`)

Configure database and JPA settings:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JPA_DDL_AUTO`, `JPA_SHOW_SQL`, `JPA_FORMAT_SQL`

### Infrastructure (`infra/.env`)

Configure for production deployment:

- PostgreSQL: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
- Backend: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `BACKEND_PORT`
- Node: `NODE_ENV`

## Access URLs

| Service | URL |
|---------|-----|
| Admin Console | <http://localhost/admin> |
| Customer Portal | <http://localhost/customer> |
| Driver PWA | <http://localhost/driver> |
| Backend API | <http://localhost/api/> |

## Available Commands

```bash
make help          # Show all commands
make dev-restart   # Restart development services
make prod-restart  # Restart production services
make clean         # Remove containers and volumes
```

## Database Backup System

The system includes automatic database backups to protect against data loss.

### Features

- ✅ **Automatic daily backups** at 2:00 AM using cron
- ✅ **Automatic cleanup** - keeps only the last 3 backups
- ✅ **Compressed backups** (gzip)
- ✅ **Persistent storage** in Docker volume
- ✅ **Independent of CI/CD** - keeps running during deployments

### View Backups

```bash
# List available backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# Check backup logs
docker compose -f infra/docker-compose.yaml exec backup cat /var/log/backup.log
```

### Download Backup

```bash
# Download a backup to your local machine
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

### Documentation

- **Basic usage**: [docker/backup/README.md](docker/backup/README.md)
- **CI/CD integration**: [BACKUP_CICD.md](BACKUP_CICD.md)
- **VM setup guide**: [VM_SETUP.md](VM_SETUP.md)

## Services

- **PostgreSQL** (port 5433): Database
- **Backup**: Automated database backup service
- **Backend** (port 8080): Spring Boot API
- **Frontend Apps** (port 3000 each): Next.js applications
- **Nginx** (port 80): Reverse proxy
- **Keycloak**: Authentication and authorization
- **MinIO**: Object storage
