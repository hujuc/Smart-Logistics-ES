# Business Layer

This package contains the domain models and use-case logic that power the SLMS backend
    ## Folder structure

- `order_shipment_module/` – implementation for the current shipping and warehouse workflows, organised into `domain/`, `dto/`, `repository/`, and `service/` packages.
  - `domain/` – JPA entities for warehouses and shipment orders that already carry Shippo and ShipStation identifiers, plus the status enum used to track lifecycle transitions.
  - `dto/` – Lombok-based request and response objects with validation hints so controllers can accept and return data consistently, including fields for Shippo and ShipStation metadata.
  - `repository/` – Spring Data JPA interfaces that persist warehouses and shipment orders and support lookups by code or reference.
  - `service/` – transactional services that enforce uniqueness rules, fetch related warehouses, and convert entities to DTOs during CRUD operations.
- `shared/exception/` – reusable HTTP-aware exceptions for not-found and conflict scenarios, implemented with Lombok’s `@StandardException` to reduce boilerplate.
- `notification_module`, `rate_carrier_module`, `reporting_module`, `route_module`, `visibility_module` – reserved folders for future business capabilities that will follow the same structure when implemented.
