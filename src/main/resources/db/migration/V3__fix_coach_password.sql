-- Fix coach password: Update plain text to BCrypt encoded
-- Password: coach123
-- BCrypt hash generated with cost factor 10

UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqRQNJQVYX3LMz7FLmEGxjNxqzHCi'
WHERE email = 'seed.coach@academy.com'
  AND password = 'coach123';
