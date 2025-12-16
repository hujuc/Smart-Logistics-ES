# SLMS Documentation

## Getting Started

### Essential Reading
- [Developer Handbook](handbook/dev-handbook.md) - Setup, architecture overview, getting started guide
- [Repository Workflow](handbook/repo-workflow.md) - Git workflow, branching strategy, PR process

### Quick Links
- [Data Model](handbook/data-model.md) - Current database schema (7-8 tables)
- [Chatbot Module](handbook/chatbot-module.md) - AI assistant functionality
- [Observability](handbook/observability.md) - SLIs, SLOs, and monitoring

## Architecture

### Architecture Decision Records (ADRs)

ADRs document significant architectural decisions made throughout the project lifecycle.

| ADR | Title | Sprint |
|-----|-------|--------|
| [0001](adr/0001-initial-system-architecture.md) | Initial System Architecture | 1 |
| [0002](adr/0002-data-model-definition.md) | Data Model Definition (Original Plan) | 2 | 
| [0003](adr/0003-architecture-update-db-integration.md) | Architecture Update - DB Integration | 2 |
| [0004](adr/0004-architecture-update-security-&-auth.md) | Security & Authentication | 2 |
| [0005](adr/0005-observability-monitoring-strategy.md) | Observability and Monitoring | 4 |
| [0006](adr/0006-shipment-entity-introduction.md) | Shipment Entity Introduction | 3 |
| [0007](adr/0007-chatbot-module-integration.md) | Chatbot Module Integration | 4 |
| [0008](adr/0008-data-model-implementation-strategy.md) | Data Model Implementation Strategy | 2-4 |

### Key Concepts

- **Modular Monolith:** Single Spring Boot application with clear module boundaries
- **BFF Pattern:** Each frontend (admin-console, customer-portal, driver-pwa) has its own Backend-for-Frontend
- **Order vs Shipment:** Orders represent customer requests; Shipments represent carrier execution
- **Cross-Module References:** Modules reference each other via ID fields, not entity references

## Data Model

### Current Implementation
- [Data Model Handbook](handbook/data-model.md) - Implemented tables and schema (current state)
- [ADR-0008: Implementation Strategy](adr/0008-data-model-implementation-strategy.md) - Why implementation differs from original plan

### Historical Documentation
- [ADR-0002: Original Data Model](adr/0002-data-model-definition.md) - Comprehensive 18-table design (historical record)
- [Sprint 2 Data Model Definition](per-sprint/sprint2/data-model-definition/data-model-definition.md) - Detailed original schema

### Key Changes
- **Shipment Entity Added:** See [ADR-0006](adr/0006-shipment-entity-introduction.md)
- **Chatbot Module Added:** See [ADR-0007](adr/0007-chatbot-module-integration.md)
- **Users Table Removed:** Keycloak handles authentication
- **Phased Implementation:** Core tables first, advanced features deferred

## Features & Modules

### Implemented Modules
- [Chatbot Module](handbook/chatbot-module.md) - AI assistant with role/frontend/route-based filtering
- [Observability](handbook/observability.md) - SLIs, SLOs, and monitoring strategy
- [Frontend Observability Setup](per-sprint/sprint5/frontend-observability-setup.md) - Elastic APM RUM implementation guide
- [Carrier Module](per-sprint/sprint3/carrier-module-readme.md) - ShipEngine integration and carrier management
- [Shipment Documents](per-sprint/sprint4/shipment-documents-guide.md) - Document generation and management
- [Health Checks](per-sprint/sprint4/health-checks-implementation.md) - Application health monitoring

### Technical Guides
- [AWS Deployment Guide](per-sprint/sprint4/aws-deployment-guide.md) - Cloud deployment instructions

## Requirements & Design

### User Requirements
- [Personas & User Stories](per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md) - User personas, scenarios, and 18 user stories
- [Use Case Diagrams](per-sprint/sprint2/use-case-diagrams/) - Visual representation of system interactions

### API Documentation
- [Endpoint Listings](per-sprint/sprint2/endpoint-listing/) - API endpoints organized by role:
  - [Logistics Manager Endpoints](per-sprint/sprint2/endpoint-listing/logistics-manager-endpoint-listing.md)
  - [Warehouse Staff Endpoints](per-sprint/sprint2/endpoint-listing/warehouse-management-endpoint-listing.md)
  - [Customer Service Endpoints](per-sprint/sprint2/endpoint-listing/customer-service-endpoint-listing.md)
  - [Driver/Carrier Endpoints](per-sprint/sprint2/endpoint-listing/driver-carrier-endpoint-listing.md)
  - [Customer Endpoints](per-sprint/sprint2/endpoint-listing/customer-endpoint-listing.md)

