#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TERRAFORM_DIR="${SCRIPT_DIR}/terraform"

export COMPOSE_PROJECT_NAME="slms"
export COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yaml"

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
  
  # Remove all stopped replica containers managed by Terraform
  local stopped_containers=$(docker ps -a --filter "status=exited" --format '{{.Names}}' | grep -E 'slms-(backend|admin-console|customer-portal|driver-pwa|landing-page|postgres)-prod')
  
  if [ -n "$stopped_containers" ]; then
    echo "$stopped_containers" | xargs docker rm -f >/dev/null
    log "Removed stopped containers"
  else
    log "No stopped containers to clean up"
  fi
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
  
  # Stop all running replica containers managed by Terraform
  local running_containers=$(docker ps --format '{{.Names}}' | grep -E 'slms-(backend|admin-console|customer-portal|driver-pwa|landing-page|postgres)-prod' || true)
  
  if [ -n "$running_containers" ]; then
    echo "$running_containers" | xargs docker stop >/dev/null
    log "Stopped all Terraform-managed containers"
  else
    log "No running containers to stop"
  fi
}

remove_conflicting_containers() {
  log "Removing conflicting containers before Terraform apply..."
  
  # Remove ALL containers (running or stopped) that match Terraform naming pattern
  local all_containers=$(docker ps -a --format '{{.Names}}' | grep -E 'slms-(backend|admin-console|customer-portal|driver-pwa|landing-page|postgres)-prod' || true)
  
  if [ -n "$all_containers" ]; then
    echo "$all_containers" | xargs docker rm -f >/dev/null 2>&1 || true
    log "Removed existing containers to avoid conflicts"
  else
    log "No conflicting containers found"
  fi
}

terraform_restart() {
  log "Restarting Terraform-managed containers"
  
  # Restart all running replica containers
  local running_containers=$(docker ps --format '{{.Names}}' | grep -E 'slms-(backend|admin-console|customer-portal|driver-pwa|landing-page|postgres)-prod' || true)
  
  if [ -n "$running_containers" ]; then
    echo "$running_containers" | xargs docker restart >/dev/null
    log "Restarted all Terraform-managed containers"
  else
    log "No running containers to restart"
  fi
}

case "${1:-}" in
  up|deploy)
    ensure_tools
    ensure_network
    remove_conflicting_containers  # Remove ALL existing containers to avoid conflicts
    cleanup_containers
    terraform_init
    terraform_smart_deploy  # Use smart deploy instead of always destroying
    compose_up
    log "Infrastructure available at:"
    log "  Backend:        http://localhost:8080"
    log "  Admin Console:  http://localhost:3001"
    log "  Customer Portal:http://localhost:3002"
    log "  Driver PWA:     http://localhost:3003"
    log "  Landing Page:   http://localhost:3004"
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
    case "${service}" in
      backend) docker logs -f slms-backend-prod ;;
      admin) docker logs -f slms-admin-console-prod ;;
      customer) docker logs -f slms-customer-portal-prod ;;
      driver) docker logs -f slms-driver-pwa-prod ;;
      landing) docker logs -f slms-landing-page-prod ;;
      postgres) docker logs -f slms-postgres-prod ;;
      keycloak) docker logs -f slms-keycloak-prod ;;
      nginx) docker logs -f slms-nginx-prod ;;
      minio) docker logs -f slms-minio-prod ;;
      *) log "Unknown service: ${service}"; exit 1 ;;
    esac
    ;;
  status)
    docker compose ps
    (cd "${TERRAFORM_DIR}" && terraform state list || true)
    ;;
  monitor)
    log "Starting production container monitor..."
    python3 "${SCRIPT_DIR}/scripts/monitor_prod.py"
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
  echo "  status          - Show status of all services
  monitor         - Start the container monitoring and resurrection service
  rebuild         - Force rebuild of all Docker images
"
  echo "  logs [service]  - Follow logs of a service (backend|admin|customer|driver|landing|postgres|keycloak|nginx|minio)"
  exit 1
  ;;
esac
