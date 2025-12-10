# Data Model Handbook

## Overview

SLMS uses PostgreSQL 16 as the primary database with a relational schema designed to support the core logistics workflow. The system currently implements 7-8 tables organized by functional module.

## Current Implementation

### Core Principles

- **Mixed Primary Key Strategy** - Long IDs for core entities (Order, Carrier, Shipment), UUIDs for auxiliary entities (DeliveryException, AssistantDocument)
- **JSONB for Flexibility** - Addresses and metadata stored as JSONB
- **Shared Schema** - Single database with logical separation by module
- **Cross-Module References** - Modules reference each other via ID fields (Long or UUID) rather than entity relationships
- **Keycloak for Users** - User authentication and management handled by Keycloak (no users table in application database)

## Database Tables

### Order Module (4 tables)

**orders**
- Core shipment order tracking from creation to delivery
- Fields: id (Long), reference, description, status, shipping address fields, estimatedDeliveryDate, warehouse_id, shipment_id, customer_id, QR code fields, proof of delivery, tracking, dimensions
- Status enum: CREATED, DOCUMENTED, READY, COLLECTED, IN_TRANSIT, DELIVERED, CANCELLED, EXCEPTION
- References: Warehouse entity, Shipment via shipmentId (Long)

**order_events**
- Immutable audit trail of order status transitions
- Fields: id (Long), order_id, event_type, occurred_at, notes
- Provides chronological history for each order

**warehouses**
- Physical locations where orders originate
- Fields: id (Long), code, name, address fields, manager_id, active
- Supports multi-warehouse operations

**OrderStatus (enum)**
- Not a table, but an enum in the Order entity
- Values: CREATED, DOCUMENTED, READY, COLLECTED, IN_TRANSIT, DELIVERED, CANCELLED, EXCEPTION

### Rate Carrier Module (2 tables)

**carriers**
- Third-party shipping provider configurations
- Fields: id (Long), code, name, description, support contact info, ShipEngine/Shippo integration IDs, service levels, active, embedded address
- Stores carrier API credentials and capabilities

**shipments**
- Carrier-assigned shipments (orders become shipments when assigned to carrier and driver)
- Fields: id (Long), shipment_code, carrier_id, driver_id, ShipEngine/Shippo integration fields, totals, costs, transit time
- References: Carrier entity, driver via driverId (String - Keycloak UUID)
- Note: Orders reference shipments via Order.shipmentId to avoid cross-module entity dependencies

### Visibility Module (1 table)

**delivery_exceptions**
- Tracks delivery problems and issue resolution
- Fields: id (UUID), order_id, type, category, severity, description, source, reporter info, customer fields, assignment fields, resolution fields, SLA tracking, feedback
- Enums: ExceptionType, ExceptionStatus, Severity, Source, Priority
- Supports exception workflow from reporting through resolution

### Chatbot Module (4 tables)

**assistant_documents**
- Stores document chunks for AI assistant with role/frontend/route-based filtering
- Fields: id (UUID), chunk_id (Pinecone vector ID), title, roles, frontends, routes, locale, content, metadata_json, created_at
- Enables context-aware help based on user role and current application context

**assistant_document_roles**
- Collection table for role associations (e.g., "MANAGER", "WAREHOUSE_STAFF")

**assistant_document_frontends**
- Collection table for frontend associations (e.g., "admin-console", "customer-portal")

**assistant_document_routes**
- Collection table for route associations (e.g., "/orders", "/carriers")

## Key Design Decisions

### Order vs Shipment Separation

Orders represent customer requests (what to ship, where). Shipments represent carrier execution (how it's being shipped, by whom). This separation allows:
- Clear domain boundaries between order management and carrier operations
- Shipment entity holds carrier-specific data (tracking IDs, costs, driver assignment)
- Multiple orders can potentially be grouped into a single shipment (future capability)

### Cross-Module References

To maintain module independence while avoiding circular dependencies, entities reference other modules via ID fields:
- Order.shipmentId (Long) instead of Shipment entity reference
- DeliveryException.orderId (Long) instead of Order entity reference
- To query across modules: `shipmentRepository.findById(order.getShipmentId())`

### Primary Key Strategy

- **Long IDs**: Used for high-volume transactional entities (orders, carriers, shipments) for database performance
- **UUIDs**: Used for entities requiring global uniqueness or distributed generation (exceptions, assistant documents)

## Authentication and User Management

User authentication is handled by Keycloak, not the application database. User references in tables store Keycloak user UUIDs as strings:
- Order.customerId - Keycloak customer UUID
- Shipment.driverId - Keycloak driver UUID
- DeliveryException.reportedBy, assignedTo, resolvedBy - Keycloak user UUIDs

## External Integrations

### ShipEngine/Shippo Integration
- Carrier and Shipment tables store external system IDs (shippoShipmentId, shipEngineCarrierId, etc.)
- Enables rate comparison and label generation via unified carrier API

### Pinecone Vector Storage
- AssistantDocument.chunkId references vector embeddings stored in Pinecone
- Enables semantic search for contextual help documents

## Documentation References

- **Full Original Data Model Plan**: [Sprint 2 Data Model Definition](../per-sprint/sprint2/data-model-definition/data-model-definition.md)
- **Architecture Decisions**: [ADR-0002 Data Model Definition](../adr/0002-data-model-definition.md), [ADR-0003 Database Integration](../adr/0003-architecture-update-db-integration.md)
