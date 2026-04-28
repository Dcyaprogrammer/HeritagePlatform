# System Context: Community Heritage Resource Platform
**Target Audience:** AI Coding Assistant / Agent
**Current User:** Team Leader (dopamine) & Developer for Module 4 (Resource Drafting Engine)

## 1. Project Overview
A web-based community heritage resource sharing and curation platform. Contributors submit resources, Admins moderate them, and Viewers can search and browse approved entries.

## 2. Tech Stack
* **Frontend:** Vue.js 3 + TypeScript
* **Backend:** Spring Boot (Java)
* **Database:** MySQL (Local instances, synchronized via shared `schema.sql`)
* **Architecture:** RESTful API, Frontend-Backend Separation.

## 3. Team Division & Module Boundaries (9 Members)
*The system is strictly divided into 9 vertical slices (Full-stack: DB + API + UI). The AI assistant must respect these boundaries and avoid generating code outside the current user's scope unless explicitly requested.*

### Part 1: Identity & Governance (Users & Roles)
* **Member 1 (Auth & Security Core):** Handles `/login`, `/register`, JWT generation/validation, Spring Security interceptors, and Password Hashing. *(AI Note: Assume all requests are authenticated via JWT token in the header. Do not generate auth logic for other modules).*
* **Member 2 (User & Role Admin):** Manages RBAC (Role-Based Access Control), User Profiles, and Admin approval of Contributor status.
* **Member 3 (Master Data Management):** Manages `categories` and `tags` tables (CRUD). Provides predefined tags/categories for the submission forms.

### Part 2: Resource Production & Workflow (Core Engine)
* **👉 Member 4 (Current User - Resource Drafting Engine):** Handles the core `resources` table creation and updates. Responsible for complex forms, metadata validation (Title, Location, Category), tagging (linking `resources` with `tags`), and saving entries as `DRAFT`.
* **✅ Member 5 (Media Assets Handling) [Implemented]:** Manages `attachments` table. Handles multipart file uploads (images/documents/video chunked upload), file metadata management, and binds URLs to specific `resource_id` (`AttachmentController`).
* **Member 6 (Review State Machine):** Manages the `status` field of `resources` (`DRAFT` -> `PENDING` -> `APPROVED`/`REJECTED`). Ensures concurrent state safety.
* **Member 7 (Feedback & Revision):** Manages `review_logs` table. Handles rejection comments and the logic for Contributors to resubmit rejected drafts.

### Part 3: Public Discovery & Interaction (C-End)
* **Member 8 (Search & Discovery):** Handles complex `SELECT` queries for the public homepage. Filters by categories/tags, pagination. *Constraint: Queries must strictly append `WHERE status = 'APPROVED'`.*
* **Member 9 (Details, Comments & Archiving):** Renders the final resource detail page, manages the `comments` table, and handles the Admin `ARCHIVE` action.

## 4. Core Database Schema Context
The AI must reference these tables when generating JPA Entities, MyBatis Mappers, or SQL queries for Module 4.

* `heritage_users`: `id`, `username`, `password_hash`, `email`, `display_name`, `avatar`, `bio`, `contributor_status`, `contributor_reason`, `failed_attempts`, `lock_time`, `reset_token`, `reset_token_expiry`, `created_at`, `updated_at`
* `heritage_user_roles`: `user_id`, `role`
* `categories`: `id`, `name`, `description`, `created_at`
* `tags`: `id`, `name`, `created_at`
* `resources` (Core Entity): `id`, `title`, `description`, `location_name`, `heritage_type_code`, `category` (VARCHAR), `copyright_declaration`, `status`, `contributor_id`/`submitter_id` (FK), `category_id` (FK), `submitted_at`, `version`, `rejection_reason`, `created_at`, `updated_at`.
* `resource_tags` (Join Table): `resource_id`, `tag_id`.
* `attachments`: `id`, `resource_id` (FK), `stored_name`, `display_name`, `file_path`, `file_type`, `file_size`, `created_at`.
* `review_logs`: `id`, `resource_id` (FK), `reviewer_id` (FK), `action`, `feedback_comment`, `created_at`.
* `comments`: `id`, `resource_id` (FK), `user_id` (FK), `content`, `created_at`, `updated_at`.

## 5. Development Directives
1.  **API First:** When requested to create a new feature, ALWAYS start by defining the RESTful API endpoints and JSON request/response payloads before writing implementation code.
2.  **Strict Scope:** The current user (dopamine) is responsible for **Module 4**. Focus on `resources` (Insert/Update) and `resource_tags` relations. Do not write file upload logic (Member 5) or approval state changes (Member 6) unless simulating mocks for integration.
3.  **Standardization:** Assume the project uses a global exception handler and a standard JSON wrapper (e.g., `{ "code": 200, "message": "success", "data": {...} }`) for all API responses.
