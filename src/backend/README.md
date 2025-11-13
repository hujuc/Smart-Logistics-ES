# Backend Architecture

## Overview

The backend follows a layered architecture with Backend-For-Frontend (BFF) pattern, separating presentation concerns from business logic.

## BFF Layer (Presentation Layer)

Located in `presentation_layer/`, contains frontend-specific REST controllers organized by application:

- `landing_page_bff/` - Landing page authentication endpoints
- `admin_console_bff/` - Admin console endpoints (customer service, logistics, warehouse)
- `driver_pwa_bff/` - Driver PWA endpoints
- `customer_portal_bff/` - Customer portal endpoints

**Responsibilities:**
- Handle HTTP concerns (cookies, CORS, response formatting)
- Delegate to business services
- Provide app-specific API contracts

**Endpoints:** `/api/{app-name}/*`

## Business Layer

Located in `business_layer/`, organized by domain modules:

- `user_management_module/` - User and authentication management
- `order_shipment_module/` - Order and shipment operations
- `route_module/` - Route management
- `visibility_module/` - Tracking and visibility
- `rate_carrier_module/` - Carrier rating
- `notification_module/` - Notifications
- `reporting_module/` - Reporting
- `shared/` - Shared configurations and exceptions

**Each module contains:**
- **Controllers**: Internal REST endpoints (`/api/admin/users`, `/api/admin/orders`)
- **Services**: Core business logic + external integrations (Keycloak, etc.)
- **Repositories**: JPA data access
- **Entities**: Domain models (JPA entities)
- **DTOs**: Data transfer objects

## Request Flow

```
Frontend → BFF Controller → Business Service → Repository → Database
```

## Project Structure

- `src/main/java/Operimus/SLMS/business_layer/` - Domain models and business logic
- `src/main/java/Operimus/SLMS/presentation_layer/` - REST controllers (BFFs)
- `src/main/resources/` - Configuration files

## Order Event API

The order shipment module now exposes endpoints that surface the audit trail stored in the `order_events` table:

- `GET /api/admin/orders/{orderId}/events` – return the full status timeline for the specified order ordered by `occurredAt`.
- `GET /api/admin/orders/{orderId}/events/latest` – return only the most recent status change for the specified order.

Both endpoints respond with a JSON array/object using the following shape:

```jsonc
{
  "id": 42,
  "orderId": 5,
  "eventType": "IN_TRANSIT",
  "occurredAt": "2024-03-04T10:15:00Z",
  "notes": "Order status changed from READY_FOR_PICKUP to IN_TRANSIT"
}
```

BFFs provide app-specific API contracts while business services ensure reusable, shared logic across all frontends.

## Driver Shipment Confirmation Flow

- Each order automatically gets a QR code image stored in MinIO. The QR encodes a signed-in-driver callback URL built from `slms.qr.confirmation-base-url` (configure via `QR_CONFIRMATION_BASE_URL`, defaults to `http://localhost:8080/api/driver`).
- Scanning the code should trigger `POST /api/driver/orders/{orderId}/confirm`, which validates the authenticated driver against the shipment, flips the order status to `IN_TRANSIT`, and records the scan timestamp.
- The response reports whether all orders in the shipment have been confirmed so the driver can continue once `allOrdersConfirmed=true`.
