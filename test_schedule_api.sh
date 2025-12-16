#!/bin/bash

# Get token
TOKEN=$(curl -s http://localhost/realms/slms/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=slms-backend" \
  -d "client_secret=51UZ3VoR3RRPMHIQ2uNLdejzTkzkmei9" \
  -d "username=ric_driver" \
  -d "password=1234" \
  -d "grant_type=password" | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])")

# Get schedule
curl -s "http://localhost/api/driver/schedule" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
