# Dockerfile for Service Catalog (Product Service)
# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy only this service (standalone build)
COPY ./service-catalog/pom.xml ./pom.xml
COPY ./service-catalog/src ./src

# Build the service
RUN mvn -q -e -DskipTests clean package

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/service-product-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
