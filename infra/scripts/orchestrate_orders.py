#!/usr/bin/env python3
"""
Orchestration script that simulates ongoing activity in the SLMS backend:
- Creates new orders periodically
- Updates order statuses (CREATED -> READY_FOR_PICKUP)
- Creates shipments for ready orders
- Updates shipment statuses (IN_TRANSIT -> DELIVERED)

Usage:
    python3 orchestrate_orders.py [--interval SECONDS] [--api-base URL]
"""

import argparse
import json
import random
import sys
import time
import urllib.request
import urllib.error
from datetime import date, timedelta
from typing import Any, Dict, List, Optional

# --- Configuration & Constants ---

DESTINATION_TEMPLATES = [
    {
        "contact": "Google Reception",
        "line1": "1600 Amphitheatre Parkway",
        "line2": "Building 41, Reception Desk",
        "city": "Mountain View",
        "state": "CA",
        "postalCode": "94043",
        "country": "US",
        "phone": "+16502530000",
    },
    {
        "contact": "Acme Legal Dept",
        "line1": "1255 Bordeaux Drive",
        "line2": "Suite 200, Legal Department",
        "city": "Sunnyvale",
        "state": "CA",
        "postalCode": "94089",
        "country": "US",
        "phone": "+14085550123",
    },
    {
        "contact": "Stanford Health Care",
        "line1": "875 Blake Wilbur Drive",
        "line2": "Loading Dock 3",
        "city": "Palo Alto",
        "state": "CA",
        "postalCode": "94304",
        "country": "US",
        "phone": "+16504989000",
    },
    {
        "contact": "TechVenture Office",
        "line1": "2000 Sierra Point Parkway",
        "line2": "Unit 450, 4th Floor",
        "city": "Mountain View",
        "state": "CA",
        "postalCode": "94043",
        "country": "US",
        "phone": "+16505557890",
    },
    {
        "contact": "Restaurant Back Entrance",
        "line1": "288 Main Street",
        "line2": None,
        "city": "Los Altos",
        "state": "CA",
        "postalCode": "94022",
        "country": "US",
        "phone": "+16505552468",
    },
    {
        "contact": "Mountain View Library",
        "line1": "585 Franklin Street",
        "line2": "Receiving",
        "city": "Mountain View",
        "state": "CA",
        "postalCode": "94041",
        "country": "US",
        "phone": "+16509036337",
    },
    {
        "contact": "Stanford Research Lab",
        "line1": "318 Campus Drive",
        "line2": "Clark Center, Room S371",
        "city": "Stanford",
        "state": "CA",
        "postalCode": "94305",
        "country": "US",
        "phone": "+16507232300",
    },
    {
        "contact": "Boutique Moderne",
        "line1": "665 East El Camino Real",
        "line2": "Service Entrance",
        "city": "Sunnyvale",
        "state": "CA",
        "postalCode": "94087",
        "country": "US",
        "phone": "+14085553690",
    },
    {
        "contact": "MV Sports Center",
        "line1": "3700 Middlefield Road",
        "line2": "Equipment Room",
        "city": "Mountain View",
        "state": "CA",
        "postalCode": "94043",
        "country": "US",
        "phone": "+16505558901",
    },
    {
        "contact": "Gallery Moderne",
        "line1": "169 University Avenue",
        "line2": "Rear Loading Bay",
        "city": "Palo Alto",
        "state": "CA",
        "postalCode": "94301",
        "country": "US",
        "phone": "+16505554567",
    },
]

SERVICE_LEVEL_CODES = [
    "shippo_priority",
    "shippo_express",
    "shippo_economy",
    "shipstation_ground",
    "shipstation_same_day",
]

TRANSIT_TIMES = [
    "1-2 business days",
    "2-3 business days",
    "3-5 business days",
    "Next day",
]

# --- Helpers ---

def log(message: str) -> None:
    print(f"[orchestrator] {message}", flush=True)

def warn(message: str) -> None:
    print(f"[orchestrator][warn] {message}")

def error(message: str) -> None:
    print(f"[orchestrator][error] {message}", file=sys.stderr)

