# SLMS Infrastructure - Terraform + Docker Compose

This infrastructure setup uses **Terraform** to manage core application services (PostgreSQL, Backend, Frontends) and **Docker Compose** for external services (Keycloak, MinIO, Nginx).

## Architecture

### Terraform-managed services:
- **PostgreSQL** - Database (port 5432)
- **Backend** - Spring Boot API (internal port 8080, two replicas load-balanced by Nginx at `/api`)
- **Admin Console** - Next.js frontend (internal port 3000, two replicas served at `/admin`)
- **Customer Portal** - Next.js frontend (internal port 3000, two replicas served at `/customer`)
- **Driver PWA** - Next.js frontend (internal port 3000, two replicas served at `/driver`)
- **Landing Page** - Next.js frontend (internal port 3000, two replicas served at `/`)

### Docker Compose-managed services:
- **Keycloak** - Authentication service (port 8180)
- **MinIO** - Object storage (ports 9000, 9001)
- **Nginx** - Reverse proxy (port 80)

All services communicate through the shared `slms-network` Docker network.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose v2+
- Terraform 1.5+

## Setup Instructions

### 1. Configure Environment Variables

Populate the shared dotenv file used by both Docker Compose and Terraform:

```bash
cd infra
cp .env.example .env  # only if you still need to create it
```

Update `.env` with your actual credentials:
- Database passwords and host/port
- Keycloak secrets
- API keys (Shippo, ShipEngine)
- MinIO credentials
- Host port overrides (e.g. `DB_PORT` if 5432 is busy)

Terraform automatically reads from this file (or whatever path you set via `var.env_file`).
If you need to override a specific value for Terraform only, create an optional
`terraform.tfvars` and set just the values you want to override.

### 2. Initialize Terraform

```bash
cd infra/terraform
terraform init
```

### 3. Create the Docker Network

The network must exist before running Terraform:

```bash
docker network create slms-network
```

### 4. Deploy Services

#### Option A: Deploy everything at once

```bash
# Start Terraform services
cd infra/terraform
terraform apply

# Start Docker Compose services
cd ../
docker compose up -d
```

#### Option B: Deploy step by step

```bash
# 1. Start external services first
cd infra
docker compose up -d keycloak minio minio-create-bucket

# 2. Deploy Terraform infrastructure
cd terraform
terraform apply

# 3. Start Nginx proxy
cd ../
docker compose up -d nginx
```

### 5. Verify Deployment

Check that all services are running:

```bash
# Terraform services
terraform show

# Docker Compose services
docker compose ps

# Check network connectivity
docker network inspect slms-network
```

Access the services (proxied by Nginx unless noted):
- Backend API: http://localhost/api
- Admin Console: http://localhost/admin
- Customer Portal: http://localhost/customer
- Driver PWA: http://localhost/driver
- Landing Page: http://localhost/
- Keycloak: http://localhost:8180
- MinIO Console: http://localhost:9001
- Nginx Proxy: http://localhost

## Management Commands

### Terraform Operations

```bash
cd infra/terraform

# View planned changes
terraform plan

# Apply changes
terraform apply

# Destroy Terraform-managed infrastructure
terraform destroy

# View current state
terraform show

# List resources
terraform state list
```

### Docker Compose Operations

```bash
cd infra

# Start services
docker compose up -d

# Stop services
docker compose down

# View logs
docker compose logs -f [service-name]

# Restart a service
docker compose restart [service-name]
```

## Stateless Service Scaling

- Backend and all Next.js frontends run with **two replicas** by default.
- Traffic reaches them through the `slms-nginx-prod` reverse proxy, which load-balances across the replicas.
- To change the replica count globally, set `service_replica_count` in `infra/terraform/terraform.tfvars` **or** export `SERVICE_REPLICA_COUNT`/`TF_VAR_service_replica_count` before running `terraform apply` or `./deploy.sh`.
- Example: `SERVICE_REPLICA_COUNT=3 ./deploy.sh up`

