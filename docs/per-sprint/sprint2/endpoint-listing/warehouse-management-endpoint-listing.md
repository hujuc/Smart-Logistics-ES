# Warehouse Management – API Endpoint Listing

## Overview
This document lists all API endpoints required by the **Warehouse Staff Interface** and maps each one to its corresponding **database tables**.  
The endpoints cover the key warehouse functions:
- "Processing a new order" (US4)
- "Generating shipping labels and documents" (US5)
- "Confirming carrier pickup" (US6)

---

## Order Management
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/orders?status=<status>&search=<query>&page=<n>` | Fetch list of orders filtered by status or search term | `orders`, `customers`, `carriers`, `warehouses` |
| **GET** | `/orders/{id}` | Retrieve detailed order info (customer, carrier, destination, dimensions, etc.) | `orders`, `customers`, `carriers`, `shipping_documents` |
| **POST** | `/orders` | Create a new order in the system | `orders`, `users`, `warehouses` |
| **POST** | `/orders/{id}/generate-label` | Generate and link shipping label PDF | `shipping_documents`, `orders`, `users` |
| **GET** | `/orders/{id}/label` | Retrieve existing shipping label PDF | `shipping_documents` |
| **POST** | `/orders/{id}/generate-packing-slip` | Generate and link packing slip PDF | `shipping_documents`, `orders`, `users` |
| **GET** | `/orders/{id}/packing-slip` | Retrieve existing packing slip PDF | `shipping_documents` |
| **PATCH** | `/orders/{id}/status` | Update order status (e.g., PENDING → DOCUMENTED → READY) | `orders`, `audit_logs` |

---

## Collections (Carrier Pickup)
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/orders?status=ready` | List all orders prepared for pickup | `orders`, `carriers`, `warehouses` |
| **POST** | `/collections` | Confirm pickup event (creates record and updates orders) | `orders`, `users`, `audit_logs` |
| **POST** | `/collections/{id}/scan` | Register scanned package QR codes | `orders`, `shipping_documents` |
| **PUT** | `/collections/{id}` | Update pickup details (driver, vehicle, time, signature) | `orders`, `users`, `audit_logs` |
| **GET** | `/collections/{id}` | Retrieve specific collection details | `orders`, `users` |
| **GET** | `/collections/{id}/scanned-items` | List scanned items for confirmation | `orders` |

---

## Documents & Reports
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/documents/{order_id}/label` | Retrieve shipping label PDF | `shipping_documents` |
| **GET** | `/documents/{order_id}/packing-slip` | Retrieve packing slip PDF | `shipping_documents` |
| **GET** | `/documents/manifest/{carrier_id}` | Retrieve manifest for carrier | `shipping_documents`, `orders` |

---