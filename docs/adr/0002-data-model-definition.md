# 0002 – Data Model Definition

**Sprint 2**

## Context

Following the initial system architecture definition, we needed to establish a comprehensive data model that would support the entire logistics workflow from order creation through delivery completion. The data model needed to accommodate multiple sprints of feature development while maintaining consistency and enabling future enhancements.

**Key requirements:**

- Support complete order lifecycle (creation, documentation, collection, transit, delivery)
- Track relationships between users, warehouses, carriers, orders, and deliveries
- Enable real-time tracking with event history
- Support route optimization and delivery manifest management
- Handle delivery exceptions and customer service workflows
- Provide foundation for analytics and KPI reporting
- Support multi-warehouse operations
- Enable customer notification preferences and tracking
- Maintain audit trail for compliance

**Constraints:**

- Must use PostgreSQL as the primary database
- Need to support JSONB for flexible schema sections (addresses, metadata)
- Must maintain referential integrity
- Should optimize for both transactional writes and analytical queries
- Need to support future scaling across multiple databases

**Options considered:**

- Single database with all tables
- Database-per-service (full microservices data independence)
- Shared database with logical schema separation
- NoSQL document store
- Hybrid SQL + NoSQL approach

## Decision

We adopted a **relational database model using PostgreSQL** with a comprehensive schema organized into logical groups by sprint and functional domain.

### Database Organization

**Foundation Tables (Core System):**
- `users` - Central authentication and role-based access
- `warehouses` - Multi-warehouse support with location data

**Sprint 2 Tables (Order Management):**
- `carriers` - Third-party shipping provider configuration
- `orders` - Core shipment order tracking
- `shipping_documents` - Generated labels, manifests, and POD

**Sprint 3 Tables (Carrier Integration & Tracking):**
- `carrier_rates` - Shipping rates for comparison and optimization
- `tracking_events` - Real-time status updates from carriers and drivers

**Sprint 4 Tables (Driver & Route Management):**
- `drivers` - Driver profiles and availability
- `delivery_routes` - Daily route planning and optimization
- `delivery_manifests` - Individual delivery tasks within routes
- `delivery_attempts` - Multi-attempt delivery logging

**Sprint 5 Tables (Notifications & Customer Portal):**
- `customers` - Customer profiles and preferences
- `notification_templates` - Reusable multi-language templates
- `notification_logs` - Sent notification tracking
- `customer_preferences` - Granular delivery and notification settings

**Sprint 6 Tables (Analytics & Exception Management):**
- `delivery_exceptions` - Problem tracking and resolution
- `kpi_metrics` - Pre-calculated performance metrics
- `reports` - Generated report storage
- `audit_logs` - Complete system audit trail

### Key Design Patterns

**UUID Primary Keys:**
- All tables use UUIDs for globally unique identifiers
- Enables future database sharding and distributed systems

**JSONB for Flexible Data:**
- Addresses (origin_address, destination_address)
- Metadata (document metadata, carrier-specific data)
- Structured arrays (saved_addresses, optimized_path)

**Temporal Tracking:**
- Comprehensive timestamp fields (created_at, updated_at, occurred_at)
- Event history in JSONB for audit purposes

**Status Enumerations:**
- CHECK constraints for valid status values
- Clear state transitions for orders and deliveries

**Indexing Strategy:**
- Primary indexes on foreign keys
- Composite indexes for common query patterns
- Indexes on status fields for filtering

Refer to detailed schema:
- [Data Model Documentation](../per-sprint/sprint2/data-model-definition/data-model.md)

## Consequences

**Positive:**

- **Comprehensive coverage** - Single source of truth for all system data
- **Referential integrity** - Foreign key constraints ensure data consistency
- **Query flexibility** - Relational model supports complex joins and analytics
- **JSONB benefits** - Flexible schema for addresses and metadata without sacrificing query capability
- **Audit trail** - Complete tracking of changes through audit_logs and event tables
- **Sprint alignment** - Tables organized by sprint facilitate incremental development
- **Performance** - Proper indexing supports fast queries for common operations
- **Scalability path** - UUID keys and logical separation enable future sharding
- **Analytics ready** - Pre-calculated KPI tables reduce real-time computation overhead

**Negative:**

- **Schema complexity** - 17 tables with many relationships require careful management
- **Migration overhead** - Schema changes require coordinated migrations across services
- **Join complexity** - Some queries require multiple table joins
- **JSONB trade-offs** - While flexible, JSONB queries can be slower than normalized columns
- **Single point of failure** - Shared database creates coupling between services
- **Scaling challenges** - Vertical scaling limits without sharding implementation

## Alternatives Considered

### Option 1: Database-per-Service
**Why not chosen:**
- Would require complex distributed transactions for order workflows
- Difficult to maintain referential integrity across databases
- Joins across services would require application-level logic
- Increased operational complexity
- Current scale doesn't justify the overhead

## Related Decisions

- ADR-0001 – Initial System Architecture (Sprint 1)
- ADR-0003 – Architecture Update (Sprint 2)

## Notes

- Schema supports future horizontal partitioning by warehouse_id or date ranges
- JSONB fields allow carrier-specific data without schema changes
- Audit log design captures complete change history for compliance
- Event sourcing pattern considered but deferred due to complexity
- Database migrations managed through versioned migration scripts
- Each service accesses only its relevant schema sections, enforcing logical boundaries
- Performance benchmarks show sub-second query times for typical operations
- Schema reviewed and approved by database architect and development team
