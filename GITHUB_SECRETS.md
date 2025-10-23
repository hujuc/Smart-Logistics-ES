# GitHub Secrets Configuration for Production

This document lists all the secrets you need to configure in your GitHub repository for production deployment.

## How to Add Secrets

1. Go to your GitHub repository
2. Navigate to: **Settings → Secrets and variables → Actions**
3. Click **"New repository secret"**
4. Add each secret below

---

## Required Secrets

### PostgreSQL Configuration

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `POSTGRES_DB` | `slms` | Database name |
| `POSTGRES_USER` | `slms` | Database user |
| `POSTGRES_PASSWORD` | `<strong-random-password>` | **CHANGE THIS** - Database password |
| `POSTGRES_PORT` | `5432` | Internal PostgreSQL port |
| `POSTGRES_HOST` | `postgres` | Database host (container name) |
| `POSTGRES_USERNAME` | `slms` | Database username (same as POSTGRES_USER) |

### Backend Configuration

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `BACKEND_PORT` | `8080` | Backend service port |

### Keycloak Server Configuration

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `KEYCLOAK_ADMIN` | `admin` | Keycloak admin username |
| `KEYCLOAK_ADMIN_PASSWORD` | `<strong-random-password>` | **CHANGE THIS** - Keycloak admin password |
| `KC_DB` | `postgres` | Keycloak database type |
| `KC_DB_URL` | `jdbc:postgresql://postgres:5432/slms` | Keycloak database connection URL |
| `KC_DB_USERNAME` | `slms` | Keycloak database user |
| `KC_DB_PASSWORD` | `<same-as-postgres-password>` | Keycloak database password |
| `KC_HOSTNAME` | `yourdomain.com` | **CHANGE THIS** - Your production domain |
| `KC_HOSTNAME_PORT` | `443` | External port (443 for HTTPS) |
| `KC_HOSTNAME_STRICT` | `false` | Hostname strict mode |
| `KC_HTTP_ENABLED` | `true` | Enable HTTP inside Docker network |
| `KC_PROXY_HEADERS` | `xforwarded` | Proxy header handling |
| `KC_PROXY` | `edge` | Proxy mode (edge for nginx) |

### Keycloak Connection for Backend

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `KEYCLOAK_HOST` | `keycloak` | Internal Keycloak host (container name) |
| `KEYCLOAK_PORT` | `8080` | Internal Keycloak port |
| `KEYCLOAK_REALM` | `slms` | Keycloak realm name |
| `KEYCLOAK_ADMIN_CLIENT_ID` | `slms-backend` | Backend OAuth client ID |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | `<from-keycloak-console>` | **GET FROM KEYCLOAK** - Backend client secret |

### NextAuth Configuration (Admin Console)

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `NEXTAUTH_URL` | `https://yourdomain.com/admin` | **CHANGE THIS** - External admin console URL |
| `NEXTAUTH_URL_INTERNAL` | `http://admin-console:3000/admin` | Internal admin console URL |
| `NEXTAUTH_SECRET` | `<generate-random-32-chars>` | **GENERATE THIS** - NextAuth encryption secret |
| `AUTH_TRUST_HOST` | `true` | Trust host header |

**To generate NEXTAUTH_SECRET:**
```bash
openssl rand -base64 32
```

### Admin Console Keycloak OAuth

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `KEYCLOAK_CLIENT_ID` | `slms-backend` | Frontend OAuth client ID |
| `KEYCLOAK_CLIENT_SECRET` | `<from-keycloak-console>` | **GET FROM KEYCLOAK** - Frontend client secret |
| `KEYCLOAK_BASE_URL` | `https://yourdomain.com` | **CHANGE THIS** - External Keycloak base URL |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal Keycloak URL |

### MinIO Configuration

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `MINIO_HOST` | `minio` | MinIO host (container name) |
| `MINIO_PORT` | `9000` | MinIO port |
| `MINIO_ROOT_USER` | `minioadmin` | **CHANGE THIS** - MinIO admin user |
| `MINIO_ROOT_PASSWORD` | `<strong-random-password>` | **CHANGE THIS** - MinIO admin password |
| `MINIO_BUCKET` | `slms-documents` | Default bucket name |

### Node Environment

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `NODE_ENV` | `production` | Node environment |
| `NEXT_TELEMETRY_DISABLED` | `1` | Disable Next.js telemetry |

### Legacy (Optional)

| Secret Name | Example Value | Description |
|------------|---------------|-------------|
| `FRONTEND_PORT` | `3000` | Legacy frontend port (may not be used) |

---

## Critical Secrets to Change

**DO NOT use these default values in production:**

1. ✅ **Generate new random secrets:**
   - `NEXTAUTH_SECRET` - Run: `openssl rand -base64 32`
   - `POSTGRES_PASSWORD`
   - `KEYCLOAK_ADMIN_PASSWORD`
   - `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD`

2. ✅ **Get from Keycloak Admin Console:**
   - `KEYCLOAK_ADMIN_CLIENT_SECRET`
   - `KEYCLOAK_CLIENT_SECRET`

   Steps:
   1. Login to Keycloak admin: `https://yourdomain.com/admin/master/console/`
   2. Select realm: **slms**
   3. Go to: **Clients → slms-backend**
   4. Click: **Credentials** tab
   5. Copy: **Client Secret**

3. ✅ **Update with your domain:**
   - `KC_HOSTNAME` → `yourdomain.com`
   - `NEXTAUTH_URL` → `https://yourdomain.com/admin`
   - `KEYCLOAK_BASE_URL` → `https://yourdomain.com`

---

## Verification Checklist

Before deploying:

- [ ] All secrets with `<change-this>` or `<generate-this>` have been updated
- [ ] `NEXTAUTH_SECRET` has been generated with `openssl rand -base64 32`
- [ ] All `yourdomain.com` references updated to your actual domain
- [ ] Keycloak client secrets retrieved from Keycloak admin console
- [ ] All passwords are strong and unique
- [ ] No default/dev secrets are used in production

---

## After Deployment

1. **Update Keycloak redirect URIs:**
   - Login to Keycloak admin console
   - Update `slms-backend` client redirect URIs to match production domain

2. **Verify SSL/TLS:**
   - Ensure nginx is configured for HTTPS
   - Update `KC_HTTP_ENABLED` to `false` if using end-to-end TLS

3. **Test authentication flow:**
   - Try logging into admin console
   - Verify API calls work with Keycloak tokens
