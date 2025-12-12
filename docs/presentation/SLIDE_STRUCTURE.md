# SLMS Presentation - Slide Structure

**Duration:** 15 minutes
**Format:** Slides + Live Demo
**Audience:** Academic evaluators / Mixed technical audience

---

## Overview

| # | Slide Title | Duration | Purpose |
|---|-------------|----------|---------|
| 1 | Introduction | 0:30 | Hook the audience with project identity |
| 2 | Team Members | 0:30 | Introduce the team and roles |
| 3 | Problem | 1:30 | Establish the business context and pain points |
| 4 | Context: User Stories & Personas | 1:30 | Show scope and stakeholder coverage |
| 5 | System Overview / Architecture | 2:00 | Technical architecture and tech stack |
| 6 | Live Demo - End-to-End Story | 7:00 | Demonstrate working system across all applications |
| 7 | How We Achieved This | 2:00 | Engineering practices, CI/CD, QA, observability |
| 8 | Conclusion | 1:00 | Summarize achievements and invite questions |

**Total:** 15:00 minutes

---

## Slide 1: Introduction
**Duration:** 30 seconds

### Content
- **Title:** Smart Logistics Management System (SLMS)
- **Subtitle:** Group 304 - Software Engineering 2025/2026
- **Institution:** University of Aveiro - DETI
- **Tagline:** "An intelligent platform connecting warehouses, carriers, drivers, and customers"

### Key Points
- Brief project name and course context
- Set the tone: professional, comprehensive, production-ready

---

## Slide 2: Team Members
**Duration:** 30 seconds

### Content
**Team Composition:**
- **João Roldão** (NMEC 113920) - Scrum Master
- **Hugo Castro** (NMEC 113889) - Product Owner
- **Hugo Sousa** (NMEC 112733) - QA Engineer
- **Henrique Teixeira** (NMEC 114588) - Service Analyst
- **João Varela** (NMEC 113780) - DevOps Master

### Key Points
- Show clear role assignments following Scrum/Agile methodology
- Emphasize cross-functional team structure
- Each member has specific ownership areas

---

## Slide 3: Problem
**Duration:** 1 minute 30 seconds

### Content
**The Challenge:**
"E-commerce growth has made logistics increasingly complex and frustrating for all stakeholders"

**Pain Points by Stakeholder:**

**📦 Companies (Logistics Managers & Warehouse Staff)**
- Rising transportation costs
- Fragmented carrier networks with no unified view
- Manual order processing and documentation
- Limited visibility into carrier performance
- Lack of real-time operational data

**🚚 Drivers**
- Inefficient route planning
- Paper-based proof of delivery processes
- No real-time communication with dispatch
- Difficulty reporting delivery exceptions

**👥 Customers**
- No real-time tracking visibility
- Frustration with lack of delivery updates
- Difficulty reporting problems
- Need to call customer service for simple inquiries

**📞 Customer Service Representatives**
- Overwhelmed with avoidable status inquiries
- Time-consuming order lookups across multiple systems
- Manual exception management
- Lack of tools to resolve issues efficiently

### Key Points
- Establish the real-world business context
- Make pain points relatable and concrete
- Set up SLMS as the comprehensive solution to these challenges

---

## Slide 4: Context - User Stories & Personas
**Duration:** 1 minute 30 seconds

### Content
**Project Scope:**
- **18 User Stories** completed across **5 User Personas**
- Complete coverage of the logistics workflow from order creation to final delivery

**Personas and Their Stories:**

**👔 Logistics Manager (US1-US3)**
- US1: Monitor daily KPIs and operational performance
- US2: Compare carrier rates and performance for new shipments
- US3: Generate and export performance reports (PDF/Excel/CSV)

**📦 Warehouse Staff (US4-US6)**
- US4: Process and register new orders with address validation
- US5: Generate shipping labels and documentation
- US6: Confirm carrier pickup and transfer custody

**💁 Customer Service Representative (US7-US9)**
- US7: Quick order status lookup by multiple identifiers
- US8: Manage and resolve delivery exceptions with SLA tracking
- US9: Handle customer-reported issues through support tickets

**🚚 Driver (US10-US12)**
- US10: View daily delivery manifest with route information
- US11: Navigate to delivery locations with map integration
- US12: Capture proof of delivery (photos, signatures, notes)

**👤 Customer (US13-US18)**
- US13: Track order status in real-time without authentication
- US14: View order history and details in customer portal
- US15: Receive proactive notifications about order status changes
- US16: Report delivery problems through self-service portal
- US17: Update delivery preferences and address information
- US18: Get contextual help through AI-powered chatbot

### Key Points
- Shows comprehensive scope: not just a simple tracking app
- All 18 stories successfully completed
- Coverage across the entire logistics ecosystem
- Each persona has meaningful, complete workflows

