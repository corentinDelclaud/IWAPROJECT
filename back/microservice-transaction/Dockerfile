# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy only this service (standalone build)
COPY ./microservice-transaction/pom.xml ./pom.xml
COPY ./microservice-transaction/src ./src

# Build the service
RUN mvn -q -e -DskipTests clean package

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/transaction-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
