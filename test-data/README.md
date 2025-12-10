# Test Data for Navigation & Route Optimization

## Problem

Your test order (ID: 38) currently has the **same address** for both the warehouse and the delivery location:
- **Warehouse**: 1600 Amphitheatre Parkway, Mountain View, CA
- **Delivery**: 1600 Amphitheatre Parkway, Mountain View, CA ❌

This causes the navigation system to show "0 km" distance because there's no route needed.

## Solution

You need to update the delivery address to be **different** from the warehouse address.

---

## Quick Fix: Update Order 38

Run this SQL to change order 38's delivery address to Apple Park (15km from Google HQ):

```bash
# Connect to your PostgreSQL database and run:
psql -h localhost -U your_username -d your_database -f test-data/fix-order-38.sql
```

Or use a database GUI tool (like DBeaver, pgAdmin) to run `fix-order-38.sql`.

**Expected Result:**
- **Warehouse**: 1600 Amphitheatre Parkway, Mountain View, CA
- **Delivery**: 1 Apple Park Way, Cupertino, CA ✅
- **Route**: ~15 km driving route will be displayed

---

## Option 2: Create Multiple Test Orders

For testing **route optimization** (which requires 2+ deliveries), run:

```bash
psql -h localhost -U your_username -d your_database -f test-data/create-test-deliveries.sql
```

This creates 5 test orders at different Bay Area locations:
1. **Order 38**: Apple Park, Cupertino (15 km)
2. **Order TEST-002**: Meta HQ, Menlo Park (20 km)
3. **Order TEST-003**: Stanford University, Stanford (25 km)
4. **Order TEST-004**: San Francisco (50 km)
5. **Order TEST-005**: Tesla, Palo Alto (18 km)

Then you can test:
- ✅ Route optimization (automatically orders deliveries efficiently)
- ✅ Multi-stop navigation
- ✅ Trip simulation with multiple stops

---

## Manual Database Update (Alternative)

If you prefer to update manually using a database GUI:

```sql
UPDATE orders
SET
  shipping_address = '1 Apple Park Way, Cupertino, CA 95014',
  shipping_city = 'Cupertino',
  shipping_state = 'CA',
  shipping_postal_code = '95014'
WHERE id = 38;
```

---

## How to Connect to Your Database

Based on your `application.properties`, you need these environment variables:

```bash
# Find your database connection info:
echo $DB_HOST      # Default: localhost
echo $DB_PORT      # Default: 5432
echo $DB_NAME      # Your database name
echo $DB_USERNAME  # Your username
echo $DB_PASSWORD  # Your password
```

Then connect:

```bash
psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME
```

---

## After Updating the Database

1. **Refresh the driver PWA** in your browser
2. Go to **Daily Schedule**
3. Click **"Start Navigation"** or **"Trip Simulation"**
4. You should now see:
   - ✅ Blue marker at warehouse (Google HQ)
   - ✅ Red marker at delivery (Apple Park)
   - ✅ Blue route line connecting them
   - ✅ Real distance (e.g., "15.2 km")
   - ✅ Real duration (e.g., "18 min")
   - ✅ Arrival time calculated

---

## Test Addresses (California, USA)

Use these for creating more test deliveries:

| Location | Address | Distance from Google HQ |
|----------|---------|-------------------------|
| **Apple HQ** | 1 Apple Park Way, Cupertino, CA 95014 | ~15 km |
| **Meta HQ** | 1 Hacker Way, Menlo Park, CA 94025 | ~20 km |
| **Stanford** | 450 Serra Mall, Stanford, CA 94305 | ~25 km |
| **SF Downtown** | 1 Market St, San Francisco, CA 94105 | ~50 km |
| **Tesla** | 3500 Deer Creek Rd, Palo Alto, CA 94304 | ~18 km |
| **Oracle** | 500 Oracle Pkwy, Redwood City, CA 94065 | ~12 km |
| **Netflix** | 100 Winchester Cir, Los Gatos, CA 95032 | ~35 km |

All addresses are verified and work with Mapbox geocoding API.

---

## Troubleshooting

**Still showing same location after update?**
1. Clear browser cache
2. Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)
3. Check database was actually updated: `SELECT * FROM orders WHERE id = 38;`

**Route optimization not working?**
- You need at least **2 deliveries** assigned to a shipment
- Check that orders have `shipment_id` set
- Verify driver is assigned to the shipment

**Geocoding fails?**
- Ensure addresses include city, state, postal code, and country
- Format: `Street Address, City, State ZIP, Country`
