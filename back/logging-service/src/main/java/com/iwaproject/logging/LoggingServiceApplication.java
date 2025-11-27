package com.iwaproject.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main application class for the Logging Service
 * This service consumes logs from Kafka topics and stores them for analysis
 */
@SpringBootApplication
@EnableKafka
public class LoggingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingServiceApplication.class, args);
    }
}
