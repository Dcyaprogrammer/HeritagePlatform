# Heritage Platform - Backend

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    %% 1. User & Permission Module
    HERITAGE_USERS {
        bigint id PK "Primary Key"
        varchar username "Username"
        varchar password_hash "Encrypted Password"
        varchar email "Email"
        varchar display_name "Display Name" 
        varchar avatar "Avatar Image URL"
        text bio "User Profile / Bio"
        varchar contributor_status "Status: NONE, APPROVED, REJECTED"
        text contributor_reason "Application Reason"
        int failed_attempts "Login failed attempts"
        datetime lock_time "Account lock time"
        varchar reset_token "Password reset token"
        datetime reset_token_expiry "Token expiry time"
        datetime created_at "Creation Time"
        datetime updated_at "Update Time"
    }
    
    HERITAGE_USER_ROLES {
        bigint user_id PK, FK "User ID"
        varchar role PK "Role: ADMIN, CONTRIBUTOR, VIEWER"
    }

    %% 2. Core Resource Module
    RESOURCES {
        bigint id PK "Primary Key"
        varchar title "Title"
        text description "Heritage Description"
        varchar location_name "Location / Place"
        varchar copyright_declaration "Copyright / Usage Declaration"
        varchar status "Status: DRAFT, PENDING_REVIEW, APPROVED, REJECTED, ARCHIVED"
        bigint contributor_id FK "Contributor ID (Linked to HeritageUsers)"
        int category_id FK "Category ID (Linked to Categories)"
        varchar heritage_type_code "Leaf type code, e.g. RIT_BRONZE"
        datetime created_at "Creation Time"
        datetime updated_at "Update Time"
    }
    
    CATEGORIES {
        int id PK "Primary Key"
        varchar name "Category Name"
        varchar description "Category Description"
        datetime created_at "Creation Time"
    }
    
    TAGS {
        bigint id PK "Primary Key"
        varchar name "Tag Name"
        datetime created_at "Creation Time"
    }
    
    RESOURCE_TAGS {
        bigint resource_id PK, FK "Resource ID"
        bigint tag_id PK, FK "Tag ID"
    }
    
    ATTACHMENTS {
        bigint id PK "Primary Key"
        bigint resource_id FK "Resource ID"
        varchar file_path "File Path or External URL"
        varchar file_type "Type: IMAGE, DOCUMENT, VIDEO, AUDIO, LINK"
        datetime created_at "Upload Time"
    }

    %% 3. Review & Interaction Module
    REVIEW_LOGS {
        bigint id PK "Primary Key"
        bigint resource_id FK "Reviewed Resource ID"
        bigint reviewer_id FK "Reviewer ID (Linked to HeritageUsers)"
        varchar action "Action: APPROVE, REJECT"
        text feedback_comment "Rejection/Feedback Comment"
        datetime created_at "Review Time"
    }
    
    COMMENTS {
        bigint id PK "Primary Key"
        bigint resource_id FK "Resource ID"
        bigint user_id FK "Commenter ID"
        text content "Comment Content"
        datetime created_at "Comment Time"
        datetime updated_at "Update Time"
    }

    %% Relationships
    HERITAGE_USERS ||--o{ HERITAGE_USER_ROLES : "assigned to"
    HERITAGE_USERS ||--o{ RESOURCES : "submits (Contributor)"
    CATEGORIES ||--o{ RESOURCES : "belongs to"
    RESOURCES ||--o{ RESOURCE_TAGS : "has tags"
    TAGS ||--o{ RESOURCE_TAGS : "associated with"
    RESOURCES ||--o{ ATTACHMENTS : "contains (Media)"
    RESOURCES ||--o{ REVIEW_LOGS : "undergoes review"
    HERITAGE_USERS ||--o{ REVIEW_LOGS : "executes review (Reviewer)"
    RESOURCES ||--o{ COMMENTS : "receives comments"
    HERITAGE_USERS ||--o{ COMMENTS : "posts comments"
```

### Backend Setup

1. **Navigate to the backend directory:**
   ```bash
   cd platform
   ```

2. **Configure your local environment:**
   Copy the example configuration file to create your own local `application.properties`:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
   *Note: Open `application.properties` and update `spring.datasource.password` if your local MySQL has a password.*

3. **Start the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
