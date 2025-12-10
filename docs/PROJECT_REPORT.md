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

![João Ferreira](per-sprint/sprint2/personas-scenarios-US-update/imgs/joaoFerreira.png)

João Ferreira represents the strategic management layer within the SLMS ecosystem. At 42 years of age, João occupies a senior managerial position in a retail company, bringing extensive professional experience in operations and transportation logistics to his role. His expertise encompasses the full spectrum of supply chain management, from tactical operational decisions to strategic planning and optimization.

As a Logistics Manager, João's primary professional objectives center on achieving comprehensive operational visibility and continuous improvement of logistics performance. He seeks to obtain a global view of all transportation operations under his purview, enabling him to identify patterns, trends, and opportunities for optimization. Cost reduction represents a critical mandate, with particular emphasis on decreasing shipping expenses while simultaneously increasing on-time delivery rates. Furthermore, João requires robust mechanisms for evaluating carrier performance across multiple dimensions, including reliability, cost-effectiveness, and service quality, to inform strategic vendor management decisions.

Despite his experience and expertise, João faces significant challenges inherent to traditional logistics management systems. The absence of real-time operational data severely constrains his ability to make timely, data-driven decisions and respond proactively to emerging issues. Additionally, the manual nature of carrier comparison processes consumes valuable time and introduces potential for human error, preventing efficient optimization of shipping strategies. These frustrations highlight the critical need for integrated, automated systems that consolidate logistics data and provide actionable intelligence.

Within the SLMS, João leverages the platform's comprehensive monitoring and reporting capabilities to address these challenges systematically. He routinely monitors key performance indicators (KPIs) through the system's dashboard, tracking metrics such as shipping costs, delivery performance rates, and carrier reliability. The platform enables him to implement and refine shipping optimization strategies based on empirical data, identifying underperforming carriers and reallocating shipments to more efficient alternatives. Furthermore, João utilizes the system's reporting functionality to export detailed performance analyses, facilitating communication with executive leadership and supporting strategic decision-making at the organizational level. Through these capabilities, SLMS transforms João's role from reactive problem-solving to proactive strategic management of logistics operations.

### 5.1.2 Ana Martins - Warehouse Staff

![Ana Martins](per-sprint/sprint2/personas-scenarios-US-update/imgs/anaMartins.png)

Ana Martins embodies the operational core of the logistics workflow, representing the warehouse personnel responsible for the critical transition of orders from internal processing to carrier dispatch. At 29 years of age, Ana works as a warehouse employee within the shipping department, where she handles the day-to-day operations that directly impact delivery timelines and customer satisfaction.

Ana's professional responsibilities require precision, efficiency, and attention to detail across multiple operational domains. Her primary objectives include processing orders with speed and accuracy to maintain throughput and meet service level agreements. She must ensure that each shipment is accompanied by correct labels and comprehensive documentation, as errors at this stage can cascade into significant downstream issues including delivery failures, customer complaints, and financial penalties. Additionally, Ana is committed to continuously reducing errors in the shipping process through systematic procedures and quality controls.

The nature of Ana's work presents several persistent operational challenges that impede efficiency and increase cognitive load. Manual processes consume considerable time and introduce opportunities for human error, particularly during high-volume periods when warehouse activity intensifies. Furthermore, the necessity of managing multiple carrier-specific systems, each with distinct interfaces, requirements, and workflows, creates operational complexity and increases the learning curve for warehouse staff. This fragmentation prevents the development of streamlined, standardized procedures that could enhance both speed and accuracy.

Within the SLMS environment, Ana benefits from integrated workflows that consolidate previously disparate tasks into a unified operational interface. The platform's automated label and shipping document generation capabilities eliminate manual data entry and reduce preparation time significantly, enabling Ana to efficiently process orders that have been created in the system. When carriers arrive for pickup, Ana efficiently updates the system to reflect the collection status, ensuring real-time visibility for all stakeholders. Through these functionalities, SLMS transforms Ana's role from managing multiple disconnected systems to executing a streamlined, error-resistant workflow that enhances both productivity and job satisfaction.

### 5.1.3 Rita Lopes - Customer Service Representative

![Rita Lopes](per-sprint/sprint2/personas-scenarios-US-update/imgs/ritaLopes.png)

Rita Lopes serves as the critical interface between the organization and its customers, representing the customer service layer responsible for managing inquiries, resolving issues, and maintaining customer satisfaction throughout the delivery lifecycle. At 34 years of age, Rita functions as a call center staff member who fields customer calls and emails concerning delivery status, exceptions, and concerns.

