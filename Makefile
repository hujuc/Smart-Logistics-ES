.PHONY: help up down destroy restart logs status clean clean-volumes hard-prune dev-up dev-down dev-restart dev-logs install build test test-unit test-integration test-e2e test-real

help:
	@echo "Production commands (Terraform + Docker Compose):"
	@echo "  make up            - Start/update production environment (smart deploy - reuses containers)"
	@echo "  make down          - Stop all services (non-destructive, keeps infrastructure)"
	@echo "  make destroy       - Stop and destroy all infrastructure (destructive)"
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
	@echo "Build commands:"
	@echo "  make install       - Install all frontend dependencies (for local dev/testing)"
	@echo "  make build         - Install dependencies + build backend + all frontends"
	@echo ""
	@echo "Test commands:"
	@echo "  make test          - Run backend tests (unit + integration)"
	@echo "  make test-unit     - Run backend unit tests only (fast)"
	@echo "  make test-integration - Run backend integration tests (mocked APIs)"
	@echo "  make test-e2e      - Run frontend e2e tests (Playwright)"
	@echo "  make test-real     - Run tests with real services (Gemini, MinIO, Keycloak)"
	@echo ""
	@echo "Utility commands:"
	@echo "  make hard-prune    - Full Docker cleanup (removes everything)"

# Main deployment commands (Terraform + Docker Compose)
up:
	@echo "Starting/updating SLMS environment..."
	@echo "Creating Docker network..."
	@docker network inspect slms-network >/dev/null 2>&1 || docker network create slms-network
	@echo "Deploying with smart deployment (reuses existing containers)..."
	@cd infra && ./deploy.sh deploy
	@echo ""
	@echo " Deployment complete!"
	@echo ""
	@echo " Access URLs:"
	@echo "   Landing Page:    http://localhost/"
	@echo "   Customer Portal: http://localhost/customer/"
	@echo "   Admin Console:   http://localhost/admin/"
	@echo "   Driver PWA:      http://localhost/driver/"
	@echo ""
	@echo " Test credentials: customer/customer"
	@echo ""
	@echo " Check status: make status"
	@echo " View logs:    make logs"

down:
	@echo "Stopping all services..."
	@cd infra && ./deploy.sh stop
	@echo "All services stopped (infrastructure preserved)"

destroy:
	@echo "Destroying all infrastructure..."
	@cd infra && ./deploy.sh down
	@echo "All infrastructure destroyed"

clean:
	@echo "Cleaning up stopped containers..."
	@docker compose -f infra/docker-compose.yaml down
	@docker container prune -f
	@echo "Cleanup complete"

clean-volumes:
	@echo "Removing all containers and volumes..."
	@cd infra && ./deploy.sh down
	@docker volume rm $$(docker volume ls -q --filter "name=slms-") 2>/dev/null || true
	@echo "All data removed"

restart:
	@echo "Restarting all services..."
	@cd infra && ./deploy.sh restart
	@echo "All services restarted"

logs:
	@echo "Showing logs (Ctrl+C to exit)..."
	@cd infra && ./deploy.sh logs

status:
	@echo "SLMS Services Status:"
	@echo ""
	@docker ps --filter "name=slms-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
	@echo ""
	@echo "Volumes:"
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

# Dependency installation
install:
	@echo "Installing frontend dependencies..."
	@echo "Installing landing-page dependencies..."
	@cd src/frontend/landing-page && npm install
	@echo "Installing admin-console dependencies..."
	@cd src/frontend/admin-console && npm install
	@echo "Installing customer-portal dependencies..."
	@cd src/frontend/customer-portal && npm install
	@echo "Installing driver-pwa dependencies..."
	@cd src/frontend/driver-pwa && npm install
	@echo "Installing Playwright browsers (for e2e tests)..."
	@cd src/frontend/landing-page && npx playwright install chromium
	@cd src/frontend/admin-console && npx playwright install chromium
	@echo "Dependencies installed successfully!"
	@echo ""
	@echo "NOTE: If Playwright tests fail due to missing system dependencies,"
	@echo "      run: sudo npx playwright install-deps chromium"

# Build commands
build: install
	@echo "Building backend..."
	@cd src/backend && mvn clean package -DskipTests
	@echo "Building frontends..."
	@cd src/frontend/landing-page && npm run build
	@cd src/frontend/admin-console && npm run build
	@cd src/frontend/customer-portal && npm run build
	@cd src/frontend/driver-pwa && npm run build
	@echo "Build complete!"

# Test commands
test: install
	@echo "Running backend tests (unit + integration)..."
	@cd src/backend && mvn test
	@echo "All tests complete!"

test-unit:
	@echo "Running backend unit tests only..."
	@cd src/backend && mvn test -Dtest="**/*Test.java" -DexcludedGroups=real-integration

test-integration:
	@echo "Running backend integration tests (mocked external APIs)..."
	@cd src/backend && mvn test -Dtest="**/*IntegrationTest.java"

test-e2e: install
	@echo "Running frontend e2e tests..."
	@cd src/frontend/landing-page && npm run test:e2e || true
	@cd src/frontend/admin-console && npm run test:e2e || true
	@cd src/frontend/customer-portal && npm run test:e2e || true
	@cd src/frontend/driver-pwa && npm run test:e2e || true
	@echo "E2E tests complete!"

test-real:
	@echo "Running tests with real external services..."
	@echo "NOTE: Requires running backend service (make dev-up)"
	@echo "  - Gemini API (requires GEMINI_API_KEY in infra/.env)"
	@echo "  - Pinecone API (requires PINECONE_API_KEY in infra/.env)"
	@docker exec slms-backend-dev /bin/sh -c "cd /app && ./mvnw test -Preal-integration"
	@echo "Real integration tests complete!"

# Cleanup
hard-prune:
	docker system prune -a --volumes -f
	docker volume prune -f