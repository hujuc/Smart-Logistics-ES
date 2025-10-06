ARG NODE_VERSION=20

FROM node:${NODE_VERSION}-alpine AS deps
ARG FRONTEND_PATH
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY ${FRONTEND_PATH}/package.json ./package.json
COPY ${FRONTEND_PATH}/package-lock.json ./package-lock.json
RUN npm ci

FROM node:${NODE_VERSION}-alpine AS builder
ARG FRONTEND_PATH
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY ${FRONTEND_PATH}/ ./
RUN npm run build

FROM node:${NODE_VERSION}-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
COPY --from=builder /app .
EXPOSE 3000
CMD ["npm", "run", "start"]