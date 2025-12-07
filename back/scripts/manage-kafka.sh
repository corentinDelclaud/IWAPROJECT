#!/bin/bash
# Kafka Management Script for IWA Project
# This script helps manage Kafka topics for the logging service

KAFKA_CONTAINER="iwa-kafka"
BOOTSTRAP_SERVER="localhost:9092"
KAFKA_BIN="/opt/kafka/bin"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if Kafka is running
check_kafka() {
    if ! docker ps | grep -q $KAFKA_CONTAINER; then
        echo -e "${RED}Error: Kafka container is not running${NC}"
        echo "Please start Kafka with: docker-compose up -d kafka"
        exit 1
    fi
}

# Function to create all required topics
create_topics() {
    echo -e "${YELLOW}Creating Kafka topics for logging...${NC}"
    
    topics=(
        "logs-auth-service"
        "logs-user-service"
        "logs-catalog-service"
        "logs-api-gateway"
        "logs-stripe-service"
    )
    
    for topic in "${topics[@]}"; do
        echo -e "${GREEN}Creating topic: $topic${NC}"
        docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-topics.sh \
            --bootstrap-server $BOOTSTRAP_SERVER \
            --create \
            --topic $topic \
            --partitions 3 \
            --replication-factor 1 \
            --if-not-exists
    done
    
    echo -e "${GREEN}All topics created successfully!${NC}"
}

# Function to list all topics
list_topics() {
    echo -e "${YELLOW}Listing all Kafka topics...${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-topics.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --list
}

# Function to describe a topic
describe_topic() {
    if [ -z "$1" ]; then
        echo -e "${RED}Error: Please provide a topic name${NC}"
        echo "Usage: $0 describe <topic-name>"
        exit 1
    fi
    
    echo -e "${YELLOW}Describing topic: $1${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-topics.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --describe \
        --topic $1
}

# Function to delete a topic
delete_topic() {
    if [ -z "$1" ]; then
        echo -e "${RED}Error: Please provide a topic name${NC}"
        echo "Usage: $0 delete <topic-name>"
        exit 1
    fi
    
    echo -e "${YELLOW}Deleting topic: $1${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-topics.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --delete \
        --topic $1
}

# Function to list consumer groups
list_groups() {
    echo -e "${YELLOW}Listing consumer groups...${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-consumer-groups.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --list
}

# Function to describe a consumer group
describe_group() {
    if [ -z "$1" ]; then
        echo -e "${RED}Error: Please provide a group name${NC}"
        echo "Usage: $0 describe-group <group-name>"
        exit 1
    fi
    
    echo -e "${YELLOW}Describing consumer group: $1${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-consumer-groups.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --describe \
        --group $1
}

# Function to produce test messages
produce_test() {
    if [ -z "$1" ]; then
        echo -e "${RED}Error: Please provide a topic name${NC}"
        echo "Usage: $0 produce <topic-name>"
        exit 1
    fi
    
    echo -e "${YELLOW}Starting producer for topic: $1${NC}"
    echo -e "${GREEN}Enter messages (Ctrl+C to exit):${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-console-producer.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --topic $1
}

# Function to consume messages
consume_test() {
    if [ -z "$1" ]; then
        echo -e "${RED}Error: Please provide a topic name${NC}"
        echo "Usage: $0 consume <topic-name>"
        exit 1
    fi
    
    echo -e "${YELLOW}Starting consumer for topic: $1${NC}"
    echo -e "${GREEN}Consuming from beginning (Ctrl+C to exit):${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN/kafka-console-consumer.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --topic $1 \
        --from-beginning
}

# Function to show help
show_help() {
    echo "Kafka Management Script for IWA Project"
    echo ""
    echo "Usage: $0 <command> [arguments]"
    echo ""
    echo "Commands:"
    echo "  create                  - Create all required topics"
    echo "  list                    - List all topics"
    echo "  describe <topic>        - Describe a specific topic"
    echo "  delete <topic>          - Delete a specific topic"
    echo "  list-groups             - List all consumer groups"
    echo "  describe-group <group>  - Describe a specific consumer group"
    echo "  produce <topic>         - Start a producer for a topic"
    echo "  consume <topic>         - Start a consumer for a topic"
    echo "  help                    - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 create"
    echo "  $0 list"
    echo "  $0 describe logs-user-service"
    echo "  $0 produce logs-user-service"
    echo "  $0 consume logs-user-service"
}

# Main script logic
check_kafka

case "$1" in
    create)
        create_topics
        ;;
    list)
        list_topics
        ;;
    describe)
        describe_topic "$2"
        ;;
    delete)
        delete_topic "$2"
        ;;
    list-groups)
        list_groups
        ;;
    describe-group)
        describe_group "$2"
        ;;
    produce)
        produce_test "$2"
        ;;
    consume)
        consume_test "$2"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo -e "${RED}Error: Unknown command '$1'${NC}"
        echo ""
        show_help
        exit 1
        ;;
esac
