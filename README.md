# HeritagePlatform

Group Coursework for CPT202

## Tech Stack
- Java 21 (JDK 21+ required)
- Spring Boot 4.0.3
- Maven Wrapper (no Maven pre-install required, use `./mvnw` / `mvnw.cmd`)
- Vue 3 + Vite 5

## Project Structure
```
HeritagePlatform/
├─ platform/                # Backend (Spring Boot API)
│  ├─ src/main/java/...     # Business code
│  ├─ src/main/resources/   # Backend configuration
│  └─ pom.xml
├─ frontend/                # Frontend (Vue + Vite)
│  ├─ src/
│  ├─ public/
│  └─ package.json
├─ package.json             # Monorepo scripts
└─ README.md
```

## Development Environment
- JDK: 21 or later
- Maven: optional (recommended to use the Maven Wrapper included in this repo)
- Node.js: 18+ (LTS recommended)

## Quick Start
Start the backend first, then start the frontend.

1) Start backend (Spring Boot)
```bash
npm run dev:backend
```

2) Start frontend (Vue)
```bash
npm --prefix frontend install
npm run dev:frontend
```

Frontend URL:
```
http://localhost:5173
```

Backend API example:
```
http://localhost:8080/api/health
```

## Common Commands
- Start backend dev server: `npm run dev:backend`
- Start frontend dev server: `npm run dev:frontend`
- Run backend tests: `npm run test:backend`
- Build backend: `npm run build:backend`
- Build frontend: `npm run build:frontend`
