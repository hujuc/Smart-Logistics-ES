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

## Directory Structure

The infrastructure directory is organized as follows:

- **backup_management/** - Database backup and restore scripts (backup.sh, restore.sh, setup-backup.sh)
- **docker/** - Dockerfiles for containerizing services
  - `backend/` - Spring Boot application Dockerfiles (dev & prod)
  - `frontend/` - Next.js application Dockerfiles (dev & prod)
  - `backup/` - Backup service configuration and scripts
- **keycloak/** - Keycloak realm configuration and setup (users, roles, clients)
- **nginx/** - Nginx reverse proxy configuration for load balancing and routing
- **scripts/** - Automation scripts
  - `seed_demo_data.py` - Populates database with realistic demo data
  - `monitor_prod.py` - Production monitoring utilities
- **terraform/** - Infrastructure as Code (IaC) for managing Docker containers (PostgreSQL, Backend, Frontends)
- **docker-compose.yaml** - External services orchestration (Keycloak, MinIO, Nginx)
- **docker-compose.dev.yml** - Development environment configuration
- **.env** - Environment variables (secrets, API keys, database credentials)
- **deploy.sh** - Deployment automation script

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

- ✅ **Backups automáticos** em cada push para `main` (pré e pós deploy)
- ✅ **Limpeza automática** – mantém apenas os últimos 4 ficheiros
- ✅ **Comprimido** (gzip) e com labels por deploy
- ✅ **Podem ser disparados manualmente** via `docker compose exec backup ...`

### View Backups

```bash
# List available backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# Copy the latest pre/post backups produced pelo workflow
ls -lh backups/
```

### Download Backup

```bash
# Download a backup to your local machine
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

### Documentation

- **Basic usage**: [docker/backup/README.md](docker/backup/README.md)

## Demo Data Seeder

Need realistic demo data (warehouses, orders, shipments, Keycloak users)? Use the automation script under `infra/scripts`.

```bash
# Populate ~10 records for each entity (uses real ShipEngine/Shippo APIs)
python infra/scripts/seed_demo_data.py \
  --warehouses 10 \
  --orders 10 \
  --shipments 10 \
  --users 10
```

**Requirements**
- `make up` running (backend, Keycloak, MinIO, Shippo keys, etc.)
- Shippo/ShipEngine API keys configured in `infra/.env` (script triggers real carrier sync + label generation)
- Python 3.11+

**What it does**
- Registers demo users (customer, driver, warehouse staff, CSR, manager) via `/api/landing-page/auth/register`
- Imports carriers through `/api/admin/carriers/integrations/sync`
- Creates warehouses, orders, and shipments using `/api/admin/**` endpoints
- Assigns orders to shipments so that `ShipmentLabelGenerationService` hits Shippo and stores PDFs in MinIO

Pass `--api-base-url` if you expose the backend differently (defaults to `http://localhost:8080/api`). Use `--seed` for deterministic data or lower the counts if you only need a few records.

## Services

- **PostgreSQL** (port 5433): Database
- **Backup**: Serviço que corre `backup.sh` quando o workflow/manual o invoca
- **Backend** (port 8080): Spring Boot API
- **Frontend Apps** (port 3000 each): Next.js applications
- **Nginx** (port 80): Reverse proxy
- **Keycloak**: Authentication and authorization
- **MinIO**: Object storage
