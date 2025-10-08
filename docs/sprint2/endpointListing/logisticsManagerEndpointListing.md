# Logistics Manager – API Endpoint Listing

## Overview
This document lists all API endpoints required by the **Logistics Manager Interface** and maps each one to its corresponding **database tables**.  
The endpoints cover the main managerial functions:
- Dashboard KPIs and performance tracking (US1)
- Carrier comparison and selection (US2)
- Reports and analytics generation (US3)

---

## Dashboard
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/dashboard/summary` | Retrieve global dashboard summary (orders processed, pending pickups, deliveries completed, exception rate) | `orders`, `delivery_exceptions`, `warehouses`, `kpi_metrics` |
| **GET** | `/dashboard/kpis?period=<daily/weekly/monthly>` | Retrieve historical KPI trends for chart rendering | `kpi_metrics` |
| **GET** | `/dashboard/performance-charts` | Get comparative performance per carrier (on-time rate, avg cost, reliability) | `kpi_metrics`, `carriers` |
| **GET** | `/dashboard/quick-stats` | Retrieve daily operational stats (orders processed, in-transit, completed) | `orders`, `kpi_metrics` |

---

## Carrier Comparison
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/orders?status=unassigned` | Retrieve list of orders without assigned carrier (used in *New Shipment* view). | `orders`, `customers`, `warehouses` |
| **PATCH** | `/orders/assign-carrier` | Assign selected orders to a carrier. Body includes `order_ids[]` and `carrier_id`. | `orders`, `carriers`, `audit_logs` |
| **GET** | `/orders?status=in_transit` | Retrieve list of all ongoing shipments (*Current Shipments* tab). | `orders`, `carriers`, `warehouses` |
| **GET** | `/orders?status=<status>&from=<date>&to=<date>&carrier=<id>` | Retrieve all historical orders with filters (used in *All Orders* tab). | `orders`, `customers`, `carriers`, `warehouses` |
| **GET** | `/carriers` | List all carriers available for selection. | `carriers` |
| **GET** | `/carrier-rates?origin=<zone>&destination=<zone>&weight=<kg>&service=<type>` | Fetch cost and transit time estimates for comparison (*New Shipment* view). | `carrier_rates`, `carriers` |
| **GET** | `/carriers/{id}` | Retrieve full carrier details (used in *Carrier Details* view). | `carriers`, `kpi_metrics` |
| **GET** | `/carriers/{id}/performance?period=<period>` | Get historical performance metrics for a carrier. | `kpi_metrics`, `orders` |
| **GET** | `/carriers/{id}/coverage` | Retrieve list of zones/regions served by the carrier. | `carrier_rates`, `carriers` |
| **GET** | `/orders/{id}` | Retrieve full details of a specific order/shipment (used in detail view). | `orders`, `customers`, `carriers`, `shipping_documents` |

---

## Reports & Analytics
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/reports/templates` | Get available report types and templates (Performance, Cost, Exception, Custom) | `reports` |
| **POST** | `/reports/generate` | Generate a new report based on selected parameters (type, date range, filters) | `reports`, `kpi_metrics`, `orders`, `carriers`, `warehouses` |
| **GET** | `/reports/{id}` | Retrieve generated report details and metadata | `reports`, `users` |
| **GET** | `/reports/{id}/download?format=<pdf/excel/csv>` | Download a generated report in selected format | `reports` |
| **GET** | `/reports/history?type=<type>` | List previously generated reports by category or date range | `reports` |
| **POST** | `/reports/schedule` | Schedule recurring report generation (weekly, monthly, etc.) | `reports`, `users` |
| **DELETE** | `/reports/{id}` | Delete or expire a report from history | `reports`, `audit_logs` |

---

## Management & Settings
| Method | Endpoint | Description | Main Tables |
|--------|-----------|--------------|--------------|
| **GET** | `/users/me` | Retrieve logged-in manager profile | `users` |
| **GET** | `/audit/logs?entity=<entity>&period=<range>` | Fetch system activity logs for transparency | `audit_logs` |
| **PATCH** | `/settings/preferences` | Update dashboard settings (preferred KPIs, default filters, notification thresholds) | `users` |

