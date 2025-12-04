# User Stories Update - Sprint 6

## Summary of Changes

This document reflects adjustments made to the user stories for Sprint 6 based on time constraints and implemented features that were missing documentation.

### Changes Made

1. **Removed US16** - "Real-time order tracking" (21 story points)
   - Reason: Out of scope due to time constraints
   - The customer portal provides tracking functionality, but full real-time tracking with live location updates was deemed too complex for the current timeline

2. **Renumbered User Stories**
   - US17 → US16: "Receiving automatic notifications" (13 points)
   - US18 → US17: "Reporting a delivery issue" (8 points)

3. **Added US18** - "AI Assistant for contextual help" (13 story points)
   - Reason: Chatbot functionality is already implemented (Sprint 4) but was missing formal user story documentation
   - Adds comprehensive AI assistance across all user roles and frontends

4. **Removed Keycloak registration confirmation email** from US14 acceptance criteria
   - Reason: Handled by Keycloak, out of scope for SLMS user stories

### Impact
- **Net stories:** 18 (removed 1, added 1)
- **Net story points:** -8 (removed 21, added 13)
- **Status:** 12 completed, 5 in progress, 1 new (chatbot documentation)

---

## Updated User Stories List

### João Ferreira (Logistics Manager)

#### US1 – "Daily monitoring of KPIs" (8) ✅ COMPLETED
As a Logistics Manager I want to access a dashboard with transportation KPIs so that I can monitor performance.

**Acceptance Criteria**
- **View daily KPIs**
  - *Given* that I am logged into the SLMS as a Logistics Manager
  - *When* I open the dashboard
  - *Then* the system must show me KPIs such as on-time delivery rate, shipping costs and carrier performance

#### US2 – "Carrier comparison for new shipments" (13) ✅ COMPLETED
As a Logistics Manager I want to compare available carriers so that I can select the best option for new shipments.

**Acceptance Criteria**
- **Access carrier comparison**
  - *Given* that I am logged into the SLMS as a Logistics Manager
  - *When* I open the carrier integration module
  - *Then* the system must display shipping costs and transit times for each carrier

- **Select the optimal carrier**
  - *Given* that I am reviewing the carrier list
  - *When* I choose one carrier
  - *Then* the system must assign that carrier to the shipment

#### US3 – "Generating performance reports" (8) ✅ COMPLETED
As a Logistics Manager I want to export reports so that I can share logistics performance with management.

**Acceptance Criteria**
- **Generate performance reports**
  - *Given* that I am logged into the SLMS as a Logistics Manager
  - *When* I select the reporting option
  - *Then* the system must generate detailed reports with costs, on-time delivery rate and carrier performance

- **Export reports**
  - *Given* that the report is generated
  - *When* I click on export
  - *Then* the system must allow me to download it in formats such as PDF or Excel

---

### Ana Martins (Warehouse Staff)

#### US4 – "Processing a new order" (5) ✅ COMPLETED
As Warehouse Staff I want to register new orders in the system so that I can prepare them for shipment.

**Acceptance Criteria**
- **Register order details**
  - *Given* that a new order arrives at the warehouse
  - *When* I enter order information (destination, weight, dimensions, carrier) into the SLMS
  - *Then* the system must save the order as ready for shipment

#### US5 – "Generating shipping labels and documents" (3) ✅ COMPLETED
As Warehouse Staff I want to generate labels and shipping documents so that I ensure each order is ready for dispatch.

**Acceptance Criteria**
- **Generate labels and documents**
  - *Given* that I am logged into the SLMS as Warehouse Staff
  - *When* I select an order
  - *Then* the system must generate the shipping label, packing slip and shipping manifest for the carrier

#### US6 – "Confirming carrier pickup" (3) ✅ COMPLETED
As Warehouse Staff I want to confirm carrier pickups in order to keep the system updated.

**Acceptance Criteria**
- **Confirm pickup**
  - *Given* that the carrier has collected the packages
  - *When* I update the SLMS
  - *Then* the system must mark the orders as "collected"

---

### Rita Lopes (Customer Service Representative)

#### US7 – "Quick order status lookup" (2) ✅ COMPLETED
As a Customer Service Representative I want to check the status of an order so that I can provide fast answers to customers.

**Acceptance Criteria**
- **Search for order status**
  - *Given* that a customer contacts support
  - *When* I search the order number in the SLMS
  - *Then* the system must display the latest real-time status

#### US8 – "Resolving a delivery issue" (8) ✅ COMPLETED
As a Customer Service Representative I want to view delivery exceptions so that I can provide clear explanations and resolutions to customers.

**Acceptance Criteria**
- **Identify delivery issues**
  - *Given* that I am logged into the SLMS as Customer Service
  - *When* I search for an order with a reported exception
  - *Then* the system must display the exception details (e.g. recipient absent, incorrect address, carrier delay)

- **Provide resolution to customer**
  - *Given* that I am handling a customer call about an exception
  - *When* I explain the details and propose a solution
  - *Then* the customer must receive clear information about the next steps

