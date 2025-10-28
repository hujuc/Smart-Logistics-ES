# SLMS Backend

Spring Boot backend for the Shipment and Logistics Management System (SLMS).

Repository: https://github.com/detiuaveiro/group-project-es2526_304

## Environment Setup

1. Copy the environment template:
   ```bash
   cp .env.example .env
   ```

2. Update `.env` with your database configuration:
   ```bash
   # Database Configuration
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=slms
   DB_USERNAME=slms
   DB_PASSWORD=slms
   
   # JPA Configuration
   JPA_DDL_AUTO=update
   JPA_SHOW_SQL=true
   JPA_FORMAT_SQL=true
   ```

## Database Setup

1. Start PostgreSQL using Docker Compose:
   ```bash
   cd ../../infra
   docker-compose up -d
   ```

2. The database will be available at `localhost:5432` with:
   - Database: `slms`
   - Username: `slms`
   - Password: `slms`

## Running the Application

1. Make sure PostgreSQL is running
2. Ensure your `.env` file is configured
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Environment Variables

The application uses environment variables for configuration. Default values are provided as fallbacks:

- `DB_HOST` - Database host (default: `localhost`)
- `DB_PORT` - Database port (default: `5432`)
- `DB_NAME` - Database name (default: `slms`)
- `DB_USERNAME` - Database username (default: `slms`)
- `DB_PASSWORD` - Database password (default: `slms`)
- `JPA_DDL_AUTO` - Hibernate DDL mode (default: `update`)
- `JPA_SHOW_SQL` - Show SQL queries (default: `true`)
- `JPA_FORMAT_SQL` - Format SQL queries (default: `true`)

## Project Structure

- `src/main/java/Operimus/SLMS/business_layer/` - Domain models and business logic
- `src/main/java/Operimus/SLMS/presentation_layer/` - REST controllers (BFFs)
- `src/main/resources/` - Configuration files

## Order Event API

The order shipment module now exposes endpoints that surface the audit trail stored in the `order_events` table:

- `GET /api/admin/orders/{orderId}/events` – return the full status timeline for the specified order ordered by `occurredAt`.
- `GET /api/admin/orders/{orderId}/events/latest` – return only the most recent status change for the specified order.

Both endpoints respond with a JSON array/object using the following shape:

```jsonc
{
  "id": 42,
  "orderId": 5,
  "eventType": "IN_TRANSIT",
  "occurredAt": "2024-03-04T10:15:00Z",
  "notes": "Order status changed from READY_FOR_PICKUP to IN_TRANSIT"
}
```
