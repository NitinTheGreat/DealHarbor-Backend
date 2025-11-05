.\mvnw spring-boot:run
docker run -d --name redis-local -p 6379:6379 redis:latest
http://localhost:8080/admin.html
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


# POST AUTH Routes

```plaintext
## 🎓 Student Verification Endpoints

### 1. Send Student Email OTP
```http
POST /api/student-verification/send-otp
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
"studentEmail": "[john.doe@vitstudent.ac.in](mailto:john.doe@vitstudent.ac.in)"
}

```plaintext

**Response:**
\`\`\`json
"OTP sent to your student email. Please check your inbox."
```

**Example:**
```bash
curl -X POST [http://localhost:8080/api/student-verification/send-otp](http://localhost:8080/api/student-verification/send-otp) -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d '{
"studentEmail": "[john.doe@vitstudent.ac.in](mailto:john.doe@vitstudent.ac.in)"
}'

```plaintext

### 2. Verify Student Email OTP
```http
POST /api/student-verification/verify-otp
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body (Minimal):**
```json
{
"studentEmail": "[john.doe@vitstudent.ac.in](mailto:john.doe@vitstudent.ac.in)",
"otp": "123456"
}

```plaintext

**Request Body (Complete - All Optional):**
\`\`\`json
{
  "studentEmail": "john.doe@vitstudent.ac.in",
  "otp": "123456",
  "universityId": "VIT2021001",
  "graduationYear": 2025,
  "department": "Computer Science"
}
```

**Response:**
```json
"Student verification successful! You are now a verified VIT student."

```plaintext

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/student-verification/verify-otp \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentEmail": "john.doe@vitstudent.ac.in",
    "otp": "123456",
    "universityId": "VIT2021001",
    "graduationYear": 2025,
    "department": "Computer Science"
  }'
```

---

## 📦 Product Endpoints

### 1. Get All Products (Public)

```plaintext
GET /api/products?page=0&size=20&sortBy=date_desc
```

**Query Parameters:**

- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sortBy` (optional): Sort order (default: date_desc)

- `price_asc`, `price_desc`, `date_asc`, `date_desc`, `popularity`





**Response:**
```json
{
"content": [
{
"id": "product-id-1",
"title": "MacBook Pro 2021",
"description": "Excellent condition MacBook Pro",
"price": 85000.00,
"originalPrice": 120000.00,
"isNegotiable": true,
"condition": "LIKE_NEW",
"brand": "Apple",
"model": "MacBook Pro 14-inch",
"status": "APPROVED",
"pickupLocation": "VIT Campus",
"deliveryAvailable": false,
"viewCount": 45,
"favoriteCount": 12,
"isFeatured": false,
"tags": ["laptop", "apple", "macbook"],
"createdAt": "2024-01-15T10:00:00Z",
"updatedAt": "2024-01-15T10:00:00Z",
"sellerId": "seller-id-1",
"sellerName": "John Seller",
"sellerBadge": "TRUSTED_SELLER",
"sellerRating": 4.5,
"sellerIsVerifiedStudent": true,
"categoryId": "cat-2-1",
"categoryName": "Laptops & Computers",
"images": [
{
"id": "img-1",
"imageUrl": "/api/images/products/macbook1.jpg",
"altText": "MacBook Pro front view",
"isPrimary": true,
"sortOrder": 0
}
],
"primaryImageUrl": "/api/images/products/macbook1.jpg"
}
],
"page": 0,
"size": 20,
"totalElements": 150,
"totalPages": 8,
"first": true,
"last": false,
"hasNext": true,
"hasPrevious": false
}

```plaintext

**Example:**
\`\`\`bash
curl "http://localhost:8080/api/products?page=0&size=10&sortBy=price_asc"
```

### 2. Get Products by Category

```plaintext
GET /api/products/category/{categoryId}?page=0&size=20&sortBy=date_desc
```

**Example:**
```bash
curl "[http://localhost:8080/api/products/category/cat-2-1?page=0&size=10](http://localhost:8080/api/products/category/cat-2-1?page=0&size=10)"

```plaintext

### 3. Search Products
```http
POST /api/products/search
Content-Type: application/json
```

**Request Body:**
```json
{
"keyword": "macbook",
"categoryId": "cat-2-1",
"minPrice": 50000.00,
"maxPrice": 100000.00,
"conditions": ["LIKE_NEW", "GOOD"],
"location": "VIT Campus",
"deliveryAvailable": false,
"isNegotiable": true,
"verifiedStudentsOnly": true,
"sortBy": "price_asc",
"page": 0,
"size": 20
}

```plaintext

**Response:** Same format as Get All Products

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "laptop",
    "minPrice": 30000,
    "maxPrice": 80000,
    "verifiedStudentsOnly": true,
    "page": 0,
    "size": 10
  }'
```

### 4. Get Featured Products

```plaintext
GET /api/products/featured?page=0&size=20
```

**Example:**
```bash
curl "[http://localhost:8080/api/products/featured?page=0&size=5](http://localhost:8080/api/products/featured?page=0&size=5)"

```plaintext

### 5. Get Product by ID
```http
GET /api/products/{productId}
```

**Response:** Single product object (same format as in list)

**Example:**
```bash
curl [http://localhost:8080/api/products/product-id-123](http://localhost:8080/api/products/product-id-123)

```plaintext

### 6. Create Product (Protected)
```http
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
"title": "iPhone 13 Pro",
"description": "Excellent condition iPhone 13 Pro with all accessories",
"price": 65000.00,
"originalPrice": 79900.00,
"isNegotiable": true,
"condition": "LIKE_NEW",
"brand": "Apple",
"model": "iPhone 13 Pro",
"categoryId": "cat-2-2",
"tags": ["iphone", "apple", "smartphone"],
"pickupLocation": "VIT Main Campus",
"deliveryAvailable": false,
"imageUrls": [
"/api/images/products/iphone1.jpg",
"/api/images/products/iphone2.jpg"
]
}

```plaintext

**Response:** Created product object

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 75000.00,
    "originalPrice": 90000.00,
    "isNegotiable": true,
    "condition": "GOOD",
    "brand": "ASUS",
    "model": "ROG Strix",
    "categoryId": "cat-2-1",
    "tags": ["gaming", "laptop", "asus"],
    "pickupLocation": "VIT Campus",
    "deliveryAvailable": false,
    "imageUrls": []
  }'
```

### 7. Get User's Products (Protected)

```plaintext
GET /api/products/my-products?page=0&size=20
Authorization: Bearer {token}
```