- **Close exception**
  - *Given* that the customer has been informed and the issue addressed
  - *When* I update the SLMS with resolution notes
  - *Then* the system must mark the exception as closed and keep the resolution history available for future reference

#### US9 – "Handling customer-reported issues" (13) 🔄 IN PROGRESS
As a Customer Service Representative I want to receive notifications of problems reported by customers so that I can follow up and resolve them quickly.

**Acceptance Criteria**
- **Receive notification**
  - *Given* that a customer has reported a problem
  - *When* the SLMS registers it
  - *Then* I must receive a notification with the order details and the issue description

- **Follow up with customer**
  - *Given* that I received a notification of a customer-reported problem
  - *When* I contact the customer or update the order
  - *Then* the SLMS must allow me to log resolution notes and close the exception

---

### Carlos Silva (Driver / Carrier)

#### US10 – "Viewing delivery manifest" (8) 🔄 IN PROGRESS
As a Driver I want to view my delivery manifest so that I know all packages I need to deliver.

**Acceptance Criteria**
- **Access delivery manifest**
  - *Given* that I am logged into the SLMS mobile app
  - *When* I open my daily schedule
  - *Then* the system must show a list of packages with addresses and priorities

#### US11 – "Receiving optimized delivery routes" (9) 🔄 IN PROGRESS
As a Driver I want to receive optimized delivery routes so that I can complete deliveries more efficiently.

**Acceptance Criteria**
- **Receive route planning**
  - *Given* that I am logged into the SLMS mobile app
  - *When* I request my route
  - *Then* the system must provide me with the optimal delivery sequence

#### US12 – "Recording proof of delivery (POD)" (8) ✅ COMPLETED
As a Driver I want to record proof of delivery so that I am able to validate that the package was delivered.

**Acceptance Criteria**
- **Register POD**
  - *Given* that I have delivered a package
  - *When* I collect a signature or take a delivery photo
  - *Then* the SLMS must update the order as "delivered" in real time

#### US13 – "Reporting delivery exceptions" (5) 🔄 IN PROGRESS
As a Driver I want to report delivery exceptions so that I can keep the system updated when delivery fails.

**Acceptance Criteria**
- **Report failed delivery**
  - *Given* that I cannot deliver a package
  - *When* I select a reason (e.g. recipient absent) in the mobile app
  - *Then* the system must update the order status as "exception"

---

### Mariana Costa (Customer)

#### US14 – "Creating an Account through the Landing Page" (5) ✅ COMPLETED
As a Customer I want to create an account through the SLMS landing page so that I can access my delivery history and track my orders.

**Acceptance Criteria**
- **Access registration page**
  - *Given* that I am on the SLMS landing page
  - *When* I click on "Create Account"
  - *Then* I must be redirected to the Keycloak registration screen

- **Complete registration**
  - *Given* that I enter valid registration details (name, email, password)
  - *When* I submit the form
  - *Then* my account must be created

- **Access customer portal**
  - *Given* that my registration is complete
  - *When* I return to the customer portal
  - *Then* I can log in with my new credentials

#### US15 – "Logging In through the Landing Page" (3) ✅ COMPLETED
As a Customer I want to log in through the SLMS landing page so that I can securely access my order tracking portal.

**Acceptance Criteria**
- **Access login page**
  - *Given* that I am on the SLMS landing page
  - *When* I click on "Login"
  - *Then* I am redirected to the Keycloak authentication page

- **Successful authentication**
  - *Given* that I enter valid credentials or choose a social login option
  - *When* I log in successfully
  - *Then* I am redirected to my customer dashboard

- **Session management**
  - *Given* that I am logged in
  - *When* I close the browser or my session expires
  - *Then* I must log in again to regain access

#### ~~US16 – "Real-time order tracking" (21)~~ ❌ REMOVED
**Reason:** Out of scope due to time constraints. Basic tracking functionality exists in customer portal, but full real-time GPS tracking with live location updates was deemed too complex for current sprint timeline.

#### US16 – "Receiving automatic notifications" (13) 🔄 IN PROGRESS
**Previously US17**

As a Customer I want to receive notifications about my order so that I can stay informed.

**Acceptance Criteria**
- **Receive shipment updates**
  - *Given* that my order status changes (e.g. shipped, out for delivery, failed attempt)
  - *When* the event occurs
  - *Then* the SLMS must send me an automatic notification (email or SMS)

#### US17 – "Reporting a delivery issue" (8) ✅ COMPLETED
**Previously US18**

As a Customer I want to report delivery issues through the tracking portal so that I can notify support and get faster resolutions.

**Acceptance Criteria**
- **Access report option**
  - *Given* that I am in the SLMS customer portal with my tracking number
  - *When* I open my order details
  - *Then* the system must display an option to "Report a problem"

- **Submit problem details**
  - *Given* that I have identified a problem with my order
  - *When* I select the "Report a problem" option and enter details (e.g. wrong address, damaged package, unavailable at delivery time)
  - *Then* the SLMS must register the issue as an open exception linked to my order

- **Notify customer service**
  - *Given* that I have submitted a problem report
  - *When* the exception is created
  - *Then* the SLMS must notify a Customer Service Representative so that they can follow up with me

---

