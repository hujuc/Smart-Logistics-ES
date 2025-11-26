FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests \
  && JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" -print -quit) \
  && cp "${JAR_FILE}" app.jar \
  && rm -rf target

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /workspace/app.jar app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

# JVM optimizations for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+OptimizeStringConcat", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
