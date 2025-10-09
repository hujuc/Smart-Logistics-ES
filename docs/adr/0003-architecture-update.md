# 0003 – Architecture Update

**Sprint 2**

## Context

After the initial architecture definition in Sprint 1 and beginning implementation work in Sprint 2, we needed to integrate key infrastructure components that were planned but not yet implemented:

- Database integration into the architecture
- Third-party carrier API integration strategy
- Containerization and deployment infrastructure
- Message broker for asynchronous operations

**Requirements for Sprint 2:**

- Integrate PostgreSQL database with proper schema and migrations
- Finalize carrier API integration approach (direct vs. aggregator)
- Containerize all components for consistent deployment
- Set up Kafka for event-driven messaging
- Define Docker Compose configuration for local development

**Constraints:**

- Must work with existing modular monolith architecture
- Limited budget for third-party services
- Team needs simple local development setup
- Must support future production deployment

**Options considered:**

- Direct carrier API integration vs. carrier aggregator service
- Self-hosted vs. managed database
- Docker vs. traditional deployment
- Kafka vs. RabbitMQ for messaging

## Decision

We implemented the following architectural updates in Sprint 2:

### 1. Database Integration

**PostgreSQL as Primary Database:**

- **Version:** PostgreSQL 16-alpine (Docker image)
- **Schema:** Comprehensive relational schema with 17 tables organized by domain
- **Access pattern:** Each backend module has its own repository layer accessing shared schema
- **Connection:** Spring Data JPA with connection pooling
- **Local setup:** Docker container via Docker Compose

**Database Design Decisions:**

- Shared database with logical schema separation (not database-per-module)
- UUID primary keys for all tables
- JSONB columns for flexible data (addresses, metadata, preferences)
- Comprehensive indexing strategy for common queries
- Foreign key constraints for referential integrity

### 2. Third-Party Carrier API Integration

**Decision: Use ShipEngine as Carrier Aggregator**

Instead of integrating directly with each carrier (FedEx, UPS, DHL), we chose to use **ShipEngine API** as a unified carrier aggregation service.

**Key ShipEngine Capabilities:**
- `/v1/carriers` - List all connected carriers
- `/v1/rates/estimate` - Quick price comparison across carriers
- `/v1/shipments` - Create shipment records
- `/v1/labels` - Generate shipping labels with tracking numbers
- `/v1/tracking/start` - Subscribe to tracking updates
- `/v1/environments/webhooks` - Register webhooks for real-time events

**Integration Architecture:**
- Carrier Integration Module calls ShipEngine REST API
- API key stored in environment variables
- Webhook endpoint receives tracking updates
- Circuit breaker pattern for API resilience

### 3. Containerization Strategy

**Docker Containers:**

All components are containerized for consistent deployment:

1. **Backend Container**
   - Base image: `eclipse-temurin:21-jre-alpine`
   - Spring Boot application packaged as JAR
   - Exposes port 8080
   - Environment variables for configuration

2. **Frontend Containers** (3 separate containers)
   - Customer Portal: `node:20-alpine` + Next.js build
   - Admin Console: `node:20-alpine` + Next.js build
   - Driver PWA: `node:20-alpine` + Next.js build

3. **Infrastructure Containers**
   - **PostgreSQL:** `postgres:16-alpine`
   - **Redis:** `redis:7-alpine` (planned for Sprint 3)
   - **MinIO:** `minio/minio:latest` (planned for Sprint 3)

Refer to updated architecture diagrams:
- [Updated Logic Architecture](../per-sprint/sprint2/architecture-update/logic-architecture.png)
- [Updated Installation Architecture](../per-sprint/sprint2/architecture-update/instalation-architecture.png)

## Consequences

**Positive:**

- **Database integration** - Persistent storage enables real production use
- **Simplified carrier integration** - ShipEngine provides unified API instead of managing 3+ carrier integrations
- **Faster development** - No need to learn FedEx, UPS, DHL APIs separately
- **Cost predictability** - ShipEngine pricing vs. individual carrier developer accounts
- **Containerization** - Consistent environments from dev to production
- **Easy local setup** - `docker compose up` starts entire stack
- **Scalability path** - Containers can be orchestrated with Kubernetes later
- **Webhook support** - Real-time tracking updates via ShipEngine webhooks

**Negative:**

- **Vendor dependency** - Reliance on ShipEngine as single point of failure
- **API costs** - ShipEngine charges per API call and shipment
- **Limited carrier control** - Cannot customize carrier-specific features
- **Docker overhead** - Additional complexity for developers unfamiliar with Docker
- **Database coupling** - Shared database creates tight coupling between modules

## Alternatives Considered

### Option 1: Direct Carrier API Integration
**Why not chosen:**
- Would require integrating with FedEx, UPS, DHL APIs separately
- Each carrier has different API design, authentication, data models
- Maintenance burden of keeping up with 3+ API versions
- Significantly more development time (estimated 3x longer)
- Individual carrier developer accounts and API keys required

### Option 2: AWS RDS for Database
**Why not chosen:**
- Higher costs for development/testing environments
- Requires AWS account setup and management
- Self-hosted PostgreSQL in Docker is sufficient for current scale
- Can migrate to managed database later when needed

## Related Decisions

- ADR-0001 – Initial System Architecture (Sprint 1)
- ADR-0002 – Data Model Definition (Sprint 2)

## Notes

- **ShipEngine Account:** Team registered for ShipEngine developer account with test credentials
- **Database Migrations:** Using Flyway for versioned database schema migrations
- **Docker Compose:** Separate compose files for development (`docker-compose.yml`) and production (`docker-compose.prod.yml`)
- **Environment Variables:** All sensitive configuration (DB passwords, API keys) externalized to `.env` file
- **Container Networking:** All containers on same Docker network for inter-service communication
- **Volume Persistence:** Database data persisted in Docker volumes to prevent data loss
- **ShipEngine Webhooks:** Webhook endpoint exposed at `/api/webhooks/shipengine` to receive tracking updates
- **Future Enhancements:** Redis caching and MinIO object storage planned for Sprint 3
- **Local Development:** Developers run `docker compose up` to start full stack; backend can also run directly via IntelliJ for debugging
