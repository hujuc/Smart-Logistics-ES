#!/bin/bash

# Script to sync shared library files to all frontends
# Run this script after making changes to src/frontend/shared/

echo "🔄 Syncing shared library files to all frontends..."

FRONTENDS=("landing-page" "admin-console" "customer-portal" "driver-pwa")
SHARED_SOURCE="./shared"

for frontend in "${FRONTENDS[@]}"; do
  TARGET="./$frontend/shared"

  echo "  📦 Syncing to $frontend..."

  # Create shared directory if it doesn't exist
  mkdir -p "$TARGET"

  # Copy all files from shared to frontend/shared
  cp -r "$SHARED_SOURCE"/* "$TARGET"/

  echo "  ✅ $frontend updated"
done

echo ""
echo "✨ Sync complete! All frontends have the latest shared library."
echo ""
echo "💡 Tip: Run this script whenever you modify files in src/frontend/shared/"
