# Dockerfile for API Gateway
# Multi-stage build for API Gateway
# Stage 1: Build with Maven from repo root so parent POM is available
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
# Copy root pom and module sources
COPY ./pom.xml ./pom.xml
COPY ./api-gateway ./api-gateway
COPY ./auth-service ./auth-service
COPY ./user-microservice ./user-microservice
# Build only the api-gateway module and its dependencies, skip tests
RUN mvn -q -e -DskipTests -pl api-gateway -am clean package

# Stage 2: Runtime image with only the built jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]