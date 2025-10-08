# Driver / Carrier Mobile – API Endpoint Listing

## Overview
This document lists all API endpoints required by the **Driver/Carrier Mobile Interface** and maps each one to its corresponding **database tables**.  
These endpoints cover the main delivery operations and driver workflow features:
- Route assignment and daily delivery manifest management (US11)
- Real-time navigation and status updates (US12)
- Proof of Delivery (POD) capture (US13)
- Exception and failed delivery reporting (US14)

---

## 1. Driver Authentication & Profile

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **POST** | `/auth/login` | Authenticate driver using email/phone and password; returns access token. | `users`, `drivers` |
| **GET** | `/drivers/me` | Retrieve logged-in driver profile and current status. | `drivers`, `users`, `carriers` |
| **PATCH** | `/drivers/me/status` | Update driver’s availability or route status (`AVAILABLE`, `BREAK`, `ON_ROUTE`, `OFF_DUTY`). | `drivers`, `audit_logs` |
| **PATCH** | `/drivers/me/location` | Update driver’s current GPS location (background sync). | `drivers` |

---

## 2. Daily Route & Delivery Manifest Management

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/routes/today` | Retrieve driver’s assigned route for the current day, including total stops and progress. | `delivery_routes`, `drivers` |
| **GET** | `/routes/{id}` | Retrieve detailed route information (stops, sequence, ETA, metrics). | `delivery_routes`, `delivery_manifests`, `orders` |
| **POST** | `/routes/{id}/start` | Mark route as started (updates actual_start timestamp). | `delivery_routes`, `audit_logs` |
| **POST** | `/routes/{id}/complete` | Mark route as completed after last delivery (updates actual_end). | `delivery_routes`, `audit_logs` |
| **POST** | `/routes/{id}/optimize` | Request route re-optimization (e.g., for traffic or failed stops). | `delivery_routes` |

---

## 3. Delivery Manifest Operations

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/routes/{id}/deliveries` | List all delivery stops for a given route (sequence, address, status). | `delivery_manifests`, `orders`, `customers` |
| **GET** | `/deliveries/{id}` | Retrieve full delivery details for a specific stop (customer, package info, notes). | `delivery_manifests`, `orders`, `customers` |
| **PATCH** | `/deliveries/{id}/status` | Update delivery status (`EN_ROUTE`, `ARRIVED`, `DELIVERED`, `FAILED`). | `delivery_manifests`, `tracking_events`, `audit_logs` |
| **POST** | `/deliveries/{id}/arrived` | Confirm arrival at delivery location (GPS + timestamp). | `delivery_manifests`, `tracking_events` |
| **POST** | `/deliveries/{id}/skip` | Skip a stop (with justification or exception). | `delivery_manifests`, `delivery_attempts`, `tracking_events` |

---

## 4. Proof of Delivery (POD)

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **POST** | `/deliveries/{id}/pod/signature` | Upload digital signature for proof of delivery. | `delivery_manifests`, `shipping_documents` |
| **POST** | `/deliveries/{id}/pod/photo` | Upload delivery photo as evidence of drop-off. | `delivery_manifests`, `shipping_documents` |
| **POST** | `/deliveries/{id}/pod/delivery_note` | Add delivery note or special instructions from customer. | `delivery_manifests`, `shipping_documents` |
| **POST** | `/deliveries/{id}/complete` | Complete delivery with POD details (recipient name, relationship, GPS, notes). | `delivery_manifests`, `delivery_attempts`, `tracking_events`, `orders` |
| **GET** | `/deliveries/{id}/pod` | Retrieve proof of delivery data (photos, signatures). | `delivery_manifests`, `shipping_documents` |

---

## 5. Exception & Failed Delivery Reporting

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **POST** | `/deliveries/{id}/exception` | Report delivery issue or failure (damaged, absent, wrong address, etc.). | `delivery_exceptions`, `delivery_attempts`, `tracking_events` |
| **POST** | `/deliveries/{id}/exception/photos` | Upload supporting photos for the exception report. | `delivery_exceptions` |
| **GET** | `/deliveries/{id}/exceptions` | Retrieve all exceptions logged for a specific delivery. | `delivery_exceptions` |
| **PATCH** | `/deliveries/{id}/exception/{exception_id}` | Update or resolve exception (e.g., after redelivery or return). | `delivery_exceptions`, `audit_logs` |
| **POST** | `/deliveries/{id}/attempt` | Log a delivery attempt (success or failure) with GPS and timestamp. | `delivery_attempts`, `delivery_manifests`, `tracking_events` |

---

## 6. Real-Time Tracking & Telemetry

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **POST** | `/tracking/heartbeat` | Send periodic GPS + status updates for live tracking (background). | `drivers`, `tracking_events` |
| **GET** | `/tracking/{order_id}` | Retrieve tracking history for a specific order. | `tracking_events`, `orders` |
| **POST** | `/tracking/event` | Log a manual tracking event (e.g., “Out for Delivery”). | `tracking_events` |

---

## 7. Notifications & Alerts

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/notifications` | Retrieve driver’s pending notifications (new routes, customer messages). | `notification_logs` |
| **PATCH** | `/notifications/{id}/read` | Mark a notification as read. | `notification_logs` |

---
