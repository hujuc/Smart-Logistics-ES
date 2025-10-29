# Data Model Handbook

## Overview

SLMS uses **PostgreSQL 16** as the primary database with a comprehensive relational schema designed to support the entire logistics workflow.

## Database Structure

### Core Principles

- **UUID Primary Keys** - All tables use UUIDs for globally unique identifiers
- **JSONB for Flexibility** - Addresses, metadata, and preferences stored as JSONB
- **Shared Schema** - Single database with logical separation by module
- **Strong Relationships** - Foreign keys ensure referential integrity

### Table Organization

**18 tables grouped by functional domain:**

#### Foundation (2 tables)

- `users` - Authentication and RBAC
- `warehouses` - Multi-warehouse support

#### Order Management (4 tables)

- `carriers` - Third-party shipping providers
- `orders` - Core shipment tracking
- `order_events` - Status transition timeline for each order
- `shipping_documents` - Labels, manifests, POD

#### Tracking (2 tables)

- `carrier_rates` - Shipping rate comparison
- `tracking_events` - Real-time status updates

#### Delivery (4 tables)

- `drivers` - Driver profiles
- `delivery_routes` - Daily route planning
- `delivery_manifests` - Individual delivery stops
- `delivery_attempts` - Multi-attempt logging

#### Customer Portal (4 tables)

- `customers` - Customer profiles
- `notification_templates` - Multi-language templates
- `notification_logs` - Notification tracking
- `customer_preferences` - Delivery preferences

#### Analytics (2 tables)

- `delivery_exceptions` - Problem tracking
- `kpi_metrics` - Pre-calculated KPIs
- `reports` - Generated reports
- `audit_logs` - Change history

## Documentation

**Full Schema Documentation:** [Data Model Definition](../per-sprint/sprint2/data-model-definition/data-model-definition.md)

For detailed table structures, relationships, and field descriptions, see the complete data model documentation.
