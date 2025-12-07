# Quick Start Guide - Logging Service with Kafka

This guide will help you get started with the new centralized logging service using Apache Kafka.

## Prerequisites

- Docker and Docker Compose installed
- All microservices running
- Kafka broker running (configured without Zookeeper using KRaft mode)

## Step 1: Start the Infrastructure

Start all services including Kafka and the logging service:

```bash
cd /home/etienne/Documents/IWAPROJECT/back
docker-compose up -d
```

This will start:
- PostgreSQL databases (users, catalog, keycloak, logs)
- Kafka broker (using KRaft mode)
- All microservices
- Logging service

## Step 2: Verify Kafka is Running

Check that Kafka container is running:

```bash
docker ps | grep kafka
```

You should see `iwa-kafka` running on port 9092.

## Step 3: Create Kafka Topics

Use the provided script to create all required topics:

```bash
chmod +x scripts/manage-kafka.sh
./scripts/manage-kafka.sh create
```

This creates the following topics:
- `logs-auth-service`
- `logs-user-service`
- `logs-catalog-service`
- `logs-api-gateway`
- `logs-stripe-service`

## Step 4: Verify Topics Were Created

List all topics:

```bash
./scripts/manage-kafka.sh list
```

Get details about a specific topic:

```bash
./scripts/manage-kafka.sh describe logs-user-service
```

## Step 5: Test the Logging Service

### Test with Console Producer/Consumer

1. **Open a producer** to send test messages:

```bash
./scripts/manage-kafka.sh produce logs-user-service
```

2. **In another terminal, open a consumer**:

```bash
./scripts/manage-kafka.sh consume logs-user-service
```

3. **Type a test message** in the producer terminal and press Enter. You should see it appear in the consumer terminal.

### Test with JSON Messages

Access the Kafka container:

```bash
docker exec -it iwa-kafka sh
cd /opt/kafka/bin
```

Produce a JSON log message:

```bash
./kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic logs-user-service \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Then paste this JSON message:

```json
user-123:{"serviceName":"user-microservice","logLevel":"INFO","message":"User created successfully","timestamp":"2025-11-26T10:30:00.000","threadName":"http-nio-8081-exec-1","loggerName":"com.iwaproject.user.service.UserService","correlationId":"abc-123","userId":"user-456","requestUri":"/api/users","requestMethod":"POST","responseStatus":201}
```

## Step 6: Query Logs via REST API

The logging service exposes a REST API on port 8087.

### Get all logs from a service:

```bash
curl "http://localhost:8087/api/logs/service/user-microservice?page=0&size=10"
```

### Get error logs:

```bash
curl "http://localhost:8087/api/logs/errors?page=0&size=10"
```

### Get logs by level:

```bash
curl "http://localhost:8087/api/logs/level/ERROR?page=0&size=10"
```

### Get logs by correlation ID:

```bash
curl "http://localhost:8087/api/logs/correlation/abc-123"
```

### Get service statistics:

```bash
curl "http://localhost:8087/api/logs/stats/user-microservice"
```

### Get logs within time range:

```bash
curl "http://localhost:8087/api/logs/time-range?start=2025-11-26T00:00:00&end=2025-11-26T23:59:59&page=0&size=10"
```

## Step 7: Check Logging Service Health

```bash
curl http://localhost:8087/api/logs/health
curl http://localhost:8087/actuator/health
```

## Step 8: Monitor Consumer Groups

Check the status of the logging service consumer group:

```bash
./scripts/manage-kafka.sh list-groups
./scripts/manage-kafka.sh describe-group logging-service-group
```

This shows:
- Current offset
- Log end offset
- Lag (how far behind the consumer is)
- Consumer ID
- Host
- Partition assignment

## Advanced: Kafka Commands Reference

### Inside Kafka Container

Access the Kafka container shell:

```bash
docker exec -it iwa-kafka sh
cd /opt/kafka/bin
```

### Create a Topic Manually

```bash
./kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic test-topic \
  --partitions 3 \
  --replication-factor 1
```

### List Topics

```bash
./kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Describe Topic

```bash
./kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic logs-user-service
```

### Modify Partitions

```bash
./kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --alter \
  --topic logs-user-service \
  --partitions 10
```

### Delete a Topic

```bash
./kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic test-topic
```

### Console Producer with Key:Value

```bash
./kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic logs-user-service \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Then type messages like:
```
key1:value1
key2:value2
```

### Console Consumer with Key Display

```bash
./kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic logs-user-service \
  --property print.key=true \
  --from-beginning
```

### Consumer with Specific Group

```bash
./kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic logs-user-service \
  --consumer-property group.id=mygroup \
  --from-beginning
```

### List Consumer Groups

```bash
./kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list
```

### Describe Consumer Group

```bash
./kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group logging-service-group
```

## Troubleshooting

### Kafka Not Starting

1. Check container logs:
```bash
docker logs iwa-kafka
```

2. Ensure port 9092 is not in use:
```bash
lsof -i :9092
```

3. Restart Kafka:
```bash
docker-compose restart kafka
```

### Logging Service Not Receiving Messages

1. Check if topics exist:
```bash
./scripts/manage-kafka.sh list
```

2. Check consumer group status:
```bash
./scripts/manage-kafka.sh describe-group logging-service-group
```

3. Check logging service logs:
```bash
docker logs iwa-logging-service
```

4. Verify Kafka connectivity from logging service:
```bash
docker exec iwa-logging-service ping kafka
```

### Messages Not Persisting

1. Check database connection:
```bash
docker logs iwa-postgres-logs
```

2. Check logging service application logs:
```bash
docker logs iwa-logging-service -f
```

3. Verify database exists:
```bash
docker exec -it iwa-postgres-logs psql -U postgres -d iwa_logs -c "\dt"
```

## Production Considerations

### Kafka Configuration

For production, consider:

1. **Multiple brokers** for high availability
2. **Increased replication factor** (at least 3)
3. **More partitions** for better parallelism
4. **Appropriate retention policies**

### Log Retention

The current setup keeps logs for:
- **In Kafka**: 7 days (168 hours)
- **In Database**: 30 days (configurable in LogService)

Adjust these based on your requirements.

### Monitoring

Monitor:
- Kafka broker health
- Consumer lag
- Database size
- Service performance

Use the actuator endpoints:
```bash
curl http://localhost:8087/actuator/metrics
curl http://localhost:8087/actuator/prometheus
```

## Next Steps

To integrate other microservices with Kafka:

1. Add Kafka producer dependency to the microservice
2. Configure Kafka producer
3. Create a service to send logs to Kafka
4. Configure appropriate topic names

See the logging service README for detailed examples.
