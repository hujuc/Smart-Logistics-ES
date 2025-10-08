# Epic and Sprint Management

## Proposed Epics

### E1 - Order & Shipment Management

Register orders, generate docs, update pickups, central dashboard (FR-2).  
**User Stories:** US4 (register new order), US5 (generate labels), US6 (confirm pickup).

### E2 - Carrier Integration & Rate Management

Compare carriers, rate retrieval, optimal carrier selection (FR-3).  
**User Stories:** US2 (carrier comparison).

### E3 - Tracking & Visibility

Real-time tracking, status dashboards, customer/CSR access (FR-4).  
**User Stories:** US7 (status lookup), US14 (real-time tracking).

### E4 - Route Optimization & Driver Support

Optimized routes, navigation, mobile app (FR-5, FR-6).  
**User Stories:** US10 (view manifest), US11 (optimized routes), US12 (proof of delivery), US13 (exceptions).

### E5 - Reporting & Analytics

KPI dashboards, exportable reports, historical analysis (FR-7).  
**User Stories:** US1 (monitor KPIs), US3 (export reports).

### E6 - Notifications & Alerts

Automated multi-channel notifications (FR-8).  
**User Stories:** US15 (receive notifications).

### E7 - Customer Portal

Track orders, delivery history, preferences, issue reporting (FR-9).  
**User Stories:** US14 (tracking), US16 (report issue).

---

## Sprint Distribution

### Sprint 1 (01 Oct) - Technical foundation

- Setup CI/CD, repo, environment.
- Define architecture and system roles.
- Prepare backlog and architecture documentation.

### Sprint 2 (15 Oct) - First MVP

**Epic 2 (Order Management):**  
US4 – Register new order  
US5 – Generate labels  
US6 – Confirm pickup  
Minimal customer portal (basic tracking page stub).

**Implementation Summary:**

- Implement OrderManagement
- API endpoints for creating orders, generating shipping docs (PDF/labels mock), marking pickup.
- UI for warehouse staff to create new orders.
- Simple dashboard to list orders and mark as collected.
- Connect backend & frontend order APIs.

### Sprint 3 (29 Oct) - Version 2.x.x + testing integration

**Epic 2 (Carrier Integration):** US2 (carrier comparison).  
**Epic 3 (Tracking & Visibility):** basic tracking for warehouse/CSR.

**Implementation Summary:**

- Carrier Integration API  
- Implement carrier comparison logic  
- Logistics Manager UI for comparing carriers  
- CSR UI to lookup order status  
- Basic tracking visible in customer portal (order location/status)

### Sprint 4 (12 Nov) - Version 3.x.x + IaC + SLOs

**Epic 5 (Route Optimization & Driver App):**  
US10 (manifest)  
US11 (optimized routes)  
US12 (proof of delivery)

**Infra as Code setup (Terraform/Ansible).**  
Define first SLOs.

**Implementation Summary:**

- Driver APIs  
  - Retrieve manifest (US10).  
  - Generate optimized routes (mock Google Maps or OSRM API).  
  - Record POD (US12).  
- Mobile app (React Native or PWA) for drivers:  
  - View delivery manifest.  
  - Optimized route display (Google Maps integration).  
  - Upload proof of delivery (photo/GPS).  
- Connect to Google Maps API (routes).

### Sprint 5 (26 Nov) - Version 4.x.x + observability

**Epic 7 (Notifications & Alerts):** US15 (notifications).  
**Epic 8 (Customer Portal expansion):** US14 (real-time tracking).

**Implementation Summary:**

- Notification service: trigger email/SMS  
- Customer portal backend upgraded to real-time tracking  
- Customer portal: real-time order tracking

### Sprint 6 (10 Dec) - Version 5.x.x + refinements

**Epic 6 (Reporting & Analytics):**  
US1 (KPI dashboard)  
US3 (export reports)  

**Epic 8 (Customer Portal advanced):** US16 (report issue).  

Post-mortem environment setup.

**Implementation Summary:**

- Reporting & Analytics service  
- KPI dashboard (US1).  
- Export reports (US3).  
- Customer portal enhancements: report issues (US16)  
- Manager dashboard with KPIs and reports export  
- Customer portal UI for reporting delivery issues  
- Report exports (PDF/Excel/CSV).

### Sprint 7 (16 Dec) - Final delivery & presentations

Final refinements and stabilization.  
Documentation & technical report.  
Prepare presentation demo.

**Implementation Summary:**

- Final improvements
