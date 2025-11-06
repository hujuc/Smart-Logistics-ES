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
  status)
    docker compose ps
    (cd "${TERRAFORM_DIR}" && terraform state list || true)
    ;;
*)
  echo "Usage: $0 {deploy|up|destroy|down|status}"
  exit 1
  ;;
esac