### Design Resources
- [Prototypes](per-sprint/sprint2/prototypes/) - UI/UX design mockups
- [QA Manual](per-sprint/sprint2/qa-manual-update/qa-manual.md) - Quality assurance procedures

## Sprint Documentation (Historical)

Sprint-by-sprint documentation preserves the evolution of the project.

### Sprint 1 - Initial Planning
- [Project Specification](per-sprint/sprint1/project-specification.md)
- [Requirements](per-sprint/sprint1/requirements.md)
- [Epic & Sprint Planning](per-sprint/sprint1/epic-&-sprint-planning.md)
- [Personas & User Stories](per-sprint/sprint1/personas-scenarios-US.md)
- [QA Manual](per-sprint/sprint1/qa-manual.md)
- [Carrier APIs Investigation](per-sprint/sprint1/carrier-shipping-apis-investigation.md)

### Sprint 2 - Core Development
- [Data Model Definition](per-sprint/sprint2/data-model-definition/data-model-definition.md)
- [Personas & User Stories Update](per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md)
- [Use Case Diagrams](per-sprint/sprint2/use-case-diagrams/)
- [Endpoint Listings](per-sprint/sprint2/endpoint-listing/)
- [Prototypes](per-sprint/sprint2/prototypes/)
- [QA Manual Update](per-sprint/sprint2/qa-manual-update/qa-manual.md)
- Architecture Updates:
  - [Database Integration](per-sprint/sprint2/architecture-update-db-integration/)
  - [Security & Authentication](per-sprint/sprint2/architecture-update-security-&-auth/)

### Sprint 3 - Carrier Integration
- [Carrier Module README](per-sprint/sprint3/carrier-module-readme.md)

### Sprint 4 - Production Readiness
- [Observability](per-sprint/sprint4/observability.md)
- [Health Checks Implementation](per-sprint/sprint4/health-checks-implementation.md)
- [Shipment Documents Guide](per-sprint/sprint4/shipment-documents-guide.md)
- [AWS Deployment Guide](per-sprint/sprint4/aws-deployment-guide.md)

### Sprint 5 - Frontend Observability
- [Frontend Observability Setup](per-sprint/sprint5/frontend-observability-setup.md)

### Sprint 6 - Requirements Refinement
- [Use Case Diagrams Updated](per-sprint/sprint6/use-case-diagrams-updated/)
- [User Stories Update](per-sprint/sprint6/user-stories-update.md)

## Handbook

Developer guides and operational procedures.

- [Developer Handbook](handbook/dev-handbook.md) - Essential development guide
- [Architecture Diagram](handbook/Architecture_SLMS.png) - System architecture overview
- [Data Model](handbook/data-model.md) - Current database schema
- [Chatbot Module](handbook/chatbot-module.md) - AI assistant documentation
- [Observability](handbook/observability.md) - SLIs and SLOs
- [CI/CD Pipeline](handbook/ci-cd-pipeline.md) - Continuous integration and deployment workflows
- [Repository Workflow](handbook/repo-workflow.md) - Git workflow and PR process

## Contributing

### Creating New ADRs

1. Copy the template from [adr/adr-template.md](adr/adr-template.md)
2. Number sequentially (e.g., 0009)
3. Fill in all required sections
4. Update this README index
5. Submit for team review

### Documentation Standards

- Use markdown format
- Be concise and factual
- No emojis (except in examples where necessary)
- Include code examples where helpful
- Link to related documentation
- Keep ADRs immutable (historical record)
- Update handbooks with current implementation

## Resources

### External Documentation
- [Spring Boot 3.5 Documentation](https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/)
- [Next.js 15 Documentation](https://nextjs.org/docs)
- [Elastic APM Documentation](https://www.elastic.co/guide/en/apm/guide/current/index.html)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [ShipEngine API Documentation](https://www.shipengine.com/docs/)

### Project Resources
- GitHub Repository: [group-project-es2526_304](https://github.com/detiuaveiro/group-project-es2526_304)
- Elastic Cloud Console: [cloud.elastic.co](https://cloud.elastic.co)
