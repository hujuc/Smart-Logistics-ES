.PHONY: help dev-up dev-down dev-restart dev-logs prod-up prod-down prod-restart prod-logs clean

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
	@echo "Utility commands:"
	@echo "  make clean         - Remove all containers and volumes"

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

# Cleanup
clean:
	docker compose -f infra/docker-compose.dev.yml down -v
	docker compose -f infra/docker-compose.yaml down -v
