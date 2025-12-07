# Logging Service

Centralized logging service for IWA Project using Apache Kafka.

## Overview

This service consumes log messages from various microservices via Kafka topics and stores them in a PostgreSQL database for analysis and monitoring.

## Features

- **Real-time Log Consumption**: Consumes logs from Kafka topics
- **Centralized Storage**: Stores all logs in PostgreSQL database
- **Query API**: RESTful API to query and analyze logs
- **Multiple Log Levels**: Support for DEBUG, INFO, WARN, ERROR, FATAL
- **Correlation Tracking**: Track related logs using correlation IDs
- **Automatic Cleanup**: Scheduled task to remove old logs (30 days retention)
- **Statistics**: Get log statistics by service and level

## Architecture

```
Microservices → Kafka Topics → Logging Service → PostgreSQL
                                       ↓
                                  REST API (Query Logs)
```

## Kafka Topics

The service listens to the following topics:
- `logs-auth-service`: Logs from Auth Service
- `logs-user-service`: Logs from User Microservice
- `logs-catalog-service`: Logs from Catalog Service
- `logs-api-gateway`: Logs from API Gateway
- `logs-stripe-service`: Logs from Stripe Service

## API Endpoints

### Get Logs by Service
```
GET /api/logs/service/{serviceName}?page=0&size=50&sortBy=timestamp&sortDir=DESC
```

### Get Logs by Level
```
GET /api/logs/level/{logLevel}?page=0&size=50
```

### Get Error Logs
```
GET /api/logs/errors?page=0&size=50
```

### Get Logs by Time Range
```
GET /api/logs/time-range?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59&page=0&size=50
```

### Get Logs by Correlation ID
```
GET /api/logs/correlation/{correlationId}
```

### Get Service Statistics
```
GET /api/logs/stats/{serviceName}
```

### Health Check
```
GET /api/logs/health
```

## Configuration

### Environment Variables

- `SERVER_PORT`: Service port (default: 8087)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address (default: kafka:9092)
- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

## Running the Service

### With Docker Compose
```bash
docker-compose up logging-service
```

### Standalone
```bash
mvn spring-boot:run
```

## Log Message Format

Log messages sent to Kafka should follow this JSON format:

```json
{
  "serviceName": "user-microservice",
  "logLevel": "INFO",
  "message": "User created successfully",
  "timestamp": "2025-11-26T10:30:00.000",
  "threadName": "http-nio-8081-exec-1",
  "loggerName": "com.iwaproject.user.service.UserService",
  "correlationId": "abc-123-def",
  "userId": "user-456",
  "requestUri": "/api/users",
  "requestMethod": "POST",
  "responseStatus": 201
}
```

## Kafka Producer Example (for other microservices)

To send logs to Kafka from other microservices, add this dependency:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

And configure a producer:

```java
@Service
public class KafkaLogProducer {
    
    @Autowired
    private KafkaTemplate<String, LogMessage> kafkaTemplate;
    
    public void sendLog(LogMessage logMessage) {
        kafkaTemplate.send("logs-your-service", logMessage);
    }
}
```

## Maintenance

### Log Retention

Logs are automatically cleaned up after 30 days. This can be configured in the `LogService.cleanupOldLogs()` method.

### Monitoring

The service exposes actuator endpoints for monitoring:
- Health: `http://localhost:8087/actuator/health`
- Metrics: `http://localhost:8087/actuator/metrics`
- Prometheus: `http://localhost:8087/actuator/prometheus`

## Development

### Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Docker Build
```bash
docker build -t iwa-logging-service:latest .
```

## Troubleshooting

### Kafka Connection Issues
- Verify Kafka broker is running: `docker ps | grep kafka`
- Check bootstrap servers configuration
- Verify network connectivity between services

### Database Issues
- Ensure PostgreSQL container is running
- Check database credentials
- Verify database URL is correct

### Missing Logs
- Check Kafka topics exist: `kafka-topics.sh --list`
- Verify producer services are sending logs
- Check consumer group status: `kafka-consumer-groups.sh --describe --group logging-service-group`
