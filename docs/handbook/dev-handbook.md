# Developer Handbook

## What is SLMS?

**Smart Logistics Management System (SLMS)** is a comprehensive logistics platform that connects multiple actors in the shipping ecosystem: warehouse staff, carriers, customer service representatives, drivers, and customers.

### Core Capabilities

- **Order Management** - Register orders, generate labels and documents, track order lifecycle
- **Carrier Integration** - Compare rates and create shipments via ShipEngine API
- **Shipment Management** - Assign orders to carriers and drivers, track shipment details
- **Exception Handling** - Report and resolve delivery issues
- **AI Assistant** - Context-aware help with role-based and route-based filtering
- **Multi-warehouse Support** - Manage orders across multiple warehouse locations

## Architecture Overview

### Backend - Modular Monolith

**Single Spring Boot application** with clear module boundaries:

- **Order Module** - Order creation, document generation, status tracking, warehouse management
- **Rate Carrier Module** - ShipEngine API integration, rate comparison, carrier and shipment management
- **Visibility Module** - Delivery exception tracking and resolution
- **Chatbot Module** - AI assistant with context-aware help, vector storage integration
- **User Management Module** - Keycloak integration for authentication and authorization
- **Notification Module** - Limited implementation
- **Reporting Module** - Limited implementation
- **Route Module** - Limited implementation

**Tech Stack:**
- Spring Boot 3.5.6 + Java 21
- Spring Data JPA + PostgreSQL

### Frontend - Three Separate Applications

**1. Customer Portal** (Next.js)
- Public-facing order tracking
- Delivery preferences
- Problem reporting

**2. Admin Console** (Next.js)
- Warehouse staff operations
- Customer service dashboard
- Manager analytics

**3. Driver PWA** (Next.js)
- Delivery manifest
- Proof of delivery capture
- Route navigation

Each frontend has its own **BFF (Backend-for-Frontend)** layer.

### Infrastructure Components

- **PostgreSQL 16** - Primary database (7-8 tables, relational schema)
- **Keycloak** - Authentication and user management
- **ShipEngine API** - Unified carrier integration (FedEx, UPS, DHL)
- **Gemini API** - LLM for AI assistant functionality
- **Pinecone** - Vector database for semantic search
- **MinIO** - Object storage for documents and images
- **Docker** - All components containerized
- **Nginx** - Reverse proxy and load balancer

## Getting Started

### Prerequisites
- Java 21
- Node.js 20+
- Docker & Docker Compose
- Make (build automation)

### Local Setup

1. Copy `infra/.env.example` to `infra/.env` and fill required values (Keycloak, MinIO, API keys)

2. Start the development environment:
   ```bash
   make up
   ```
   This command:
   - Creates Docker network (slms-network)
   - Runs Terraform to provision infrastructure
   - Builds and starts all services (backend, 4 frontends, PostgreSQL, Keycloak, MinIO, Nginx)
   - Smart deployment: reuses existing containers when possible

3. Access the applications:
   - Landing Page: http://localhost/
   - Customer Portal: http://localhost/customer/
   - Admin Console: http://localhost/admin/
   - Driver PWA: http://localhost/driver/

### Common Commands

**Development:**
```bash
make status          # Check running containers and health status
make logs            # View logs (default: backend only)
make logs SERVICE=all # View all service logs
make restart         # Restart all services
make down            # Stop services (keeps infrastructure)
make destroy         # Stop and remove all infrastructure
```

**Development Environment (Docker Compose only):**
```bash
make dev-up          # Start dev environment
make dev-down        # Stop dev environment
make dev-restart     # Restart dev environment
make dev-logs        # View dev logs
```

**Build & Test:**
```bash
make install         # Install frontend dependencies
make build           # Build backend + all frontends
make test-ci         # Run unit + mocked integration tests with JaCoCo coverage gate
make test-backend-integration  # Run CI-safe integration tests (Docker: Keycloak, MinIO, Kafka)
make test-e2e        # Run frontend e2e tests (Playwright - requires services running via make up)
make test-real       # [MANUAL] Run tests with real external APIs (Gemini, Pinecone)
```

For detailed test organization and CI/CD pipeline documentation, see [CI/CD Pipeline](ci-cd-pipeline.md).

**Cleanup:**
```bash
make clean           # Remove stopped containers
make clean-volumes   # Remove containers and all data (destructive)
make hard-prune      # Full Docker cleanup (removes everything)
```

**Help:**
```bash
make help            # Show all available commands
```

## Project Structure

