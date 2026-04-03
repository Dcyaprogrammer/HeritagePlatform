# HeritagePlatform

Group Coursework for CPT202: A web-based community heritage resource sharing and curation platform.

## Tech Stack
- **Java**: 21 (JDK 21+ required)
- **Backend**: Spring Boot 4.0.3, Spring Data JPA
- **Database**: MySQL 8.x
- **Frontend**: Vue 3 + Vite 5
- **Build Tool**: Maven Wrapper (no Maven pre-install required)

## Project Structure
```
HeritagePlatform/
├─ platform/                # Backend (Spring Boot API)
│  ├─ src/main/java/...     # Business logic & Controllers
│  ├─ src/main/resources/   # Application config & schema.sql
│  └─ pom.xml
├─ frontend/                # Frontend (Vue 3 + Vite)
│  ├─ src/                  # Vue components and assets
│  └─ package.json
├─ package.json             # Monorepo scripts
└─ README.md
```

## Prerequisites & Development Environment
1. **JDK 21** or later installed.
2. **Node.js 18+** (LTS recommended) installed.
3. **MySQL 8.x** installed and running locally.
4. An IDE like **IntelliJ IDEA** (recommended for backend) or **VS Code**.

---

## 🚀 Quick Start Guide for Team Members

Follow these steps exactly to set up your local development environment.

### Step 1: Database Setup (One-time only)
1. Ensure your local MySQL server is running.
2. Log into your MySQL server:
   ```bash
   mysql -u root -p
   ```
   *(If your root user has no password, just omit the `-p`)*
3. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS heritage_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   exit;
   ```

### Step 2: Backend Configuration (One-time only)
Because database credentials contain sensitive information, `application.properties` is ignored by Git. You must create your own local copy:

1. Navigate to the backend resources folder: `platform/src/main/resources/`
2. Modify `application.properties` and fill in your local MySQL username and password:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=YOUR_LOCAL_MYSQL_PASSWORD
   ```
   *(If your password is empty, leave it blank: `spring.datasource.password=`)*

> **Note:** Spring Boot will automatically run `schema.sql` on startup to create all tables for you. Do not execute it manually!

### Step 3: Frontend Setup (One-time only)
Install the necessary Node.js dependencies for the Vue frontend.
Open a terminal in the root directory and run:
```bash
npm --prefix frontend install
```
*(Alternatively, you can `cd frontend` and run `npm install`)*

### Step 4: Run the Application

You can start both the backend and frontend using the convenient npm scripts in the root directory.

**1) Start backend (Spring Boot)**
Open a terminal in the root directory and run:
```bash
npm run dev:backend
```
*(Under the hood, this runs `cd platform && ./mvnw spring-boot:run`)*
*(The backend will start on `http://localhost:8080`)*

**2) Start frontend (Vue)**
Open a second terminal in the root directory and run:
```bash
npm run dev:frontend
```
*(The frontend will start on `http://localhost:5173`)*

---

## 🔗 Connection Verification
To verify that everything is working:
1. Open your browser and go to: `http://localhost:5173`
2. If you see the text **"后端状态：ok"**, congratulations! The frontend has successfully connected to the Spring Boot backend, and the backend has successfully connected to MySQL.

## 🛠 Common Commands
Run these from the root `HeritagePlatform/` directory:
- Start backend dev server: `npm run dev:backend`
- Start frontend dev server: `npm run dev:frontend`
- Run backend tests: `npm run test:backend`
- Build backend: `npm run build:backend`
- Build frontend: `npm run build:frontend`
