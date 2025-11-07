ARG NODE_VERSION=20

FROM node:${NODE_VERSION}-alpine AS deps
ARG FRONTEND_PATH
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY ${FRONTEND_PATH}/package.json ./package.json
COPY ${FRONTEND_PATH}/package-lock.json ./package-lock.json
RUN --mount=type=cache,target=/root/.npm npm ci

FROM node:${NODE_VERSION}-alpine AS builder
ARG FRONTEND_PATH
ARG NEXT_PUBLIC_API_URL
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
COPY --from=deps /app/node_modules ./node_modules
COPY ${FRONTEND_PATH}/ ./
RUN npm run build \
  && npm prune --omit=dev \
  && rm -rf .next/cache node_modules/.cache

FROM node:${NODE_VERSION}-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
