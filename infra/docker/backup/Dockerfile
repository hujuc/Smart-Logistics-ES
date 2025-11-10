FROM postgres:16-alpine

# Install required tools for manual backups triggered by CI/CD
RUN apk add --no-cache \
    bash \
    gzip \
    postgresql-client \
    && rm -rf /var/cache/apk/*

# Copy backup script
COPY infra/scripts/backup.sh /usr/local/bin/backup.sh
RUN chmod +x /usr/local/bin/backup.sh

# Create backup directory with proper permissions
RUN mkdir -p /backups && chmod 755 /backups

WORKDIR /backups

# Keep container alive so workflow/manual execs can run the script on demand
CMD ["sleep", "infinity"]
