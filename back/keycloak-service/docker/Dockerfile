FROM openjdk:17-jdk-slim AS build
VOLUME /tmp
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build target/keycloak-service-0.0.1-SNAPSHOT.jar /app/keycloak-service.jar
ENTRYPOINT ["java","-jar","/app/keycloak-service.jar"]