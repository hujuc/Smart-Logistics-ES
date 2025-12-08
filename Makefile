.PHONY: help up down destroy restart logs status clean clean-volumes rebuild hard-prune dev-up dev-down dev-restart dev-logs test test-ci test-unit test-backend-integration test-e2e test-e2e-ci test-real build install

help:
	@echo "Production commands (Terraform + Docker Compose):"
	@echo "  make up            - Start/update production environment (smart deploy - reuses containers)"
	@echo "  make down          - Stop all services (non-destructive, keeps infrastructure)"
	@echo "  make destroy       - Stop and destroy all infrastructure (destructive)"
	@echo "  make restart       - Restart all running services"
	@echo "  make logs          - View logs (default: backend, add SERVICE=all for all logs)"
	@echo "  make status        - Show running containers and health status"
	@echo "  make rebuild       - Force rebuild all Docker images (ignores cache)"
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
	@echo "  make test-ci       - Run unit + mocked integration tests with JaCoCo coverage gate"
	@echo "  make test-unit     - Run backend unit tests only (fast)"
	@echo "  make test-backend-integration - Run CI-safe integration tests (Docker: Keycloak, MinIO, Kafka)"
	@echo "  make test-e2e      - Run frontend e2e tests (with install)"
	@echo "  make test-e2e-ci   - Run frontend e2e tests (CI mode, no install)"
	@echo "  make test-real     - [LOCAL DEV] Run tests with real external APIs (requires API keys)"
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
	@docker container prune -f
	@echo "Cleanup complete"

clean-volumes-prod:
	@echo "Removing all containers and volumes..."
	@cd infra && ./deploy.sh down
	@docker volume rm $$(docker volume ls -q --filter "name=slms-") 2>/dev/null || true
	@echo "All data removed"

clean-volumes-dev:
	@echo "Removing all development containers and volumes..."
	@docker compose -f infra/docker-compose.dev.yml down -v
	@echo "All development data removed"

restart:
	@echo "Restarting all services..."
	@cd infra && ./deploy.sh restart
	@echo "All services restarted"

logs:
	@echo "Showing logs (Ctrl+C to exit)..."
	@cd infra && ./deploy.sh logs $(SERVICE)

rebuild:
	@echo "Force rebuilding all Docker images..."
	@cd infra && ./deploy.sh rebuild
	@echo "Rebuild complete!"

status:
	@echo "===== SLMS Services Status ====="
	@echo ""
	@echo "Containers (with health status):"
	@docker ps --filter "name=slms-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null || echo "No containers running"
	@echo ""
	@echo "Volumes:"
	@docker volume ls --filter "name=slms-" --format "table {{.Name}}\t{{.Driver}}" 2>/dev/null || echo "No volumes found"
	@echo ""
	@echo "Terraform-managed resources:"
	@cd infra/terraform && terraform state list 2>/dev/null | wc -l | xargs -I {} echo "  {} resources managed" || echo "  Terraform not initialized"
	@echo ""
	@echo "Network:"
	@docker network inspect slms-network --format "  {{.Name}}: {{len .Containers}} containers connected" 2>/dev/null || echo "  slms-network: not found"

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
	@echo "Installing frontend dependencies (using npm ci for reproducibility)..."
	@echo "Installing landing-page dependencies..."
	@cd src/frontend/landing-page && npm ci
	@echo "Installing admin-console dependencies..."
	@cd src/frontend/admin-console && npm ci
	@echo "Installing customer-portal dependencies..."
	@cd src/frontend/customer-portal && npm ci
	@echo "Installing driver-pwa dependencies..."
	@cd src/frontend/driver-pwa && npm ci
	@echo "Installing Playwright browsers (for e2e tests)..."
	@cd src/frontend/landing-page && npx playwright install chromium
	@cd src/frontend/admin-console && npx playwright install chromium
	@cd src/frontend/customer-portal && npx playwright install chromium
	@echo "Dependencies installed successfully!"
	@echo ""
	@echo "NOTE: If Playwright tests fail due to missing system dependencies,"
	@echo "      run: sudo npx playwright install-deps chromium"
	@echo ""
	@echo "TIP: To add new packages, run 'npm install <package>' directly in the frontend directory"

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
test:
	@echo "Running backend tests (unit + integration)..."
	@cd src/backend && mvn clean test
	@echo "All tests complete!"

test-unit:
	@echo "Running backend unit tests only..."
	@cd src/backend && mvn clean test -Dtest="**/*Test.java" -DexcludedGroups=real-integration

test-e2e: install
	@echo "Running frontend e2e tests..."
	@cd src/frontend/landing-page && npm run test:e2e
	@cd src/frontend/admin-console && npm run test:e2e
	@cd src/frontend/customer-portal && npm run test:e2e
	@echo "E2E tests complete!"

test-e2e-ci:
	@echo "Running frontend e2e tests (CI mode - no install)..."
	@cd src/frontend/landing-page && npm run test:e2e
	@cd src/frontend/admin-console && npm run test:e2e
	@cd src/frontend/customer-portal && npm run test:e2e
	@echo "E2E tests complete!"

test-backend-integration:
	@echo "Running CI-safe integration tests (requires Keycloak, MinIO, Kafka)..."
	@cd src/backend && mvn test -Dgroups=ci-integration
	@echo "CI-safe integration tests complete!"

test-real:
	@echo "Running tests with real external services..."
	@echo "NOTE: Requires running backend service (make dev-up)"
	@echo "  - Gemini API (requires GEMINI_API_KEY in infra/.env)"
	@echo "  - Pinecone API (requires PINECONE_API_KEY in infra/.env)"
	@docker exec slms-backend-dev /bin/sh -c "cd /app && ./mvnw test -Preal-integration"
	@echo "Real integration tests complete!"

test-ci:
	@echo "Running CI tests with quality gates (JaCoCo coverage)..."
	cd src/backend && mvn -B -ntp -DskipTests=false -Dtest='!**/*IntegrationTest' clean verify

# Cleanup
hard-prune:
	docker system prune -a --volumes -f
	docker volume prune -f