-- Initialize schema: users (with password_hash), and seed data some default users

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Seed users; use inserts with a NOT EXISTS guard so it works on MySQL and H2
INSERT INTO users (name, email, password_hash)
SELECT 'Alice', 'alice@cineschedule.com', '$2a$10$4CNGSid9eLMo11TATsWuIe5Mqs7UA9kIzKgqBAahSkpukT933vULu'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'alice@cineschedule.com');

INSERT INTO users (name, email, password_hash)
SELECT 'Bob', 'bob@cineschedule.com', '$2a$10$ckqv.9JTwjZFw6RkxX5tyuzA.7Mm6ROcj6BKrgL5uQWYB9H6dKLmy'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'bob@cineschedule.com');

INSERT INTO users (name, email, password_hash)
SELECT 'Charlie', 'charlie@cineschedule.com', '$2a$10$9nK3gXD/Y9rZjOsW6sRgE.iFl6EPiN9cue./zrEW.BcofJ6lL6EQC'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'charlie@cineschedule.com');