**Response:** Same format as Get All Products (user's products only)

**Example:**
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" "[http://localhost:8080/api/products/my-products?page=0&size=10](http://localhost:8080/api/products/my-products?page=0&size=10)"

```plaintext

### 8. Update Product (Protected)
```http
PUT /api/products/{productId}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:** Same as Create Product (all fields optional)
```json
{
"title": "Updated iPhone 13 Pro",
"price": 60000.00,
"isNegotiable": false
}

```plaintext

**Response:** Updated product object

### 9. Delete Product (Protected)
```http
DELETE /api/products/{productId}
Authorization: Bearer {token}
```

**Response:**
```json
"Product deleted successfully"

```plaintext

**Example:**
\`\`\`bash
curl -X DELETE http://localhost:8080/api/products/product-id-123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📂 Category Endpoints

### 1. Get All Categories

```plaintext
GET /api/categories
```

**Response:**
```json
[
{
"id": "cat-1",
"name": "Books & Study Materials",
"description": "Textbooks, notes, study guides",
"parentId": null,
"iconUrl": "/icons/books.png",
"isActive": true,
"sortOrder": 1,
"createdAt": "2024-01-01T00:00:00Z",
"productCount": 45,
"subcategories": null
}
]

```plaintext

**Example:**
\`\`\`bash
curl http://localhost:8080/api/categories
```

### 2. Get Main Categories (with Subcategories)

```plaintext
GET /api/categories/main
```

**Response:**
```json
[
{
"id": "cat-1",
"name": "Books & Study Materials",
"description": "Textbooks, notes, study guides",
"parentId": null,
"iconUrl": "/icons/books.png",
"isActive": true,
"sortOrder": 1,
"createdAt": "2024-01-01T00:00:00Z",
"productCount": 45,
"subcategories": [
{
"id": "cat-1-1",
"name": "Computer Science",
"description": "CS textbooks and materials",
"parentId": "cat-1",
"iconUrl": null,
"isActive": true,
"sortOrder": 1,
"createdAt": "2024-01-01T00:00:00Z",
"productCount": 12,
"subcategories": null
}
]
}
]

```plaintext

### 3. Get Category by ID
```http
GET /api/categories/{categoryId}
```

**Response:** Single category object

**Example:**
```bash
curl [http://localhost:8080/api/categories/cat-1](http://localhost:8080/api/categories/cat-1)

```plaintext

### 4. Get Subcategories
```http
GET /api/categories/{parentId}/subcategories
```

**Response:** Array of subcategory objects

**Example:**
```bash
curl [http://localhost:8080/api/categories/cat-1/subcategories](http://localhost:8080/api/categories/cat-1/subcategories)

```plaintext

---

## 🖼️ Image Endpoints

### 1. Get Default Avatar
```http
GET /api/images/default-avatar.png
```

**Response:** PNG image file

### 2. Upload Profile Photo (Protected)

```plaintext
POST /api/images/upload-profile-photo
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**

- `file`: Image file (PNG, JPG, JPEG)


**Response:**
```json
"/api/images/profile-photos/1642234567890_profile.jpg"

```plaintext

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/images/upload-profile-photo \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@profile.jpg"
```

### 3. Get Profile Photo

```plaintext
GET /api/images/profile-photos/{filename}
```

**Response:** Image file

---

## ❌ Error Responses

### Standard Error Format

```json
{
"error": true,
"message": "Error description",
"timestamp": 1642234567890
}

```plaintext

### Common HTTP Status Codes

#### 400 Bad Request
\`\`\`json
{
  "error": true,
  "message": "Invalid request data",
  "timestamp": 1642234567890
}
```

#### 401 Unauthorized

```json
{
"error": true,
"message": "Authentication required",
"timestamp": 1642234567890
}

```plaintext

#### 403 Forbidden
\`\`\`json
{
  "error": true,
  "message": "Access denied",
  "timestamp": 1642234567890
}
```

#### 404 Not Found

```json
{
"error": true,
"message": "Resource not found",
"timestamp": 1642234567890
}

```plaintext

#### 500 Internal Server Error
\`\`\`json
{
  "error": true,
  "message": "An unexpected error occurred",
  "timestamp": 1642234567890
}
```

---

## 🔑 Authentication

### Bearer Token Format

```plaintext
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Expiration

- **Access Token**: 15 minutes
- **Refresh Token**: 7 days
- **OTP**: 15 minutes


---

## 📊 Product Conditions

- `NEW`: Brand new, never used
- `LIKE_NEW`: Barely used, excellent condition
- `GOOD`: Used but in good working condition
- `FAIR`: Shows wear but still functional
- `POOR`: Heavy wear, may need repairs
- `USED`: Previously owned, normal wear


## 🏆 Seller Badges

- `NEW_SELLER`: Just getting started
- `ACTIVE_SELLER`: Regular seller with good activity
- `TRUSTED_SELLER`: Reliable seller with great reviews
- `DEALHARBOR_CHOICE`: Top-rated seller with excellent track record
- `PREMIUM_SELLER`: Elite seller with outstanding performance
- `LEGENDARY_SELLER`: The best of the best


## 🎓 Valid Student Email Domains

- `@vitstudent.ac.in`
- `@vit.ac.in`
- `@vitchennai.ac.in`


---

## 🧪 Quick Test Commands

### Test Registration & Login Flow

```bash

# 1. Register

curl -X POST [http://localhost:8080/api/auth/register](http://localhost:8080/api/auth/register) -H "Content-Type: application/json" -d '{"email":"[test@example.com](mailto:test@example.com)","password":"password123","name":"Test User"}'

# 2. Verify (use OTP from console)

curl -X POST [http://localhost:8080/api/auth/verify](http://localhost:8080/api/auth/verify) -H "Content-Type: application/json" -d '{"email":"[test@example.com](mailto:test@example.com)","otp":"123456"}'

# 3. Login

curl -X POST [http://localhost:8080/api/auth/login](http://localhost:8080/api/auth/login) -H "Content-Type: application/json" -d '{"email":"[test@example.com](mailto:test@example.com)","password":"password123"}'

From what we've built today, we have:

1. **Authentication Routes** (`/api/auth/*`)
2. **Student Verification Routes** (`/api/student-verification/*`)
3. **Product Routes** (`/api/products/*`)
4. **Category Routes** (`/api/categories/*`)
5. **Test Routes** (`/api/test/*`)
6. **Image Routes** (`/api/images/*`)
7. **Root Routes** (`/`, `/health`)


I need to document each route with:

- HTTP method
- Endpoint
- Authentication requirement
- Request body (if applicable)
- Response format
- Example usage with curl
- Status codes


Let me create a comprehensive API documentation.
`</Thinking>`

### 📚 **DealHarbor Backend API Documentation**

Complete API reference for all endpoints with examples and usage.

`<CodeProject id="dealharbor-backend-fixes">````markdown file="API_DOCUMENTATION.md"
...
```

**Response:**
```json
"DealHarbor Backend is running!"

```plaintext

**Example:**
\`\`\`bash
curl http://localhost:8080/
```

### 2. Health Check

```plaintext
GET /health
```

**Response:**
```json
"OK"

```plaintext

**Example:**
\`\`\`bash
curl http://localhost:8080/health
```

### 3. Test Health

```plaintext
GET /api/test/health
```

**Response:**
```json
"Server is running successfully!"

```plaintext

### 4. Public Test Endpoint
```http
GET /api/test/public
```

**Response:**
```json
"This is a public endpoint - no auth required"

```plaintext

### 5. Protected Test Endpoint
```http
GET /api/test/protected
Authorization: Bearer {token}
```

**Response:**
```json
"Protected endpoint accessed by: [user@example.com](mailto:user@example.com)"

```plaintext

**Example:**
\`\`\`bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/test/protected
```

---

## 🔐 Authentication Endpoints

### 1. Register User

```plaintext
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)",
"password": "password123",
"name": "John Doe"
}

```plaintext

**Response:**
\`\`\`json
"Registration successful. OTP sent to email."
```

**Auto-Verification (VIT Email):**
```json
{
"email": "[student@vitstudent.ac.in](mailto:student@vitstudent.ac.in)",
"password": "password123",
"name": "VIT Student"
}

```plaintext

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123",
    "name": "John Doe"
  }'
```

### 2. Verify Email OTP

```plaintext
POST /api/auth/verify
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)",
"otp": "123456"
}

```plaintext

**Response:**
\`\`\`json
"Email verified successfully. You can now log in."
```

**Example:**
```bash
curl -X POST [http://localhost:8080/api/auth/verify](http://localhost:8080/api/auth/verify) -H "Content-Type: application/json" -d '{
"email": "[john.doe@example.com](mailto:john.doe@example.com)",
"otp": "123456"
}'

```plaintext

### 3. Resend OTP
```http
POST /api/auth/resend-otp
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)"
}

```plaintext

**Response:**
\`\`\`json
"New OTP sent to email."
```

### 4. Login

```plaintext
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)",
"password": "password123"
}

```plaintext

**Response:**
\`\`\`json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Example:**
```bash
curl -X POST [http://localhost:8080/api/auth/login](http://localhost:8080/api/auth/login) -H "Content-Type: application/json" -d '{
"email": "[test@dealharbor.com](mailto:test@dealharbor.com)",
"password": "password123"
}'

```plaintext

### 5. Get Current User Profile
```http
GET /api/auth/me
Authorization: Bearer {token}
```

**Response:**
```json
{
"id": "user-id-123",
"email": "[user@example.com](mailto:user@example.com)",
"name": "John Doe",
"bio": "Student at VIT",
"phoneNumber": "+91-9876543210",
"profilePhotoUrl": "/api/images/default-avatar.png",
"role": "USER",
"enabled": true,
"locked": false,
"provider": "LOCAL",
"createdAt": "2024-01-15T10:30:00Z",
"lastLoginAt": "2024-01-15T15:45:00Z",
"sellerBadge": "TRUSTED_SELLER",
"sellerRating": 4.5,
"buyerRating": 4.2,
"totalSales": 25,
"totalPurchases": 10,
"totalListings": 30,
"activeListings": 5,
"totalRevenue": 15000.00,
"responseRate": 95.5,
"positiveReviews": 20,
"negativeReviews": 2,
"firstSaleAt": "2024-01-10T12:00:00Z",
"universityId": "VIT2021001",
"graduationYear": 2025,
"department": "Computer Science",
"isVerifiedStudent": true,
"overallRating": 4.35,
"sellerSuccessRate": 83.33
}

```plaintext

**Example:**
\`\`\`bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/auth/me
```

### 6. Update Profile

```plaintext
PUT /api/auth/profile
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
"name": "John Updated",
"bio": "Updated bio",
"phoneNumber": "+91-9876543210"
}

```plaintext

**Response:** Same as Get Current User Profile

### 7. Change Password
```http
POST /api/auth/change-password
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
"currentPassword": "oldpassword123",
"newPassword": "newpassword123"
}

```plaintext

**Response:**
\`\`\`json
"Password changed successfully."
```

### 8. Forgot Password

```plaintext
POST /api/auth/forgot-password
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)"
}

```plaintext

**Response:**
\`\`\`json
"Password reset OTP sent to email."
```

### 9. Reset Password

```plaintext
POST /api/auth/reset-password
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)",
"otp": "123456",
"newPassword": "newpassword123"
}

```plaintext

**Response:**
\`\`\`json
"Password reset successfully."
```

### 10. Check Email Exists

```plaintext
POST /api/auth/check-email
Content-Type: application/json
```

**Request Body:**
```json
{
"email": "[user@example.com](mailto:user@example.com)"
}

```plaintext

**Response:**
\`\`\`json
{
  "exists": true,
  "verified": true
}
```

### 11. Refresh Token

```plaintext
POST /api/auth/refresh?refreshToken={token}
```

**Response:**
```json
{
"accessToken": "new-access-token",
"refreshToken": "new-refresh-token"
}

```plaintext

### 12. Logout
```http
POST /api/auth/logout?refreshToken={token}
Authorization: Bearer {token}
```

**Response:**
```json
"Logged out successfully."

```plaintext

### 13. Logout All Devices
```http
POST /api/auth/logout-all
Authorization: Bearer {token}
```

**Response:**
```json
"Logged out from all devices successfully."

```plaintext

### 14. Get Active Sessions
```http
GET /api/auth/sessions
Authorization: Bearer {token}
```

**Response:**
```json
[
{
"id": "session-id-1",
"ipAddress": "192.168.1.100",
"deviceInfo": "Windows PC",
"createdAt": "2024-01-15T10:00:00Z",
"lastUsedAt": "2024-01-15T15:30:00Z",
"current": false
}
]

```plaintext

### 15. Get Security Events
```http
GET /api/auth/security-events
Authorization: Bearer {token}
```

**Response:**
```json
[
{
"eventType": "LOGIN",
"ipAddress": "192.168.1.100",
"description": "Successful login",
"timestamp": "2024-01-15T15:30:00Z"
}
]

```plaintext

### 16. Get Account Stats
```http
GET /api/auth/account-stats
Authorization: Bearer {token}
```

**Response:**
```json
{
"activeSessions": 2,
"lastLogin": "2024-01-15T15:30:00Z",
"lastLoginIp": "192.168.1.100",
"recentLoginAttempts": 0,
"twoFactorEnabled": false,
"accountCreated": "2024-01-01T10:00:00Z"
}

```plaintext

---

## 🎓 Student Verification Endpoints

### 1. Send Student Email OTP
```http
POST /api/student-verification/send-otp
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
"studentEmail": "[john.doe@vitstudent.ac.in](mailto:john.doe@vitstudent.ac.in)"
}

```plaintext

**Response:**
\`\`\`json
"OTP sent to your student email. Please check your inbox."
```

**Example:**
```bash
curl -X POST [http://localhost:8080/api/student-verification/send-otp](http://localhost:8080/api/student-verification/send-otp) -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d '{
"studentEmail": "[john.doe@vitstudent.ac.in](mailto:john.doe@vitstudent.ac.in)"
}'

```plaintext

### 2. Verify Student Email OTP
```http
POST /api/student-verification/verify-otp
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body (Minimal):**
```json
{
"studentEmail": "[john.doe@vitstudent.ac.in](mailto:john.doe@vitstudent.ac.in)",
"otp": "123456"
}

```plaintext

**Request Body (Complete - All Optional):**
\`\`\`json
{
  "studentEmail": "john.doe@vitstudent.ac.in",
  "otp": "123456",
  "universityId": "VIT2021001",
  "graduationYear": 2025,
  "department": "Computer Science"
}
```

**Response:**
```json
"Student verification successful! You are now a verified VIT student."

```plaintext

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/student-verification/verify-otp \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "studentEmail": "john.doe@vitstudent.ac.in",
    "otp": "123456",
    "universityId": "VIT2021001",
    "graduationYear": 2025,
    "department": "Computer Science"
  }'
```

---

## 📦 Product Endpoints

### 1. Get All Products (Public)

```plaintext
GET /api/products?page=0&size=20&sortBy=date_desc
```

**Query Parameters:**

- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sortBy` (optional): Sort order (default: date_desc)

- `price_asc`, `price_desc`, `date_asc`, `date_desc`, `popularity`





**Response:**
```json
{
"content": [
{
"id": "product-id-1",
"title": "MacBook Pro 2021",
"description": "Excellent condition MacBook Pro",
"price": 85000.00,
"originalPrice": 120000.00,
"isNegotiable": true,
"condition": "LIKE_NEW",
"brand": "Apple",
"model": "MacBook Pro 14-inch",
"status": "APPROVED",
"pickupLocation": "VIT Campus",
"deliveryAvailable": false,
"viewCount": 45,
"favoriteCount": 12,
"isFeatured": false,
"tags": ["laptop", "apple", "macbook"],
"createdAt": "2024-01-15T10:00:00Z",
"updatedAt": "2024-01-15T10:00:00Z",
"sellerId": "seller-id-1",
"sellerName": "John Seller",
"sellerBadge": "TRUSTED_SELLER",
"sellerRating": 4.5,
"sellerIsVerifiedStudent": true,
"categoryId": "cat-2-1",
"categoryName": "Laptops & Computers",
"images": [
{
"id": "img-1",
"imageUrl": "/api/images/products/macbook1.jpg",
"altText": "MacBook Pro front view",
"isPrimary": true,
"sortOrder": 0
}
],
"primaryImageUrl": "/api/images/products/macbook1.jpg"
}
],
"page": 0,
"size": 20,
"totalElements": 150,
"totalPages": 8,
"first": true,
"last": false,
"hasNext": true,
"hasPrevious": false
}

```plaintext

**Example:**
\`\`\`bash
curl "http://localhost:8080/api/products?page=0&size=10&sortBy=price_asc"
```

### 2. Get Products by Category

```plaintext
GET /api/products/category/{categoryId}?page=0&size=20&sortBy=date_desc
```

**Example:**
```bash
curl "[http://localhost:8080/api/products/category/cat-2-1?page=0&size=10](http://localhost:8080/api/products/category/cat-2-1?page=0&size=10)"

```plaintext

### 3. Search Products
```http
POST /api/products/search
Content-Type: application/json
```

**Request Body:**
```json
{
"keyword": "macbook",
"categoryId": "cat-2-1",
"minPrice": 50000.00,
"maxPrice": 100000.00,
"conditions": ["LIKE_NEW", "GOOD"],
"location": "VIT Campus",
"deliveryAvailable": false,
"isNegotiable": true,
"verifiedStudentsOnly": true,
"sortBy": "price_asc",
"page": 0,
"size": 20
}

```plaintext

**Response:** Same format as Get All Products

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "laptop",
    "minPrice": 30000,
    "maxPrice": 80000,
    "verifiedStudentsOnly": true,
    "page": 0,
    "size": 10
  }'
