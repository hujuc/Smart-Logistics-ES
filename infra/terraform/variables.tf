# PostgreSQL Configuration
variable "postgres_user" {
  description = "PostgreSQL username"
  type        = string
  default     = "slms_user"
}

variable "postgres_password" {
  description = "PostgreSQL password"
  type        = string
  sensitive   = true
}

variable "postgres_db" {
  description = "PostgreSQL database name"
  type        = string
  default     = "slms_db"
}

variable "postgres_host_port" {
  description = "Host port exposed for PostgreSQL"
  type        = number
  default     = 5432
}

# Keycloak Configuration
variable "keycloak_realm" {
  description = "Keycloak realm name"
  type        = string
  default     = "slms"
}

variable "keycloak_client_id" {
  description = "Keycloak client ID"
  type        = string
  default     = "slms-backend"
}

variable "keycloak_client_secret" {
  description = "Keycloak client secret"
  type        = string
  sensitive   = true
}

# MinIO Configuration
variable "minio_endpoint" {
  description = "MinIO endpoint URL"
  type        = string
  default     = "http://minio:9000"
}

variable "minio_access_key" {
  description = "MinIO access key"
  type        = string
  default     = "minioadmin"
}

variable "minio_secret_key" {
  description = "MinIO secret key"
  type        = string
  sensitive   = true
  default     = "minioadmin"
}

variable "minio_bucket_name" {
  description = "MinIO bucket name for documents"
  type        = string
  default     = "slms-documents"
}

# Shippo API Configuration
variable "shippo_api_key" {
  description = "Shippo API key"
  type        = string
  sensitive   = true
}

variable "shippo_base_url" {
  description = "Shippo API base URL"
  type        = string
  default     = "https://api.goshippo.com"
}

# ShipEngine API Configuration
variable "shipengine_api_key" {
  description = "ShipEngine API key"
  type        = string
  sensitive   = true
}

variable "shipengine_base_url" {
  description = "ShipEngine API base URL"
  type        = string
  default     = "https://api.shipengine.com/v1"
}

# Docker Network
variable "docker_network_name" {
  description = "Existing Docker network that connects Terraform and Docker Compose services"
  type        = string
  default     = "slms-network"
}
