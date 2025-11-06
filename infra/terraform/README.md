# SLMS Infrastructure - Terraform + Docker Compose

This infrastructure setup uses **Terraform** to manage core application services (PostgreSQL, Backend, Frontends) and **Docker Compose** for external services (Keycloak, MinIO, Nginx).

## Architecture

### Terraform-managed services:
- **PostgreSQL** - Database (port 5432)
- **Backend** - Spring Boot API (port 8080)
- **Admin Console** - Next.js frontend (port 3001)
- **Customer Portal** - Next.js frontend (port 3002)
- **Driver PWA** - Next.js frontend (port 3003)
- **Landing Page** - Next.js frontend (port 3004)

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

Copy the example file and configure your values:

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your actual credentials:
- Database passwords
- Keycloak secrets
- API keys (Shippo, ShipEngine)
- MinIO credentials
- Host port overrides (for example `postgres_host_port` if 5432 is busy)

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

Access the services:
- Backend API: http://localhost:8080
- Admin Console: http://localhost:3001
- Customer Portal: http://localhost:3002
- Driver PWA: http://localhost:3003
- Landing Page: http://localhost:3004
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

### View Service Logs

```bash
# Terraform-managed services
docker logs slms-postgres
docker logs slms-backend-dev
docker logs slms-admin-console-dev

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
docker exec -it slms-postgres psql -U slms_user -d slms_db
```

## Network Configuration

All services are connected to the `slms-network` bridge network:

- The network must be created manually (e.g. `docker network create slms-network`)
- Terraform references the existing network; it does **not** manage its lifecycle
- Docker Compose services also reuse the same external network
- Containers can communicate using service/container names as hostnames

Example: Backend connects to PostgreSQL using `slms-postgres:5432`

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
- 80, 5432, 8080, 8180, 9000, 9001, 3001-3004

```bash
# Check port usage
sudo lsof -i :[port]
```

If PostgreSQL is already running locally, set `postgres_host_port` in `terraform.tfvars` to an available value (e.g. `15432`) and update your clients accordingly.

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

- The `terraform.tfvars` file contains sensitive data - **never commit it to git**
- Use `.tfvars` file or environment variables for secrets
- For production, use proper secrets management (HashiCorp Vault, AWS Secrets Manager, etc.)
- The current setup is for development/testing only

## File Structure

```
infra/
├── terraform/
│   ├── main.tf              # Main Terraform configuration
│   ├── variables.tf         # Variable definitions
│   ├── outputs.tf           # Output values
│   ├── terraform.tfvars     # Your actual values (gitignored)
│   └── terraform.tfvars.example  # Example values
├── docker-compose.yaml      # External services only
├── .env                     # Environment variables for compose
└── nginx/
    └── default.conf         # Nginx configuration
```