```

### 4. Get Featured Products

```plaintext
GET /api/products/featured?page=0&size=20
```

**Example:**
```bash
curl "[http://localhost:8080/api/products/featured?page=0&size=5](http://localhost:8080/api/products/featured?page=0&size=5)"

```plaintext

### 5. Get Product by ID
```http
GET /api/products/{productId}
```

**Response:** Single product object (same format as in list)

**Example:**
```bash
curl [http://localhost:8080/api/products/product-id-123](http://localhost:8080/api/products/product-id-123)

```plaintext

### 6. Create Product (Protected)
```http
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
"title": "iPhone 13 Pro",
"description": "Excellent condition iPhone 13 Pro with all accessories",
"price": 65000.00,
"originalPrice": 79900.00,
"isNegotiable": true,
"condition": "LIKE_NEW",
"brand": "Apple",
"model": "iPhone 13 Pro",
"categoryId": "cat-2-2",
"tags": ["iphone", "apple", "smartphone"],
"pickupLocation": "VIT Main Campus",
"deliveryAvailable": false,
"imageUrls": [
"/api/images/products/iphone1.jpg",
"/api/images/products/iphone2.jpg"
]
}

```plaintext

**Response:** Created product object

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 75000.00,
    "originalPrice": 90000.00,
    "isNegotiable": true,
    "condition": "GOOD",
    "brand": "ASUS",
    "model": "ROG Strix",
    "categoryId": "cat-2-1",
    "tags": ["gaming", "laptop", "asus"],
    "pickupLocation": "VIT Campus",
    "deliveryAvailable": false,
    "imageUrls": []
  }'
```

