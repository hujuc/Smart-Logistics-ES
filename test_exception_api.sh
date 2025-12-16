#!/bin/bash

# Get token
TOKEN=$(curl -s http://localhost/realms/slms/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=slms-backend" \
  -d "client_secret=51UZ3VoR3RRPMHIQ2uNLdejzTkzkmei9" \
  -d "username=ric_driver" \
  -d "password=1234" \
  -d "grant_type=password" | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])")

echo "Got token: ${TOKEN:0:50}..."

# Test exception endpoint
curl -i -X POST "http://localhost/api/driver/orders/104/exception" \
  -H "Authorization: Bearer $TOKEN" \
  -F "exceptionType=NOT_DELIVERED" \
  -F "description=Test exception"
