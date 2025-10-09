# Customer Portal – API Endpoint Listing

## Overview
This document lists all API endpoints required by the **Customer Portal Interface** and maps each one to its corresponding **database tables**.  
The endpoints cover key customer-facing functions:
- "Real-time order tracking" (US14)
- "Receiving automatic notifications" (US15)
- "Reporting a delivery issue" (US16)

---

## Tracking Page
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/track?code=<tracking_number>` | Retrieve current order status, timeline, and estimated delivery date for public tracking page. | `orders`, `tracking_events`, `delivery_manifests`, `carriers` |
| **GET** | `/track/{tracking_number}/events` | Get chronological list of tracking events with timestamps and locations. | `tracking_events` |
| **GET** | `/track/{tracking_number}/map` | Retrieve latest geolocation of package for map visualization. | `tracking_events`, `delivery_routes`, `drivers` |
| **GET** | `/track/{tracking_number}/documents` | Retrieve downloadable delivery-related documents (label, proof of delivery). | `shipping_documents` |
| **POST** | `/track/{tracking_number}/subscribe` | Subscribe to email/SMS notifications for this tracking number. | `notification_logs`, `customers`, `orders` |
| **POST** | `/track/{tracking_number}/contact-support` | Submit inquiry or issue related to this shipment. | `customer_support_tickets`, `orders`, `customers` |
---

## Customer Account (Registered Users)
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/customers/me` | Retrieve authenticated customer profile and preferences. | `customers`, `customer_preferences` |
| **PATCH** | `/customers/me` | Update basic account information (name, contact details). | `customers`, `audit_logs` |
| **GET** | `/customers/me/orders?status=<status>&from=<date>&to=<date>` | Retrieve list of customer’s active or historical orders. | `orders`, `tracking_events`, `delivery_exceptions` |
| **GET** | `/customers/me/orders/{id}` | Retrieve detailed information about a specific order. | `orders`, `tracking_events`, `shipping_documents` |
| **GET** | `/customers/me/active-shipments` | Retrieve list of currently in-transit orders. | `orders`, `tracking_events` |
| **PATCH** | `/customers/me/preferences` | Update delivery or notification preferences. | `customer_preferences`, `customers` |
| **GET** | `/customers/me/preferences` | Retrieve all stored customer preferences. | `customer_preferences` |

---

## Issues & Customer Support
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/customers/me/orders` | Retrieve all orders belonging to the logged-in customer (for issue reporting view). | `orders`, `carriers`, `tracking_events` |
| **GET** | `/customers/me/orders/{id}` | Retrieve details of a specific order (used in “See Details”). | `orders`, `tracking_events`, `shipping_documents` |
| **POST** | `/delivery-exceptions` | Create a new delivery exception record linked to an order (triggered by “New Issue”). | `delivery_exceptions`, `orders`, `customers`, `users` |
| **GET** | `/customers/me/delivery-exceptions?status=<status>` | List all delivery exceptions reported by the logged-in customer (for *Active Issues* view). | `delivery_exceptions`, `orders` |
| **GET** | `/delivery-exceptions/{id}` | Retrieve full details of a specific delivery exception (issue details screen). | `delivery_exceptions`, `orders`, `users` |
| **PATCH** | `/delivery-exceptions/{id}/feedback` | Submit feedback after resolution (rating & comment). | `delivery_exceptions` |
| **POST** | `/support/contact` | Send a general inquiry or message to the customer service team. | `notification_logs`, `customers`, `users` |

---

## Notifications & Communication
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/notifications` | Retrieve list of recent notifications (email, SMS, push). | `notification_logs`, `notification_templates` |
| **PATCH** | `/notifications/{id}/mark-read` | Mark a notification as read. | `notification_logs` |
| **POST** | `/notifications/subscribe` | Enable or update notification subscriptions (email/SMS). | `customer_preferences`, `notification_logs` |
| **GET** | `/notifications/preferences` | Retrieve notification channel preferences. | `customer_preferences` |
| **PATCH** | `/notifications/preferences` | Update notification preferences by type. | `customer_preferences`, `customers` |
| **POST** | `/notifications/test` | Send a test notification for validation. | `notification_templates`, `notification_logs` |

---

## Authentication & Access
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **POST** | `/auth/signup` | Create a new customer account. | `customers`, `audit_logs` |
| **POST** | `/auth/login` | Authenticate customer credentials. | `customers` |
| **POST** | `/auth/logout` | End session for authenticated customer. | `audit_logs` |
