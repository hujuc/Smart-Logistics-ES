.PHONY: help up down restart logs status clean clean-volumes hard-prune dev-up dev-down dev-restart dev-logs prod-up prod-down prod-restart prod-logs test test-unit test-integration test-integration-minio test-integration-keycloak test-all test-services-up test-services-down

help:
	@echo "Production commands (Terraform + Docker Compose):"
	@echo "  make up            - Start/update production environment (smart deploy - reuses containers)"
	@echo "  make down          - Stop all services and destroy infrastructure"
	@echo "  make restart       - Restart all running services"
	@echo "  make logs          - View all logs"
	@echo "  make status        - Show running containers"
	@echo "  make clean         - Remove stopped containers only"
	@echo "  make clean-volumes - Remove containers and all data (destructive)"
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
	@echo "  make hard-prune    - Full Docker cleanup (removes everything)"

# Main deployment commands (Terraform + Docker Compose)
up:
	@echo "🚀 Starting/updating SLMS environment..."
	@echo "1️⃣  Creating Docker network..."
	@docker network inspect slms-network >/dev/null 2>&1 || docker network create slms-network
	@echo "2️⃣  Deploying with smart deployment (reuses existing containers)..."
	@cd infra && ./deploy.sh deploy
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
	@cd infra && ./deploy.sh down
	@echo "✅ All services stopped"

clean:
	@echo "🧹 Cleaning up stopped containers..."
	@docker compose -f infra/docker-compose.yaml down
	@docker container prune -f
	@echo "✅ Cleanup complete"

clean-volumes:
	@echo "🗑️  Removing all containers and volumes..."
	@cd infra && ./deploy.sh down
	@docker volume rm $$(docker volume ls -q --filter "name=slms-") 2>/dev/null || true
	@echo "✅ All data removed"

restart:
	@echo "🔄 Restarting all services..."
	@cd infra && ./deploy.sh restart
	@echo "✅ All services restarted"

logs:
	@echo "📋 Showing logs (Ctrl+C to exit)..."
	@cd infra && ./deploy.sh logs

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
	@echo "🧹 Cleaning up stopped containers..."
	@docker compose -f infra/docker-compose.yaml down
	@docker container prune -f
	@echo "✅ Cleanup complete"

clean-volumes:
	@echo "🗑️  Removing all containers and volumes..."
	@cd infra && ./deploy.sh down
	@docker volume rm $$(docker volume ls -q --filter "name=slms-") 2>/dev/null || true
	@echo "✅ All data removed"

hard-prune:
	docker system prune -a --volumes -f
	docker volume prune -f