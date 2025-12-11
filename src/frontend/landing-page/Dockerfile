FROM node:20-alpine AS deps
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1

COPY package.json package-lock.json* ./
RUN npm ci

FROM node:20-alpine AS builder
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1

# Accept build arguments for Next.js public environment variables
ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_ELASTIC_APM_SERVER_URL
ARG NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN
ARG NEXT_PUBLIC_DEPLOYMENT_ENV
ARG NEXT_PUBLIC_PROJECT_VERSION
ARG NEXT_PUBLIC_TRACING_SAMPLING_RATE
ARG NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN

# Set as environment variables so Next.js can embed them during build
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_ELASTIC_APM_SERVER_URL=$NEXT_PUBLIC_ELASTIC_APM_SERVER_URL
ENV NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN=$NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN
ENV NEXT_PUBLIC_DEPLOYMENT_ENV=$NEXT_PUBLIC_DEPLOYMENT_ENV
ENV NEXT_PUBLIC_PROJECT_VERSION=$NEXT_PUBLIC_PROJECT_VERSION
ENV NEXT_PUBLIC_TRACING_SAMPLING_RATE=$NEXT_PUBLIC_TRACING_SAMPLING_RATE
ENV NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN=$NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN

COPY --from=deps /app/node_modules ./node_modules
COPY . .
# Copy shared components from parent directory
COPY ../shared ../shared

RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs
EXPOSE 3000
CMD ["node", "server.js"]
