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

  # Use variables directly (from terraform.tfvars or CLI)
  # Provide sensible defaults where variables are null
  config_postgres_user      = var.postgres_user != null ? var.postgres_user : "slms"
  config_postgres_password  = var.postgres_password != null ? var.postgres_password : "slms"
  config_postgres_db        = var.postgres_db != null ? var.postgres_db : "slms"
  config_postgres_host_port = var.postgres_host_port != null ? var.postgres_host_port : 5432

  config_keycloak_realm          = var.keycloak_realm != null ? var.keycloak_realm : "slms"
  config_keycloak_client_id      = var.keycloak_client_id != null ? var.keycloak_client_id : "slms-backend"
  config_keycloak_client_secret  = var.keycloak_client_secret != null ? var.keycloak_client_secret : ""
  config_keycloak_hostname_url   = var.keycloak_hostname_url != null ? var.keycloak_hostname_url : "http://localhost:8180"
  config_keycloak_admin          = var.keycloak_admin != null ? var.keycloak_admin : "admin"
  config_keycloak_admin_password = var.keycloak_admin_password != null ? var.keycloak_admin_password : "admin"
  config_jwt_issuer_uri = var.jwt_issuer_uri != null ? var.jwt_issuer_uri : "http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com:8080/realms/slms"

  config_minio_endpoint    = var.minio_endpoint != null ? var.minio_endpoint : "http://slms-minio-prod:9000"
  config_minio_access_key  = var.minio_access_key != null ? var.minio_access_key : "minioadmin"
  config_minio_secret_key  = var.minio_secret_key != null ? var.minio_secret_key : "minioadmin"
  config_minio_bucket_name = var.minio_bucket_name != null ? var.minio_bucket_name : "slms-documents"

  config_shippo_api_key  = var.shippo_api_key != null ? var.shippo_api_key : ""
  config_shippo_base_url = var.shippo_base_url != null ? var.shippo_base_url : "https://api.goshippo.com"

  config_shipengine_api_key  = var.shipengine_api_key != null ? var.shipengine_api_key : ""
  config_shipengine_base_url = var.shipengine_base_url != null ? var.shipengine_base_url : "https://api.shipengine.com/v1"

  config_docker_network = var.docker_network_name != null ? var.docker_network_name : "slms-network"

  # Frontend configuration
  config_next_public_api_url   = var.next_public_api_url != null ? var.next_public_api_url : "http://localhost/api"
  config_flagsmith_env_key     = var.flagsmith_env_key != null ? var.flagsmith_env_key : ""
  config_mapbox_access_token   = var.mapbox_access_token != null ? var.mapbox_access_token : ""
  config_service_replica_count = var.service_replica_count != null ? var.service_replica_count : 2

  # Observability configuration
  config_elastic_apm_server_url   = var.elastic_apm_server_url != null ? var.elastic_apm_server_url : ""
  config_elastic_apm_secret_token = var.elastic_apm_secret_token != null ? var.elastic_apm_secret_token : ""
  config_deployment_env           = var.deployment_env != null ? var.deployment_env : "development"
  config_project_version          = var.project_version != null ? var.project_version : "0.0.1-SNAPSHOT"
  config_tracing_sampling_rate    = var.tracing_sampling_rate != null ? var.tracing_sampling_rate : "1.0"
  config_log_level_root           = var.log_level_root != null ? var.log_level_root : "INFO"
  config_log_level_app            = var.log_level_app != null ? var.log_level_app : "DEBUG"
  config_log_level_web            = var.log_level_web != null ? var.log_level_web : "INFO"
  config_log_level_security       = var.log_level_security != null ? var.log_level_security : "INFO"
}

# Network - usar a mesma rede que o docker-compose (deve existir previamente)
data "docker_network" "slms_network" {
  name = local.config_docker_network
}

