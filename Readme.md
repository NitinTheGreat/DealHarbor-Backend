
# DealHarbor Backend Application
 * 
 * An intra-university e-commerce platform built with Spring Boot 3.x and PostgreSQL (Supabase).
 * DealHarbor enables students within a university to buy and sell products exclusively among 
 * their academic community, fostering a secure and trusted marketplace environment.
 * 
 * Features:
    - Student-to-student product marketplace
    - University-specific user authentication and authorization
    - Product listing, browsing, and transaction management
 
 * Technology Stack:
    - Framework: Spring Boot 3.x
     - Database: PostgreSQL with Supabase
     -  Authentication: Spring Security
     - API: RESTful web services
  
  



```shellscript
# Test 1: Root endpoint
curl -X GET http://localhost:8080/

# Test 2: Health check
curl -X GET http://localhost:8080/health

# Test 3: API health check
curl -X GET http://localhost:8080/api/test/health
```

##  Step 2: User Registration Flow

```shellscript
# Test 4: Check if email exists (should return false)
curl -X POST http://localhost:8080/api/auth/check-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com"
  }'

# Test 5: Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New User",
    "email": "newuser@example.com",
    "password": "password123"
  }'

# Test 6: Verify OTP (REPLACE 123456 with actual OTP from console)
curl -X POST http://localhost:8080/api/auth/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "otp": "123456"
  }'

# Test 7: Resend OTP (if needed)
curl -X POST http://localhost:8080/api/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com"
  }'
```

## **📍 Step 3: Login and Token Management**

```shellscript
# Test 8: Login with test user (created automatically)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@dealharbor.com",
    "password": "password123"
  }'

# Test 9: Login with newly registered user (after OTP verification)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123"
  }'

# Test 10: Refresh token (REPLACE with actual refresh token from login response)
curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=YOUR_REFRESH_TOKEN_HERE"
```

## **📍 Step 4: Protected Endpoints (Need JWT Token)**

**⚠️ IMPORTANT: Replace `YOUR_ACCESS_TOKEN_HERE` with the actual JWT token from login response**

```shellscript
# Test 11: Get current user profile
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Test 12: Update profile
curl -X PUT http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "bio": "I am a computer science student",
    "phoneNumber": "+1234567890"
  }'

# Test 13: Get account statistics
curl -X GET http://localhost:8080/api/auth/account-stats \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Test 14: Get active sessions
curl -X GET http://localhost:8080/api/auth/sessions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Test 15: Get security events
curl -X GET http://localhost:8080/api/auth/security-events \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

## **📍 Step 5: Password Management**

```shellscript
# Test 16: Forgot password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@dealharbor.com"
  }'

# Test 17: Reset password (REPLACE 123456 with actual OTP from console)
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@dealharbor.com",
    "otp": "123456",
    "newPassword": "newpassword123"
  }'

# Test 18: Change password (requires authentication)
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newpassword456"
  }'
```

## **📍 Step 6: Email Management**

```shellscript
# Test 19: Change email (requires authentication)
curl -X POST http://localhost:8080/api/auth/change-email \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "newEmail": "newemail@example.com",
    "password": "password123"
  }'

# Test 20: Verify email change (REPLACE 123456 with actual OTP)
curl -X POST http://localhost:8080/api/auth/verify-email-change \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com",
    "otp": "123456"
  }'
```

## **📍 Step 7: Profile Photo Management**

```shellscript
# Test 21: Get default avatar
curl -X GET http://localhost:8080/api/images/default-avatar.png

# Test 22: Upload profile photo (replace path/to/image.jpg with actual image path)
curl -X POST http://localhost:8080/api/images/upload-profile-photo \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -F "file=@/path/to/image.jpg"

# Test 23: Update profile photo URL (REPLACE with URL from upload response)
curl -X PUT http://localhost:8080/api/auth/profile-photo \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '"/api/images/profile-photos/1640995200000_image.jpg"'
```

## **📍 Step 8: Session Management**

```shellscript
# Test 24: Terminate specific session (REPLACE with actual session ID)
curl -X DELETE http://localhost:8080/api/auth/sessions/SESSION_ID_HERE \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Test 25: Logout from current device (REPLACE with actual refresh token)
curl -X POST "http://localhost:8080/api/auth/logout?refreshToken=YOUR_REFRESH_TOKEN_HERE" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Test 26: Logout from all devices
curl -X POST http://localhost:8080/api/auth/logout-all \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

## **📍 Step 9: Account Deletion**

```shellscript
# Test 27: Delete account (CAREFUL - this will delete the account!)
curl -X DELETE http://localhost:8080/api/auth/account \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "password123",
    "reason": "Testing account deletion"
  }'
```

## **📍 Step 10: OAuth2 Testing**

```shellscript
# Test 28: Google OAuth2 (open in browser)
# Visit: http://localhost:8080/oauth2/authorization/google

# Test 29: GitHub OAuth2 (open in browser)  
# Visit: http://localhost:8080/oauth2/authorization/github
```

## **📍 Step 11: Protected Test Endpoints**

```shellscript
# Test 30: Public test endpoint
curl -X GET http://localhost:8080/api/test/public

# Test 31: Protected test endpoint
curl -X GET http://localhost:8080/api/test/protected \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# Test 32: Echo test
curl -X POST http://localhost:8080/api/test/echo \
  -H "Content-Type: application/json" \
  -d '"Hello DealHarbor!"'
```