class HttpClient:
    def __init__(self, base_url: str, timeout: int = 30) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    def request(
        self,
        method: str,
        path: str,
        payload: Optional[Any] = None,
        headers: Optional[Dict[str, str]] = None,
    ) -> Any:
        url = f"{self.base_url}{path}" if path.startswith("/") else f"{self.base_url}/{path}"
        data = None
        req_headers = {"Accept": "application/json"}
        if headers:
            req_headers.update(headers)
        if payload is not None:
            data = json.dumps(payload).encode("utf-8")
            req_headers.setdefault("Content-Type", "application/json")

        request = urllib.request.Request(
            url,
            data=data,
            headers=req_headers,
            method=method.upper(),
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                body = response.read()
                if not body:
                    return None
                text = body.decode("utf-8")
                try:
                    return json.loads(text)
                except json.JSONDecodeError:
                    return text
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8", errors="ignore")
            # Don't crash on 404s or other errors, just return None or raise
            # For orchestration, we might want to just log and continue
            log(f"HTTP {exc.code} for {method} {url}: {body or exc.reason}")
            return None
        except urllib.error.URLError as exc:
            log(f"Connection failed for {method} {url}: {exc.reason}")
            return None

def random_token(length: int) -> str:
    alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    return "".join(random.choice(alphabet) for _ in range(length))

# --- Orchestration Logic ---

class Orchestrator:
    def __init__(self, api_base_url: str) -> None:
        self.http = HttpClient(api_base_url)
        self.timestamp = int(time.time())

    def tick(self) -> None:
        self.timestamp = int(time.time())
        
        # 1. Create a new order on every tick (since interval is long)
        self.create_random_order()

        # 2. Process existing orders (CREATED -> READY_FOR_PICKUP) - limit to 5 per tick
        self.process_created_orders(max_count=5)

        # 3. Create shipments for ready orders (READY_FOR_PICKUP -> IN_TRANSIT) - limit to 1 batch per tick
        self.process_ready_orders(max_count=1)

        # 4. Process shipments status transitions - limit to 5 per tick
        self.process_shipments(max_count=5)


    def create_random_order(self) -> None:
        warehouses = self.http.request("GET", "/admin/warehouses")
        if not warehouses:
            return

        warehouse = random.choice(warehouses)
        destination = random.choice(DESTINATION_TEMPLATES)
        
        # Try to get a customer, or just use a dummy email
        # We don't strictly need a customer ID for the order to be valid in this demo context usually
        # But let's try to fetch one if possible, or just skip customerId
        
        reference = f"ORD-{self.timestamp % 100000:05d}-{random.randint(1, 999):03d}"
        tracking_number = f"TRK{self.timestamp % 100000:05d}{random.randint(1, 999):03d}"
        today = date.today()
        estimated_delivery = today + timedelta(days=random.randint(2, 9))
        
        shipping_address = f"{destination['contact']}, {destination['line1']}"
        if destination.get("line2"):
            shipping_address += f", {destination['line2']}"
        shipping_address += f", {destination['postalCode']} {destination['city']}, {destination['country']}"

        payload = {
            "reference": reference,
            "description": f"Orchestrated order to {destination['city']}",
            "status": "CREATED",
            "shippingAddress": shipping_address,
            "shippingAddressLine1": destination["line1"],
            "shippingAddressLine2": destination.get("line2"),
            "shippingCity": destination["city"],
            "shippingStateOrProvince": destination["state"],
            "shippingPostalCode": destination["postalCode"],
            "shippingCountry": destination["country"],
            "shippingPhone": destination["phone"],
            "shippingEmail": f"customer-{random_token(4)}@demo.slms",
            "estimatedDeliveryDate": estimated_delivery.isoformat(),
            "warehouseId": warehouse["id"],
            "trackingNumber": tracking_number,
            "weight": round(random.uniform(1.0, 12.0), 2),
            "weightUnit": "kg",
            "length": round(random.uniform(20, 60), 1),
            "width": round(random.uniform(15, 40), 1),
            "height": round(random.uniform(8, 25), 1),
            "distanceUnit": "cm",
        }
        
        response = self.http.request("POST", "/admin/orders", payload=payload)
        if response:
            log(f"Created order {response.get('reference')} at {warehouse.get('code')}")

    def process_created_orders(self, max_count: int = 5) -> None:
        # Fetch orders. Ideally we'd filter by status=CREATED but the API might not support it.
        # We'll fetch all and filter client-side if needed, but that's expensive.
        # Let's assume we can just fetch recent ones or just a page.
        # For demo purposes, let's just fetch the list and pick a few to update.
        
        orders = self.http.request("GET", "/admin/orders")
        if not orders:
            return
            
        created_orders = [o for o in orders if o.get("status") == "CREATED"]
        
        # Pick up to max_count to update
        process_count = min(len(created_orders), max_count)
        for _ in range(process_count):
            if not created_orders:
                break
            order = random.choice(created_orders)
            created_orders.remove(order)  # Don't process same order twice
            # Update status to READY_FOR_PICKUP
            # We need to know the endpoint for updating status.
            # Usually PUT /admin/orders/{id} with full payload or PATCH
            # Let's assume PUT with the same payload but updated status
            
            # First we need the full details if the list view is partial
            # But let's try to just send the fields we want to change if PATCH is supported
            # or send the whole object back if we have it.
            
            # The seed script uses POST to create.
            # Let's try to update status.
            
            order["status"] = "READY_FOR_PICKUP"
            # We might need to ensure all required fields are present.
            # The 'orders' list item might be a summary.
            
            # Let's try to fetch detail first
            detail = self.http.request("GET", f"/admin/orders/{order['id']}")
            if detail:
                detail["status"] = "READY_FOR_PICKUP"
                self.http.request("PUT", f"/admin/orders/{order['id']}", payload=detail)
                log(f"Updated order {order['reference']} to READY_FOR_PICKUP")

    def process_ready_orders(self, max_count: int = 1) -> None:
        # Create shipments for READY_FOR_PICKUP orders
        # max_count here refers to number of shipment batches to create
        orders = self.http.request("GET", "/admin/orders")
        if not orders:
            return
            
        ready_orders = [o for o in orders if o.get("status") == "READY_FOR_PICKUP"]
        if not ready_orders:
            return
            
        # We need carriers and drivers
        carriers = self.http.request("GET", "/admin/carriers")
        if not carriers:
            log("No carriers found, cannot create shipments")
            return

        # Try to fetch drivers. /api/users might be protected, but let's try.
        # If it fails, we can't proceed with shipment creation easily without a known driver ID.
        # However, we can try to reuse a driver ID from an existing shipment if any exist.
        drivers = []
        users = self.http.request("GET", "/users") # Try public/admin endpoint if available
        if users and isinstance(users, list):
            # Filter for drivers. The user object structure depends on Keycloak/UserRepresentation
            # Usually has "realmRoles" or we check username convention
            # The seed script creates users with username starting with "driver-"
            drivers = [u for u in users if u.get("username", "").startswith("driver-")]
        
        if not drivers:
            # Fallback: check existing shipments to find a valid driver ID
            existing_shipments = self.http.request("GET", "/admin/shipments")
            if existing_shipments:
                known_driver_ids = {s.get("driverId") for s in existing_shipments if s.get("driverId")}
                if known_driver_ids:
                    # Create dummy driver objects
                    drivers = [{"id": did, "username": "unknown"} for did in known_driver_ids]

        if not drivers:
            log("No drivers found, cannot create shipments")
            return

        # Create 1 shipment with up to 5 orders
        batch_size = 5
        batch = ready_orders[:batch_size]
        
        carrier = random.choice(carriers)
        driver = random.choice(drivers)
        shipment_code = f"SHP-{self.timestamp % 100000:05d}-{random.randint(1, 999):03d}"
        
        # Calculate totals
        total_weight = sum(o.get("weight", 0.0) for o in batch)
        
        payload = {
            "shipmentCode": shipment_code,
            "carrierId": carrier["id"],
            "driverId": driver["id"],
            "orderIds": [o["id"] for o in batch],
            "shippoShipmentId": f"shippo_{shipment_code.lower()}",
            "shippoTransactionId": f"txn_{random_token(8).lower()}",
            "shipstationOrderId": f"ss_{shipment_code.lower()}",
            "shipstationCarrierCode": carrier.get("code"),
            "shipstationServiceCode": "shippo_priority",
            "totalOrders": len(batch),
            "totalWeight": round(total_weight, 2),
            "weightUnit": "kg",
            "estimatedDeliveryDate": (date.today() + timedelta(days=3)).isoformat(),
            "shippingCost": round(random.uniform(10, 50), 2),
            "transitTime": "2-3 business days",
        }
        
        response = self.http.request("POST", "/admin/shipments", payload=payload)
        if response:
            log(f"Created shipment {response.get('shipmentCode')} with {len(batch)} orders")

    def process_shipments(self, max_count: int = 5) -> None:
        # Move shipments: PENDING_ACCEPTANCE -> IN_TRANSIT -> COMPLETED
        shipments = self.http.request("GET", "/admin/shipments")
        if not shipments:
            return

        # 1. Accept pending shipments (up to max_count)
        pending = [s for s in shipments if s.get("status") == "PENDING_ACCEPTANCE"]
        process_count = min(len(pending), max_count)
        for _ in range(process_count):
            if not pending:
                break
            shipment = random.choice(pending)
            pending.remove(shipment)  # Don't process same shipment twice
            # Use our new admin endpoint
            self.http.request("PUT", f"/admin/shipments/{shipment['id']}/status?status=IN_TRANSIT")
            log(f"Accepted shipment {shipment.get('shipmentCode')} (IN_TRANSIT)")

        # 2. Deliver in-transit shipments (up to max_count)
        in_transit = [s for s in shipments if s.get("status") == "IN_TRANSIT"]
        process_count = min(len(in_transit), max_count)
        for _ in range(process_count):
            if not in_transit:
                break
            shipment = random.choice(in_transit)
            in_transit.remove(shipment)  # Don't process same shipment twice
            # Use our new admin endpoint
            self.http.request("PUT", f"/admin/shipments/{shipment['id']}/status?status=COMPLETED")
            log(f"Delivered shipment {shipment.get('shipmentCode')} (COMPLETED)")

# --- Main ---

def main() -> None:
    parser = argparse.ArgumentParser(description="Orchestrate SLMS orders")
    parser.add_argument("--interval", type=int, default=600, help="Loop interval in seconds (default: 10 minutes)")
    parser.add_argument("--api-base", default="http://localhost:8080/api", help="API base URL")
    args = parser.parse_args()

    orchestrator = Orchestrator(args.api_base)
    log(f"Starting orchestration loop (interval={args.interval}s, api={args.api_base})")

    try:
        while True:
            orchestrator.tick()
            time.sleep(args.interval)
    except KeyboardInterrupt:
        log("Stopping orchestration.")

if __name__ == "__main__":
    main()
