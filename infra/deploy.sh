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
  local containers=(
    slms-postgres
    slms-backend-dev
    slms-admin-console-dev
    slms-customer-portal-dev
    slms-driver-pwa-dev
    slms-landing-page-dev
  )
  for name in "${containers[@]}"; do
    if docker ps -a --format '{{.Names}}' | grep -qx "${name}"; then
      log "Removing existing container ${name}"
      docker rm -f "${name}" >/dev/null
    fi
  done
}

terraform_init() {
  (cd "${TERRAFORM_DIR}" && terraform init -input=false)
}

terraform_apply() {
  (cd "${TERRAFORM_DIR}" && terraform apply -auto-approve)
}

terraform_destroy() {
  (cd "${TERRAFORM_DIR}" && terraform destroy -auto-approve)
}

compose_up() {
  log "Starting auxiliary services (Keycloak, MinIO, Nginx)"
  docker compose up -d keycloak minio minio-create-bucket nginx
}

compose_down() {
  log "Stopping Docker Compose services"
  docker compose down --remove-orphans
}

terraform_stop() {
  log "Stopping Terraform-managed containers"
  local containers=(
    slms-postgres
    slms-backend-dev
    slms-admin-console-dev
    slms-customer-portal-dev
    slms-driver-pwa-dev
    slms-landing-page-dev
  )
  for name in "${containers[@]}"; do
    if docker ps --format '{{.Names}}' | grep -qx "${name}"; then
      log "Stopping container ${name}"
      docker stop "${name}" >/dev/null
    fi
  done
}

terraform_restart() {
  log "Restarting Terraform-managed containers"
  local containers=(
    slms-postgres
    slms-backend-dev
    slms-admin-console-dev
    slms-customer-portal-dev
    slms-driver-pwa-dev
    slms-landing-page-dev
  )
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
    terraform_apply
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
      backend) docker logs -f slms-backend-dev ;;
      admin) docker logs -f slms-admin-console-dev ;;
      customer) docker logs -f slms-customer-portal-dev ;;
      driver) docker logs -f slms-driver-pwa-dev ;;
      landing) docker logs -f slms-landing-page-dev ;;
      postgres) docker logs -f slms-postgres ;;
      keycloak) docker logs -f slms-keycloak-dev ;;
      nginx) docker logs -f slms-nginx-dev ;;
      minio) docker logs -f minio ;;
      *) log "Unknown service: ${service}"; exit 1 ;;
    esac
    ;;
  status)
    docker compose ps
    (cd "${TERRAFORM_DIR}" && terraform state list || true)
    ;;
*)
  echo "Usage: $0 {deploy|up|destroy|down|stop|restart|status|logs [service]}"
  echo ""
  echo "Commands:"
  echo "  deploy|up       - Deploy full infrastructure"
  echo "  destroy|down    - Destroy all infrastructure"
  echo "  stop            - Stop all containers (without removing)"
  echo "  restart         - Restart all containers"
  echo "  status          - Show status of all services"
  echo "  logs [service]  - Follow logs of a service (backend|admin|customer|driver|landing|postgres|keycloak|nginx|minio)"
  exit 1
  ;;
esac
