# Carrier Module & Integrations

This backend now keeps a `carriers` table, links orders to carriers, and can pull carrier metadata from the Shippo and ShipEngine sandbox APIs.

## Environment Variables

Set these in `.env`, `src/backend/.env`, or `infra/.env`:

- `SHIPPO_API_KEY` – Shippo sandbox token (format `shippo_test_...`).
- `SHIPPO_BASE_URL` – optional, defaults to `https://api.goshippo.com`.
- `SHIPENGINE_API_KEY` – ShipEngine sandbox key (format `TEST_...`).
- `SANDBOX_API_KEY` – optional fallback for ShipEngine if you prefer the original name.
- `SHIPENGINE_BASE_URL` – optional, defaults to `https://api.shipengine.com/v1`.

## Carrier Endpoints

All endpoints live under `/api/admin/carriers`.

- `GET /` – list carriers stored in the database.
- `GET /{id}` – fetch one carrier by id.
- `POST /` – create a carrier. Body accepts code, name, optional contacts, address, Shippo and ShipEngine ids.
- `PUT /{id}` – update an existing carrier.
- `DELETE /{id}` – remove a carrier.
- `GET /integrations/snapshot` – call Shippo+ShipEngine with the configured sandbox keys and return the merged view without storing anything.
- `POST /integrations/sync` – call both APIs, upsert carriers in the local table, and return the saved records.

All carrier payloads use the same shape:

```jsonc
{
  "code": "DHL",
  "name": "DHL Express",
  "description": "International express carrier",
  "supportEmail": "support@dhl.com",
  "supportPhone": "+49 228 4333112",
  "website": "https://www.dhl.com",
  "serviceLevelsSummary": "Express Worldwide, Economy Select",
  "addressLine1": "Charles-de-Gaulle-Str. 20",
  "city": "Bonn",
  "stateOrProvince": "NRW",
  "postalCode": "53113",
  "country": "DE",
  "shippoCarrierId": "shippo-object-id",
  "shippoAccountId": "shippo-account",
  "shipEngineCarrierId": "se-carrier-id",
  "shipEngineAccountNumber": "se-account"
}
```

## Orders With Carriers

`ShipmentOrder` now has an optional `carrierId` field:

- `POST /api/admin/orders`
- `PUT /api/admin/orders/{id}`

Include `carrierId` in the request body to link an order to a carrier.

```jsonc
{
  "reference": "ORD-001",
  "shippingAddress": "Rua do Cliente 42, Porto",
  "warehouseId": 1,
  "carrierId": 3,
  "estimatedDeliveryDate": "2024-12-01"
}
```

Responses now add `carrierId`, `carrierCode`, and `carrierName` so the UI can present the chosen carrier.

## Typical Workflow

1. Set the Shippo and ShipEngine sandbox keys in your environment.
2. Run the backend.
3. `GET /api/admin/carriers/integrations/snapshot` to see what the APIs return.
4. `POST /api/admin/carriers/integrations/sync` to upsert those carriers locally.
5. Create orders referencing `carrierId`.

The tests cover carrier CRUD, order–carrier linking, and the merge logic for the third‑party clients.
