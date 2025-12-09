-- Check current warehouse and order data
-- Run this to see what's currently in your database

-- 1. Check warehouses
SELECT
  id,
  code,
  name,
  address_line1 || ', ' || city || ', ' || state_or_province || ' ' || postal_code AS full_address,
  country,
  active
FROM warehouses
ORDER BY id;

-- 2. Check orders and their addresses
SELECT
  o.id,
  o.reference,
  o.status,
  o.shipping_address,
  o.shipping_city || ', ' || o.shipping_state || ' ' || o.shipping_postal_code AS delivery_location,
  w.name AS warehouse_name,
  w.city AS warehouse_city,
  o.shipment_id
FROM orders o
LEFT JOIN warehouses w ON o.warehouse_id = w.id
ORDER BY o.id DESC
LIMIT 20;

-- 3. Check if warehouse and delivery are at same location
SELECT
  o.id AS order_id,
  o.reference,
  CASE
    WHEN w.address_line1 = o.shipping_address THEN '⚠️ SAME LOCATION'
    ELSE '✓ Different locations'
  END AS location_check,
  w.address_line1 AS warehouse_address,
  o.shipping_address AS delivery_address
FROM orders o
JOIN warehouses w ON o.warehouse_id = w.id
WHERE o.id = 38;

-- 4. Check shipments and assigned drivers
SELECT
  s.id,
  s.shipment_code,
  s.status,
  s.driver_id,
  s.total_orders,
  COUNT(o.id) AS actual_order_count
FROM shipments s
LEFT JOIN orders o ON o.shipment_id = s.id
GROUP BY s.id, s.shipment_code, s.status, s.driver_id, s.total_orders
ORDER BY s.id DESC
LIMIT 10;

-- 5. Check which orders are assigned to which shipments
SELECT
  s.shipment_code,
  s.driver_id,
  o.id AS order_id,
  o.reference,
  o.status,
  o.shipping_city
FROM shipments s
LEFT JOIN orders o ON o.shipment_id = s.id
WHERE s.driver_id IS NOT NULL
ORDER BY s.id, o.id;
