# Infrastructure Setup

## Quick Start

### Development Environment

```bash
# From project root
make dev-up        # Start services
make dev-logs      # View logs
make dev-down      # Stop services
```

### Production Environment

```bash
# From project root
cp infra/.env.example infra/.env  # Configure environment
make prod-up       # Start services
make prod-logs     # View logs
make prod-down     # Stop services
```

## Environment Configuration

### Backend (`src/backend/.env`)

Configure database and JPA settings:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JPA_DDL_AUTO`, `JPA_SHOW_SQL`, `JPA_FORMAT_SQL`

### Infrastructure (`infra/.env`)

Configure for production deployment:

- PostgreSQL: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
- Backend: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `BACKEND_PORT`
- Node: `NODE_ENV`

## Access URLs

| Service | URL |
|---------|-----|
| Admin Console | <http://localhost/admin> |
| Customer Portal | <http://localhost/customer> |
| Driver PWA | <http://localhost/driver> |
| Backend API | <http://localhost/api/> |

## Available Commands

```bash
make help          # Show all commands
make dev-restart   # Restart development services
make prod-restart  # Restart production services
make clean         # Remove containers and volumes
```

## Services

- **PostgreSQL** (port 5433): Database
- **Backend** (port 8080): Spring Boot API
- **Frontend Apps** (port 3000 each): Next.js applications
- **Nginx** (port 80): Reverse proxy
