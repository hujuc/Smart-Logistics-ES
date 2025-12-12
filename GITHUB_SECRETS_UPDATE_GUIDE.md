# GitHub Secrets Update Guide

## Purpose
This guide provides instructions for updating GitHub repository secrets to point to the new EC2 instance URL.

## Background
The application has been migrated to a new AWS EC2 instance:
- **Old URL**: `ec2-35-181-9-117.eu-west-3.compute.amazonaws.com`
- **New URL**: `ec2-51-44-208-160.eu-west-3.compute.amazonaws.com`

All GitHub secrets referencing the old URL must be updated to ensure the CI/CD pipeline deploys with the correct configuration.

## How to Update GitHub Secrets

1. Navigate to your GitHub repository
2. Go to **Settings** → **Secrets and variables** → **Actions**
3. Update each secret listed below with the new value

---

## Required Secret Updates

### 1. PUBLIC_HOST
**Current Value**: `ec2-35-181-9-117.eu-west-3.compute.amazonaws.com`
**New Value**: `ec2-51-44-208-160.eu-west-3.compute.amazonaws.com`

**Purpose**: Public hostname for external access to the application

---

### 2. KC_HOSTNAME
**Current Value**: `ec2-35-181-9-117.eu-west-3.compute.amazonaws.com`
**New Value**: `ec2-51-44-208-160.eu-west-3.compute.amazonaws.com`

**Purpose**: Keycloak's public hostname (without protocol/port)

---

### 3. KC_HOSTNAME_URL
**Current Value**: `http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com:8180`
**New Value**: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com:8180`

**Purpose**: Full Keycloak URL used for issuer in JWT tokens

---

### 4. JWT_ISSUER_URI
**Current Value**: `http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com:8180/realms/slms`
**New Value**: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com:8180/realms/slms`

**Purpose**: JWT token issuer URI for backend validation

---

### 5. KEYCLOAK_BASE_URL
**Current Value**: `http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com:8180`
**New Value**: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com:8180`

**Purpose**: Base URL for Keycloak authentication service

---

### 6. NEXT_PUBLIC_API_URL
**Current Value**: `http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com/api`
**New Value**: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com/api`

**Purpose**: Frontend API base URL (visible to browser)

---

### 7. QR_CONFIRMATION_BASE_URL
**Current Value**: `http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com/api/driver`
**New Value**: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com/api/driver`

**Purpose**: Base URL for QR code confirmation endpoints

---

### 8. KAFKA_ADVERTISED_HOST
**Current Value**: `ec2-35-181-9-117.eu-west-3.compute.amazonaws.com`
**New Value**: `ec2-51-44-208-160.eu-west-3.compute.amazonaws.com`

**Purpose**: Kafka advertised hostname for external connections

---

### 9. NEXTAUTH_URL
**Current Value**: `http://ec2-35-181-9-117.eu-west-3.compute.amazonaws.com/customer`
**New Value**: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com/customer`

**Purpose**: NextAuth callback URL for customer portal authentication

---

## Verification Steps

After updating all secrets:

1. **Trigger a new deployment**:
   ```bash
   git push origin main
   # or manually trigger the workflow from GitHub Actions
   ```

2. **Monitor the deployment**:
   - Check GitHub Actions workflow execution
   - Look for any errors related to Keycloak or authentication

3. **Test the application**:
   - Visit: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com/`
   - Try logging in to verify Keycloak authentication works
   - Check that API calls are going to the correct URL

4. **Verify JWT tokens**:
   - Log in to the application
   - Check browser DevTools → Network → Response headers
   - Decode the JWT token and verify the `iss` (issuer) claim contains the new URL

---

## Local Configuration Files

The following local files have already been updated:
- ✅ `infra/terraform/terraform.tfvars`
- ✅ `infra/terraform/main.tf`
- ✅ `infra/docker-compose.yaml`

---

## Troubleshooting

### Issue: Still seeing requests to old URL
**Solution**: Clear browser cache and cookies, then restart the application containers

### Issue: Keycloak authentication fails
**Solution**:
1. Restart Keycloak container: `docker restart slms-keycloak-prod`
2. Verify `KC_HOSTNAME_URL` environment variable in Keycloak container
3. Check Keycloak logs: `docker logs slms-keycloak-prod`

### Issue: Frontend can't connect to backend
**Solution**:
1. Verify `NEXT_PUBLIC_API_URL` is set correctly in frontend containers
2. Check nginx reverse proxy configuration
3. Verify network connectivity between EC2 instance and client

---

## Important Notes

1. **Order matters**: Update all secrets before triggering a deployment
2. **Case sensitive**: Secret names must match exactly as shown above
3. **No trailing slashes**: URLs should not have trailing slashes unless specified
4. **Port numbers**: Don't forget to include port `8180` for Keycloak URLs
5. **Rebuild required**: After updating secrets, a full deployment is required to apply changes

---

## Contact

If you encounter issues after updating secrets, check:
- GitHub Actions workflow logs
- Container logs: `docker logs <container-name>`
- Keycloak admin console: `http://ec2-51-44-208-160.eu-west-3.compute.amazonaws.com:8180`
