# CI/CD Pipeline Documentation

This document provides comprehensive documentation of the SLMS CI/CD pipeline, including workflows, test organization, secrets management, and deployment processes.

## Table of Contents

1. [Overview](#overview)
2. [CI - Quality Gate](#ci---quality-gate-ci-qualityyml)
3. [CI - Integration & E2E](#ci---integration--e2e-ci-integrationyml)
4. [CD - Deployment](#cd---deployment-cd-deployyml)
5. [Test Organization](#test-organization)
6. [Composite Action](#composite-action-setup-slms-env)
7. [Secrets & Environment Variables](#secrets--environment-variables)
8. [Branch Protection Rules](#branch-protection-rules)
9. [Deployment Process](#deployment-process)
10. [Future Improvements](#future-improvements-phase-4---deferred)

---

## Overview

The SLMS CI/CD pipeline consists of **three main workflows** that work together to ensure code quality, integration correctness, and successful deployment:

```
┌─────────────────────────────────────────────────────────────┐
│                      SLMS CI/CD Pipeline                     │
└─────────────────────────────────────────────────────────────┘

PR to develop/main          PR to main              Merge to main
       │                         │                         │
       ▼                         ▼                         ▼
┌─────────────┐          ┌──────────────┐         ┌──────────────┐
│ci-quality   │          │ci-integration│         │  cd-deploy   │
│             │          │              │         │              │
│ • Unit Tests│          │ • Real Docker│         │ • Terraform  │
│ • Mocked    │          │   Services   │         │ • Docker     │
│   Integration│         │ • Backend    │         │   Compose    │
│ • Coverage  │          │   Integration│         │ • Health     │
│   Gate ≥70% │          │ • E2E Tests  │         │   Checks     │
│ • Lint      │          │              │         │              │
│ • Build     │          │              │         │              │
└─────────────┘          └──────────────┘         └──────────────┘
```

### When Each Workflow Runs

| Workflow | Trigger | Branch | Purpose |
|----------|---------|--------|---------|
| **ci-quality.yml** | Pull Request | `develop`, `main` | Fast quality gate - ensures code meets minimum standards |
| **ci-integration.yml** | Pull Request | `main` only | Comprehensive integration testing before production |
| **cd-deploy.yml** | Merge / Manual | `main` | Automated production deployment |

### Branch Strategy Integration

- **`develop`** branch: Requires `ci-quality` to pass
- **`main`** branch: Requires both `ci-quality` AND `ci-integration` to pass
- **Production**: Automatically deploys when code merges to `main`

---

## CI - Quality Gate (ci-quality.yml)

Fast quality gate that runs on all pull requests to `develop` or `main`.

### Triggers

```yaml
on:
  pull_request:
    branches: [develop, main]
  workflow_dispatch:
```

### Jobs

#### Backend Quality
- **Runs:** Unit tests + mocked integration tests
- **Coverage Gate:** JaCoCo enforces ≥70% code coverage
- **Test Command:** `make test-ci`
- **What's included:**
  - All unit tests (`**/*Test.java`)
  - Mocked integration tests using `@MockitoBean`
    - `DocumentSeederIntegrationTest`
    - `ChatbotFlowIntegrationTest`

#### Frontend Quality
- **Matrix:** 4 applications
  - `admin-console`
  - `customer-portal`
  - `driver-pwa`
  - `landing-page`
- **Steps per app:**
  1. Install dependencies (`npm ci`)
  2. Run linter (`npm run lint`)
  3. Build application (`npm run build`)

### What Passes the Gate

- All tests pass
- Code coverage ≥70%
- No lint errors
- All applications build successfully

### Artifacts

- JaCoCo coverage report
- Allure test results
- JUnit test results (published to PR)

---

## CI - Integration & E2E (ci-integration.yml)

Comprehensive integration and end-to-end testing that runs only on pull requests to `main`.

### Triggers

```yaml
on:
  pull_request:
    branches: [main]
```

### Jobs

#### Backend Integration Tests

**Setup:**
- Starts real Docker services:
  - PostgreSQL (database)
  - Keycloak (authentication)
  - MinIO (object storage)
  - Kafka + Zookeeper (messaging)

**Execution:**
- **Test Command:** `make test-backend-integration`
- **Maven Command:** `mvn test -Dgroups=ci-integration`
- **What runs:**
  - `OrderDocumentServiceIntegrationTest` (tests MinIO integration)
  - `KeycloakUserServiceIntegrationTest` (tests Keycloak integration)

**Why these tests?**
- Validate real integration with Docker services
- Ensure services are correctly configured
- Test database migrations and schema

#### E2E Tests

**Matrix:** 4 applications
- `admin-console`
- `customer-portal`
- `driver-pwa`
- `landing-page`

**Setup:**
- Starts **full SLMS stack:**
  - PostgreSQL, Keycloak, MinIO
  - Backend (Spring Boot)
  - All 4 frontends (Next.js)
  - Nginx (reverse proxy)

**Technology:**
- **Framework:** Playwright (NOT Cucumber currently)
- **Type:** Real E2E tests (backend + frontend integration)
- **Test Files:** `*.spec.ts`

**Key Point:** These ARE true end-to-end tests that exercise the full stack, not just UI tests.

### Test Organization

| Tag | Purpose | Services Required | Runs In |
|-----|---------|------------------|---------|
| `@Tag("ci-integration")` | Real Docker service integration | MinIO, Keycloak | ci-integration |
| `@MockitoBean` | Mocked integration tests | None (mocked) | ci-quality |
| `@Tag("real-integration")` | Real external API tests | Gemini, Pinecone APIs | LOCAL DEV ONLY |

**Why `real-integration` tests don't run in CI:**
- Too slow (real API calls add latency)
- API rate limits and costs
- Not necessary for every PR (mocked tests verify logic)
- Production uses real APIs (validated post-deploy)

---

## CD - Deployment (cd-deploy.yml)

Automated production deployment to AWS self-hosted runner.

### Triggers

```yaml
on:
  push:
    branches: [main]
  workflow_dispatch:  # Manual trigger
```

### Deployment Steps

1. **Checkout code** from `main` branch
2. **Setup environment** using `setup-slms-env` composite action
3. **Cleanup** old containers and Docker images
4. **Deploy** with Terraform + Docker Compose
5. **Restart Nginx** for DNS refresh
6. **Health checks** verify deployment success

### What Gets Deployed

**Application Stack:**
- Backend (Spring Boot) - includes fully functional chatbot with Gemini/Pinecone
- 4 Frontends (Next.js):
  - Landing Page
  - Customer Portal
  - Admin Console
  - Driver PWA

**Infrastructure:**
- PostgreSQL 16
- Keycloak
- MinIO
- Kafka + Zookeeper
- Nginx (reverse proxy)

### Deployment Strategy

- **Smart deployment:** Reuses existing containers when possible
- **Zero-downtime:** Nginx restart with minimal interruption
- **Health checks:** Backend and Nginx must be running post-deployment

### Deployment vs CI Differences

| Aspect | ci-integration | cd-deploy |
|--------|----------------|-----------|
| Infrastructure | Docker Compose | Terraform + Docker Compose |
| Runner | GitHub-hosted | Self-hosted AWS |
| Services | Test databases | Production databases |
| Networking | localhost | Production DNS |
| driver-pwa E2E | Included | N/A |

---

## Test Organization

### Complete Test Type Matrix

| Test Type | Location | Tag/Marker | Runs In | Requirements | Command |
|-----------|----------|------------|---------|--------------|---------|
| **Unit Tests** | `**/*Test.java` | None | ci-quality | None | `make test-ci` |
| **Mocked Integration** | `DocumentSeederIntegrationTest`<br>`ChatbotFlowIntegrationTest` | `@MockitoBean` | ci-quality | None (mocked) | `make test-ci` |
| **CI Integration** | `OrderDocumentServiceIntegrationTest`<br>`KeycloakUserServiceIntegrationTest` | `@Tag("ci-integration")` | ci-integration | Docker services | `make test-backend-integration` |
| **Real API Integration** | `PineconeRealIntegrationTest`<br>`ChatbotBffIntegrationTest`<br>`GeminiClientTest` (2 tests) | `@Tag("real-integration")` | **LOCAL DEV ONLY** | Real API keys | `make test-real` |
| **E2E Tests** | `*.spec.ts` (Playwright) | None | ci-integration | Full stack | `make test-e2e` |

### Make Commands

```bash
# CI Quality Gate (fast - ~5-10 min)
make test-ci
# Runs: Unit tests + mocked integration tests
# Maven: mvn verify (JaCoCo coverage gate enforced)

# CI Integration Tests (~10-15 min)
make test-backend-integration
# Runs: CI-safe integration tests only
# Maven: mvn test -Dgroups=ci-integration
# Requires: Docker services (Keycloak, MinIO, Kafka) running

# Real API Tests (manual verification only)
make test-real
# Runs: Tests with real external APIs
# Maven: docker exec slms-backend-dev ./mvnw test -Preal-integration
# Requires: GEMINI_API_KEY, PINECONE_API_KEY in infra/.env
# Note: Runs inside Docker container

# E2E Tests
make test-e2e
# Runs: Playwright E2E tests for all frontends
# Requires: Full stack already running (make up or make dev-up)
# Apps: admin-console, customer-portal, driver-pwa, landing-page
```

### Why Three Test Levels?

**1. Mocked Integration (`@MockitoBean` - ci-quality)**
- **Fast:** No external services
- **Reliable:** No flakiness from external dependencies
- **Purpose:** Verify business logic and mocking layer

**2. CI Integration (`@Tag("ci-integration")` - ci-integration)**
- **Real services:** Docker-based infrastructure
- **Medium speed:** Services startup overhead
- **Purpose:** Validate real integrations with Keycloak, MinIO, Kafka

**3. Real API Integration (`@Tag("real-integration")` - LOCAL ONLY)**
- **Slow:** Real network calls to Gemini, Pinecone
- **Expensive:** API rate limits and costs
- **Purpose:** Manual verification before production releases
- **Not in CI:** Too slow/expensive for every PR

---

## Composite Action (setup-slms-env)

Single source of truth for environment configuration across all workflows.

### Purpose

Eliminates 240+ lines of duplicated environment setup code by centralizing configuration in one reusable action.

### Location

`.github/actions/setup-slms-env/action.yml`

### Modes

**1. CI Mode (default)**
```yaml
- uses: ./.github/actions/setup-slms-env
  with:
    mode: ci
```
- Generates `infra/.env` for Docker Compose
- Used by: ci-integration.yml

**2. Deploy Mode**
```yaml
- uses: ./.github/actions/setup-slms-env
  with:
    mode: deploy
```
- Generates `infra/.env` AND `infra/terraform/terraform.tfvars`
- Used by: cd-deploy.yml

### Generated Files

**infra/.env:**
```bash
# Database
POSTGRES_DB=slms_db
POSTGRES_USER=slms_admin
# ... 50+ environment variables
```

**infra/terraform/terraform.tfvars:**
```hcl
postgres_db = "slms_db"
postgres_user = "slms_admin"
# ... Terraform variables
```

### Benefits

✅ Single source of truth for environment configuration
✅ Reduces workflow file sizes
✅ Easier maintenance and updates
✅ Consistent configuration across CI and deployment

---

## Secrets & Environment Variables

All secrets are configured in GitHub repository settings (`Settings > Secrets and variables > Actions`).

### Required GitHub Secrets

#### Database
```
POSTGRES_DB              # Database name (e.g., slms_db)
POSTGRES_USER            # Database admin user
POSTGRES_PASSWORD        # Database admin password
POSTGRES_PORT            # Database port (default: 5432)
DB_HOST                  # Database host
DB_PORT                  # Application database port
DB_NAME                  # Application database name
DB_USERNAME              # Application database user
DB_PASSWORD              # Application database password
```

#### Keycloak (Authentication)
```
KEYCLOAK_ADMIN                    # Keycloak admin username
KEYCLOAK_ADMIN_PASSWORD           # Keycloak admin password
KEYCLOAK_ADMIN_CLIENT_ID          # Backend service account client ID
KEYCLOAK_ADMIN_CLIENT_SECRET      # Backend service account client secret
KEYCLOAK_HOST                     # Keycloak hostname
KEYCLOAK_PORT                     # Keycloak port
KEYCLOAK_REALM                    # Keycloak realm name (default: slms)
JWT_ISSUER_URI                    # JWT token issuer URI
KEYCLOAK_ISSUER                   # Keycloak issuer URL
KEYCLOAK_BASE_URL                 # Keycloak base URL
KEYCLOAK_INTERNAL_URL             # Internal Keycloak URL
KEYCLOAK_CLIENT_ID                # Frontend client ID
KEYCLOAK_CLIENT_SECRET            # Frontend client secret

# Keycloak Configuration
KC_DB                             # Keycloak database type
KC_DB_URL                         # Keycloak database URL
KC_DB_USERNAME                    # Keycloak database user
KC_DB_PASSWORD                    # Keycloak database password
KC_HOSTNAME                       # Keycloak hostname
KC_PROXY                          # Keycloak proxy mode
KC_HOSTNAME_PORT                  # Keycloak hostname port
KC_HOSTNAME_STRICT                # Strict hostname validation
KC_HTTP_ENABLED                   # Enable HTTP
KC_PROXY_HEADERS                  # Proxy headers forwarding
KC_HOSTNAME_URL                   # Full hostname URL
```

#### MinIO (Object Storage)
```
MINIO_HOST                # MinIO hostname
MINIO_PORT                # MinIO port
MINIO_ROOT_USER           # MinIO admin user
MINIO_ROOT_PASSWORD       # MinIO admin password
MINIO_BUCKET              # Default bucket name
```

#### Kafka (Messaging)
```
KAFKA_BOOTSTRAP_SERVERS            # Kafka bootstrap servers
KAFKA_BROKER_HOST                  # Kafka broker hostname
KAFKA_BROKER_PORT                  # Kafka broker port
KAFKA_BROKER_EXTERNAL_PORT         # External Kafka port
KAFKA_ADVERTISED_HOST              # Advertised hostname
KAFKA_ZOOKEEPER_HOST               # Zookeeper hostname
KAFKA_ZOOKEEPER_PORT               # Zookeeper port
KAFKA_CONSUMER_GROUP_ID            # Consumer group ID
KAFKA_CONSUMER_AUTO_OFFSET_RESET   # Offset reset strategy
KAFKA_SECURITY_PROTOCOL            # Security protocol
```

#### External APIs (Required for Production)
```
SHIPPO_API_KEY            # Shippo shipping API key
SHIPENGINE_API_KEY        # ShipEngine API key

# Chatbot LLM (Gemini)
GEMINI_API_KEY            # Google Gemini API key (REQUIRED)
GEMINI_MODEL              # Model name (default: gemini-2.0-flash)
GEMINI_EMBEDDING_MODEL    # Embedding model (default: text-embedding-004)

# Vector Database (Pinecone)
PINECONE_API_KEY          # Pinecone API key (REQUIRED)
PINECONE_HOST             # Pinecone host URL
PINECONE_INDEX            # Index name (default: slms-chatbot)
PINECONE_NAMESPACE        # Namespace (default: en-US)
```

**Important Note on Chatbot APIs:**
- `GEMINI_API_KEY` and `PINECONE_API_KEY` **ARE** in GitHub Secrets
- Required for production deployment (chatbot is fully functional in production)
- CI tests mock these APIs to avoid rate limits and costs on every PR
- Local developers can use `make test-real` with keys in `infra/.env` for manual verification

#### NextAuth & Frontend
```
NEXTAUTH_SECRET                    # NextAuth secret key
NEXTAUTH_URL                       # NextAuth callback URL
NEXT_PUBLIC_API_URL                # Public API base URL
NEXT_PUBLIC_FLAGSMITH_ENV_KEY      # Flagsmith feature flags key
NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN    # Mapbox API token
```

#### Observability

**Backend:**
```
ELASTIC_APM_SERVER_URL      # Elastic APM server URL
ELASTIC_APM_SECRET_TOKEN    # Elastic APM authentication token
DEPLOYMENT_ENV              # Environment name (e.g., production)
PROJECT_VERSION             # Application version
TRACING_SAMPLING_RATE       # APM sampling rate (0.0 - 1.0)
LOG_LEVEL_ROOT              # Root logger level
LOG_LEVEL_APP               # Application logger level
LOG_LEVEL_WEB               # Web request logger level
LOG_LEVEL_SECURITY          # Security logger level
```

**Frontend:**
```
NEXT_PUBLIC_ELASTIC_APM_SERVER_URL      # Public APM server URL
NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN    # Public APM token
NEXT_PUBLIC_DEPLOYMENT_ENV              # Public environment name
NEXT_PUBLIC_PROJECT_VERSION             # Public version
NEXT_PUBLIC_TRACING_SAMPLING_RATE       # Public sampling rate
```

#### Application
```
BACKEND_PORT                # Backend server port
FRONTEND_PORT               # Frontend server port
NODE_ENV                    # Node environment (production/development)
NEXT_TELEMETRY_DISABLED     # Disable Next.js telemetry
PUBLIC_HOST                 # Public hostname
QR_CONFIRMATION_BASE_URL    # QR code confirmation URL
```

---

## Branch Protection Rules

### main Branch

**Required Status Checks:**
- ✅ ci-quality / Backend Quality
- ✅ ci-quality / Frontend (all 4 apps)
- ✅ ci-integration / Backend Integration Tests
- ✅ ci-integration / E2E Tests (all 4 apps)

**Additional Rules:**
- Minimum 1 approval required
- No force pushes
- Linear history preferred

### develop Branch

**Required Status Checks:**
- ✅ ci-quality / Backend Quality
- ✅ ci-quality / Frontend (all 4 apps)

**Additional Rules:**
- Minimum 1 approval required
- No force pushes

---

## Deployment Process

### Automated Deployment

Triggered automatically when code is merged to `main`:

```
git checkout main
git merge develop
git push origin main
  ↓
cd-deploy.yml triggers
  ↓
Deployment to production
```

### Manual Deployment

Can be triggered manually via GitHub Actions UI:

1. Go to **Actions** tab
2. Select **CD - Deploy to Production**
3. Click **Run workflow**
4. Select `main` branch
5. Click **Run workflow**

### Deployment Flow

```
┌─────────────────────┐
│ 1. Checkout Code    │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ 2. Setup Env        │
│   (setup-slms-env)  │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ 3. Cleanup          │
│   - Stop containers │
│   - Remove images   │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ 4. Deploy           │
│   - Terraform apply │
│   - Docker Compose  │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ 5. Restart Nginx    │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ 6. Health Checks    │
│   - Backend running?│
│   - Nginx running?  │
└─────────────────────┘
```

### Smart Deployment

The deployment process is optimized to reuse existing containers when possible:

- **Terraform state:** Preserves existing infrastructure
- **Docker layers:** Caches unchanged layers
- **Minimal downtime:** Only restarts changed services

### Health Checks

Post-deployment validation:

```bash
# Check backend is running
docker ps | grep slms-backend-prod

# Check nginx is running
docker ps | grep slms-nginx-prod
```

If health checks fail, deployment exits with error code 1.

---

## Summary

The SLMS CI/CD pipeline provides:

- **Fast feedback** with ci-quality gate (~5-10 min)
- **Comprehensive validation** with ci-integration (~30-45 min)
- **Automated deployment** to production
- **High confidence** (95%) that passing CI means successful deploy
- **Clear test organization** with three test levels
- **Secure secrets management** with GitHub Secrets
- **Zero-downtime deployments** with smart container reuse

**For developers:**
- Run `make test-ci` before creating PR
- Ensure ci-quality passes on `develop` PRs
- Ensure both ci-quality and ci-integration pass on `main` PRs
- Use `make test-real` for manual API verification when needed

**For operations:**
- Monitor deployment workflows in GitHub Actions
- Check health after each deployment
- Review logs: `docker compose -f infra/docker-compose.dev.yml logs`
