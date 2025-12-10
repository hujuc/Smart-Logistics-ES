#!/usr/bin/env python3
"""
Seed script that populates the SLMS backend with demo data:
- Creates Keycloak users (customers, drivers, warehouse staff, CSR, managers)
- Imports carriers via real ShipEngine/Shippo integrations
- Creates warehouses, orders, and shipments using /api/admin endpoints

Requirements:
- Backend + Keycloak + MinIO + Shippo/ShipEngine integrations running (`make up`)
- API base URL accessible from host (defaults to http://localhost:8080/api)
- Python 3.11+ (stdlib only)
"""

from __future__ import annotations

import argparse
import base64
import json
import math
import os
import random
import sys
import time
from dataclasses import dataclass
from datetime import date, timedelta
from pathlib import Path
from typing import Any, Dict, List, Optional
import urllib.error
import urllib.request

ROLE_SEQUENCE = [
    "CUSTOMER",
    "DRIVER",
    "WAREHOUSE_STAFF",
    "CUSTOMER_SERVICE_REP",
    "LOGISTICS_MANAGER",
]

ROLE_SLUGS = {
    "CUSTOMER": "cust",
    "DRIVER": "driver",
    "WAREHOUSE_STAFF": "wh",
    "CUSTOMER_SERVICE_REP": "csr",
    "LOGISTICS_MANAGER": "mgr",
}

ROLE_LABELS = {
    "CUSTOMER": "Customer",
    "DRIVER": "Driver",
    "WAREHOUSE_STAFF": "Warehouse Staff",
    "CUSTOMER_SERVICE_REP": "Customer Service Rep",
    "LOGISTICS_MANAGER": "Logistics Manager",
}

NAME_POOLS = {
    "CUSTOMER": {
        "first": ["Maria", "Ana", "Carla", "Sofia", "Paula", "Laura"],
        "last": ["Silva", "Costa", "Pereira", "Santos", "Matos", "Figueira"],
    },
    "DRIVER": {
        "first": ["Joao", "Miguel", "Rui", "Paulo", "Bruno", "Tiago"],
        "last": ["Ferreira", "Gomes", "Teixeira", "Barros", "Lima", "Antunes"],
    },
    "WAREHOUSE_STAFF": {
        "first": ["Ines", "Beatriz", "Luisa", "Patricia", "Helena", "Daniela"],
        "last": ["Cardoso", "Vieira", "Cabral", "Monteiro", "Serra", "Ramalho"],
    },
    "CUSTOMER_SERVICE_REP": {
        "first": ["Rita", "Daniel", "Lucia", "Andre", "Carolina", "Marta"],
        "last": ["Neves", "Brito", "Campos", "Fraga", "Morais", "Pinto"],
    },
    "LOGISTICS_MANAGER": {
        "first": ["Carlos", "Hugo", "Ricardo", "Vasco", "Leonor", "Patricia"],
        "last": ["Navarro", "Reis", "Cunha", "Magalhaes", "Medeiros", "Andrade"],
    },
}

