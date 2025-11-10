.PHONY: help up down restart logs status clean clean-volumes hard-prune dev-up dev-down dev-restart dev-logs prod-up prod-down prod-restart prod-logs test test-unit test-integration test-integration-minio test-integration-keycloak test-all test-services-up test-services-down

help:
	@echo "Production commands (Terraform + Docker Compose):"
	@echo "  make up            - Start complete production environment (Terraform + Docker Compose)"
	@echo "  make down          - Stop all services"
	@echo "  make restart       - Restart all services"
	@echo "  make logs          - View all logs"
	@echo "  make status        - Show running containers"
	@echo ""
	@echo "Development commands (Docker Compose only):"
	@echo "  make dev-up        - Start development environment"
	@echo "  make dev-down      - Stop development environment"
	@echo "  make dev-restart   - Restart development environment"
	@echo "  make dev-logs      - View development logs"
	@echo ""
	@echo "Test commands:"
	@echo "  make test          - Run all unit tests and integration tests"
	@echo ""
	@echo "Utility commands:"
	@echo "  make clean         - Remove all containers and volumes"
	@echo "  make clean-volumes - Remove containers and volumes (including data)"
	@echo "  make hard-prune    - Full Docker cleanup (removes everything)"

# Main deployment commands (Terraform + Docker Compose)
up:
	@echo "🚀 Starting complete SLMS environment..."
	@echo "1️⃣  Creating Docker network..."
	@docker network inspect slms-network >/dev/null 2>&1 || docker network create slms-network
	@echo "2️⃣  Starting supporting services (Keycloak, MinIO, Nginx)..."
	@docker compose -f infra/docker-compose.yaml up -d
	@echo "3️⃣  Waiting for services to be ready..."
	@sleep 5
	@echo "4️⃣  Deploying application with Terraform..."
	@rm -f infra/terraform/.terraform.tfstate.lock.info
	@cd infra/terraform && terraform init -upgrade && terraform apply -auto-approve
	@echo "5️⃣  Restarting Nginx to refresh DNS..."
	@docker restart slms-nginx-dev
	@echo ""
	@echo "✅ Deployment complete!"
	@echo ""
	@echo "📍 Access URLs:"
	@echo "   Landing Page:    http://localhost/"
	@echo "   Customer Portal: http://localhost/customer/"
	@echo "   Admin Console:   http://localhost/admin/"
	@echo "   Driver PWA:      http://localhost/driver/"
	@echo ""
	@echo "🔑 Test credentials: customer/customer"
	@echo ""
	@echo "📊 Check status: make status"
	@echo "📋 View logs:    make logs"

down:
	@echo "🛑 Stopping all services..."
	@rm -f infra/terraform/.terraform.tfstate.lock.info
	@cd infra/terraform && terraform destroy -auto-approve || true
	@docker compose -f infra/docker-compose.yaml down
	@echo "✅ All services stopped"

restart:
	@echo "🔄 Restarting all services..."
	@docker compose -f infra/docker-compose.yaml restart
	@docker restart $$(docker ps -q --filter "name=slms-") 2>/dev/null || true
	@echo "✅ All services restarted"

logs:
	@echo "📋 Showing logs (Ctrl+C to exit)..."
	@docker compose -f infra/docker-compose.yaml logs -f &
	@docker logs -f slms-backend-dev 2>/dev/null || true

status:
	@echo "📊 SLMS Services Status:"
	@echo ""
	@docker ps --filter "name=slms-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
	@echo ""
	@echo "💾 Volumes:"
	@docker volume ls --filter "name=slms-" --format "table {{.Name}}\t{{.Driver}}"

# Development environment
dev-up:
	docker compose -f infra/docker-compose.dev.yml up --build -d

dev-down:
	docker compose -f infra/docker-compose.dev.yml down

dev-restart:
	docker compose -f infra/docker-compose.dev.yml restart

dev-logs:
	docker compose -f infra/docker-compose.dev.yml logs -f

# Test commands
test:
	@echo "Running ALL tests (unit + integration)..."
	@echo "Make sure services are running: make test-services-up"
	cd src/backend && mvn clean test

# Cleanup
clean:
	docker compose -f infra/docker-compose.dev.yml down
	docker compose -f infra/docker-compose.yaml down

clean-volumes:
	docker compose -f infra/docker-compose.dev.yml down -v
	docker compose -f infra/docker-compose.yaml down -v

hard-prune:
	docker system prune -a --volumes -f
	docker volume prune -f