

## Useful endpoints (short list)
- `GET /v1/carriers` — list connected carriers and services.  
- `POST /v1/rates/estimate` — quick price estimate for checkout.  
- `POST /v1/rates` — full rate calculation (more options).  
- `POST /v1/shipments` — create a shipment record.  
- `POST /v1/labels` — buy / generate a label (returns tracking and label URL).  
- `POST /v1/tracking/start` — register a tracking number to get updates.  
- `POST /v1/environments/webhooks` — register webhooks for events (track.updated etc.).  
- `POST /v1/addresses/validate` — validate or clean addresses.  
- `POST /v1/labels/{id}/void` — void an existing label.

---

## Examples

### List carriers
```bash
curl -X GET "https://api.shipengine.com/v1/carriers"   -H "API-Key: $SHIPENGINE_API_KEY"   -H "Content-Type: application/json"
```

### Quick estimate (PT → ES)
```bash
curl -X POST "https://api.shipengine.com/v1/rates/estimate"   -H "API-Key: $SHIPENGINE_API_KEY"   -H "Content-Type: application/json"   -d '{
    "shipment": {
      "ship_from": {"postal_code":"4000","country_code":"PT"},
      "ship_to": {"postal_code":"28001","country_code":"ES"},
      "packages":[{"weight":{"value":2,"unit":"kg"},"dimensions":{"length":30,"width":20,"height":10}}]
    }
  }'
```

### Create shipment
```bash
curl -X POST "https://api.shipengine.com/v1/shipments"   -H "API-Key: $SHIPENGINE_API_KEY"   -H "Content-Type: application/json"   -d '{
    "shipment": {
      "validate_address": "no_validation",
      "ship_from": {"name":"Shipper","postal_code":"4000","country_code":"PT","address_line1":"Rua A 1","city_locality":"Porto"},
      "ship_to": {"name":"Receiver","postal_code":"28001","country_code":"ES","address_line1":"Calle B 1","city_locality":"Madrid"},
      "packages":[{"weight":{"value":2,"unit":"kg"},"dimensions":{"length":30,"width":20,"height":10}}],
      "external_shipment_id":"ORDER-1234"
    }
  }'
```

### Buy a label
```bash
curl -X POST "https://api.shipengine.com/v1/labels"   -H "API-Key: $SHIPENGINE_API_KEY"   -H "Content-Type: application/json"   -d '{
    "label": {
      "shipment_id": "{SHIPMENT_ID}",
      "carrier_id": "se-338908",
      "service_code": "usps_priority_mail",
      "package": {"weight": {"value":2,"unit":"kg"}}
    }
  }'
```

### Start tracking a number
```bash
curl -X POST "https://api.shipengine.com/v1/tracking/start"   -H "API-Key: $SHIPENGINE_API_KEY"   -H "Content-Type: application/json"   -d '{"carrier_code":"stamps_com","tracking_number":"EZ1000000001"}'
```

### Create a webhook for tracking updates
```bash
curl -X POST "https://api.shipengine.com/v1/environments/webhooks"   -H "API-Key: $SHIPENGINE_API_KEY"   -H "Content-Type: application/json"   -d '{"url":"https://your-domain.example/webhooks/shipengine","event":"track.updated"}'
```

