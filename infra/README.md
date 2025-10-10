## Development environment with Docker

- Copy any required environment variables to `.env` files if they are not already present in `src/backend`.
- Start the stack with `docker compose -f docker-compose.dev.yml up --build`.
- Access the applications through the reverse proxy:
  - Admin console: http://localhost/admin
  - Customer portal: http://localhost/customer
  - Driver PWA: http://localhost/driver
  - Backend API: http://localhost/api/ 
- Stop the stack with `docker compose -f docker-compose.dev.yml down`.