---

## Slide 5: System Overview / Architecture
**Duration:** 2 minutes

### Content

**Architecture Diagram:**
- Display the `handbook/Architecture_SLMS.png` diagram showing:
  - 4 Frontend Applications
  - Backend with modular structure
  - External integrations
  - Data persistence layer

**4 Frontend Applications:**
1. **Landing Page** - Public-facing order tracking and marketing
2. **Admin Console** - Unified interface for Logistics Managers, Warehouse Staff, and CSRs
3. **Customer Portal** - Self-service application for end customers
4. **Driver PWA** - Mobile Progressive Web App for delivery drivers

**Backend Architecture:**
- **Pattern:** Modular Monolith with Backend-for-Frontend (BFF)
- **7 Business Modules:**
  - Order Module
  - Rate & Carrier Module
  - Visibility & Tracking Module
  - User Management Module
  - Notification Module
  - Reporting Module
  - AI Chatbot Module

**Technology Stack:**

**Backend:**
- Spring Boot 3.5.0 + Java 21
- Spring Data JPA + Hibernate
- PostgreSQL 16
- Apache Kafka + Zookeeper

**Frontend:**
- Next.js 15.5.4 + React 19.1.0
- TypeScript + Tailwind CSS
- NextAuth.js for authentication

**AI & Intelligence:**
- Google Gemini (LLM + Embeddings)
- Pinecone Vector Database
- RAG (Retrieval-Augmented Generation) + Tool Calling

**External Integrations:**
- **Auth:** Keycloak 26.0 (OAuth 2.0 / OpenID Connect)
- **Shipping:** ShipEngine + Shippo APIs (FedEx, UPS, DHL)
- **Storage:** MinIO (S3-compatible object storage)
- **Monitoring:** Elastic APM (Backend + Frontend RUM)
- **Maps:** Mapbox (navigation and routing)
- **Feature Flags:** Flagsmith

**Infrastructure:**
- Docker containerization
- Terraform (Infrastructure as Code)
- Nginx reverse proxy
- GitHub Actions CI/CD

### Key Points
- Modular monolith balances complexity and maintainability
- BFF pattern provides frontend-specific APIs
- Modern, production-ready tech stack
- Comprehensive external service integration
- AI/RAG is a key differentiator

---

## Slide 6: Live Demo - End-to-End Story
**Duration:** 7 minutes

### Content
**Title Slide Only:**
- "Live Demo: End-to-End Delivery Story"
- "Following a package from warehouse to customer's door"

**Demo Flow (see DEMO_SCRIPT.md for details):**
1. **Warehouse Operations** (Admin Console)
   - Create/select order
   - Compare carrier rates
   - Generate shipping label
   - Confirm carrier pickup

2. **Driver Delivery** (Driver PWA)
   - View delivery manifest
   - Navigate with Mapbox routing
   - Capture proof of delivery photo
   - Update delivery status

3. **Customer Tracking** (Customer Portal)
   - Real-time order tracking
   - View delivery timeline
   - See proof of delivery

4. **★ AI Chatbot** (Multiple Frontends)
   - Natural language query: "What's the status of my order?"
   - RAG retrieval + Tool execution
   - Real-time database access with role-based authorization

5. **Exception Management** (Admin Console - CSR)
   - Create/view delivery exception
   - Assign to CSR
   - Resolve with notes

6. **Analytics Dashboard** (Admin Console - Manager)
   - Real-time KPI dashboard
   - Carrier performance comparison
   - Report generation capabilities

### Key Points
- Demo shows all 4 applications in action
- Emphasize transitions between different user roles
- Highlight the AI chatbot as innovative feature
- Show security: different users see different capabilities
- Real system, real data, real-time updates

---

## Slide 7: How We Achieved This - Engineering Excellence
**Duration:** 2 minutes

### Content

**Three Pillars of Engineering Excellence:**

### 1. Quality Assurance & Testing
**Test Coverage:**
- **70%+ code coverage** enforced by JaCoCo
- Minimum coverage gates in CI pipeline

**Test Pyramid:**
- **Unit Tests:** Service layer, business logic, validation
- **Integration Tests:** API endpoints, database interactions, external service mocks
- **E2E Tests:** Playwright automation across all 4 frontends

**Testing Frameworks:**
- Backend: JUnit 5 + Mockito + Spring Boot Test
- Frontend: Jest + React Testing Library
- E2E: Playwright with cross-browser support

---

### 2. CI/CD Automation
**GitHub Actions Pipeline:**
- Automated build on every push
- Parallel test execution (unit + integration + E2E)
- Docker image builds with multi-stage optimization
- Automated deployment on merge to main

