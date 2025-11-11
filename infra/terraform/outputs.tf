output "postgres_host" {
  description = "PostgreSQL container hostname"
  value       = docker_container.postgres.name
}

output "backend_url" {
  description = "Backend service URL"
  value       = "http://localhost:8080"
}

output "admin_console_url" {
  description = "Admin Console URL"
  value       = "http://localhost:3001"
}

output "customer_portal_url" {
  description = "Customer Portal URL"
  value       = "http://localhost:3002"
}

output "driver_pwa_url" {
  description = "Driver PWA URL"
  value       = "http://localhost:3003"
}

output "landing_page_url" {
  description = "Landing Page URL"
  value       = "http://localhost:3004"
}

output "network_name" {
  description = "Docker network name"
  value       = data.docker_network.slms_network.name
}
