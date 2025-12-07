# Dockerfile for User Microservice
# Utilise le JDK pour le build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY ./pom.xml ./pom.xml
COPY ./api-gateway ./api-gateway
COPY ./auth-service ./auth-service
COPY ./user-microservice ./user-microservice
COPY ./stripe-service ./stripe-service
COPY ./logging-service ./logging-service
RUN mvn -q -e -DskipTests -pl user-microservice -am clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/user-microservice/target/user-microservice-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]