.PHONY: help dev-up dev-down dev-restart dev-logs prod-up prod-down prod-restart prod-logs clean test test-unit test-integration test-integration-minio test-integration-keycloak test-all test-services-up test-services-down

help:
	@echo "Development commands:"
	@echo "  make dev-up        - Start development environment"
	@echo "  make dev-down      - Stop development environment"
	@echo "  make dev-restart   - Restart development environment"
	@echo "  make dev-logs      - View development logs"
	@echo ""
	@echo "Production commands:"
	@echo "  make prod-up       - Start production environment"
	@echo "  make prod-down     - Stop production environment"
	@echo "  make prod-restart  - Restart production environment"
	@echo "  make prod-logs     - View production logs"
	@echo ""
	@echo "Test commands:"
	@echo "  make test                      - Run all unit tests (no external services needed)"
	@echo "  make test-unit                 - Run unit tests only"
	@echo "  make test-integration          - Run all integration tests (requires services)"
	@echo "  make test-integration-minio    - Run MinIO integration tests"
	@echo "  make test-integration-keycloak - Run Keycloak integration tests"
	@echo "  make test-all                  - Run all tests including integration"
	@echo "  make test-services-up          - Start MinIO and Keycloak for testing"
	@echo "  make test-services-down        - Stop test services"
	@echo ""
	@echo "Utility commands:"
	@echo "  make clean         - Remove all containers and volumes"
	@echo ""
	@echo "Note: Database backups run automatically at 2:00 AM daily (keeps last 3)"

# Development environment
dev-up:
	docker compose -f infra/docker-compose.dev.yml up --build -d

dev-down:
	docker compose -f infra/docker-compose.dev.yml down

dev-restart:
	docker compose -f infra/docker-compose.dev.yml restart

dev-logs:
	docker compose -f infra/docker-compose.dev.yml logs -f

# Production environment
prod-up:
	docker compose -f infra/docker-compose.yaml up --build -d

prod-down:
	docker compose -f infra/docker-compose.yaml down

prod-restart:
	docker compose -f infra/docker-compose.yaml restart

prod-logs:
	docker compose -f infra/docker-compose.yaml logs -f

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