Rita's professional mandate centers on delivering responsive, accurate customer service while efficiently managing a high volume of interactions. Her primary objectives include obtaining rapid access to comprehensive order status information to minimize customer wait times and provide immediate answers. She is tasked with resolving complex delivery issues such as delays, failed delivery attempts, and address discrepancies through effective problem-solving and coordination with logistics operations. Ultimately, Rita strives to continuously improve the overall customer service experience by anticipating needs, providing proactive communication, and ensuring consistent service quality across all customer touchpoints.

The execution of Rita's responsibilities is significantly hampered by information asymmetries and system limitations inherent to traditional logistics platforms. The absence of real-time information forces Rita to provide customers with outdated or incomplete status updates, undermining confidence and requiring follow-up interactions. Moreover, the necessity of manually contacting carriers to obtain current shipment information introduces substantial delays, frustration for both Rita and customers, and potential for miscommunication. These constraints prevent Rita from delivering the responsive, informed service that modern customers expect.

Through the SLMS platform, Rita gains immediate access to comprehensive order visibility and exception management tools that transform her service delivery capabilities. She performs instant status checks by querying order tracking numbers, accessing real-time location data, delivery progress, and detailed event histories. When delivery exceptions occur, Rita leverages the system's exception management module to review categorized issues, understand root causes, and coordinate resolutions with appropriate stakeholders. The platform enables her to provide immediate, accurate feedback to customers based on current system data rather than delayed carrier communications. Additionally, Rita records all customer interactions, resolution actions, and follow-up requirements directly in SLMS, ensuring continuity across service interactions and enabling data-driven quality improvements. These capabilities empower Rita to deliver superior customer service while reducing resolution times and improving first-call resolution rates.

### 5.1.4 Carlos Silva - Driver/Carrier

![Carlos Silva](per-sprint/sprint2/personas-scenarios-US-update/imgs/carlosSilva.png)

Carlos Silva represents the mobile workforce responsible for the final and most customer-facing stage of the logistics chain: last-mile delivery. At 38 years of age, Carlos works as a carrier driver managing both urban and intercity delivery routes, directly interfacing with end customers and serving as the tangible representation of service quality.

Carlos's operational objectives emphasize efficiency, accuracy, and effective communication throughout the delivery process. He requires clear, unambiguous delivery instructions and route guidance to minimize confusion and prevent failed delivery attempts. Delivering packages quickly and efficiently is paramount, as driver productivity directly impacts both operational costs and customer satisfaction metrics. Furthermore, Carlos needs streamlined mechanisms for recording proof of delivery (photographic evidence) to validate successful completions and protect against disputed deliveries.

The challenges Carlos encounters in traditional delivery operations significantly impede his effectiveness and job satisfaction. Poorly planned routes result in wasted time, unnecessary mileage, increased fuel consumption, and reduced daily delivery capacity. The difficulty of updating delivery status in real-time while mobile creates information lag, preventing timely visibility for customers and internal stakeholders. Additionally, cumbersome proof-of-delivery collection processes slow down the delivery workflow and create potential for incomplete documentation.

The SLMS mobile application provides Carlos with purpose-built tools designed specifically for the demands of mobile delivery operations. He accesses his complete delivery manifest through the mobile app, viewing all assigned packages with destination details, delivery priorities, and special instructions in a clear, organized interface. Upon completing each delivery, Carlos efficiently captures proof of delivery through integrated mobile functionality (photograph capture), which are immediately synchronized to the central system. When delivery attempts fail, Carlos reports exceptions directly through the app, selecting from predefined reason codes and adding contextual notes, ensuring that customer service teams receive immediate notification. Through these mobile-optimized capabilities, SLMS enhances Carlos's productivity, reduces administrative burden, and improves the accuracy and timeliness of delivery operations.

### 5.1.5 Mariana Costa - Customer

![Mariana Costa](per-sprint/sprint2/personas-scenarios-US-update/imgs/marianaCosta.png)

Mariana Costa represents the end consumer perspective within the SLMS ecosystem, embodying the expectations and requirements of modern e-commerce customers. At 26 years of age, Mariana is a frequent online shopper whose purchasing behavior reflects broader market trends toward digital commerce and heightened expectations for delivery transparency and reliability.

Mariana's primary objectives as a customer center on visibility, reliability, control, and autonomy throughout the order fulfillment process. She seeks the ability to independently create and manage her own orders without requiring intermediary assistance, exercising direct control over her logistics needs. She seeks comprehensive real-time tracking capabilities that provide continuous visibility into her order's location and status from dispatch through final delivery. Receiving orders within the promised timeframe is essential, as delivery reliability directly influences her satisfaction and likelihood of repeat purchases. Additionally, Mariana expects to receive proactive, automatic notifications regarding any changes, delays, or exceptions that may affect her delivery, enabling her to plan accordingly and maintain confidence in the service.

