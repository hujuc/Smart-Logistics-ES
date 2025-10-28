-- Migration: Create delivery_exceptions table
-- Author: Customer Support BFF Implementation
-- Date: 2025-10-27

CREATE TABLE IF NOT EXISTS delivery_exceptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id BIGINT NOT NULL,

    -- Exception details
    type VARCHAR(50) NOT NULL CHECK (type IN ('DAMAGED', 'LOST', 'DELAYED', 'WRONG_ADDRESS', 'WRONG_ITEM', 'NOT_DELIVERED', 'OTHER')),
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(20) DEFAULT 'MEDIUM' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description TEXT NOT NULL,

    -- Source
    source VARCHAR(50) NOT NULL CHECK (source IN ('CUSTOMER', 'DRIVER', 'SYSTEM', 'CARRIER')),
    reported_by UUID,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Customer reported specifics
    customer_reported BOOLEAN DEFAULT false,
    customer_id UUID,

    -- Assignment & Status
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'ESCALATED')),
    assigned_to UUID,
    priority VARCHAR(20) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    -- Resolution
    resolution_notes TEXT,
    resolution_type VARCHAR(50),
    resolved_by UUID,
    resolved_at TIMESTAMP,

    -- SLA tracking
    sla_deadline TIMESTAMP,
    sla_breached BOOLEAN DEFAULT false,

    -- Customer satisfaction
    customer_feedback INTEGER CHECK (customer_feedback BETWEEN 1 AND 5),
    feedback_comment TEXT,

    -- Audit
    updated_at TIMESTAMP,
    history JSONB
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_exceptions_order ON delivery_exceptions(order_id);
CREATE INDEX IF NOT EXISTS idx_exceptions_status ON delivery_exceptions(status);
CREATE INDEX IF NOT EXISTS idx_exceptions_assigned ON delivery_exceptions(assigned_to);
CREATE INDEX IF NOT EXISTS idx_exceptions_customer ON delivery_exceptions(customer_id);
CREATE INDEX IF NOT EXISTS idx_exceptions_priority ON delivery_exceptions(priority);
CREATE INDEX IF NOT EXISTS idx_exceptions_sla_deadline ON delivery_exceptions(sla_deadline);
CREATE INDEX IF NOT EXISTS idx_exceptions_reported_at ON delivery_exceptions(reported_at);

-- Add comment to table
COMMENT ON TABLE delivery_exceptions IS 'Tracks and manages all delivery problems reported by customers, drivers, or detected by the system';
