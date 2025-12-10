-- Quick fix: Update order 38 to have a different delivery address
-- This changes the delivery from Google HQ to Apple HQ (about 15km away)

UPDATE orders
SET
  shipping_address = '1 Apple Park Way, Cupertino, CA 95014',
  shipping_address_line1 = '1 Apple Park Way',
  shipping_address_line2 = NULL,
  shipping_city = 'Cupertino',
  shipping_state = 'CA',
  shipping_postal_code = '95014',
  shipping_country = 'US',
  shipping_phone = '+1-408-996-1010'
WHERE id = 38;

-- Verify the update
SELECT
  id,
  reference,
  shipping_address,
  shipping_city,
  shipping_state,
  shipping_postal_code,
  shipping_country
FROM orders
WHERE id = 38;