### All Users (Cross-Role)

#### US18 – "AI Assistant for contextual help" (13) 🆕 NEW
As a SLMS User I want to access an AI assistant so that I can get instant contextual help based on my role and current page.

**Acceptance Criteria**
- **Access chatbot interface**
  - *Given* that I am logged into any SLMS frontend (admin-console, customer-portal, driver-pwa, landing-page)
  - *When* I click on the chat widget icon
  - *Then* the system must open an interactive chat interface

- **Receive role-based responses**
  - *Given* that I am asking a question in the chatbot
  - *When* the system processes my query
  - *Then* I must only receive answers based on content authorized for my role (e.g., warehouse staff don't see manager-only information)

- **Get contextual help**
  - *Given* that I am on a specific page or route (e.g., /orders, /carriers, /tracking)
  - *When* I ask a question
  - *Then* the system must prioritize content relevant to my current page context

- **Multilingual support**
  - *Given* that I have set my language preference to Portuguese or English
  - *When* I interact with the chatbot
  - *Then* the system must provide responses in my preferred language

- **Natural language interaction**
  - *Given* that I ask a question in conversational language
  - *When* the chatbot processes my query
  - *Then* I must receive a natural language response (not just keyword matches or static answers)

- **Semantic search capability**
  - *Given* that I ask a question using different words than the documentation
  - *When* the system searches for relevant content
  - *Then* it must find semantically similar content (meaning-based, not just keyword-based)

**Technical Notes**
- Backend: Gemini API for LLM, Pinecone for vector database
- Database: AssistantDocument entity with role/frontend/route filtering
- Frontend: Chat widget component integrated in all 4 frontends
- See [ADR-0007](../../adr/0007-chatbot-module-integration.md) and [Chatbot Module Handbook](../../handbook/chatbot-module.md) for implementation details

---

## Status Summary

### Completed Stories (12)
- US1: Daily KPI monitoring (João)
- US2: Carrier comparison (João)
- US3: Performance reports (João)
- US4: Processing new order (Ana)
- US5: Generating labels (Ana)
- US6: Confirming pickup (Ana)
- US7: Quick order status lookup (Rita)
- US8: Resolving delivery issue (Rita)
- US12: Recording POD (Carlos)
- US14: Creating account (Mariana)
- US15: Logging in (Mariana)
- US17: Reporting delivery issue (Mariana)

### In Progress Stories (5)
- US9: Handling customer-reported issues (Rita)
- US10: Viewing delivery manifest (Carlos)
- US11: Optimized delivery routes (Carlos)
- US13: Reporting delivery exceptions (Carlos)
- US16: Receiving notifications (Mariana)

### New/Pending Stories (1)
- US18: AI Assistant for contextual help (All Users)

### Total
- **18 user stories**
- **12 completed (67%)**
- **5 in progress (28%)**
- **1 new (5%)**

---

## Rationale

### Why Remove US16?

The original US16 "Real-time order tracking" with 21 story points was removed because:

1. **Complexity:** Full real-time GPS tracking with live map updates requires:
   - Integration with carrier GPS systems
   - Real-time data streaming infrastructure
   - Complex frontend mapping components (e.g., Google Maps, Mapbox)
   - Significant backend changes for location updates

2. **Time Constraints:** Sprint 6 timeline does not allow for this level of complexity

3. **Existing Functionality:** The customer portal already provides:
   - Order status tracking
   - Status history
   - Expected delivery dates
   - This covers 80% of customer tracking needs

4. **Future Enhancement:** Real-time GPS tracking can be added in a future sprint when time permits

### Why Add US18 (Chatbot)?

The chatbot user story was added because:

1. **Already Implemented:** The chatbot module was implemented in Sprint 4 with:
   - Gemini LLM integration
   - Pinecone vector database
   - Role-based content filtering
   - Frontend-specific help
   - Route-based contextual assistance
   - Multilingual support (Portuguese/English)

2. **Missing Documentation:** Despite being implemented and tested, there was no formal user story documenting this feature

3. **Cross-Cutting Feature:** Benefits all user roles:
   - Logistics managers get help with KPIs and reports
   - Warehouse staff get help with order processing
   - Customer service gets help with exception handling
   - Drivers get help with deliveries
   - Customers get help with tracking and issues

4. **Significant Value:** The AI assistant provides:
   - Contextual, role-specific help
   - Natural language interaction
   - Reduces need for external documentation
   - Improves user experience across all frontends

### Impact on Sprint Velocity

- **Removed:** 21 story points (US16)
- **Added:** 13 story points (US18)
- **Net Change:** -8 story points
- **Reason:** Exchanging an unimplemented complex feature for an already-implemented feature that needed documentation

This change aligns the user story backlog with actual implementation progress and ensures all implemented features are properly documented.

---

## References

- [Original Personas & User Stories](../sprint2/personas-scenarios-US-update/personas-scenarios-US.md)
- [ADR-0007: Chatbot Module Integration](../../adr/0007-chatbot-module-integration.md)
- [Chatbot Module Handbook](../../handbook/chatbot-module.md)
- [Data Model](../../handbook/data-model.md)
