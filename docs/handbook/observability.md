# Observability: SLIs and SLOs

**Last Updated:** 2025-11-11
**Project:** Smart Logistics Management System (SLMS)
**Status:** Active

## Table of Contents

1. [Overview](#overview)
2. [Service Level Indicators (SLIs)](#service-level-indicators-slis)
3. [Service Level Objectives (SLOs)](#service-level-objectives-slos)

---

## Overview

This document defines the Service Level Indicators (SLIs) and Service Level Objectives (SLOs) for the SLMS platform. These metrics ensure we maintain reliable, performant service for all user roles (Logistics Managers, Warehouse Staff, Customer Service Representatives, Drivers, and Customers).

### Context

The SLMS platform is a modular monolith Spring Boot application with multiple Next.js frontends, serving critical logistics operations including:
- Order management and processing
- Real-time shipment tracking
- Carrier integrations (FedEx, UPS, DHL via Shippo/ShipEngine)
- Route optimization for drivers
- Customer notifications and reporting

Given the critical nature of logistics operations, we establish clear reliability targets to ensure business continuity and user satisfaction.

---

## Service Level Indicators (SLIs)

SLIs are quantifiable measurements of service behavior from the user's perspective. We define three core SLIs:

### 1. Availability SLI

**Definition:** The percentage of successful HTTP requests over a given time period.

**Measurement:**
- A request is considered "successful" if it returns a non-5xx HTTP status code
- Measured at the nginx reverse proxy layer (overall system availability)
- Separately measured at backend API layer for internal visibility

**Formula:**
```
Availability (%) = (Total Requests - 5xx Responses) / Total Requests × 100
```

**Example Calculation:**
- Total requests in 24 hours: 100,000
- 5xx responses: 50
- Availability = (100,000 - 50) / 100,000 × 100 = **99.95%**

**Scope:**
- **All user-facing endpoints:** `/customer/*`, `/driver/*`, `/warehouse/*`, `/logistics/*`, `/customer-service/*` and `/auth/*`
- **Health checks:** `/actuator/health` (excluded from availability calculations)
- **Static assets:** Excluded (tracked separately)

**Critical Endpoints (weighted higher):**
- `POST /api/orders` - Order creation
- `GET /api/orders/{id}` - Order retrieval
- `POST /api/shipments` - Shipment creation
- `GET /api/tracking/{id}` - Tracking information
- `POST /api/auth/*` - Authentication endpoints

---

### 2. Latency SLI

**Definition:** The percentage of requests that complete within a specified time threshold (500ms for overall system).

**Measurement:**
- Measured from when nginx receives the request to when it sends the first byte of the response
- Tracks P95 (95th percentile) and P99 (99th percentile) latencies
- Separate thresholds for read vs. write operations
- Excludes external carrier API calls

**Formula:**
```
Latency SLI (%) = (Requests Completed < 500ms) / Total Requests × 100
```

**Example Calculation:**
- Total requests in 1 hour: 10,000
- Requests under 500ms: 9,700
- Latency SLI = 9,700 / 10,000 × 100 = **97%**

**Latency Buckets:**

| Operation Type | P95 Target | P99 Target | Examples |
|---------------|-----------|-----------|----------|
| **Read (GET)** | < 250ms | < 400ms | Get order, list shipments, tracking |
| **Write (POST/PUT)** | < 400ms | < 700ms | Create order, update shipment status |
| **Complex Operations** | < 1000ms | < 2000ms | Route optimization, analytics |
| **External API Calls** | Excluded | Excluded | Carrier API integrations (tracked separately) |

**Measurement Points:**
- **Nginx access logs:** Total request duration (user-perceived latency)
- **Spring Boot Actuator:** Application-level metrics (`/actuator/metrics/http.server.requests`)
- **Database queries:** Slow query logs (> 100ms)
- **External API calls:** Custom Micrometer timers for Shippo/ShipEngine integrations

---

### 3. Error Rate SLI

**Definition:** The percentage of failed transactions over total transactions.

**Measurement:**
- Tracks both 4xx (client errors) and 5xx (server errors) separately
- Focuses on critical business transactions (not all HTTP requests)
- Excludes expected errors (e.g., 401 Unauthorized on invalid credentials)

**Formula:**
```
Error Rate (%) = (Failed Transactions) / Total Transactions × 100

Where:
- Failed Transactions = 5xx errors + critical 4xx errors (400, 409, 422)
- Total Transactions = All requests to transactional endpoints
```

**Example Calculation:**
- Total transactional requests in 24 hours: 50,000
- 5xx errors: 100
- Critical 4xx errors (400, 409, 422): 150
- Error Rate = (100 + 150) / 50,000 × 100 = **0.5%**

**Error Categories:**

| Category | HTTP Codes | Impact | Counted in SLI |
|----------|-----------|--------|----------------|
| **Server Errors** | 500, 502, 503, 504 | High - System failure | Yes |
| **Data Validation Errors** | 400, 422 | Medium - Bad request data | Yes (critical endpoints only) |
| **Conflict Errors** | 409 | Medium - Business logic violation | Yes |
| **Authentication Errors** | 401, 403 | Low - Expected user errors | No |
| **Not Found Errors** | 404 | Low - Resource doesn't exist | No |
| **Rate Limiting** | 429 | Medium - Capacity issue | Yes |

**Critical Transaction Endpoints:**
- `POST /api/orders` - Order creation
- `POST /api/shipments` - Shipment creation
- `PUT /api/shipments/{id}/status` - Shipment status updates
- `POST /api/carriers/rates` - Rate calculation requests
- `POST /api/routes/optimize` - Route optimization

---

## Service Level Objectives (SLOs)

SLOs are target values or ranges for our SLIs that we commit to maintaining.

### System-Wide SLOs

| SLI | SLO Target | Measurement Window | Error Budget |
|-----|-----------|-------------------|--------------|
| **Availability** | ≥ 99.5% | 30-day rolling window | 3.6 hours/month |
| **Latency** | 95% of requests < 500ms | 24-hour rolling window | 5% of requests |
| **Error Rate** | < 1% | 24-hour rolling window | 1% of transactions |

> **Note:** These SLO targets are calibrated for an academic project environment with limited 24/7 monitoring and external API dependencies. In a production environment with full monitoring infrastructure, on-call support, and carrier API SLAs, we would target higher availability (99.9%+) and stricter latency thresholds.

### Detailed SLO Breakdown

#### 1. Availability SLO: ≥ 99.5%

**Target:** 99.5% of all requests return non-5xx responses over a 30-day period.

**Interpretation:**
- **Allowed downtime:** ~3.6 hours per month (or ~7 minutes per day)
- **Error budget:** 0.5% of requests (e.g., 500 failures per 100,000 requests)

**Rationale:**
- Realistic target for academic project without 24/7 on-call support
- Accounts for external carrier API failures outside our control (Shippo, ShipEngine)
- Balances reliability with development velocity, testing, and maintenance windows
- Still represents excellent uptime (216 hours up out of 720 hours/month)

**Measurement Window:** 30-day rolling window
- Smooths out short-term incidents
- Provides meaningful monthly reporting
- Allows for planned maintenance windows

**Exclusions:**
- Scheduled maintenance (communicated 48h in advance)
- Third-party carrier API failures (tracked separately)
- User error (401, 403, 404 responses)

---

#### 2. Latency SLO: 95% of requests < 500ms

**Target:** 95th percentile (P95) request latency must be under 500ms over a 24-hour period (excluding external carrier API calls).

**Interpretation:**
- 95 out of 100 requests complete in under 500ms
- 5% of requests may exceed 500ms (error budget)
- P99 (99th percentile) should remain under 1000ms (1 second)

**Rationale:**
- Realistic target that accounts for Spring Boot + JPA + PostgreSQL stack latency
- Excludes external carrier API calls which can take 2-3 seconds
- Still provides good user experience (sub-500ms is responsive)
- Achievable without extensive caching/optimization infrastructure

**Measurement Window:** 24-hour rolling window
- Captures daily usage patterns (peak vs. off-peak)
- Shorter window for faster feedback on performance degradation
- Enables rapid response to latency issues

**Per-Endpoint Latency Targets:**

| Endpoint Pattern | Operation | Target P95 | Target P99 | Justification |
|-----------------|-----------|-----------|-----------|---------------|
| `GET /api/orders` | List orders | 250ms | 400ms | Database read with pagination |
| `GET /api/orders/{id}` | Get single order | 150ms | 250ms | Simple database lookup |
| `POST /api/orders` | Create order | 400ms | 700ms | Database write + validation + business logic |
| `GET /api/tracking/{id}` | Real-time tracking | 300ms | 500ms | Database lookup + status aggregation |
| `POST /api/routes/optimize` | Route optimization | 1000ms | 2000ms | Complex algorithm |

**Excluded from SLO (tracked separately):**
- `POST /api/carriers/rates` - Get shipping rates (2-5 seconds, depends on Shippo/ShipEngine)
- Report generation endpoints (5+ seconds expected)
- Bulk export operations (10+ seconds expected)
- File upload endpoints (depends on file size)

---

#### 3. Error Rate SLO: < 1%

**Target:** Less than 1% of critical business transactions fail over a 24-hour period.

**Interpretation:**
- **Server errors (5xx):** < 0.5% of requests
- **Critical client errors (400, 409, 422):** < 0.5% of requests
- **Combined error rate:** < 1% total

**Rationale:**
- 99% success rate ensures high transaction reliability
- Allows for occasional transient failures (network blips, temporary resource exhaustion)
- More lenient than availability SLO (which is 99.9%) to account for edge cases

**Measurement Window:** 24-hour rolling window
- Rapid detection of error rate spikes
- Daily reporting for operations team
- Correlates with daily business metrics

**Error Budget Breakdown:**

For 100,000 daily transactions:
- **Allowed 5xx errors:** 500 (0.5%)
- **Allowed 4xx errors (critical):** 500 (0.5%)
- **Total error budget:** 1,000 failures (1%)

**Critical vs. Non-Critical Errors:**

**Critical (counted in SLO):**
- 500 Internal Server Error - Application crash
- 502 Bad Gateway - Backend unavailable
- 503 Service Unavailable - Resource exhaustion
- 504 Gateway Timeout - Slow backend response
- 400 Bad Request - On order/shipment creation (indicates data validation failure)
- 409 Conflict - Duplicate order ID, status transition violation
- 422 Unprocessable Entity - Business rule violation

**Non-Critical (excluded from SLO):**
- 401 Unauthorized - Invalid/missing authentication token
- 403 Forbidden - Insufficient permissions (expected for role-based access)
- 404 Not Found - Resource doesn't exist (often user typo or stale link)
- 429 Too Many Requests - Rate limiting (tracked separately for capacity planning)
