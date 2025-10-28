# Developer Handbook

## What is SLMS?

**Smart Logistics Management System (SLMS)** is a comprehensive logistics platform that connects multiple actors in the shipping ecosystem: warehouse staff, carriers, customer service representatives, drivers, and customers.

### Core Capabilities

- **Order Management** - Register orders, generate labels, track shipments
- **Carrier Integration** - Compare rates and create shipments via ShipEngine API
- **Real-time Tracking** - Live order status updates and notifications
- **Route Optimization** - Optimized delivery routes for drivers
- **Analytics** - KPI dashboards and performance reports
- **Notifications** - Email/SMS alerts for order updates

## Architecture Overview

### Backend - Modular Monolith

**Single Spring Boot application** with clear module boundaries:

- **Order Shipment Module** - Order creation, document generation, status tracking
- **Carrier Integration Module** - ShipEngine API integration, rate comparison
- **Visibility Module** - Real-time tracking and event aggregation
- **Route Module** - Delivery route optimization
- **Notification Module** - Email, SMS, in-app notifications
- **Reporting Module** - KPI dashboards and analytics

**Tech Stack:**
- Spring Boot 3.5.6 + Java 21
- Spring Data JPA + PostgreSQL

### Frontend - Three Separate Applications

**1. Customer Portal** (Next.js)
- Public-facing order tracking
- Delivery preferences
- Problem reporting

**2. Admin Console** (Next.js)
- Warehouse staff operations
- Customer service dashboard
- Manager analytics

**3. Driver PWA** (Next.js)
- Delivery manifest
- Proof of delivery capture
- Route navigation

Each frontend has its own **BFF (Backend-for-Frontend)** layer.

### Infrastructure Components

- **PostgreSQL 16** - Primary database (18 tables, relational schema)
- **ShipEngine API** - Unified carrier integration (FedEx, UPS, DHL)
- **Docker** - All components containerized

## Getting Started

### Prerequisites
- Java 21
- Node.js 20+
- Docker & Docker Compose

## Project Structure

```bash
group-project-es2526_304/
   docs/               # Documentation
      adr/           # Architecture Decision Records
      handbook/      # Developer guides
      per-sprint/    # Sprint documentation
   infra/             # Docker Compose configs
   src/
      backend/       # Spring Boot monolith
      frontend/      # Three Next.js apps
         admin-console/
         customer-portal/
         driver-pwa/
```

## Key Documentation

- [Architecture Decision Records](../adr/)
  - [ADR-0001: Initial System Architecture](../adr/0001-initial-system-architecture.md)
  - [ADR-0002: Data Model Definition](../adr/0002-data-model-definition.md)
  - [ADR-0003: Architecture Update](../adr/0003-architecture-update.md)
- [Data Model](data-model.md)
- [Repository Workflow](repo-workflow.md)
- [QA Manual](qa-manual.md)
