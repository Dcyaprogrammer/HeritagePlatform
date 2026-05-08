-- Mock users
-- BCrypt hash below corresponds to the plain password: abc123
INSERT IGNORE INTO heritage_users (
    id, username, password_hash, display_name, email, bio,
    contributor_status, contributor_reason, failed_attempts,
    lock_time, reset_token, reset_token_expiry, created_at, updated_at
) VALUES
    (1, 'admin', '$2a$10$rUZ9uB0z5/KeXJFv76qNduG4mwS8frtmLat931jPA2uTPy2PBSDNi',
     'System Admin', 'admin@example.com', 'Platform administrator account.',
     'NONE', NULL, 0, NULL, NULL, NULL, '2026-04-20 09:00:00', '2026-04-20 09:00:00'),
    (2, 'viewer', '$2a$10$rUZ9uB0z5/KeXJFv76qNduG4mwS8frtmLat931jPA2uTPy2PBSDNi',
     'Community Viewer', 'viewer@example.com', 'Reads and comments on public resources.',
     'NONE', NULL, 0, NULL, NULL, NULL, '2026-04-20 09:05:00', '2026-04-20 09:05:00'),
    (3, 'contributor', '$2a$10$rUZ9uB0z5/KeXJFv76qNduG4mwS8frtmLat931jPA2uTPy2PBSDNi',
     'Local Contributor', 'contributor@example.com', 'Shares local heritage stories.',
     'APPROVED', 'Existing approved contributor.', 0, NULL, NULL, NULL, '2026-04-20 09:10:00', '2026-04-20 09:10:00');

INSERT IGNORE INTO heritage_user_roles (user_id, role) VALUES
    (1, 'ADMIN'),
    (2, 'VIEWER'),
    (3, 'CONTRIBUTOR'),
    (3, 'VIEWER');

-- Master data
INSERT IGNORE INTO categories (id, name, description, created_at) VALUES
    (1, 'Oral tradition', 'Oral traditions, expressions, and local proverbs.', '2026-04-20 10:00:00'),
    (2, 'Craft & objects', 'Traditional craftsmanship and material culture.', '2026-04-20 10:01:00'),
    (3, 'Historic sites', 'Buildings, streets, and place-based heritage.', '2026-04-20 10:02:00');

INSERT IGNORE INTO tags (id, name, created_at) VALUES
    (1, 'oral history', '2026-04-20 10:10:00'),
    (2, 'historic streets', '2026-04-20 10:11:00'),
    (3, 'indigo dyeing', '2026-04-20 10:12:00'),
    (4, 'maritime', '2026-04-20 10:13:00'),
    (5, 'proverbs', '2026-04-20 10:14:00'),
    (6, 'garden archive', '2026-04-20 10:15:00');

-- Resources across draft / pending / approved / rejected states
INSERT IGNORE INTO resources (
    id, title, description, location_name, heritage_type_code, category, category_id,
    copyright_declaration, submitted_at, status, version,
    contributor_id, submitter_id, reviewed_by_id, reviewed_at, rejection_reason,
    created_at, updated_at
) VALUES
    (1, 'Oral history excerpt: Pingjiang Road historic district',
     'Local chronicles about the water-town lifestyle around Pingjiang Road.',
     'Suzhou, Jiangsu', 'CULT_BOOKS_DOCUMENTS', 'Historic sites', 3,
     'CC BY-NC-SA 4.0', '2026-04-28 19:28:00', 'APPROVED', 0,
     3, 3, 1, '2026-04-29 09:30:00', NULL,
     '2026-04-28 19:00:00', '2026-04-29 09:30:00'),
    (2, 'Traditional indigo resist-dyed cloth (lan yin hua bu)',
     'An overview of Tongxiang blue calico patterns, materials, and workshop practice.',
     'Tongxiang, Zhejiang', 'LIFE_TEXTILE', 'Craft & objects', 2,
     'Educational use only', '2026-05-01 11:00:00', 'PENDING_REVIEW', 0,
     3, 3, NULL, NULL, NULL,
     '2026-05-01 10:15:00', '2026-05-01 11:00:00'),
    (3, 'Coastal tide and weather proverbs',
     'A community-compiled set of sayings about tides and weather near the coast.',
     'Haining, Zhejiang', 'CULT_BOOKS_DOCUMENTS', 'Oral tradition', 1,
     'Community share-alike', '2026-04-30 08:15:00', 'REJECTED', 1,
     3, 3, 1, '2026-04-30 17:20:00',
     'Please provide primary-source references before resubmitting.',
     '2026-04-29 18:00:00', '2026-04-30 17:20:00'),
    (4, 'Draft: Old city wall restoration notes',
     'Initial field notes on the old city wall structure before restoration.',
     'Nanjing, Jiangsu', 'RIT_ARCHITECTURE', 'Historic sites', 3,
     'None', NULL, 'DRAFT', 0,
     3, 3, NULL, NULL, NULL,
     '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (5, 'Humble Administrator''s Garden maintenance ledger',
     'A translated digest of maintenance records for the classical Suzhou garden.',
     'Suzhou, Jiangsu', 'CULT_BOOKS_DOCUMENTS', 'Historic sites', 3,
     'Public domain source digest', '2026-04-27 14:00:00', 'APPROVED', 2,
     3, 3, 1, '2026-04-28 09:45:00', NULL,
     '2026-04-27 12:30:00', '2026-04-28 09:45:00');

INSERT IGNORE INTO resource_tags (resource_id, tag_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (3, 4),
    (3, 5),
    (5, 2),
    (5, 6);

INSERT IGNORE INTO review_logs (
    id, resource_id, reviewer_id, action, feedback_comment, operated_at
) VALUES
    (1, 1, 1, 'APPROVED',
     'Looks accurate and ready for publication.',
     '2026-04-29 09:30:00'),
    (2, 3, 1, 'REJECTED',
     'Please provide primary-source references before resubmitting.',
     '2026-04-30 17:20:00'),
    (3, 5, 1, 'APPROVED',
     'Useful archival context and well-structured summary.',
     '2026-04-28 09:45:00');

INSERT IGNORE INTO comments (
    id, resource_id, user_id, content, created_at, updated_at
) VALUES
    (1, 1, 2,
     'The lane descriptions are vivid and make the place easy to picture.',
     '2026-04-29 12:00:00', '2026-04-29 12:00:00'),
    (2, 5, 3,
     'This garden ledger is a good companion resource for architecture students.',
     '2026-04-29 13:30:00', '2026-04-29 13:30:00');
