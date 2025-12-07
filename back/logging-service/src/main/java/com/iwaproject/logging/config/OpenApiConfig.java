package com.iwaproject.logging.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Logging Service
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loggingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logging Service API")
                        .description("Centralized logging service that collects and manages logs from all microservices using Kafka")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IWA Project Team")
                                .email("support@iwaproject.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Local development server"),
                        new Server()
                                .url("http://logging-service:8084")
                                .description("Docker container")
                ));
    }
}
