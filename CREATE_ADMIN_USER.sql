-- ================================================================
-- DealHarbor Admin User Creation Script for Supabase
-- ================================================================
-- 
-- INSTRUCTIONS:
-- 1. First, register the user normally via the application:
--    POST http://localhost:8080/api/auth/register
--    {
--      "name": "Nitin Admin",
--      "email": "nitinpandey1304@gmail.com",
--      "password": "thegreat1"
--    }
--
-- 2. Verify the email using the OTP sent (check console logs if email isn't working)
--
-- 3. Then run this SQL in Supabase SQL Editor to promote to ADMIN:
-- ================================================================

-- Option 1: If user already exists and is verified, just update role
UPDATE users 
SET role = 'ADMIN'
WHERE email = 'nitinpandey1304@gmail.com';

-- ================================================================
-- Option 2: Create admin user directly in database (if not registered yet)
-- ================================================================
-- NOTE: Password hash below is BCrypt hash of "thegreat1"
-- If you want a different password, register via API first, then use Option 1

INSERT INTO users (
    id,
    name,
    email,
    password_hash,
    role,
    enabled,
    locked,
    email_verified,
    is_verified_student,
    is_banned,
    seller_badge,
    seller_rating,
    buyer_rating,
    total_listings,
    active_listings,
    total_sales,
    total_purchases,
    total_revenue,
    response_rate,
    positive_reviews,
    negative_reviews,
    failed_login_attempts,
    two_factor_enabled,
    deleted,
    provider,
    profile_photo_url,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'Nitin Admin',
    'nitinpandey1304@gmail.com',
    '$2a$10$8X8X8X8X8X8X8X8X8X8X8uOiH.9Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5',  -- PLACEHOLDER: Replace with actual BCrypt hash
    'ADMIN',
    true,
    false,
    true,
    false,
    false,
    'NEW_SELLER',
    0.00,
    0.00,
    0,
    0,
    0,
    0,
    0.00,
    100.00,
    0,
    0,
    0,
    false,
    false,
    'LOCAL',
    '/api/images/default-avatar.png',
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE 
SET role = 'ADMIN',
    enabled = true,
    email_verified = true,
    updated_at = NOW();

-- ================================================================
-- Verify the admin user was created/updated
-- ================================================================
SELECT 
    id,
    name,
    email,
    role,
    enabled,
    email_verified,
    created_at
FROM users 
WHERE email = 'nitinpandey1304@gmail.com';

-- ================================================================
-- RECOMMENDED APPROACH:
-- ================================================================
-- 1. Register via API (ensures proper password hashing)
-- 2. Verify email via OTP
-- 3. Run Option 1 query above to promote to ADMIN
-- 
-- This is more secure than inserting with a placeholder hash!
-- ================================================================
