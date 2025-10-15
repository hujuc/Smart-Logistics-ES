# Presentation Layer

This package hosts the Backends for Frontends (BFFs) that expose REST APIs to each client surface

## Folder structure

- `admin_console_bff/` – controllers backing the internal admin web application. They receive DTOs, delegate work to business services, and return the mapped responses.
  - `WarehouseController` wires `/api/admin/warehouses` CRUD endpoints to the `WarehouseService` and DTOs from the business layer.
  - `OrderController` provides `/api/admin/orders` CRUD endpoints that call the `OrderService` for business logic and return order DTOs.
  - `HelloController` keeps a lightweight `/hello` check to verify the service is running during development.
- `customer_portal_bff/` – placeholder for the public tracking portal BFF; it will gain controllers as the portal is implemented.
- `driver_pwa_bff/` – placeholder for the driver progressive web app BFF; it will mirror the admin pattern when endpoints are added.
