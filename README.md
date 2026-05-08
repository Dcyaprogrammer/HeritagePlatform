# HeritagePlatform

A web-based community heritage resource sharing and curation platform.

## Tech Stack

### Frontend
- Vue 3
- Vue Router 4
- Vite 5
- Element Plus
- Axios

### Backend
- Java 17+
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- JWT (`jjwt`)
- Spring Mail
- Springdoc OpenAPI / Swagger UI

### Database and Tooling
- MySQL 8+
- Maven Wrapper
- Node.js 18+
- npm

## Project Structure

```text
HeritagePlatform/
├─ frontend/                     # Vue application
├─ platform/                     # Spring Boot application
│  └─ src/main/resources/
│     ├─ application.properties
│     ├─ schema.sql
│     └─ data.sql
├─ package.json                  # Root scripts
└─ README.md
```

## How to Run

### 1. Install dependencies

From the project root:

```bash
npm install
npm --prefix frontend install
```

### 2. Prepare MySQL

Make sure your local MySQL server is running.

Create the database if needed:

```sql
CREATE DATABASE IF NOT EXISTS heritage_platform
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3. Configure the backend

Backend configuration lives under `platform/src/main/resources/`.

Use one of these approaches:

- Edit `application.properties` directly for local development.
- Or copy `application-local.properties.example` to `application-local.properties` and put local overrides there.

At minimum, confirm your MySQL settings are correct:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/heritage_platform?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

On each backend startup, Spring Boot attempts to run `schema.sql` and `data.sql`.
These scripts are written to be mostly idempotent, so they can be re-applied during local development without recreating the database from scratch.

### 4. Start the backend

From the project root:

```bash
npm run dev:backend
```

This runs:

```bash
cd platform && ./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

### 5. Start the frontend

In a second terminal, from the project root:

```bash
npm run dev:frontend
```

Frontend URL:

```text
http://localhost:5173
```

## Test Accounts

The local seed data provides these three test users:

```text
admin / abc123
viewer / abc123
contributor / abc123
```

Emails:

```text
admin@example.com
viewer@example.com
contributor@example.com
```

## Useful Commands

Run these from the project root:

```bash
npm run dev:backend
npm run dev:frontend
npm run test:backend
npm run build:backend
npm run build:frontend
```

## Notes

- The backend uses the Maven Wrapper, so a separate Maven install is not required.
- The frontend is served by Vite in development.
- Swagger UI is available through the Spring Boot app when the backend is running.
