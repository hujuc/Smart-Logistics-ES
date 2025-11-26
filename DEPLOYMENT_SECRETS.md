# Deployment Secrets Configuration

This document lists all secrets that need to be added to your CI/CD platform (GitHub Secrets, GitLab CI/CD Variables, etc.) for the observability features to work in production.

## GitHub Secrets Setup

Go to: **Repository** → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

### Frontend Observability Secrets (Required)

| Secret Name | Value | Description |
|------------|-------|-------------|
| `NEXT_PUBLIC_ELASTIC_APM_SERVER_URL` | `https://b5353967fc9f4e85877feb7fbecb0c23.apm.europe-west1.gcp.cloud.es.io:443` | Elastic APM server endpoint for frontend RUM |
| `NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN` | `EZOEbP0QsCgopAKXLY` | Authentication token for Elastic APM |
| `NEXT_PUBLIC_DEPLOYMENT_ENV` | `production` (or `staging`) | Deployment environment identifier |
| `NEXT_PUBLIC_PROJECT_VERSION` | `0.0.1` or use `${{ github.sha }}` | Application version for tracking |
| `NEXT_PUBLIC_TRACING_SAMPLING_RATE` | `0.1` (10%) for production, `1.0` (100%) for staging | Percentage of traces to send |
| `NEXT_PUBLIC_API_URL` | Your production API URL (e.g., `https://api.yourapp.com`) | Backend API base URL |

### Backend Observability Secrets (If not already added)

| Secret Name | Value | Description |
|------------|-------|-------------|
| `ELASTIC_APM_SERVER_URL` | `https://b5353967fc9f4e85877feb7fbecb0c23.apm.europe-west1.gcp.cloud.es.io:443` | Elastic APM server endpoint for backend |
| `ELASTIC_APM_SECRET_TOKEN` | `EZOEbP0QsCgopAKXLY` | Authentication token for backend APM |
| `DEPLOYMENT_ENV` | `production` (or `staging`) | Backend deployment environment |
| `PROJECT_VERSION` | `0.0.1` or use `${{ github.sha }}` | Backend version |
| `TRACING_SAMPLING_RATE` | `1.0` | Backend trace sampling rate |

### Other Existing Secrets (For reference)

| Secret Name | Description |
|------------|-------------|
| `NEXT_PUBLIC_FLAGSMITH_ENV_KEY` | Feature flag management |
| `NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN` | Mapbox for driver PWA |
| `NEXTAUTH_SECRET` | NextAuth.js authentication |
| `KEYCLOAK_CLIENT_SECRET` | Keycloak OAuth2 client secret |
| `DB_PASSWORD` | PostgreSQL database password |
| `MINIO_ROOT_PASSWORD` | MinIO object storage password |

## GitHub Actions Workflow Example

Update your workflow file (e.g., `.github/workflows/deploy.yml`) to pass secrets as build arguments:

### Frontend Build Example

```yaml
name: Build and Deploy Frontend

on:
  push:
    branches: [main, develop]

jobs:
  build-frontend:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        frontend: [admin-console, customer-portal, driver-pwa, landing-page]

    steps:
      - uses: actions/checkout@v4

      - name: Build ${{ matrix.frontend }}
        run: |
          docker build \
            --build-arg FRONTEND_PATH=src/frontend/${{ matrix.frontend }} \
            --build-arg NEXT_PUBLIC_API_URL=${{ secrets.NEXT_PUBLIC_API_URL }} \
            --build-arg NEXT_PUBLIC_ELASTIC_APM_SERVER_URL=${{ secrets.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL }} \
            --build-arg NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN=${{ secrets.NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN }} \
            --build-arg NEXT_PUBLIC_DEPLOYMENT_ENV=production \
            --build-arg NEXT_PUBLIC_PROJECT_VERSION=${{ github.sha }} \
            --build-arg NEXT_PUBLIC_TRACING_SAMPLING_RATE=${{ secrets.NEXT_PUBLIC_TRACING_SAMPLING_RATE }} \
            --build-arg NEXT_PUBLIC_FLAGSMITH_ENV_KEY=${{ secrets.NEXT_PUBLIC_FLAGSMITH_ENV_KEY }} \
            --build-arg NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN=${{ secrets.NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN }} \
            -f infra/docker/frontend/Dockerfile.prod \
            -t slms-${{ matrix.frontend }}:${{ github.sha }} \
            .

      - name: Push to Registry
        run: |
          # Your registry push commands here
```

### Backend Build Example

```yaml
  build-backend:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Build Backend
        run: |
          docker build \
            -f infra/docker/backend/Dockerfile \
            -t slms-backend:${{ github.sha }} \
            .

      - name: Deploy with Environment Variables
        run: |
          docker run -d \
            -e ELASTIC_APM_SERVER_URL=${{ secrets.ELASTIC_APM_SERVER_URL }} \
            -e ELASTIC_APM_SECRET_TOKEN=${{ secrets.ELASTIC_APM_SECRET_TOKEN }} \
            -e DEPLOYMENT_ENV=production \
            -e PROJECT_VERSION=${{ github.sha }} \
            -e TRACING_SAMPLING_RATE=${{ secrets.TRACING_SAMPLING_RATE }} \
            slms-backend:${{ github.sha }}
```

## Docker Compose Production Example

For production deployment using Docker Compose:

