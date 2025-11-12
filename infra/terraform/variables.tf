# PostgreSQL Configuration
variable "postgres_user" {
  description = "PostgreSQL username"
  type        = string
  default     = null
}

variable "postgres_password" {
  description = "PostgreSQL password"
  type        = string
  sensitive   = true
  default     = null
}

variable "postgres_db" {
  description = "PostgreSQL database name"
  type        = string
  default     = null
}

variable "postgres_host_port" {
  description = "Host port exposed for PostgreSQL"
  type        = number
  default     = null
}

# Keycloak Configuration
variable "keycloak_realm" {
  description = "Keycloak realm name"
  type        = string
  default     = null
}

variable "keycloak_client_id" {
  description = "Keycloak client ID"
  type        = string
  default     = null
}

variable "keycloak_client_secret" {
  description = "Keycloak client secret"
  type        = string
  sensitive   = true
  default     = null
}

variable "keycloak_hostname_url" {
  description = "Public Keycloak URL (e.g., http://auth.yourdomain.com:8180) - used for JWT issuer"
  type        = string
  default     = null
}

variable "keycloak_admin" {
  description = "Keycloak admin username"
  type        = string
  default     = null
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password"
  type        = string
  sensitive   = true
  default     = null
}

# MinIO Configuration
variable "minio_endpoint" {
  description = "MinIO endpoint URL"
  type        = string
  default     = null
}

variable "minio_access_key" {
  description = "MinIO access key"
  type        = string
  default     = null
}

variable "minio_secret_key" {
  description = "MinIO secret key"
  type        = string
  sensitive   = true
  default     = null
}

variable "minio_bucket_name" {
  description = "MinIO bucket name for documents"
  type        = string
  default     = null
}

# Shippo API Configuration
variable "shippo_api_key" {
  description = "Shippo API key"
  type        = string
  sensitive   = true
  default     = null
}

variable "shippo_base_url" {
  description = "Shippo API base URL"
  type        = string
  default     = null
}

# ShipEngine API Configuration
variable "shipengine_api_key" {
  description = "ShipEngine API key"
  type        = string
  sensitive   = true
  default     = null
}

variable "shipengine_base_url" {
  description = "ShipEngine API base URL"
  type        = string
  default     = null
}

# Docker Network
variable "docker_network_name" {
  description = "Existing Docker network that connects Terraform and Docker Compose services"
  type        = string
  default     = null
}

# Public Host Configuration
variable "public_host" {
  description = "Public hostname or IP for external access (e.g., AWS EC2 public IP). Defaults to localhost for local development."
  type        = string
  default     = ""
}

# Frontend Configuration
variable "next_public_api_url" {
  description = "Base API URL for frontend applications (e.g., http://localhost/api)"
  type        = string
  default     = null
}

variable "flagsmith_env_key" {
  description = "Flagsmith Environment Key for customer portal feature flags"
  type        = string
  sensitive   = true
  default     = null
}

variable "mapbox_access_token" {
  description = "Mapbox access token for driver PWA map functionality"
  type        = string
  sensitive   = true
  default     = null
}

# Service scaling
variable "service_replica_count" {
  description = "Number of replicas provisioned for each stateless service (backend and frontends)"
  type        = number
  default     = 2
}
