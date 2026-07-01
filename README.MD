# TaskAPI

A Spring Boot REST API for task management.

## Prerequisites

- Java 25, Gradle, Docker Desktop

## Setup

```bash
# 1. Copy and fill in environment file
cp .env.example .env.dev

# 2. Start the database
docker-compose --env-file .env.dev up -d

# 2.a. If database failed to due to port error, restart Windows NAT service
net stop winnat
net start winnat

# 3. Run the application
./gradlew bootRun
./gradlew bootRun --debug-jvm
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Helpful Commands

```bash
# Start environment
docker-compose --env-file .env.dev up -d

# Stop environment
docker-compose --env-file .env.dev down

# Fresh start (wipes database)
docker-compose --env-file .env.dev down -v

# View container logs
docker logs taskapi-mssql-dev

# Gradle commands
./gradlew clean compileJava
./gradlew clean compileTestJava
./gradlew clean test
./gradlew clean build
```

---