Traditional e-commerce logistics experiences present significant pain points that undermine Mariana's confidence and satisfaction. The lack of transparency in package tracking creates uncertainty and anxiety, particularly for time-sensitive or valuable purchases. When issues arise, the burden of initiating contact with the retailer or carrier to obtain information or resolve problems represents an unacceptable friction point in the customer experience. These frustrations reflect a fundamental misalignment between customer expectations shaped by leading e-commerce platforms and the capabilities of traditional logistics systems.

The SLMS customer portal empowers Mariana with self-service capabilities and proactive communication that address these pain points comprehensively. She can create new orders directly through the customer portal, entering all necessary shipment details including destination addresses and warehouse selection, with the system automatically associating orders with her customer account for security and tracking purposes. She accesses the public tracking portal where she is able to see her full order history, where she can search for a specific order by entering its unique tracking number, immediately viewing her order's current location, transit history, and estimated delivery date without requiring assistance. The system delivers automatic notifications at critical milestones, including shipment confirmation, out-for-delivery status, and delivery completion, as well as proactive alerts regarding any exceptions or delays. When Mariana identifies issues such as incorrect delivery addresses, scheduling conflicts, or package damage, she reports them directly through the portal's integrated issue reporting functionality, which creates tracked exceptions visible to customer service representatives. Through these capabilities, SLMS transforms Mariana's experience from passive waiting and reactive problem-solving to active monitoring and confident engagement with the delivery process, ultimately enhancing satisfaction and building brand loyalty.

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

**Responsible: João Roldão**

*[6-8 pages with security implementation details]*

**Reference:** `docs/adr/0004-architecture-update-security-&-auth.md`

## 8.1 Authentication & Authorization

**Topics to cover:**

**Keycloak OAuth2/OIDC:**
- OAuth2 authorization code flow
- OIDC authentication
- JWT token issuance and validation
- Token refresh mechanism

**Role-Based Access Control:**
- 5 roles: MANAGER, WAREHOUSE, CSR, DRIVER, CUSTOMER
- Permission mapping per role
- Endpoint-level authorization

**Session Management:**
- Session timeout configuration
- Concurrent session handling
- Logout and token invalidation

## 8.2 API Security

**Topics to cover:**

**HTTPS Enforcement:**
- SSL/TLS certificate configuration
- Redirect HTTP to HTTPS (production)
- Certificate renewal

**CORS Configuration:**
- Allowed origins
- Allowed methods
- Credentials handling

**Rate Limiting:**
- Request rate limits per client
- Burst handling
- DDoS protection

**Input Validation:**
- Request validation
- SQL injection prevention
- XSS protection

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

**Responsible: Henrique Teixeira**

*[4-5 pages with AI integration details]*

**User Story**: US19 (All Users)

**Reference:** `docs/handbook/chatbot-module.md`, `docs/adr/0007-chatbot-module-integration.md`, Code in `src/backend/src/main/java/Operimus/SLMS/business_layer/chatbot_module/`

## 10.1 Context-Aware Assistance

**Topics to cover:**
- User role detection
- Current page/route context
- Personalized responses based on user type
- Frontend application detection

## 10.2 Role-Based Content Filtering

**Topics to cover:**
- Logistics Manager: KPI insights, carrier comparisons
- Warehouse Staff: Order processing help
- CSR: Exception resolution guides
- Driver: Delivery procedures
- Customer: Tracking, FAQ

## 10.3 Frontend-Specific Help

**Topics to cover:**
- Admin Console assistance
- Customer Portal guidance
- Driver PWA instructions
- Route-based contextual guidance
- Help specific to current UI view

## 10.4 Multilingual Support

**Topics to cover:**
- English and Portuguese
- Language detection
- Localized responses
- Cultural context awareness

## 10.5 Semantic Search with Pinecone

**Topics to cover:**
- Document embedding (768 dimensions)
- Vector similarity search
- Cosine metric for relevance
- Context retrieval
- Top-k configuration

## 10.6 Natural Language Responses via Gemini

**Topics to cover:**
- Query understanding
- Context-aware generation
- Conversational tone
- Fallback responses
- Safety settings

## 10.7 Document Management

**Topics to cover:**
- Document ingestion pipeline
- Chunking strategy (500-1000 tokens)
- Metadata tagging (role, frontend, route)
- Update and versioning
- Content filtering

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
