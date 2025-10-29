# Keycloak Setup Guide

## First Time Setup

### 1. Start Keycloak

```bash
make dev-up
```

### 2. Import Realm

1. Open **http://localhost:8180**
2. Login with admin credentials (from docker-compose)
3. Click realm dropdown (top-left, shows "master")
4. Click **"Create Realm"**
5. Click **"Browse"** → select `infra/keycloak/realm-export.json`
6. Click **"Create"**

### 3. Get Client Secret

1. Select **"slms"** realm
2. Go to **Clients** → **slms-backend**
3. Click **Credentials** tab
4. Copy the **Client Secret**

### 4. Configure Backend

Add to `src/backend/.env` or `application.properties`:

```env
KEYCLOAK_ADMIN_CLIENT_SECRET=<paste-secret-here>
```

### 5. Restart

```bash
make clean
make dev-up
```

## Verify Setup

### Test Authentication

```bash
curl -X POST http://localhost:8180/realms/slms/protocol/openid-connect/token \
  -d "client_id=slms-backend" \
  -d "client_secret=<your-secret>" 
  -d "username=customer" \
  -d "password=customer" \
  -d "grant_type=password"
```

Should return JSON with `access_token`.

## Clean Start

To start fresh:

```bash
make clean-volumes
make dev-up
# Then repeat steps 2-5
```
