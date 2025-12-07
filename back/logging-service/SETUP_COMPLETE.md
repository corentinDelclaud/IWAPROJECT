# Logging Service - Setup Complete! 🎉

## What Was Created

I've successfully created a centralized logging service for your IWA Project using Apache Kafka (KRaft mode - no Zookeeper). Here's what was added:

### 📁 New Service Structure

```
back/logging-service/
├── src/
│   ├── main/
│   │   ├── java/com/iwaproject/logging/
│   │   │   ├── LoggingServiceApplication.java
│   │   │   ├── config/
│   │   │   │   └── KafkaConsumerConfig.java
│   │   │   ├── consumer/
│   │   │   │   └── LogConsumer.java
│   │   │   ├── controller/
│   │   │   │   └── LogController.java
│   │   │   ├── model/
│   │   │   │   ├── LogEntry.java
│   │   │   │   └── LogMessage.java
│   │   │   ├── repository/
│   │   │   │   └── LogEntryRepository.java
│   │   │   └── service/
│   │   │       └── LogService.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/iwaproject/logging/
│           └── service/
│               └── LogServiceTest.java
├── Dockerfile
├── pom.xml
├── README.md
└── QUICK_START.md
```

### 🔧 Updated Files

1. **back/pom.xml** - Added logging-service module
2. **back/docker-compose.yml** - Added:
   - Kafka broker (KRaft mode - no Zookeeper!)
   - PostgreSQL database for logs
   - Logging service container
3. **Dockerfiles** - Updated all service Dockerfiles to include logging-service in build context:
   - auth-service/Dockerfile
   - user-microservice/Dockerfile
   - api-gateway/Dockerfile

### 🛠️ New Utilities

1. **scripts/manage-kafka.sh** - Utility script for managing Kafka topics and consumers
2. **logging-service/QUICK_START.md** - Comprehensive getting started guide

## 🚀 Quick Start

### Step 1: Start All Services

```bash
cd /home/etienne/Documents/IWAPROJECT/back
docker-compose up -d
```

This will start:
- ✅ Kafka broker (port 9092) - using KRaft (no Zookeeper)
- ✅ PostgreSQL for logs (port 5436)
- ✅ Logging service (port 8084)
- ✅ All existing microservices

### Step 2: Create Kafka Topics

```bash
./scripts/manage-kafka.sh create
```

This creates topics for all your services:
- logs-auth-service
- logs-user-service
- logs-catalog-service
- logs-api-gateway
- logs-stripe-service

### Step 3: Verify Everything Works

Check Kafka topics:
```bash
./scripts/manage-kafka.sh list
```

Check logging service health:
```bash
curl http://localhost:8084/api/logs/health
```

### Step 4: Test with Sample Log

Produce a test log message:
```bash
./scripts/manage-kafka.sh produce logs-user-service
```

Then paste this JSON and press Enter:
```json
{"serviceName":"user-microservice","logLevel":"INFO","message":"Test log message","timestamp":"2025-11-26T10:30:00.000"}
```

Query the log via API:
```bash
curl "http://localhost:8084/api/logs/service/user-microservice?page=0&size=10"
```

## 📊 Service Ports

- **Kafka**: 9092
- **Logging Service**: 8084
- **Logs Database**: 5436

## 🔑 Key Features

### Kafka Configuration (KRaft Mode)
- ✅ No Zookeeper dependency
- ✅ Simplified architecture
- ✅ 3 partitions per topic
- ✅ 7-day message retention
- ✅ Health checks enabled

### Logging Service Features
- ✅ Real-time log consumption from Kafka
- ✅ PostgreSQL storage with 30-day retention
- ✅ REST API for querying logs
- ✅ Correlation ID tracking
- ✅ Multiple log levels (DEBUG, INFO, WARN, ERROR, FATAL)
- ✅ Automatic cleanup of old logs
- ✅ Service statistics

### REST API Endpoints

```bash
# Get logs by service
GET /api/logs/service/{serviceName}

# Get error logs only
GET /api/logs/errors

# Get logs by level
GET /api/logs/level/{logLevel}

# Get logs by time range
GET /api/logs/time-range?start=...&end=...

# Get logs by correlation ID
GET /api/logs/correlation/{correlationId}

# Get service statistics
GET /api/logs/stats/{serviceName}
```

