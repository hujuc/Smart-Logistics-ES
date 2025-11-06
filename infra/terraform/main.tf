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
}

# Network - usar a mesma rede que o docker-compose (deve existir previamente)
data "docker_network" "slms_network" {
  name = var.docker_network_name
}

# PostgreSQL Database
resource "docker_image" "postgres" {
  name = "postgres:16-alpine"
}

resource "docker_container" "postgres" {
  name  = "slms-postgres"
  image = docker_image.postgres.image_id

  env = [
    "POSTGRES_USER=${var.postgres_user}",
    "POSTGRES_PASSWORD=${var.postgres_password}",
    "POSTGRES_DB=${var.postgres_db}",
    "POSTGRES_HOST_AUTH_METHOD=scram-sha-256",
    "POSTGRES_INITDB_ARGS=--auth-host=scram-sha-256"
  ]

  ports {
    internal = 5432
    external = var.postgres_host_port
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
    test     = ["CMD-SHELL", "pg_isready -U ${var.postgres_user} -d ${var.postgres_db}"]
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
    "SPRING_DATASOURCE_URL=jdbc:postgresql://${docker_container.postgres.name}:5432/${var.postgres_db}",
    "SPRING_DATASOURCE_USERNAME=${var.postgres_user}",
    "SPRING_DATASOURCE_PASSWORD=${var.postgres_password}",
    "SPRING_JPA_HIBERNATE_DDL_AUTO=update",
    "KEYCLOAK_REALM=${var.keycloak_realm}",
    "KEYCLOAK_CLIENT_ID=${var.keycloak_client_id}",
    "KEYCLOAK_CLIENT_SECRET=${var.keycloak_client_secret}",
    "MINIO_ENDPOINT=${var.minio_endpoint}",
    "MINIO_ACCESS_KEY=${var.minio_access_key}",
    "MINIO_SECRET_KEY=${var.minio_secret_key}",
    "MINIO_BUCKET_NAME=${var.minio_bucket_name}",
    "SHIPPO_API_KEY=${var.shippo_api_key}",
    "SHIPPO_BASE_URL=${var.shippo_base_url}",
    "SHIPENGINE_API_KEY=${var.shipengine_api_key}",
    "SHIPENGINE_BASE_URL=${var.shipengine_base_url}"
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
    dockerfile = "infra/docker/frontend/Dockerfile.dev"
    tag        = ["slms-admin-console:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/admin-console"
    }
  }
}

resource "docker_container" "admin_console" {
  name  = "slms-admin-console-dev"
  image = docker_image.admin_console.image_id

  command = ["npm", "run", "dev"]

  env = [
    "NODE_ENV=development",
    "NEXT_TELEMETRY_DISABLED=1",
    "CHOKIDAR_USEPOLLING=true",
    "WATCHPACK_POLLING=true"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3001
  }

  volumes {
    host_path      = local.admin_console_dir
    container_path = "/app"
  }

  volumes {
    volume_name    = docker_volume.admin_console_node_modules.name
    container_path = "/app/node_modules"
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

resource "docker_volume" "admin_console_node_modules" {
  name = "slms-admin-console-node-modules"
}

# Customer Portal Frontend
resource "docker_image" "customer_portal" {
  name = "slms-customer-portal:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.dev"
    tag        = ["slms-customer-portal:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/customer-portal"
    }
  }
}

resource "docker_container" "customer_portal" {
  name  = "slms-customer-portal-dev"
  image = docker_image.customer_portal.image_id

  command = ["npm", "run", "dev"]

  env = [
    "NODE_ENV=development",
    "NEXT_TELEMETRY_DISABLED=1",
    "CHOKIDAR_USEPOLLING=true",
    "WATCHPACK_POLLING=true"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3002
  }

  volumes {
    host_path      = local.customer_portal_dir
    container_path = "/app"
  }

  volumes {
    volume_name    = docker_volume.customer_portal_node_modules.name
    container_path = "/app/node_modules"
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

resource "docker_volume" "customer_portal_node_modules" {
  name = "slms-customer-portal-node-modules"
}

# Driver PWA Frontend
resource "docker_image" "driver_pwa" {
  name = "slms-driver-pwa:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.dev"
    tag        = ["slms-driver-pwa:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/driver-pwa"
    }
  }
}

resource "docker_container" "driver_pwa" {
  name  = "slms-driver-pwa-dev"
  image = docker_image.driver_pwa.image_id

  command = ["npm", "run", "dev"]

  env = [
    "NODE_ENV=development",
    "NEXT_TELEMETRY_DISABLED=1",
    "CHOKIDAR_USEPOLLING=true",
    "WATCHPACK_POLLING=true"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3003
  }

  volumes {
    host_path      = local.driver_pwa_dir
    container_path = "/app"
  }

  volumes {
    volume_name    = docker_volume.driver_pwa_node_modules.name
    container_path = "/app/node_modules"
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

resource "docker_volume" "driver_pwa_node_modules" {
  name = "slms-driver-pwa-node-modules"
}

# Landing Page Frontend
resource "docker_image" "landing_page" {
  name = "slms-landing-page:latest"
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.dev"
    tag        = ["slms-landing-page:latest"]
    build_args = {
      FRONTEND_PATH = "src/frontend/landing-page"
    }
  }
}

resource "docker_container" "landing_page" {
  name  = "slms-landing-page-dev"
  image = docker_image.landing_page.image_id

  command = ["npm", "run", "dev"]

  env = [
    "NODE_ENV=development",
    "NEXT_TELEMETRY_DISABLED=1",
    "CHOKIDAR_USEPOLLING=true",
    "WATCHPACK_POLLING=true"
  ]

  working_dir = "/app"

  ports {
    internal = 3000
    external = 3004
  }

  volumes {
    host_path      = local.landing_page_dir
    container_path = "/app"
  }

  volumes {
    volume_name    = docker_volume.landing_page_node_modules.name
    container_path = "/app/node_modules"
  }

  networks_advanced {
    name = data.docker_network.slms_network.name
  }

  restart = "unless-stopped"
}

resource "docker_volume" "landing_page_node_modules" {
  name = "slms-landing-page-node-modules"
}