WAREHOUSE_TEMPLATES = [
    {
        "code_prefix": "MEM",
        "name": "Memphis Air Cargo Hub",
        "addressLine1": "3131 Democrat Rd",
        "addressLine2": None,
        "city": "Memphis",
        "state": "TN",
        "postalCode": "38118",
        "country": "United States",
    },
    {
        "code_prefix": "SDF",
        "name": "Louisville Worldport Gateway",
        "addressLine1": "911 Grade Ln",
        "addressLine2": None,
        "city": "Louisville",
        "state": "KY",
        "postalCode": "40213",
        "country": "United States",
    },
    {
        "code_prefix": "ONT",
        "name": "Ontario Inland Empire Depot",
        "addressLine1": "1120 W Mission Blvd",
        "addressLine2": None,
        "city": "Ontario",
        "state": "CA",
        "postalCode": "91762",
        "country": "United States",
    },
    {
        "code_prefix": "DFW",
        "name": "Dallas-Fort Worth Regional",
        "addressLine1": "2400 Aviation Dr",
        "addressLine2": None,
        "city": "Dallas",
        "state": "TX",
        "postalCode": "75261",
        "country": "United States",
    },
    {
        "code_prefix": "EWR",
        "name": "Newark Gateway Facility",
        "addressLine1": "620 Frelinghuysen Ave",
        "addressLine2": None,
        "city": "Newark",
        "state": "NJ",
        "postalCode": "07114",
        "country": "United States",
    },
    {
        "code_prefix": "ORD",
        "name": "Chicago O'Hare Fulfillment Campus",
        "addressLine1": "2200 E Devon Ave",
        "addressLine2": None,
        "city": "Des Plaines",
        "state": "IL",
        "postalCode": "60018",
        "country": "United States",
    },
    {
        "code_prefix": "MIA",
        "name": "Miami International Logistics",
        "addressLine1": "2903 NW 79th Ave",
        "addressLine2": None,
        "city": "Doral",
        "state": "FL",
        "postalCode": "33122",
        "country": "United States",
    },
    {
        "code_prefix": "DEN",
        "name": "Denver Front Range Center",
        "addressLine1": "11203 E 55th Ave",
        "addressLine2": None,
        "city": "Denver",
        "state": "CO",
        "postalCode": "80239",
        "country": "United States",
    },
]

