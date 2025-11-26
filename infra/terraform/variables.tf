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

variable "jwt_issuer_uri" {
  description = "JWT Issuer URI for token validation (e.g., http://public-host:8180/realms/slms)"
  type        = string
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

# QR Code Configuration
variable "qr_confirmation_base_url" {
  description = "Base URL for QR code confirmation endpoints (e.g., http://yourdomain.com/api/driver)"
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

# Observability - Elastic APM / OpenTelemetry Configuration
variable "elastic_apm_server_url" {
  description = "Elastic APM Server URL for OpenTelemetry export (e.g., https://your-deployment.apm.elastic-cloud.com:443)"
  type        = string
  sensitive   = true
  default     = null
}

variable "elastic_apm_secret_token" {
  description = "Elastic APM Secret Token for authentication"
  type        = string
  sensitive   = true
  default     = null
}

variable "deployment_env" {
  description = "Deployment environment name (development, staging, production)"
  type        = string
  default     = "development"
}

variable "project_version" {
  description = "Application version for tracing metadata"
  type        = string
  default     = "0.0.1-SNAPSHOT"
}

variable "tracing_sampling_rate" {
  description = "Tracing sampling rate (0.0 to 1.0, where 1.0 = 100%)"
  type        = string
  default     = "1.0"
}

variable "log_level_root" {
  description = "Root logging level (TRACE, DEBUG, INFO, WARN, ERROR)"
  type        = string
  default     = "INFO"
}

variable "log_level_app" {
  description = "Application logging level"
  type        = string
  default     = "DEBUG"
}

variable "log_level_web" {
  description = "Spring Web logging level"
  type        = string
  default     = "INFO"
}

variable "log_level_security" {
  description = "Spring Security logging level"
  type        = string
  default     = "INFO"
}

# -----------------------------------------------------------------------------
# Chatbot Module - AI & Vector Database Configuration
# -----------------------------------------------------------------------------
variable "gemini_api_key" {
  description = "Google Gemini AI API key"
  type        = string
  sensitive   = true
  default     = null
}

variable "gemini_model" {
  description = "Google Gemini AI model name"
  type        = string
  default     = "gemini-2.0-flash"
}

variable "gemini_embedding_model" {
  description = "Google Gemini embedding model name"
  type        = string
  default     = "text-embedding-004"
}

variable "pinecone_api_key" {
  description = "Pinecone vector database API key"
  type        = string
  sensitive   = true
  default     = null
}

variable "pinecone_host" {
  description = "Pinecone vector database host URL"
  type        = string
  default     = null
}

variable "pinecone_index" {
  description = "Pinecone index name"
  type        = string
  default     = "slms-chatbot"
}

variable "pinecone_namespace" {
  description = "Pinecone namespace"
  type        = string
  default     = "en-US"
}
