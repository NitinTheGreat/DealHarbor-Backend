-- Add missing columns to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS bio VARCHAR(500),
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS provider VARCHAR(20) DEFAULT 'LOCAL',
ADD COLUMN IF NOT EXISTS google_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS github_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Update existing users to have default values
UPDATE users 
SET 
    profile_photo_url = '/api/images/default-avatar.png' 
WHERE profile_photo_url IS NULL;

UPDATE users 
SET 
    provider = 'LOCAL' 
WHERE provider IS NULL;

UPDATE users 
SET 
    deleted = FALSE 
WHERE deleted IS NULL;

-- Create missing tables if they don't exist
CREATE TABLE IF NOT EXISTS login_attempts (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    successful BOOLEAN NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    user_agent TEXT
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    refresh_token VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    device_info VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS security_events (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    description TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);
CREATE INDEX IF NOT EXISTS idx_login_attempts_email ON login_attempts(email);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_security_events_user_id ON security_events(user_id);
