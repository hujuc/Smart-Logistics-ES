# Terraform Migration Summary

This document records what changed when the infrastructure was moved to Terraform and how to operate the new setup. Everything is written in plain English so the whole team can use it quickly.

## Files Created or Updated

### Terraform workspace
```
infra/terraform/
├── main.tf                  # Docker resources managed by Terraform
├── variables.tf             # Input variables (including defaults)
├── outputs.tf               # Useful values exported after apply
├── terraform.tfvars.example # Sample values to copy and edit
├── terraform.tfvars         # Local values (ignored from Git)
├── .gitignore               # Protects state and secrets
└── README.md                # Detailed how‑to guide
```

### Supporting scripts and docs
```
infra/
├── deploy.sh            # Wrapper that runs Terraform and Docker Compose
├── docker-compose.yaml  # External services (Keycloak, MinIO, Nginx)
└── MIGRATION.md         # Step-by-step migration notes
```

### Continuous Integration
```
.github/workflows/
└── terraform.yml        # GitHub Actions pipeline for plan/apply
```

## Final Architecture

/| Managed by Terraform (local Docker provider) | Access |
|----------------------------------------------|--------|
| PostgreSQL                                   | Internal only (`slms-postgres-prod` on `slms-network`) |
| Spring Boot backend                          | 2 replicas on port 8080, exposed via Nginx at `/api` |
| Admin Console (Next.js)                      | 2 replicas on port 3000, exposed via Nginx at `/admin` |
| Customer Portal (Next.js)                    | 2 replicas on port 3000, exposed via Nginx at `/customer` |
| Driver PWA (Next.js)                         | 2 replicas on port 3000, exposed via Nginx at `/driver` |
| Landing Page (Next.js)                       | 2 replicas on port 3000, exposed via Nginx at `/` |

All Terraform-managed containers join the shared Docker network `slms-network`.

| Managed by Docker Compose | Ports / Notes |
|---------------------------|----------------|
| Keycloak                  | 8180 |
| MinIO                     | 9000 (API), 9001 (console) |
| Nginx reverse proxy       | 80 (load-balances backend/frontends) |

Compose services also connect to `slms-network` so they can talk to Terraform resources by container name.

## Quick Start

1. Copy and edit config files:
   ```bash
   cp infra/.env.example infra/.env
   cp infra/terraform/terraform.tfvars.example infra/terraform/terraform.tfvars
   ```
   Fill real secrets. Change `postgres_host_port` if 5432 is already used on the host.

2. Create the shared network (only once per machine):
   ```bash
   docker network create slms-network
   ```

3. Deploy everything:
   ```bash
   ./infra/deploy.sh deploy
   ```

The script runs `terraform init`, `terraform apply`, removes conflicting containers, and then starts compose services.

## Useful Commands (deploy.sh)

```bash
# Full deployment (Terraform + Compose)
./infra/deploy.sh deploy

# Tear everything down
./infra/deploy.sh down

# Show running containers and Terraform state
./infra/deploy.sh status
```

For service logs, use the wrapper (replica parameter optional) or Compose:

```bash
cd infra
./deploy.sh logs backend 1
./deploy.sh logs backend 2
./deploy.sh logs admin 1

docker compose logs -f keycloak
docker compose logs -f nginx
```

## Required Variables (terraform.tfvars)

Example snippet:

```hcl
postgres_user      = "slms"
postgres_password  = "change-me"
postgres_db        = "slms"
postgres_host_port = 15432

keycloak_realm         = "slms"
keycloak_client_id     = "slms-backend"
keycloak_client_secret = "replace-this"

minio_endpoint    = "http://minio:9000"
minio_access_key  = "minioadmin"
minio_secret_key  = "change-me"
minio_bucket_name = "slms-documents"

shippo_api_key       = "shippo_test_..."
shippo_base_url      = "https://api.goshippo.com"
shipengine_api_key   = "TEST_..."
shipengine_base_url  = "https://api.shipengine.com/v1"
docker_network_name  = "slms-network"
```

Values in `terraform.tfvars` override defaults declared in `variables.tf`. Do not commit the filled file.

## Why This Approach

* **Infrastructure as Code** – Every container definition lives in Git. Changes are reviewable and repeatable.
* **Split responsibilities** – Terraform handles core services. Docker Compose keeps third-party tools that are easier to manage with plain compose (Keycloak, MinIO, Nginx).
* **Automation friendly** – `deploy.sh` wraps daily tasks. The GitHub workflow runs `terraform fmt`, `terraform plan`, and `terraform apply` on pull requests and merges.
* **Shared network** – All services use the same Docker bridge network, so they can communicate using container names (for example backend connects to `slms-postgres-prod:5432`).

## Development Workflow (local machine or VM)

1. Update code.
2. Re-run `./infra/deploy.sh deploy`. Terraform will rebuild images if Dockerfiles changed and recreate containers when needed. Compose will restart external services.
3. When finished, run `./infra/deploy.sh down` to free resources.

If you prefer manual Terraform commands:

```bash
cd infra/terraform
terraform plan
terraform apply
terraform destroy
```

## Notes for Deployment on a VM

* Install Docker Engine, Docker Compose Plugin, and Terraform 1.5+.
* Clone the repository and repeat the quick start steps above.
* Expose the required ports (firewall / security groups).
* Consider using strong secrets and TLS if the VM is reachable from the internet.

## Maintenance Checklist

* **Terraform version** – Update `TF_VERSION` in `.github/workflows/terraform.yml` when bumping Terraform.
* **New services** – Add new blocks to `main.tf`, update `variables.tf` and `README.md`, run `terraform fmt`, then apply.
* **Port conflicts** – Adjust `postgres_host_port` or other port mappings if the host already uses a port.
* **State files** – Keep `terraform.tfstate` private. It stays ignored by Git. Back it up if you use remote storage later.

This document will be kept under version control. Update it whenever the Terraform setup changes so future teammates can follow the same process without surprises.