### View Service Logs

```bash
# Terraform-managed services (replica index defaults to 1)
cd infra
./deploy.sh logs backend 1
./deploy.sh logs backend 2
./deploy.sh logs admin 1
./deploy.sh logs customer 1
./deploy.sh logs driver 1
./deploy.sh logs landing 1
./deploy.sh logs postgres

# Docker Compose services
docker compose logs keycloak
docker compose logs minio
docker compose logs nginx
```

## Development Workflow

### Hot Reload for Frontends

The frontend containers are configured with volume mounts for hot reload:

```bash
# Edit files in:
# - src/frontend/admin-console
# - src/frontend/customer-portal
# - src/frontend/driver-pwa
# - src/frontend/landing-page

# Changes will be automatically reflected in running containers
```

### Backend Development

To rebuild and redeploy the backend after code changes:

```bash
cd infra/terraform
terraform apply -replace=docker_container.backend
```

### Database Management

Connect to PostgreSQL:

```bash
docker exec -it slms-postgres-prod psql -U slms_user -d slms_db
```

## Network Configuration

All services are connected to the `slms-network` bridge network:

- The network must be created manually (e.g. `docker network create slms-network`)
- Terraform references the existing network; it does **not** manage its lifecycle
- Docker Compose services also reuse the same external network
- Containers can communicate using service/container names as hostnames

Example: Backend connects to PostgreSQL using `slms-postgres-prod:5432`

## Troubleshooting

### Network not found error

If you get "network slms-network not found":

```bash
docker network create slms-network
```

### Terraform state issues

```bash
# Remove problematic resources from state
terraform state rm [resource-name]

# Re-import if needed
terraform import [resource-type].[name] [resource-id]
```

### Port conflicts

Ensure these ports are free before starting:
- 80 (Nginx)
- 5432 (PostgreSQL)
- 8180 (Keycloak)
- 9000-9001 (MinIO)

The backend and frontend replicas no longer expose host ports directly; always access them through the Nginx proxy on port 80.

```bash
# Check port usage
sudo lsof -i :[port]
```

If PostgreSQL is already running locally, adjust `DB_PORT` in `.env` (or override `postgres_host_port` via `terraform.tfvars`) to an available value (e.g. `15432`) and update your clients accordingly.

### Container fails to start

```bash
# Check logs
docker logs [container-name]

# Inspect container
docker inspect [container-name]

# Check network
docker network inspect slms-network
```

## Clean Up

### Full cleanup (all services and data)

```bash
# Stop and remove Terraform services
cd infra/terraform
terraform destroy

# Stop and remove Docker Compose services
cd ../
docker compose down -v

# Remove network
docker network rm slms-network

# Remove dangling images (optional)
docker system prune -a
```

### Partial cleanup (keep data volumes)

```bash
# Terraform services
terraform destroy

# Docker Compose (without volumes)
docker compose down
```

## Migration from Pure Docker Compose

If migrating from the old all-in-compose setup:

1. Stop existing services: `docker compose down`
2. Create network: `docker network create slms-network`
3. Initialize Terraform: `terraform init`
4. Apply Terraform: `terraform apply`
5. Start remaining compose services: `docker compose up -d`

## Security Notes

- The `.env` file contains sensitive data - **never commit it to git**
- Use `.env`, TF_VAR_* environment variables, or Terraform Cloud/CI secrets for sensitive values
- For production, use proper secrets management (HashiCorp Vault, AWS Secrets Manager, etc.)
- The current setup is for development/testing only

## File Structure

```
infra/
├── terraform/
│   ├── main.tf              # Main Terraform configuration
│   ├── variables.tf         # Variable definitions
│   ├── outputs.tf           # Output values
│   ├── terraform.tfvars     # Optional overrides (gitignored)
│   └── terraform.tfvars.example  # Example overrides
├── docker-compose.yaml      # External services only
├── .env                     # Environment variables for compose
└── nginx/
    └── default.conf         # Nginx configuration
```
