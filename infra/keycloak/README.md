# SLMS Keycloak Configuration

This directory contains Keycloak realm configuration for SLMS authentication and authorization.

## What's Configured

### Roles (5)

| Role | Description |
|------|-------------|
| CUSTOMER | Place orders, track shipments |
| CUSTOMER_SERVICE_REP | Handle customer inquiries |
| DRIVER | View deliveries, update status |
| LOGISTICS_MANAGER | Oversee operations, reports |
| WAREHOUSE_STAFF | Manage inventory |

### Test Users (5)

| Username | Password | Role |
|----------|----------|------|
| customer | customer | CUSTOMER |
| customer_service | support | CUSTOMER_SERVICE_REP |
| driver | driver | DRIVER |
| logistics_manager | manager | LOGISTICS_MANAGER |
| warehouse | warehouse | WAREHOUSE_STAFF |

**Note:** All passwords are permanent (no change required on first login).

### Clients (4)

- **slms-backend** - Backend API (port 8180, confidential)
- **slms-admin-console** - Admin web app (port 3000, public)
- **slms-customer-portal** - Customer web app (port 3001, public)
- **slms-driver-pwa** - Driver mobile app (port 3002, public)

## Files

- `realm-export.json` - Keycloak realm configuration
- `SETUP.md` - Setup instructions

## Security Settings

- **Password Policy**: Minimum 4 characters (dev-friendly)
- **Brute Force Protection**: Enabled (5 max failures)
- **Email Verification**: Disabled (for development)
- **Port**: 8180

## Quick Start

See [SETUP.md](SETUP.md) for complete setup instructions.
