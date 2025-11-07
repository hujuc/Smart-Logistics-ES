
# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy apenas o pom.xml primeiro para cache de dependências
COPY src/backend/pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

# Copy source code
COPY src/backend/src ./src

# Build com otimizações
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests -Dmaven.javadoc.skip=true \
  && JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" -print -quit) \
  && cp "${JAR_FILE}" app.jar

# Stage 2: Runtime (mais leve com Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

# Copy apenas o JAR necessário
COPY --from=builder --chown=appuser:appgroup /workspace/app.jar app.jar

# Trocar para usuário não-root
USER appuser

EXPOSE 8080

# Otimizações JVM para containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+OptimizeStringConcat", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
