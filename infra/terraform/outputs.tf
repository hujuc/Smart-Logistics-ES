output "postgres_host" {
  description = "PostgreSQL container hostname"
  value       = docker_container.postgres.name
}

output "backend_url" {
  description = "Backend service URL"
  value       = "http://${var.public_host != "" ? var.public_host : "localhost"}:8080"
}

output "admin_console_url" {
  description = "Admin Console URL"
  value       = "http://${var.public_host != "" ? var.public_host : "localhost"}:3001"
}

output "customer_portal_url" {
  description = "Customer Portal URL"
  value       = "http://${var.public_host != "" ? var.public_host : "localhost"}:3002"
}

output "driver_pwa_url" {
  description = "Driver PWA URL"
  value       = "http://${var.public_host != "" ? var.public_host : "localhost"}:3003"
}

output "landing_page_url" {
  description = "Landing Page URL"
  value       = "http://${var.public_host != "" ? var.public_host : "localhost"}:3004"
}

output "nginx_url" {
  description = "Nginx reverse proxy URL"
  value       = "http://${var.public_host != "" ? var.public_host : "localhost"}"
}

output "network_name" {
  description = "Docker network name"
  value       = data.docker_network.slms_network.name
