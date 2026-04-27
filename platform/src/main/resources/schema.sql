-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS heritage_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE heritage_platform;

-- 1. Identity & Governance (Users & Roles)
CREATE TABLE IF NOT EXISTS heritage_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    email VARCHAR(100),
    display_name VARCHAR(120) NOT NULL,
    avatar VARCHAR(500),
    bio TEXT,
    contributor_status VARCHAR(20) DEFAULT 'NONE',
    contributor_reason TEXT,
    failed_attempts INT DEFAULT 0,
    lock_time DATETIME,
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS heritage_user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES heritage_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Master Data (Categories & Tags)
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Core Resource Module
CREATE TABLE IF NOT EXISTS resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location_name VARCHAR(255),
    copyright_declaration VARCHAR(255),
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT, PENDING_REVIEW, APPROVED, REJECTED, ARCHIVED',
    contributor_id BIGINT NOT NULL,
    category_id INT NOT NULL,
--新加的
    heritage_type_code VARCHAR(64) NULL COMMENT 'Leaf type code, e.g. RIT_BRONZE',
--
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contributor_id) REFERENCES heritage_users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ensure columns required by review module exist on existing databases.
-- These are no-op on fresh databases that already include them.
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'category'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN category VARCHAR(120) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'submitted_at'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN submitted_at DATETIME NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'version'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'submitter_id'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN submitter_id BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'reviewed_by_id'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN reviewed_by_id BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'reviewed_at'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN reviewed_at DATETIME NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'rejection_reason'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN rejection_reason VARCHAR(4000) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Backfill compatibility values where possible
UPDATE resources
SET submitter_id = contributor_id
WHERE submitter_id IS NULL;

UPDATE resources
SET submitted_at = created_at
WHERE submitted_at IS NULL;

UPDATE resources
SET category = CAST(category_id AS CHAR)
WHERE category IS NULL;

--新加的
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'resources'
      AND COLUMN_NAME = 'heritage_type_code'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE resources ADD COLUMN heritage_type_code VARCHAR(64) NULL COMMENT ''Leaf type code, e.g. RIT_BRONZE''',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
--




CREATE TABLE IF NOT EXISTS resource_tags (
    resource_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (resource_id, tag_id),
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT COMMENT 'references resources(id); FK + NOT NULL will be enabled after integration with team member 4',
    stored_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL COMMENT 'image, pdf, word, video, audio, document',
    file_size BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Review & Interaction Module
CREATE TABLE IF NOT EXISTS review_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL COMMENT 'APPROVE, REJECT',
    feedback_comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES heritage_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES heritage_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