# PostgreSQL Database
resource "docker_image" "postgres" {
  name         = "postgres:16-alpine"
  keep_locally = true # Keep image locally to avoid re-pulling on every apply
}

resource "docker_container" "postgres" {
  name  = "slms-postgres-prod"
  image = docker_image.postgres.image_id

  env = [
    "POSTGRES_USER=${local.config_postgres_user}",
    "POSTGRES_PASSWORD=${local.config_postgres_password}",
    "POSTGRES_DB=${local.config_postgres_db}",
    "POSTGRES_HOST_AUTH_METHOD=scram-sha-256",
    "POSTGRES_INITDB_ARGS=--auth-host=scram-sha-256"
  ]

  # No port exposure needed - PostgreSQL only needs to be accessible within the Docker network
  # Other containers can connect using the container name 'slms-postgres'

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
  name = "slms-postgres-data-prod"
}

# Backend Service
resource "docker_image" "backend" {
  name         = "slms-backend:latest"
  keep_locally = true # Keep image locally for faster rebuilds
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
  count = local.config_service_replica_count
  name  = "slms-backend-prod-${count.index + 1}"
  image = docker_image.backend.image_id

  env = [
    "SPRING_DATASOURCE_URL=jdbc:postgresql://${docker_container.postgres.name}:5432/${local.config_postgres_db}",
    "SPRING_DATASOURCE_USERNAME=${local.config_postgres_user}",
    "SPRING_DATASOURCE_PASSWORD=${local.config_postgres_password}",
    "SPRING_JPA_HIBERNATE_DDL_AUTO=update",
    "DB_HOST=${docker_container.postgres.name}",
    "DB_PORT=5432",
    "DB_NAME=${local.config_postgres_db}",
    "DB_USERNAME=${local.config_postgres_user}",
    "DB_PASSWORD=${local.config_postgres_password}",
    "KEYCLOAK_HOST=slms-keycloak-prod",
    "KEYCLOAK_PORT=8080",
    "KEYCLOAK_REALM=${local.config_keycloak_realm}",
    "KEYCLOAK_CLIENT_ID=${local.config_keycloak_client_id}",
    "KEYCLOAK_CLIENT_SECRET=${local.config_keycloak_client_secret}",
    "KEYCLOAK_ADMIN_CLIENT_ID=${local.config_keycloak_client_id}",
    "KEYCLOAK_ADMIN_CLIENT_SECRET=${local.config_keycloak_client_secret}",
    "JWT_ISSUER_URI=${local.config_jwt_issuer_uri}",
    "MINIO_HOST=slms-minio-prod",
    "MINIO_PORT=9000",
    "MINIO_ROOT_USER=${local.config_minio_access_key}",
    "MINIO_ROOT_PASSWORD=${local.config_minio_secret_key}",
    "MINIO_BUCKET=${local.config_minio_bucket_name}",
    "MINIO_BUCKET_NAME=${local.config_minio_bucket_name}",
    "MINIO_ENDPOINT=${local.config_minio_endpoint}",
    "MINIO_ACCESS_KEY=${local.config_minio_access_key}",
    "MINIO_SECRET_KEY=${local.config_minio_secret_key}",
    "SHIPPO_API_KEY=${local.config_shippo_api_key}",
    "SHIPPO_BASE_URL=${local.config_shippo_base_url}",
    "SHIPENGINE_API_KEY=${local.config_shipengine_api_key}",
    "SHIPENGINE_BASE_URL=${local.config_shipengine_base_url}",
    "ELASTIC_APM_SERVER_URL=${local.config_elastic_apm_server_url}",
    "ELASTIC_APM_SECRET_TOKEN=${local.config_elastic_apm_secret_token}",
    "DEPLOYMENT_ENV=${local.config_deployment_env}",
    "PROJECT_VERSION=${local.config_project_version}",
    "TRACING_SAMPLING_RATE=${local.config_tracing_sampling_rate}",
    "LOG_LEVEL_ROOT=${local.config_log_level_root}",
    "LOG_LEVEL_APP=${local.config_log_level_app}",
    "LOG_LEVEL_WEB=${local.config_log_level_web}",
    "LOG_LEVEL_SECURITY=${local.config_log_level_security}"
  ]

  networks_advanced {
    name    = data.docker_network.slms_network.name
    aliases = ["slms-backend-prod"]
  }

  restart = "unless-stopped"

  depends_on = [docker_container.postgres]
}

