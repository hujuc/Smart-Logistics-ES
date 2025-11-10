#!/bin/bash

# Quick Setup Script for Backup System on VM
# Run this on your VM: bash setup-backup.sh

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║     SLMS Database Backup System - VM Setup                    ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Check if we're in the right directory
if [ ! -f "infra/docker-compose.yaml" ]; then
    echo "❌ Error: Must run from project root directory (~/actio)"
    echo "   Current directory: $(pwd)"
    echo ""
    echo "Please run:"
    echo "  cd ~/actio"
    echo "  bash setup-backup.sh"
    exit 1
fi

echo "✓ In correct directory"
echo ""

# Check if .env exists
if [ ! -f "infra/.env" ]; then
    echo "❌ Error: infra/.env not found!"
    echo "   Please create it first from .env.example"
    exit 1
fi

echo "✓ .env file found"
echo ""

# Check if MAX_BACKUPS is already set
if grep -q "^MAX_BACKUPS=" infra/.env; then
    echo "✓ MAX_BACKUPS already configured in .env"
else
    echo "Adding MAX_BACKUPS=4 to .env..."
    echo "" >> infra/.env
    echo "# Backup Configuration" >> infra/.env
    echo "MAX_BACKUPS=4" >> infra/.env
    echo "✓ MAX_BACKUPS added to .env"
fi
echo ""

# Check Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running or you don't have permissions"
    echo ""
    echo "If you get permission errors, run:"
    echo "  sudo usermod -aG docker $USER"
    echo "  Then log out and back in"
    exit 1
fi

echo "✓ Docker is accessible"
echo ""

# Build and start services
echo "Building and starting services..."
echo "(This may take a few minutes)"
echo ""

docker compose -f infra/docker-compose.yaml up -d --build backup

echo ""
echo "✓ Services started"
echo ""

# Wait for services to be ready
echo "Waiting for services to initialize..."
sleep 10

# Check backup service status
echo ""
echo "Checking backup service status..."
if docker compose -f infra/docker-compose.yaml ps backup | grep -q "Up"; then
    echo "✓ Backup service is running"
else
    echo "⚠️  Warning: Backup service may not be running properly"
    echo "   Check logs with: docker compose -f infra/docker-compose.yaml logs backup"
fi
echo ""

# Check if cron is running
echo "Checking cron scheduler..."
if docker compose -f infra/docker-compose.yaml exec -T backup ps aux 2>/dev/null | grep -q "[c]rond"; then
    echo "✓ Cron scheduler is running"
else
    echo "⚠️  Warning: Cron may not be running"
fi
echo ""

# Test manual backup
echo "════════════════════════════════════════════════════════════════"
echo "Testing manual backup..."
echo "════════════════════════════════════════════════════════════════"
echo ""

if docker compose -f infra/docker-compose.yaml exec -T backup /usr/local/bin/backup.sh; then
    echo ""
    echo "✓ Manual backup test successful!"
else
    echo ""
    echo "⚠️  Backup test failed - check logs"
fi
echo ""

# List backups
echo "════════════════════════════════════════════════════════════════"
echo "Available backups:"
echo "════════════════════════════════════════════════════════════════"
docker compose -f infra/docker-compose.yaml exec -T backup ls -lh /backups 2>/dev/null || echo "No backups yet"
echo ""

# Show summary
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                    Setup Complete!                             ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "📅 Automatic backups will run daily at 2:00 AM"
echo "💾 Only the last 4 backups will be kept"
echo ""
echo "Useful commands:"
echo "  • List backups:"
echo "    docker compose -f infra/docker-compose.yaml exec backup ls -lh /backups"
echo ""
echo "  • Check logs:"
echo "    docker compose -f infra/docker-compose.yaml logs backup"
echo ""
echo "  • Manual backup:"
echo "    docker compose -f infra/docker-compose.yaml exec backup /usr/local/bin/backup.sh"
echo ""
echo "  • Download backup:"
echo "    docker cp slms-backup:/backups/backup_slms_YYYYMMDD_HHMMSS.sql.gz ./"
echo ""
echo "For more help, see: VM_QUICK_CHECK.md"
echo ""
