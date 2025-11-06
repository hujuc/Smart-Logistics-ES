FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests \
  && JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" -print -quit) \
  && cp "${JAR_FILE}" app.jar \
  && rm -rf target

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
