# Keycloak Service

This project is a microservice that integrates with Keycloak for authentication and authorization, utilizing a PostgreSQL database for data persistence. It provides a set of RESTful APIs to manage Keycloak realms, users, and clients.

## Project Structure

- **src/**: Contains the main application code.
  - **main/**: The main application source code.
    - **java/**: Java source files.
      - **iwaproject/keycloak_service/**: The main package for the Keycloak service.
        - **KeycloakServiceApplication.java**: The entry point of the application.
        - **config/**: Configuration classes for database and security settings.
        - **controller/**: REST controllers for handling API requests.
        - **dto/**: Data Transfer Objects for communication between client and server.
        - **exception/**: Custom exception handling classes.
        - **service/**: Service classes containing business logic.
    - **resources/**: Configuration files for the application.
- **keycloak-config/**: Contains Keycloak realm configurations and themes.
- **docker/**: Docker-related files for containerization.
- **scripts/**: Utility scripts for managing the Keycloak service.
- **.env.example**: Example environment variables for configuration.
- **.gitignore**: Specifies files to be ignored by Git.
- **pom.xml**: Maven configuration file for dependencies and build settings.
- **README.md**: Documentation for the project.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- Docker (optional, for containerization)

### Setup

1. Clone the repository:
   ```
   git clone <repository-url>
   cd keycloak-service
   ```

2. Build the project:
   ```
   mvn clean install
   ```

3. Configure the application:
   - Update the `application.yml` file with your PostgreSQL database connection details.

4. Run the application:
   ```
   mvn spring-boot:run
   ```

### Docker Setup

To run the service using Docker, follow these steps:

1. Build the Docker image:
   ```
   docker build -t keycloak-service:latest ./docker
   ```

2. Start the services using Docker Compose:
   ```
   docker-compose up -d
   ```

### Usage

- The service exposes various endpoints for managing Keycloak realms, users, and clients. Refer to the API documentation for detailed usage instructions.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.