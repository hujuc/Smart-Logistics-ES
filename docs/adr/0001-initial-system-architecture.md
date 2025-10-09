# 0001 – Initial System Architecture

**Sprint 1**

## Context

At the start of the Smart Logistics Management System (SLMS) project, we needed to define a foundational architecture that could support multiple user roles (Logistics Manager, Warehouse Staff, Customer Service Representative, Driver, and Customer), integrate with third-party carrier APIs, and provide real-time tracking and analytics capabilities.

**Key requirements:**

- Support for multi-role access with role-based permissions
- Real-time order tracking and status updates
- Integration with multiple third-party shipping carriers (FedEx, UPS, DHL)
- Scalable and maintainable architecture
- Clear separation between internal operations and customer-facing functionality
- Mobile support for drivers

**Constraints:**

- Limited initial development resources
- Need to integrate with existing carrier APIs
- Must support both web and mobile interfaces
- Performance requirements: 3-second response times, 2-second order processing

**Options considered:**

- Monolithic architecture
- Microservices architecture
- Modular monolith with clear domain boundaries

## Decision

We chose a **modular monolith architecture** with clear domain boundaries and the following key components:

### Logic Architecture

The system is organized into the following core modules within a single deployable application:

1. **Order Shipment Module** - Handles order registration, document generation, and status tracking
2. **Carrier Integration Module** - Manages API connections to third-party carriers (FedEx, UPS, DHL)
3. **Visability Module** - Aggregates and provides real-time tracking information
4. **Route Module** - Generates optimized delivery routes for drivers
5. **Notification Module** - Handles email, SMS, and in-app notifications
6. **Reporting Module** - Provides KPI dashboards and generates reports

**Separate Frontend Applications (with BFF pattern):**

7. **Customer Portal** - Public-facing interface for order tracking and delivery management (Next.js + BFF)
8. **Internal Console** - Internal interface for warehouse staff, CSRs, and managers (Next.js + BFF)
9. **Driver PWA** - Progressive Web App for drivers with proof of delivery and route navigation (Next.js + BFF)

Each frontend has its own Backend-for-Frontend (BFF) layer that aggregates and transforms data from the main backend API to suit specific frontend needs.

### Installation Architecture

**Backend Infrastructure:**

- **Spring Boot Application** - Single monolithic backend with modular structure

**Frontend Infrastructure:**

- Three separate Next.js applications (customer-portal, admin-console, driver-pwa)
- Each frontend communicates with backend via REST API

Refer to architecture diagrams:

- [Logic Architecture](../per-sprint/sprint1/logic-architecture.jpg)
- [Installation Architecture](../per-sprint/sprint1/instalation-architecture.jpg)

## Consequences

**Positive:**

- **Simplicity** - Single deployable backend reduces operational complexity
- **Development speed** - Faster initial development without distributed system overhead
- **Easier debugging** - Single process makes debugging and tracing issues straightforward
- **Data consistency** - Shared database ensures ACID transactions across all operations
- **No network latency** - Module-to-module calls are in-process, avoiding network overhead
- **Simpler testing** - Integration tests run in a single process
- **Clear module boundaries** - Organized by domain (order, tracking, carrier, etc.) ensures maintainability
- **Cost effective** - Lower infrastructure costs compared to microservices
- **Team size appropriate** - Good fit for small-to-medium teams (5 developers)
- **Future flexibility** - Modular structure allows migration to microservices if needed

**Negative:**

- **Scaling granularity** - Cannot scale individual modules independently (must scale entire backend)
- **Deployment coupling** - All modules deploy together; one change requires full redeployment
- **Technology lock-in** - All backend modules must use Java/Spring Boot
- **Resource sharing** - Heavy modules (analytics) share resources with critical modules (order management)
- **Potential bottlenecks** - Single database can become a bottleneck as system grows
- **Build time** - Full application rebuild required even for small changes

## Alternatives Considered

### Option 1: Traditional Monolithic Architecture (No Module Boundaries)
**Why not chosen:**
- No clear separation of concerns would lead to spaghetti code
- Difficult to maintain as features grow
- Hard to onboard new developers
- No clear ownership of different domains
- Refactoring becomes increasingly difficult over time

### Option 2: Microservices Architecture
**Why not chosen:**
- Overkill for initial team size (5 developers) and expected load
- Significantly higher operational complexity (service discovery, distributed tracing, etc.)
- Network latency between services would impact performance
- Distributed transactions are complex for order management workflows
- Higher infrastructure costs (multiple deployments, service mesh, etc.)
- Longer initial development time to set up infrastructure
- Team lacks extensive distributed systems experience
- Can migrate to this later if needed, keeping modularity enables easier transition

## Related Decisions

- ADR-0002 – Data Model Definition (Sprint 2)
- ADR-0003 – Architecture Update (Sprint 2)

## Notes

- **Module organization:** Backend follows package-by-feature structure (e.g., `com.slms.order`, `com.slms.tracking`, `com.slms.carrier`)
- **Database access:** Each module has its own repository layer; shared database with logical schema separation
- **API design:** RESTful endpoints organized by module (e.g., `/api/orders`, `/api/tracking`, `/api/carriers`)
- **Future migration path:** Clean module boundaries allow extraction to microservices if scaling requirements change
- **Technology stack:** Spring Boot 3.5.6 + Java 21 for backend; Next.js 15 + React 19 for frontends
