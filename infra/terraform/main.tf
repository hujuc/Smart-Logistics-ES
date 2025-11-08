terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }
}

provider "docker" {
  host = "unix:///var/run/docker.sock"
}

locals {
  repo_root           = abspath("${path.module}/../..")
  backend_dir         = "${local.repo_root}/src/backend"
  admin_console_dir   = "${local.repo_root}/src/frontend/admin-console"
  customer_portal_dir = "${local.repo_root}/src/frontend/customer-portal"
  driver_pwa_dir      = "${local.repo_root}/src/frontend/driver-pwa"
  landing_page_dir    = "${local.repo_root}/src/frontend/landing-page"

  env_file_path = (
    var.env_file != null && trimspace(var.env_file) != "" ?
    (
      startswith(var.env_file, "/") ?
      var.env_file :
      abspath("${path.module}/${var.env_file}")
    ) :
    ""
  )

  env_file_contents = local.env_file_path != "" && fileexists(local.env_file_path) ? file(local.env_file_path) : ""

  env_lines = [
    for raw_line in split("\n", local.env_file_contents) : trimspace(raw_line)
    if trimspace(raw_line) != "" && !startswith(trimspace(raw_line), "#") && can(regex("^[A-Za-z_][A-Za-z0-9_]*\\s*=", trimspace(raw_line)))
  ]

  env_keys = [
    for line in local.env_lines : trimspace(split(line, "=")[0])
  ]

  env_values = [
    for line in local.env_lines :
    trimspace(replace(join("=", slice(split(line, "="), 1, length(split(line, "=")))), "\"", ""))
  ]

  env_map = zipmap(local.env_keys, local.env_values)

  config_postgres_user     = coalesce(var.postgres_user, lookup(local.env_map, "POSTGRES_USER", lookup(local.env_map, "DB_USERNAME", null)))
  config_postgres_password = coalesce(var.postgres_password, lookup(local.env_map, "POSTGRES_PASSWORD", lookup(local.env_map, "DB_PASSWORD", null)))
  config_postgres_db       = coalesce(var.postgres_db, lookup(local.env_map, "POSTGRES_DB", lookup(local.env_map, "DB_NAME", null)))
  config_postgres_host_port = coalesce(
    var.postgres_host_port,
    try(tonumber(lookup(local.env_map, "POSTGRES_HOST_PORT", "")), null),
    try(tonumber(lookup(local.env_map, "DB_PORT", "")), null),
    5432
  )

  config_keycloak_realm     = coalesce(var.keycloak_realm, lookup(local.env_map, "KEYCLOAK_REALM", null))
  config_keycloak_client_id = coalesce(var.keycloak_client_id, lookup(local.env_map, "KEYCLOAK_CLIENT_ID", lookup(local.env_map, "KEYCLOAK_ADMIN_CLIENT_ID", null)))
  config_keycloak_client_secret = coalesce(
    var.keycloak_client_secret,
    lookup(local.env_map, "KEYCLOAK_CLIENT_SECRET", lookup(local.env_map, "KEYCLOAK_ADMIN_CLIENT_SECRET", null))
  )

  config_minio_endpoint = coalesce(
    var.minio_endpoint,
    lookup(local.env_map, "MINIO_ENDPOINT", null),
    (lookup(local.env_map, "MINIO_HOST", null) != null && lookup(local.env_map, "MINIO_PORT", null) != null ?
      "http://${lookup(local.env_map, "MINIO_HOST", "")}:${lookup(local.env_map, "MINIO_PORT", "")}" :
    null)
  )
  config_minio_access_key  = coalesce(var.minio_access_key, lookup(local.env_map, "MINIO_ACCESS_KEY", lookup(local.env_map, "MINIO_ROOT_USER", null)))
  config_minio_secret_key  = coalesce(var.minio_secret_key, lookup(local.env_map, "MINIO_SECRET_KEY", lookup(local.env_map, "MINIO_ROOT_PASSWORD", null)))
  config_minio_bucket_name = coalesce(var.minio_bucket_name, lookup(local.env_map, "MINIO_BUCKET_NAME", lookup(local.env_map, "MINIO_BUCKET", null)))

  config_shippo_api_key  = coalesce(var.shippo_api_key, lookup(local.env_map, "SHIPPO_API_KEY", null))
  config_shippo_base_url = coalesce(var.shippo_base_url, lookup(local.env_map, "SHIPPO_BASE_URL", "https://api.goshippo.com"))

  config_shipengine_api_key  = coalesce(var.shipengine_api_key, lookup(local.env_map, "SHIPENGINE_API_KEY", lookup(local.env_map, "SANDBOX_API_KEY", null)))
  config_shipengine_base_url = coalesce(var.shipengine_base_url, lookup(local.env_map, "SHIPENGINE_BASE_URL", "https://api.shipengine.com/v1"))

  config_docker_network = coalesce(var.docker_network_name, lookup(local.env_map, "DOCKER_NETWORK_NAME", "slms-network"))
}

