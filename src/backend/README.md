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

BFFs provide app-specific API contracts while business services ensure reusable, shared logic across all frontends.
