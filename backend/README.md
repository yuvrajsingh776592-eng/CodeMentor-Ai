# CodeMentor AI API

The Spring Boot REST API is being built in phases. The initial backend foundation includes the application entry point, an API health route, and a centralized safe error response. Database access, authentication, and feature endpoints are added in later phases.

## Requirements

- JDK 21 or newer
- Apache Maven 3.9 or newer

## Run locally

From this directory:

```sh
mvn spring-boot:run
```

Then request `http://localhost:8080/api/health`. A healthy response includes `status`, `version`, and a UTC timestamp.

To package the application:

```sh
mvn package
java -jar target/codementor-api-0.1.0-SNAPSHOT.jar
```

Set `SERVER_PORT` or `APP_VERSION` in the environment to override the defaults.