DESTINATION_TEMPLATES = [
    {
        "contact": "Lisa Thompson",
        "line1": "525 Market St",
        "line2": "Suite 200",
        "city": "San Francisco",
        "state": "CA",
        "postalCode": "94105",
        "country": "United States",
        "phone": "+14155550123",
    },
    {
        "contact": "James Walker",
        "line1": "350 5th Ave",
        "line2": "Floor 45",
        "city": "New York",
        "state": "NY",
        "postalCode": "10118",
        "country": "United States",
        "phone": "+12125551234",
    },
    {
        "contact": "Caroline Diaz",
        "line1": "233 S Wacker Dr",
        "line2": "Unit 4201",
        "city": "Chicago",
        "state": "IL",
        "postalCode": "60606",
        "country": "United States",
        "phone": "+13125559876",
    },
    {
        "contact": "Michael Brown",
        "line1": "6400 Sheridan Blvd",
        "line2": None,
        "city": "Arvada",
        "state": "CO",
        "postalCode": "80003",
        "country": "United States",
        "phone": "+13035554111",
    },
    {
        "contact": "Olivia Patel",
        "line1": "401 Biscayne Blvd",
        "line2": "Apt 12C",
        "city": "Miami",
        "state": "FL",
        "postalCode": "33132",
        "country": "United States",
        "phone": "+13055550102",
    },
    {
        "contact": "Ethan Brooks",
        "line1": "1701 John F Kennedy Blvd",
        "line2": "Suite 300",
        "city": "Philadelphia",
        "state": "PA",
        "postalCode": "19103",
        "country": "United States",
        "phone": "+12155557654",
    },
    {
        "contact": "Sophia Nguyen",
        "line1": "500 S Buena Vista St",
        "line2": "Building 125",
        "city": "Burbank",
        "state": "CA",
        "postalCode": "91521",
        "country": "United States",
        "phone": "+18185550222",
    },
    {
        "contact": "Daniel Harris",
        "line1": "2711 N Haskell Ave",
        "line2": "Suite 2500",
        "city": "Dallas",
        "state": "TX",
        "postalCode": "75204",
        "country": "United States",
        "phone": "+12145559887",
    },
    {
        "contact": "Ava Johnson",
        "line1": "125 Summer St",
        "line2": "Floor 8",
        "city": "Boston",
        "state": "MA",
        "postalCode": "02110",
        "country": "United States",
        "phone": "+16175554400",
    },
    {
        "contact": "Mason Clark",
        "line1": "600 Travis St",
        "line2": "Suite 4200",
        "city": "Houston",
        "state": "TX",
        "postalCode": "77002",
        "country": "United States",
        "phone": "+17135559000",
    },
    {
        "contact": "Emma Scott",
        "line1": "1 Apple Park Way",
        "line2": None,
        "city": "Cupertino",
        "state": "CA",
        "postalCode": "95014",
        "country": "United States",
        "phone": "+14085550199",
    },
    {
        "contact": "Henry Collins",
        "line1": "270 Park Ave",
        "line2": "Floor 35",
        "city": "New York",
        "state": "NY",
        "postalCode": "10017",
        "country": "United States",
        "phone": "+12125557600",
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


def log(message: str) -> None:
    print(f"[seed] {message}")


def warn(message: str) -> None:
    print(f"[seed][warn] {message}")


def error(message: str) -> None:
    print(f"[seed][error] {message}", file=sys.stderr)


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
            raise RuntimeError(f"{method.upper()} {url} failed ({exc.code}): {body or exc.reason}") from exc
        except urllib.error.URLError as exc:
            raise RuntimeError(f"{method.upper()} {url} failed: {exc.reason}") from exc


def load_env_file(path: Path) -> Dict[str, str]:
    if not path.exists():
        return {}
    result: Dict[str, str] = {}
    for raw in path.read_text().splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        cleaned = value.strip().strip('"').strip("'")
        result[key.strip()] = cleaned
    return result


def resolve_api_base(cli_value: Optional[str], env_data: Dict[str, str]) -> str:
    if cli_value:
        return cli_value.rstrip("/")
    env_value = (
        os.environ.get("SLMS_API_BASE_URL")
        or env_data.get("SLMS_API_BASE_URL")
    )
    if env_value:
        return env_value.rstrip("/")
    candidate = os.environ.get("NEXT_PUBLIC_API_URL") or env_data.get("NEXT_PUBLIC_API_URL")
    if candidate:
        candidate = candidate.rstrip("/")
        return candidate if candidate.endswith("/api") else f"{candidate}/api"
    return "http://localhost:8080/api"


def decode_jwt_payload(token: str) -> Dict[str, Any]:
    if not token or "." not in token:
        return {}
    parts = token.split(".")
    if len(parts) < 2:
        return {}
    payload = parts[1]
    padding = "=" * (-len(payload) % 4)
    try:
        decoded = base64.urlsafe_b64decode(payload + padding)
        return json.loads(decoded.decode("utf-8"))
    except Exception:
        return {}


@dataclass
class SeedConfig:
    api_base_url: str
    warehouse_count: int
    order_count: int
    shipment_count: int
    user_count: int
    seed: Optional[int]


class Seeder:
    def __init__(self, config: SeedConfig) -> None:
        self.config = config
        self.http = HttpClient(config.api_base_url)
        self.random = random.Random(config.seed or time.time())
        self.role_counters = {role: 0 for role in ROLE_SEQUENCE}
        self.timestamp = int(time.time())

    def run(self) -> Dict[str, Any]:
        carriers = self.ensure_carriers()
        users_by_role = self.create_users(self.config.user_count)
        self.ensure_role_presence(users_by_role, "CUSTOMER")
        self.ensure_role_presence(users_by_role, "DRIVER")
        warehouses = self.create_warehouses(self.config.warehouse_count)
        orders = self.create_orders(
            self.config.order_count,
            warehouses,
            users_by_role.get("CUSTOMER", []),
        )
        shipments = self.create_shipments(
            self.config.shipment_count,
            carriers,
            orders,
            users_by_role.get("DRIVER", []),
        )
        return {
            "carriers": carriers,
            "users": users_by_role,
            "warehouses": warehouses,
            "orders": orders,
            "shipments": shipments,
            "api_base_url": self.config.api_base_url,
        }

    def ensure_carriers(self) -> List[Dict[str, Any]]:
        carriers = self.http.request("GET", "/admin/carriers") or []
        if carriers:
            log(f"Carriers already present ({len(carriers)} records)")
            return carriers

        log("No carriers found. Triggering synchronization with ShipEngine/Shippo ...")
        self.http.request("POST", "/admin/carriers/integrations/sync", payload={})
        time.sleep(2)
        carriers = self.http.request("GET", "/admin/carriers") or []
        if not carriers:
            raise RuntimeError(
                "Carrier sync returned no records. Check ShipEngine/Shippo API keys in infra/.env."
            )
        log(f"Synchronized {len(carriers)} carriers from external APIs")
        return carriers

    def create_users(self, count: int) -> Dict[str, List[Dict[str, Any]]]:
        results: Dict[str, List[Dict[str, Any]]] = {role: [] for role in ROLE_SEQUENCE}
        if count <= 0:
            warn("Skipping user creation (count=0)")
            return results

        log(f"Creating {count} Keycloak demo users via /api/landing-page/auth/register")
        for index in range(count):
            role = ROLE_SEQUENCE[index % len(ROLE_SEQUENCE)]
            user = self._create_user(role)
            results[role].append(user)
            time.sleep(0.2)
        return results

    def ensure_role_presence(
        self,
        users_by_role: Dict[str, List[Dict[str, Any]]],
        role: str,
    ) -> None:
        if users_by_role.get(role):
            return
        warn(f"No {ROLE_LABELS.get(role, role)} accounts were created automatically. Generating one now.")
        user = self._create_user(role)
        users_by_role.setdefault(role, []).append(user)

    def _create_user(self, role: str) -> Dict[str, Any]:
        role_meta = NAME_POOLS.get(role)
        if not role_meta:
            raise RuntimeError(f"Unsupported role {role}")
        counter = self.role_counters[role]
        self.role_counters[role] += 1
        username = f"{ROLE_SLUGS[role]}-{self.timestamp % 10000:04d}-{counter:02d}-{self._random_token(3).lower()}"
        first_name = self.random.choice(role_meta["first"])
        last_name = self.random.choice(role_meta["last"])
        email = f"{username}@demo.slms"
        password = "DemoPass!23"

        payload = {
            "username": username,
            "email": email,
            "password": password,
            "firstName": first_name,
            "lastName": last_name,
            "roles": [role],
        }
        response = self.http.request("POST", "/landing-page/auth/register", payload=payload)
        access_token = response.get("accessToken")
        jwt_payload = decode_jwt_payload(access_token)
        user_id = jwt_payload.get("sub") or f"unknown-{username}"
        log(f"  + Created {ROLE_LABELS.get(role, role)} user '{username}' (id={user_id})")
        return {
            "id": user_id,
            "username": username,
            "email": email,
            "role": role,
            "password": password,
        }

    def create_warehouses(self, count: int) -> List[Dict[str, Any]]:
        if count <= 0:
            warn("Skipping warehouse creation (count=0)")
            return []

        log(f"Creating {count} warehouses via /api/admin/warehouses")
        existing = self.http.request("GET", "/admin/warehouses") or []
        existing_codes = {wh.get("code", "").upper() for wh in existing}
        created: List[Dict[str, Any]] = []

        for index in range(count):
            template = WAREHOUSE_TEMPLATES[index % len(WAREHOUSE_TEMPLATES)]
            code = self._generate_unique_code(
                prefix=f"WH-{template['code_prefix']}",
                existing=existing_codes,
            )
            payload = {
                "code": code,
                "name": f"{template['name']} #{index + 1}",
                "addressLine1": template["addressLine1"],
                "addressLine2": template.get("addressLine2"),
                "city": template["city"],
                "stateOrProvince": template["state"],
                "country": template["country"],
                "postalCode": template["postalCode"],
                "active": True,
                "shippoLocationToken": f"shippo_{code.lower()}",
                "shipstationWarehouseId": f"ss_{code.lower()}",
            }
            response = self.http.request("POST", "/admin/warehouses", payload=payload)
            created.append(response)
            log(f"  + Warehouse {response.get('code')} (id={response.get('id')})")
        return created

    def create_orders(
        self,
        count: int,
        warehouses: List[Dict[str, Any]],
        customers: List[Dict[str, Any]],
    ) -> List[Dict[str, Any]]:
        if count <= 0:
            warn("Skipping order creation (count=0)")
            return []
        if not warehouses:
            warn("No warehouses available. Cannot create orders.")
            return []

        log(f"Creating {count} orders via /api/admin/orders")
        orders: List[Dict[str, Any]] = []
        today = date.today()

        for index in range(count):
            warehouse = self.random.choice(warehouses)
            destination = DESTINATION_TEMPLATES[index % len(DESTINATION_TEMPLATES)]
            customer = customers[index % len(customers)] if customers else None
            reference = f"ORD-{self.timestamp % 100000:05d}-{index + 1:03d}"
            tracking_number = f"TRK{self.timestamp % 100000:05d}{index:03d}"
            estimated_delivery = today + timedelta(days=self.random.randint(2, 9))
            weight = round(self.random.uniform(1.0, 12.0), 2)
            length = round(self.random.uniform(20, 60), 1)
            width = round(self.random.uniform(15, 40), 1)
            height = round(self.random.uniform(8, 25), 1)

            shipping_address = f"{destination['contact']}, {destination['line1']}"
            if destination.get("line2"):
                shipping_address += f", {destination['line2']}"
            shipping_address += f", {destination['postalCode']} {destination['city']}, {destination['country']}"

            payload = {
                "reference": reference,
                "description": f"Demo order #{index + 1} headed to {destination['city']}",
                "status": "CREATED",
                "shippingAddress": shipping_address,
                "shippingAddressLine1": destination["line1"],
                "shippingAddressLine2": destination.get("line2"),
                "shippingCity": destination["city"],
                "shippingStateOrProvince": destination["state"],
                "shippingPostalCode": destination["postalCode"],
                "shippingCountry": destination["country"],
                "shippingPhone": destination["phone"],
                "shippingEmail": customer["email"] if customer else f"customer{index}@demo.slms",
                "estimatedDeliveryDate": estimated_delivery.isoformat(),
                "warehouseId": warehouse["id"],
                "customerId": customer["id"] if customer else None,
                "trackingNumber": tracking_number,
                "weight": weight,
                "weightUnit": "kg",
                "length": length,
                "width": width,
                "height": height,
                "distanceUnit": "cm",
            }
            response = self.http.request("POST", "/admin/orders", payload=payload)
            orders.append({
                "id": response.get("id"),
                "reference": response.get("reference"),
                "weight": weight,
                "weightUnit": "kg",
                "warehouseId": warehouse["id"],
            })
            log(f"  + Order {response.get('reference')} (id={response.get('id')}) staged at warehouse {warehouse.get('code')}")
        return orders

    def create_shipments(
        self,
        requested: int,
        carriers: List[Dict[str, Any]],
        orders: List[Dict[str, Any]],
        drivers: List[Dict[str, Any]],
    ) -> List[Dict[str, Any]]:
        if requested <= 0:
            warn("Skipping shipment creation (count=0)")
            return []
        if not orders:
            warn("No orders available to create shipments.")
            return []
        if not carriers:
            warn("No carriers available. Cannot create shipments.")
            return []
        if not drivers:
            warn("No driver accounts available. Cannot create shipments.")
            return []

        target_count = min(requested, len(orders))
        if target_count < requested:
            warn(f"Requested {requested} shipments but only {len(orders)} orders are available. Creating {target_count} shipments.")

        log(f"Creating {target_count} shipments via /api/admin/shipments")
        shipments: List[Dict[str, Any]] = []
        order_queue = orders.copy()

        for index in range(target_count):
            if not order_queue:
                break
            remaining_shipments = target_count - index
            batch_size = max(1, math.ceil(len(order_queue) / remaining_shipments))
            batch: List[Dict[str, Any]] = []
            for _ in range(min(batch_size, len(order_queue))):
                batch.append(order_queue.pop(0))

            carrier = carriers[index % len(carriers)]
            driver = drivers[index % len(drivers)]
            shipment_code = f"SHP-{self.timestamp % 100000:05d}-{index + 1:03d}"
            total_weight = round(sum(item.get("weight", 0.0) for item in batch), 2)
            estimated_delivery = (date.today() + timedelta(days=self.random.randint(3, 10))).isoformat()

            payload = {
                "shipmentCode": shipment_code,
                "carrierId": carrier["id"],
                "driverId": driver["id"],
                "orderIds": [item["id"] for item in batch],
                "shippoShipmentId": f"shippo_{shipment_code.lower()}",
                "shippoTransactionId": f"txn_{self._random_token(8).lower()}",
                "shipstationOrderId": f"ss_{shipment_code.lower()}",
                "shipstationCarrierCode": carrier.get("code"),
                "shipstationServiceCode": self.random.choice(SERVICE_LEVEL_CODES),
                "totalOrders": len(batch),
                "totalWeight": total_weight,
                "weightUnit": "kg",
                "estimatedDeliveryDate": estimated_delivery,
                "shippingCost": round(self.random.uniform(12, 65), 2),
                "transitTime": self.random.choice(TRANSIT_TIMES),
            }
            response = self.http.request("POST", "/admin/shipments", payload=payload)
            shipments.append(response)
            log(f"  + Shipment {response.get('shipmentCode')} carrier={carrier.get('code')} driver={driver['username']} orders={len(batch)}")
        return shipments

    def _generate_unique_code(self, prefix: str, existing: set[str]) -> str:
        while True:
            code = f"{prefix}-{self.timestamp % 1000:03d}-{self._random_token(3)}"
            if code.upper() not in existing:
                existing.add(code.upper())
                return code

    def _random_token(self, length: int) -> str:
        alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        return "".join(self.random.choice(alphabet) for _ in range(length))


def parse_args() -> argparse.Namespace:
    default_env = Path(__file__).resolve().parent.parent / ".env"
    parser = argparse.ArgumentParser(
        description="Populate the SLMS backend with demo data (warehouses, orders, shipments, users).",
    )
    parser.add_argument("--env-file", default=str(default_env), help="Path to infra/.env (default: %(default)s)")
    parser.add_argument("--api-base-url", help="Override API base URL (e.g. http://localhost:8080/api)")
    parser.add_argument("--warehouses", type=int, default=10, help="Number of warehouses to create (default: %(default)s)")
    parser.add_argument("--orders", type=int, default=10, help="Number of orders to create (default: %(default)s)")
    parser.add_argument("--shipments", type=int, default=10, help="Number of shipments to create (default: %(default)s)")
    parser.add_argument("--users", type=int, default=10, help="Total number of demo users to create (default: %(default)s)")
    parser.add_argument("--seed", type=int, help="Optional RNG seed for reproducible data")
    return parser.parse_args()


def print_summary(summary: Dict[str, Any]) -> None:
    print("\n=== Demo data summary ===")
    print(f"API base URL       : {summary['api_base_url']}")
    print(f"Carriers available : {len(summary['carriers'])}")
    print(f"Warehouses created : {len(summary['warehouses'])}")
    print(f"Orders created     : {len(summary['orders'])}")
    print(f"Shipments created  : {len(summary['shipments'])}")

    print("\nUsers created:")
    users = summary["users"]
    for role in ROLE_SEQUENCE:
        role_users = users.get(role, [])
        if not role_users:
            continue
        print(f"  {ROLE_LABELS.get(role, role)} ({len(role_users)}):")
        for user in role_users:
            print(f"    - {user['username']} / {user['password']} (id={user['id']})")

    print("\nWarehouses:")
    for wh in summary["warehouses"]:
        print(f"  - {wh.get('code')} (id={wh.get('id')}), {wh.get('city')}, {wh.get('country')}")

    print("\nOrders:")
    for order in summary["orders"]:
        print(f"  - {order['reference']} (id={order['id']})")

    if summary["shipments"]:
        print("\nShipments:")
        for shipment in summary["shipments"]:
            print(
                f"  - {shipment.get('shipmentCode')} (carrier {shipment.get('carrierCode')}, id={shipment.get('id')})"
            )

    print("\nReminder: shipments trigger Shippo label generation. Check backend logs and MinIO for generated documents.")


def main() -> None:
    args = parse_args()
    env_file = Path(args.env_file)
    env_data = load_env_file(env_file) if env_file.exists() else {}
    api_base = resolve_api_base(args.api_base_url, env_data)
    log(f"Using API base URL: {api_base}")

    config = SeedConfig(
        api_base_url=api_base,
        warehouse_count=args.warehouses,
        order_count=args.orders,
        shipment_count=args.shipments,
        user_count=args.users,
        seed=args.seed,
    )

    seeder = Seeder(config)
    summary = seeder.run()
    print_summary(summary)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        error("Seed script cancelled by user.")
        sys.exit(1)
    except Exception as exc:
        error(str(exc))
        sys.exit(1)
