#!/bin/bash

# Demo Order Creation Script
# Creates multiple orders with all fields populated
# All destinations near Mountain View, CA

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost/api"
KEYCLOAK_URL="http://localhost/realms/slms"

echo -e "${BLUE}=== SLMS Demo Order Creation ===${NC}"
echo ""

# Step 1: Get authentication token
echo -e "${BLUE}Step 1: Authenticating...${NC}"
TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=slms-backend" \
  -d "client_secret=51UZ3VoR3RRPMHIQ2uNLdejzTkzkmei9" \
  -d "username=henrique_warehouse" \
  -d "password=1234" \
  -d "grant_type=password")

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "Failed to get access token"
    exit 1
fi

echo -e "${GREEN}✓ Authentication successful${NC}"
echo ""

# Generate unique timestamp suffix for this run
TIMESTAMP=$(date +%s)
echo -e "${BLUE}Using timestamp: ${TIMESTAMP}${NC}"
echo ""

# Array of complete orders with all fields filled
declare -a ORDERS=(
# Order 1: Electronics - Google Office Reception
'{"reference":"DEMO-ELEC-'${TIMESTAMP}'-001","description":"High-priority electronics shipment - MacBook Pro 16inch, iPad Pro, and accessories for Google Workspace team","status":"CREATED","shippingAddress":"1600 Amphitheatre Parkway, Building 41, Mountain View, CA, 94043, US","shippingAddressLine1":"1600 Amphitheatre Parkway","shippingAddressLine2":"Building 41, Reception Desk","shippingCity":"Mountain View","shippingStateOrProvince":"CA","shippingPostalCode":"94043","shippingCountry":"US","shippingPhone":"+1-650-253-0000","shippingEmail":"reception@google.example.com","estimatedDeliveryDate":"2025-12-20","warehouseId":1,"customerId":"customer-google-001","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-001","weight":15.5,"weightUnit":"lb","length":24.0,"width":18.0,"height":12.0,"distanceUnit":"in"}'

# Order 2: Documents - Nearby Sunnyvale Office
'{"reference":"DEMO-DOC-'${TIMESTAMP}'-002","description":"Confidential legal documents and contracts requiring signature - Time sensitive delivery","status":"CREATED","shippingAddress":"1255 Bordeaux Drive, Suite 200, Sunnyvale, CA, 94089, US","shippingAddressLine1":"1255 Bordeaux Drive","shippingAddressLine2":"Suite 200, Legal Department","shippingCity":"Sunnyvale","shippingStateOrProvince":"CA","shippingPostalCode":"94089","shippingCountry":"US","shippingPhone":"+1-408-555-0123","shippingEmail":"legal@acmecorp.example.com","estimatedDeliveryDate":"2025-12-19","warehouseId":1,"customerId":"customer-acme-002","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-002","weight":2.3,"weightUnit":"lb","length":12.0,"width":9.0,"height":2.0,"distanceUnit":"in"}'

# Order 3: Medical Supplies - Palo Alto Hospital
'{"reference":"DEMO-MED-'${TIMESTAMP}'-003","description":"Urgent medical supplies - Diagnostic equipment and sterile surgical instruments for Stanford Health Care","status":"READY_FOR_PICKUP","shippingAddress":"875 Blake Wilbur Drive, Palo Alto, CA, 94304, US","shippingAddressLine1":"875 Blake Wilbur Drive","shippingAddressLine2":"Stanford Health Care - Loading Dock 3","shippingCity":"Palo Alto","shippingStateOrProvince":"CA","shippingPostalCode":"94304","shippingCountry":"US","shippingPhone":"+1-650-498-9000","shippingEmail":"logistics@stanfordhealthcare.example.org","estimatedDeliveryDate":"2025-12-18","warehouseId":1,"customerId":"customer-stanford-003","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-003","weight":28.7,"weightUnit":"lb","length":36.0,"width":24.0,"height":18.0,"distanceUnit":"in"}'

# Order 4: Office Furniture - Mountain View Startup
'{"reference":"DEMO-FURN-'${TIMESTAMP}'-004","description":"Modern office furniture - Standing desks, ergonomic chairs, and collaboration pod for TechVenture startup","status":"CREATED","shippingAddress":"2000 Sierra Point Parkway, Unit 450, Mountain View, CA, 94043, US","shippingAddressLine1":"2000 Sierra Point Parkway","shippingAddressLine2":"Unit 450, 4th Floor","shippingCity":"Mountain View","shippingStateOrProvince":"CA","shippingPostalCode":"94043","shippingCountry":"US","shippingPhone":"+1-650-555-7890","shippingEmail":"office@techventure.example.com","estimatedDeliveryDate":"2025-12-22","warehouseId":1,"customerId":"customer-techventure-004","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-004","weight":185.0,"weightUnit":"lb","length":72.0,"width":48.0,"height":36.0,"distanceUnit":"in"}'

# Order 5: Perishable Goods - Los Altos Restaurant
'{"reference":"DEMO-FOOD-'${TIMESTAMP}'-005","description":"Fresh organic produce and specialty ingredients - Premium imported cheeses, truffles, and seasonal vegetables","status":"READY_FOR_PICKUP","shippingAddress":"288 Main Street, Los Altos, CA, 94022, US","shippingAddressLine1":"288 Main Street","shippingAddressLine2":"Restaurant Back Entrance","shippingCity":"Los Altos","shippingStateOrProvince":"CA","shippingPostalCode":"94022","shippingCountry":"US","shippingPhone":"+1-650-555-2468","shippingEmail":"chef@finedining.example.com","estimatedDeliveryDate":"2025-12-17","warehouseId":1,"customerId":"customer-restaurant-005","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-005","weight":42.5,"weightUnit":"lb","length":24.0,"width":20.0,"height":16.0,"distanceUnit":"in"}'

# Order 6: Books and Educational Materials - Mountain View Library
'{"reference":"DEMO-BOOK-'${TIMESTAMP}'-006","description":"Educational materials shipment - Technical books, e-readers, and learning kits for community library program","status":"CREATED","shippingAddress":"585 Franklin Street, Mountain View, CA, 94041, US","shippingAddressLine1":"585 Franklin Street","shippingAddressLine2":"Mountain View Public Library - Receiving","shippingCity":"Mountain View","shippingStateOrProvince":"CA","shippingPostalCode":"94041","shippingCountry":"US","shippingPhone":"+1-650-903-6337","shippingEmail":"acquisitions@mountainviewlibrary.example.org","estimatedDeliveryDate":"2025-12-21","warehouseId":1,"customerId":"customer-library-006","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-006","weight":67.8,"weightUnit":"lb","length":30.0,"width":24.0,"height":20.0,"distanceUnit":"in"}'

# Order 7: Lab Equipment - Stanford Research Lab
'{"reference":"DEMO-LAB-'${TIMESTAMP}'-007","description":"Scientific research equipment - High-precision microscopes, spectrophotometer, and laboratory glassware","status":"CREATED","shippingAddress":"318 Campus Drive, Stanford, CA, 94305, US","shippingAddressLine1":"318 Campus Drive","shippingAddressLine2":"Clark Center, Room S371","shippingCity":"Stanford","shippingStateOrProvince":"CA","shippingPostalCode":"94305","shippingCountry":"US","shippingPhone":"+1-650-723-2300","shippingEmail":"labmanager@stanford.example.edu","estimatedDeliveryDate":"2025-12-23","warehouseId":1,"customerId":"customer-stanford-007","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-007","weight":94.2,"weightUnit":"lb","length":42.0,"width":32.0,"height":28.0,"distanceUnit":"in"}'

# Order 8: Fashion Retail - Sunnyvale Boutique
'{"reference":"DEMO-FASH-'${TIMESTAMP}'-008","description":"Spring collection preview - Designer clothing, accessories, and premium footwear from European fashion houses","status":"READY_FOR_PICKUP","shippingAddress":"665 East El Camino Real, Sunnyvale, CA, 94087, US","shippingAddressLine1":"665 East El Camino Real","shippingAddressLine2":"Boutique Moderne - Service Entrance","shippingCity":"Sunnyvale","shippingStateOrProvince":"CA","shippingPostalCode":"94087","shippingCountry":"US","shippingPhone":"+1-408-555-3690","shippingEmail":"inventory@boutiquemodern.example.com","estimatedDeliveryDate":"2025-12-20","warehouseId":1,"customerId":"customer-boutique-008","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-008","weight":38.4,"weightUnit":"lb","length":36.0,"width":24.0,"height":12.0,"distanceUnit":"in"}'

# Order 9: Sporting Goods - Mountain View Sports Center
'{"reference":"DEMO-SPORT-'${TIMESTAMP}'-009","description":"Athletic equipment order - Professional grade fitness machines, yoga mats, weights, and training accessories","status":"CREATED","shippingAddress":"3700 Middlefield Road, Mountain View, CA, 94043, US","shippingAddressLine1":"3700 Middlefield Road","shippingAddressLine2":"Mountain View Sports Center - Equipment Room","shippingCity":"Mountain View","shippingStateOrProvince":"CA","shippingPostalCode":"94043","shippingCountry":"US","shippingPhone":"+1-650-555-8901","shippingEmail":"equipment@mvsports.example.com","estimatedDeliveryDate":"2025-12-24","warehouseId":1,"customerId":"customer-sports-009","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-009","weight":215.6,"weightUnit":"lb","length":84.0,"width":48.0,"height":42.0,"distanceUnit":"in"}'

# Order 10: Art Supplies - Palo Alto Art Gallery
'{"reference":"DEMO-ART-'${TIMESTAMP}'-010","description":"Fine art materials - Professional canvases, oil paints, sculpture tools, and gallery display equipment","status":"CREATED","shippingAddress":"169 University Avenue, Palo Alto, CA, 94301, US","shippingAddressLine1":"169 University Avenue","shippingAddressLine2":"Gallery Moderne - Rear Loading Bay","shippingCity":"Palo Alto","shippingStateOrProvince":"CA","shippingPostalCode":"94301","shippingCountry":"US","shippingPhone":"+1-650-555-4567","shippingEmail":"curator@gallerymoderne.example.com","estimatedDeliveryDate":"2025-12-19","warehouseId":1,"customerId":"customer-gallery-010","trackingNumber":"TRK-DEMO-'${TIMESTAMP}'-010","weight":52.3,"weightUnit":"lb","length":48.0,"width":36.0,"height":24.0,"distanceUnit":"in"}'
)