# Network - usar a mesma rede que o docker-compose (deve existir previamente)
data "docker_network" "slms_network" {
  name = local.config_docker_network
}

# PostgreSQL Database
resource "docker_image" "postgres" {
  name = "postgres:16-alpine"
}

resource "docker_container" "postgres" {
  name  = "slms-postgres"
  image = docker_image.postgres.image_id

  env = [
    "POSTGRES_USER=${local.config_postgres_user}",
    "POSTGRES_PASSWORD=${local.config_postgres_password}",
    "POSTGRES_DB=${local.config_postgres_db}",
    "POSTGRES_HOST_AUTH_METHOD=scram-sha-256",
    "POSTGRES_INITDB_ARGS=--auth-host=scram-sha-256"
  ]

  ports {
    internal = 5432
    external = local.config_postgres_host_port
  }

  volumes {
    volume_name    = docker_volume.postgres_data.name
    container_path = "/var/lib/postgresql/data"
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"

  healthcheck {
    test     = ["CMD-SHELL", "pg_isready -U ${local.config_postgres_user} -d ${local.config_postgres_db}"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
  }
}

resource "docker_volume" "postgres_data" {
  name = "slms-postgres-data"
}

# Backend Service
resource "docker_image" "backend" {
  name = "slms-backend:latest"
  build {
    context    = local.backend_dir
    dockerfile = "Dockerfile"
    tag        = ["slms-backend:latest"]
    build_args = {
      BUILDKIT_INLINE_CACHE = "1"
    }
  }
}

resource "docker_container" "backend" {
  name  = "slms-backend-dev"
  image = docker_image.backend.image_id

  env = [
    "SPRING_DATASOURCE_URL=jdbc:postgresql://${docker_container.postgres.name}:5432/${local.config_postgres_db}",
    "SPRING_DATASOURCE_USERNAME=${local.config_postgres_user}",
    "SPRING_DATASOURCE_PASSWORD=${local.config_postgres_password}",
    "SPRING_JPA_HIBERNATE_DDL_AUTO=update",
    "KEYCLOAK_HOST=slms-keycloak-dev",
    "KEYCLOAK_PORT=8080",
    "KEYCLOAK_REALM=${local.config_keycloak_realm}",
    "KEYCLOAK_CLIENT_ID=${local.config_keycloak_client_id}",
    "KEYCLOAK_CLIENT_SECRET=${local.config_keycloak_client_secret}",
    "KEYCLOAK_ADMIN_CLIENT_ID=${local.config_keycloak_client_id}",
    "KEYCLOAK_ADMIN_CLIENT_SECRET=${local.config_keycloak_client_secret}",
    "MINIO_HOST=minio",
    "MINIO_PORT=9000",
    "MINIO_ROOT_USER=${local.config_minio_access_key}",
    "MINIO_ROOT_PASSWORD=${local.config_minio_secret_key}",
    "MINIO_BUCKET=${local.config_minio_bucket_name}",
    "MINIO_ENDPOINT=${local.config_minio_endpoint}",
    "MINIO_ACCESS_KEY=${local.config_minio_access_key}",
    "MINIO_SECRET_KEY=${local.config_minio_secret_key}",
    "MINIO_BUCKET_NAME=${local.config_minio_bucket_name}",
    "SHIPPO_API_KEY=${local.config_shippo_api_key}",
    "SHIPPO_BASE_URL=${local.config_shippo_base_url}",
    "SHIPENGINE_API_KEY=${local.config_shipengine_api_key}",
    "SHIPENGINE_BASE_URL=${local.config_shipengine_base_url}"
  ]

  ports {
    internal = 8080
    external = 8080
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"

  depends_on = [docker_container.postgres]
}

# Admin Console Frontend
resource "docker_image" "admin_console" {
  name = "slms-admin-console:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-admin-console:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/admin-console"
    }
  }
}

resource "docker_container" "admin_console" {
  name  = "slms-admin-console-prod"
  image = docker_image.admin_console.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3001
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

# Customer Portal Frontend
resource "docker_image" "customer_portal" {
  name = "slms-customer-portal:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-customer-portal:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/customer-portal"
    }
  }
}

resource "docker_container" "customer_portal" {
  name  = "slms-customer-portal-prod"
  image = docker_image.customer_portal.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3002
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

# Driver PWA Frontend
resource "docker_image" "driver_pwa" {
  name = "slms-driver-pwa:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-driver-pwa:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/driver-pwa"
    }
  }
}

resource "docker_container" "driver_pwa" {
  name  = "slms-driver-pwa-prod"
  image = docker_image.driver_pwa.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3003
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

# Landing Page Frontend
# Landing Page Frontend
resource "docker_image" "landing_page" {
  name = "slms-landing-page:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-landing-page:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/landing-page"
    }
  }
}

resource "docker_container" "landing_page" {
  name  = "slms-landing-page-prod"
  image = docker_image.landing_page.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3004
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}
