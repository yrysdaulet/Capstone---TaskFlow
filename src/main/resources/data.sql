-- Demo data for Taskflow
-- Password for both users is "password" (BCrypt hash)
-- INSERT INTO users (id, username, email, password, created_at) VALUES
--   (21, 'demo', 'demo@example.com', '$2a$10$UA7DEX0wjjZZO2Tn1ZkmTOweDY4swSFlKGFW6D21XuqRMdHY4/3Ga', NOW()),
--   (22, 'viewer', 'viewer@example.com', '$2a$10$UA7DEX0wjjZZO2Tn1ZkmTOweDY4swSFlKGFW6D21XuqRMdHY4/3Ga', NOW());

INSERT INTO boards (id, title, created_at, owner_id)
VALUES (100, 'Demo Board', NOW(), 21)
ON CONFLICT (id) DO NOTHING;

INSERT INTO lists (id, title, position, board_id)
VALUES
    (1000, 'To Do', 0, 100),
    (1001, 'In Progress', 1, 100),
    (1002, 'Done', 2, 100)
ON CONFLICT (id) DO NOTHING;

INSERT INTO cards (id, title, description, due_date, position, list_id)
VALUES
    (2000, 'Set up project', 'Initialize repository and modules', NULL, 0, 1000),
    (2001, 'Implement backend', 'Spring Boot services and controllers', NULL, 1, 1000),
    (2002, 'Design UI', 'Basic Kanban board', NULL, 0, 1001),
    (2003, 'Polish styles', 'Nice looking columns and cards', NULL, 0, 1002)
ON CONFLICT (id) DO NOTHING;

INSERT INTO board_users (user_id, board_id, role)
VALUES (22, 100, 'VIEWER')
ON CONFLICT (user_id, board_id) DO NOTHING;