# Create each order
echo -e "${BLUE}Step 2: Creating demo orders...${NC}"
echo ""

ORDER_COUNT=1
for ORDER_JSON in "${ORDERS[@]}"; do
    echo -e "${BLUE}Creating order ${ORDER_COUNT}/10...${NC}"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/admin/orders" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${ACCESS_TOKEN}" \
      -d "${ORDER_JSON}")

    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" == "201" ]; then
        ORDER_ID=$(echo $BODY | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        REFERENCE=$(echo $BODY | grep -o '"reference":"[^"]*' | head -1 | cut -d'"' -f4)
        echo -e "${GREEN}✓ Order created: ID=${ORDER_ID}, Reference=${REFERENCE}${NC}"
    else
        echo -e "✗ Failed to create order (HTTP ${HTTP_CODE})"
        echo "Response: $BODY"
    fi

    ORDER_COUNT=$((ORDER_COUNT + 1))
    echo ""
done

echo -e "${GREEN}=== Demo order creation complete! ===${NC}"
echo ""
echo "Summary:"
echo "- 10 complete orders created"
echo "- All with full address details near Mountain View, CA"
echo "- Mix of different product types and priorities"
echo "- All fields populated including dimensions and contact info"
echo ""
echo "You can view these orders at: http://localhost/admin"