# Admin Console Frontend
resource "docker_image" "admin_console" {
  name         = "slms-admin-console:latest"
  keep_locally = true
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-admin-console:latest"]
    build_args = {
      FRONTEND_PATH       = "src/frontend/admin-console"
      NEXT_PUBLIC_API_URL = local.config_next_public_api_url
    }
  }
}

resource "docker_container" "admin_console" {
  count = local.config_service_replica_count
  name  = "slms-admin-console-prod-${count.index + 1}"
  image = docker_image.admin_console.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  networks_advanced {
    name    = data.docker_network.slms_network.name
    aliases = ["slms-admin-console-prod"]
  }

  restart = "unless-stopped"
}

# Customer Portal Frontend
resource "docker_image" "customer_portal" {
  name         = "slms-customer-portal:latest"
  keep_locally = true
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-customer-portal:latest"]
    build_args = {
      FRONTEND_PATH                 = "src/frontend/customer-portal"
      NEXT_PUBLIC_API_URL           = local.config_next_public_api_url
      NEXT_PUBLIC_FLAGSMITH_ENV_KEY = local.config_flagsmith_env_key
      KEYCLOAK_BASE_URL             = local.config_keycloak_hostname_url
      NEXTAUTH_URL                  = "http://${var.public_host != "" ? var.public_host : "localhost"}/customer"
    }
  }
}

resource "docker_container" "customer_portal" {
  count = local.config_service_replica_count
  name  = "slms-customer-portal-prod-${count.index + 1}"
  image = docker_image.customer_portal.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  networks_advanced {
    name    = data.docker_network.slms_network.name
    aliases = ["slms-customer-portal-prod"]
  }

  restart = "unless-stopped"
}

# Driver PWA Frontend
resource "docker_image" "driver_pwa" {
  name         = "slms-driver-pwa:latest"
  keep_locally = true
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-driver-pwa:latest"]
    build_args = {
      FRONTEND_PATH                   = "src/frontend/driver-pwa"
      NEXT_PUBLIC_API_URL             = local.config_next_public_api_url
      NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN = local.config_mapbox_access_token
    }
  }
}

resource "docker_container" "driver_pwa" {
  count = local.config_service_replica_count
  name  = "slms-driver-pwa-prod-${count.index + 1}"
  image = docker_image.driver_pwa.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  networks_advanced {
    name    = data.docker_network.slms_network.name
    aliases = ["slms-driver-pwa-prod"]
  }

  restart = "unless-stopped"
}

# Landing Page Frontend
resource "docker_image" "landing_page" {
  name         = "slms-landing-page:latest"
  keep_locally = true
  build {
    context    = local.repo_root
    dockerfile = "infra/docker/frontend/Dockerfile.prod"
    tag        = ["slms-landing-page:latest"]
    build_args = {
      FRONTEND_PATH       = "src/frontend/landing-page"
      NEXT_PUBLIC_API_URL = local.config_next_public_api_url
    }
  }
}

resource "docker_container" "landing_page" {
  count = local.config_service_replica_count
  name  = "slms-landing-page-prod-${count.index + 1}"
  image = docker_image.landing_page.image_id

  env = [
    "NODE_ENV=production",
    "NEXT_TELEMETRY_DISABLED=1"
  ]

  working_dir = "/app"

  networks_advanced {
    name    = data.docker_network.slms_network.name
    aliases = ["slms-landing-page-prod"]
  }

  restart = "unless-stopped"
}
