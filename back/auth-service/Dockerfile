# Dockerfile for Auth Service
# Utilise le JDK pour le build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY ./pom.xml ./pom.xml
COPY ./api-gateway ./api-gateway
COPY ./auth-service ./auth-service
COPY ./user-microservice ./user-microservice
COPY ./stripe-service ./stripe-service
RUN mvn -q -e -DskipTests -pl auth-service -am clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]