-- =========================================================
-- 初始化模拟数据 (Mock Data) 
-- 密码统一使用 BCrypt 加密，明文为: 123456
-- 使用 INSERT IGNORE 防止重复插入报错
-- =========================================================

-- 1. 插入测试用户 (密码都是 123456)
INSERT IGNORE INTO heritage_users (id, username, password_hash, email, display_name, bio, contributor_status, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$1aGW5m83a7AYqMeqbR0nseBQX.z4pmMhXfeitdqXUMjtZ7OJsbHhe', 'admin@example.com', 'System Admin', 'I am the admin.', 'APPROVED', NOW(), NOW()),
(2, 'reviewer', '$2a$10$1aGW5m83a7AYqMeqbR0nseBQX.z4pmMhXfeitdqXUMjtZ7OJsbHhe', 'reviewer@example.com', 'Content Reviewer', 'I review submissions.', 'APPROVED', NOW(), NOW()),
(3, 'contributor', '$2a$10$1aGW5m83a7AYqMeqbR0nseBQX.z4pmMhXfeitdqXUMjtZ7OJsbHhe', 'contributor@example.com', 'Local Contributor', 'I share heritage stories.', 'APPROVED', NOW(), NOW());

-- 2. 分配角色
INSERT IGNORE INTO heritage_user_roles (user_id, role) VALUES 
(1, 'ADMIN'),
(2, 'ADMIN'),
(3, 'CONTRIBUTOR'),
(3, 'VIEWER');

-- 4. 插入分类 (Categories)
INSERT IGNORE INTO categories (id, name, description, created_at) VALUES
(1, 'Oral tradition', 'Oral traditions, expressions, and local proverbs', NOW()),
(2, 'Craft & objects', 'Traditional craftsmanship and handmade artifacts', NOW()),
(3, 'Historic sites', 'Buildings, streets, and geographical heritage', NOW());

-- 5. 插入标签 (Tags)
INSERT IGNORE INTO tags (id, name, created_at) VALUES 
(1, 'oral history', NOW()), 
(2, 'historic streets', NOW()), 
(3, 'indigo dyeing', NOW()),
(4, 'maritime', NOW()),
(5, 'proverbs', NOW());

-- 6. 插入资源 (Resources) - 包含不同状态供不同组员测试
INSERT IGNORE INTO resources (id, title, description, location_name, copyright_declaration, status, contributor_id, category_id, created_at, updated_at, submitted_at) VALUES
(1, 'Oral history excerpt: Pingjiang Road historic district', 'Local chronicles from the Han dynasty, detailing the water town lifestyle.', 'Suzhou, China', 'CC BY-NC-SA 4.0', 'APPROVED', 3, 3, NOW(), NOW(), NOW()),
(2, 'Traditional indigo resist-dyed cloth (lan yin hua bu)', 'Tongxiang blue calico uses plant indigo. This resource details the traditional dyeing patterns.', 'Tongxiang, Jiaxing', 'Educational use only', 'PENDING_REVIEW', 3, 2, NOW(), NOW(), NOW()),
(3, 'Coastal tide and weather proverbs', 'A community-compiled set of more than fifty proverbs about the tides and weather near the coast.', 'Haining, Zhejiang', 'Community share-alike', 'REJECTED', 3, 1, NOW(), NOW(), NOW()),
(4, 'Draft: Old city wall restoration', 'Initial notes on the old city wall structure before the 2020 restoration project.', 'Nanjing, Jiangsu', 'None', 'DRAFT', 3, 3, NOW(), NOW(), NULL);

-- 7. 绑定资源标签 (Resource Tags)
INSERT IGNORE INTO resource_tags (resource_id, tag_id) VALUES 
(1, 1), (1, 2), 
(2, 3), 
(3, 4), (3, 5);

-- 8. 插入审核日志 (Review Logs) - 供组员6/7测试查看历史记录
INSERT IGNORE INTO review_logs (id, resource_id, reviewer_id, action, feedback_comment, created_at) VALUES
(1, 1, 2, 'APPROVE', 'Looks great, the information is accurate. Approved for publishing.', NOW()),
(2, 3, 2, 'REJECT', 'Please provide more reliable sources and references for these proverbs before resubmitting.', NOW());