```yaml
# docker-compose.prod.yml
services:
  admin-console:
    build:
      context: ..
      dockerfile: infra/docker/frontend/Dockerfile.prod
      args:
        FRONTEND_PATH: src/frontend/admin-console
        NEXT_PUBLIC_API_URL: ${NEXT_PUBLIC_API_URL}
        NEXT_PUBLIC_ELASTIC_APM_SERVER_URL: ${NEXT_PUBLIC_ELASTIC_APM_SERVER_URL}
        NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN: ${NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN}
        NEXT_PUBLIC_DEPLOYMENT_ENV: ${NEXT_PUBLIC_DEPLOYMENT_ENV}
        NEXT_PUBLIC_PROJECT_VERSION: ${NEXT_PUBLIC_PROJECT_VERSION}
        NEXT_PUBLIC_TRACING_SAMPLING_RATE: ${NEXT_PUBLIC_TRACING_SAMPLING_RATE}
```

## Kubernetes Secrets Example

If deploying to Kubernetes:

```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: observability-secrets
type: Opaque
stringData:
  NEXT_PUBLIC_ELASTIC_APM_SERVER_URL: "https://b5353967fc9f4e85877feb7fbecb0c23.apm.europe-west1.gcp.cloud.es.io:443"
  NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN: "EZOEbP0QsCgopAKXLY"
  NEXT_PUBLIC_DEPLOYMENT_ENV: "production"
  NEXT_PUBLIC_TRACING_SAMPLING_RATE: "0.1"
```

Then reference in deployment:

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: slms-admin-console
spec:
  template:
    spec:
      containers:
      - name: admin-console
        image: slms-admin-console:latest
        env:
        - name: NEXT_PUBLIC_ELASTIC_APM_SERVER_URL
          valueFrom:
            secretKeyRef:
              name: observability-secrets
              key: NEXT_PUBLIC_ELASTIC_APM_SERVER_URL
        # ... other env vars
```

## Environment-Specific Configuration

### Development
```bash
NEXT_PUBLIC_DEPLOYMENT_ENV=development
NEXT_PUBLIC_TRACING_SAMPLING_RATE=1.0  # 100% sampling
NEXT_PUBLIC_API_URL=http://localhost
```

### Staging
```bash
NEXT_PUBLIC_DEPLOYMENT_ENV=staging
NEXT_PUBLIC_TRACING_SAMPLING_RATE=1.0  # 100% sampling
NEXT_PUBLIC_API_URL=https://staging-api.yourapp.com
```

### Production
```bash
NEXT_PUBLIC_DEPLOYMENT_ENV=production
NEXT_PUBLIC_TRACING_SAMPLING_RATE=0.1  # 10% sampling (adjust based on traffic)
NEXT_PUBLIC_API_URL=https://api.yourapp.com
```

## Security Best Practices

### ⚠️ Important Notes

1. **Secret Token Exposure**: The `NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN` will be **visible in browser JavaScript** because it uses the `NEXT_PUBLIC_` prefix. This is acceptable for Elastic APM RUM as:
   - The token is write-only (can only send data, not read)
   - Elastic APM server validates requests
   - Browser-based RUM requires client-accessible credentials

2. **Production Sampling**: Set `NEXT_PUBLIC_TRACING_SAMPLING_RATE` to a lower value (e.g., `0.1` = 10%) in production to:
   - Reduce costs
   - Minimize performance impact
   - Still capture representative data

3. **Rotate Tokens Regularly**:
   - Update Elastic APM tokens every 90 days
   - Update all environments when rotating
   - Test staging before production updates

4. **Separate Environments**: Use different Elastic deployments for:
   - Development/Staging → One deployment
   - Production → Separate deployment with stricter access controls

## Verification Checklist

After adding secrets and deploying:

- [ ] Check GitHub Actions workflow runs successfully
- [ ] Verify build logs show environment variables are set
- [ ] Deploy to staging/production
- [ ] Open browser console and check for APM initialization messages
- [ ] Go to Elastic Cloud → Kibana → APM → Services
- [ ] Verify frontend services appear with correct environment tags
- [ ] Click on a service and verify traces are coming in
- [ ] Check Service Map shows frontend → backend connections
- [ ] Verify Web Vitals are being captured

## Troubleshooting

### Build Fails with "ARG not found"
- Ensure Dockerfile has all ARG declarations
- Check workflow passes all required `--build-arg` flags

### No Data in Elastic APM
- Verify secrets are correctly named (case-sensitive)
- Check browser network tab for requests to APM endpoint
- Verify firewall allows outbound HTTPS to Elastic Cloud
- Check browser console for initialization errors

### Traces Not Connecting Frontend → Backend
- Verify `NEXT_PUBLIC_API_URL` matches actual API URL
- Check CORS configuration on backend
- Verify backend APM agent is running

## When Elastic Free Trial Ends

To migrate to a new Elastic deployment:

1. Create new Elastic Cloud deployment
2. Get new APM server URL and secret token
3. Update GitHub Secrets with new values:
   - `NEXT_PUBLIC_ELASTIC_APM_SERVER_URL`
   - `NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN`
   - `ELASTIC_APM_SERVER_URL` (backend)
   - `ELASTIC_APM_SECRET_TOKEN` (backend)
4. Re-deploy all services
5. Export/import alert rules and dashboards from old deployment

## Support

For issues:
1. Check workflow logs in GitHub Actions
2. Check application logs: `docker logs <container-name>`
3. Verify secrets are set: Settings → Secrets → Actions
4. Test locally with same environment variables
5. Check Elastic APM documentation: https://www.elastic.co/guide/en/apm/agent/rum-js/current/index.html
