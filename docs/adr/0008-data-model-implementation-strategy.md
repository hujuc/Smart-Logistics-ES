# 0008 – Data Model Implementation Strategy

**Sprint 2-4**

## Context

ADR-0002 defined a comprehensive data model with 18 tables covering the entire logistics workflow from order creation through delivery analytics. However, during implementation, we faced practical constraints that required phased development.

### Original Data Model Scope (ADR-0002)

**Foundation Tables (2):**
- users, warehouses

**Order Management (4):**
- carriers, orders, order_events, shipping_documents

**Tracking (2):**
- carrier_rates, tracking_events

**Delivery (4):**
- drivers, delivery_routes, delivery_manifests, delivery_attempts

**Customer Portal (4):**
- customers, notification_templates, notification_logs, customer_preferences

**Analytics (4):**
- delivery_exceptions, kpi_metrics, reports, audit_logs

**Total: 18 tables**

### Problem

- Limited development time per sprint
- Need to deliver working features incrementally
- Risk of over-engineering before user validation
- External dependencies (Keycloak, ShipEngine) changed some requirements
- Some features needed real-world usage feedback before full implementation

### Constraints

- Must deliver core shipping workflow first (highest business value)
- Cannot compromise data integrity or future extensibility
- Team capacity: 5 developers across frontend and backend
- Sprint duration: 2 weeks
- Need to balance completeness with pragmatic delivery

## Decision

We adopted a **phased data model implementation strategy**, implementing core tables first and deferring advanced features.

### Phase 1: Core Shipping Workflow (Sprint 2-3)

**Implemented Tables (7-8 tables):**

1. **warehouses** - Multi-warehouse support (order origin tracking)
2. **orders** - Core order tracking with lifecycle states
3. **order_events** - Audit trail for order status transitions
4. **carriers** - Carrier configuration and ShipEngine integration
5. **shipments** - NEW: Carrier execution (not in original model, see ADR-0006)
6. **delivery_exceptions** - Issue tracking and resolution
7. **assistant_documents** - NEW: AI chatbot support (not in original model, see ADR-0007)
8. **assistant_document_roles, assistant_document_frontends, assistant_document_routes** - Chatbot filtering tables

**Rationale:**
- Enables complete order lifecycle: create → document → assign → track → deliver
- Supports carrier integration via ShipEngine
- Handles exception management
- Provides AI assistance to users

**User Stories Supported:**
- US4: Processing new orders
- US5: Generating shipping labels and documents
- US6: Confirming carrier pickups
- US8: Resolving delivery issues
- Partial support for US16: Real-time tracking (via order status, not full carrier tracking events)

### Phase 2: Deferred Features (Future Sprints)

**Deferred Tables:**

**Route Optimization (4 tables):**
- drivers - Driver profiles and availability
- delivery_routes - Daily route planning
- delivery_manifests - Individual delivery stops
- delivery_attempts - Multi-attempt logging

**Reason for Deferral:** Requires driver onboarding, route optimization algorithms, and mobile PWA maturity

**Customer Portal (4 tables):**
- customers - Customer profiles
- notification_templates - Multi-language templates
- notification_logs - Notification tracking
- customer_preferences - Delivery preferences

**Reason for Deferral:** Using Keycloak for customer authentication; notification system needs requirements validation

**Advanced Tracking (2 tables):**
- carrier_rates - Rate comparison database
- tracking_events - Real-time carrier webhook events

**Reason for Deferral:** ShipEngine provides rate comparison via API (no need for database caching); tracking events pending carrier webhook integration

**Analytics (3 tables):**
- kpi_metrics - Pre-calculated KPIs
- reports - Generated reports
- audit_logs - Complete audit trail

**Reason for Deferral:** Analytics requirements need production data to validate; audit logging can be added when compliance requirements are clear

**Users Table:**
- Not implemented - Keycloak manages all user data

**Reason for Deferral:** External authentication system (Keycloak) eliminates need for application user table

### Primary Key Strategy Change

**Original Design (ADR-0002):** UUID primary keys for all tables

**Implemented Strategy:** Mixed approach
- **Long IDs:** orders, carriers, shipments, warehouses (high-volume transactional tables)
- **UUIDs:** delivery_exceptions, assistant_documents (distributed generation, lower volume)

**Rationale:**
- Long IDs have better database performance for high-volume tables
- Sequential IDs are easier to debug and reference in logs
- UUIDs still used where global uniqueness is valuable (exceptions, documents)

### New Entities Not in Original Model

**Shipment Entity (ADR-0006):**
- Added to separate order concerns from carrier execution
- Stores ShipEngine integration data
- Enables future multi-order shipments

**Assistant Documents (ADR-0007):**
- Added to support AI chatbot functionality
- Stores contextual help content with role/frontend/route filtering
- Not in original requirements but identified as high-value feature

## Consequences

**Positive:**

- Faster delivery of core features (order management, carrier integration, exceptions)
- Validated core workflow with real usage before building advanced features
- Avoided over-engineering unused features
- Learned that Keycloak eliminates need for users table
- Discovered need for Shipment entity (better than original design)
- Identified chatbot as high-value addition
- Can adapt deferred features based on user feedback
- Reduced initial database complexity (7-8 tables vs 18)
- Focused development effort on highest business value

**Negative:**

- Some user stories only partially implemented (US16 tracking without full carrier events)
- Documentation out of sync (ADR-0002 describes unimplemented tables)
- Need to track deferred features to avoid forgetting them
- Cannot demonstrate advanced features (route optimization, analytics) yet
- Some frontend prototypes designed for unimplemented backend features
- Mixed PK strategy requires documentation to explain rationale

## Alternatives Considered

### Option 1: Implement Full Data Model Upfront
**Why not chosen:**
- Would take 3-4 additional sprints
- Risk of building unused or incorrectly designed features
- No early user feedback on core workflow
- Over-engineering before validation
- Keycloak integration would make users table redundant

### Option 2: Minimal MVP (Orders Only)
**Why not chosen:**
- Too limited: cannot handle exceptions or carrier integration
- No path to production readiness
- Missing critical business workflows
- Would require major rework later

### Option 3: Implement One Module at a Time
**Why not chosen:**
- Cannot deliver complete workflows (order creation without carrier assignment is incomplete)
- Cross-module dependencies make isolated implementation difficult
- Delays delivering value to users

## Related Decisions

- ADR-0002 – Data Model Definition (original comprehensive design)
- ADR-0003 – Architecture Update (database integration)
- ADR-0004 – Security & Auth (Keycloak eliminates users table)
- ADR-0006 – Shipment Entity Introduction (addition not in original model)
- ADR-0007 – Chatbot Module Integration (addition not in original model)

## Notes

- Original data model documentation preserved in ADR-0002 as historical record
- Current implementation documented in handbook/data-model.md
- Deferred features tracked in product backlog for future sprints
- Decision to defer was iterative: evaluated after each sprint based on priorities
- Some deferred tables may be redesigned based on lessons learned
- Analytics tables deferred until production data available to validate metrics
- Customer portal tables deferred pending customer onboarding strategy
- Route optimization tables deferred pending driver PWA maturity
- This ADR documents the "why" behind implementation divergence from original plan
