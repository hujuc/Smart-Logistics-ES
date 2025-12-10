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

1. [Introduction](#1-introduction)
2. [System Architecture](#2-system-architecture)
3. [Data Model](#3-data-model)
4. [User Requirements](#4-user-requirements)
5. [Developer Onboarding](#5-developer-onboarding)
6. [Production Configuration](#6-production-configuration)
7. [Security & Authentication](#7-security--authentication)
8. [Key Features & Modules](#8-key-features--modules)
9. [AI-Powered Chatbot](#9-ai-powered-chatbot)
10. [Testing & Quality Assurance](#10-testing--quality-assurance)
11. [Observability & Monitoring](#11-observability--monitoring)
12. [Lessons Learned & Future Work](#12-lessons-learned--future-work)
13. [Conclusion](#13-conclusion)

---

# 1. Introduction

## 1.1 Project Context

The exponential growth of e-commerce has transformed logistics into one of the most critical components of business competitiveness. Customers increasingly expect fast, reliable, and transparent deliveries, while companies face rising transportation costs, fragmented carrier networks, and limited visibility across the delivery chain. Traditional logistics management solutions often fail to provide real-time data, carrier performance comparisons, and seamless customer communication. As a result, companies struggle to reduce costs, drivers face inefficient route planning, customer service representatives spend valuable time handling avoidable calls, and customers remain frustrated due to lack of visibility over their orders.

The Smart Logistics Management System (SLMS) addresses these challenges by providing a centralized, intelligent platform that integrates multiple actors in the logistics ecosystem: logistics managers, warehouse staff, carriers, customer service representatives, and end customers. By offering real-time insights, automation, and advanced exception handling, SLMS optimizes logistics operations, increases customer satisfaction, and reduces inefficiencies across the supply chain.

This project was developed as part of the Software Engineering course at the University of Aveiro (DETI) during the 2025/2026 academic year by Group 304, demonstrating the practical application of modern software engineering practices, architecture patterns, and cloud-native technologies in solving real-world business problems.

## 1.2 Objectives & Scope

The primary objective of SLMS is to provide a comprehensive logistics management solution that supports all key roles involved in the shipping and delivery process. The system addresses several core challenges:

- **Real-time monitoring and decision-making:** Enable logistics managers to track key performance indicators (KPIs), compare carrier performance, and generate reports for data-driven decision-making
- **Streamlined warehouse operations:** Support warehouse staff with order registration, automatic label and document generation, and carrier pickup confirmation
- **Improved customer service efficiency:** Provide customer service representatives with real-time access to order status, delivery exceptions, and tools to resolve issues quickly
- **Driver empowerment:** Equip carriers with a mobile Progressive Web Application (PWA) to view delivery manifests, receive optimized routes, and record proof of delivery
- **Customer transparency:** Allow customers to track orders in real time, receive proactive notifications, and report delivery problems directly through a public portal
- **Structured exception management:** Ensure that delivery problems such as failed attempts, incorrect addresses, or damaged packages are logged, assigned, and resolved systematically

The system's scope encompasses 18 user stories addressing 5 distinct user personas (detailed in Section 4), with 12 user stories completed, 5 in progress, and 1 pending. The platform handles the complete order lifecycle from creation through carrier selection, shipment, tracking, exception handling, and final delivery confirmation.

## 1.3 System Overview

SLMS implements a modular monolith architecture designed to balance development velocity, operational simplicity, and long-term maintainability. The system comprises four distinct frontend applications, each optimized for specific user roles through the Backend-for-Frontend (BFF) pattern:

- **Landing Page:** Public-facing application for unauthenticated order tracking and system information
- **Admin Console:** Comprehensive administrative interface serving three internal roles—logistics managers, warehouse staff, and customer service representatives—with role-specific dashboards, order management, carrier integration, exception handling, and reporting capabilities
- **Customer Portal:** Self-service application enabling customers to track their orders, view delivery history, and manage delivery preferences
- **Driver PWA:** Mobile-optimized Progressive Web Application providing drivers with delivery manifests, route optimization, real-time status updates, and proof of delivery capture

These frontends interact with a centralized backend organized into eight business modules: Order Management, Rate & Carrier Integration, Visibility & Tracking, User Management, Notifications, Reporting, Warehouse Management, and AI-Powered Chatbot. Each module encapsulates specific domain logic while maintaining clear boundaries that prevent tight coupling and enable independent evolution (detailed in Section 2).

The technology stack combines mature, production-proven technologies with modern capabilities. The backend leverages **Spring Boot 3.5.0** with **Java 21** for application logic, **Spring Data JPA** with **Hibernate** for object-relational mapping, and **PostgreSQL 16** for relational data storage. All four frontend applications are built with **Next.js 15.5.4** and **React 19.1.0**, using **TypeScript** for type safety and **Tailwind CSS** for styling. The infrastructure employs **Docker** for containerization, **Terraform** for infrastructure as code, and **Nginx** as the reverse proxy and load balancer.

Key external integrations include **Keycloak 26.0** for OAuth 2.0/OpenID Connect authentication and authorization, **ShipEngine API** for multi-carrier integration (FedEx, UPS, DHL), **Google Gemini** for large language model capabilities in the AI chatbot, **Pinecone** for vector database and semantic search, **Elastic APM** for application performance monitoring and distributed tracing, **MinIO** for S3-compatible object storage of shipping documents and proof of delivery photos, **Mapbox** for mapping and routing services in the Driver PWA, and **Flagsmith** for feature flag management. The complete technical stack is detailed in Section 2.3.

## 1.4 Development Approach

The project follows an Agile/Scrum methodology with a five-person development team, each member fulfilling a specific role:

- **Scrum Master (João Roldão, NMEC 113920):** Sprint coordination and backlog management
- **Product Owner (Hugo Castro, NMEC 113889):** Requirement prioritization, user story definition
- **QA Engineer (Hugo Sousa, NMEC 112733):** Testing strategy and quality assurance
- **Service Analyst (Henrique Teixeira, NMEC 114588):** Requirements analysis and feature specification
- **DevOps Master (João Varela, NMEC 113780):** Infrastructure management and CI/CD pipeline implementation

Development proceeded through multiple sprint cycles, with each sprint delivering incremental functionality. The team employed continuous integration and deployment practices, comprehensive documentation, and regular retrospectives to maintain quality and adapt to evolving requirements. The development workflow and collaboration practices are documented in the project handbook.

## 1.5 Report Organization

This report provides comprehensive documentation of the SLMS project, organized to serve different audiences and information needs:

- **Section 2 (System Architecture)** details the architectural decisions, design patterns, and component interactions that structure the system
- **Section 3 (Data Model)** describes the database schema, entity relationships, and data modeling decisions
- **Section 4 (User Requirements)** presents the five user personas, usage scenarios, and 18 user stories that drive system functionality
- **Section 5 (Developer Onboarding)** provides practical guidance for developers setting up local environments and contributing to the codebase
- **Section 6 (Production Configuration)** covers environment variables, external service configuration, build processes, CI/CD pipelines, and deployment procedures
- **Section 7 (Security & Authentication)** explains the OAuth 2.0/OIDC integration with Keycloak, role-based access control, and security patterns
- **Section 8 (Key Features & Modules)** describes the major functional capabilities organized by frontend application
- **Section 9 (AI-Powered Chatbot)** documents the architecture, RAG implementation, and tool-calling system for context-aware assistance
- **Section 10 (Testing & Quality Assurance)** outlines the testing strategy, coverage metrics, and quality assurance processes
- **Section 11 (Observability & Monitoring)** presents the SLI/SLO definitions, monitoring infrastructure, and alerting mechanisms
- **Section 12 (Conclusion)** synthesizes project achievements, technical contributions, academic value, and future evolution opportunities

---

# 2. System Architecture

## 2.1 Architectural Overview

The Smart Logistics Management System (SLMS) employs a modular monolith architecture designed to balance development velocity, operational simplicity, and long-term maintainability. This architectural approach organizes the system into well-defined, loosely coupled modules within a single deployable application, while maintaining clear boundaries between different functional domains.

![SLMS Architecture Diagram](handbook/Architecture_SLMS.png)

**Figure 3.1:** Overall SLMS Architecture showing Frontend, Backend, Persistence Layer, and External Integrations

### Architectural Philosophy

The decision to adopt a modular monolith rather than a microservices architecture stemmed from careful consideration of the project's context: a development team of five engineers building a comprehensive logistics platform under time constraints. While microservices offer attractive benefits such as independent scaling and technology diversity, they introduce significant operational complexity that would have diverted effort from delivering business value. The modular monolith provides a pragmatic middle ground—offering many of the organizational benefits of microservices while avoiding the overhead of distributed systems management.

At its core, the architecture establishes clear module boundaries around business capabilities. Each module owns its domain logic, data access patterns, and internal workflows, communicating with other modules through well-defined interfaces. This separation of concerns ensures that changes within one module—such as modifications to the order management logic—do not ripple unpredictably through the carrier integration or notification systems. The boundaries are enforced through package structure and dependency management, preventing tight coupling between modules while allowing them to collaborate when necessary.

### Modular Monolith Design

The backend application comprises eight distinct business modules, each encapsulating a specific domain within the logistics workflow. The Order Module manages the complete lifecycle of shipment orders from creation through delivery. The Rate Carrier Module handles interactions with shipping carriers and manages the physical shipment entities. The Visibility Module provides real-time tracking and exception management capabilities. Supporting these core logistics functions, the User Management Module integrates with the authentication system, while the Notification Module handles asynchronous communication with users. The Reporting Module generates performance metrics and dashboards for management oversight, and the Chatbot Module is for providing AI-powered assistance to users across all frontends.

This modular organization delivers several key advantages. Each module can evolve independently—teams can refactor the internal implementation of rate calculation without touching order management code. The clear boundaries make the codebase more understandable, as developers can reason about each module's responsibilities without needing to understand the entire system. Testing becomes more focused, as unit and integration tests can target specific modules. The architecture also preserves future flexibility: if operational needs change and independent scaling becomes necessary, well-defined module boundaries provide natural seams for extracting microservices.

### Backend-for-Frontend Pattern

Complementing the modular backend, the system implements the Backend-for-Frontend (BFF) pattern to optimize interactions between the business logic layer and the four distinct frontend applications. Each frontend—the Landing Page, Admin Console, Customer Portal, and Driver PWA—has unique requirements for data structure, aggregation, and security context. Rather than forcing all frontends to consume a single generic API, the BFF pattern provides each frontend with a dedicated backend layer that aggregates and transforms data specifically for that client's needs.

The Admin Console BFF, for instance, serves three different internal user roles (logistics managers, warehouse staff, and customer service representatives) through a unified interface. It combines data from multiple business modules—pulling order details from the Order Module, carrier information from the Rate Carrier Module, and exception data from the Visibility Module—and presents them in aggregated views optimized for administrative dashboards. The Customer Portal BFF, by contrast, focuses exclusively on customer-facing data: order tracking information, delivery status updates, and self-service capabilities. It enforces strict data isolation, ensuring customers can only access their own orders while providing a simplified API surface tailored to the customer experience.

The Driver PWA BFF serves the unique needs of mobile delivery drivers. It provides delivery manifests, route information, and real-time status update capabilities, all optimized for the constraints of mobile networks and intermittent connectivity. The BFF aggregates shipment details, destination addresses, and proof-of-delivery requirements into compact payloads designed for efficient transmission over cellular networks. Finally, the Landing Page BFF handles public-facing endpoints such as order tracking lookups that don't require authentication, implementing appropriate rate limiting and caching strategies.

This pattern delivers significant benefits beyond simple API convenience. Security becomes more robust, as each BFF can enforce role-specific authorization rules and data access controls appropriate to its frontend. The BFFs serve as a security boundary, preventing frontends from directly accessing backend business logic and ensuring all requests flow through a controlled layer that validates permissions. Performance optimizes naturally, as each BFF can tailor its responses—pre-aggregating data that would otherwise require multiple round trips, implementing frontend-specific caching strategies, and minimizing payload sizes for mobile clients. The pattern also isolates frontends from backend complexity, allowing business modules to evolve their internal APIs without breaking frontend applications, as the BFFs can adapt and translate changes.

### Deployment and Scalability

Despite being a monolithic application, the architecture supports operational flexibility through containerization and infrastructure as code. The entire system deploys as a set of Docker containers—one for the Spring Boot backend, four for the Next.js frontend applications, and additional containers for infrastructure services like PostgreSQL, Keycloak, Kafka, and MinIO. This containerized approach ensures consistency across development, testing, and production environments while providing deployment flexibility.

The system scales vertically by allocating additional resources to containers, a straightforward approach that aligns well with the modular monolith's single-process nature. For horizontal scaling, multiple backend instances run behind a load balancer, with session affinity or stateless JWT-based authentication ensuring requests can route to any instance. The database uses connection pooling to manage concurrent access efficiently, while Kafka provides asynchronous processing for operations that don't require immediate completion, such as notification delivery.

The architecture recognizes that different components may have different scaling needs. The frontend applications, being relatively stateless, can scale independently from the backend. Static assets serve through Nginx with appropriate caching headers, reducing load on the application servers. The MinIO object storage scales independently to handle document and image storage growth. This selective scaling capability demonstrates that monolithic architecture doesn't preclude operational sophistication—it simply concentrates scaling decisions on the components that matter most.

### Separation of Concerns

The architecture maintains clear separation between different types of concerns. Business logic resides entirely within the backend modules, isolated from presentation concerns. The modules interact with the database through repository interfaces, abstracting data access patterns and enabling easier testing through mocks. Domain entities remain independent of their persistence mechanism, following clean architecture principles that keep core business rules free from framework dependencies.

Cross-cutting concerns—logging, monitoring, security, and transaction management—are handled through framework-level capabilities rather than being scattered throughout business logic. Spring Boot's autoconfiguration and aspect-oriented programming features provide centralized solutions for these concerns, ensuring consistent application across all modules without duplicating code.

The separation extends to the frontend architecture as well. Each Next.js application maintains its own component library, styling system, and state management, ensuring that changes to the admin interface don't affect the customer portal. Shared utilities and common components are organized in a dedicated shared module, providing code reuse without creating tight coupling between applications.

### Deployment Architecture

The system deploys as containerized services orchestrated through Docker Compose in development and Terraform in production. The infrastructure consists of self-hosted containers for core services—PostgreSQL 16 for data persistence, Keycloak 26.0 for authentication, MinIO for S3-compatible object storage, Apache Kafka for event streaming, and Nginx as reverse proxy—alongside the Spring Boot backend and four Next.js frontend applications. All containers run within a shared Docker network enabling service discovery while maintaining isolation.

External managed services provide specialized capabilities: Shippo for multi-carrier shipping integration, Google Gemini for AI chatbot functionality, Pinecone for vector search, Elastic APM for observability, Mapbox for mapping and routing, and Flagsmith for feature flag management. This hybrid approach balances operational simplicity for core infrastructure with leveraging managed services for complex, specialized functionality.

## 2.2 Logic Architecture

The logic architecture of SLMS organizes functionality into distinct layers, with the business layer at its core containing eight specialized modules that encapsulate domain logic. These modules interact with a presentation layer that implements the Backend-for-Frontend pattern, and a data access layer that manages persistence through PostgreSQL.

### 2.2.1 Backend Modules

The backend comprises eight specialized modules, each encapsulating a distinct domain within the logistics workflow. Modules operate independently while collaborating through well-defined interfaces.

**Order Module:** Manages the complete order lifecycle from creation to delivery. Captures shipment details (package dimensions, destination, special requirements), tracks order status through a state machine (PENDING → READY → ASSIGNED → IN_TRANSIT → DELIVERED), generates shipping documents stored in MinIO, and maintains an immutable audit trail of all order events.

**Rate Carrier Module:** Handles carrier integration and shipment execution through the Shippo API aggregator, supporting FedEx, UPS, and DHL. Provides rate comparison functionality for cost-optimal carrier selection, generates shipping labels with tracking numbers, manages shipment entities containing carrier-specific data, and tracks driver assignments for delivery execution.

**Visibility Module:** Aggregates real-time tracking updates from multiple sources (carrier webhooks, driver app, warehouse staff) into a unified order status view. Manages delivery exception workflow—capturing, categorizing, prioritizing, and routing exceptions to customer service representatives based on severity, workload, and expertise. Integrates with notifications to keep stakeholders informed.

**Chatbot Module:** Provides AI-powered contextual assistance using Google Gemini (LLM), Pinecone (vector database for semantic search), and custom tool calling for real-time database queries. Filters help content by user role, frontend application, current page route, and language preference. Tools enable the AI to answer questions like "What's the status of my order?" with actual database data, enforcing role-based authorization on tool execution.

**User Management Module:** Bridges application logic with Keycloak authentication. Validates JWT tokens on every request, extracting user identity and roles (LOGISTICS_MANAGER, WAREHOUSE_STAFF, CUSTOMER_SERVICE, DRIVER, CUSTOMER) for authorization decisions. Enforces role-based access control across all modules and handles token refresh for seamless user experience.

**Notification Module:** Event-driven communication system built on Kafka. Subscribes to events from other modules (order status changes, exceptions, deliveries) and transforms them into user notifications via email and SMS. Maintains multi-language templates, respects user channel preferences, implements delivery tracking and retry logic, and prioritizes critical alerts.

**Reporting Module:** Transforms operational data into business intelligence for logistics managers. Calculates KPIs (on-time delivery %, average duration, cost per package, carrier performance, exception rates), generates real-time dashboards, enables time-series analysis for pattern identification, and supports custom ad-hoc reports. Uses pre-calculated aggregates and caching to avoid impacting transactional performance.

### 2.2.2 Frontend Applications

SLMS implements four separate Next.js applications rather than a single monolithic frontend. This separation enables each application to optimize for its specific user persona's workflows, security requirements, and device constraints without compromising other user experiences. All frontends share Next.js 15, React 19, and TypeScript but diverge in features and interface complexity.

**Why Four Separate Frontends?** Different user groups have fundamentally different needs that would create complexity if forced into a single application. Internal staff need sophisticated tools on desktop computers; customers need simple tracking on any device; drivers need mobile-optimized interfaces with offline capability. Separate applications allow independent evolution, tailored performance optimization, and appropriate security boundaries.

**Landing Page (Public visitors):** Public entry point providing marketing information and unauthenticated order tracking. Enables anyone with a tracking number to check shipment status without creating an account. Routes authenticated users to appropriate applications based on their role (managers to Admin Console, customers to Customer Portal).

**Admin Console (Logistics managers, warehouse staff, customer service):** Unified internal operations interface serving three roles with role-based UI adaptation. Managers access analytics dashboards and carrier configuration; warehouse staff create orders and generate shipping labels; customer service investigates issues and manages delivery exceptions. Consolidation enables cross-functional visibility while maintaining focused interfaces per role.

**Customer Portal (End customers):** Self-service interface for order tracking, delivery preference management, and issue reporting. Prioritizes simplicity and responsive design for infrequent users across all devices. Implements strict data isolation ensuring customers only access their own orders. Provides real-time tracking updates and direct exception reporting to reduce customer service load.

**Driver PWA (Delivery drivers):** Mobile Progressive Web App for field operations with offline-first design. Displays optimized delivery manifests with turn-by-turn navigation (Mapbox integration), captures proof of delivery (photos, signatures), and reports delivery exceptions. Service workers cache route data for offline access, with automatic sync when connectivity returns. Optimized for mobile constraints: large touch targets, minimal typing, battery efficiency.

### 2.2.3 BFF Pattern Implementation

The Backend-for-Frontend (BFF) pattern provides dedicated API layers tailored to each frontend's specific requirements rather than exposing a single generic API to all clients. Four distinct BFF controllers in the presentation layer align with the four frontend applications.

Each BFF orchestrates calls to multiple business modules, aggregating data into optimized responses that eliminate multiple frontend round trips. For example, an order detail request might combine data from Order, Rate Carrier, Visibility, and Reporting modules into a single response. The BFF also transforms data formats (date conversions, restructuring objects) and handles frontend-specific patterns (pagination, sorting, filtering).

The BFF layer enforces frontend-specific security boundaries. The Customer Portal BFF verifies order ownership before returning data, preventing customers from accessing others' information. The Admin Console BFF implements role-specific endpoint authorization. The Driver PWA BFF validates shipment assignments. Each BFF optimizes for its frontend's constraints: the Driver PWA BFF minimizes payload sizes for mobile bandwidth, while the Admin Console BFF prioritizes comprehensive data over size.

## 2.3 Technical Stack

The SLMS technical stack combines mature, production-proven technologies with selective adoption of modern capabilities where they provide clear value. This section provides a concise overview of the technologies employed across backend, frontend, infrastructure, and external services.

### Backend Technologies

**Spring Boot 3.5.0** serves as the application framework, providing dependency injection, autoconfiguration, and production-ready features. **Java 21** brings modern language features including records, pattern matching, and virtual threads. The Spring ecosystem provides comprehensive capabilities out of the box, reducing the amount of custom infrastructure code required.

**Spring Data JPA** with **Hibernate** handles object-relational mapping and data access. Repository interfaces provide CRUD operations without boilerplate code, while the JPA specification ensures portability if database or ORM needs change. Transaction management happens declaratively through annotations, simplifying consistency management.

**Spring Security** implements OAuth 2.0 resource server capabilities, validating JWT tokens and enforcing method-level authorization. The framework integrates seamlessly with Keycloak, extracting user identity and roles from token claims to enable role-based access control throughout the application.

**PostgreSQL 16** (Alpine-based Docker image) provides the relational database foundation. ACID transactions ensure data consistency, foreign key constraints maintain referential integrity, and JSONB columns handle semi-structured data. The database supports the full application data model including orders, shipments, carriers, exceptions, users, and chatbot documents.

**Apache Kafka 7.6.1** and **Zookeeper** provide event streaming capabilities. Kafka enables asynchronous notification delivery and event-driven architecture patterns, decoupling event publishers from consumers while providing durability and replay capabilities.

**Maven** handles dependency management, build lifecycle, and packaging. The POM file declares dependencies on Spring Boot starters, database drivers, testing frameworks, and external service clients, with Maven resolving transitive dependencies and ensuring consistent versions across the build.

### Frontend Technologies

**Next.js 15.5.4** powers all four frontend applications, providing server-side rendering, static site generation, API routes, and file-based routing. The framework optimizes performance through automatic code splitting and image optimization while simplifying deployment and SEO.

**React 19.1.0** implements the component-based UI architecture. Hooks manage component state and lifecycle, while the virtual DOM efficiently updates the browser DOM. All four frontends share React as their foundation while implementing different features and interfaces.

**TypeScript** adds static typing to JavaScript, catching errors at compile time rather than runtime. Type definitions document component props and API contracts, improve IDE autocomplete and refactoring capabilities, and provide confidence when making changes.

**Tailwind CSS** provides utility-first styling, enabling rapid UI development with consistent design tokens. The approach reduces custom CSS, maintains design system consistency, and produces small production bundles through automatic purging of unused styles.

**NextAuth.js** integrates frontend applications with Keycloak authentication. The library handles OAuth 2.0 flows, session management, and token refresh, providing protected route capabilities and user session hooks.

**Elastic APM RUM 5.17.0** instruments frontend applications for real user monitoring. The library tracks page loads, route changes, API calls, JavaScript errors, and Core Web Vitals (LCP, INP, CLS), sending telemetry to Elastic Cloud for analysis.

**Flagsmith** client libraries enable feature flag integration. Frontend applications fetch flag states at initialization and periodically refresh them, using flags to control feature availability without code deployments.

### Infrastructure Technologies

**Docker** provides containerization for all application and infrastructure components. Containers ensure consistency across development and production environments while enabling isolated execution and efficient resource usage.

**Docker Compose** orchestrates the multi-container development environment, defining services, networks, volumes, and dependencies. A single command starts the entire SLMS stack locally, simplifying developer onboarding and testing.

**Terraform** implements infrastructure as code for production deployments. Declarative configuration describes desired infrastructure state, with Terraform handling provisioning and managing the Docker provider for container deployments.

**Nginx 1.27** (Alpine-based) serves as the reverse proxy and load balancer. Path-based routing directs requests to appropriate frontend or backend containers, while the proxy handles connection pooling, request buffering, and static asset serving.

**GitHub Actions** automates CI/CD workflows. Automated builds compile code, run tests, build Docker images, and deploy to environments, ensuring code quality and enabling rapid iteration.

### External Services

**Keycloak 26.0** provides identity and access management through OAuth 2.0 and OpenID Connect protocols. The service manages user credentials, issues JWT tokens, and provides an admin console for user management.

**MinIO** implements S3-compatible object storage for documents and images. Shipping labels, packing slips, customs declarations, and proof of delivery photos store in MinIO buckets, accessible through standard S3 APIs.

**Shippo API** aggregates carrier integrations, providing unified access to FedEx, UPS, and DHL services. The API supports rate requests, label generation, and tracking webhooks, abstracting carrier-specific details.

**Google Gemini** provides large language model capabilities for the chatbot. The API handles natural language understanding, response generation, and text embedding (768-dimensional vectors) for semantic search.

**Pinecone** offers managed vector database services for semantic search. The database stores document embeddings generated by Gemini, supporting cosine similarity queries to find semantically related content.

**Elastic APM** (managed Elastic Cloud instance) provides application performance monitoring and distributed tracing. Both backend Java agent instrumentation and frontend RUM libraries send telemetry for analysis through Kibana dashboards.

**Mapbox** supplies mapping and routing services. APIs provide address geocoding, route calculation with traffic awareness, and turn-by-turn directions. Frontend JavaScript libraries render interactive maps in the Driver PWA.

**Flagsmith** delivers feature flag management as a service. Applications fetch flag states through REST APIs, enabling dynamic feature control without code deployments. Flags support gradual rollouts, A/B testing, and emergency feature toggles.

This technology stack balances proven stability with modern capabilities, prioritizing technologies with strong ecosystems, comprehensive documentation, and active communities. The stack supports current requirements while maintaining flexibility for future evolution.

---

# 3. Data Model

The SLMS data model is designed to support the complete logistics workflow from order creation through delivery and exception handling. Built on PostgreSQL 16, the database schema implements approximately 10 tables organized into modular business domains.

## 3.1 Database Overview

The system uses a relational database model with PostgreSQL 16 as the primary data store. The schema follows a modular organization aligned with business domains (order management, carrier integration, exception tracking) to maintain clear boundaries and enable independent evolution of each functional area.

**Core Characteristics:**

- **No User Management Table:** All user authentication and management is delegated to Keycloak, an external identity provider. The application database stores only Keycloak user identifiers (UUIDs) where needed to track which users performed actions (e.g., which customer placed an order, which driver is assigned to a shipment).

- **Module-Based Organization:** Tables are grouped into business modules (Order Module, Rate Carrier Module, Visibility Module) that mirror the backend architecture. Modules reference each other through identifier fields rather than database-level foreign key constraints between modules, maintaining independence while enabling necessary relationships.

- **Mixed Primary Key Strategy:** High-volume transactional tables (orders, shipments, carriers) use auto-increment Long IDs for performance. Lower-volume tables requiring global uniqueness (delivery exceptions) use UUID primary keys.

The database currently implements 10 tables supporting the core logistics workflow, with reserved module namespaces for future extensions (route optimization, notification management).

## 3.2 Core Entities

This section describes the main business entities that form the foundation of the SLMS data model. The focus is on their business purpose and role in the logistics workflow.

### Order

The Order entity represents a customer's shipment request - a package that needs to be delivered from a warehouse to a destination address. Each order tracks:
- Unique reference number for customer communication
- Shipping destination (address, contact information)
- Order status progression (CREATED → READY_FOR_PICKUP → ASSIGNED_TO_SHIPMENT → IN_TRANSIT → DELIVERED)
- Package dimensions and weight for rate calculation
- Which warehouse the order originates from
- Which shipment it's assigned to for carrier execution
- Customer who placed the order (Keycloak UUID)
- QR code for driver confirmation workflows
- Proof of delivery photo captured by driver

Orders are the central entity in the system - they represent what customers care about: "Where is my package?"

### OrderEvent

The OrderEvent entity provides an immutable audit trail of order status transitions. Every time an order changes state (e.g., from CREATED to IN_TRANSIT), a new event is recorded with:
- The status the order transitioned to
- Timestamp when the transition occurred
- Optional notes explaining the transition

Events are append-only - they are never updated or deleted. This creates a complete, tamper-proof history of an order's journey through the system, supporting compliance requirements and debugging.

### Warehouse

The Warehouse entity represents physical facilities where orders originate. Each warehouse record includes:
- Unique code and display name
- Physical address
- Whether the warehouse is active and accepting orders
- Integration tokens for third-party carrier systems (Shippo, ShipStation)

SLMS supports multi-warehouse operations, allowing the same system to manage shipments from multiple physical locations.

### Carrier

The Carrier entity represents third-party shipping providers (FedEx, UPS, DHL, etc.) integrated with SLMS. Each carrier record stores:
- Unique code and name
- Contact information (support email, phone, website)
- Integration credentials for ShipEngine and Shippo APIs
- Service level summary (express, standard delivery options)
- Whether the carrier is active and available for selection
- Carrier physical address

Carriers are reference data configured by system administrators to enable multi-carrier shipping options.

### Shipment

The Shipment entity represents the carrier execution of deliveries. When orders are assigned to a carrier and driver for actual delivery, a shipment is created. Each shipment tracks:
- Unique shipment code
- Which carrier company is handling the delivery
- Which driver is assigned (Keycloak UUID)
- External carrier system IDs (Shippo, ShipStation tracking IDs)
- Total number of orders in the shipment
- Aggregate weight and dimensions
- Shipping cost and estimated delivery date
- Transit time estimate

The separation of Order (customer request) from Shipment (carrier execution) is a key architectural decision explained in Section 3.4.1.

### DeliveryException

The DeliveryException entity tracks delivery problems and manages their resolution. Each exception represents a specific issue with an order that requires attention:
- Which order is affected
- Problem classification (DAMAGED, LOST, DELAYED, WRONG_ADDRESS, etc.)
- Severity level (LOW, MEDIUM, HIGH, CRITICAL)
- Who reported the exception (customer, driver, or system)
- Current workflow status (OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED)
- Who is assigned to resolve it (customer service representative)
- Resolution notes and outcome
- SLA deadline and whether it was breached
- Customer feedback rating and comments

Exceptions enable structured problem management with assignment workflows, SLA tracking, and customer feedback loops.

## 3.3 Business Relationships

The relationships between entities reflect the logistics workflow and business rules. This section explains how entities relate from a business logic perspective.

### Order → Warehouse

**Relationship:** Every order originates from exactly one warehouse.

**Business Logic:** When warehouse staff create an order, they specify which physical facility it's shipping from. This relationship enables:
- Tracking which warehouse is responsible for preparing the shipment
- Multi-warehouse operations where different facilities handle different orders
- Warehouse-level performance metrics (orders processed, average processing time)

The warehouse assignment is mandatory when creating an order and remains fixed - orders don't move between warehouses.

### Order → Shipment

**Relationship:** Orders are assigned to shipments for carrier execution. An order starts without a shipment assignment and receives one when assigned to a carrier and driver.

**Business Logic:** This relationship represents the transition from order preparation to active delivery:
- Orders are created and documented (labels generated, package prepared)
- When ready for pickup, warehouse staff assign orders to a shipment
- The shipment groups one or more orders for delivery by a specific carrier and driver
- Orders reference their shipment to track carrier execution

This relationship is nullable - not all orders have shipments (e.g., newly created orders, cancelled orders).

### Shipment → Carrier

**Relationship:** Every shipment is executed by exactly one carrier company.

**Business Logic:** When creating a shipment, staff select which carrier (FedEx, UPS, DHL) will handle the delivery. This relationship:
- Links shipments to carrier configurations (API credentials, service levels)
- Enables carrier-level analytics (performance comparison, cost analysis)
- Determines which external carrier API to use for tracking and label generation

Carrier assignment is mandatory when creating a shipment and typically remains fixed, though shipments could potentially be reassigned if carriers refuse pickup.

### OrderEvent → Order

**Relationship:** Each order has many events tracking its status changes over time.

**Business Logic:** As orders progress through their lifecycle, events are created to document each transition:
- Order created → CREATED event
- Package prepared → READY_FOR_PICKUP event
- Assigned to shipment → ASSIGNED_TO_SHIPMENT event
- Delivered → DELIVERED event

This one-to-many relationship provides complete order history for compliance, debugging, and customer communication ("Your order was picked up at 2pm, out for delivery at 4pm...").

### DeliveryException → Order

**Relationship:** Each exception relates to exactly one order.

**Business Logic:** When delivery problems occur (package damaged, wrong address, failed delivery attempt), exceptions are created and linked to the affected order. This relationship:
- Connects problem reports to specific customer shipments
- Enables order-level exception history ("This order had 2 delivery attempts before success")
- Supports customer service workflows ("What exceptions exist for this customer's orders?")

One order can have multiple exceptions over time (e.g., delayed delivery, then damaged package), creating a one-to-many relationship.

### Cross-Module References

Unlike traditional database designs where all relationships use foreign key constraints, SLMS implements cross-module references through identifier fields. For example:
- Order stores a `shipmentId` number rather than a database-level foreign key to Shipment
- DeliveryException stores an `orderId` number rather than a foreign key to Order

**Business Justification:** This pattern maintains module boundaries and independence. Each business domain (order management, carrier integration, exception tracking) can evolve independently without cascading changes to other modules. While this requires manual relationship traversal in code, it provides architectural flexibility as the system grows.

## 3.4 Key Design Decisions

This section examines the most significant architectural decisions in the data model, focusing on business rationale rather than technical implementation details.

### 3.4.1 Order-Shipment Separation

**Decision:** Model customer orders and carrier shipments as separate entities in different modules.

**Business Rationale:**

The separation between Order and Shipment reflects two distinct business concepts:

- **Order** represents the customer's perspective: "I need this package delivered to this address." Orders are created when customers place shipment requests. They contain customer-facing information (destination address, package contents, delivery instructions) and exist independently of carrier selection.

- **Shipment** represents the carrier's perspective: "We're delivering these packages via this carrier with this driver." Shipments are created when orders are assigned for execution. They contain carrier operational data (which carrier, which driver, what the shipping cost is, external tracking IDs).

**Benefits:**

1. **Customer Independence:** Orders exist before carriers are selected. Customers can create orders, warehouse staff can prepare them, and carrier assignment happens later in the workflow.

2. **Carrier Flexibility:** If a carrier refuses pickup or shipment assignment needs to change, the order data remains intact. Only the shipment relationship changes.

3. **Integration Isolation:** All third-party carrier API integration (ShipEngine, Shippo tracking IDs, carrier-specific metadata) lives in the Shipment entity. This keeps external system coupling separate from core customer order data.

4. **Future Consolidation:** The model supports potential multi-order shipments where one driver delivers multiple customer orders in a single trip, optimizing delivery routes.

This separation aligns with domain-driven design principles: Order and Shipment represent different bounded contexts in the logistics domain.

### 3.4.2 External User Management (Keycloak)

**Decision:** No users table in application database; all user management delegated to Keycloak.

**Business Rationale:**

User authentication and management is a cross-cutting concern that benefits from specialized external systems rather than custom implementation:

**Benefits:**
- **Security:** Keycloak provides enterprise-grade password management, brute force protection, and account security policies
- **Feature Richness:** Immediate access to multi-factor authentication, password reset workflows, email verification
- **Standards Compliance:** OAuth2 and OpenID Connect standard protocols
- **Reduced Maintenance:** Security updates and patches handled by the Keycloak project rather than internal team
- **Single Sign-On:** Potential to integrate SLMS with other organizational systems using shared authentication

**Implementation:**
Application tables store Keycloak user UUIDs where needed:
- Orders reference customers via `customer_id` (Keycloak UUID)
- Shipments reference drivers via `driver_id` (Keycloak UUID)
- Exceptions track reporters and assignees via Keycloak UUIDs

When user details are needed (name, email, role), services query Keycloak's API using the stored UUID.

**Trade-off:**
This introduces a runtime dependency on Keycloak availability. However, the security and feature benefits far outweigh this operational consideration, and Keycloak's reliability is production-proven.

### 3.4.3 Immutable Audit Trail

**Decision:** Order events are append-only - never updated or deleted.

**Business Rationale:**

OrderEvent provides an audit trail of order status transitions. Making this trail immutable serves several business needs:

**Compliance:** Many industries require tamper-proof audit logs showing exactly when orders changed status and who initiated changes. Immutable events provide this guarantee.

**Debugging:** When investigating delivery problems or customer complaints, a complete chronological history is invaluable. "The order was marked delivered at 5pm, but our driver says they couldn't access the building."

**Customer Communication:** Immutable history enables accurate tracking updates to customers without concern that past events might be modified or deleted.

**Data Integrity:** Events represent historical fact - "this order transitioned to IN_TRANSIT at 2pm on Tuesday." Historical facts should not change.

**Implementation:** The system enforces immutability by:
- No update timestamp on events (only creation timestamp)
- Repository layer provides no update or delete methods
- Service layer only creates new events, never modifies existing ones

If a mistake is recorded, the correction is a new event, maintaining full audit trail: "Incorrectly marked DELIVERED at 3pm, corrected to IN_TRANSIT at 3:15pm."

### 3.4.4 Module Independence Through ID References

**Decision:** Business modules reference each other through identifier fields rather than database-level foreign key constraints.

**Business Rationale:**

While traditional database design encourages comprehensive foreign key constraints, SLMS prioritizes module independence for organizational scalability:

**Team Autonomy:** Different teams can own different modules (order management team, carrier integration team, exception handling team) without coordinating database schema changes.

**Independent Evolution:** Each module's schema can evolve independently. Adding fields to Shipment doesn't require coordinating changes to Order module.

**Deployment Flexibility:** Module boundaries create potential for future service separation if scaling requirements demand it.

**Trade-off:**
This pattern requires application code to manually maintain referential integrity (ensuring orders reference valid shipments). However, the organizational benefits - team autonomy and independent evolution - justify this technical trade-off as the system and team grow.

---

# 4. User Requirements

**Responsible: Hugo Castro**

*[10-12 pages with persona images and scenarios]*

## 4.1 Personas

**Reference:** `docs/per-sprint/sprint2/personas-scenarios-US-update/personas-scenarios-US.md`

### 4.1.1 João Ferreira - Logistics Manager

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/joaoFerreira.png`

**Topics to cover:**
- Age: 42
- Profile: Senior manager with operations experience
- Goals: Global operations view, cost reduction, carrier performance evaluation
- Frustrations: Lack of real-time data, manual carrier comparison
- How he uses SLMS: KPI monitoring, strategy optimization, report exports

### 4.1.2 Ana Martins - Warehouse Staff

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/anaMartins.png`

**Topics to cover:**
- Age: 29
- Profile: Warehouse employee, shipping department
- Goals: Quick order processing, correct labels/docs, error reduction
- Frustrations: Manual processes, carrier-specific systems
- How she uses SLMS: Order registration, label generation, pickup updates

### 4.1.3 Rita Lopes - Customer Service Representative

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/ritaLopes.png`

**Topics to cover:**
- Age: 34
- Profile: Call center staff
- Goals: Quick status access, issue resolution, improved CX
- Frustrations: No real-time info, manual carrier contact
- How she uses SLMS: Status checks, exception management, customer feedback

### 4.1.4 Carlos Silva - Driver/Carrier

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/carlosSilva.png`

**Topics to cover:**
- Age: 38
- Profile: Delivery driver (urban/intercity routes)
- Goals: Clear instructions, efficient delivery, easy POD
- Frustrations: Poor route planning, difficult status updates
- How he uses SLMS: Mobile manifest, optimized routes, POD capture

### 4.1.5 Mariana Costa - Customer

**Image:** `docs/per-sprint/sprint2/personas-scenarios-US-update/imgs/marianaCosta.png`

**Topics to cover:**
- Age: 26
- Profile: Frequent online shopper
- Goals: Real-time tracking, delivery transparency, issue reporting
- Frustrations: Lack of visibility, poor communication
- How she uses SLMS: Order tracking, notifications, problem reporting

## 4.2 Usage Scenarios

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

## 4.3 User Stories

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

## 4.4 Feature Prioritization

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

## 4.5 Use Case Diagrams

**Diagrams to include:**
- `docs/per-sprint/sprint2/use-case-diagrams/slms.png` (overall system)
- `docs/per-sprint/sprint2/use-case-diagrams/logistics-manager.png`
- `docs/per-sprint/sprint2/use-case-diagrams/warehouse-staff.png`
- `docs/per-sprint/sprint2/use-case-diagrams/customer-service-representative.png`
- `docs/per-sprint/sprint2/use-case-diagrams/driver-carrier.png`
- `docs/per-sprint/sprint2/use-case-diagrams/customer.png`

---

# 5. Developer Onboarding

**Responsible: Hugo Castro**

*[8-10 pages with step-by-step instructions]*

**Reference:** `docs/handbook/dev-handbook.md`

## 5.1 Prerequisites

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

## 5.2 Initial Environment Setup

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

## 5.3 Project Structure Walkthrough

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

## 5.4 Common Development Commands

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

## 5.5 Development Workflow

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

## 5.6 Testing Strategy

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

## 5.7 Troubleshooting Guide

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

# 6. Production Configuration

**Responsible: João Varela**

Production configuration ensures the Smart Logistics Management System is reproducible across laptops, CI runners, and the AWS EC2 host that runs the live stack. All runtimes are containerized, and configuration is centralized in `.env`/`terraform.tfvars` so that Terraform can build the application images while Docker Compose provisions the supporting services (Keycloak, MinIO, Kafka, Nginx). `infra/deploy.sh` orchestrates both layers, guaranteeing that the same artefacts and health checks are exercised locally and in the self-hosted GitHub Actions runner.

## 6.1 Environment Variables

The project is configuration-first: every service reads settings from environment variables, with sane defaults checked into `src/backend/.env.example` and `infra/.env.example`. CI uses the composite action `.github/actions/setup-slms-env` to generate `.env` and `infra/terraform/terraform.tfvars` from GitHub Secrets, while local developers copy the examples and override only what is necessary.

### 6.1.1 Backend Configuration

The Spring Boot application resolves properties from `application.properties`, which maps directly to env vars injected by Terraform (`docker_container.backend`) or by `docker-compose.dev.yml` in development. The most important groups are:

- **Database:** `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` build `spring.datasource.url` automatically. Terraform sets `SPRING_DATASOURCE_URL` in production to keep the runtime independent from host port mappings.
- **OAuth2/Keycloak:** `KEYCLOAK_HOST`, `KEYCLOAK_PORT`, `KEYCLOAK_REALM`, `KEYCLOAK_ADMIN_CLIENT_ID/SECRET`, `JWT_ISSUER_URI`. The issuer must exactly match `KC_HOSTNAME_URL` to avoid invalid token issuer errors when the stack sits behind Nginx.
- **Object Storage:** `MINIO_HOST`, `MINIO_PORT`, `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `MINIO_BUCKET`. Both `aws.s3.*` properties and the chatbot document storage use these values.
- **Kafka:** `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SECURITY_PROTOCOL`, `KAFKA_CONSUMER_GROUP_ID`, `KAFKA_CONSUMER_AUTO_OFFSET_RESET` (defaults to `earliest` for deterministic consumers).
- **Carrier APIs:** `SHIPPO_API_KEY`, `SHIPENGINE_API_KEY`, `SHIPPO_BASE_URL`, `SHIPENGINE_BASE_URL`. Tests will short-circuit gracefully when the keys are absent.
- **Observability:** `ELASTIC_APM_SERVER_URL`, `ELASTIC_APM_SECRET_TOKEN`, `DEPLOYMENT_ENV`, `PROJECT_VERSION`, `TRACING_SAMPLING_RATE`, and the log levels (`LOG_LEVEL_ROOT`, `LOG_LEVEL_APP`, `LOG_LEVEL_WEB`, `LOG_LEVEL_SECURITY`).
- **AI stack:** `GEMINI_API_KEY`, `GEMINI_MODEL`, `GEMINI_EMBEDDING_MODEL`, `PINECONE_API_KEY`, `PINECONE_HOST`, `PINECONE_INDEX`, `PINECONE_NAMESPACE`.
- **Misc:** `QR_CONFIRMATION_BASE_URL` (used by the Driver PWA QR flow) and `chatbot.seeder` toggles for optional knowledge-base seeding during non-production boots.

A production-oriented `.env` for the backend (outside Docker) looks like:

```properties
DB_HOST=localhost
DB_PORT=5433
DB_NAME=slms
DB_USERNAME=slms
DB_PASSWORD=********

KEYCLOAK_HOST=localhost
KEYCLOAK_PORT=8180
KEYCLOAK_REALM=slms
KEYCLOAK_ADMIN_CLIENT_ID=slms-backend
KEYCLOAK_ADMIN_CLIENT_SECRET=********
JWT_ISSUER_URI=http://<public-host>:8180/realms/slms

MINIO_HOST=localhost
MINIO_PORT=9000
MINIO_ROOT_USER=<access_key>
MINIO_ROOT_PASSWORD=<secret_key>
MINIO_BUCKET=slms-documents

SHIPENGINE_API_KEY=<production_key>
SHIPPO_API_KEY=<production_key>
ELASTIC_APM_SERVER_URL=https://<elastic-host>:443
ELASTIC_APM_SECRET_TOKEN=<token>
DEPLOYMENT_ENV=production
PROJECT_VERSION=0.0.1
TRACING_SAMPLING_RATE=0.2
GEMINI_API_KEY=<google_key>
PINECONE_API_KEY=<pinecone_key>
PINECONE_HOST=https://<index>.svc.<region>.pinecone.io
```

During development `JPA_DDL_AUTO=update` accelerates schema evolution; in production switch to `validate` once a migration tool (Flyway/Liquibase) is introduced to avoid accidental data loss.

### 6.1.2 Infrastructure Configuration

`infra/.env` feeds both Docker Compose and Terraform. Compose uses it to start Keycloak, MinIO, Kafka, and Nginx; Terraform reads the same values through the `env_file` mechanism in `setup-slms-env` so that app containers are built with identical settings. Critical fields:

- **Database + Keycloak:** `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `KC_DB_URL`, `KC_HOSTNAME_URL`, `KC_PROXY=edge`. `KC_HOSTNAME_URL` must be the publicly reachable URL (including port) so that Keycloak signs tokens with the correct issuer.
- **Network/ports:** Override `POSTGRES_PORT` or `KAFKA_*_PORT` if those ports are occupied on the host.
- **Object storage:** `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `MINIO_BUCKET_NAME`.
- **Frontend base URLs (dev only):** `NEXT_PUBLIC_API_URL`, `NEXT_PUBLIC_FLAGSMITH_ENV_KEY`, `NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN`. Production builds bake these through Terraform instead.
- **Backup retention:** `MAX_BACKUPS=4` controls how many dumps the backup image keeps (used by `setup-backup.sh`).

Example fragment:

```bash
POSTGRES_DB=slms
POSTGRES_USER=slms
POSTGRES_PASSWORD=********
KC_HOSTNAME_URL=http://<public-host>:8180
KC_HTTP_ENABLED=true
JWT_ISSUER_URI=http://<public-host>:8180/realms/slms
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=********
MINIO_BUCKET_NAME=slms-documents
NEXT_PUBLIC_API_URL=http://<public-host>/api
MAX_BACKUPS=4
```

### 6.1.3 Frontend Build-Time Variables

Next.js embeds configuration at build time, so correct values must be provided before `npm run build`. Terraform handles this via the `public_host` variable and build args in `infra/terraform/main.tf`, preventing the "localhost in production" problem described in `docs/per-sprint/sprint4/aws-deployment-guide.md`.

- **Admin Console (`/admin`):** `NEXT_PUBLIC_API_URL=http://<host>/api`, `NEXT_PUBLIC_KEYCLOAK_URL=http://<host>:8180`, `NEXT_PUBLIC_KEYCLOAK_REALM=slms`, `NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=slms-admin-console`.
- **Customer Portal (`/customer`):** same API URL, Keycloak realm/client (`slms-customer-portal`), plus `KEYCLOAK_BASE_URL` and `NEXTAUTH_URL=http://<host>/customer` for NextAuth callbacks.
- **Driver PWA (`/driver`):** `NEXT_PUBLIC_API_URL=http://<host>/api/driver`, `NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=slms-driver-pwa`, and `NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN`.
- **Landing Page:** `NEXT_PUBLIC_API_URL=http://<host>/api/landing-page`.
- **Feature flags and RUM:** `NEXT_PUBLIC_FLAGSMITH_ENV_KEY` for the customer portal and `NEXT_PUBLIC_ELASTIC_APM_SERVER_URL/SECRET_TOKEN/DEPLOYMENT_ENV/PROJECT_VERSION/TRACING_SAMPLING_RATE` shared across all apps.

A minimal `infra/terraform/terraform.tfvars` for production hosts:

```hcl
public_host           = "http://ec2-13-37-232-192.eu-west-3.compute.amazonaws.com/"
docker_network_name   = "slms-network"
service_replica_count = 2  # scale to 3+ when CPU is available
next_public_api_url   = "http://ec2-13-37-232-192.eu-west-3.compute.amazonaws.com/api"
flagsmith_env_key     = "<prod_flagsmith_key>"
mapbox_access_token   = "<prod_mapbox_token>"
```

## 6.2 External Services Configuration

External dependencies run as first-class services within the stack to keep development and production behaviour aligned. Keycloak, MinIO, Kafka, and Nginx are provisioned by Docker Compose (`infra/docker-compose.yaml`), while the backend and all frontends consume their endpoints through the shared `slms-network` bridge network.

### 6.2.1 Keycloak Setup

Keycloak 26.0 ships as `slms-keycloak-prod` and imports the preconfigured realm from `infra/keycloak/realm-export.json` on first boot. It uses the Terraform-managed PostgreSQL instance (`slms-postgres-prod`) for persistence, so ensure Terraform applies before or alongside Compose.

1. Configure admin credentials in `infra/.env` (`KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD`) and the public issuer in `KC_HOSTNAME_URL`.
2. Start the stack (`make up` or `cd infra && ./deploy.sh deploy`). Nginx proxies `/auth` and `/realms` to Keycloak on port 80; direct admin access is available at `http://<host>:8180/admin/`.
3. Clients imported with the realm:
   - `slms-backend` (confidential) for the Spring Boot resource server
   - `slms-admin-console` (public) for the Admin Console
   - `slms-customer-portal` (public) for the Customer Portal
   - `slms-driver-pwa` (public) for the Driver PWA
4. Set redirect URIs to the Nginx paths: `http://<host>/admin/*`, `http://<host>/customer/*`, `http://<host>/driver/*`, plus `http://<host>/api/auth/*` for NextAuth.
5. Realm roles mirror the personas: `LOGISTICS_MANAGER`, `WAREHOUSE_STAFF`, `CUSTOMER_SERVICE_REP`, `DRIVER`, `CUSTOMER`. Tokens embed these roles and are validated by Spring Security using `JWT_ISSUER_URI`.
6. Harden production by keeping `KC_PROXY=edge`, enabling TLS termination at Nginx, and rotating the client secret stored in GitHub Secrets or `.env`.

### 6.2.2 MinIO Setup

MinIO provides S3-compatible storage for labels, documents, and proof-of-delivery images. The `slms-minio-prod` container exposes the API on `:9000` and the console on `:9001`. On startup, `slms-minio-create-bucket-prod` ensures the bucket defined by `MINIO_BUCKET_NAME` exists.

- Log into the console (`http://<host>:9001`) with `MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD` to verify the bucket and configure optional retention policies.
- The backend uses the same credentials through `aws.s3.*` properties; no extra secrets file is required.
- For production hardening, place MinIO behind TLS (either via Nginx or an upstream ALB) and restrict console access to trusted IPs.

Health check: `curl -f http://<host>:9000/minio/health/live`.

### 6.2.3 ShipEngine Integration

Carrier integrations rely on API keys injected through the environment and surfaced to the Rate & Carrier Module:

1. Obtain sandbox/production keys from ShipEngine and set `SHIPENGINE_API_KEY` (and `SHIPENGINE_BASE_URL` if using a private endpoint).
2. Terraform propagates the key into the backend containers; the `ShipEngineApiClient` will no-op when no key is present, keeping CI green.
3. To validate end-to-end, start the stack and run `make test-real` (requires the key) or trigger the rate comparison flows in the Admin Console; generated labels and documents are saved to MinIO.
4. Webhooks can target Nginx (`http://<host>/api/...`) once exposed; configure them with secret signatures when moving beyond sandbox.

Reference investigations are documented in `docs/per-sprint/sprint1/carrier-shipping-apis-investigation.md` and `docs/per-sprint/sprint3/carrier-module-readme.md`.

### 6.2.4 AI Services Configuration

The chatbot module combines Google Gemini for reasoning and Pinecone for retrieval:

- Create a Gemini API key and set `GEMINI_API_KEY`, selecting `GEMINI_MODEL=gemini-2.0-flash` and `GEMINI_EMBEDDING_MODEL=text-embedding-004`.
- In Pinecone, create an index `slms-chatbot` with 768 dimensions, cosine metric, and namespace `en-US`; set `PINECONE_API_KEY` and `PINECONE_HOST`.
- The backend reads these values at startup. Keep `chatbot.seeder` disabled in production; enable it temporarily (env var `CHATBOT_SEEDER_ENABLED=true`) when repopulating the index from project documents.

### 6.2.5 Observability Endpoints

Elastic APM is wired into both backend and frontends via `ELASTIC_APM_*` and `NEXT_PUBLIC_ELASTIC_APM_*`. The backend exposes `/actuator/*` and Prometheus metrics through Nginx (`/actuator/`), which feed the SLI/SLOs discussed in Section 11.

## 6.3 Build Process

Builds are deterministic and cache-aware to minimize CI time and avoid "works on my machine" drift.

### 6.3.1 Backend Build

- Local/CI command: `cd src/backend && ./mvnw clean package -DskipTests` (also invoked via `make build`).
- Docker: `infra/docker/backend/Dockerfile` is a two-stage build. The builder stage pre-downloads dependencies (`mvn dependency:go-offline`), builds the jar, and copies only the artifact into a slim JRE 21 image running as a non-root user with G1GC tuning.
- Terraform rebuilds the `slms-backend:latest` image when source changes; force a rebuild with `make rebuild` or `terraform taint docker_image.backend`.

The packaged artifact is `target/slms-backend-0.0.1-SNAPSHOT.jar` for local use and `app.jar` inside the runtime image.

### 6.3.2 Frontend Builds

Each Next.js app uses the shared `infra/docker/frontend/Dockerfile`:

1. Dependencies are installed with `npm ci` in a dedicated `deps` stage to leverage layer caching.
2. Build args inject `NEXT_PUBLIC_*` values; `npm run build` produces a standalone server bundle.
3. `npm prune --omit=dev` strips dev dependencies; the runtime image serves on port 3000 with `node server.js`.

For local hot-reload development, `infra/docker-compose.dev.yml` mounts source folders and runs `npm run dev` with polling enabled (`CHOKIDAR_USEPOLLING`, `WATCHPACK_POLLING`).

### 6.3.3 Developer Workflow

- **Spin up dev stack:** `make dev-up` (Compose only, with Keycloak/MinIO/Kafka/backends/frontends and Nginx proxy).
- **Install dependencies:** `make install` (frontends) and `./mvnw` (backend).
- **Run tests:** `make test-unit`, `make test-backend-integration` (requires supporting containers), `make test-e2e` (Playwright).
- **Production artefacts:** `make build` builds all frontends and backend locally; CI mirrors this in the deploy workflow.

## 6.4 Deployment Pipeline (CI/CD)

Three GitHub Actions workflows cover quality, integration, and production deployment. Concurrency guards prevent overlapping deploys.

### 6.4.1 Quality Gate (`ci-quality.yml`)

Triggered on pull requests to `develop` and `main`:

- Backend: runs unit + mocked integration tests with JaCoCo coverage gates (`make test-ci`), uploads reports, and posts status to the PR.
- Frontend: matrix over all four apps, running `npm ci`, `npm run lint`, and `npm run build` with stubbed `NEXT_PUBLIC_*` values to catch build-time config errors.
- Fails fast if coverage or build steps regress.

### 6.4.2 Integration & E2E (`ci-integration.yml`)

Triggered on pull requests to `main`:

- Uses the `setup-slms-env` action to generate `.env` for containers.
- Spins up dependencies with `docker-compose.dev.yml` + `docker-compose.ci.yml` (PostgreSQL on 55432, Keycloak on 28180, MinIO on 39000, Kafka/Zookeeper).
- Backend integration tests run against real Keycloak/MinIO/Kafka (`make test-backend-integration`).
- Playwright E2E tests run per frontend (`admin-console`, `customer-portal`, `driver-pwa`, `landing-page`) against the locally composed stack; reports are uploaded as artifacts.
- Cleans containers and volumes after each job to avoid state leakage.

### 6.4.3 Continuous Deployment (`cd-deploy.yml`)

Triggered on pushes to `main` or manual dispatch; runs on the self-hosted `aws-runner`:

1. Generates `infra/.env` and `infra/terraform/terraform.tfvars` from GitHub Secrets via `setup-slms-env` (`mode: deploy`), covering database, Keycloak, MinIO, Kafka, observability, and AI keys.
2. Stops and prunes old containers and images to avoid conflicts.
3. Executes `infra/deploy.sh deploy`, which performs a Terraform plan/apply (building backend/frontends, PostgreSQL) and brings up Compose services (`zookeeper`, `kafka`, `keycloak`, `minio`, `nginx`).
4. Restarts Nginx to refresh DNS and validates that backend and Nginx containers are running.
5. Health checks can be extended with `infra/test-health-checks.sh` post-deploy.

Secrets stay within the runner; nothing is committed back to the repository.

## 6.5 Infrastructure as Code (Terraform)

Terraform (provider `kreuzwerker/docker`) manages application containers and PostgreSQL, while Compose handles shared infrastructure. This split keeps stateful services (DB, object storage, auth) explicit and allows stateless services to scale independently.

- **Resources:** `docker_container.postgres` (PostgreSQL 16 with SCRAM auth and volume `slms-postgres-data-prod`), backend replicas, and four frontend replicas defined in `infra/terraform/main.tf`. All containers join the external `slms-network`, which `deploy.sh` creates automatically if missing.
- **Scaling:** `service_replica_count` (default 2) sets replica counts for backend and each frontend. Nginx load balances with `least_conn` (`infra/nginx/default.conf`).
- **Configuration flow:** values come from `terraform.tfvars` or defaults; outputs in `infra/terraform/outputs.tf` expose service URLs for verification.
- **Operations:** `make up` (or `./deploy.sh deploy`) runs a "smart" plan/apply that reuses unchanged containers. Force rebuild with `make rebuild` or `terraform taint docker_image.<name>`. Destroy resources with `make destroy` (`terraform destroy` + `docker compose down`).

## 6.6 AWS Deployment

The live stack runs on a single EC2 instance using the same automation as local builds. Guidance in `docs/per-sprint/sprint4/aws-deployment-guide.md` applies:

1. Provision an EC2 instance (t3.medium or larger, 30GB+ EBS). Open inbound 80/443 (Nginx), 8180 (Keycloak admin), 22 (SSH), and optionally 9000/9001 (MinIO) restricted to trusted IPs.
2. Install Docker, Docker Compose v2, and Terraform 1.5+ on the host.
3. Clone the repository and prepare configuration:
   ```bash
   cd infra
   cp .env.example .env   # fill secrets
   cd terraform
   cat > terraform.tfvars <<EOF
   public_host = "$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)"
   docker_network_name = "slms-network"
   EOF
   ```
4. Deploy with `cd .. && ./deploy.sh deploy` (or `make up` from project root). The script creates the network, builds images, applies Terraform, and brings up Compose services.
5. Validate: `make status`, `curl http://<public-ip>/health`, and optionally run `infra/test-health-checks.sh` to exercise all health endpoints through Nginx.

When the public host changes, rebuild frontends (`./deploy.sh down && ./deploy.sh deploy`) so baked URLs reflect the new hostname.

## 6.7 Database Management

PostgreSQL 16 runs in `slms-postgres-prod` with data persisted to `slms-postgres-data-prod`. Authentication uses SCRAM-SHA-256 (`POSTGRES_HOST_AUTH_METHOD=scram-sha-256`), and other services connect via the Docker network (`slms-postgres-prod:5432`).

### 6.7.1 Backup System

A dedicated backup image (`infra/docker/backup/Dockerfile`) wraps `infra/scripts/backup.sh` to produce compressed dumps and keep only the last `MAX_BACKUPS` files. The helper script `infra/setup-backup.sh` prepares `.env` (adds `MAX_BACKUPS`) and starts the `backup` service when present in the Compose stack.

Typical workflow (run on the server or CI runner):

```bash
# Build/start backup container (ensure compose defines the 'backup' service)
docker compose -f infra/docker-compose.yaml up -d --build backup

# Create a backup with optional label (e.g., pre-deploy SHA)
docker compose -f infra/docker-compose.yaml exec backup BACKUP_LABEL=pre-$SHA /usr/local/bin/backup.sh

# List available backups
docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups

# Download a backup
docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./
```

Restores use `infra/scripts/restore.sh`, which recreates the database and streams the selected dump:

```bash
docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/restore.sh backup_slms_20250101_120000.sql.gz
```

Integrate the backup step before/after deployments on the self-hosted runner to guard against failed rollouts.

### 6.7.2 Migration Strategy

Schema changes are currently applied with `spring.jpa.hibernate.ddl-auto=update` for velocity. For production hardening:

- Switch to `validate` in `.env`/Terraform and manage migrations with Flyway or Liquibase stored in version control.
- Use `make test-backend-integration` to validate migrations against a clean database before releasing.
- Avoid ad-hoc psql changes in production; encode changes in migration scripts and re-run Terraform.

## 6.8 Secrets Management

Secrets never enter source control:

- `.env` and `infra/terraform/terraform.tfvars` are gitignored; `.env.example` and `terraform.tfvars.example` carry non-sensitive defaults only.
- GitHub Actions rely on repository secrets; the `setup-slms-env` action materializes them into files on the runner and cleans the workspace after jobs.
- Rotate API keys regularly (ShipEngine, Gemini, Pinecone, Flagsmith, Mapbox, Keycloak client secrets) and propagate them through the secrets store and `.env` for local testing.
- For cloud deployments, prefer a managed secret store (AWS Secrets Manager/SSM Parameter Store) and mount values via environment variables instead of baking them into images.

## 6.9 Scaling Considerations

Although SLMS is a modular monolith, the stack is prepared for horizontal scale and resilience:

- **Stateless replicas:** Backend and all frontends run two replicas by default (`service_replica_count`), load-balanced by Nginx with `least_conn`. Increase replicas to handle higher traffic; backends remain stateless because JWT validation avoids sticky sessions.
- **Database connections:** PostgreSQL uses connection pooling in the driver; tune via `SPRING_DATASOURCE_*` (HikariCP) if needed when adding replicas. Monitor with Elastic APM metrics.
- **Kafka throughput:** Kafka/Zookeeper are single-instance in this stack; scale up or move to a managed broker if event volume grows.
- **Caching and CDN:** Static assets are served by Next.js/Nginx; introduce CDN caching or Redis-backed caches as a future optimization for rate shopping and reporting.
- **Availability checks:** Nginx and container healthchecks (in Terraform and Compose) ensure unhealthy instances are rotated out automatically. `infra/test-health-checks.sh` provides a quick regression check after scaling changes.

---

# 7. Security & Authentication

**Reference:** `docs/adr/0004-architecture-update-security-&-auth.md`

Security is a critical cornerstone of the Smart Logistics Management System (SLMS), given its role in managing sensitive logistics operations across multiple user types and frontend applications. The system requires robust authentication and authorization mechanisms to ensure that users can only access resources and perform actions appropriate to their roles. This section details the comprehensive security architecture implemented in SLMS, with particular focus on the centralized authentication system powered by Keycloak and the Backend-For-Frontend (BFF) security pattern.

The SLMS security architecture addresses several key challenges inherent in modern multi-tenant logistics platforms:

1. **Multi-Role Access Control**: The system serves five distinct user personas—Logistics Managers, Warehouse Staff, Customer Service Representatives, Drivers, and Customers—each requiring different levels of access and different sets of capabilities.

2. **Multiple Frontend Applications**: With four separate Next.js applications (Landing Page, Admin Console, Customer Portal, and Driver PWA), the system needs unified authentication that works seamlessly across all interfaces while maintaining strict access boundaries.

3. **Centralized Identity Management**: Rather than implementing custom authentication in each frontend or module, SLMS leverages a dedicated Identity and Access Management (IAM) service to provide enterprise-grade security features including Single Sign-On (SSO), comprehensive audit trails, and standardized token-based authentication.

4. **Security at the Edge**: By implementing the BFF pattern, SLMS enforces security at the presentation layer, ensuring that unauthorized requests never reach the business logic layer.

The decision to adopt Keycloak as the authentication provider and implement the BFF pattern represents a deliberate architectural choice documented in ADR-0004. This approach provides a future-proof foundation that supports the system's potential evolution toward a microservices architecture while maintaining strong security guarantees today.

## 7.1 Authentication Architecture

### 7.1.1 Keycloak as Centralized Identity Provider

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

### 7.1.2 User Roles and Permissions

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

### 7.1.3 Authentication Flow

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

## 7.2 Backend-For-Frontend (BFF) Security Pattern

The Backend-For-Frontend (BFF) pattern is a cornerstone of SLMS's security architecture. Rather than having frontend applications communicate directly with business layer services, each frontend has a dedicated BFF controller in the Spring Boot backend that serves as its exclusive API gateway.

### 7.2.1 BFF Architecture Overview

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

### 7.2.2 Role-Based Endpoint Access Control

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

## 7.3 Security Benefits and Trade-offs

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

# 8. Key Features & Modules

**Responsible: Henrique Teixeira**

*[8-10 pages organized by frontend application and user stories]*

## 8.1 Admin Console Features

**User Stories**: US1-US3 (Logistics Manager), US4-US6 (Warehouse Staff), US7-US9 (Customer Service Representative)

### 8.1.1 Logistics Manager Dashboard (US1-US3)

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

### 8.1.2 Warehouse Operations (US4-US6)

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

### 8.1.3 Customer Service Tools (US7-US9)

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

## 8.2 Driver PWA Features

**User Stories**: US10-US13 (Driver/Carrier)

### 8.2.1 Delivery Manifest View (US10)

**Topics to cover:**
- List of deliveries for the day
- Route sequence
- Order details per delivery
- Customer addresses
- Mobile-optimized interface

### 8.2.2 Route Optimization (US11)

**Topics to cover:**
- Route planning support
- Route-based driver assignment
- Optimized delivery sequence
- Navigation integration

### 8.2.3 Proof of Delivery Capture (US12)

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

### 8.2.4 Exception Reporting (US13)

**Topics to cover:**
- Driver-initiated exception reporting
- Automatic exception detection
- Failed delivery documentation
- Issue type selection
- Photo evidence upload

**Backend modules**: Route, Order, Visibility

## 8.3 Customer Portal Features

**User Stories**: US14-US15, US17-US18 (Customer)

### 8.3.1 Account Management (US14-US15)

**Topics to cover:**
- Account creation (US14)
- Secure login (US15)
- Profile management
- Delivery preferences
- Notification preferences

### 8.3.2 Order Tracking (US17)

**Topics to cover:**
- Real-time order tracking
- Tracking by order number
- Delivery status updates
- Delivery notifications
- Estimated delivery time
- Order history

### 8.3.3 Issue Reporting (US18)

**Topics to cover:**
- Customer-initiated issue reporting
- Delivery problem documentation
- Communication with customer service
- Issue status tracking
- Resolution feedback

**Backend modules**: User Management, Order, Visibility

## 8.4 Shared Backend Services

### 8.4.1 Carrier Integration & Rate Management

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

### 8.4.2 Notification System

**Topics to cover:**
- Email and SMS notifications
- Notification templates
- Event-driven alerts
- Scheduled notifications

**Backend module**: Notification

---

# 9. AI-Powered Chatbot

**Reference:** `docs/handbook/chatbot-module.md`, `docs/adr/0007-chatbot-module-integration.md`

The Smart Logistics Management System incorporates an AI-powered chatbot assistant that provides context-aware, role-specific help to users across all frontend applications. Unlike traditional static documentation, the SLMS chatbot understands the user's role, application context, and current page to deliver highly relevant, personalized assistance.

The chatbot addresses several critical challenges:

- **Role-Specific Guidance**: Different roles (Logistics Managers, Warehouse Staff, CSRs, Drivers, Customers) require completely different help content
- **Contextual Relevance**: Help must relate to the user's current page and task
- **Multi-Frontend Consistency**: Four separate frontend applications need consistent yet application-specific guidance
- **Multilingual Support**: English and Portuguese content with cultural appropriateness
- **Conversational Interaction**: Natural language questions with direct, contextual answers

The implementation follows ADR-0007, combining Google Gemini's large language model with Pinecone's vector database to create a Retrieval-Augmented Generation (RAG) system. This architecture grounds responses in actual SLMS documentation while maintaining conversational fluency. The Landing Page chatbot is publicly accessible, while authenticated users receive role-filtered assistance based on Keycloak JWT tokens, maintaining the principle of least privilege.

## 9.1 Chatbot Architecture

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

## 9.2 RAG Implementation

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

## 9.3 Security and Access Control

The chatbot integrates with SLMS's security model through role-based access control. Following the Backend-For-Frontend (BFF) pattern described in Section 7, each frontend has a dedicated chat BFF controller that extracts user context from JWT tokens and enforces access rules.

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

## 9.4 Tool Calling System for Database Access

While the RAG system provides contextual help based on static documentation, the tool calling system enables the chatbot to answer questions requiring real-time database queries. This capability transforms the chatbot from a documentation assistant into an intelligent data retrieval interface that can look up specific orders, check warehouse inventory, track shipments, and query delivery exceptions—all within the user's security context.

### 9.4.1 Tool Interface and Implementation

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

### 9.4.2 Implemented Tools and Capabilities

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

### 9.4.3 Tool Execution Workflow

The tool calling workflow integrates seamlessly with the RAG query flow described in Section 9.2:

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

### 9.4.4 Multi-Iteration Tool Calling

The chatbot supports multi-turn tool usage within a single user question, enabling complex queries like:

**User**: "Show me my most recent order and check if there are any delivery issues"

**Iteration 1**: LLM calls `order_lookup` (with customer ID filter) → Returns Order #12345
**Iteration 2**: LLM calls `delivery_exception_tool` (with orderId: 12345) → Returns no exceptions
**Final Response**: "Your most recent order is #12345, currently IN_TRANSIT. There are no delivery issues reported."

This multi-iteration capability is controlled by `MAX_TOOL_ITERATIONS = 3` in ChatbotService, preventing infinite loops while allowing sophisticated query patterns. Each iteration is logged with timing and authorization context for debugging and security auditing.

## 9.5 Configuration and Deployment

The chatbot requires Gemini API keys (for LLM and embeddings) and Pinecone credentials configured via environment variables. Before deployment, a Pinecone index must be created with 768 dimensions and cosine similarity metric. API keys should be managed securely through secret management systems and never committed to version control. The DocumentSeederService can automatically populate initial help content when enabled (chatbot.seeder.enabled=true), though this should be disabled in production after initial deployment to prevent duplicate content creation.

Both Gemini and Pinecone are usage-based services requiring cost monitoring. Gemini charges per million tokens, while Pinecone charges for index hosting and query volume. The chatbot integrates with Spring Boot Actuator health checks for monitoring API availability and alerting on service degradation.

---

# 10. Testing & Quality Assurance

**Responsible: Hugo Sousa**

*[5-6 pages with testing approach]*

**Reference:** `docs/per-sprint/sprint2/qa-manual-update/qa-manual.md`

## 10.1 Testing Strategy

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

## 10.2 Test Coverage

### 10.2.1 Backend Testing

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

### 10.2.2 Frontend Testing

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

### 10.2.3 Integration Testing

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

### 10.2.4 E2E Testing

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

### 10.2.5 Real Integration Tests

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

## 10.3 Coverage Requirements

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

## 10.4 QA Manual

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

## 10.5 CI/CD Validation

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

# 11. Observability & Monitoring

**Responsible: Henrique Teixeira**

*[6-8 pages with observability strategy]*

**Reference:** `docs/handbook/observability.md`, `docs/per-sprint/sprint4/observability.md`, `docs/adr/0005-observability-monitoring-strategy.md`

## 11.1 Service Level Indicators (SLIs)

**Topics to cover:**

| SLI | Definition | Target SLO | Measurement Window |
|-----|-----------|------------|-------------------|
| **Availability** | % of successful requests | ≥99.5% | 30 days |
| **Latency (p95)** | 95th percentile duration | <500ms | 24 hours |
| **Latency (p99)** | 99th percentile duration | <1500ms | 24 hours |
| **Error Rate** | % of failed requests | <1% | 24 hours |
| **Data Integrity** | % successful DB ops | ≥99.99% | 24 hours |

## 11.2 Service Level Objectives (SLOs)

**Topics to cover:**

**Detailed targets per endpoint type:**
- Read endpoints: <200ms p95
- Write endpoints: <500ms p95
- Complex queries: <1000ms p95

**Error Budget:**
- Calculation methodology
- Budget consumption tracking
- Response to budget exhaustion

## 11.3 Monitoring Stack

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

## 11.4 Alerting

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

# 12. Conclusion

## 12.1 Project Achievements

The Smart Logistics Management System (SLMS) represents a comprehensive solution to the challenges faced by modern logistics operations in an increasingly competitive e-commerce landscape. The platform successfully delivers a complete order-to-delivery lifecycle management system that serves five distinct user personas through four specialized frontend applications, supported by a modular backend architecture and extensive external service integrations.

From a functional perspective, the system addresses the core requirements established at project inception. The platform supports **18 user stories** spanning the needs of logistics managers, warehouse staff, customer service representatives, carriers, and end customers. These user stories translate into concrete capabilities: logistics managers can compare carrier performance metrics and generate operational reports; warehouse staff can register orders and generate shipping documentation with automated label creation; customer service representatives can track orders in real time and resolve delivery exceptions systematically; drivers receive optimized delivery manifests through a mobile Progressive Web Application with proof-of-delivery capture; and customers can track their shipments, view delivery history, and report problems through a self-service portal.

The **multi-carrier integration** through ShipEngine API enables the system to compare rates and transit times across FedEx, UPS, and DHL, providing logistics managers with data-driven decision-making capabilities. The **AI-powered chatbot** delivers context-aware assistance across all frontend applications, combining large language model capabilities from Google Gemini with semantic search through Pinecone's vector database. The chatbot understands user roles, current application context, and navigation state to provide relevant answers from the comprehensive documentation repository, while also executing database queries through a structured tool-calling system when users need real-time operational data.

The **exception handling system** ensures that delivery problems—failed delivery attempts, incorrect addresses, damaged packages—are captured, assigned to appropriate personnel, and tracked through resolution. Real-time tracking capabilities provide visibility into shipment status from carrier acceptance through final delivery, with proactive notifications keeping all stakeholders informed of status changes and potential issues.

From a technical delivery perspective, the project encompasses a **modular monolith backend** organized into eight distinct business modules (Order Management, Rate & Carrier Integration, Visibility & Tracking, User Management, Notifications, Reporting, Warehouse Management, and AI Chatbot), four Next.js frontend applications implementing the Backend-for-Frontend pattern, a complete PostgreSQL database schema with comprehensive entity relationships, and full infrastructure as code definitions using Terraform for deployment across development, staging, and production environments.

The **CI/CD pipeline** implemented through GitHub Actions automates testing, building, and deployment processes, ensuring code quality through automated test execution and enforcing quality gates before changes reach production. The **observability infrastructure** based on Elastic APM provides distributed tracing, application performance monitoring, and service level indicator tracking aligned with defined SLOs for system availability and performance.

From a process perspective, the development followed Agile/Scrum methodology across multiple sprint cycles, with each sprint delivering incremental functionality. The team maintained comprehensive documentation throughout development, producing **32+ markdown files** including Architecture Decision Records (ADRs) capturing major technical decisions, a living developer handbook covering environment setup and development workflows, sprint-specific documentation tracking feature delivery and technical evolution, and endpoint listings documenting the API surface for each BFF layer.

## 12.2 Technical Contributions

The SLMS project demonstrates the application of modern software architecture patterns and engineering practices in solving real-world logistics challenges. The technical approach balances pragmatic decision-making with adherence to established patterns and best practices.

### Architectural Approach

The **modular monolith architecture** provides a structured approach to managing complexity while maintaining operational simplicity. Unlike a traditional monolithic application where boundaries between functional areas blur over time, SLMS enforces clear module boundaries through package structure and dependency management. Each of the eight backend modules encapsulates specific domain logic—the Order Module owns order lifecycle management, the Rate Carrier Module handles carrier interactions and shipment entities, the Visibility Module manages tracking and exception workflows—while maintaining well-defined interfaces for inter-module communication.

This architectural approach delivers practical benefits that proved valuable throughout development. Teams could work on different modules with minimal coordination overhead, as changes to order processing logic did not require modifications to carrier integration code. Debugging became more straightforward, as issues could be isolated to specific modules based on their functional responsibilities. The single-process deployment model simplified operational management compared to a microservices architecture, avoiding the complexity of distributed systems coordination, service discovery, and inter-service communication reliability.

The architecture preserves future flexibility through its clear module boundaries. Should operational requirements change—for example, if the AI chatbot module needs independent scaling to handle varying query loads—the well-defined interfaces provide natural extraction points for migrating to a distributed architecture without requiring wholesale system redesign.

### Backend-for-Frontend Pattern

The **BFF pattern implementation** addresses the diverse needs of four distinct frontend applications without forcing a one-size-fits-all API design. Each BFF layer—serving the Admin Console, Customer Portal, Driver PWA, and Landing Page respectively—aggregates data from multiple backend modules and transforms it into structures optimized for its specific frontend.

The Admin Console BFF serves as an example of this specialization. It supports three internal user roles (logistics managers, warehouse staff, customer service representatives) through role-specific dashboards that aggregate data from Order, Carrier, and Visibility modules. Rather than requiring the frontend to make multiple API calls and assemble this data client-side, the BFF pre-aggregates related information—combining order details, carrier performance metrics, and exception data—into cohesive response structures tailored to administrative workflows.

The Customer Portal BFF, by contrast, focuses exclusively on customer-facing data access with strict isolation boundaries. It enforces that customers can only access their own orders, provides simplified tracking information without exposing internal operational details, and optimizes payload structures for the limited set of customer self-service operations. The Driver PWA BFF addresses mobile-specific constraints by delivering compact payloads suitable for cellular network transmission, organizing delivery manifests for efficient offline access, and providing specialized endpoints for proof-of-delivery photo uploads and status updates from delivery routes.

This pattern also establishes security boundaries. Rather than allowing frontends to directly access backend business logic, all requests flow through BFF layers that validate authentication tokens, enforce role-based authorization rules, and ensure data access complies with security policies. The BFFs prevent unauthorized access attempts and provide consistent security enforcement across all client applications.

### Authentication and Authorization

The integration with **Keycloak** for OAuth 2.0 and OpenID Connect provides enterprise-grade authentication and authorization capabilities. The system implements role-based access control with five distinct roles (Logistics Manager, Warehouse Staff, Customer Service Representative, Driver, Customer) mapped to appropriate permissions within each frontend application.

The authentication flow uses standard OAuth 2.0 authorization code flow with PKCE (Proof Key for Code Exchange) for secure token exchange. Each frontend application represents a separate Keycloak client with appropriate redirect URIs and access policies. The BFF layers validate JWT access tokens on every request, extracting user roles and identity information to enforce authorization rules. This approach ensures centralized identity management while allowing fine-grained permission control within the application layer.

### External Service Integration

The system integrates multiple external services to deliver comprehensive functionality:

**ShipEngine API integration** enables multi-carrier rate comparison and shipping label generation. The Rate Carrier Module abstracts carrier-specific details behind a unified interface, allowing the system to compare rates across FedEx, UPS, and DHL through a single API surface. Error handling accommodates API rate limits, retry logic manages transient failures, and comprehensive test coverage includes both mocked responses for unit tests and optional real API integration tests for validation.

**AI capabilities** combine Google Gemini for large language model processing with Pinecone vector database for semantic document search. The Chatbot Module implements a Retrieval-Augmented Generation (RAG) architecture that grounds AI responses in the project's documentation corpus. When users ask questions, the system generates embeddings of the query, searches Pinecone for semantically similar documentation chunks, and provides these as context to Gemini for response generation. The tool-calling system extends the chatbot beyond pure documentation lookup, allowing it to execute structured database queries when users request real-time operational data.

**Object storage** through MinIO provides S3-compatible storage for shipping documents and proof-of-delivery photos. The Warehouse Management Module generates shipping labels and customs documentation, storing these as objects retrievable through signed URLs. The Driver PWA uploads proof-of-delivery photos captured during delivery completion, associating these with specific shipment records for audit trails and dispute resolution.

**Observability** through Elastic APM provides distributed tracing across the application stack. The system instruments both backend services and frontend applications, correlating user interactions in the browser with corresponding backend API calls and database queries. This end-to-end visibility enables performance analysis and troubleshooting of complex interactions spanning multiple system components.

### Quality Practices

The testing approach follows the test pyramid model with comprehensive unit testing forming the foundation, integration tests validating module interactions and external service integrations, and end-to-end tests covering critical user workflows. The project targets **70% code coverage** measured through JaCoCo for backend code, with unit tests using JUnit 5 and Mockito for dependency isolation.

Integration tests employ Testcontainers for database testing, allowing tests to run against real PostgreSQL instances in isolated containers rather than relying on in-memory databases that may not fully reflect production behavior. External API integrations include both mocked tests for CI/CD pipelines and optional real integration tests tagged separately for validation against actual external services.

The **CI/CD pipeline** enforces quality gates through automated test execution on every pull request, preventing merges when tests fail or coverage falls below thresholds. The build process includes linting, type checking for TypeScript frontends, and security vulnerability scanning of dependencies.

### Documentation Approach

The project employs **Architecture Decision Records (ADRs)** to document significant technical decisions with their context, options considered, and consequences. Eight ADRs capture decisions ranging from the initial modular monolith architecture selection through database integration strategy, security implementation, observability approach, and chatbot module design. This creates a decision log that helps current and future team members understand not just what was built, but why particular approaches were chosen over alternatives.

The **developer handbook** provides practical onboarding guidance covering environment prerequisites, initial setup procedures, project structure walkthrough, common development commands, testing strategy, and troubleshooting guides. This living documentation evolved throughout the project as the team encountered and resolved setup issues, capturing institutional knowledge that reduces onboarding friction for new contributors.

## 12.5 Final Remarks

The Smart Logistics Management System project successfully demonstrates the application of modern software engineering practices to solve real-world logistics challenges. The delivered platform provides comprehensive functionality serving five distinct user personas through specialized frontend applications, supported by a well-architected backend system with extensive external service integrations.

The development process followed Agile/Scrum methodology with iterative delivery, comprehensive documentation, and systematic quality practices. The technical implementation employs established architectural patterns—modular monolith organization, Backend-for-Frontend design, OAuth 2.0/OIDC authentication—supported by industry-standard technologies. The extensive documentation, including Architecture Decision Records, developer handbook, and sprint artifacts, ensures knowledge preservation and facilitates future maintenance and enhancement.

From an academic perspective, the project provided practical application of software engineering principles including requirements engineering, architectural design, iterative development, and software quality assurance. The hands-on experience with modern technologies—Spring Boot, Next.js, Terraform, Docker, CI/CD—develops professionally relevant technical skills. The challenges encountered and addressed throughout development—from modular boundary definition to external API integration to security implementation—provided valuable learning experiences beyond theoretical coursework.

The team's collaborative effort throughout the development process contributed to the project's success. Effective use of version control, code review practices, documentation discipline, and clear communication enabled coordination across multiple concurrent workstreams. The defined roles—Scrum Master, Product Owner, QA Engineer, Service Analyst, DevOps Master—provided clear responsibilities while maintaining collaborative ownership of the overall system quality and delivery.

The SLMS platform represents a complete, functional logistics management system ready for deployment in real-world e-commerce operations, demonstrating that rigorous software engineering practices and modern architectural patterns can successfully address complex business challenges.
