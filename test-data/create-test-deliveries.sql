-- Test Deliveries for Route Optimization and Trip Simulation
-- This creates deliveries with different addresses in the California Bay Area

-- Assumption: You have a warehouse with ID 1 at Google HQ
-- If your warehouse ID is different, update the warehouse_id values below

-- Update existing order 38 to have a different delivery address
UPDATE orders
SET
  shipping_address = '1 Apple Park Way',
  shipping_address_line1 = '1 Apple Park Way',
  shipping_address_line2 = NULL,
  shipping_city = 'Cupertino',
  shipping_state = 'CA',
  shipping_postal_code = '95014',
  shipping_country = 'US'
WHERE id = 38;

-- Add more test orders for route optimization testing
-- You'll need to replace {warehouse_id} and {customer_id} with actual values

-- Order 2: Facebook/Meta HQ
INSERT INTO orders (
  reference,
  description,
  status,
  shipping_address,
  shipping_address_line1,
  shipping_city,
  shipping_state,
  shipping_postal_code,
  shipping_country,
  shipping_phone,
  warehouse_id,
  customer_id,
  weight,
  weight_unit
) VALUES (
  'TEST-002',
  'Test delivery to Meta HQ',
  'CREATED',
  '1 Hacker Way, Menlo Park, CA 94025',
  '1 Hacker Way',
  'Menlo Park',
  'CA',
  '94025',
  'US',
  '+1-650-543-4800',
  1, -- Replace with your warehouse_id
  'test-customer-001',
  5.0,
  'kg'
);

-- Order 3: Stanford University
INSERT INTO orders (
  reference,
  description,
  status,
  shipping_address,
  shipping_address_line1,
  shipping_city,
  shipping_state,
  shipping_postal_code,
  shipping_country,
  shipping_phone,
  warehouse_id,
  customer_id,
  weight,
  weight_unit
) VALUES (
  'TEST-003',
  'Test delivery to Stanford',
  'CREATED',
  '450 Serra Mall, Stanford, CA 94305',
  '450 Serra Mall',
  'Stanford',
  'CA',
  '94305',
  'US',
  '+1-650-723-2300',
  1, -- Replace with your warehouse_id
  'test-customer-002',
  3.5,
  'kg'
);

-- Order 4: San Francisco
INSERT INTO orders (
  reference,
  description,
  status,
  shipping_address,
  shipping_address_line1,
  shipping_city,
  shipping_state,
  shipping_postal_code,
  shipping_country,
  shipping_phone,
  warehouse_id,
  customer_id,
  weight,
  weight_unit
) VALUES (
  'TEST-004',
  'Test delivery to San Francisco',
  'CREATED',
  '1 Market St, San Francisco, CA 94105',
  '1 Market St',
  'San Francisco',
  'CA',
  '94105',
  'US',
  '+1-415-555-0100',
  1, -- Replace with your warehouse_id
  'test-customer-003',
  2.0,
  'kg'
);

-- Order 5: Tesla Palo Alto
INSERT INTO orders (
  reference,
  description,
  status,
  shipping_address,
  shipping_address_line1,
  shipping_city,
  shipping_state,
  shipping_postal_code,
  shipping_country,
  shipping_phone,
  warehouse_id,
  customer_id,
  weight,
  weight_unit
) VALUES (
  'TEST-005',
  'Test delivery to Tesla',
  'CREATED',
  '3500 Deer Creek Rd, Palo Alto, CA 94304',
  '3500 Deer Creek Rd',
  'Palo Alto',
  'CA',
  '94304',
  'US',
  '+1-650-681-5000',
  1, -- Replace with your warehouse_id
  'test-customer-004',
  8.0,
  'kg'
);

-- View the results
SELECT
  id,
  reference,
  shipping_address,
  shipping_city,
  shipping_state,
  shipping_postal_code
FROM orders
WHERE reference LIKE 'TEST-%'
ORDER BY id;
