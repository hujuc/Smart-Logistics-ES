# Customer Service – API Endpoint Listing

## Overview
This document lists all API endpoints required by the **Customer Service Representative Interface** and maps each one to its corresponding **database tables**.  
The endpoints support the key customer support and issue resolution functions:
- "Quick order status lookup" (US7)
- "Resolving a delivery issue" (US8)
- "Handling customer-reported issues" (US9)

---

## 1. Order Search & Information Access

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/orders/search?query=<term>` | Search orders by number, tracking number, customer name, email, or phone. | `orders`, `customers`, `tracking_events` |
| **GET** | `/orders/{id}` | Retrieve detailed order information (customer details, carrier, tracking, etc.). | `orders`, `customers`, `carriers`, `tracking_events` |
| **GET** | `/orders/{id}/tracking` | Retrieve full tracking history and events for a specific order. | `tracking_events`, `orders`, `carriers` |
| **GET** | `/customers/{id}/orders` | List all orders placed by a specific customer. | `orders`, `customers` |
| **GET** | `/customers/{id}` | Retrieve customer profile, contact info, and order history summary. | `customers`, `orders` |

---

## 2. Exception Management

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/delivery-exceptions?status=<status>&priority=<priority>&assigned_to=<user_id>` | List delivery exceptions filtered by status, severity, or assignment (CSR queue). | `delivery_exceptions`, `orders`, `customers`, `users` |
| **GET** | `/delivery-exceptions/{id}` | Retrieve full details of a specific exception (type, order, tracking, customer). | `delivery_exceptions`, `orders`, `tracking_events`, `drivers` |
| **PATCH** | `/delivery-exceptions/{id}` | Update exception details (status, priority, assigned CSR, resolution notes). | `delivery_exceptions`, `users`, `audit_logs` |
| **POST** | `/delivery-exceptions/{id}/contact-carrier` | Log a carrier contact action related to the exception. | `delivery_exceptions`, `notification_logs`, `carriers` |
| **POST** | `/delivery-exceptions/{id}/contact-customer` | Send an update message/email to the customer about an active exception. | `notification_logs`, `customers`, `delivery_exceptions` |
| **POST** | `/delivery-exceptions/{id}/reschedule-delivery` | Reschedule the delivery for an exception case (e.g., missed delivery). | `delivery_manifests`, `orders`, `delivery_exceptions` |
| **POST** | `/delivery-exceptions/{id}/request-return` | Initiate a return workflow for a failed or damaged delivery. | `orders`, `delivery_exceptions`, `audit_logs` |
| **POST** | `/delivery-exceptions/{id}/close` | Close an exception after resolution (final status update). | `delivery_exceptions`, `audit_logs` |

---

## 3. Dashboard & Service KPIs

| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/dashboard/exceptions/summary` | Retrieve key metrics for exception handling (new today, pending, resolved, avg. resolution time). | `delivery_exceptions`, `kpi_metrics` |
| **GET** | `/dashboard/alerts` | Get list of overdue or high-severity exceptions requiring attention. | `delivery_exceptions` |
| **GET** | `/dashboard/my-cases` | Retrieve exceptions currently assigned to the logged-in CSR. | `delivery_exceptions`, `users` |

