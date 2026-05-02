CREATE DATABASE IF NOT EXISTS heritage_platform
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE heritage_platform;

CREATE TABLE IF NOT EXISTS heritage_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    email VARCHAR(100) NULL,
    avatar VARCHAR(500) NULL,
    bio TEXT NULL,
    contributor_status VARCHAR(20) DEFAULT 'NONE',
    contributor_reason TEXT NULL,
    failed_attempts INT DEFAULT 0,
    lock_time DATETIME NULL,
    reset_token VARCHAR(255) NULL,
    reset_token_expiry DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS heritage_user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES heritage_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT NULL,
    location_name VARCHAR(255) NULL,
    heritage_type_code VARCHAR(64) NULL,
    category VARCHAR(120) NULL,
    category_id INT NOT NULL,
    copyright_declaration VARCHAR(255) NULL,
    submitted_at DATETIME NULL,
    status ENUM('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED')
        NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    contributor_id BIGINT NOT NULL,
    submitter_id BIGINT NULL,
    reviewed_by_id BIGINT NULL,
    reviewed_at DATETIME NULL,
    rejection_reason VARCHAR(4000) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_resources_status_submitted_at (status, submitted_at),
    KEY idx_resources_category_id (category_id),
    KEY idx_resources_contributor_id (contributor_id),
    KEY idx_resources_submitter_id (submitter_id),
    KEY idx_resources_reviewed_by_id (reviewed_by_id),
    CONSTRAINT fk_resources_category
        FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_resources_contributor
        FOREIGN KEY (contributor_id) REFERENCES heritage_users(id),
    CONSTRAINT fk_resources_submitter
        FOREIGN KEY (submitter_id) REFERENCES heritage_users(id),
    CONSTRAINT fk_resources_reviewed_by
        FOREIGN KEY (reviewed_by_id) REFERENCES heritage_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS resource_tags (
    resource_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (resource_id, tag_id),
    CONSTRAINT fk_resource_tags_resource
        FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NULL,
    uploader_id BIGINT NULL,
    stored_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NULL,
    file_type VARCHAR(255) NULL,
    file_size BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_attachments_resource_id (resource_id),
    KEY idx_attachments_uploader_id (uploader_id),
    CONSTRAINT fk_attachments_resource
        FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_uploader
        FOREIGN KEY (uploader_id) REFERENCES heritage_users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    action ENUM('APPROVE', 'APPROVED', 'REJECT', 'REJECTED') NOT NULL,
    feedback_comment VARCHAR(4000) NULL,
    operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_review_logs_resource_operated_at (resource_id, operated_at),
    KEY idx_review_logs_reviewer_id (reviewer_id),
    CONSTRAINT fk_review_logs_resource
        FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_logs_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES heritage_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_comments_resource_id (resource_id),
    KEY idx_comments_user_id (user_id),
    CONSTRAINT fk_comments_resource
        FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES heritage_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    token_jti VARCHAR(255) NULL,
    ip_address VARCHAR(255) NULL,
    device_info VARCHAR(255) NULL,
    login_time DATETIME(6) NULL,
    UNIQUE KEY uk_user_sessions_token_jti (token_jti),
    KEY idx_user_sessions_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE resources
    MODIFY COLUMN title VARCHAR(500) NOT NULL,
    MODIFY COLUMN heritage_type_code VARCHAR(64) NULL,
    MODIFY COLUMN category VARCHAR(120) NULL,
    MODIFY COLUMN submitted_at DATETIME NULL,
    MODIFY COLUMN status ENUM('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED')
        NOT NULL DEFAULT 'DRAFT',
    MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0,
    MODIFY COLUMN reviewed_at DATETIME NULL,
    MODIFY COLUMN rejection_reason VARCHAR(4000) NULL;

ALTER TABLE attachments
    MODIFY COLUMN resource_id BIGINT NULL,
    MODIFY COLUMN uploader_id BIGINT NULL,
    MODIFY COLUMN file_path VARCHAR(255) NULL,
    MODIFY COLUMN file_type VARCHAR(255) NULL,
    MODIFY COLUMN file_size BIGINT NULL;

ALTER TABLE review_logs
    MODIFY COLUMN action ENUM('APPROVE', 'APPROVED', 'REJECT', 'REJECTED') NOT NULL,
    MODIFY COLUMN feedback_comment VARCHAR(4000) NULL,
    MODIFY COLUMN operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE comments
    MODIFY COLUMN content TEXT NOT NULL;

UPDATE resources r
LEFT JOIN categories c ON c.id = r.category_id
SET r.submitter_id = COALESCE(r.submitter_id, r.contributor_id),
    r.category = CASE
        WHEN c.name IS NOT NULL THEN c.name
        ELSE r.category
    END
WHERE r.submitter_id IS NULL
   OR r.category IS NULL
   OR r.category = ''
   OR r.category = CAST(r.category_id AS CHAR);
