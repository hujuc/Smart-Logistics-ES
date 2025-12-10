# 0006 – Shipment Entity Introduction

**Sprint 3**

## Context

During implementation of the order management and carrier integration modules, we discovered that the original data model (ADR-0002) did not adequately separate concerns between customer orders and carrier shipments.

### Problem

The original design combined order information and shipment details in a single Order entity:
- Orders contained carrier-specific data (ShipEngine IDs, tracking numbers, costs)
- No clear separation between "what to ship" vs "how it's being shipped"
- Difficult to assign multiple orders to a single shipment
- Mixing customer-facing data with carrier-facing data

### Requirements

- Clear domain separation between order management and carrier operations
- Support for carrier and driver assignment workflow
- Ability to group multiple orders into shipments (future capability)
- Isolate carrier-specific integration data from order data
- Maintain module independence (order_module and rate_carrier_module)

### Constraints

- Cannot use entity references across modules (circular dependency risk)
- Must maintain backwards compatibility with existing orders
- Database migrations must be non-breaking
- Frontend code should not require major refactoring

## Decision

We introduced a **Shipment entity** as a separate domain concept in the rate_carrier_module.

### Domain Separation

**Order (order_module)**
- Represents customer's request to ship something
- Contains: reference, description, status, shipping address, destination, dimensions, customer details
- Lifecycle: CREATED → DOCUMENTED → READY → COLLECTED → IN_TRANSIT → DELIVERED
- Owned by: order_module

**Shipment (rate_carrier_module)**
- Represents carrier execution of transporting orders
- Contains: shipment code, carrier assignment, driver assignment, ShipEngine/Shippo integration IDs, costs, transit time
- Created when: Order is assigned to a carrier and driver
- Owned by: rate_carrier_module

### Entity Structure

**Shipment Table**
```sql
CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    shipment_code VARCHAR(64) UNIQUE NOT NULL,
    carrier_id BIGINT NOT NULL REFERENCES carriers(id),
    driver_id VARCHAR(64) NOT NULL,  -- Keycloak UUID
    shippo_shipment_id VARCHAR(128),
    shippo_transaction_id VARCHAR(128),
    shipstation_order_id VARCHAR(128),
    shipstation_carrier_code VARCHAR(64),
    shipstation_service_code VARCHAR(64),
    total_orders INTEGER,
    total_weight DOUBLE,
    weight_unit VARCHAR(16),
    estimated_delivery_date DATE,
    shipping_cost DOUBLE,
    transit_time VARCHAR(64),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Order Table (updated)**
```sql
ALTER TABLE orders
ADD COLUMN shipment_id BIGINT;  -- References shipments.id (no FK constraint)
```

### Cross-Module Reference Strategy

To avoid circular dependencies between order_module and rate_carrier_module:
- Order stores `shipmentId` as Long (not Shipment entity reference)
- To query shipment: `shipmentRepository.findById(order.getShipmentId())`
- To query orders for shipment: `orderRepository.findByShipmentId(shipmentId)`

This maintains module independence while allowing logical relationships.

### Workflow

1. Warehouse staff creates Order (status: CREATED)
2. Order processed and documents generated (status: DOCUMENTED, READY)
3. Logistics manager assigns Order to carrier and driver:
   - Shipment entity created with carrier_id and driver_id
   - Order.shipmentId updated with shipment ID
4. Order collection and transit tracking continues
5. Shipment provides carrier-specific tracking and cost information

## Consequences

**Positive:**

- Clear domain boundaries between order management and carrier operations
- Order entity cleaner: focused on customer request and delivery tracking
- Shipment entity encapsulates all carrier-specific data (ShipEngine, Shippo integrations)
- Enables future capability: multiple orders in a single shipment
- Module independence maintained: no cross-module entity dependencies
- Easier to understand: "Order = what to ship", "Shipment = how it's shipped"
- Better alignment with real-world logistics terminology
- Carrier module can evolve independently

**Negative:**

- Additional database table increases schema complexity
- Cross-module queries require two repository calls instead of one
- Not in original ADR-0002 data model (documentation requires update)
- Developers must understand the Order-Shipment relationship
- Slightly more complex workflow for carrier assignment

## Alternatives Considered

### Option 1: Keep everything in Order entity
**Why not chosen:**
- Mixes customer-facing and carrier-facing concerns
- Order entity becomes too large and complex
- Cannot group multiple orders into shipments
- Violates single responsibility principle
- Tight coupling between order and carrier modules

### Option 2: Use entity reference (Order → Shipment entity)
**Why not chosen:**
- Creates circular dependency between order_module and rate_carrier_module
- Violates modular architecture boundaries
- Makes modules tightly coupled
- Complicates testing and maintenance
- Goes against our modular monolith principles

### Option 3: Event-driven approach (no direct reference)
**Why not chosen:**
- Over-engineering for current scale
- Adds complexity with event sourcing infrastructure
- Harder to query and maintain consistency
- Not justified for current requirements
- Can be adopted later if needed

## Related Decisions

- ADR-0001 – Initial System Architecture (modular monolith)
- ADR-0002 – Data Model Definition (original design)
- ADR-0003 – Architecture Update (database integration)

## Notes

- Shipment entity added in Sprint 3 during carrier integration implementation
- Migration was non-breaking: existing orders function without shipment_id
- Future enhancement: allow multiple orders per shipment (e.g., consolidated delivery routes)
- Driver assignment workflow depends on this separation
- ShipEngine and Shippo integration data now cleanly isolated in Shipment entity
- This pattern could be extended to other cross-module relationships if needed
