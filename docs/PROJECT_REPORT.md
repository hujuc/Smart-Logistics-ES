# Smart Logistics Management System (SLMS)
## Project Report - Group 304

**Course:** Software Engineering
**Academic Year:** 2025/2026
**Institution:** University of Aveiro - DETI

---

## Team

| **Role**        | **Name**           | **NMEC** |
|-----------------|--------------------|----------|
| Scrum Master    | João Roldão        | 113920   |
| Product Owner   | Hugo Castro        | 113889   |
| QA Engineer     | Hugo Sousa         | 112733   |
| Service Analyst | Henrique Teixeira  | 114588   |
| DevOps Master   | João Varela        | 113780   |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Introduction](#2-introduction)
3. [System Architecture](#3-system-architecture)
4. [Data Model](#4-data-model)
5. [User Requirements](#5-user-requirements)
6. [Developer Onboarding](#6-developer-onboarding)
7. [Production Configuration](#7-production-configuration)
8. [Security & Authentication](#8-security--authentication)
9. [Key Features & Modules](#9-key-features--modules)
10. [AI-Powered Chatbot](#10-ai-powered-chatbot)
11. [Testing & Quality Assurance](#11-testing--quality-assurance)
12. [Observability & Monitoring](#12-observability--monitoring)
13. [Lessons Learned & Future Work](#13-lessons-learned--future-work)
14. [Conclusion](#14-conclusion)
15. [Appendices](#15-appendices)

---

# 1. Executive Summary

*[2-3 pages of original content]*

## 1.1 Project Overview

**Topics to cover:**
- Brief description of SLMS purpose and scope
- Problem statement: e-commerce logistics challenges
- Target users: 5 personas across the logistics ecosystem
- Core value proposition

**Content placeholder:**
> The Smart Logistics Management System (SLMS) is a comprehensive platform designed to address critical challenges in modern e-commerce logistics...

## 1.2 Key Objectives Achieved

**Topics to cover:**
- Real-time order tracking and monitoring
- Multi-carrier integration (FedEx, UPS, DHL via ShipEngine)
- Role-based access for 5 user types
- AI-powered chatbot assistant
- Comprehensive observability and monitoring

## 1.3 System Capabilities Overview

**Topics to cover:**
- Order management workflow
- Carrier rate comparison and selection
- Delivery exception handling
- Proof of delivery (POD) capture
- KPI dashboards and reporting
- Context-aware AI assistance

## 1.4 Technology Stack Summary

**Topics to cover:**
- Backend: Spring Boot 3.5.6 + Java 21
- Frontend: Next.js 15 + React 19 (4 applications)
- Database: PostgreSQL 16
- Infrastructure: Docker, Terraform, Nginx
- External integrations: Keycloak, MinIO, ShipEngine, Gemini, Pinecone, Elastic APM

## 1.5 Team Organization & Methodology

**Topics to cover:**
- Agile/Scrum approach with defined roles
- Sprint-based development (4+ sprints documented)
- Collaborative practices and tools
- Documentation-driven development

---

# 2. Introduction

*[4-5 pages of original content with references]*

## 2.1 Background and Motivation

**Topics to cover:**
- E-commerce growth and logistics complexity
- Traditional logistics pain points:
  - Lack of real-time visibility
  - Fragmented carrier networks
  - Manual processes and inefficiencies
  - Poor customer communication
- Market needs analysis

**References:**
- `docs/per-sprint/sprint1/project-specification.md` - Background section
- `docs/per-sprint/sprint1/requirements.md`

**Content guidance:**
> The exponential growth of e-commerce has transformed logistics into one of the most critical components of business competitiveness. Traditional systems fail to provide...

## 2.2 Project Objectives

**Topics to cover:**
- Enable logistics managers to track KPIs and compare carriers
- Streamline warehouse operations with automation
- Improve customer service efficiency
- Empower drivers with mobile tools
- Provide customer transparency
- Ensure structured exception management
- Facilitate system-wide integration

**Reference:** `docs/per-sprint/sprint1/project-specification.md` (Section 2)

## 2.3 Scope and Deliverables

**Topics to cover:**
- Core functionalities delivered
- User roles supported (5 personas)
- System boundaries and out-of-scope items
- Sprint-based feature delivery
- 18 user stories (12 completed, 5 in progress, 1 pending)

**References:**
- `docs/per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md`
- Sprint planning documents

## 2.4 Team Organization

**Topics to cover:**
- Role definitions and responsibilities
  - Scrum Master: Sprint coordination, impediment removal
  - Product Owner: Backlog management, stakeholder liaison
  - QA Engineer: Testing strategy, quality gates
  - Service Analyst: Requirements analysis, user stories
  - DevOps Master: Infrastructure, CI/CD, deployment
- Development methodology: Agile/Scrum
- Sprint planning and execution approach
- Tools used: Git, GitHub, Docker, Terraform

**Reference:** `docs/handbook/repo-workflow.md`

## 2.5 Report Organization

**Topics to cover:**
- Brief overview of report structure
- How to navigate the documentation
- Key sections for different audiences

---

# 3. System Architecture

**Responsible: João Roldão**

*[15-20 pages with diagrams and references]*

## 3.1 Architectural Overview

**Topics to cover:**
- High-level system architecture
- Modular monolith rationale vs microservices
- BFF (Backend-for-Frontend) pattern
- Clear separation of concerns
- Scalability considerations

**References:**
- `docs/adr/0001-initial-system-architecture.md`
- `docs/handbook/dev-handbook.md` (Architecture Overview section)

**Diagrams to include:**
- `docs/per-sprint/sprint2/architecture-update-security-&-auth/logic-architecture.png`
- `docs/per-sprint/sprint2/architecture-update-security-&-auth/instalation-architecture.png`

## 3.2 Logic Architecture

### 3.2.1 Backend Modules

**Topics to cover:**

**Order Module:**
- Order creation, registration, and lifecycle management
- Document generation (labels, packing slips)
- Warehouse association
- Order events and audit trail
- Status transitions

**Rate Carrier Module:**
- ShipEngine API integration
- Carrier management (FedEx, UPS, DHL)
- Shipment creation and assignment
- Rate comparison functionality
- Address validation

**Visibility Module:**
- Real-time tracking aggregation
- Delivery exception management
- Exception categorization and prioritization
- CSR assignment workflow

**Chatbot Module:**
- AI-powered assistance using Google Gemini
- Vector search with Pinecone (768 dimensions, cosine metric)
- Role-based content filtering
- Frontend-specific help
- Route-based contextual guidance
- Document ingestion and management

**User Management Module:**
- Keycloak integration for authentication
- OAuth2/OIDC implementation
- JWT token validation
- Role-based access control (5 roles)

**Route Module:**
- Delivery route optimization
- Driver assignment
- Route planning support

**Notification Module:**
- Email and SMS notifications
- Notification templates
- Event-driven alerts

**Reporting Module:**
- KPI calculations and dashboards
- Performance metrics
- Custom report generation

**References:**
- `docs/adr/0001-initial-system-architecture.md` (Logic Architecture section)
- `docs/handbook/dev-handbook.md`
- Source code: `src/backend/src/main/java/Operimus/SLMS/business_layer/`

### 3.2.2 Frontend Applications

**Topics to cover:**

**Landing Page:**
- Public-facing marketing and information
- Entry point to SLMS ecosystem

**Admin Console:**
- Logistics manager dashboards and KPIs
- Warehouse staff order processing
- Customer service representative tools
- Unified internal operations interface

**Customer Portal:**
- Order tracking and status lookup
- Delivery preferences management
- Issue reporting
- Self-service capabilities

**Driver PWA:**
- Delivery manifest view
- Route navigation
- Proof of delivery capture (photo + signature)
- Real-time status updates
- Offline-capable Progressive Web App

**Reference:** `docs/adr/0001-initial-system-architecture.md` (Frontend Applications section)

### 3.2.3 BFF Pattern Implementation

**Topics to cover:**
- Backend-for-Frontend rationale
- API aggregation and composition
- Security boundary enforcement
- Client-specific optimizations

## 3.3 Installation/Deployment Architecture

**Topics to cover:**

**Infrastructure Components:**
- PostgreSQL 16 (primary database)
- Keycloak (authentication & authorization)
- MinIO (object storage for documents, POD images)
- Nginx (reverse proxy and load balancing)
- Docker + Docker Compose (containerization)
- Terraform (Infrastructure as Code)

**External Integrations:**
- ShipEngine API (carrier integration)
- Google Gemini API (LLM for chatbot)
- Pinecone (vector database for semantic search)
- Elastic APM (observability and monitoring)

**Network Architecture:**
- Docker network: slms-network
- Service discovery and communication
- Port mappings and routing

**References:**
- `infra/README.md`
- `infra/docker-compose.yaml`
- `infra/terraform/` directory
- `docs/adr/0003-architecture-update-db-integration.md`

**Diagrams to include:**
- Installation architecture diagram
- Network topology

## 3.4 Component Interactions

**Topics to cover:**

**Critical Flows:**

1. **Order Creation Flow:**
   - Customer/Warehouse → Admin Console
   - Order Service → Database
   - Document generation
   - Warehouse assignment

2. **Shipment Assignment Flow:**
   - Rate comparison via ShipEngine
   - Carrier selection
   - Shipment creation
   - Driver assignment

3. **Authentication Flow:**
   - Frontend → Keycloak
   - JWT token issuance
   - Token validation in backend
   - Role-based authorization

4. **Delivery Exception Flow:**
   - Exception reporting (driver/customer)
   - Exception categorization
   - CSR assignment
   - Resolution tracking

5. **AI Chatbot Query Flow:**
   - User query → Backend
   - Embedding generation
   - Pinecone semantic search
   - Context retrieval
   - Gemini response generation
   - Role/route filtering

**References:**
- `docs/adr/0004-architecture-update-security-&-auth.md`
- API endpoint listings in `docs/per-sprint/sprint2/endpoint-listing/`

## 3.5 Architecture Decision Records (Summary)

**Topics to cover:**

**ADR-0001: Initial System Architecture**
- Decision: Modular monolith
- Rationale: Team size, complexity, deployment simplicity
- Consequences: Single deployable, clear module boundaries

**ADR-0002: Data Model Definition**
- Initial database schema design
- Entity relationships
- Primary key strategy

**ADR-0003: Database Integration**
- PostgreSQL 16 selection
- JPA/Hibernate configuration
- Connection pooling

**ADR-0004: Security & Authentication**
- Keycloak adoption
- OAuth2/OIDC implementation
- JWT token-based auth

**ADR-0005: Observability Monitoring Strategy**
- Elastic APM selection
- SLI/SLO definitions
- Logging, metrics, traces approach

**Reference:** Full ADRs in `docs/adr/` directory

## 3.6 Technical Stack

*[3-4 pages with technology justifications]*

### 3.6.1 Backend

**Topics to cover:**

**Spring Boot 3.5.6 + Java 21:**
- Modern Java features (records, pattern matching, virtual threads)
- Spring Boot autoconfiguration
- Dependency injection
- Production-ready features (actuator)

**Spring Data JPA:**
- ORM with Hibernate
- Repository abstraction
- Query methods
- Transaction management

**Spring Security:**
- OAuth2 resource server
- JWT token validation
- Method-level security
- CORS configuration

**PostgreSQL 16:**
- Relational database
- ACID compliance
- JSONB support
- Performance and scalability

**Maven:**
- Dependency management
- Build lifecycle
- Multi-module support
- Plugin ecosystem

**Reference:** `pom.xml` in `src/backend/`

### 3.6.2 Frontend

**Topics to cover:**

**Next.js 15:**
- Server-side rendering (SSR)
- Static site generation (SSG)
- API routes
- File-based routing
- Image optimization

**React 19:**
- Component-based architecture
- Hooks
- State management
- Virtual DOM

**TypeScript:**
- Type safety
- Better IDE support
- Refactoring confidence
- Documentation via types

**Tailwind CSS:**
- Utility-first CSS
- Rapid UI development
- Consistent design system
- Small bundle size

**NextAuth.js:**
- Keycloak integration
- Session management
- Client-side auth
- Protected routes

**UI Component Library:**
- shadcn/ui or similar
- Accessible components
- Customizable theming

**Reference:** `package.json` files in `src/frontend/*/`

### 3.6.3 Infrastructure

**Topics to cover:**

**Docker:**
- Container runtime
- Isolation and consistency
- Multi-stage builds
- Layer caching

**Docker Compose:**
- Multi-container orchestration
- Development environment
- Service dependencies
- Volume management

**Terraform:**
- Infrastructure as Code
- Declarative configuration
- State management
- Provider ecosystem (Docker provider)

**Nginx:**
- Reverse proxy
- Load balancing
- SSL termination
- Static file serving
- Path-based routing

**GitHub Actions:**
- CI/CD automation
- Workflow definitions
- Matrix builds
- Artifact management

**Reference:** `infra/` directory

### 3.6.4 External Services

**Topics to cover:**

**Keycloak:**
- Identity and access management
- OAuth2/OIDC provider
- User federation
- Admin console

**MinIO:**
- S3-compatible object storage
- Document storage
- Image storage (POD)
- On-premise alternative to AWS S3

**ShipEngine:**
- Multi-carrier API platform
- Rate shopping
- Label generation
- Tracking integration
- Support for FedEx, UPS, DHL

**Google Gemini:**
- Large Language Model
- Natural language understanding
- Response generation
- Multilingual support

**Pinecone:**
- Vector database
- Semantic search
- High-dimensional vectors
- Fast similarity search
- Managed service

**Elastic APM:**
- Application Performance Monitoring
- Distributed tracing
- Metrics collection
- Log correlation
- Kibana visualization

## 3.7 Critical Data Flows

**Topics to cover:**

**Order Lifecycle:**
- PENDING → PROCESSING → READY_FOR_PICKUP → PICKED_UP → IN_TRANSIT → DELIVERED
- Event tracking at each transition
- Notification triggers

**Shipment Lifecycle:**
- Rate request → carrier selection → shipment creation → tracking

**Exception Resolution:**
- Detection → categorization → assignment → investigation → resolution

**AI Chatbot Context Assembly:**
- User context (role, frontend, route)
- Document filtering
- Semantic search
- Response generation

---

# 4. Data Model

**Responsible: João Roldão**

*[8-10 pages with schema diagrams]*

## 4.1 Database Schema Overview

**Topics to cover:**
- PostgreSQL 16 as primary database
- Relational schema design principles
- UUID vs Long primary keys (mixed strategy)
- JSONB usage for flexible data (addresses, metadata)
- Cross-module reference patterns
- Referential integrity and foreign keys

**References:**
- `docs/handbook/data-model.md`
- `docs/per-sprint/sprint2/data-model-definition/data-model-definition.md`
- `docs/adr/0002-data-model-definition.md`
- `docs/adr/0008-data-model-implementation-strategy.md`

## 4.2 Core Tables by Module

### 4.2.1 Order Module (4 tables)

**ORDERS:**
- Primary order tracking table
- Fields: id, tracking_number, status, customer info, addresses, dimensions, weight
- Relationships: warehouse_id, carrier_id
- Status enum transitions

**ORDER_EVENTS:**
- Audit trail for order state changes
- Event timestamp, type, user
- Complete history tracking

**WAREHOUSES:**
- Physical warehouse locations
- Multi-warehouse support
- JSONB address storage

**OrderStatus Enum:**
- PENDING, PROCESSING, READY_FOR_PICKUP, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED, EXCEPTION

**Reference:** `docs/per-sprint/sprint2/data-model-definition/data-model-definition.md` (Orders section)

### 4.2.2 Rate Carrier Module (2 tables)

**CARRIERS:**
- Third-party shipping providers
- API configuration (endpoint, credentials)
- Capabilities JSONB
- Active/inactive status

**SHIPMENTS:**
- Carrier-assigned shipment records
- Links orders to carriers
- Tracking information
- Rate details

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/rate_carrier_module/entities/`

### 4.2.3 Visibility Module (1 table)

**DELIVERY_EXCEPTIONS:**
- Exception tracking and management
- Type, severity, priority classification
- Reporter and assignee (CSR)
- Resolution status and notes
- SLA tracking

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/visibility_module/exceptions/entities/`

### 4.2.4 Chatbot Module (4+ tables)

**ASSISTANT_DOCUMENTS:**
- Help content for AI chatbot
- Document chunks for vector search
- Metadata (role, frontend, route filters)

**Collection tables:**
- Role-based filtering collections
- Frontend-specific collections
- Route-specific collections

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/chatbot_module/entities/`

### 4.2.5 User Management

**No users table - delegated to Keycloak**
- External authentication provider
- User profiles, roles, credentials managed externally
- JWT tokens for session management

**Reference:** `docs/adr/0004-architecture-update-security-&-auth.md`

## 4.3 Entity Relationships

**Topics to cover:**

**Key Relationships:**
- Order → Warehouse (many-to-one)
- Order → Shipment (one-to-one or one-to-many)
- Shipment → Carrier (many-to-one)
- DeliveryException → Order (many-to-one)
- Order → OrderEvents (one-to-many)

**Cross-module patterns:**
- Avoiding circular dependencies
- Module boundaries in database
- Shared entities strategy

**Diagrams to include:**
- ER diagram (if available)
- Module interaction diagram

## 4.4 Key Design Decisions

**Topics to cover:**

**Order vs Shipment Separation:**
- Internal order concept vs carrier shipment
- Rationale for separation
- Flexibility benefits

**Keycloak for User Management:**
- No users table rationale
- External auth provider benefits
- JWT-based authorization

**JSONB for Flexible Data:**
- Address storage (from/to addresses)
- Metadata fields
- Carrier capabilities
- Performance considerations

**UUID Primary Keys:**
- Global uniqueness
- Distributed system benefits
- Migration from Long IDs (if applicable)

**References:**
- `docs/adr/0002-data-model-definition.md`
- `docs/adr/0006-shipment-entity-introduction.md`
- `docs/handbook/data-model.md`

---

# 5. User Requirements

**Responsible: Hugo Castro**

*[10-12 pages with persona images and scenarios]*

## 5.1 Personas

**Reference:** `docs/per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md`

### 5.1.1 João Ferreira - Logistics Manager

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/joaoFerreira.png`

**Topics to cover:**
- Age: 42
- Profile: Senior manager with operations experience
- Goals: Global operations view, cost reduction, carrier performance evaluation
- Frustrations: Lack of real-time data, manual carrier comparison
- How he uses SLMS: KPI monitoring, strategy optimization, report exports

### 5.1.2 Ana Martins - Warehouse Staff

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/anaMartins.png`

**Topics to cover:**
- Age: 29
- Profile: Warehouse employee, shipping department
- Goals: Quick order processing, correct labels/docs, error reduction
- Frustrations: Manual processes, carrier-specific systems
- How she uses SLMS: Order registration, label generation, pickup updates

### 5.1.3 Rita Lopes - Customer Service Representative

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/ritaLopes.png`

**Topics to cover:**
- Age: 34
- Profile: Call center staff
- Goals: Quick status access, issue resolution, improved CX
- Frustrations: No real-time info, manual carrier contact
- How she uses SLMS: Status checks, exception management, customer feedback

### 5.1.4 Carlos Silva - Driver/Carrier

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/carlosSilva.png`

**Topics to cover:**
- Age: 38
- Profile: Delivery driver (urban/intercity routes)
- Goals: Clear instructions, efficient delivery, easy POD
- Frustrations: Poor route planning, difficult status updates
- How he uses SLMS: Mobile manifest, optimized routes, POD capture

### 5.1.5 Mariana Costa - Customer

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/marianaCosta.png`

**Topics to cover:**
- Age: 26
- Profile: Frequent online shopper
- Goals: Real-time tracking, delivery transparency, issue reporting
- Frustrations: Lack of visibility, poor communication
- How she uses SLMS: Order tracking, notifications, problem reporting

## 5.2 Usage Scenarios

**Topics to cover:**
- 18+ detailed scenarios across all personas
- Realistic workflow examples
- Step-by-step interactions
- Expected outcomes

**Scenarios to highlight:**
1. João: Weekly KPI review and carrier performance analysis
2. Ana: Processing a new order from intake to carrier pickup
3. Rita: Resolving a delivery exception reported by customer
4. Carlos: Completing delivery route with POD capture
5. Mariana: Tracking order and reporting a delivery issue

**Reference:** `docs/per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md` (Scenarios section)

## 5.3 User Stories

**Topics to cover:**

**Complete list of 18 user stories with acceptance criteria:**

**Logistics Manager (3 stories):**
- US1: KPI monitoring dashboard
- US2: Carrier performance comparison
- US3: Custom report generation

**Warehouse Staff (3 stories):**
- US4: Order registration and processing
- US5: Automated label and document generation
- US6: Carrier pickup confirmation

**Customer Service Representative (3 stories):**
- US7: Order status lookup
- US8: Delivery exception resolution
- US9: Customer notification management

**Driver (4 stories):**
- US10: View delivery manifest
- US11: Route optimization
- US12: Proof of delivery capture
- US13: Exception reporting

**Customer (4 stories):**
- US14: Account creation
- US15: Secure login
- US16: Real-time order tracking (removed/modified)
- US17: Delivery notifications
- US18: Issue reporting

**All Users (1 story):**
- US19: AI Assistant for contextual help

**Reference:** `docs/per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md` (User Stories section)

## 5.4 Feature Prioritization

**Topics to cover:**
- Completed: 12 stories (67%)
- In Progress: 5 stories (28%)
- Pending: 1 story (5%)
- Sprint-by-sprint feature delivery
- Rationale for prioritization

**Sprint breakdown:**
- Sprint 1: Foundation (architecture, data model, basic order management)
- Sprint 2: Core features (order processing, carrier integration, personas)
- Sprint 3: Carrier module, rate comparison
- Sprint 4: Observability, AWS deployment, shipment documents
- Sprint 5+: Advanced features (chatbot, reporting, etc.)

**References:**
- `docs/per-sprint/sprint1/epic-&-sprint-planning.md`
- Sprint documentation in `docs/per-sprint/`

## 5.5 Use Case Diagrams

**Diagrams to include:**
- `docs/per-sprint/sprint2/use-case-diagrams/slms.png` (overall system)
- `docs/per-sprint/sprint2/use-case-diagrams/logistics-manager.png`
- `docs/per-sprint/sprint2/use-case-diagrams/warehouse-staff.png`
- `docs/per-sprint/sprint2/use-case-diagrams/customer-service-representative.png`
- `docs/per-sprint/sprint2/use-case-diagrams/driver-carrier.png`
- `docs/per-sprint/sprint2/use-case-diagrams/customer.png`

---

# 6. Developer Onboarding

**Responsible: Hugo Castro**

*[8-10 pages with step-by-step instructions]*

**Reference:** `docs/handbook/dev-handbook.md`

## 6.1 Prerequisites

**Topics to cover:**
- Java 21 (OpenJDK or Oracle)
- Node.js 20+ and npm
- Docker Engine 24+ and Docker Compose
- Terraform 1.5+
- Git
- Make (for build automation)
- IDE recommendations (IntelliJ IDEA, VS Code)

**System requirements:**
- 8GB RAM minimum (16GB recommended)
- 20GB free disk space
- Linux/macOS/Windows with WSL2

## 6.2 Initial Environment Setup

**Topics to cover:**

**Step 1: Clone Repository**
```bash
git clone https://github.com/detiuaveiro/group-project-es2526_304.git
cd group-project-es2526_304
```

**Step 2: Configure Backend Environment Variables**
```bash
cp src/backend/.env.example src/backend/.env
# Edit .env file with:
# - Database credentials
# - Keycloak configuration
# - MinIO settings
# - Gemini API key
# - Pinecone credentials
```

**Step 3: Configure Infrastructure Variables**
```bash
cp infra/.env.example infra/.env
# Edit with PostgreSQL, Keycloak, MinIO settings
```

**Step 4: Create Docker Network**
```bash
docker network create slms-network
```

**Step 5: Start Development Environment**
```bash
make dev-up
# OR for full Terraform deployment:
make up
```

**Step 6: Verify Installation**
```bash
make status
```

**Reference:** `docs/handbook/dev-handbook.md` (Getting Started section)

## 6.3 Project Structure Walkthrough

**Topics to cover:**

**Root directory:**
- `/src/backend` - Spring Boot application
- `/src/frontend` - Next.js applications
- `/infra` - Infrastructure as Code (Terraform, Docker)
- `/docs` - Documentation
- `Makefile` - Build automation

**Backend structure:**
- `/business_layer` - Domain modules (8 modules)
- `/presentation_layer` - BFF controllers
- `/data_layer` - JPA repositories (if separate)

**Frontend structure:**
- `/admin-console` - Internal operations UI
- `/customer-portal` - Public customer interface
- `/driver-pwa` - Mobile driver app
- `/landing-page` - Marketing/info site
- `/shared` - Shared components and utilities

**Infrastructure:**
- `/terraform` - IaC definitions
- `/docker` - Dockerfiles
- `/nginx` - Reverse proxy config
- `/keycloak` - Auth configuration
- `/scripts` - Deployment scripts

**Documentation hierarchy:**
- `/docs/adr` - Architecture Decision Records
- `/docs/handbook` - Living documentation
- `/docs/per-sprint` - Sprint artifacts
- `/docs/architecture` - Diagrams and models

## 6.4 Common Development Commands

**Topics to cover:**

**From Makefile:**

```bash
# Environment management
make up              # Start production environment (Terraform)
make dev-up          # Start dev environment (Docker Compose)
make down            # Stop services
make restart         # Restart services
make status          # Check health
make logs            # View logs

# Build and install
make install         # Install frontend dependencies
make build           # Build all components

# Testing
make test            # Run all tests
make test-unit       # Unit tests only
make test-integration # Integration tests
make test-e2e        # End-to-end tests
make test-real       # Tests with real services

# Cleanup
make clean           # Remove stopped containers
make clean-volumes   # Remove all data
```

**Backend commands:**
```bash
cd src/backend
./mvnw spring-boot:run      # Run locally
./mvnw test                 # Run tests
./mvnw clean package        # Build JAR
```

**Frontend commands:**
```bash
cd src/frontend/admin-console  # or other app
npm install                    # Install dependencies
npm run dev                    # Development server
npm run build                  # Production build
npm run test                   # Run tests
```

**Reference:** `Makefile` in repository root

## 6.5 Development Workflow

**Topics to cover:**

**Branching Strategy:**
- `main` - Production-ready code
- `develop` - Integration branch
- Feature branches: `feat/feature-name`
- Bug fixes: `fix/bug-name`
- Documentation: `docs/doc-name`

**Commit Message Conventions:**
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation
- `refactor:` - Code refactoring
- `test:` - Test additions

**Pull Request Process:**
1. Create feature branch from `develop`
2. Implement changes with tests
3. Create PR to `develop`
4. Code review (at least 1 approval)
5. CI checks must pass
6. Merge to `develop`
7. Periodic merges to `main`

**CI/CD Pipeline:**
- Automated builds on every push
- Test execution
- Code quality checks
- Deployment on merge to `main`

**Reference:** `docs/handbook/repo-workflow.md`

## 6.6 Testing Strategy

**Topics to cover:**

**Test Pyramid Approach:**

**Unit Tests (Backend):**
- JUnit 5 + Mockito
- Service layer testing
- Controller testing (MockMvc)
- Repository testing

**Integration Tests (Backend):**
- Mocked external APIs (ShipEngine, Gemini, Pinecone)
- Database integration tests
- Module integration testing

**E2E Tests (Frontend):**
- Playwright or Cypress
- User workflow testing
- Cross-browser testing

**Real Integration Tests:**
- Actual external service integration
- Requires API keys in environment
- Run with `make test-real`

**Coverage Requirements:**
- Target: 70% code coverage
- JaCoCo reports
- Coverage gates in CI

**Test Commands:**
```bash
# Backend
make test-unit            # Fast unit tests
make test-integration     # Mocked integration
make test-real            # Real services

# Frontend
cd src/frontend/admin-console
npm run test              # Unit tests
npm run test:e2e          # E2E tests
```

**Reference:** `docs/handbook/dev-handbook.md`, `Makefile`

## 6.7 Troubleshooting Guide

**Topics to cover:**

**Common Issues:**

**Port Conflicts:**
- Issue: Port 5432 already in use
- Solution: Configure `postgres_host_port` in Terraform variables

**Docker Permission Issues:**
- Issue: Permission denied errors
- Solution: Add user to docker group or use sudo

**Database Connection Errors:**
- Check PostgreSQL container is running
- Verify credentials in .env
- Check network connectivity

**Keycloak Configuration:**
- Ensure realm 'slms' is created
- Verify client configurations
- Check redirect URIs

**API Key Setup:**
- Gemini: Obtain from Google AI Studio
- Pinecone: Create account, get API key
- ShipEngine: Register and get test API key

**Frontend Build Failures:**
- Clear node_modules and reinstall
- Check Node.js version (20+)
- Verify environment variables

**Backend Build Failures:**
- Check Java version (21)
- Clean Maven cache: `./mvnw clean`
- Verify database connection

---

# 7. Production Configuration

**Responsible: João Varela**

*[10-12 pages with configuration examples]*

## 7.1 Environment Variables

**Reference:** `src/backend/.env.example`, `infra/.env.example`

### 7.1.1 Backend Configuration

**Database Configuration:**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/slms_db
SPRING_DATASOURCE_USERNAME=slms_admin
SPRING_DATASOURCE_PASSWORD=<secure_password>
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
```

**Keycloak Integration:**
```properties
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:8180/realms/slms
KEYCLOAK_AUTH_SERVER_URL=http://localhost:8180
KEYCLOAK_REALM=slms
KEYCLOAK_RESOURCE=slms-backend
KEYCLOAK_CREDENTIALS_SECRET=<client_secret>
```

**MinIO Configuration:**
```properties
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=<access_key>
MINIO_SECRET_KEY=<secret_key>
MINIO_BUCKET_NAME=slms-documents
```

**AI Module Configuration:**
```properties
GEMINI_API_KEY=<google_gemini_api_key>
PINECONE_API_KEY=<pinecone_api_key>
PINECONE_ENVIRONMENT=<pinecone_environment>
PINECONE_INDEX_NAME=slms-assistant
```

**Observability:**
```properties
ELASTIC_APM_SERVER_URL=<elastic_apm_url>
ELASTIC_APM_SECRET_TOKEN=<apm_token>
ELASTIC_APM_SERVICE_NAME=slms-backend
ELASTIC_APM_ENVIRONMENT=production
```

### 7.1.2 Infrastructure Configuration

**PostgreSQL:**
```properties
POSTGRES_DB=slms_db
POSTGRES_USER=slms_admin
POSTGRES_PASSWORD=<secure_password>
```

**Keycloak:**
```properties
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=<admin_password>
KC_HOSTNAME=localhost
KC_HTTP_PORT=8180
```

**MinIO:**
```properties
MINIO_ROOT_USER=<root_user>
MINIO_ROOT_PASSWORD=<root_password>
```

**Backend:**
```properties
BACKEND_PORT=8080
NODE_ENV=production
```

## 7.2 External Services Configuration

### 7.2.1 Keycloak Setup

**Topics to cover:**

**Realm Creation:**
- Realm name: `slms`
- Realm settings configuration

**Client Configurations (4 clients):**
1. `slms-backend` - Backend service
2. `slms-admin-console` - Admin UI
3. `slms-customer-portal` - Customer UI
4. `slms-driver-pwa` - Driver mobile app

**For each client:**
- Client ID
- Client secret
- Valid redirect URIs
- Web origins
- Access type (confidential/public)

**Role Mappings:**
- MANAGER
- WAREHOUSE
- CSR
- DRIVER
- CUSTOMER

**JWT Token Settings:**
- Token lifespan
- Refresh token settings
- Signature algorithm (RS256)

**Admin Client Credentials:**
- For programmatic user management
- Service account roles

**Reference:** `docs/adr/0004-architecture-update-security-&-auth.md`

### 7.2.2 MinIO Setup

**Topics to cover:**

**Bucket Creation:**
- Bucket name: `slms-documents`
- Versioning enabled/disabled
- Encryption settings

**Access Policies:**
- Read/write permissions
- Public vs private buckets
- CORS configuration

**Endpoint Configuration:**
- Internal endpoint for backend
- External endpoint (if needed)
- SSL/TLS configuration

### 7.2.3 ShipEngine Integration

**Topics to cover:**

**API Key Acquisition:**
- Register at ShipEngine
- Obtain sandbox/production API key
- Rate limits and quotas

**Carrier Configuration:**
- Connect carrier accounts (FedEx, UPS, DHL)
- Configure carrier services
- Test credentials

**Rate Request Setup:**
- Rate comparison configuration
- Service level definitions
- Package type configurations

**Webhook Configuration:**
- Webhook endpoint URL
- Event subscriptions
- Signature verification

**Reference:** `docs/per-sprint/sprint1/carrier-shipping-apis-investigation.md`, `docs/per-sprint/sprint3/carrier-module-readme.md`

### 7.2.4 AI Services Configuration

**Google Gemini:**
- Obtain API key from Google AI Studio
- Model selection (gemini-pro)
- Rate limits and quotas
- Safety settings

**Pinecone:**
- Create account and project
- Create index: `slms-assistant`
  - Dimensions: 768 (matches embedding model)
  - Metric: cosine
  - Pod type selection
- Obtain API key and environment
- Document ingestion process

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/chatbot_module/`

## 7.3 Build Process

### 7.3.1 Backend Build

**Topics to cover:**

```bash
cd src/backend
./mvnw clean package -DskipTests
```

**Produces:** `target/slms-backend-0.0.1-SNAPSHOT.jar`

**Build profiles:**
- Development profile
- Production profile
- Test profile

**Maven configuration highlights:**
- Spring Boot Maven plugin
- Dependency management
- Resource filtering

### 7.3.2 Frontend Builds

**Topics to cover:**

**For each application:**
```bash
cd src/frontend/{admin-console|customer-portal|driver-pwa|landing-page}
npm install
npm run build
```

**Produces:** `.next/` directory with optimized static files

**Build-time variables:**
- `NEXT_PUBLIC_BACKEND_URL`
- `NEXT_PUBLIC_KEYCLOAK_URL`
- Environment-specific configs

**Optimization:**
- Static page generation
- Image optimization
- Code splitting
- Bundle size optimization

## 7.4 Deployment Pipeline (CI/CD)

**Reference:** `.github/workflows/build.yml`, `.github/workflows/deploy.yml`

### 7.4.1 Continuous Integration (build.yml)

**Topics to cover:**

**Triggers:**
- Every push to any branch
- Every pull request

**Build Steps:**
1. Checkout code
2. Setup Java 21 and Node.js 20
3. Backend: `mvn test`
4. Frontend: `npm test` for each app
5. Integration tests with mocked services
6. Code quality checks (JaCoCo coverage)

**Quality Gates:**
- All tests must pass
- Code coverage threshold
- No critical security vulnerabilities

### 7.4.2 Continuous Deployment (deploy.yml)

**Topics to cover:**

**Triggers:**
- Push to `main` branch
- Manual workflow dispatch

**Deployment Steps:**
1. Database backup (pre-deployment)
2. Docker image builds
   - Backend image
   - Frontend images (4 apps)
3. Terraform plan and apply
4. Service deployment
5. Health checks
6. Database backup (post-deployment)

**Rollback Strategy:**
- Previous database backup restoration
- Container version rollback
- Terraform state management

**Reference:** `.github/workflows/` directory

## 7.5 Infrastructure as Code (Terraform)

**Reference:** `infra/terraform/` directory, `infra/README.md`

### 7.5.1 Resources Managed

**Topics to cover:**

**Docker Resources:**
- Network creation (`slms-network`)
- Image builds with build arguments
- Container orchestration
- Volume management
- Service dependencies

**Configuration:**
- Environment variable injection
- Port mappings
- Health checks
- Restart policies

### 7.5.2 Terraform Variables

**Topics to cover:**

**Key Variables:**
- `public_host` - Deployment hostname/IP
- `docker_network_name` - Network isolation
- Build-time environment variables for Next.js apps
- Database credentials
- Service ports

**Variable files:**
- `terraform.tfvars.example` - Template
- `terraform.tfvars` - Actual values (gitignored)

**Sensitive data handling:**
- Using Terraform variables
- Secret management best practices

### 7.5.3 Terraform Commands

```bash
cd infra/terraform
terraform init
terraform plan
terraform apply
terraform destroy
```

## 7.6 AWS Deployment

**Reference:** `docs/per-sprint/sprint4/aws-deployment-guide.md`

### 7.6.1 EC2 Setup

**Topics to cover:**

**Instance Requirements:**
- Instance type (e.g., t3.medium or larger)
- CPU: 2+ vCPUs
- RAM: 4GB minimum (8GB recommended)
- Storage: 30GB+ EBS volume

**Security Group Configuration:**
- Inbound rules:
  - HTTP (80)
  - HTTPS (443)
  - SSH (22) - restricted IPs
  - Custom ports for development
- Outbound rules: Allow all

**Public IP / Elastic IP:**
- Allocate Elastic IP for stability
- Associate with EC2 instance
- DNS configuration

### 7.6.2 Deployment Steps

**Topics to cover:**

```bash
# On EC2 instance
git clone <repository>
cd group-project-es2526_304

# Configure environment
cd infra
./setup-aws.sh      # Auto-detect public IP

# Deploy
./deploy.sh deploy
```

**Script functionality:**
- Automatic IP detection
- Environment variable setup
- Terraform execution
- Service startup

### 7.6.3 Post-Deployment Verification

**Topics to cover:**

**Health Checks:**
```bash
make status
curl http://<public-ip>/health
```

**Service Availability:**
- Landing page: `http://<public-ip>/`
- Admin console: `http://<public-ip>/admin/`
- Customer portal: `http://<public-ip>/customer/`
- Driver PWA: `http://<public-ip>/driver/`

**Log Monitoring:**
```bash
make logs
docker compose -f infra/docker-compose.yaml logs -f
```

## 7.7 Database Management

### 7.7.1 Backup System

**Topics to cover:**

**Automated Backups:**
- Pre-deployment backup
- Post-deployment backup
- Retention policy: Last 4 backups
- Backup location and naming

**Manual Backup:**
```bash
cd infra
./scripts/backup-db.sh  # If exists
```

**Backup verification:**
- Test restoration process
- Backup integrity checks

### 7.7.2 Migration Strategy

**Topics to cover:**

**JPA DDL Auto Configuration:**
- `spring.jpa.hibernate.ddl-auto=update` (development)
- `spring.jpa.hibernate.ddl-auto=validate` (production)

**Schema Versioning:**
- Liquibase or Flyway (if implemented)
- Migration scripts location
- Version control of schema changes

**Data Migration:**
- Handling data transformations
- Migration scripts for major changes

## 7.8 Secrets Management

**Topics to cover:**

**Environment Variables:**
- Never commit secrets to Git
- `.env` files in `.gitignore`
- `.env.example` as template

**API Key Rotation:**
- Regular rotation procedures
- Rotation for ShipEngine, Gemini, Pinecone
- Update without downtime

**Keycloak Client Secrets:**
- Secret generation
- Secure storage
- Rotation policy

## 7.9 Scaling Considerations

**Topics to cover:**

**Horizontal Scaling:**
- Load balancer configuration (Nginx)
- Stateless application design
- Session management across instances

**Database Connection Pooling:**
- HikariCP configuration
- Pool size tuning
- Connection leak detection

**Caching Strategies:**
- Application-level caching
- Redis integration (future)
- Cache invalidation

**Load Balancer Configuration:**
- Nginx as reverse proxy
- Round-robin or least connections
- Health check integration
- SSL termination

**Future Microservices Migration:**
- Module boundaries as microservice candidates
- Event-driven architecture considerations
- Service mesh potential

---

# 8. Security & Authentication

**Reference:** `docs/adr/0004-architecture-update-security-&-auth.md`

Security is a critical cornerstone of the Smart Logistics Management System (SLMS), given its role in managing sensitive logistics operations across multiple user types and frontend applications. The system requires robust authentication and authorization mechanisms to ensure that users can only access resources and perform actions appropriate to their roles. This section details the comprehensive security architecture implemented in SLMS, with particular focus on the centralized authentication system powered by Keycloak and the Backend-For-Frontend (BFF) security pattern.

The SLMS security architecture addresses several key challenges inherent in modern multi-tenant logistics platforms:

1. **Multi-Role Access Control**: The system serves five distinct user personas—Logistics Managers, Warehouse Staff, Customer Service Representatives, Drivers, and Customers—each requiring different levels of access and different sets of capabilities.

2. **Multiple Frontend Applications**: With four separate Next.js applications (Landing Page, Admin Console, Customer Portal, and Driver PWA), the system needs unified authentication that works seamlessly across all interfaces while maintaining strict access boundaries.

3. **Centralized Identity Management**: Rather than implementing custom authentication in each frontend or module, SLMS leverages a dedicated Identity and Access Management (IAM) service to provide enterprise-grade security features including Single Sign-On (SSO), comprehensive audit trails, and standardized token-based authentication.

4. **Security at the Edge**: By implementing the BFF pattern, SLMS enforces security at the presentation layer, ensuring that unauthorized requests never reach the business logic layer.

The decision to adopt Keycloak as the authentication provider and implement the BFF pattern represents a deliberate architectural choice documented in ADR-0004. This approach provides a future-proof foundation that supports the system's potential evolution toward a microservices architecture while maintaining strong security guarantees today.

## 8.1 Authentication Architecture

### 8.1.1 Keycloak as Centralized Identity Provider

SLMS employs Keycloak, an open-source Identity and Access Management solution, as its centralized authentication authority. Keycloak serves as the single source of truth for user credentials, roles, and authentication policies, implementing industry-standard OAuth 2.0 and OpenID Connect (OIDC) protocols.

**Architectural Position and Isolation**

A critical architectural principle in SLMS is that Keycloak operates as an external service that **never communicates directly with frontend applications**. All authentication requests are mediated through the Spring Boot backend, specifically through the Authentication BFF endpoints exposed by the Landing Page BFF controller (`/api/landing-page/auth/**`). This design provides several security advantages:

- **Single Security Boundary**: By routing all authentication through the backend, the system maintains a single, well-defined security perimeter that is easier to monitor, audit, and protect.
- **Credential Protection**: User credentials never traverse the frontend JavaScript environment, reducing the attack surface for credential theft.
- **Token Management Control**: The backend has full control over token lifecycle, including secure storage of refresh tokens in HTTP-only cookies that cannot be accessed by client-side JavaScript.
- **Simplified Frontend Security**: Frontend applications only need to manage access tokens, without requiring knowledge of Keycloak endpoints, realm configuration, or OAuth flows.

**Keycloak Deployment and Configuration**

Keycloak runs as a containerized service within the SLMS Docker Compose environment, exposed on port 8180 in development (`http://localhost:8180`). The system uses a dedicated SLMS realm (`slms`) that encapsulates all user accounts, roles, and client configurations specific to the logistics platform.

Key configuration aspects include:

- **Realm-level Roles**: Five application-specific roles defined at the realm level (detailed in section 8.1.2)
- **Client Configuration**: A confidential client configured for the Spring Boot backend with client credentials grant support
- **Token Lifespans**: Access tokens configured for 5-minute expiry (300 seconds) to limit the window of opportunity for token theft, while refresh tokens remain valid for extended periods to support seamless user sessions
- **Security Policies**: Brute force protection enabled with progressive lockout after failed login attempts
- **Audit Capabilities**: Comprehensive event logging for all authentication, authorization, and administrative actions

**OAuth 2.0 and OpenID Connect Implementation**

SLMS utilizes the OAuth 2.0 Resource Owner Password Credentials grant type for user authentication. When a user submits credentials through the Landing Page login form:

1. The frontend sends credentials to the backend Authentication BFF (`POST /api/landing-page/auth/login`)
2. The backend's `AuthService` communicates with Keycloak's token endpoint using the confidential client credentials
3. Keycloak validates the credentials against its user database
4. Upon successful authentication, Keycloak issues an access token (JWT) and a refresh token
5. The backend extracts user information from the JWT payload and returns it to the frontend along with the access token
6. The refresh token is stored in an HTTP-only cookie for secure token renewal

This approach combines the convenience of password-based authentication for internal logistics users with the security benefits of OAuth 2.0 token-based authorization.

### 8.1.2 User Roles and Permissions

SLMS defines five distinct user roles, each corresponding to a key persona in the logistics ecosystem. These roles are configured as realm-level roles in Keycloak and embedded in JWT access tokens issued to authenticated users.

| **Role** | **User Type** | **Primary Responsibilities** | **Access Level** |
|----------|---------------|------------------------------|------------------|
| `LOGISTICS_MANAGER` | Logistics Manager | Oversee all logistics operations, view system-wide KPIs and reports, manage warehouse and staff configurations, monitor carrier performance | Full administrative access to logistics operations |
| `WAREHOUSE_STAFF` | Warehouse Staff | Register incoming orders, generate shipping labels and documents, manage inventory, confirm carrier pickups, process shipments | Warehouse-specific operations with order modification rights |
| `CUSTOMER_SERVICE_REP` | Customer Service Representative | Handle customer inquiries, manage delivery exceptions, resolve customer issues, communicate order status updates | Read-only access to orders with write access to exceptions and notifications |
| `DRIVER` | Delivery Driver | View assigned delivery routes, update delivery status, capture proof-of-delivery, report delivery exceptions | Limited to own assigned deliveries with status update capabilities |
| `CUSTOMER` | End Customer | Place new orders, track order status, view delivery history, report delivery issues | Limited to own orders and related information |

**Permission Boundaries**

Each role's permissions are enforced at multiple layers:

1. **Frontend Routing**: After authentication, users are redirected to the appropriate frontend application based on their primary role (Logistics Manager, Warehouse Staff, and CSR share the Admin Console; Drivers use the Driver PWA; Customers use the Customer Portal).

2. **API Endpoint Authorization**: Spring Security configuration enforces role requirements at the BFF endpoint level (detailed in section 8.2.2).

3. **Resource Ownership**: BFF controllers implement additional authorization logic to ensure users can only access resources they own or are authorized to view (e.g., customers cannot view other customers' orders).

**Role Assignment and Management**

User roles are assigned during account creation through Keycloak's Admin API, accessed by the Logistics Manager via the Admin Console's user management interface. The `KeycloakUserService` in the backend's User Management Module provides methods for creating users and assigning roles programmatically. Once assigned, roles are immutable during a session but can be modified by administrators, requiring the user to re-authenticate for changes to take effect.

### 8.1.3 Authentication Flow

The authentication flow in SLMS is designed to provide a seamless user experience while maintaining strict security controls. The Landing Page serves as the single entry point for all users, regardless of their role or intended destination.

**Initial Authentication**

The authentication sequence unfolds as follows:

1. **Landing Page Access**: Users navigate to the SLMS landing page (`http://localhost/`), which presents a login form for existing users and a registration option for new customers.

2. **Credential Submission**: When a user submits the login form with username and password, the Landing Page frontend sends a POST request to `/api/landing-page/auth/login` with the credentials in the request body.

3. **Backend-Keycloak Communication**: The Landing Page BFF's `AuthController` receives the request and delegates to `AuthService`, which:
   - Constructs a token request using OAuth 2.0 password grant
   - Sends the request to Keycloak's token endpoint with the system's confidential client credentials
   - Includes the user's username and password in the token request

4. **Token Issuance**: Keycloak validates the credentials and, if correct, returns:
   - An **access token** (JWT) containing user identity and roles, valid for 5 minutes
   - A **refresh token** for obtaining new access tokens without re-authentication, valid for 7 days

5. **Token Processing**: The backend `AuthService`:
   - Decodes the JWT access token to extract user claims (username, email, first name, last name, and roles from the `realm_access.roles` claim)
   - Returns a `LoginResponse` object containing the access token and user information to the frontend
   - Stores the refresh token in an HTTP-only cookie named `refresh_token`, making it inaccessible to JavaScript

6. **Frontend Token Storage**: The Landing Page frontend receives the response and:
   - Stores the access token in both localStorage and a regular cookie for flexible access patterns
   - Stores the user information (username, email, name, roles) in localStorage
   - Uses this information for subsequent authenticated requests and UI personalization

---

**[FIGURE 8.1: Authentication Flow Diagram]**

Create a sequence diagram showing the complete authentication flow with the following participants and steps:

**Participants** (left to right):
1. User (actor)
2. Landing Page Frontend
3. AuthController (`/api/landing-page/auth`)
4. AuthService (Backend)
5. Keycloak (IAM Service)

**Main Flow - Successful Login**:
1. User navigates to login page → Landing Page displays login form
2. User submits credentials (username, password)
3. Landing Page sends POST to `/api/landing-page/auth/login` with credentials
4. AuthController delegates to AuthService.login()
5. AuthService sends POST to Keycloak token endpoint: `/realms/slms/protocol/openid-connect/token` with:
   - grant_type=password
   - client_id, client_secret (backend credentials)
   - username, password (user credentials)
6. Keycloak validates credentials against user database
7. **If valid**: Keycloak returns 200 OK with:
   - access_token (JWT, 5-min expiry)
   - refresh_token (7-day expiry)
   - expires_in: 300
8. AuthService decodes JWT to extract: username, email, firstName, lastName, roles (from realm_access.roles)
9. AuthService returns LoginResponse to AuthController
10. AuthController stores refresh_token in HTTP-only cookie (7-day expiry, JavaScript-inaccessible)
11. AuthController returns 200 OK to Landing Page with: accessToken, user info, roles
12. Landing Page stores access token in localStorage + cookie, user info in localStorage
13. Landing Page examines user roles and determines target frontend
14. **If** LOGISTICS_MANAGER/WAREHOUSE_STAFF/CUSTOMER_SERVICE_REP → Redirect to Admin Console
15. **Else if** DRIVER → Redirect to Driver PWA
16. **Else if** CUSTOMER → Redirect to Customer Portal

**Alternative Flow - Invalid Credentials**:
- After step 6, if credentials invalid:
- Keycloak returns 401 Unauthorized
- AuthService throws exception
- AuthController returns 401 to Landing Page with error message
- Landing Page displays error to user

**Key Points to Emphasize**:
- **No Direct Frontend-Keycloak Connection**: All requests go through backend
- Refresh token in HTTP-only cookie (secure, XSS-protected)
- Access token in localStorage/cookie (short-lived, 5 minutes)
- Role-based routing happens client-side after authentication

---

**Role-Based Routing**

After successful authentication, the Landing Page examines the user's roles and redirects them to the appropriate frontend application:

- Users with `LOGISTICS_MANAGER`, `WAREHOUSE_STAFF`, or `CUSTOMER_SERVICE_REP` roles are redirected to the Admin Console
- Users with the `DRIVER` role are redirected to the Driver PWA
- Users with the `CUSTOMER` role are redirected to the Customer Portal

This role-based routing ensures users land directly in the interface designed for their workflow, minimizing confusion and improving efficiency.

**Authenticated API Requests**

Once authenticated, all subsequent API requests from frontend applications follow this pattern:

1. The frontend retrieves the access token from localStorage or the cookie
2. The token is included in the HTTP Authorization header as a Bearer token: `Authorization: Bearer <access_token>`
3. The Spring Boot backend's Security Filter Chain intercepts the request
4. The `JwtDecoder` validates the token's signature using Keycloak's public keys (fetched from the JWK Set endpoint)
5. The decoder verifies the token's expiry time and issuer claim
6. Upon successful validation, Spring Security extracts roles from the token's `realm_access.roles` claim and constructs a `SecurityContext`
7. Request processing continues with the authenticated user's context available to controllers and services

**Token Refresh Mechanism**

To provide a seamless user experience without requiring frequent re-authentication, SLMS implements automatic token refresh:

1. When an access token nears expiry or has expired, the frontend detects the 401 Unauthorized response
2. The frontend sends a request to `/api/landing-page/auth/refresh` without explicit parameters (the refresh token is automatically sent via the HTTP-only cookie)
3. The backend's `AuthController` extracts the refresh token from the cookie and forwards it to Keycloak's token endpoint using the refresh token grant
4. Keycloak validates the refresh token and issues a new access token and refresh token pair
5. The backend returns the new access token to the frontend and updates the refresh token cookie
6. The frontend retries the original failed request with the new access token

This mechanism allows users to maintain active sessions for up to 7 days without re-entering credentials, while access tokens remain short-lived to limit security exposure.

**Logout Flow**

User-initiated logout follows a clean teardown sequence:

1. The frontend sends a POST request to `/api/landing-page/auth/logout`
2. The backend extracts the refresh token from the HTTP-only cookie
3. The backend invokes Keycloak's logout endpoint to revoke the refresh token, invalidating all associated access tokens
4. The backend clears the refresh token cookie by setting its max age to 0
5. The frontend clears all stored authentication data (access token and user information from localStorage and cookies)
6. The user is redirected to the Landing Page login screen

This comprehensive logout ensures no residual session data remains that could be exploited in a shared computer environment.

## 8.2 Backend-For-Frontend (BFF) Security Pattern

The Backend-For-Frontend (BFF) pattern is a cornerstone of SLMS's security architecture. Rather than having frontend applications communicate directly with business layer services, each frontend has a dedicated BFF controller in the Spring Boot backend that serves as its exclusive API gateway.

### 8.2.1 BFF Architecture Overview

**Motivation and Benefits**

The BFF pattern was adopted to address several architectural requirements:

1. **Security Enforcement Point**: By placing BFF controllers between frontends and business logic, the system establishes a clear security boundary where authentication and authorization are enforced before any business logic executes.

2. **Frontend-Specific APIs**: Each BFF controller can tailor its API to the specific needs and workflows of its frontend, providing only the operations and data formats that frontend requires, reducing over-fetching and exposure of sensitive data.

3. **Simplified Frontend Logic**: Frontends don't need to understand complex business layer interactions, multi-service orchestration, or security token management details—they simply call their BFF endpoints with a bearer token.

4. **Audit and Monitoring**: All requests to business logic flow through BFF controllers, providing centralized logging, request tracking, and performance monitoring per frontend application.

5. **Evolution Flexibility**: BFF controllers can evolve independently to support frontend requirements without impacting other frontends or the underlying business layer, supporting parallel development by different teams.

**BFF Controller Structure**

SLMS implements the following BFF controllers, organized by frontend application:

- **Landing Page BFF** (`LandingPageChatBffController`, `AuthController`): Handles authentication operations and public chatbot access
- **Admin Console BFFs**:
  - `LogisticsBffController`: Services the Logistics Manager interface
  - `WarehouseBffController`: Services the Warehouse Staff interface
  - `CustomerServiceBffController`: Services the Customer Service Representative interface
- **Driver PWA BFF** (`DriverBffController`): Services the Driver mobile application
- **Customer Portal BFF** (`CustomerBffController`): Services the Customer web portal

Each BFF controller is annotated with `@RestController` and maps to a role-specific URL prefix (e.g., `/api/logistics/**`, `/api/customer/**`), making the role-based access control explicit in the URL structure.

### 8.2.2 Role-Based Endpoint Access Control

Spring Security enforces role-based access control at the BFF endpoint level through the `SecurityConfig` class, which defines a Security Filter Chain that intercepts all HTTP requests.

**Endpoint-to-Role Mapping**

The security configuration establishes the following access rules:

| **Endpoint Pattern** | **Required Role** | **Served Frontend** | **Typical Operations** |
|----------------------|-------------------|---------------------|------------------------|
| `/api/logistics/**` | `LOGISTICS_MANAGER` | Admin Console (Logistics view) | View KPIs, generate reports, manage users, configure warehouses and carriers |
| `/api/warehouse/**` | `WAREHOUSE_STAFF` | Admin Console (Warehouse view) | Register orders, generate labels, manage inventory, confirm pickups |
| `/api/customer-service/**` | `CUSTOMER_SERVICE_REP` | Admin Console (CSR view) | Search orders, manage delivery exceptions, send notifications |
| `/api/driver/**` | `DRIVER` | Driver PWA | View assigned deliveries, update delivery status, upload proof of delivery |
| `/api/customer/**` | `CUSTOMER` | Customer Portal | Place orders, track shipments, view order history, report issues |
| `/api/landing-page/auth/**` | Public | Landing Page | Login, register, logout, token refresh |
| `/api/public/**` | Public | Any | Public resources (health checks, documentation) |
| `/api/admin/**` | Public (Dev only) | Development tools | Direct access to domain controllers for testing |

**Security Configuration Implementation**

The `filterChain` method in `SecurityConfig` defines these rules declaratively:

- Public endpoints (authentication, health checks) are explicitly permitted without authentication using `permitAll()`
- Each protected BFF endpoint pattern is matched with a `hasRole()` requirement, specifying the single role authorized to access it
- Development endpoints (`/api/admin/**`) are configured with `permitAll()` to support testing and debugging, but these endpoints are removed or restricted in production builds
- A catch-all rule (`anyRequest().authenticated()`) ensures that any endpoint not explicitly configured requires authentication, preventing accidental exposure of unprotected resources

Spring Security's role checking automatically:
- Extracts the user's roles from the validated JWT token
- Compares them against the required role for the requested endpoint
- Returns 403 Forbidden if the user's roles don't match
- Allows the request to proceed if the user has the required role

**Cross-Role Access Scenarios**

Some users may have multiple roles assigned (e.g., a Logistics Manager who also acts as Warehouse Staff). In such cases, the user can access all endpoints corresponding to any of their assigned roles. The Admin Console frontend adapts its interface to show all features available to the user's combined role set, while the Customer Portal and Driver PWA are designed for single-role use.

---

**[FIGURE 8.2: BFF Access Control Architecture]**

Create an architecture diagram showing the complete BFF security pattern with the following components:

**Layout**: Layered architecture diagram with four main horizontal layers plus external services at bottom

**Layer 1 - Frontend Applications (Top)**:
- Landing Page (Public Entry Point)
- Admin Console (Logistics/Warehouse/CSR interfaces)
- Customer Portal
- Driver PWA
- Show all connecting to Nginx with Bearer tokens (except Landing Page which sends credentials)

**Layer 2 - Nginx Reverse Proxy**:
- Single Nginx component routing to backend
- Port 80/443

**Layer 3 - Spring Boot Backend - BFF Controllers (Presentation Layer)**:
- Group BFF endpoints with visual distinction:
  - `/api/landing-page/auth/**` - AuthController [🔓 PUBLIC]
  - `/api/logistics/**` - LogisticsBffController [🔒 LOGISTICS_MANAGER]
  - `/api/warehouse/**` - WarehouseBffController [🔒 WAREHOUSE_STAFF]
  - `/api/customer-service/**` - CustomerServiceBffController [🔒 CUSTOMER_SERVICE_REP]
  - `/api/driver/**` - DriverBffController [🔒 DRIVER]
  - `/api/customer/**` - CustomerBffController [🔒 CUSTOMER]
  - `/api/public/**` - Public Resources [🔓 PUBLIC]
  - `/api/admin/**` - Domain Controllers [⚠️ DEV ONLY]

**Security Filter Chain (Between Layer 3 & 4)**:
- JWT Decoder component (validates token signature, expiry, issuer)
- Spring Security RBAC component (checks role requirements)
- Show flow: Request → JWT Validation → Role Check → Allow/Deny (403 Forbidden)

**Layer 4 - Business Layer Services**:
- Order Module
- Rate & Carrier Module
- Reporting Module
- Visibility Module
- Notification Module
- User Management Module
- Show BFF controllers calling these services after authorization

**External Services (Bottom)**:
- Keycloak (only connected to AuthController, not frontends)

**Key Visual Elements**:
- Color code public endpoints (green), role-protected endpoints (red/protected), dev endpoints (orange/warning)
- Use arrows to show request flow: Frontend → Nginx → BFF → Security Filter → Business Layer
- Show 403 Forbidden rejection path from RBAC back to frontends
- Emphasize that Keycloak only connects to backend, never to frontends directly

---

## 8.3 Security Benefits and Trade-offs

The security architecture implemented in SLMS provides significant benefits while introducing certain trade-offs that were carefully considered during design.

**Benefits**

1. **Centralized Authentication**: Keycloak provides a single source of truth for user identity, credentials, and access policies. This centralization simplifies user management, reduces code duplication, and ensures consistent security posture across all frontend applications.

2. **Industry-Standard Protocols**: By using OAuth 2.0 and OpenID Connect, SLMS aligns with widely adopted standards, making the system easier to integrate with external systems, audit for compliance, and maintain with industry-standard security practices.

3. **Single Sign-On Experience**: Users authenticate once at the Landing Page and gain access to all authorized frontend applications without repeated logins. This improves user experience and reduces authentication friction.

4. **Comprehensive Audit Trail**: Keycloak logs all authentication events, failed login attempts, role changes, and administrative actions. These logs support security monitoring, incident response, and compliance reporting.

5. **Defense in Depth**: Multiple security layers—network isolation, token-based authentication, role-based access control, resource ownership verification—work together to prevent unauthorized access even if one layer is compromised.

6. **Future-Proof Architecture**: Keycloak supports advanced features like multi-factor authentication, biometric login, social identity federation, and passwordless authentication. These can be adopted incrementally without redesigning the authentication architecture.

7. **Microservices Readiness**: As an external service accessible to multiple backends, Keycloak naturally supports a future migration to microservices. Each service can validate tokens independently without requiring a shared session store.

**Trade-offs**

1. **External Dependency**: Keycloak becomes a critical infrastructure component. If Keycloak is unavailable, users cannot authenticate (though already-authenticated users can continue working until their access tokens expire). This requires careful deployment, monitoring, and high availability configuration for production.

2. **Network Latency**: Every authentication request involves an additional network hop to Keycloak. While token validation after initial login is fast (using cached public keys), the login flow is slightly slower than a hypothetical internal authentication system. In practice, this latency is negligible (milliseconds) and is offset by the security benefits.

3. **Learning Curve**: Team members must understand OAuth 2.0, JWT tokens, and Keycloak configuration. This represents a steeper learning curve than simple password-based authentication. However, this investment pays off through reduced security implementation effort and access to Keycloak's extensive documentation and community support.

4. **Configuration Complexity**: Keycloak has many configuration options for realms, clients, roles, and security policies. Initial setup requires careful configuration and testing, particularly around token issuers for Docker networking scenarios. Once configured correctly, however, ongoing management is straightforward.

5. **Token Size**: JWT tokens containing user claims and roles are larger than opaque session tokens, increasing network bandwidth for API requests. The impact is minimal (typically a few hundred bytes per request), but it's a consideration for high-traffic APIs.

**Why the Trade-offs Are Worthwhile**

For a multi-frontend enterprise logistics system like SLMS, the benefits of centralized IAM far outweigh the trade-offs. The security guarantees, audit capabilities, and future extensibility provided by Keycloak would require significant engineering effort to replicate with a custom solution. Moreover, custom authentication systems often suffer from security vulnerabilities that mature IAM solutions like Keycloak have already addressed through years of hardening and community scrutiny.

The BFF pattern adds minimal overhead (one layer of method delegation) while providing substantial benefits in security enforcement, frontend-specific API design, and system evolution flexibility. The clear separation between presentation-layer concerns (handled by BFFs) and business logic (handled by domain services) improves code maintainability and testability.

---

# 9. Key Features & Modules

**Responsible: Henrique Teixeira**

*[8-10 pages organized by frontend application and user stories]*

## 9.1 Admin Console Features

**User Stories**: US1-US3 (Logistics Manager), US4-US6 (Warehouse Staff), US7-US9 (Customer Service Representative)

### 9.1.1 Logistics Manager Dashboard (US1-US3)

**Topics to cover:**

**KPI Monitoring (US1):**
- Real-time metrics dashboard
- Orders per day/week/month
- On-time delivery percentage
- Average delivery time
- Exception rate
- Trend visualization

**Carrier Performance Comparison (US2):**
- Cost comparison across carriers
- Delivery time analysis
- Reliability metrics
- Service level comparison
- On-time delivery rates per carrier

**Report Generation (US3):**
- PDF export
- Excel/CSV export
- Custom date ranges
- Scheduled reports
- Custom KPI selections

**Backend modules**: Reporting, Rate Carrier

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/reporting_module/`

### 9.1.2 Warehouse Operations (US4-US6)

**Topics to cover:**

**Order Registration and Processing (US4):**
- Customer/warehouse order intake
- Order validation
- Tracking number generation
- Database persistence
- Multi-warehouse support
- Order assignment to warehouse

**Label and Document Generation (US5):**
- Shipping labels (PDF)
- Packing slips
- Commercial invoices
- QR code generation for tracking
- Print queue management
- Batch printing support

**Carrier Pickup Confirmation (US6):**
- Pickup scheduling
- Pickup confirmation
- Status update to PICKED_UP
- Order lifecycle tracking

**Warehouse-Specific Views:**
- Dashboard for warehouse staff
- Order filtering by warehouse
- Performance metrics
- Picking and packing workflow

**Backend modules**: Order, Rate Carrier

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/order_module/`

### 9.1.3 Customer Service Tools (US7-US9)

**Topics to cover:**

**Order Status Lookup (US7):**
- Quick order search by tracking number
- Real-time status information
- Order history and events
- Customer details view

**Delivery Exception Handling (US8):**
- Exception reporting (driver/customer initiated)
- Exception categorization (type, severity, priority)
- CSR assignment workflow
- SLA tracking (response time, resolution time)
- Resolution history and notes
- Escalation procedures

**Customer Notification Management (US9):**
- Email and SMS notifications
- Notification templates
- Event-driven alerts
- Customer communication log

**Backend modules**: Visibility, Notification, Order

**Reference:** Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/visibility_module/`

## 9.2 Driver PWA Features

**User Stories**: US10-US13 (Driver/Carrier)

### 9.2.1 Delivery Manifest View (US10)

**Topics to cover:**
- List of deliveries for the day
- Route sequence
- Order details per delivery
- Customer addresses
- Mobile-optimized interface

### 9.2.2 Route Optimization (US11)

**Topics to cover:**
- Route planning support
- Route-based driver assignment
- Optimized delivery sequence
- Navigation integration

### 9.2.3 Proof of Delivery Capture (US12)

**Topics to cover:**

**Photo Capture:**
- Mobile camera integration
- Image compression
- Multiple photo support

**Digital Signature Collection:**
- Canvas-based signature capture
- Signature image generation
- Recipient name input

**Real-Time Status Updates:**
- Status change to DELIVERED
- Timestamp recording
- Location capture (if available)

**MinIO Storage Integration:**
- Upload POD images to MinIO
- Secure storage
- Retrieval for CSR and customer view
- Retention policy

**Reference:** `docs/per-sprint/sprint4/shipment-documents-guide.md`

### 9.2.4 Exception Reporting (US13)

**Topics to cover:**
- Driver-initiated exception reporting
- Automatic exception detection
- Failed delivery documentation
- Issue type selection
- Photo evidence upload

**Backend modules**: Route, Order, Visibility

## 9.3 Customer Portal Features

**User Stories**: US14-US15, US17-US18 (Customer)

### 9.3.1 Account Management (US14-US15)

**Topics to cover:**
- Account creation (US14)
- Secure login (US15)
- Profile management
- Delivery preferences
- Notification preferences

### 9.3.2 Order Tracking (US17)

**Topics to cover:**
- Real-time order tracking
- Tracking by order number
- Delivery status updates
- Delivery notifications
- Estimated delivery time
- Order history

### 9.3.3 Issue Reporting (US18)

**Topics to cover:**
- Customer-initiated issue reporting
- Delivery problem documentation
- Communication with customer service
- Issue status tracking
- Resolution feedback

**Backend modules**: User Management, Order, Visibility

## 9.4 Shared Backend Services

### 9.4.1 Carrier Integration & Rate Management

**Topics to cover:**

**ShipEngine API Integration:**
- REST API client implementation
- Authentication and rate limiting
- Error handling and retries

**Rate Comparison:**
- Request rates from multiple carriers (FedEx, UPS, DHL)
- Service level comparison
- Cost optimization
- Delivery time estimation

**Shipment Creation:**
- Shipment registration with carrier
- Label retrieval
- Tracking number assignment

**Real-Time Tracking:**
- Webhook integration for status changes
- Event aggregation
- Status synchronization

**Carrier Performance Monitoring:**
- On-time delivery rates
- Cost per shipment
- Exception rates

**Reference:**
- `docs/per-sprint/sprint3/carrier-module-readme.md`
- `docs/per-sprint/sprint1/carrier-shipping-apis-investigation.md`
- Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/rate_carrier_module/`

### 9.4.2 Notification System

**Topics to cover:**
- Email and SMS notifications
- Notification templates
- Event-driven alerts
- Scheduled notifications

**Backend module**: Notification

---

# 10. AI-Powered Chatbot

**Reference:** `docs/handbook/chatbot-module.md`, `docs/adr/0007-chatbot-module-integration.md`

The Smart Logistics Management System incorporates an AI-powered chatbot assistant that provides context-aware, role-specific help to users across all frontend applications. Unlike traditional static documentation, the SLMS chatbot understands the user's role, application context, and current page to deliver highly relevant, personalized assistance.

The chatbot addresses several critical challenges:

- **Role-Specific Guidance**: Different roles (Logistics Managers, Warehouse Staff, CSRs, Drivers, Customers) require completely different help content
- **Contextual Relevance**: Help must relate to the user's current page and task
- **Multi-Frontend Consistency**: Four separate frontend applications need consistent yet application-specific guidance
- **Multilingual Support**: English and Portuguese content with cultural appropriateness
- **Conversational Interaction**: Natural language questions with direct, contextual answers

The implementation follows ADR-0007, combining Google Gemini's large language model with Pinecone's vector database to create a Retrieval-Augmented Generation (RAG) system. This architecture grounds responses in actual SLMS documentation while maintaining conversational fluency. The Landing Page chatbot is publicly accessible, while authenticated users receive role-filtered assistance based on Keycloak JWT tokens, maintaining the principle of least privilege.

## 10.1 Chatbot Architecture

The chatbot architecture separates concerns across data storage, semantic search, natural language processing, and business logic orchestration.

**Google Gemini API Integration**

Gemini serves dual roles: the `gemini-embedding-001` model converts text into 768-dimensional semantic vectors for similarity search, while the `gemini-1.5-flash` model generates conversational responses with temperature 0.7 and 2048 token limit. The GeminiClient component handles all API communication with configured timeouts to ensure reliable, non-blocking operations. The GeminiClient supports function calling through the `generateCompletionWithTools()` method, enabling the chatbot to invoke database access tools for real-time information retrieval.

**Pinecone Vector Database**

Pinecone stores help document vectors in a dedicated index (`slms-chatbot`) with 768 dimensions and cosine similarity metric. Documents are organized by locale namespace (en-US, pt-PT) for language-specific queries. Each vector includes metadata (roles, frontends, routes, title, content) enabling filtered semantic search like "vectors similar to question AND role='DRIVER' AND frontend='driver-pwa'". The PineconeClient uses reactive WebClient for non-blocking HTTP communication, supporting vector upsert and query operations.

**Backend Service Layer**

Six specialized services orchestrate chatbot functionality:

- **ChatbotService**: Coordinates the RAG workflow and tool calling loop, receiving requests from BFF controllers, generating embeddings, querying Pinecone, augmenting prompts, calling Gemini for completion, and orchestrating tool execution across multiple iterations (max 3)
- **RagService**: Constructs metadata filters for Pinecone queries, extracts relevant chunks, and augments user prompts with context
- **EmbeddingService**: Abstracts embedding generation, enabling future model migration
- **DocumentManagementService**: Manages help document lifecycle, coordinating dual storage in PostgreSQL (metadata) and Pinecone (vectors + metadata)
- **ToolRegistry**: Auto-discovers Tool implementations via Spring component scanning at startup, maintains a thread-safe registry of available tools, and provides filtered tool definitions based on user roles
- **ToolExecutor**: Validates tool authorization against user roles, validates parameters against tool schemas, executes tools with timeout protection (5-second limit), and returns structured results or error messages

**Data Storage Architecture**

The chatbot uses dual storage: PostgreSQL stores the AssistantDocument entity (UUID id, chunkId, title, locale, content, createdAt, metadataJson) with collection tables for roles, frontends, and routes filtering dimensions. Pinecone stores the same data as vector embeddings with full metadata including content. The chunkId field links both stores.

Critically, **during RAG queries, content comes directly from Pinecone metadata**—PostgreSQL is not queried in the hot path. When Pinecone returns matching vectors after semantic search, the RagService extracts content from the returned metadata without additional database queries. This design optimizes query performance while Pinecone handles both vector similarity and metadata filtering in a single operation.

PostgreSQL serves three purposes: administrative document management (listing, editing, deleting documents), transactional consistency during document creation, and audit trail. The AssistantDocumentRepository provides CRUD operations used by administrators but not during user chat queries.

Document creation synchronously writes to both stores: embedding generation (100-300ms), PostgreSQL insert, and Pinecone upsert. If either fails, the transaction rolls back to prevent inconsistency. This higher creation latency is acceptable since document authoring is infrequent, while query performance remains fast.

**Tool Architecture**

The chatbot extends beyond static documentation through a database access tool system. The Tool interface defines the contract for all tools: `getName()`, `getDescription()`, `getParameters()`, `getAllowedRoles()`, and `execute()`. Seven tool implementations provide real-time database queries:

- **OrderLookupTool**: Retrieve detailed order information by ID or tracking number
- **OrderStatusTool**: Check current order status and event history
- **WarehouseInventoryTool**: Query warehouse information, capacity, and active order counts
- **ShipmentTrackingTool**: Get real-time shipment tracking and carrier information
- **DeliveryExceptionTool**: List active delivery exceptions with severity and assignment details
- **UserCountTool**: Count total system users
- **ShipmentCountTool**: Count total shipments

Each tool is annotated as a Spring `@Component`, enabling automatic discovery by ToolRegistry at application startup. Tools declare their allowed roles through the `getAllowedRoles()` method, forming the basis of the chatbot's authorization system.

## 10.2 RAG Implementation

Retrieval-Augmented Generation (RAG) grounds responses in SLMS documentation rather than relying solely on Gemini's pre-trained knowledge.

**Document Ingestion Pipeline**

Help documents are authored as natural language text (typically markdown) and processed for ingestion.

The EmbeddingService calls Gemini's gemini-embedding-001 to produce 768-dimensional vectors capturing semantic meaning—similar concepts like "shipment tracking" and "order monitoring" produce nearby vectors in high-dimensional space. This computationally expensive operation (100-300ms) occurs once during creation, with embeddings stored for reuse.

The DocumentManagementService coordinates storage: generate embedding, create database entity, save to PostgreSQL, prepare metadata (title, content, locale, roles, frontends, routes), and upsert vector to Pinecone. The transaction rolls back if either database operation fails, maintaining consistency. A DocumentSeederService can automatically populate initial help content on startup when enabled.

**Query Flow**

When a user submits a question, the ChatbotService orchestrates a six-step RAG workflow:

1. **Query Embedding**: The user's question is converted to a 768-dimensional vector using gemini-embedding-001. Questions phrased differently but semantically similar (e.g., "How do I upload proof of delivery?" vs "Where do I submit POD photos?") produce nearby vectors.

2. **Semantic Search with Filtering**: RagService queries Pinecone for top-5 documents, applying metadata filters for frontend, role, and locale. Pinecone executes hybrid search: filter by metadata, compute cosine similarity, return ranked matches. A driver asking about delivery procedures only sees driver-relevant English content, even if warehouse or Portuguese documentation exists.

3. **Context Retrieval and Augmentation**: RagService extracts content from Pinecone metadata and assembles an augmented prompt: "Relevant context:\n[Document 1]\n{content1}\n[Document 2]\n{content2}\n...\n---\nUser question: {message}". This grounds the LLM response in authoritative documentation.

4. **Conversation History Integration**: Previous chat messages (stored client-side) are appended to the prompt, enabling multi-turn dialogues with contextual follow-up questions.

5. **LLM Completion**: A system prompt tailored to frontend and role (customer-portal: "helping customer track orders", driver-pwa: "helping driver with deliveries", admin-console: "helping administrator with operations") is sent to Gemini with augmented context and conversation history. The LLM generates conversational, concise responses (2-3 sentences) using retrieved documentation.

6. **Response Assembly**: ChatbotService packages the LLM response with source document chunk IDs, enabling future citation display and feedback collection.

**[FIGURE 10.1: RAG Query Flow Diagram]**

Sequence diagram showing: User → Frontend → Chat BFF → ChatbotService → (Embedding: EmbeddingService → GeminiClient → Gemini API) → (Retrieval: RagService → PineconeClient → Pinecone with metadata filters) → (Augmentation: RagService assembles context) → (Generation: GeminiClient → Gemini API with augmented prompt) → Response back through layers. Highlight four phases with color coding. Include fallback path for RAG failures.

## 10.3 Security and Access Control

The chatbot integrates with SLMS's security model through role-based access control. Following the Backend-For-Frontend (BFF) pattern described in Section 8, each frontend has a dedicated chat BFF controller that extracts user context from JWT tokens and enforces access rules.

**Public and Authenticated Access**

The Landing Page chatbot endpoint (/api/landing-page/chat) is publicly accessible, returning "ANONYMOUS" for null JWT tokens. Pinecone queries filter for documents tagged with "ANONYMOUS" or empty role sets, providing general information while hiding operational documentation. Authenticated endpoints in Admin Console, Customer Portal, and Driver PWA require valid JWT tokens—BFF controllers extract the primary role and pass it to ChatbotService for filtering.

**Multi-Dimensional Filtering**

Three filtering layers ensure content relevance:

1. **Role Filtering**: Warehouse Staff see order processing help, CSRs see exception handling, Drivers see delivery procedures, Logistics Managers see KPI reports. Multi-role users use their primary role (first in JWT's realm_access.roles array).

2. **Frontend Filtering**: BFF endpoint prefixes determine frontend context (landing-page, driver-pwa, customer-portal, admin-console). A driver using Driver PWA gets mobile-optimized help; the same user in Admin Console gets desktop analytical guidance.

3. **Route Prioritization**: Current page route (e.g., /orders/create, /carriers) is included in chat requests. RagService adds route to metadata filters, boosting contextually relevant documents without excluding others—soft prioritization rather than hard filtering.

This enforces least privilege: users receive only necessary help content, reducing cognitive load, preventing exposure of privileged procedures, and supporting compliance requirements. All interactions are logged with role, frontend, and route context for security auditing and documentation improvement.

**Tool Authorization: Defense-in-Depth**

The chatbot's database access tools implement a three-layer authorization architecture to prevent information leakage:

**Layer 1: LLM-Level Awareness**
The system prompt includes explicit authorization guidance for the user's current role. Gemini receives definitions for **all** tools in the system (not just those the user can access), along with detailed instructions on which tools the user is authorized to use. When users request information requiring unauthorized tools, the LLM is trained to:
- Politely explain the access restriction
- Identify which roles have access to that capability
- Suggest alternative actions within the user's permissions
- Avoid attempting to call unauthorized tools

This LLM-level awareness enables helpful, authorization-aware responses like: "Warehouse inventory information is only accessible to warehouse staff and logistics managers. As a customer, I can help you track your orders or check delivery status."

**Layer 2: Execution-Level Enforcement**
Before executing any tool, ToolExecutor validates the user's role against the tool's `getAllowedRoles()` set. Authorization failures return error messages to the LLM rather than exposing database query results. This enforcement layer protects against:
- LLM mistakes or prompt injection attempts to bypass restrictions
- Tools being called with incorrect parameters
- Execution timeout violations (5-second hard limit per tool)

All authorization checks and tool executions are logged with user context for security auditing.

**Layer 3: Resource-Level Validation**
Individual tools implement fine-grained authorization checks on specific resources. For example, OrderLookupTool allows CUSTOMER role access but validates that `order.customerId` matches `userContext.userId()` before returning order details. This prevents customers from accessing other customers' orders even if they know the order ID. Similarly, DeliveryExceptionTool filters exceptions to show drivers only those related to their assigned deliveries.

This defense-in-depth approach ensures that authorization failures at any layer prevent information disclosure, while the LLM-level awareness provides superior user experience through contextual explanations rather than cryptic error messages.

## 10.4 Tool Calling System for Database Access

While the RAG system provides contextual help based on static documentation, the tool calling system enables the chatbot to answer questions requiring real-time database queries. This capability transforms the chatbot from a documentation assistant into an intelligent data retrieval interface that can look up specific orders, check warehouse inventory, track shipments, and query delivery exceptions—all within the user's security context.

### 10.4.1 Tool Interface and Implementation

All tools implement the `Tool` interface, which defines five core methods:

```java
public interface Tool {
    String getName();                           // Unique identifier (e.g., "order_lookup")
    String getDescription();                    // Description for LLM to understand when to use this tool
    List<ToolParameter> getParameters();        // Parameter schema (name, type, required, description)
    Set<String> getAllowedRoles();              // Roles authorized to use this tool
    ToolResult execute(Map<String, Object> params, UserContext context);
}
```

Each tool is annotated with Spring's `@Component` annotation, enabling automatic discovery during application startup. The ToolRegistry scans the Spring context for all Tool beans and registers them in a thread-safe ConcurrentHashMap, indexed by tool name.

### 10.4.2 Implemented Tools and Capabilities

**OrderLookupTool**
- **Purpose**: Retrieve comprehensive order information by ID or tracking number
- **Parameters**: `orderId` (integer, optional) OR `trackingNumber` (string, optional)
- **Returns**: Full order details including status, customer information, addresses, dimensions, weight, items
- **Authorization**: CUSTOMER (own orders only), DRIVER, WAREHOUSE_STAFF, CUSTOMER_SERVICE, LOGISTICS_MANAGER
- **Resource Validation**: Customers can only access orders where `order.customerId` matches their user ID
- **Use Case**: "What's the status of order #12345?" or "Look up tracking number TRK-ABC123"

**OrderStatusTool**
- **Purpose**: Check current order status and complete event history
- **Parameters**: `orderId` (integer, required)
- **Returns**: Current status enum and chronological list of order events (transitions, timestamps, responsible users)
- **Authorization**: CUSTOMER (own orders), DRIVER, WAREHOUSE_STAFF, CUSTOMER_SERVICE, LOGISTICS_MANAGER
- **Use Case**: "Show me the delivery timeline for order #456" or "When was order #789 picked up?"

**WarehouseInventoryTool**
- **Purpose**: Query warehouse information, capacity, and current utilization
- **Parameters**: `warehouseId` (integer, optional) - omit to list all warehouses
- **Returns**: Warehouse details (name, location, capacity), active order count, availability status
- **Authorization**: WAREHOUSE_STAFF, LOGISTICS_MANAGER
- **Use Case**: "How many orders are currently in warehouse 3?" or "Show all warehouse locations"

**ShipmentTrackingTool**
- **Purpose**: Get real-time shipment tracking information from carriers
- **Parameters**: `shipmentId` (integer, optional) OR `trackingNumber` (string, optional)
- **Returns**: Carrier information, current location, delivery ETA, tracking events
- **Authorization**: CUSTOMER (own shipments), DRIVER (assigned), CUSTOMER_SERVICE, LOGISTICS_MANAGER
- **Use Case**: "Where is my shipment?" or "Track shipment #9876"

**DeliveryExceptionTool**
- **Purpose**: List and query active delivery exceptions and issues
- **Parameters**: `orderId` (integer, optional), `severity` (string, optional: LOW, MEDIUM, HIGH, CRITICAL)
- **Returns**: List of exceptions with type, severity, priority, assignment, resolution status, notes
- **Authorization**: CUSTOMER (own orders), CUSTOMER_SERVICE, LOGISTICS_MANAGER
- **Use Case**: "Show all critical delivery exceptions" or "Are there any issues with my order?"

**UserCountTool**
- **Purpose**: Count total users registered in the system
- **Parameters**: None
- **Returns**: Total user count across all roles
- **Authorization**: LOGISTICS_MANAGER (admin analytics only)
- **Use Case**: "How many users are in the system?"

**ShipmentCountTool**
- **Purpose**: Count total shipments processed by the system
- **Parameters**: None
- **Returns**: Total shipment count
- **Authorization**: LOGISTICS_MANAGER (admin analytics only)
- **Use Case**: "How many shipments have we processed?"

### 10.4.3 Tool Execution Workflow

The tool calling workflow integrates seamlessly with the RAG query flow described in Section 10.2:

1. **Initial Query Processing**: User submits a question like "What's the status of order #12345?" through the chat interface

2. **RAG Context Retrieval**: The system performs standard RAG retrieval to gather relevant documentation context about order tracking

3. **LLM Request with Tool Definitions**: ChatbotService calls `geminiClient.generateCompletionWithTools()`, providing:
   - System prompt with authorization guidance for the user's role
   - Augmented user question with RAG context
   - Conversation history
   - **All** tool definitions in Gemini's function calling format (not just allowed tools, to enable authorization-aware responses)

4. **LLM Decision**: Gemini analyzes the question and determines:
   - Does this require database access? (Yes - order status check)
   - Which tool should I use? (order_status or order_lookup)
   - What parameters? (orderId: 12345)
   - Am I authorized? (Checks system prompt authorization guidance)

5. **Tool Call Extraction**: If Gemini's response includes function calls, ChatbotService extracts the `FunctionCall` objects containing tool name and arguments

6. **Authorization and Execution**: For each function call:
   - ToolExecutor retrieves the tool from ToolRegistry
   - Validates user role against tool's `getAllowedRoles()` (Layer 2 enforcement)
   - Validates parameters against tool's parameter schema
   - Executes tool with 5-second timeout protection
   - Returns `ToolResult` with success data or error message

7. **Tool Result Integration**: Tool results are added to the conversation history as assistant messages

8. **Iterative Refinement**: ChatbotService calls Gemini again with the conversation history including tool results. Gemini generates a natural language response incorporating the real data: "Order #12345 is currently IN_TRANSIT and was picked up on January 5th. It's expected to arrive on January 8th."

9. **Iteration Control**: The loop continues for up to 3 iterations, allowing multi-step tool usage (e.g., look up order, then check related shipment tracking, then query exceptions)

10. **Final Response**: The assistant's final text response is returned to the user

**Error Handling and Fallbacks**:
- Authorization failures return helpful messages explaining which roles have access
- Tool execution timeouts return timeout errors after 5 seconds
- Database query failures return error messages without exposing internal details
- If all tools fail, the chatbot falls back to RAG-only responses based on documentation

### 10.4.4 Multi-Iteration Tool Calling

The chatbot supports multi-turn tool usage within a single user question, enabling complex queries like:

**User**: "Show me my most recent order and check if there are any delivery issues"

**Iteration 1**: LLM calls `order_lookup` (with customer ID filter) → Returns Order #12345
**Iteration 2**: LLM calls `delivery_exception_tool` (with orderId: 12345) → Returns no exceptions
**Final Response**: "Your most recent order is #12345, currently IN_TRANSIT. There are no delivery issues reported."

This multi-iteration capability is controlled by `MAX_TOOL_ITERATIONS = 3` in ChatbotService, preventing infinite loops while allowing sophisticated query patterns. Each iteration is logged with timing and authorization context for debugging and security auditing.

## 10.5 Configuration and Deployment

The chatbot requires Gemini API keys (for LLM and embeddings) and Pinecone credentials configured via environment variables. Before deployment, a Pinecone index must be created with 768 dimensions and cosine similarity metric. API keys should be managed securely through secret management systems and never committed to version control. The DocumentSeederService can automatically populate initial help content when enabled (chatbot.seeder.enabled=true), though this should be disabled in production after initial deployment to prevent duplicate content creation.

Both Gemini and Pinecone are usage-based services requiring cost monitoring. Gemini charges per million tokens, while Pinecone charges for index hosting and query volume. The chatbot integrates with Spring Boot Actuator health checks for monitoring API availability and alerting on service degradation.

---

# 11. Testing & Quality Assurance

**Responsible: Hugo Sousa**

*[5-6 pages with testing approach]*

**Reference:** `docs/per-sprint/sprint2/qa-manual-update/qa-manual.md`

## 11.1 Testing Strategy

**Topics to cover:**

**Test Pyramid Approach:**
- Many unit tests (base)
- Fewer integration tests (middle)
- Few E2E tests (top)

**Test-Driven Development:**
- Write tests first (when applicable)
- Red-Green-Refactor cycle
- Living documentation

**Continuous Testing:**
- Tests run on every commit
- Fast feedback loop
- Quality gates

## 11.2 Test Coverage

### 11.2.1 Backend Testing

**Topics to cover:**

**JUnit 5 + Mockito:**
- Unit test framework
- Mocking external dependencies
- Parameterized tests
- Test lifecycle

**Service Layer Testing:**
- Business logic validation
- Mocked repositories
- Edge case handling

**Controller Testing:**
- MockMvc for API testing
- Request/response validation
- Authentication testing
- Error handling

**Repository Testing:**
- Database integration tests
- Query validation
- Transaction behavior

**Example test structure:**
```java
@SpringBootTest
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        // Arrange
        // Act
        // Assert
    }
}
```

### 11.2.2 Frontend Testing

**Topics to cover:**

**Jest + React Testing Library:**
- Component unit tests
- User interaction testing
- Snapshot testing

**Test Examples:**
- Form validation
- API integration (mocked)
- Navigation
- State management

### 11.2.3 Integration Testing

**Topics to cover:**

**Mocked External APIs:**
- ShipEngine mock responses
- Gemini mock responses
- Pinecone mock responses
- WireMock or similar

**Database Integration:**
- Testcontainers for PostgreSQL
- Test data setup
- Transaction rollback

**Module Integration:**
- Cross-module interactions
- BFF integration with services

### 11.2.4 E2E Testing

**Topics to cover:**

**Playwright:**
- Browser automation
- User flow testing
- Cross-browser support
- Visual regression

**Test Scenarios:**
- Complete user workflows
- Critical paths
- Error scenarios

### 11.2.5 Real Integration Tests

**Topics to cover:**

**Actual Service Integration:**
- Requires API keys
- Real Gemini API calls
- Real Pinecone queries
- Real MinIO storage
- Real Keycloak authentication

**Test Execution:**
```bash
make test-real
# OR
docker exec slms-backend-dev ./mvnw test -Preal-integration
```

**Tagged with `@Tag("real-integration")`**

## 11.3 Coverage Requirements

**Topics to cover:**

**Target: 70% code coverage**

**JaCoCo Reports:**
- Line coverage
- Branch coverage
- Method coverage
- Coverage report location: `target/site/jacoco/`

**Coverage Gates in CI:**
- Build fails if coverage drops below threshold
- Enforce coverage on new code

**Exclusions:**
- DTOs
- Configuration classes
- Main application class

## 11.4 QA Manual

**Reference:** `docs/per-sprint/sprint2/qa-manual-update/qa-manual.md`

**Topics to cover:**

**Manual Test Scenarios:**
- Organized by persona
- Step-by-step instructions
- Expected results
- Pass/fail criteria

**Acceptance Criteria Validation:**
- Each user story has test scenarios
- Verify acceptance criteria met
- Edge cases documented

**Regression Testing:**
- Test suite for major features
- Run before releases
- Automated where possible

**Exploratory Testing:**
- Unscripted testing
- Edge case discovery
- Usability testing

## 11.5 CI/CD Validation

**Topics to cover:**

**Automated Test Execution:**
- Unit tests on every commit
- Integration tests on PR
- E2E tests before merge

**Build Verification:**
- Clean build required
- No compilation warnings (target)
- Dependency vulnerability scanning

**Health Check Validation:**
- Post-deployment health checks
- Endpoint availability
- Service connectivity

**Pre-Deployment Checks:**
- All tests pass
- Coverage threshold met
- No critical issues

**Reference:** `.github/workflows/build.yml`

---

# 12. Observability & Monitoring

**Responsible: Henrique Teixeira**

*[6-8 pages with observability strategy]*

**Reference:** `docs/handbook/observability.md`, `docs/per-sprint/sprint4/observability.md`, `docs/adr/0005-observability-monitoring-strategy.md`

## 12.1 Service Level Indicators (SLIs)

**Topics to cover:**

| SLI | Definition | Target SLO | Measurement Window |
|-----|-----------|------------|-------------------|
| **Availability** | % of successful requests | ≥99.5% | 30 days |
| **Latency (p95)** | 95th percentile duration | <500ms | 24 hours |
| **Latency (p99)** | 99th percentile duration | <1500ms | 24 hours |
| **Error Rate** | % of failed requests | <1% | 24 hours |
| **Data Integrity** | % successful DB ops | ≥99.99% | 24 hours |

## 12.2 Service Level Objectives (SLOs)

**Topics to cover:**

**Detailed targets per endpoint type:**
- Read endpoints: <200ms p95
- Write endpoints: <500ms p95
- Complex queries: <1000ms p95

**Error Budget:**
- Calculation methodology
- Budget consumption tracking
- Response to budget exhaustion

## 12.3 Monitoring Stack

**Topics to cover:**

**Elastic APM:**
- Backend instrumentation (Spring Boot)
- Frontend instrumentation (Next.js)
- Automatic tracing
- Performance metrics

**Structured Logging:**
- JSON format
- Correlation IDs
- Log levels and filtering
- Centralized log aggregation

**Health Check Endpoints:**
- `/actuator/health`
- Component health indicators
- Database connectivity
- External service checks

**Performance Metrics:**
- Request rate
- Response time distribution
- Error rate
- Throughput

**Reference:** `docs/per-sprint/sprint4/health-checks-implementation.md`

## 12.4 Alerting

**Topics to cover:**

**Alert Types:**
- SLO breach notifications
- Critical error alerts (5xx errors)
- Capacity warnings (CPU, memory, disk)
- External service failures

**Alert Channels:**
- Email notifications
- Slack integration
- PagerDuty (if applicable)

**Alert Thresholds:**
- Based on SLO definitions
- Actionable vs informational

---

# 13. Lessons Learned & Future Work

*[3-4 pages of critical reflection]*

## 13.1 Architectural Decisions

**Topics to cover:**

**Modular Monolith Benefits:**
- Simplified deployment
- Easier debugging
- Faster development initially
- Clear module boundaries

**Modular Monolith Limitations:**
- Scaling entire application instead of individual modules
- Potential for boundary erosion
- Shared database considerations

**BFF Pattern Effectiveness:**
- Reduced frontend complexity
- Improved security
- Backend complexity increase
- Code duplication across BFFs

**Phased Data Model Implementation:**
- Iterative approach benefits
- Evolution over sprints
- Migration challenges

## 13.2 Technical Challenges

**Topics to cover:**

**Cross-Module Dependencies:**
- Order ↔ Shipment relationship complexity
- Managing circular dependencies
- Interface design between modules

**External API Integration:**
- ShipEngine reliability and rate limits
- API versioning challenges
- Error handling and retries
- Mock testing complexity

**Authentication Complexity:**
- Keycloak learning curve
- Multi-client configuration
- Token management across frontends
- Debugging auth issues

**Real-Time Tracking Limitations:**
- Webhook reliability
- Polling vs push
- Event ordering

**AI Integration Challenges:**
- Gemini API rate limits
- Pinecone vector dimension selection
- Document chunking strategy
- Embedding model selection

## 13.3 Development Process

**Topics to cover:**

**Agile/Scrum Effectiveness:**
- Sprint planning accuracy
- Velocity tracking
- Retrospective insights
- Standup value

**Sprint Planning Accuracy:**
- Initial estimation challenges
- Improved over time
- Unforeseen complexities

**Team Collaboration:**
- Git workflow effectiveness
- Code review process
- Documentation practices
- Communication tools (Slack, GitHub, etc.)

**Documentation-Driven Development:**
- ADRs as decision log
- Living handbook
- Sprint documentation value
- Onboarding improvements

## 13.4 Future Enhancements

**Topics to cover:**

**Removed/Pending Features:**
- US16: Real-time GPS tracking (complexity vs value)
- Advanced route optimization algorithms
- Machine learning for delivery predictions

**Enhanced Notification System:**
- SMS integration
- Push notifications for mobile apps
- Customizable notification preferences
- Notification templates

**Performance Optimizations:**
- Database query optimization
- Caching layer (Redis)
- Frontend bundle optimization
- API response pagination

**Microservices Migration:**
- Potential candidates: Chatbot, Reporting modules
- Event-driven architecture
- Service mesh (Istio, Linkerd)
- Gradual extraction strategy

**Additional Carrier Integrations:**
- More carriers beyond FedEx, UPS, DHL
- Regional carrier support
- Carrier API abstraction layer

**Mobile App Improvements:**
- Native mobile apps (React Native, Flutter)
- Offline mode for driver PWA
- Enhanced camera and GPS features
- Push notifications

**Advanced Analytics:**
- Predictive analytics for delivery times
- Anomaly detection
- Customer behavior insights
- Cost optimization recommendations

**Internationalization:**
- Multi-language support beyond English/Portuguese
- Multi-currency support
- Regional compliance (GDPR, etc.)

---

# 14. Conclusion

*[2-3 pages of synthesis and reflection]*

## 14.1 Project Achievements Summary

**Topics to cover:**

**Functional Deliverables:**
- Comprehensive logistics management platform
- 18 user stories (12 completed, 5 in progress)
- 5 persona support with role-based features
- Multi-carrier integration
- AI-powered assistance
- Real-time tracking and exception handling

**Technical Deliverables:**
- Modular monolith architecture
- 8 backend modules
- 4 frontend applications
- Complete CI/CD pipeline
- Infrastructure as Code
- Comprehensive observability

**Process Deliverables:**
- Agile/Scrum methodology
- Sprint-based delivery
- Comprehensive documentation (32+ markdown files)
- Architecture Decision Records
- Developer handbook

## 14.2 Technical Contributions

**Topics to cover:**

**Architectural Contributions:**
- Modular monolith with clear boundaries
- BFF pattern implementation
- Keycloak integration for multi-tenant auth
- Scalable infrastructure design

**Integration Achievements:**
- ShipEngine multi-carrier API
- AI integration (Gemini + Pinecone)
- Object storage with MinIO
- Observability with Elastic APM

**Quality Practices:**
- Test pyramid implementation
- 70% code coverage target
- Automated CI/CD
- Comprehensive QA manual

**Documentation Excellence:**
- ADRs for all major decisions
- Living handbook
- Onboarding guide
- Sprint artifacts

## 14.3 Academic Learning Outcomes

**Topics to cover:**

**Software Engineering Principles:**
- Requirements engineering (personas, scenarios, user stories)
- Architectural design and ADRs
- Agile/Scrum methodology
- Software quality and testing

**Technical Skills:**
- Modern Java (Spring Boot, JPA)
- Frontend development (React, Next.js)
- Infrastructure as Code (Terraform)
- Containerization (Docker)
- CI/CD (GitHub Actions)

**Collaboration Skills:**
- Version control (Git)
- Code review
- Documentation
- Team coordination

**Problem-Solving:**
- Integration challenges
- Authentication complexity
- Performance optimization
- Error handling

## 14.4 Industry Relevance

**Topics to cover:**

**Real-World Applications:**
- E-commerce logistics challenges
- Multi-carrier management
- Customer experience improvements
- Operational efficiency

**Technology Stack Alignment:**
- Industry-standard technologies
- Cloud-native approach
- Microservices readiness
- Modern DevOps practices

**Scalability and Maintainability:**
- Production-ready architecture
- Monitoring and observability
- Security best practices
- Documentation for handoff

## 14.5 Final Remarks

**Topics to cover:**

**Project Success:**
- Achieved core objectives
- Functional and complete system
- Solid foundation for future work

**Team Performance:**
- Effective collaboration
- Role clarity and accountability
- Continuous improvement

**Future Potential:**
- Migration to microservices
- Enhanced AI features
- Expanded carrier support
- Market readiness

**Acknowledgments:**
- Course instructors
- Team members
- External service providers (ShipEngine, Google, Pinecone)

---

# 15. Appendices

**Responsible: Everyone (Collaborative)**

## Appendix A: API Endpoint Documentation

**Reference:** `docs/per-sprint/sprint2/endpoint-listing/`

**Complete REST API reference organized by role:**

### A.1 Logistics Manager Endpoints

**Reference:** `docs/per-sprint/sprint2/endpoint-listing/logistics-manager-endpoint-listing.md`

**Topics to cover:**
- KPI dashboard endpoints
- Carrier comparison endpoints
- Report generation endpoints
- Analytics endpoints

### A.2 Warehouse Staff Endpoints

**Reference:** `docs/per-sprint/sprint2/endpoint-listing/warehouse-management-endpoint-listing.md`

**Topics to cover:**
- Order registration endpoints
- Label generation endpoints
- Pickup confirmation endpoints
- Warehouse-specific endpoints

### A.3 Customer Service Endpoints

**Reference:** `docs/per-sprint/sprint2/endpoint-listing/customer-service-endpoint-listing.md`

**Topics to cover:**
- Order lookup endpoints
- Exception management endpoints
- Customer notification endpoints
- Resolution tracking endpoints

### A.4 Driver/Carrier Endpoints

**Reference:** `docs/per-sprint/sprint2/endpoint-listing/driver-carrier-endpoint-listing.md`

**Topics to cover:**
- Delivery manifest endpoints
- Route endpoints
- POD upload endpoints
- Exception reporting endpoints

### A.5 Customer Endpoints

**Reference:** `docs/per-sprint/sprint2/endpoint-listing/customer-endpoint-listing.md`

**Topics to cover:**
- Order tracking endpoints
- Notification preference endpoints
- Issue reporting endpoints
- Account management endpoints

## Appendix B: Architecture Decision Records

**Reference:** `docs/adr/` directory

**Full ADR documentation:**

### B.1 ADR-0001: Initial System Architecture

**Reference:** `docs/adr/0001-initial-system-architecture.md`

**Summary:**
- Decision: Modular monolith architecture
- Context: Multi-role system with carrier integration needs
- Consequences: Simplified deployment, clear module boundaries

### B.2 ADR-0002: Data Model Definition

**Reference:** `docs/adr/0002-data-model-definition.md`

**Summary:**
- Initial database schema design
- Entity relationships
- Primary key strategy

### B.3 ADR-0003: Architecture Update - DB Integration

**Reference:** `docs/adr/0003-architecture-update-db-integration.md`

**Summary:**
- PostgreSQL 16 selection
- JPA/Hibernate configuration
- Connection pooling strategy

### B.4 ADR-0004: Architecture Update - Security & Auth

**Reference:** `docs/adr/0004-architecture-update-security-&-auth.md`

**Summary:**
- Keycloak adoption for authentication
- OAuth2/OIDC implementation
- JWT token-based authorization
- Role-based access control

### B.5 ADR-0005: Observability Monitoring Strategy

**Reference:** `docs/adr/0005-observability-monitoring-strategy.md`

**Summary:**
- Elastic APM selection
- SLI/SLO definitions (availability, latency, error rate)
- Three pillars: logs, metrics, traces
- OpenTelemetry instrumentation

### B.6 ADR-0006: Shipment Entity Introduction (if exists)

**Summary:**
- Separation of Order and Shipment concepts
- Carrier-specific shipment handling

### B.7 ADR-0007: AI Chatbot Module (if exists)

**Summary:**
- Gemini for LLM
- Pinecone for vector search
- Document ingestion strategy

### B.8 ADR-0008: Data Model Revision (if exists)

**Summary:**
- Data model updates and improvements
- Migration strategy

## Appendix C: Database Schema Diagrams

**Reference:** `docs/per-sprint/sprint2/data-model-definition/data-model-definition.md`

**Topics to cover:**

### C.1 Entity-Relationship Diagrams

- Overall ER diagram
- Module-specific diagrams
- Relationship cardinalities

### C.2 Table Definitions

**Detailed schema for each table:**
- Field names and types
- Constraints (PK, FK, unique, not null)
- Indexes
- Sample data

## Appendix D: Deployment Scripts

**Reference:** `Makefile`, `infra/deploy.sh`, `infra/terraform/`

### D.1 Makefile Targets

**Reference:** `Makefile`

**Complete list of make commands:**
- Production: `up`, `down`, `destroy`, `restart`, `logs`, `status`
- Development: `dev-up`, `dev-down`, `dev-restart`, `dev-logs`
- Build: `install`, `build`
- Test: `test`, `test-unit`, `test-integration`, `test-e2e`, `test-real`
- Cleanup: `clean`, `clean-volumes`, `hard-prune`

### D.2 Terraform Configurations Overview

**Reference:** `infra/terraform/`

**Key Terraform files:**
- `main.tf` - Resource definitions
- `variables.tf` - Input variables
- `outputs.tf` - Output values
- `terraform.tfvars.example` - Variable template

### D.3 Docker Compose Files

**Reference:** `infra/docker-compose.yaml`, `infra/docker-compose.dev.yml`

**Service definitions:**
- Backend
- Frontends (4 apps)
- PostgreSQL
- Keycloak
- MinIO
- Nginx

## Appendix E: Configuration Templates

**Reference:** `.env.example` files

### E.1 Backend Environment Template

**Reference:** `src/backend/.env.example`

**Complete .env template with descriptions**

### E.2 Infrastructure Environment Template

**Reference:** `infra/.env.example`

**Infrastructure configuration template**

### E.3 Terraform Variables Template

**Reference:** `infra/terraform/terraform.tfvars.example`

**Terraform variable template**

## Appendix F: Glossary

**Technical Terms:**
- **ADR** - Architecture Decision Record
- **APM** - Application Performance Monitoring
- **BFF** - Backend-for-Frontend
- **CORS** - Cross-Origin Resource Sharing
- **CSR** - Customer Service Representative
- **ER** - Entity-Relationship
- **IaC** - Infrastructure as Code
- **JWT** - JSON Web Token
- **KPI** - Key Performance Indicator
- **LLM** - Large Language Model
- **OAuth2** - Open Authorization 2.0
- **OIDC** - OpenID Connect
- **ORM** - Object-Relational Mapping
- **POD** - Proof of Delivery
- **PWA** - Progressive Web App
- **RBAC** - Role-Based Access Control
- **SLA** - Service Level Agreement
- **SLI** - Service Level Indicator
- **SLO** - Service Level Objective
- **SLMS** - Smart Logistics Management System
- **SSR** - Server-Side Rendering
- **SSG** - Static Site Generation

**Domain-Specific Terminology:**
- **Carrier** - Third-party shipping company (FedEx, UPS, DHL)
- **Delivery Exception** - Problem during delivery requiring resolution
- **Manifest** - List of deliveries for a driver
- **Rate Shopping** - Comparing shipping rates across carriers
- **Shipment** - Carrier-assigned package movement
- **Tracking Number** - Unique identifier for order/shipment
- **Warehouse** - Physical location for order processing

---

## Document Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-03 | Group 304 | Initial report structure |

---

**End of Report Structure**

*Note: This document provides a complete structural outline for the SLMS Project Report. Each section contains topic bullets and references to existing documentation. The report should be expanded with detailed content, maintaining academic rigor while leveraging the comprehensive documentation already created by the team.*
