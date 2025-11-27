# Dockerfile for Logging Service
# Multi-stage build for optimized image size

# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy parent pom and all module sources
COPY ./pom.xml ./pom.xml
COPY ./api-gateway ./api-gateway
COPY ./auth-service ./auth-service
COPY ./user-microservice ./user-microservice
COPY ./stripe-service ./stripe-service
COPY ./logging-service ./logging-service

# Build only the logging-service module
# The -am flag also builds requested dependencies from the reactor
RUN mvn -q -e -DskipTests -pl logging-service -am clean package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /workspace/logging-service/target/logging-service-*.jar /app/app.jar

# Expose the service port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
