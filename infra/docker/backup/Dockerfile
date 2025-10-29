FROM postgres:16-alpine

# Install required tools including crond
RUN apk add --no-cache \
    bash \
    gzip \
    postgresql-client \
    dcron \
    && rm -rf /var/cache/apk/*

# Copy backup script
COPY infra/scripts/backup.sh /usr/local/bin/backup.sh
RUN chmod +x /usr/local/bin/backup.sh

# Create backup directory with proper permissions
RUN mkdir -p /backups && chmod 755 /backups

# Create crontab for automatic backups (daily at 2:00 AM)
RUN echo "0 2 * * * /usr/local/bin/backup.sh >> /var/log/backup.log 2>&1" > /etc/crontabs/root

# Create log file
RUN touch /var/log/backup.log && chmod 644 /var/log/backup.log

WORKDIR /backups

# Start cron daemon in foreground
CMD ["crond", "-f", "-l", "2"]
