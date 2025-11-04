# syntax=docker/dockerfile:1.7

# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy only POM first to leverage layer caching for dependencies
COPY pom.xml .
# Pre-fetch dependencies (cached between builds)
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Now copy the sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# (Optional but recommended) run as non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar from the builder (works for SNAPSHOT or release versions)
COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]