```bash
group-project-es2526_304/
├── docs/                      # Documentation
│   ├── adr/                   # Architecture Decision Records (ADR-0001 through ADR-0008)
│   ├── handbook/              # Developer guides (data-model, chatbot, observability)
│   └── per-sprint/            # Sprint-by-sprint historical documentation
│
├── infra/                     # Infrastructure as Code
│   ├── docker-compose.dev.yml # Development environment
│   ├── docker-compose.prod.yml # Production environment
│   ├── terraform/             # Terraform configurations
│   └── deploy.sh              # Deployment automation script
│
├── src/
│   ├── backend/               # Spring Boot 3.5 + Java 21 (Modular Monolith)
│   │   └── src/main/java/Operimus/SLMS/
│   │       ├── SlmsApplication.java  # Main application entry point
│   │       │
│   │       ├── business_layer/       # Domain logic organized by module
│   │       │   │
│   │       │   ├── order_module/     # Order management
│   │       │   │   ├── controllers/  # REST endpoints (/api/orders)
│   │       │   │   ├── dto/          # Data transfer objects
│   │       │   │   ├── entities/     # JPA entities (Order, OrderEvent, Warehouse)
│   │       │   │   ├── repository/   # Spring Data JPA repositories
│   │       │   │   └── service/      # Business logic (OrderService, OrderDocumentService)
│   │       │   │
│   │       │   ├── rate_carrier_module/  # Carrier integration and shipments
│   │       │   │   ├── config/       # Module configuration
│   │       │   │   ├── controllers/  # REST endpoints (/api/carriers, /api/shipments)
│   │       │   │   ├── dto/          # DTOs for carriers, rates, shipments
│   │       │   │   ├── entities/     # JPA entities (Carrier, Shipment)
│   │       │   │   ├── integration/  # External API clients (ShipEngine, Shippo)
│   │       │   │   ├── repository/   # Data access layer
│   │       │   │   └── service/      # Business logic (CarrierService, ShipmentService, ShippingRateService)
│   │       │   │
│   │       │   ├── chatbot_module/   # AI assistant functionality
│   │       │   │   ├── client/       # External API clients (Gemini, Pinecone)
│   │       │   │   ├── config/       # Module configuration
│   │       │   │   ├── controllers/  # REST endpoints (/api/assistant)
│   │       │   │   ├── dto/          # Request/response DTOs
│   │       │   │   ├── entities/     # JPA entities (AssistantDocument)
│   │       │   │   ├── repository/   # Data access layer
│   │       │   │   └── service/      # Business logic (ChatbotService, DocumentService)
│   │       │   │
│   │       │   ├── visibility_module/    # Tracking and exceptions
│   │       │   │   ├── exceptions/
│   │       │   │   │   ├── controllers/  # Exception management endpoints
│   │       │   │   │   ├── dto/
│   │       │   │   │   ├── entities/     # DeliveryException entity
│   │       │   │   │   ├── repository/
│   │       │   │   │   └── service/
│   │       │   │   └── tracking/         # Shipment tracking (future)
│   │       │   │
│   │       │   ├── user_management_module/  # Keycloak integration
│   │       │   │   ├── config/       # Security configuration
│   │       │   │   └── service/      # User/role services
│   │       │   │
│   │       │   ├── notification_module/   # Notification system (limited implementation)
│   │       │   ├── reporting_module/      # Analytics and reports (limited implementation)
│   │       │   ├── route_module/          # Route optimization (limited implementation)
│   │       │   └── shared/                # Shared utilities and exceptions
│   │       │
│   │       ├── presentation_layer/   # BFF (Backend-for-Frontend) pattern
│   │       │   ├── admin_console_bff/
│   │       │   │   ├── logistics_bff/    # Logistics manager endpoints
│   │       │   │   ├── warehouse_bff/    # Warehouse staff endpoints
│   │       │   │   └── customer_service_bff/  # CSR endpoints
│   │       │   ├── customer_portal_bff/  # Customer-facing endpoints
│   │       │   ├── driver_pwa_bff/       # Driver mobile endpoints
│   │       │   ├── landing_page_bff/     # Public marketing endpoints
│   │       │   └── shared/               # Common BFF utilities
│   │       │
│   │       └── observability/        # Monitoring and tracing
│   │           ├── apm/              # Elastic APM configuration
│   │           └── logging/          # Structured logging
│   │
│   └── frontend/                     # Three Next.js 15 applications
│       ├── admin-console/            # Admin Console (logistics, warehouse, CSR)
│       ├── customer-portal/          # Customer Portal (order tracking)
│       ├── driver-pwa/               # Driver PWA (delivery management)
│       ├── landing-page/             # Public landing page
│       └── shared/                   # Shared components and utilities
│           ├── components/           # Reusable React components
│           └── lib/                  # Shared libraries (observability, auth)
│
├── Makefile                          # Build automation and commands
└── README.md                         # Project overview
```

### Backend Architecture Notes

**Module Organization:**
- Each module is self-contained with its own controllers, services, entities, and repositories
- Modules communicate via service interfaces, not direct dependencies
- Cross-module entity references use ID fields (Long/UUID) instead of entity references

**Layering Pattern:**
- **Controllers:** HTTP endpoints, request validation, response mapping
- **Services:** Business logic, transaction management, orchestration
- **Repositories:** Data access, database queries
- **DTOs:** API contracts, decoupled from entities
- **Entities:** JPA entities, database mapping

**BFF Pattern:**
- Each frontend has dedicated BFF endpoints
- BFFs aggregate data from multiple business modules
- Role-based endpoints (logistics, warehouse, customer service) within admin console BFF

**Testing:**
- Unit tests: `src/backend/src/test/java/` (mirrors main structure)
- Integration tests: Mocked external services
- Real integration tests: With actual Gemini/Pinecone/MinIO services

## Key Documentation

- [Architecture Decision Records](../adr/)
  - [ADR-0001: Initial System Architecture](../adr/0001-initial-system-architecture.md)
  - [ADR-0002: Data Model Definition](../adr/0002-data-model-definition.md)
  - [ADR-0003: Architecture Update](../adr/0003-architecture-update.md)
- [Data Model](data-model.md)
- [Repository Workflow](repo-workflow.md)
- [QA Manual](qa-manual.md)
