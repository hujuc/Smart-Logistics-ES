# Data Model

## SLMS Database Schema - Detailed Table Descriptions

---

## Foundation Tables (Core System)

### USERS
Central authentication and authorization table for all system users.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('MANAGER', 'WAREHOUSE', 'CSR', 'DRIVER', 'ADMIN')),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
);
```

**Purpose:** Stores all system users across different roles (João the Manager, Ana the Warehouse Staff, Rita the CSR, Carlos the Driver)

**Connects to:**
- ORDERS (created_by, collected_by)
- DRIVERS (user_id)
- AUDIT_LOGS (user_id)
- DELIVERY_EXCEPTIONS (reported_by, assigned_to)

---

### WAREHOUSES
Physical locations where orders are processed and shipped from.

```sql
CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) UNIQUE NOT NULL,  -- e.g., 'WH-LISBON-01'
    name VARCHAR(100) NOT NULL,
    address JSONB NOT NULL,  -- {street, city, postal_code, country, lat, lng}
    timezone VARCHAR(50) DEFAULT 'Europe/Lisbon',
    manager_id UUID REFERENCES users(id),
    active BOOLEAN DEFAULT true,

    INDEX idx_warehouse_code (code)
);
```

**Purpose:** Multiple warehouse support for scaling operations

**Connects to:**
- ORDERS (warehouse_id)
- KPI_METRICS (warehouse_id)

---

## Sprint 2 Tables - Order Management (US4, US5, US6)

### CARRIERS
Third-party shipping companies integrated with the system.

```sql
CREATE TABLE carriers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) UNIQUE NOT NULL,  -- 'DHL', 'FEDEX', 'UPS'
    name VARCHAR(100) NOT NULL,
    api_endpoint VARCHAR(255),
    api_key VARCHAR(255),  -- Encrypted
    webhook_url VARCHAR(255),  -- For receiving updates
    capabilities JSONB,  -- {express: true, standard: true, international: true}
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_carrier_code (code)
);

-- Initial data
INSERT INTO carriers (code, name, capabilities) VALUES
    ('DHL', 'DHL Express', '{"express": true, "standard": true, "international": true}'),
    ('FEDEX', 'FedEx', '{"express": true, "standard": true, "international": true}'),
    ('UPS', 'UPS', '{"express": true, "standard": false, "international": true}');