## 📝 Kafka Management Commands

The `manage-kafka.sh` script provides these commands:

```bash
# Create all topics
./scripts/manage-kafka.sh create

# List all topics
./scripts/manage-kafka.sh list

# Describe a topic
./scripts/manage-kafka.sh describe logs-user-service

# List consumer groups
./scripts/manage-kafka.sh list-groups

# Describe consumer group (check lag, offsets)
./scripts/manage-kafka.sh describe-group logging-service-group

# Produce test messages
./scripts/manage-kafka.sh produce logs-user-service

# Consume messages
./scripts/manage-kafka.sh consume logs-user-service
```

## 🔍 Monitoring

### Check Consumer Lag
```bash
./scripts/manage-kafka.sh describe-group logging-service-group
```

### View Logging Service Logs
```bash
docker logs iwa-logging-service -f
```

### Check Database
```bash
docker exec -it iwa-postgres-logs psql -U postgres -d iwa_logs -c "SELECT COUNT(*) FROM log_entries;"
```

### Actuator Endpoints
```bash
curl http://localhost:8084/actuator/health
curl http://localhost:8084/actuator/metrics
curl http://localhost:8084/actuator/prometheus
```

## 🔄 Next Steps

To integrate Kafka logging into your other microservices:

1. **Add Kafka dependency** to the service's pom.xml
2. **Create a KafkaProducer** service
3. **Send logs to Kafka** instead of/in addition to file logging
4. **Use correlation IDs** to track requests across services

Example producer code is available in the logging-service README.

## 🗂️ Architecture

```
┌─────────────────┐
│  Microservices  │
│  (api-gateway,  │
│   user-service, │
│   auth-service, │
│   etc.)         │
└────────┬────────┘
         │ Produces log messages
         ▼
┌─────────────────┐
│  Kafka Topics   │
│  - logs-auth    │
│  - logs-user    │
│  - logs-catalog │
│  - logs-gateway │
│  - logs-stripe  │
└────────┬────────┘
         │ Consumes
         ▼
┌─────────────────┐      ┌──────────────┐
│ Logging Service │─────▶│ PostgreSQL   │
│  (Port 8084)    │      │ (iwa_logs)   │
└─────────────────┘      └──────────────┘
         │
         │ REST API
         ▼
┌─────────────────┐
│  Query Logs     │
│  - By service   │
│  - By level     │
│  - By time      │
│  - Statistics   │
└─────────────────┘
```

## ⚙️ Configuration

### Environment Variables

The logging service uses these environment variables (set in docker-compose.yml):

- `KAFKA_BOOTSTRAP_SERVERS`: kafka:9092
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://postgres-logs:5432/iwa_logs
- `SERVER_PORT`: 8084

### Log Retention

- **Kafka**: 7 days (168 hours)
- **Database**: 30 days (automatic cleanup at 2 AM daily)

Both can be adjusted in configuration files.

## 🐛 Troubleshooting

### Kafka won't start
```bash
docker logs iwa-kafka
docker-compose restart kafka
```

### No logs appearing
```bash
# Check topics exist
./scripts/manage-kafka.sh list

# Check consumer group status
./scripts/manage-kafka.sh describe-group logging-service-group

# Check service logs
docker logs iwa-logging-service -f
```

### Database connection issues
```bash
docker logs iwa-postgres-logs
docker exec -it iwa-postgres-logs psql -U postgres -d iwa_logs
```

## 📚 Documentation

- **README.md** - Full service documentation
- **QUICK_START.md** - Detailed getting started guide with Kafka tutorial
- **scripts/manage-kafka.sh** - Run with `--help` for usage

## ✅ What's Working

- ✅ Kafka broker running in KRaft mode (no Zookeeper)
- ✅ Logging service built and ready to deploy
- ✅ PostgreSQL database for logs configured
- ✅ REST API for querying logs
- ✅ Kafka management script
- ✅ All Dockerfiles updated
- ✅ Parent POM updated

## 🎯 Ready to Use!

Your logging service is ready to deploy. Just run:

```bash
docker-compose up -d
./scripts/manage-kafka.sh create
```

Then start sending logs to Kafka topics and query them via the REST API!

---

**Need help?** Check the QUICK_START.md in the logging-service directory for detailed tutorials and examples.
