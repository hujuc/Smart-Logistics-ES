#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TERRAFORM_DIR="${SCRIPT_DIR}/terraform"

export COMPOSE_PROJECT_NAME="slms"
export COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yaml"
SERVICE_REPLICA_COUNT="${SERVICE_REPLICA_COUNT:-2}"
readonly SERVICE_REPLICA_COUNT
export TF_VAR_service_replica_count="${SERVICE_REPLICA_COUNT}"

readonly TERRAFORM_REPLICATED_BASES=(
  slms-backend-prod
  slms-admin-console-prod
  slms-customer-portal-prod
  slms-driver-pwa-prod
  slms-landing-page-prod
)

terraform_managed_containers() {
  printf '%s\n' "slms-postgres-prod"
  local base
  for base in "${TERRAFORM_REPLICATED_BASES[@]}"; do
    for ((replica=1; replica<=SERVICE_REPLICA_COUNT; replica++)); do
      printf '%s-%s\n' "${base}" "${replica}"
    done
  done
}

replicated_name() {
  local base="$1"
  local replica="${2:-1}"

  if (( replica < 1 || replica > SERVICE_REPLICA_COUNT )); then
    log "Replica ${replica} out of range (1-${SERVICE_REPLICA_COUNT}) for ${base}"
    exit 1
  fi

  printf '%s-%s\n' "${base}" "${replica}"
}

log() {
  printf '[%s] %s\n' "$(date +%H:%M:%S)" "$*"
}

ensure_tools() {
  command -v docker >/dev/null || { log "Docker not found"; exit 1; }
  command -v terraform >/dev/null || { log "Terraform not found"; exit 1; }
}

ensure_network() {
  local network="${1:-slms-network}"
  if ! docker network inspect "${network}" >/dev/null 2>&1; then
    log "Creating docker network ${network}"
    docker network create "${network}"
  fi
}

cleanup_containers() {
  log "Checking for outdated containers..."
  mapfile -t containers < <(terraform_managed_containers)

  # Only remove stopped containers, keep running ones
  for name in "${containers[@]}"; do
    if docker ps -a --filter "status=exited" --format '{{.Names}}' | grep -qx "${name}"; then
      log "Removing stopped container ${name}"
      docker rm -f "${name}" >/dev/null
    fi
  done
}

terraform_init() {
  (cd "${TERRAFORM_DIR}" && terraform init -input=false)
}

terraform_apply() {
  log "Applying Terraform changes (will reuse existing resources when possible)..."
  (cd "${TERRAFORM_DIR}" && terraform apply -auto-approve)
}

terraform_destroy() {
  (cd "${TERRAFORM_DIR}" && terraform destroy -auto-approve)
}

# Smarter deployment: only rebuilds changed images
terraform_smart_deploy() {
  log "Smart deploy: checking for changes..."
  
  # First, run terraform plan to see what changed
  (cd "${TERRAFORM_DIR}" && terraform plan -out=tfplan >/dev/null 2>&1)
  
  # Apply the plan (this will only update what changed)
  log "Applying infrastructure changes..."
  (cd "${TERRAFORM_DIR}" && terraform apply -auto-approve tfplan)
  
  # Clean up plan file
  rm -f "${TERRAFORM_DIR}/tfplan"
}

compose_up() {
  log "Starting auxiliary services (Keycloak, MinIO, Nginx)"
  docker compose up -d --force-recreate keycloak minio minio-create-bucket nginx
}

compose_down() {
  log "Stopping Docker Compose services"
  docker compose down --remove-orphans
}

terraform_stop() {
  log "Stopping Terraform-managed containers"
  mapfile -t containers < <(terraform_managed_containers)

  for name in "${containers[@]}"; do
    if docker ps --format '{{.Names}}' | grep -qx "${name}"; then
      log "Stopping container ${name}"
      docker stop "${name}" >/dev/null
    fi
  done
}

terraform_restart() {
  log "Restarting Terraform-managed containers"
  mapfile -t containers < <(terraform_managed_containers)

  for name in "${containers[@]}"; do
    if docker ps -a --format '{{.Names}}' | grep -qx "${name}"; then
      log "Restarting container ${name}"
      docker restart "${name}" >/dev/null
    fi
  done
}

case "${1:-}" in
  up|deploy)
    ensure_tools
    ensure_network
    cleanup_containers
    terraform_init
    terraform_smart_deploy  # Use smart deploy instead of always destroying
    compose_up
    log "Infrastructure available at:"
    log "  Backend API:    http://localhost/api"
    log "  Admin Console:  http://localhost/admin"
    log "  Customer Portal:http://localhost/customer"
    log "  Driver PWA:     http://localhost/driver"
    log "  Landing Page:   http://localhost/"
    log "  Keycloak:       http://localhost:8180"
    log "  MinIO Console:  http://localhost:9001"
    log "  Nginx:          http://localhost"
    ;;
  down|destroy)
    compose_down
    terraform_destroy
    ;;
  stop)
    log "Stopping all services..."
    terraform_stop
    compose_down
    log "All services stopped"
    ;;
  restart)
    log "Restarting all services..."
    terraform_restart
    docker compose restart
    log "All services restarted"
    ;;
  logs)
    service="${2:-backend}"
    replica="${3:-1}"
    case "${service}" in
      backend) container="$(replicated_name slms-backend-prod "${replica}")" ;;
      admin) container="$(replicated_name slms-admin-console-prod "${replica}")" ;;
      customer) container="$(replicated_name slms-customer-portal-prod "${replica}")" ;;
      driver) container="$(replicated_name slms-driver-pwa-prod "${replica}")" ;;
      landing) container="$(replicated_name slms-landing-page-prod "${replica}")" ;;
      postgres) container="slms-postgres-prod" ;;
      keycloak) container="slms-keycloak-prod" ;;
      nginx) container="slms-nginx-prod" ;;
      minio) container="slms-minio-prod" ;;
      *) log "Unknown service: ${service}"; exit 1 ;;
    esac
    docker logs -f "${container}"
    ;;
  status)
    docker compose ps
    (cd "${TERRAFORM_DIR}" && terraform state list || true)
    ;;
  rebuild)
    log "Forcing rebuild of all images..."
    ensure_tools
    ensure_network
    
    # Taint all image resources to force rebuild
    (cd "${TERRAFORM_DIR}" && {
      terraform taint docker_image.backend 2>/dev/null || true
      terraform taint docker_image.admin_console 2>/dev/null || true
      terraform taint docker_image.customer_portal 2>/dev/null || true
      terraform taint docker_image.driver_pwa 2>/dev/null || true
      terraform taint docker_image.landing_page 2>/dev/null || true
    })
    
    terraform_init
    terraform_smart_deploy
    compose_up
    log "✅ Rebuild complete!"
    ;;
*)
  echo "Usage: $0 {deploy|up|destroy|down|stop|restart|status|rebuild|logs [service]}"
  echo ""
  echo "Commands:"
  echo "  deploy|up       - Smart deploy (reuses existing containers when possible)"
  echo "  destroy|down    - Destroy all infrastructure"
  echo "  stop            - Stop all containers (without removing)"
  echo "  restart         - Restart all containers"
  echo "  status          - Show status of all services"
  echo "  rebuild         - Force rebuild of all Docker images"
  echo "  logs [service] [replica] - Follow logs of a service (replica applies to backend/frontends)"
  exit 1
  ;;
esac
