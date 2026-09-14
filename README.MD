# TaskAPI

A Spring Boot REST API for task management.

## Prerequisites

- Java 25, Gradle, Docker Desktop

## Setup

```bash
# 1. Copy and fill in environment file
cp .env.dev.example .env.dev

# 2. Start the database
docker-compose --env-file .env.dev up -d

# 2.a. If database failed to due to port error, restart Windows NAT service
net stop winnat
net start winnat

# 3. Run the application
./gradlew bootRun
./gradlew bootRun --debug-jvm

# Generate a JWT signing keypair (see jshell recipe in project notes) and paste into
# JWT_PRIVATE_KEY / JWT_PUBLIC_KEY in .env.dev
openssl genpkey -algorithm Ed25519 -outform DER | openssl base64 -A

# Update database schema
./scripts/new-migration.ps1 -Service task -Description "add task priority index"                                                                                                                             


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
docker-compose --env-file .env.dev restart

# View container logs
docker logs taskapi-mssql-dev

# Gradle commands
./gradlew clean compileJava
./gradlew clean compileTestJava
./gradlew clean test
./gradlew clean build
```

---

## Health probes

Orchestrators (Kubernetes, ECS, etc.) must target the split probes, not the aggregate endpoint:

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

Do not point a liveness check at `/actuator/health` — it goes DOWN on any DB blip
(via Liquibase's/JPA's DataSource health indicator) and would trigger unnecessary
container restarts for a transient dependency issue rather than an actual app hang.

---

## Live dashboard (SSE)

Two-instance verification is manual — a Docker harness or dual embedded server
isn't worth building for this:

```bash
docker-compose up -d
./gradlew :service-task:bootRun
./gradlew :service-task:bootRun --args="--server.port=8082 --jobrunr.background-job-server.enabled=false --jobrunr.dashboard.enabled=false"
```

Open a stream (`GET /tasks/stream`) against `:8082`, write against `:8080`
(e.g. `PUT /tasks/{uuid}`), and confirm the event arrives on the `:8082`
stream. Disabling the JobRunr background server on the second instance avoids
two schedulers competing in dev.

**Known gap:** the nightly `SweepOverdueTasksTasklet` (01:00, up to
`MAX_SWEEP = 100_000` tasks) does not publish per-task events — broadcasting
one event per swept task would flood Redis and every connected client.
Dashboards do not reflect the overnight OVERDUE sweep until manually
refreshed. If this becomes a problem, the fix is a single `SWEEP_COMPLETED`
broadcast to all connected users, not per-task events.

**ALB dependency:** the ALB idle timeout must stay above the SSE heartbeat
interval (20s, see `SseHeartbeat`). Default ALB idle timeout is 60s, which is
fine as-is — don't lower it later without raising the heartbeat interval to
match.