**Infrastructure as Code:**
- Terraform for reproducible deployments
- Docker Compose for local development
- Environment parity (dev = staging = production)

**Git Workflow:**
- Feature branches with PR reviews
- Branch protection rules (passing tests required)
- Conventional commit messages
- ADR (Architecture Decision Records) documentation

---

### 3. Observability & Monitoring
**Elastic APM Integration:**
- Backend Java agent with automatic instrumentation
- Frontend RUM (Real User Monitoring) on all 4 apps
- Distributed tracing with W3C Trace Context standard

**What We Monitor:**
- Application performance (response times, throughput)
- Error rates and stack traces
- Database query performance
- External API latency (ShipEngine, Gemini, Pinecone)
- Frontend Core Web Vitals (LCP, INP, CLS)

**SLI/SLO Definitions:**
- API response time p95 < 500ms
- Error rate < 1%
- Frontend page load < 2 seconds

### Key Points
- Professional software engineering practices throughout
- Comprehensive testing at all levels
- Full automation from commit to deployment
- Production-grade observability infrastructure
- Quality and reliability built-in, not bolted on

---

## Slide 8: Conclusion
**Duration:** 1 minute

### Content

**Project Achievements:**
- ✅ **18/18 User Stories Completed**
- ✅ **4 Production-Ready Applications**
- ✅ **Full CI/CD Pipeline** with automated testing and deployment
- ✅ **Innovative AI Chatbot** with RAG + Tool Calling
- ✅ **70%+ Test Coverage** with comprehensive test pyramid
- ✅ **Complete Observability** with Elastic APM monitoring
- ✅ **Modern Architecture** using industry best practices

**Impact Statement:**
"SLMS demonstrates a complete, production-ready logistics management platform built using modern software engineering practices, Agile methodology, and comprehensive quality assurance."

**What Sets SLMS Apart:**
- Complete ecosystem coverage (5 personas, all workflows)
- AI-powered contextual assistance (not just static documentation)
- Engineering maturity (testing, CI/CD, observability)
- Production-ready infrastructure (Docker, Terraform, monitoring)

**Thank You**
"Questions?"

### Key Points
- Reinforce completeness and professionalism
- Highlight innovative aspects (AI chatbot)
- Show engineering maturity beyond just features
- Leave time for questions and discussion

---

## Timing Safety Mechanisms

**If Running Over Time:**
1. Shorten Slide 3 (Problem) to 1 minute - focus on 2-3 key pain points
2. In Demo, skip Exception Management workflow (saves 1-1.5 minutes)
3. Condense Slide 7 to highlight only 2 pillars instead of 3
4. Keep Slides 4, 5, 6 (Core content) intact

**If Finishing Early:**
- Expand Demo with more chatbot interactions
- Show additional features (report generation, notification history)
- Discuss specific technical challenges and solutions
- Preview future roadmap or extension opportunities

---

## Key Messages to Emphasize

1. **Scope & Completeness:** All 18 user stories, 5 personas, complete workflows
2. **Technical Innovation:** AI chatbot with RAG and tool calling for real-time data access
3. **Engineering Excellence:** 70% test coverage, full CI/CD, production observability
4. **Modern Architecture:** Modular monolith with BFF pattern, comprehensive tech stack
5. **Production-Ready:** Not just a prototype - fully functional, monitored, tested system

---

## Presentation Checklist

### Before Presentation Day:
- [ ] All team members review slide content and speaker notes
- [ ] Practice demo flow multiple times
- [ ] Prepare backup screenshots/video in case live demo fails
- [ ] Test all applications are running and accessible
- [ ] Pre-seed database with realistic test data
- [ ] Confirm all external services are operational (Keycloak, Gemini, etc.)
- [ ] Prepare answers to anticipated questions
- [ ] Time the full presentation with transitions

### Day of Presentation:
- [ ] Start all Docker services 15 minutes early
- [ ] Open all application tabs in browser (pre-logged in as different users)
- [ ] Test demo flow one final time
- [ ] Have backup screenshots ready
- [ ] Water/mints for speakers
- [ ] Backup presentation on USB drive or cloud
- [ ] Phone on silent

---

## Backup Plan

**If Live Demo Fails:**
- Have screenshots of each demo step in slide deck
- Pre-recorded video demo (2-3 minutes) showing key flows
- Walk through screenshots while narrating what would happen
- Focus extra time on architecture and engineering practices slides

**Technical Issues Checklist:**
- Backend not responding → Show Elastic APM dashboards with historical data
- Frontend won't load → Use screenshots + explain architecture
- AI chatbot not working → Show tool definitions and explain RAG workflow
- Database connection issues → Show data model diagrams and explain entity relationships