```

**Purpose:** Store carrier configurations for rate comparison and API integration

**Connects to:**
- ORDERS (carrier_id)
- CARRIER_RATES (carrier_id)
- DRIVERS (carrier_id)

---

### ORDERS
Core table for all shipment orders in the system.

```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) UNIQUE NOT NULL,  -- 'ORD-2024-001234'
    external_reference VARCHAR(100),  -- Customer's order reference
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Relationships
    carrier_id UUID REFERENCES carriers(id),
    warehouse_id UUID REFERENCES warehouses(id) NOT NULL,
    customer_id UUID REFERENCES customers(id),

    -- Package details
    weight DECIMAL(10,3) NOT NULL,  -- in kg
    dimensions JSONB,  -- {length: 30, width: 20, height: 15} in cm
    package_type VARCHAR(50),  -- 'BOX', 'ENVELOPE', 'PALLET'
    fragile BOOLEAN DEFAULT false,

    -- Addresses
    origin_address JSONB NOT NULL,  -- Warehouse address (denormalized for history)
    destination_address JSONB NOT NULL,  -- {name, phone, street, city, postal_code, country}

    -- Tracking
    carrier_tracking_number VARCHAR(100),  -- External tracking from carrier
    internal_tracking_code VARCHAR(100) UNIQUE,  -- Our tracking code

    -- Timestamps & Users
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id) NOT NULL,  -- Ana who created it
    documented_at TIMESTAMP,  -- When labels were generated
    pickup_at TIMESTAMP,  -- When carrier collected
    collected_by UUID REFERENCES users(id),  -- Ana who confirmed pickup
    delivered_at TIMESTAMP,

    -- Costs
    shipping_cost DECIMAL(10,2),
    insurance_value DECIMAL(10,2),

    CONSTRAINT check_status CHECK (status IN ('PENDING', 'DOCUMENTED', 'READY', 'COLLECTED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED', 'EXCEPTION')),

    INDEX idx_orders_status (status),
    INDEX idx_orders_number (order_number),
    INDEX idx_orders_created (created_at),
    INDEX idx_orders_carrier (carrier_id),
    INDEX idx_orders_customer (customer_id)
);
```

**Purpose:** Central order management, tracks lifecycle from creation to delivery

**Connects to:**
- SHIPPING_DOCUMENTS (order_id)
- TRACKING_EVENTS (order_id)
- DELIVERY_MANIFESTS (order_id)
- DELIVERY_EXCEPTIONS (order_id)
- NOTIFICATION_LOGS (order_id)
- ORDER_EVENTS (order_id)

---

### ORDER_EVENTS
Immutable audit trail capturing every status transition an order experiences.

```sql
CREATE TABLE order_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    event_type VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,

    CONSTRAINT check_event_type CHECK (event_type IN (
        'CREATED', 'READY_FOR_PICKUP', 'COLLECTED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'
    )),

    INDEX idx_order_events_order (order_id, occurred_at DESC)
);
```

**Purpose:** Preserve a chronological history of order state changes for reporting and customer visibility

**Connects to:**
- ORDERS (order_id)

---

### SHIPPING_DOCUMENTS
Generated documents for each order (labels, manifests, etc).

```sql
CREATE TABLE shipping_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE NOT NULL,
    type VARCHAR(30) NOT NULL,
    document_url VARCHAR(500) NOT NULL,  -- MinIO path
    document_hash VARCHAR(64),  -- SHA-256 for integrity
    file_size_kb INTEGER,
    metadata JSONB,  -- {pages: 2, format: 'A4', barcode: '...'}
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by UUID REFERENCES users(id) NOT NULL,

    CONSTRAINT check_doc_type CHECK (type IN ('SHIPPING_LABEL', 'TRANSPORT_MANIFEST', 'PACKING_SLIP', 'INVOICE', 'CUSTOMS_FORM', 'POD')),

    INDEX idx_docs_order (order_id),
    INDEX idx_docs_type (type)
);
```

**Purpose:** Track all generated documents, link to MinIO storage

**Connects to:**
- ORDERS (order_id)
- USERS (generated_by)

---

## Sprint 3 Tables - Carrier Integration & Tracking

### CARRIER_RATES
Shipping rates and transit times for carrier comparison.

```sql
CREATE TABLE carrier_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    carrier_id UUID REFERENCES carriers(id) ON DELETE CASCADE NOT NULL,
    service_type VARCHAR(50) NOT NULL,  -- 'EXPRESS', 'STANDARD', 'ECONOMY'

    -- Zone/Region based pricing
    origin_zone VARCHAR(50) NOT NULL,  -- 'PT-LISBON', 'PT-PORTO'
    destination_zone VARCHAR(50) NOT NULL,

    -- Weight ranges
    weight_min DECIMAL(10,3) NOT NULL,  -- kg
    weight_max DECIMAL(10,3) NOT NULL,

    -- Pricing
    base_price DECIMAL(10,2) NOT NULL,
    price_per_kg DECIMAL(10,2),
    fuel_surcharge_percent DECIMAL(5,2),

    -- Service levels
    transit_days_min INTEGER NOT NULL,
    transit_days_max INTEGER NOT NULL,
    cutoff_time TIME,  -- Daily cutoff for same-day processing

    -- Validity
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,
    active BOOLEAN DEFAULT true,

    INDEX idx_rates_carrier (carrier_id),
    INDEX idx_rates_zones (origin_zone, destination_zone),
    INDEX idx_rates_validity (valid_from, valid_until)
);
```

**Purpose:** Enable João (Manager) to compare carrier prices and select optimal shipping

**Connects to:**
- CARRIERS (carrier_id)

---

### TRACKING_EVENTS
Real-time tracking updates from carriers and drivers.

```sql
CREATE TABLE tracking_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE NOT NULL,
    carrier_tracking_number VARCHAR(100),

    event_type VARCHAR(50) NOT NULL,
    event_code VARCHAR(50),  -- Carrier-specific codes
    description TEXT NOT NULL,

    -- Location
    location JSONB,  -- {lat, lng, address, city, country}
    location_type VARCHAR(50),  -- 'WAREHOUSE', 'HUB', 'DELIVERY_ADDRESS'

    -- Source & Time
    source VARCHAR(50) NOT NULL,  -- 'CARRIER_API', 'DRIVER_APP', 'MANUAL', 'SYSTEM'
    occurred_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Additional data
    metadata JSONB,  -- Carrier-specific additional info

    CONSTRAINT check_event_type CHECK (event_type IN ('CREATED', 'PICKED_UP', 'IN_TRANSIT', 'ARRIVED_AT_HUB', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED_ATTEMPT', 'RETURNED', 'EXCEPTION')),

    INDEX idx_tracking_order (order_id),
    INDEX idx_tracking_occurred (occurred_at),
    INDEX idx_tracking_type (event_type)
);
```

**Purpose:** Provide Rita (CSR) and Mariana (Customer) with real-time order status

**Connects to:**
- ORDERS (order_id)

---

## Sprint 4 Tables - Driver & Route Management

### DRIVERS
Driver profiles and status tracking.

```sql
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) UNIQUE NOT NULL,
    carrier_id UUID REFERENCES carriers(id),
    employee_code VARCHAR(50),
    license_number VARCHAR(50) NOT NULL,
    license_expiry DATE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    vehicle_id VARCHAR(50),
    vehicle_type VARCHAR(50),  -- 'VAN', 'TRUCK', 'BIKE'
    max_capacity_kg DECIMAL(10,2),

    -- Status
    status VARCHAR(20) DEFAULT 'OFF_DUTY',
    current_location JSONB,  -- {lat, lng, updated_at}
    last_location_update TIMESTAMP,

    -- Availability
    working_hours JSONB,  -- {monday: {start: '08:00', end: '17:00'}, ...}
    active BOOLEAN DEFAULT true,

    CONSTRAINT check_driver_status CHECK (status IN ('AVAILABLE', 'ON_ROUTE', 'BREAK', 'OFF_DUTY')),

    INDEX idx_drivers_user (user_id),
    INDEX idx_drivers_carrier (carrier_id),
    INDEX idx_drivers_status (status)
);
```

**Purpose:** Track Carlos (Driver) and other drivers, their availability and location

**Connects to:**
- USERS (user_id)
- CARRIERS (carrier_id)
- DELIVERY_ROUTES (driver_id)

---

### DELIVERY_ROUTES
Daily route planning for drivers.

```sql
CREATE TABLE delivery_routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) NOT NULL,
    route_date DATE NOT NULL,
    route_code VARCHAR(50) UNIQUE NOT NULL,  -- 'RT-2024-10-15-001'

    -- Status & Timing
    status VARCHAR(20) DEFAULT 'PLANNED',
    planned_start TIME NOT NULL,
    actual_start TIMESTAMP,
    actual_end TIMESTAMP,

    -- Route details
    total_stops INTEGER NOT NULL,
    completed_stops INTEGER DEFAULT 0,
    total_packages INTEGER NOT NULL,
    delivered_packages INTEGER DEFAULT 0,

    -- Optimization
    total_distance_km DECIMAL(10,2),
    estimated_duration_minutes INTEGER,
    optimized_path JSONB,  -- Array of coordinates in order
    optimization_algorithm VARCHAR(50),  -- 'NEAREST_NEIGHBOR', 'GENETIC', 'MANUAL'

    -- Performance
    fuel_consumption_liters DECIMAL(10,2),
    delays_minutes INTEGER,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),

    CONSTRAINT check_route_status CHECK (status IN ('PLANNED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    UNIQUE (driver_id, route_date),

    INDEX idx_routes_driver (driver_id),
    INDEX idx_routes_date (route_date),
    INDEX idx_routes_status (status)
);
```

**Purpose:** Organize daily delivery routes with optimization

**Connects to:**
- DRIVERS (driver_id)
- DELIVERY_MANIFESTS (route_id)

---

### DELIVERY_MANIFESTS
Individual delivery tasks within a route.

```sql
CREATE TABLE delivery_manifests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id UUID REFERENCES delivery_routes(id) ON DELETE CASCADE NOT NULL,
    order_id UUID REFERENCES orders(id) NOT NULL,

    -- Sequence & Priority
    sequence_number INTEGER NOT NULL,  -- Order in route
    priority VARCHAR(20) DEFAULT 'NORMAL',  -- 'HIGH', 'NORMAL', 'LOW'

    -- Timing
    estimated_arrival TIMESTAMP,
    arrival_window_start TIME,
    arrival_window_end TIME,
    actual_arrival TIMESTAMP,
    time_at_stop_minutes INTEGER,

    -- Delivery Status
    delivery_status VARCHAR(30) DEFAULT 'PENDING',
    delivered_at TIMESTAMP,
    recipient_name VARCHAR(100),
    recipient_relationship VARCHAR(50),  -- 'SELF', 'FAMILY', 'NEIGHBOR', 'RECEPTIONIST'

    -- Proof of Delivery
    signature_url VARCHAR(500),  -- MinIO path
    photo_url VARCHAR(500),  -- MinIO path
    delivery_notes TEXT,

    -- Failed attempts
    failure_reason VARCHAR(100),
    failure_code VARCHAR(20),  -- 'ABSENT', 'REFUSED', 'WRONG_ADDRESS'

    CONSTRAINT check_delivery_status CHECK (delivery_status IN ('PENDING', 'EN_ROUTE', 'ARRIVED', 'DELIVERED', 'FAILED', 'RESCHEDULED')),
    UNIQUE (route_id, sequence_number),

    INDEX idx_manifest_route (route_id),
    INDEX idx_manifest_order (order_id),
    INDEX idx_manifest_status (delivery_status)
);
```

**Purpose:** Track each delivery stop Carlos makes on his route

**Connects to:**
- DELIVERY_ROUTES (route_id)
- ORDERS (order_id)
- DELIVERY_ATTEMPTS (manifest_id)

---

### DELIVERY_ATTEMPTS
Log of all delivery attempts (successful or failed).

```sql
CREATE TABLE delivery_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manifest_id UUID REFERENCES delivery_manifests(id) ON DELETE CASCADE NOT NULL,
    attempt_number INTEGER NOT NULL DEFAULT 1,

    -- Attempt details
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(20) NOT NULL,

    -- Failure details
    failure_code VARCHAR(50),
    failure_reason TEXT,

    -- Evidence
    photo_url VARCHAR(500),
    gps_location JSONB,  -- {lat, lng, accuracy}

    -- Next action
    next_action VARCHAR(50),  -- 'RETRY_TOMORROW', 'RETURN_TO_SENDER', 'LEAVE_AT_DEPOT'
    notes TEXT,

    driver_id UUID REFERENCES drivers(id),

    CONSTRAINT check_result CHECK (result IN ('SUCCESS', 'FAILED', 'PARTIAL')),
    UNIQUE (manifest_id, attempt_number),

    INDEX idx_attempts_manifest (manifest_id),
    INDEX idx_attempts_result (result)
);
```

**Purpose:** Track multiple delivery attempts, important for exception handling

**Connects to:**
- DELIVERY_MANIFESTS (manifest_id)
- DRIVERS (driver_id)

---

## Sprint 5 Tables - Notifications & Customer Portal

### CUSTOMERS
Customer profiles for tracking and communication.

```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    name VARCHAR(100) NOT NULL,

    -- Preferences
    preferred_contact VARCHAR(20) DEFAULT 'EMAIL',
    language VARCHAR(5) DEFAULT 'PT',
    timezone VARCHAR(50) DEFAULT 'Europe/Lisbon',

    -- Address book
    default_address JSONB,
    saved_addresses JSONB,  -- Array of addresses

    -- Stats
    total_orders INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_order_at TIMESTAMP,

    -- Auth (for customer portal)
    password_hash VARCHAR(255),
    email_verified BOOLEAN DEFAULT false,

    CONSTRAINT check_contact CHECK (preferred_contact IN ('EMAIL', 'SMS', 'BOTH', 'NONE')),

    INDEX idx_customers_email (email),
    INDEX idx_customers_phone (phone)
);
```

**Purpose:** Store Mariana's (Customer) profile and preferences

**Connects to:**
- ORDERS (customer_id)
- NOTIFICATION_LOGS (customer_id)
- CUSTOMER_PREFERENCES (customer_id)

---

### NOTIFICATION_TEMPLATES
Reusable templates for automated communications.

```sql
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,  -- 'ORDER_CONFIRMED', 'OUT_FOR_DELIVERY'
    event_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    language VARCHAR(5) NOT NULL DEFAULT 'PT',

    -- Content
    subject VARCHAR(200),  -- For emails
    body_template TEXT NOT NULL,  -- With placeholders {{order_number}}, {{customer_name}}

    -- SMS specific
    sms_template TEXT,  -- Shorter version for SMS

    -- Configuration
    priority VARCHAR(20) DEFAULT 'NORMAL',
    active BOOLEAN DEFAULT true,

    -- Timing rules
    delay_minutes INTEGER DEFAULT 0,  -- Delay after event
    quiet_hours JSONB,  -- {start: '22:00', end: '08:00'}

    CONSTRAINT check_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    UNIQUE (event_type, channel, language),

    INDEX idx_templates_event (event_type),
    INDEX idx_templates_active (active)
);
```

**Purpose:** Manage multi-language notification templates

**Connects to:**
- NOTIFICATION_LOGS (template_id)

---

### NOTIFICATION_LOGS
Track all sent notifications and their status.

```sql
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(id),
    order_id UUID REFERENCES orders(id),
    template_id UUID REFERENCES notification_templates(id),

    -- Details
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,  -- Email or phone
    subject VARCHAR(200),
    content TEXT NOT NULL,  -- Rendered template

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,  -- For emails with tracking
    clicked_at TIMESTAMP,

    -- Error handling
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,

    -- Provider info
    provider VARCHAR(50),  -- 'SENDGRID', 'TWILIO'
    provider_message_id VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_status CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),

    INDEX idx_notif_customer (customer_id),
    INDEX idx_notif_order (order_id),
    INDEX idx_notif_status (status),
    INDEX idx_notif_created (created_at)
);
```

**Purpose:** Track notification delivery to ensure Mariana receives updates

**Connects to:**
- CUSTOMERS (customer_id)
- ORDERS (order_id)
- NOTIFICATION_TEMPLATES (template_id)

---

### CUSTOMER_PREFERENCES
Granular notification preferences per customer.

```sql
CREATE TABLE customer_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,

    -- Notification preferences by type
    notification_type VARCHAR(50) NOT NULL,  -- 'ORDER_UPDATES', 'MARKETING', 'DELIVERY_ALERTS'
    enabled BOOLEAN DEFAULT true,
    channel VARCHAR(20),  -- Override default channel

    -- Delivery preferences
    preferred_delivery_window JSONB,  -- {start: '09:00', end: '17:00'}
    weekend_delivery BOOLEAN DEFAULT true,
    signature_required BOOLEAN DEFAULT false,
    leave_at_door BOOLEAN DEFAULT false,

    -- Special instructions
    delivery_instructions TEXT,
    gate_code VARCHAR(20),

    UNIQUE (customer_id, notification_type),

    INDEX idx_prefs_customer (customer_id)
);
```

**Purpose:** Store detailed customer preferences for personalized service

**Connects to:**
- CUSTOMERS (customer_id)

---

## Sprint 6 Tables - Analytics & Exception Management

### DELIVERY_EXCEPTIONS
Track and manage all delivery problems.

```sql
CREATE TABLE delivery_exceptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id) NOT NULL,

    -- Exception details
    type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(20) DEFAULT 'MEDIUM',
    description TEXT NOT NULL,

    -- Source
    source VARCHAR(50) NOT NULL,  -- 'CUSTOMER', 'DRIVER', 'SYSTEM', 'CARRIER'
    reported_by UUID REFERENCES users(id),
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Customer reported specifics (US16)
    customer_reported BOOLEAN DEFAULT false,
    customer_id UUID REFERENCES customers(id),

    -- Assignment & Status
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_to UUID REFERENCES users(id),  -- CSR handling it
    priority VARCHAR(20) DEFAULT 'MEDIUM',

    -- Resolution
    resolution_notes TEXT,
    resolution_type VARCHAR(50),  -- 'REDELIVERY', 'REFUND', 'REPLACEMENT'
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMP,

    -- SLA tracking
    sla_deadline TIMESTAMP,
    sla_breached BOOLEAN DEFAULT false,

    -- Customer satisfaction
    customer_feedback INTEGER CHECK (customer_feedback BETWEEN 1 AND 5),
    feedback_comment TEXT,

    -- Audit
    history JSONB,  -- Array of status changes

    CONSTRAINT check_type CHECK (type IN ('DAMAGED', 'LOST', 'DELAYED', 'WRONG_ADDRESS', 'WRONG_ITEM', 'NOT_DELIVERED', 'OTHER')),
    CONSTRAINT check_status CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'ESCALATED')),
    CONSTRAINT check_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    INDEX idx_exceptions_order (order_id),
    INDEX idx_exceptions_status (status),
    INDEX idx_exceptions_assigned (assigned_to),
    INDEX idx_exceptions_customer (customer_id)
);
```

**Purpose:** Handle Rita's (CSR) exception management and Mariana's problem reports

**Connects to:**
- ORDERS (order_id)
- CUSTOMERS (customer_id)
- USERS (reported_by, assigned_to, resolved_by)

---

### KPI_METRICS
Pre-calculated KPIs for dashboard performance.

```sql
CREATE TABLE kpi_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_type VARCHAR(50) NOT NULL,
    period VARCHAR(20) NOT NULL,
    period_date DATE NOT NULL,

    -- Dimensions (for filtering/grouping)
    carrier_id UUID REFERENCES carriers(id),
    warehouse_id UUID REFERENCES warehouses(id),
    driver_id UUID REFERENCES drivers(id),

    -- Values
    value DECIMAL(20,4) NOT NULL,
    count INTEGER,  -- Number of items in calculation

    -- Additional metrics
    details JSONB,  -- {min: x, max: y, avg: z, percentile_95: w}

    -- Meta
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    calculation_version VARCHAR(10),  -- Track calculation logic changes

    CONSTRAINT check_period CHECK (period IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    UNIQUE (metric_type, period, period_date, carrier_id, warehouse_id, driver_id),

    INDEX idx_kpi_type (metric_type),
    INDEX idx_kpi_date (period_date),
    INDEX idx_kpi_carrier (carrier_id)
);
```

**Example metrics:**
- ON_TIME_DELIVERY_RATE
- AVG_DELIVERY_TIME
- COST_PER_SHIPMENT
- ORDERS_PROCESSED
- EXCEPTION_RATE
- CUSTOMER_SATISFACTION

**Purpose:** Pre-calculated metrics for João's dashboard performance

**Connects to:**
- CARRIERS (carrier_id)
- WAREHOUSES (warehouse_id)
- DRIVERS (driver_id)

---

### REPORTS
Generated and stored reports.

```sql
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,  -- 'RPT-2024-10-001'
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,

    -- Period
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,

    -- Filters applied
    filters JSONB,  -- {carrier_ids: [...], warehouses: [...]}

    -- Content
    data_snapshot JSONB,  -- Denormalized data at time of generation
    summary JSONB,  -- Key metrics summary

    -- File
    file_url VARCHAR(500),  -- MinIO path
    file_format VARCHAR(20),  -- 'PDF', 'EXCEL', 'CSV'
    file_size_kb INTEGER,

    -- Meta
    generated_by UUID REFERENCES users(id) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE,  -- When report becomes stale

    -- Scheduling
    scheduled BOOLEAN DEFAULT false,
    schedule_config JSONB,  -- {frequency: 'WEEKLY', day: 'MONDAY', time: '08:00'}

    CONSTRAINT check_type CHECK (type IN ('PERFORMANCE', 'CARRIER', 'COST', 'EXCEPTION', 'CUSTOM')),

    INDEX idx_reports_type (type),
    INDEX idx_reports_period (period_start, period_end),
    INDEX idx_reports_generated_by (generated_by)
);
```

**Purpose:** Store João's exported reports for historical reference

**Connects to:**
- USERS (generated_by)

---

### AUDIT_LOGS
Complete audit trail for compliance and debugging.

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),

    -- What was changed
    entity_type VARCHAR(50) NOT NULL,  -- 'ORDER', 'CUSTOMER', etc.
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,

    -- Change details
    old_values JSONB,
    new_values JSONB,
    changed_fields TEXT[],  -- Array of field names

    -- Context
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(100),

    -- When
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'VIEW', 'EXPORT')),

    INDEX idx_audit_user (user_id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_timestamp (timestamp)
);
```

**Purpose:** Track all changes for security and compliance

**Connects to:**
- USERS (user_id)