### 7. Get User's Products (Protected)

```plaintext
GET /api/products/my-products?page=0&size=20
Authorization: Bearer {token}
```

**Response:** Same format as Get All Products (user's products only)

**Example:**
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" "[http://localhost:8080/api/products/my-products?page=0&size=10](http://localhost:8080/api/products/my-products?page=0&size=10)"

```plaintext

### 8. Update Product (Protected)
```http
PUT /api/products/{productId}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:** Same as Create Product (all fields optional)
```json
{
"title": "Updated iPhone 13 Pro",
"price": 60000.00,
"isNegotiable": false
}

```plaintext

**Response:** Updated product object

### 9. Delete Product (Protected)
```http
DELETE /api/products/{productId}
Authorization: Bearer {token}
```

**Response:**
```json
"Product deleted successfully"

```plaintext

**Example:**
\`\`\`bash
curl -X DELETE http://localhost:8080/api/products/product-id-123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📂 Category Endpoints

### 1. Get All Categories

```plaintext
GET /api/categories
```

**Response:**
```json
[
{
"id": "cat-1",
"name": "Books & Study Materials",
"description": "Textbooks, notes, study guides",
"parentId": null,
"iconUrl": "/icons/books.png",
"isActive": true,
"sortOrder": 1,
"createdAt": "2024-01-01T00:00:00Z",
"productCount": 45,
"subcategories": null
}
]

```plaintext

**Example:**
\`\`\`bash
curl http://localhost:8080/api/categories
```

### 2. Get Main Categories (with Subcategories)

```plaintext
GET /api/categories/main
```

**Response:**
```json
[
{
"id": "cat-1",
"name": "Books & Study Materials",
"description": "Textbooks, notes, study guides",
"parentId": null,
"iconUrl": "/icons/books.png",
"isActive": true,
"sortOrder": 1,
"createdAt": "2024-01-01T00:00:00Z",
"productCount": 45,
"subcategories": [
{
"id": "cat-1-1",
"name": "Computer Science",
"description": "CS textbooks and materials",
"parentId": "cat-1",
"iconUrl": null,
"isActive": true,
"sortOrder": 1,
"createdAt": "2024-01-01T00:00:00Z",
"productCount": 12,
"subcategories": null
}
]
}
]

```plaintext

### 3. Get Category by ID
```http
GET /api/categories/{categoryId}
```

**Response:** Single category object

**Example:**
```bash
curl [http://localhost:8080/api/categories/cat-1](http://localhost:8080/api/categories/cat-1)

```plaintext

### 4. Get Subcategories
```http
GET /api/categories/{parentId}/subcategories
```

**Response:** Array of subcategory objects

**Example:**
```bash
curl [http://localhost:8080/api/categories/cat-1/subcategories](http://localhost:8080/api/categories/cat-1/subcategories)

```plaintext

---

## 🖼️ Image Endpoints

### 1. Get Default Avatar
```http
GET /api/images/default-avatar.png
```

**Response:** PNG image file

### 2. Upload Profile Photo (Protected)

```plaintext
POST /api/images/upload-profile-photo
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**

- `file`: Image file (PNG, JPG, JPEG)


**Response:**
```json
"/api/images/profile-photos/1642234567890_profile.jpg"

```plaintext

**Example:**
\`\`\`bash
curl -X POST http://localhost:8080/api/images/upload-profile-photo \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@profile.jpg"
```

### 3. Get Profile Photo

```plaintext
GET /api/images/profile-photos/{filename}
```

**Response:** Image file

---

## ❌ Error Responses

### Standard Error Format

```json
{
"error": true,
"message": "Error description",
"timestamp": 1642234567890
}

```plaintext

### Common HTTP Status Codes

#### 400 Bad Request
\`\`\`json
{
  "error": true,
  "message": "Invalid request data",
  "timestamp": 1642234567890
}
```

#### 401 Unauthorized

```json
{
"error": true,
"message": "Authentication required",
"timestamp": 1642234567890
}

```plaintext

#### 403 Forbidden
\`\`\`json
{
  "error": true,
  "message": "Access denied",
  "timestamp": 1642234567890
}
```

#### 404 Not Found

```json
{
"error": true,
"message": "Resource not found",
"timestamp": 1642234567890
}

```plaintext

#### 500 Internal Server Error
\`\`\`json
{
  "error": true,
  "message": "An unexpected error occurred",
  "timestamp": 1642234567890
}
```

---

## 🔑 Authentication

### Bearer Token Format

```plaintext
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Expiration

- **Access Token**: 15 minutes
- **Refresh Token**: 7 days
- **OTP**: 15 minutes


---

## 📊 Product Conditions

- `NEW`: Brand new, never used
- `LIKE_NEW`: Barely used, excellent condition
- `GOOD`: Used but in good working condition
- `FAIR`: Shows wear but still functional
- `POOR`: Heavy wear, may need repairs
- `USED`: Previously owned, normal wear


## 🏆 Seller Badges

- `NEW_SELLER`: Just getting started
- `ACTIVE_SELLER`: Regular seller with good activity
- `TRUSTED_SELLER`: Reliable seller with great reviews
- `DEALHARBOR_CHOICE`: Top-rated seller with excellent track record
- `PREMIUM_SELLER`: Elite seller with outstanding performance
- `LEGENDARY_SELLER`: The best of the best


## 🎓 Valid Student Email Domains

- `@vitstudent.ac.in`
- `@vit.ac.in`
- `@vitchennai.ac.in`


---

## 🧪 Quick Test Commands

### Test Registration & Login Flow

```bash

# 1. Register

curl -X POST [http://localhost:8080/api/auth/register](http://localhost:8080/api/auth/register) -H "Content-Type: application/json" -d '{"email":"[test@example.com](mailto:test@example.com)","password":"password123","name":"Test User"}'

# 2. Verify (use OTP from console)

curl -X POST [http://localhost:8080/api/auth/verify](http://localhost:8080/api/auth/verify) -H "Content-Type: application/json" -d '{"email":"[test@example.com](mailto:test@example.com)","otp":"123456"}'

# 3. Login

curl -X POST [http://localhost:8080/api/auth/login](http://localhost:8080/api/auth/login) -H "Content-Type: application/json" -d '{"email":"[test@example.com](mailto:test@example.com)","password":"password123"}'

# 4. Get Profile (use token from login)

curl -H "Authorization: Bearer YOUR_TOKEN" [http://localhost:8080/api/auth/me](http://localhost:8080/api/auth/me)

```plaintext

### Test Student Verification
\`\`\`bash
# 1. Send OTP
curl -X POST http://localhost:8080/api/student-verification/send-otp \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"studentEmail":"test@vitstudent.ac.in"}'

# 2. Verify OTP
curl -X POST http://localhost:8080/api/student-verification/verify-otp \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"studentEmail":"test@vitstudent.ac.in","otp":"123456"}'
```

### Test Product Operations

```bash

# 1. Get all products

curl [http://localhost:8080/api/products](http://localhost:8080/api/products)

# 2. Search products

curl -X POST [http://localhost:8080/api/products/search](http://localhost:8080/api/products/search) -H "Content-Type: application/json" -d '{"keyword":"laptop","page":0,"size":5}'

# 3. Create product

curl -X POST [http://localhost:8080/api/products](http://localhost:8080/api/products) -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d '{"title":"Test Product","description":"Test","price":1000,"categoryId":"cat-1","condition":"GOOD"}'

```plaintext

---

## 📞 Support

For API support and questions:
- **Email**: support@dealharbor.com
- **Documentation**: This file
- **Status Page**: `GET /health`

---

*Last Updated: January 2024*
*Version: 1.0.0*
```

```shellscript
#!/bin/bash

# DealHarbor Backend - Complete API Test Suite
BASE_URL="http://localhost:8080"
echo "🚀 Testing DealHarbor Backend - All Endpoints"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local expected_status=$3
    local description=$4
    local data=$5
    local headers=$6
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "\n${BLUE}Test $TOTAL_TESTS: $description${NC}"
    echo "  $method $endpoint"
    
    if [ -n "$data" ] && [ -n "$headers" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint" -H "$headers" -d "$data")
    elif [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint" -H "Content-Type: application/json" -d "$data")
    elif [ -n "$headers" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint" -H "$headers")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "  ${GREEN}✅ PASSED${NC} (Status: $http_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ FAILED${NC} (Expected: $expected_status, Got: $http_code)"
    fi
    
    if [ ${#body} -gt 100 ]; then
        echo "  Response: ${body:0:100}..."
    else
        echo "  Response: $body"
    fi
}

echo -e "\n${YELLOW}=== HEALTH & TEST ENDPOINTS ===${NC}"

test_endpoint "GET" "/" 200 "Root health check"
test_endpoint "GET" "/health" 200 "Health check"
test_endpoint "GET" "/api/test/health" 200 "Test health endpoint"
test_endpoint "GET" "/api/test/public" 200 "Public test endpoint"

echo -e "\n${YELLOW}=== AUTHENTICATION ENDPOINTS ===${NC}"

# Test user registration
test_endpoint "POST" "/api/auth/register" 200 "Register new user" \
    '{"email":"testuser@example.com","password":"password123","name":"Test User"}'

# Test student auto-verification registration
test_endpoint "POST" "/api/auth/register" 200 "Register with student email (auto-verify)" \
    '{"email":"autotest@vitstudent.ac.in","password":"password123","name":"Auto Test Student"}'

# Test existing user login
test_endpoint "POST" "/api/auth/login" 200 "Login with existing user" \
    '{"email":"test@dealharbor.com","password":"password123"}'

# Store token for authenticated requests (you'll need to extract this manually)
TOKEN="YOUR_TOKEN_HERE"

# Test check email
test_endpoint "POST" "/api/auth/check-email" 200 "Check if email exists" \
    '{"email":"test@dealharbor.com"}'

# Test forgot password
test_endpoint "POST" "/api/auth/forgot-password" 200 "Forgot password request" \
    '{"email":"test@dealharbor.com"}'

echo -e "\n${YELLOW}=== CATEGORY ENDPOINTS ===${NC}"

test_endpoint "GET" "/api/categories" 200 "Get all categories"
test_endpoint "GET" "/api/categories/main" 200 "Get main categories with subcategories"
test_endpoint "GET" "/api/categories/cat-1" 200 "Get category by ID"
test_endpoint "GET" "/api/categories/cat-1/subcategories" 200 "Get subcategories"

echo -e "\n${YELLOW}=== PRODUCT ENDPOINTS (PUBLIC) ===${NC}"

test_endpoint "GET" "/api/products" 200 "Get all products"
test_endpoint "GET" "/api/products?page=0&size=5&sortBy=price_asc" 200 "Get products with pagination and sorting"
test_endpoint "GET" "/api/products/category/cat-1" 200 "Get products by category"
test_endpoint "GET" "/api/products/featured" 200 "Get featured products"

# Test product search
test_endpoint "POST" "/api/products/search" 200 "Search products" \
    '{"keyword":"book","page":0,"size":5}'

# Test advanced search
test_endpoint "POST" "/api/products/search" 200 "Advanced product search" \
    '{"minPrice":100,"maxPrice":5000,"verifiedStudentsOnly":true,"page":0,"size":10}'

echo -e "\n${YELLOW}=== IMAGE ENDPOINTS ===${NC}"

test_endpoint "GET" "/api/images/default-avatar.png" 200 "Get default avatar"

echo -e "\n${YELLOW}=== ERROR HANDLING TESTS ===${NC}"

test_endpoint "GET" "/api/products/nonexistent-id" 400 "Get non-existent product"
test_endpoint "GET" "/api/categories/nonexistent-id" 400 "Get non-existent category"
test_endpoint "POST" "/api/auth/login" 400 "Login with invalid credentials" \
    '{"email":"invalid@example.com","password":"wrongpassword"}'

echo -e "\n${YELLOW}=== PROTECTED ENDPOINTS (Need Token) ===${NC}"
echo -e "${RED}Note: These tests require a valid JWT token${NC}"

# These would need a real token to test properly
echo "  GET /api/auth/me - Get current user profile"
echo "  POST /api/products - Create product"
echo "  GET /api/products/my-products - Get user's products"
echo "  POST /api/student-verification/send-otp - Send student verification OTP"
echo "  POST /api/student-verification/verify-otp - Verify student OTP"

echo -e "\n${YELLOW}=== TEST SUMMARY ===${NC}"
echo "Total Tests: $TOTAL_TESTS"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$((TOTAL_TESTS - PASSED_TESTS))${NC}"

if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
    echo -e "\n${GREEN}🎉 All tests passed!${NC}"
else
    echo -e "\n${YELLOW}⚠️  Some tests failed. Check the output above.${NC}"
fi

echo -e "\n${BLUE}📝 Next Steps:${NC}"
echo "1. Get a JWT token by logging in"
echo "2. Test protected endpoints manually with the token"
echo "3. Test OTP verification flows"
echo "4. Test file upload endpoints"

echo -e "\n${BLUE}🔧 Manual Test Commands:${NC}"
echo "# Get token:"
echo "curl -X POST $BASE_URL/api/auth/login -H 'Content-Type: application/json' -d '{\"email\":\"test@dealharbor.com\",\"password\":\"password123\"}'"
echo ""
echo "# Test protected endpoint:"
echo "curl -H 'Authorization: Bearer YOUR_TOKEN' $BASE_URL/api/auth/me"
echo ""
echo "# Test student verification:"
echo "curl -X POST $BASE_URL/api/student-verification/send-otp -H 'Authorization: Bearer YOUR_TOKEN' -H 'Content-Type: application/json' -d '{\"studentEmail\":\"test@vitstudent.ac.in\"}'"
```

```json
{
  "info": {
    "name": "DealHarbor Backend API",
    "description": "Complete API collection for DealHarbor Backend",
    "version": "1.0.0"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    },
    {
      "key": "accessToken",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "Health & Test",
      "item": [
        {
          "name": "Root Health Check",
          "request": {
            "method": "GET",
            "header": [],
            "url": "{{baseUrl}}/"
          }
        },
        {
          "name": "Health Check",
          "request": {
            "method": "GET",
            "header": [],
            "url": "{{baseUrl}}/health"
          }
        },
        {
          "name": "Test Health",
          "request": {
            "method": "GET",
            "header": [],
            "url": "{{baseUrl}}/api/test/health"
          }
        },
        {
          "name": "Public Test",
          "request": {
            "method": "GET",
            "header": [],
            "url": "{{baseUrl}}/api/test/public"
          }
        },
        {
          "name": "Protected Test",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "url": "{{baseUrl}}/api/test/protected"
          }
        }
      ]
    },
```


### ** New Features Added:**

1. **💖 Favorites/Wishlist System**
2. **⭐ Reviews & Ratings (Products + Users)**
3. **💬 Messaging System**
4. **🔔 Notification System**
5. **📦 Enhanced Order Management**


---

## 📚 **API Endpoints Documentation**

### **💖 Favorites Endpoints**

#### **1. Add Product to Favorites**

```plaintext
POST /api/favorites/{productId}
Authorization: Bearer {token}
```

**Example:**

```shellscript
curl -X POST http://localhost:8080/api/favorites/product-123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:**

```json
"Product added to favorites"
```

#### **2. Remove from Favorites**

```plaintext
DELETE /api/favorites/{productId}
Authorization: Bearer {token}
```

#### **3. Get User's Favorites**

```plaintext
GET /api/favorites?page=0&size=20
Authorization: Bearer {token}
```

**Response:**

```json
{
  "content": [
    {
      "id": "fav-123",
      "productId": "product-123",
      "productTitle": "MacBook Pro 2021",
      "productImageUrl": "/api/images/products/macbook1.jpg",
      "productPrice": "85000.00",
      "productStatus": "APPROVED",
      "sellerId": "seller-123",
      "sellerName": "John Seller",
      "createdAt": "2024-01-15T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

#### **4. Check if Product is in Favorites**

```plaintext
GET /api/favorites/check/{productId}
Authorization: Bearer {token}
```

**Response:**

```json
true
```

---

### **⭐ Review Endpoints**

#### **1. Create Product Review**

```plaintext
POST /api/reviews/products
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "productId": "product-123",
  "orderId": "order-456",
  "rating": 4.5,
  "comment": "Great product, exactly as described!"
}
```

**Response:**

```json
{
  "id": "review-123",
  "reviewerId": "user-123",
  "reviewerName": "John Doe",
  "reviewerProfilePhoto": "/api/images/default-avatar.png",
  "reviewerIsVerifiedStudent": true,
  "rating": 4.5,
  "comment": "Great product, exactly as described!",
  "isVerifiedPurchase": true,
  "isHelpful": false,
  "helpfulCount": 0,
  "createdAt": "2024-01-15T10:00:00Z"
}
```

#### **2. Get Product Reviews**

```plaintext
GET /api/reviews/products/{productId}?page=0&size=10
```

#### **3. Create User Review (Seller/Buyer)**

```plaintext
POST /api/reviews/users
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "orderId": "order-123",
  "revieweeId": "seller-123",
  "reviewType": "SELLER_REVIEW",
  "rating": 4.8,
  "comment": "Excellent seller, very responsive!",
  "communicationRating": 5.0,
  "reliabilityRating": 4.5,
  "speedRating": 4.8
}
```

#### **4. Get User Reviews**

```plaintext
GET /api/reviews/users/{userId}?reviewType=SELLER_REVIEW&page=0&size=10
```

---

### **💬 Messaging Endpoints**

#### **1. Start Conversation**

```plaintext
POST /api/messages/conversations?otherUserId=user-123&productId=product-456
Authorization: Bearer {token}
```

**Response:**

```json
{
  "id": "conv-123",
  "otherUserId": "user-123",
  "otherUserName": "Jane Seller",
  "otherUserProfilePhoto": "/api/images/default-avatar.png",
  "productId": "product-456",
  "productTitle": "iPhone 13 Pro",
  "productImageUrl": "/api/images/products/iphone1.jpg",
  "orderId": null,
  "lastMessage": "",
  "lastMessageAt": "2024-01-15T10:00:00Z",
  "unreadCount": 0,
  "isActive": true
}
```

#### **2. Send Message**

```plaintext
POST /api/messages/conversations/{conversationId}/messages
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "content": "Hi! Is this product still available?",
  "messageType": "TEXT",
  "attachmentUrl": null
}
```

#### **3. Get Conversations**

```plaintext
GET /api/messages/conversations?page=0&size=20
Authorization: Bearer {token}
```

#### **4. Get Conversation Messages**

```plaintext
GET /api/messages/conversations/{conversationId}/messages?page=0&size=50
Authorization: Bearer {token}
```

#### **5. Mark Conversation as Read**

```plaintext
PUT /api/messages/conversations/{conversationId}/read
Authorization: Bearer {token}
```

#### **6. Get Unread Message Count**

```plaintext
GET /api/messages/unread-count
Authorization: Bearer {token}
```

**Response:**

```json
3
```

---

### **🔔 Notification Endpoints**

#### **1. Get User Notifications**

```plaintext
GET /api/notifications?page=0&size=20&unreadOnly=false
Authorization: Bearer {token}
```

**Response:**

```json
{
  "content": [
    {
      "id": "notif-123",
      "title": "New Order Received",
      "message": "John Doe wants to buy your product 'MacBook Pro 2021'",
      "type": "ORDER_CREATED",
      "actionUrl": "/orders/order-123",
      "relatedEntityId": "order-123",
      "relatedEntityType": "ORDER",
      "isRead": false,
      "createdAt": "2024-01-15T10:00:00Z",
      "readAt": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 10
}
```

#### **2. Get Unread Notification Count**

```plaintext
GET /api/notifications/unread-count
Authorization: Bearer {token}
```

#### **3. Mark Notification as Read**

```plaintext
PUT /api/notifications/{notificationId}/read
Authorization: Bearer {token}
```

#### **4. Mark All Notifications as Read**

```plaintext
PUT /api/notifications/mark-all-read
Authorization: Bearer {token}
```

---

### **📦 Enhanced Order Endpoints**

#### **1. Create Order**

```plaintext
POST /api/orders
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "productId": "product-123",
  "agreedPrice": 80000.00,
  "buyerNotes": "Can we meet at the main campus?",
  "pickupLocation": "VIT Main Campus",
  "deliveryMethod": "PICKUP"
}
```

#### **2. Update Order Status (Seller Only)**

```plaintext
PUT /api/orders/{orderId}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "status": "CONFIRMED",
  "sellerNotes": "Order confirmed! Let's meet tomorrow at 3 PM.",
  "pickupLocation": "VIT Main Gate"
}
```

#### **3. Get Buyer Orders**

```plaintext
GET /api/orders/buyer?status=PENDING&page=0&size=20
Authorization: Bearer {token}
```

#### **4. Get Seller Orders**

```plaintext
GET /api/orders/seller?status=CONFIRMED&page=0&size=20
Authorization: Bearer {token}
```

#### **5. Get Order Details**

```plaintext
GET /api/orders/{orderId}
Authorization: Bearer {token}
```

#### **6. Cancel Order**

```plaintext
DELETE /api/orders/{orderId}
Authorization: Bearer {token}
```

---

## 🧪 **Complete Testing Examples**

### **Test Complete User Flow:**

```shellscript
# 1. Login and get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@dealharbor.com","password":"password123"}' | \
  jq -r '.accessToken')

# 2. Add product to favorites
curl -X POST http://localhost:8080/api/favorites/product-123 \
  -H "Authorization: Bearer $TOKEN"

# 3. Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-123",
    "agreedPrice": 75000.00,
    "buyerNotes": "Interested in this product",
    "pickupLocation": "VIT Campus",
    "deliveryMethod": "PICKUP"
  }'

# 4. Start a conversation
curl -X POST "http://localhost:8080/api/messages/conversations?otherUserId=seller-123&productId=product-123" \
  -H "Authorization: Bearer $TOKEN"

# 5. Send a message
curl -X POST http://localhost:8080/api/messages/conversations/conv-123/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hi! Is this product still available?",
    "messageType": "TEXT"
  }'

# 6. Create a product review
curl -X POST http://localhost:8080/api/reviews/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-123",
    "orderId": "order-456",
    "rating": 4.5,
    "comment": "Great product!"
  }'

# 7. Get notifications
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/notifications?page=0&size=10"
```

## **Admin Endpoints Available:**

- `GET /api/admin/dashboard` - Admin dashboard with stats
- `GET /api/admin/products` - All products (any status)
- `PUT /api/admin/products/{id}` - Update any product
- `GET /api/admin/users` - All users with filters
- `PUT /api/admin/users/{id}` - Update any user
- `GET /api/admin/products/search?keyword=` - Search products
- `GET /api/admin/users/search?keyword=` - Search users

# Current project structure

src/main/java/com/dealharbor/dealharbor_backend/
├── 📁 config/
│   ├── GlobalExceptionHandler.java
│   └── SecurityConfig.java
│
├── 📁 controllers/
│   ├── AdminController.java              🆕 NEW - Admin management
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── FavoriteController.java
│   ├── ImageController.java
│   ├── MessagingController.java
│   ├── NotificationController.java
│   ├── OrderController.java
│   ├── ProductController.java
│   ├── ReviewController.java
│   ├── RootController.java
│   ├── StudentVerificationController.java
│   └── TestController.java
│
├── 📁 dto/
│   ├── AccountStatsResponse.java
│   ├── AdminDashboardResponse.java       🆕 NEW - Admin dashboard data
│   ├── AdminProductActionRequest.java    🆕 NEW - Admin product actions
│   ├── AdminUserActionRequest.java       🆕 NEW - Admin user actions
│   ├── AdminVerifyStudentRequest.java
│   ├── CategoryResponse.java
│   ├── ChangeEmailRequest.java
│   ├── ChangePasswordRequest.java
│   ├── CheckEmailRequest.java
│   ├── CheckEmailResponse.java
│   ├── ConversationResponse.java
│   ├── DeleteAccountRequest.java
│   ├── FavoriteResponse.java
│   ├── ForgotPasswordRequest.java
│   ├── GitHubOAuth2UserInfo.java
│   ├── GoogleOAuth2UserInfo.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── MessageRequest.java
│   ├── MessageResponse.java
│   ├── NotificationResponse.java
│   ├── OAuth2UserInfo.java
│   ├── OrderCreateRequest.java
│   ├── OrderResponse.java
│   ├── OrderUpdateRequest.java
│   ├── OtpVerifyRequest.java
│   ├── PagedResponse.java
│   ├── ProductCreateRequest.java
│   ├── ProductImageResponse.java
│   ├── ProductResponse.java
│   ├── ProductReviewRequest.java
│   ├── ProductReviewResponse.java
│   ├── ProductSearchRequest.java
│   ├── ProductUpdateRequest.java
│   ├── RegisterRequest.java
│   ├── ResendOtpRequest.java
│   ├── ResetPasswordRequest.java
│   ├── SecurityEventResponse.java
│   ├── StudentEmailOtpRequest.java
│   ├── StudentEmailOtpVerifyRequest.java
│   ├── StudentVerificationRequest.java
│   ├── UpdateProfileRequest.java
│   ├── UserProfileResponse.java
│   ├── UserReviewRequest.java
│   ├── UserReviewResponse.java
│   └── UserSessionResponse.java
│
├── 📁 entities/
│   ├── AdminAction.java
│   ├── Category.java
│   ├── Conversation.java
│   ├── Favorite.java
│   ├── LoginAttempt.java
│   ├── Message.java
│   ├── Notification.java
│   ├── Order.java
│   ├── OtpToken.java
│   ├── Product.java
│   ├── ProductImage.java
│   ├── ProductReview.java
│   ├── RefreshToken.java
│   ├── SecurityEvent.java
│   ├── StudentOtpToken.java
│   ├── User.java
│   ├── UserReview.java
│   └── UserSession.java
│
├── 📁 enums/
│   ├── AuthProvider.java
│   ├── DeliveryMethod.java
│   ├── MessageType.java
│   ├── NotificationType.java
│   ├── OrderStatus.java
│   ├── ProductCondition.java
│   ├── ProductStatus.java
│   ├── ReviewType.java
│   ├── Role.java
│   ├── SellerBadge.java
│   └── UserStatus.java
│
├── 📁 repositories/
│   ├── AdminActionRepository.java
│   ├── CategoryRepository.java
│   ├── ConversationRepository.java
│   ├── FavoriteRepository.java
│   ├── LoginAttemptRepository.java
│   ├── MessageRepository.java
│   ├── NotificationRepository.java
│   ├── OrderRepository.java            ✅ UPDATED - Admin queries
│   ├── OtpTokenRepository.java
│   ├── ProductImageRepository.java
│   ├── ProductRepository.java          ✅ UPDATED - Admin queries
│   ├── ProductReviewRepository.java    ✅ UPDATED - Admin queries
│   ├── RefreshTokenRepository.java
│   ├── SecurityEventRepository.java
│   ├── StudentOtpTokenRepository.java
│   ├── UserRepository.java             ✅ UPDATED - Admin queries
│   ├── UserReviewRepository.java       ✅ UPDATED - Admin queries
│   └── UserSessionRepository.java
│
├── 📁 security/
│   ├── JwtAuthFilter.java
│   ├── JwtTokenProvider.java
│   ├── OAuth2AuthenticationSuccessHandler.java
│   ├── OAuth2UserInfoFactory.java
│   └── UserPrincipal.java
│
├── 📁 services/
│   ├── AdminService.java               🆕 NEW - Admin business logic
│   ├── AuthService.java
│   ├── CategoryInitService.java        🆕 NEW - Initialize categories
│   ├── CategoryService.java
│   ├── CustomOAuth2UserService.java
│   ├── DatabaseInitService.java
│   ├── EmailService.java               ✅ UPDATED - Admin notifications
│   ├── FavoriteService.java
│   ├── MessagingService.java
│   ├── NotificationService.java
│   ├── OrderService.java
│   ├── ProductService.java             ✅ UPDATED - Seller ID attachment
│   ├── ReviewService.java
│   ├── SecurityService.java
│   ├── StudentVerificationService.java
│   └── UserDetailsServiceImpl.java
│
└── DealharbourBackendApplication.java

📁 src/main/resources/
├── application.properties
├── application-local.properties
└── 📁 static/
    └── default-avatar.png

📁 database-migrations/
├── database-complete-features-migration.sql
├── database-complete-migration.sql
├── database-final-migration.sql         🆕 NEW - Final schema
├── database-migration.sql
├── database-student-verification-migration.sql
└── database-update-migration.sql

📁 scripts/
└── test-api-endpoints.sh