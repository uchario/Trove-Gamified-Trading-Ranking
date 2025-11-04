# syntax=docker/dockerfile:1.7

# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Cache deps
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ---- Runtime Stage (Debian-based, multi-arch) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user (Debian)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy artifact
COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]