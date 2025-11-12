# AWS Deployment Configuration

## Problem Solved

Frontend apps were hardcoded to use `localhost`, causing API requests to fail when deployed on AWS EC2.

## Solution

Frontend apps now use **build-time environment variables** (`NEXT_PUBLIC_*`) that are configured via Terraform's `public_host` variable.

## Quick Setup for AWS EC2

### Option 1: Automatic Setup (Recommended)

```bash
cd infra
./setup-aws.sh
./deploy.sh deploy
```

The script will:
1. Auto-detect your AWS EC2 public IP
2. Create `terraform/terraform.tfvars` with the correct `public_host`
3. You can then run `./deploy.sh deploy`

### Option 2: Manual Setup

```bash
# 1. Get your EC2 public IP
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
echo $PUBLIC_IP

# 2. Create terraform.tfvars
cd infra/terraform
cat > terraform.tfvars <<EOF
public_host = "$PUBLIC_IP"
docker_network_name = "slms-network"
EOF

# 3. Deploy
cd ..
./deploy.sh deploy
```

## Local Development

For local development, leave `public_host` empty (it defaults to `localhost`):

```bash
cd infra/terraform
cat > terraform.tfvars <<EOF
public_host = ""
docker_network_name = "slms-network"
EOF

cd ..
./deploy.sh deploy
```

## How It Works

1. **Dockerfile**: Accepts `NEXT_PUBLIC_*` as build arguments
2. **Terraform**: Passes `public_host` to Dockerfile build args
3. **Next.js**: Inlines these variables during `npm run build`
4. **Result**: Frontend JavaScript has correct URLs baked in

## Variables Set Per Frontend

### Admin Console
- `NEXT_PUBLIC_API_URL`: `http://<public_host>/api`
- `NEXT_PUBLIC_KEYCLOAK_URL`: `http://<public_host>:8180`
- `NEXT_PUBLIC_KEYCLOAK_REALM`: `slms`
- `NEXT_PUBLIC_KEYCLOAK_CLIENT_ID`: `slms-admin-console`

### Customer Portal
- `NEXT_PUBLIC_API_URL`: `http://<public_host>/api`
- `NEXT_PUBLIC_KEYCLOAK_URL`: `http://<public_host>:8180`
- `NEXT_PUBLIC_KEYCLOAK_REALM`: `slms`
- `NEXT_PUBLIC_KEYCLOAK_CLIENT_ID`: `slms-customer-portal`
- `KEYCLOAK_BASE_URL`: `http://<public_host>:8180`
- `NEXTAUTH_URL`: `http://<public_host>:3002`

### Driver PWA
- `NEXT_PUBLIC_API_URL`: `http://<public_host>/api/driver`
- `NEXT_PUBLIC_KEYCLOAK_URL`: `http://<public_host>:8180`
- `NEXT_PUBLIC_KEYCLOAK_REALM`: `slms`
- `NEXT_PUBLIC_KEYCLOAK_CLIENT_ID`: `slms-driver-pwa`

### Landing Page
- `NEXT_PUBLIC_API_URL`: `http://<public_host>/api/landing-page`

## Rebuilding After Changing public_host

When you change `public_host`, you **must rebuild** the frontend images:

```bash
cd infra
./deploy.sh down    # Stop and remove containers
./deploy.sh deploy  # Rebuild with new public_host
```

## Troubleshooting

### Frontend still uses localhost
- Check `terraform.tfvars` has correct `public_host`
- Rebuild: `./deploy.sh down && ./deploy.sh deploy`
- Check build logs: `docker logs slms-admin-console-prod`

### Images not rebuilding
Terraform caches images. Force rebuild:
```bash
cd infra/terraform
terraform taint docker_image.admin_console
terraform taint docker_image.customer_portal
terraform taint docker_image.driver_pwa
terraform taint docker_image.landing_page
terraform apply
```
