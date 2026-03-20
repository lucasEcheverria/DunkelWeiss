# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DunkelWeiss is a full-stack Java web application using a Gradle multi-module structure:
- **`server/`** — Spring Boot REST API backend (port 8080), Spring Data JDBC + MySQL
- **`client/`** — Spring Boot frontend (port 8081), Thymeleaf templates
- **`lib/`** — Shared library (DTOs, validation, Jackson annotations) used by both modules

Build tool: Gradle with Kotlin DSL (`build.gradle.kts`). Java 21.

## Essential Commands

```bash
# Build
./gradlew build                  # All modules
./gradlew :server:build          # Server only
./gradlew clean build            # Full clean rebuild

# Run
./gradlew :server:bootRun        # Backend API on :8080
./gradlew :client:bootRun        # Frontend on :8081

# Test
./gradlew test                   # All modules
./gradlew :server:test           # Server tests only
./gradlew :server:test --tests "com.example.MyTest.myMethod"  # Single test

# Database (Docker)
cd server && docker compose up -d    # Start MySQL
cd server && docker compose down     # Stop MySQL
```

## Development Setup

Each developer must create `server/.env` with database credentials:
```
DB_ROOT_PASSWORD=root
DB_USER=admin
DB_PASSWORD=admin
DB_NAME=dunkelweiss_db
```

The server's `application.properties` references `server/compose.yaml` for Docker Compose integration. Spring Boot auto-manages the compose lifecycle during `bootRun`.

Database schema is initialized from `server/db/schema.sql` (mounted at `/docker-entrypoint-initdb.d` in the container).

## Architecture

- **lib** is a `java-library` and must not depend on server or client — it's a shared utility layer.
- **server** depends on `lib` and exposes REST endpoints; uses Spring Data JDBC (not JPA) for data access.
- **client** depends on `lib` and communicates with the server API; renders views via Thymeleaf.
- Both `server` and `client` are independent Spring Boot applications (separate JVMs).

Port assignments are fixed: server=8080, client=8081 (defined in each module's `application.properties`).
