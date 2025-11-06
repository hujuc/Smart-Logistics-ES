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

variable "env_file" {
  description = "Path to the dotenv file providing default values when variables are unset"
  type        = string
  default     = "../.env"
}
