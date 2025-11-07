# **DealHarbor Backend API Documentation**

## **Complete API Routes with Postman Examples**

---

## 🔐 **1. AUTHENTICATION ROUTES**

### **1.1 User Registration**

```plaintext
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@vitstudent.ac.in",
  "password": "SecurePass123!",
  "phoneNumber": "+91-9876543210"
}
```

**Response:**

```json
{
  "message": "Registration successful. OTP sent to email."
}
```

### **1.2 Verify Email OTP**

```plaintext
POST http://localhost:8080/api/auth/verify
Content-Type: application/json

{
  "email": "john.doe@vitstudent.ac.in",
  "otp": "123456"
}
```

**Response:**

```json
{
  "message": "Email verified successfully. You can now log in."
}
```

### **1.3 Resend OTP**

```plaintext
POST http://localhost:8080/api/auth/resend-otp
Content-Type: application/json

{
  "email": "john.doe@vitstudent.ac.in"
}
```

**Response:**

```json
{
  "message": "New OTP sent to email."
}
```

### **1.4 User Login**

```plaintext
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@vitstudent.ac.in",
  "password": "SecurePass123!"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "user-123",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@vitstudent.ac.in",
    "role": "USER",
    "isStudentVerified": false,
    "profilePhotoUrl": "/api/images/default-avatar.png"
  }
}
```

### **1.5 Refresh Token**

```plaintext
POST http://localhost:8080/api/auth/refresh?refreshToken=your_refresh_token_here
```

**Response:**

```json
{
  "accessToken": "new_access_token",
  "refreshToken": "new_refresh_token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### **1.6 Forgot Password**

```plaintext
POST http://localhost:8080/api/auth/forgot-password
Content-Type: application/json

{
  "email": "john.doe@vitstudent.ac.in"
}
```

**Response:**

```json
{
  "message": "Password reset OTP sent to email."
}
```

### **1.7 Reset Password**

```plaintext
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
  "email": "john.doe@vitstudent.ac.in",
  "otp": "123456",
  "newPassword": "NewSecurePass123!"
}
```

**Response:**

```json
{
  "message": "Password reset successfully."
}
```

### **1.8 Check Email Availability**

```plaintext
POST http://localhost:8080/api/auth/check-email
Content-Type: application/json

{
  "email": "test@vitstudent.ac.in"
}
```

**Response:**

```json
{
  "available": true,
  "message": "Email is available"
}
```

### **1.9 Test Auth Endpoints**

```plaintext
GET http://localhost:8080/api/auth/test
```

**Response:**

```json
{
  "message": "Auth endpoints are working!"
}
```

---

## 🔒 **PROTECTED AUTHENTICATION ROUTES** (Require JWT Token)

### **1.10 Get Current User Profile**

```plaintext
GET http://localhost:8080/api/auth/me
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "id": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@vitstudent.ac.in",
  "phoneNumber": "+91-9876543210",
  "role": "USER",
  "isStudentVerified": true,
  "profilePhotoUrl": "/api/images/profile-photos/user-123.jpg",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastLoginAt": "2024-01-20T14:25:00Z"
}
```

### **1.11 Logout**

```plaintext
POST http://localhost:8080/api/auth/logout?refreshToken=your_refresh_token
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Logged out successfully."
}
```

### **1.12 Logout from All Devices**

```plaintext
POST http://localhost:8080/api/auth/logout-all
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Logged out from all devices successfully."
}
```

### **1.13 Change Password**

```plaintext
POST http://localhost:8080/api/auth/change-password
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

**Response:**

```json
{
  "message": "Password changed successfully."
}
```

### **1.14 Update Profile**

```plaintext
PUT http://localhost:8080/api/auth/profile
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "firstName": "John Updated",
  "lastName": "Doe Updated",
  "phoneNumber": "+91-9876543211",
  "bio": "Computer Science student at VIT"
}
```

**Response:**

```json
{
  "id": "user-123",
  "firstName": "John Updated",
  "lastName": "Doe Updated",
  "email": "john.doe@vitstudent.ac.in",
  "phoneNumber": "+91-9876543211",
  "bio": "Computer Science student at VIT",
  "role": "USER",
  "isStudentVerified": true,
  "profilePhotoUrl": "/api/images/profile-photos/user-123.jpg"
}
```

### **1.15 Update Profile Photo**

```plaintext
PUT http://localhost:8080/api/auth/profile-photo
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

"https://example.com/new-photo.jpg"
```

**Response:**

```json
{
  "id": "user-123",
  "firstName": "John",
  "lastName": "Doe",
  "profilePhotoUrl": "https://example.com/new-photo.jpg"
}
```

### **1.16 Get Active Sessions**

```plaintext
GET http://localhost:8080/api/auth/sessions
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
[
  {
    "id": "session-123",
    "deviceInfo": "Chrome on Windows",
    "ipAddress": "192.168.1.100",
    "location": "Vellore, India",
    "createdAt": "2024-01-20T10:00:00Z",
    "lastAccessedAt": "2024-01-20T14:30:00Z",
    "isCurrentSession": true
  }
]
```

### **1.17 Terminate Specific Session**

```plaintext
DELETE http://localhost:8080/api/auth/sessions/session-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Session terminated successfully."
}
```

### **1.18 Get Security Events**

```plaintext
GET http://localhost:8080/api/auth/security-events
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
[
  {
    "id": "event-123",
    "eventType": "LOGIN_SUCCESS",
    "description": "Successful login from Chrome",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "createdAt": "2024-01-20T10:00:00Z"
  }
]
```

### **1.19 Get Account Statistics**

```plaintext
GET http://localhost:8080/api/auth/account-stats
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "totalProducts": 5,
  "activeProducts": 3,
  "soldProducts": 2,
  "totalOrders": 8,
  "completedOrders": 6,
  "totalReviews": 12,
  "averageRating": 4.5,
  "accountAge": 45
}
```

### **1.20 Change Email**

```plaintext
POST http://localhost:8080/api/auth/change-email
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "newEmail": "john.new@vitstudent.ac.in",
  "password": "CurrentPassword123!"
}
```

**Response:**

```json
{
  "message": "Email change OTP sent to new email address."
}
```

### **1.21 Verify Email Change**

```plaintext
POST http://localhost:8080/api/auth/verify-email-change
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "email": "john.new@vitstudent.ac.in",
  "otp": "123456"
}
```

**Response:**

```json
{
  "message": "Email changed successfully."
}
```

### **1.22 Delete Account**

```plaintext
DELETE http://localhost:8080/api/auth/account
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "password": "CurrentPassword123!",
  "reason": "No longer needed"
}
```

**Response:**

```json
{
  "message": "Account deleted successfully."
}
```

---

## 🎓 **2. STUDENT VERIFICATION ROUTES**

### **2.1 Send Student Email OTP**

```plaintext
POST http://localhost:8080/api/student-verification/send-otp
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "studentEmail": "john.doe@vitstudent.ac.in"
}
```

**Response:**

```json
{
  "message": "OTP sent to your student email. Please check your inbox."
}
```

### **2.2 Verify Student Email OTP**

```plaintext
POST http://localhost:8080/api/student-verification/verify-otp
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "studentEmail": "john.doe@vitstudent.ac.in",
  "otp": "123456"
}
```

**Response:**

```json
{
  "message": "Student verification successful! You are now a verified VIT student."
}
```

---

## 📂 **3. CATEGORY ROUTES**

### **3.1 Get All Categories**

```plaintext
GET http://localhost:8080/api/categories
```

**Response:**

```json
[
  {
    "id": "electronics",
    "name": "Electronics",
    "description": "Electronic devices and gadgets",
    "parentId": null,
    "subcategories": [
      {
        "id": "smartphones",
        "name": "Smartphones",
        "description": "Mobile phones and accessories",
        "parentId": "electronics"
      },
      {
        "id": "laptops-computers",
        "name": "Laptops & Computers",
        "description": "Laptops, desktops, and computer accessories",
        "parentId": "electronics"
      }
    ]
  }
]
```

### **3.2 Get Main Categories Only**

```plaintext
GET http://localhost:8080/api/categories/main
```

**Response:**

```json
[
  {
    "id": "electronics",
    "name": "Electronics",
    "description": "Electronic devices and gadgets",
    "parentId": null
  },
  {
    "id": "books-study",
    "name": "Books & Study",
    "description": "Academic books and study materials",
    "parentId": null
  }
]
```

### **3.3 Get Specific Category**

```plaintext
GET http://localhost:8080/api/categories/electronics
```

**Response:**

```json
{
  "id": "electronics",
  "name": "Electronics",
  "description": "Electronic devices and gadgets",
  "parentId": null,
  "subcategories": [
    {
      "id": "smartphones",
      "name": "Smartphones",
      "description": "Mobile phones and accessories",
      "parentId": "electronics"
    }
  ]
}
```

### **3.4 Get Subcategories**

```plaintext
GET http://localhost:8080/api/categories/electronics/subcategories
```

**Response:**

```json
[
  {
    "id": "smartphones",
    "name": "Smartphones",
    "description": "Mobile phones and accessories",
    "parentId": "electronics"
  },
  {
    "id": "laptops-computers",
    "name": "Laptops & Computers",
    "description": "Laptops, desktops, and computer accessories",
    "parentId": "electronics"
  }
]
```

---

## 📦 **4. PRODUCT ROUTES**

### **4.1 Get All Products (Public)**

```plaintext
GET http://localhost:8080/api/products?page=0&size=20&sortBy=date_desc
```

**Response:**

```json
{
  "content": [
    {
      "id": "product-123",
      "title": "iPhone 13 Pro",
      "description": "Excellent condition iPhone with all accessories",
      "price": 65000.00,
      "originalPrice": 79900.00,
      "isNegotiable": true,
      "condition": "LIKE_NEW",
      "brand": "Apple",
      "model": "iPhone 13 Pro",
      "category": {
        "id": "smartphones",
        "name": "Smartphones"
      },
      "tags": ["iphone", "apple", "smartphone"],
      "pickupLocation": "VIT Main Campus",
      "deliveryAvailable": false,
      "status": "APPROVED",
      "isFeatured": false,
      "seller": {
        "id": "user-123",
        "firstName": "John",
        "lastName": "Doe",
        "isStudentVerified": true,
        "profilePhotoUrl": "/api/images/profile-photos/user-123.jpg"
      },
      "images": [
        {
          "id": "img-1",
          "url": "/api/images/products/iphone1.jpg",
          "isPrimary": true
        }
      ],
      "createdAt": "2024-01-20T10:00:00Z",
      "updatedAt": "2024-01-20T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### **4.2 Get Products by Category**

```plaintext
GET http://localhost:8080/api/products/category/smartphones?page=0&size=20&sortBy=price_asc
```

**Response:** Same structure as above, filtered by category

### **4.3 Search Products**

```plaintext
POST http://localhost:8080/api/products/search
Content-Type: application/json

{
  "keyword": "iPhone",
  "categoryId": "smartphones",
  "minPrice": 50000,
  "maxPrice": 80000,
  "condition": "LIKE_NEW",
  "location": "VIT",
  "sortBy": "price_asc",
  "page": 0,
  "size": 20
}
```

**Response:** Same structure as Get All Products

### **4.4 Get Featured Products**

```plaintext
GET http://localhost:8080/api/products/featured?page=0&size=20
```

**Response:** Same structure as Get All Products, only featured products

### **4.5 Get Specific Product**

```plaintext
GET http://localhost:8080/api/products/product-123
```

**Response:**

```json
{
  "id": "product-123",
  "title": "iPhone 13 Pro",
  "description": "Excellent condition iPhone with all accessories",
  "price": 65000.00,
  "originalPrice": 79900.00,
  "isNegotiable": true,
  "condition": "LIKE_NEW",
  "brand": "Apple",
  "model": "iPhone 13 Pro",
  "category": {
    "id": "smartphones",
    "name": "Smartphones"
  },
  "tags": ["iphone", "apple", "smartphone"],
  "pickupLocation": "VIT Main Campus",
  "deliveryAvailable": false,
  "status": "APPROVED",
  "isFeatured": false,
  "seller": {
    "id": "user-123",
    "firstName": "John",
    "lastName": "Doe",
    "isStudentVerified": true,
    "profilePhotoUrl": "/api/images/profile-photos/user-123.jpg"
  },
  "images": [
    {
      "id": "img-1",
      "url": "/api/images/products/iphone1.jpg",
      "isPrimary": true
    }
  ],
  "viewCount": 25,
  "favoriteCount": 3,
  "createdAt": "2024-01-20T10:00:00Z",
  "updatedAt": "2024-01-20T10:00:00Z"
}
```

---

## 🔒 **PROTECTED PRODUCT ROUTES** (Require JWT Token)

### **4.6 Create Product**

```plaintext
POST http://localhost:8080/api/products
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "title": "iPhone 13 Pro",
  "description": "Excellent condition iPhone 13 Pro with all accessories including charger, earphones, and original box. No scratches or dents.",
  "price": 65000.00,
  "originalPrice": 79900.00,
  "isNegotiable": true,
  "condition": "LIKE_NEW",
  "brand": "Apple",
  "model": "iPhone 13 Pro",
  "categoryId": "smartphones",
  "tags": ["iphone", "apple", "smartphone", "mobile"],
  "pickupLocation": "VIT Main Campus, Block A",
  "deliveryAvailable": false,
  "imageUrls": [
    "/api/images/products/iphone1.jpg",
    "/api/images/products/iphone2.jpg"
  ]
}
```

**Response:**

```json
{
  "id": "product-124",
  "title": "iPhone 13 Pro",
  "description": "Excellent condition iPhone 13 Pro with all accessories",
  "price": 65000.00,
  "status": "PENDING",
  "seller": {
    "id": "user-123",
    "firstName": "John",
    "lastName": "Doe"
  },
  "createdAt": "2024-01-20T15:00:00Z"
}
```

### **4.7 Get User's Products**

```plaintext
GET http://localhost:8080/api/products/my-products?page=0&size=20
Authorization: Bearer your_jwt_token_here
```

**Response:** Same structure as Get All Products, only user's products

### **4.8 Update Product**

```plaintext
PUT http://localhost:8080/api/products/product-123
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "title": "iPhone 13 Pro - Updated",
  "description": "Updated description",
  "price": 63000.00,
  "isNegotiable": false,
  "condition": "GOOD",
  "tags": ["iphone", "apple", "smartphone", "urgent-sale"]
}
```

**Response:** Updated product object

### **4.9 Delete Product**

```plaintext
DELETE http://localhost:8080/api/products/product-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Product deleted successfully"
}
```

---

## 🛒 **5. ORDER ROUTES** (All require JWT Token)

### **5.1 Create Order**

```plaintext
POST http://localhost:8080/api/orders
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "productId": "product-123",
  "offeredPrice": 62000.00,
  "deliveryMethod": "PICKUP",
  "deliveryAddress": "VIT Hostel Block B, Room 205",
  "message": "Hi, I'm interested in buying this iPhone. Can we meet tomorrow?"
}
```

**Response:**

```json
{
  "id": "order-123",
  "product": {
    "id": "product-123",
    "title": "iPhone 13 Pro",
    "price": 65000.00
  },
  "buyer": {
    "id": "user-456",
    "firstName": "Jane",
    "lastName": "Smith"
  },
  "seller": {
    "id": "user-123",
    "firstName": "John",
    "lastName": "Doe"
  },
  "offeredPrice": 62000.00,
  "finalPrice": null,
  "status": "PENDING",
  "deliveryMethod": "PICKUP",
  "deliveryAddress": "VIT Hostel Block B, Room 205",
  "message": "Hi, I'm interested in buying this iPhone. Can we meet tomorrow?",
  "createdAt": "2024-01-20T16:00:00Z"
}
```

### **5.2 Update Order Status**

```plaintext
PUT http://localhost:8080/api/orders/order-123
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "status": "ACCEPTED",
  "finalPrice": 63000.00,
  "message": "Accepted your offer. Let's meet at 3 PM tomorrow."
}
```

**Response:** Updated order object

### **5.3 Get Buyer Orders**

```plaintext
GET http://localhost:8080/api/orders/buyer?status=PENDING&page=0&size=20
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "content": [
    {
      "id": "order-123",
      "product": {
        "id": "product-123",
        "title": "iPhone 13 Pro",
        "price": 65000.00,
        "images": [
          {
            "url": "/api/images/products/iphone1.jpg",
            "isPrimary": true
          }
        ]
      },
      "seller": {
        "id": "user-123",
        "firstName": "John",
        "lastName": "Doe"
      },
      "offeredPrice": 62000.00,
      "finalPrice": 63000.00,
      "status": "ACCEPTED",
      "deliveryMethod": "PICKUP",
      "createdAt": "2024-01-20T16:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### **5.4 Get Seller Orders**

```plaintext
GET http://localhost:8080/api/orders/seller?status=COMPLETED&page=0&size=20
Authorization: Bearer your_jwt_token_here
```

**Response:** Same structure as buyer orders

### **5.5 Get Specific Order**

```plaintext
GET http://localhost:8080/api/orders/order-123
Authorization: Bearer your_jwt_token_here
```

**Response:** Complete order object with all details

### **5.6 Cancel Order**

```plaintext
DELETE http://localhost:8080/api/orders/order-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Order cancelled successfully"
}
```

---

## ⭐ **6. FAVORITES ROUTES** (All require JWT Token)

### **6.1 Add to Favorites**

```plaintext
POST http://localhost:8080/api/favorites/product-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Product added to favorites"
}
```

### **6.2 Remove from Favorites**

```plaintext
DELETE http://localhost:8080/api/favorites/product-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Product removed from favorites"
}
```

### **6.3 Get User Favorites**

```plaintext
GET http://localhost:8080/api/favorites?page=0&size=20
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "content": [
    {
      "id": "fav-123",
      "product": {
        "id": "product-123",
        "title": "iPhone 13 Pro",
        "price": 65000.00,
        "condition": "LIKE_NEW",
        "images": [
          {
            "url": "/api/images/products/iphone1.jpg",
            "isPrimary": true
          }
        ],
        "seller": {
          "id": "user-123",
          "firstName": "John",
          "lastName": "Doe"
        }
      },
      "addedAt": "2024-01-20T17:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### **6.4 Check if Product is in Favorites**

```plaintext
GET http://localhost:8080/api/favorites/check/product-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
true
```

---

## 💬 **7. MESSAGING ROUTES** (All require JWT Token)

### **7.1 Start Conversation**

```plaintext
POST http://localhost:8080/api/messages/conversations?otherUserId=user-123&productId=product-123
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "id": "conv-123",
  "otherUser": {
    "id": "user-123",
    "firstName": "John",
    "lastName": "Doe",
    "profilePhotoUrl": "/api/images/profile-photos/user-123.jpg"
  },
  "product": {
    "id": "product-123",
    "title": "iPhone 13 Pro",
    "price": 65000.00
  },
  "lastMessage": null,
  "unreadCount": 0,
  "createdAt": "2024-01-20T18:00:00Z",
  "updatedAt": "2024-01-20T18:00:00Z"
}
```

### **7.2 Get User Conversations**

```plaintext
GET http://localhost:8080/api/messages/conversations?page=0&size=20
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "content": [
    {
      "id": "conv-123",
      "otherUser": {
        "id": "user-123",
        "firstName": "John",
        "lastName": "Doe",
        "profilePhotoUrl": "/api/images/profile-photos/user-123.jpg"
      },
      "product": {
        "id": "product-123",
        "title": "iPhone 13 Pro",
        "price": 65000.00
      },
      "lastMessage": {
        "id": "msg-456",
        "content": "Is the phone still available?",
        "sentAt": "2024-01-20T18:30:00Z"
      },
      "unreadCount": 2,
      "updatedAt": "2024-01-20T18:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### **7.3 Send Message**

```plaintext
POST http://localhost:8080/api/messages/conversations/conv-123/messages
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "content": "Hi! Is this iPhone still available?",
  "messageType": "TEXT"
}
```

**Response:**

```json
{
  "id": "msg-789",
  "content": "Hi! Is this iPhone still available?",
  "messageType": "TEXT",
  "sender": {
    "id": "user-456",
    "firstName": "Jane",
    "lastName": "Smith"
  },
  "sentAt": "2024-01-20T19:00:00Z",
  "isRead": false
}
```

### **7.4 Get Conversation Messages**

```plaintext
GET http://localhost:8080/api/messages/conversations/conv-123/messages?page=0&size=50
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "content": [
    {
      "id": "msg-789",
      "content": "Hi! Is this iPhone still available?",
      "messageType": "TEXT",
      "sender": {
        "id": "user-456",
        "firstName": "Jane",
        "lastName": "Smith"
      },
      "sentAt": "2024-01-20T19:00:00Z",
      "isRead": true
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 1,
  "totalPages": 1
}
```

### **7.5 Mark Conversation as Read**

```plaintext
PUT http://localhost:8080/api/messages/conversations/conv-123/read
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Conversation marked as read"
}
```

### **7.6 Get Unread Message Count**

```plaintext
GET http://localhost:8080/api/messages/unread-count
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
5
```

---

## 🔔 **8. NOTIFICATION ROUTES** (All require JWT Token)

### **8.1 Get User Notifications**

```plaintext
GET http://localhost:8080/api/notifications?page=0&size=20&unreadOnly=false
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "content": [
    {
      "id": "notif-123",
      "type": "ORDER_RECEIVED",
      "title": "New Order Received",
      "message": "Jane Smith placed an order for your iPhone 13 Pro",
      "data": {
        "orderId": "order-123",
        "productId": "product-123"
      },
      "isRead": false,
      "createdAt": "2024-01-20T20:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### **8.2 Get Unread Notification Count**

```plaintext
GET http://localhost:8080/api/notifications/unread-count
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
3
```

### **8.3 Mark Notification as Read**

```plaintext
PUT http://localhost:8080/api/notifications/notif-123/read
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "Notification marked as read"
}
```

### **8.4 Mark All Notifications as Read**

```plaintext
PUT http://localhost:8080/api/notifications/mark-all-read
Authorization: Bearer your_jwt_token_here
```

**Response:**

```json
{
  "message": "All notifications marked as read"
}
```

---

## ⭐ **9. REVIEW ROUTES**

### **9.1 Create Product Review**

```plaintext
POST http://localhost:8080/api/reviews/products
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "productId": "product-123",
  "orderId": "order-123",
  "rating": 5,
  "comment": "Excellent product! Exactly as described. Fast delivery and great seller communication."
}
```

**Response:**

```json
{
  "id": "review-123",
  "product": {
    "id": "product-123",
    "title": "iPhone 13 Pro"
  },
  "reviewer": {
    "id": "user-456",
    "firstName": "Jane",
    "lastName": "Smith"
  },
  "rating": 5,
  "comment": "Excellent product! Exactly as described. Fast delivery and great seller communication.",
  "createdAt": "2024-01-20T21:00:00Z"
}
```

### **9.2 Get Product Reviews**

```plaintext
GET http://localhost:8080/api/reviews/products/product-123?page=0&size=10
```

**Response:**

```json
{
  "content": [
    {
      "id": "review-123",
      "reviewer": {
        "id": "user-456",
        "firstName": "Jane",
        "lastName": "Smith",
        "profilePhotoUrl": "/api/images/profile-photos/user-456.jpg"
      },
      "rating": 5,
      "comment": "Excellent product! Exactly as described.",
      "createdAt": "2024-01-20T21:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

### **9.3 Create User Review**

```plaintext
POST http://localhost:8080/api/reviews/users
Authorization: Bearer your_jwt_token_here
Content-Type: application/json

{
  "reviewedUserId": "user-123",
  "orderId": "order-123",
  "reviewType": "SELLER",
  "rating": 5,
  "comment": "Great seller! Very responsive and honest about product condition."
}
```

**Response:**

```json
{
  "id": "user-review-123",
  "reviewedUser": {
    "id": "user-123",
    "firstName": "John",
    "lastName": "Doe"
  },
  "reviewer": {
    "id": "user-456",
    "firstName": "Jane",
    "lastName": "Smith"
  },
  "reviewType": "SELLER",
  "rating": 5,
  "comment": "Great seller! Very responsive and honest about product condition.",
  "createdAt": "2024-01-20T21:30:00Z"
}
```

### **9.4 Get User Reviews**

```plaintext
GET http://localhost:8080/api/reviews/users/user-123?reviewType=SELLER&page=0&size=10
```

**Response:**

```json
{
  "content": [
    {
      "id": "user-review-123",
      "reviewer": {
        "id": "user-456",
        "firstName": "Jane",
        "lastName": "Smith",
        "profilePhotoUrl": "/api/images/profile-photos/user-456.jpg"
      },
      "reviewType": "SELLER",
      "rating": 5,
      "comment": "Great seller! Very responsive and honest about product condition.",
      "createdAt": "2024-01-20T21:30:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 🖼️ **10. IMAGE ROUTES**

### **10.1 Get Default Avatar**

```plaintext
GET http://localhost:8080/api/images/default-avatar.png
```

**Response:** Image file

### **10.2 Upload Profile Photo**

```plaintext
POST http://localhost:8080/api/images/upload-profile-photo
Authorization: Bearer your_jwt_token_here
Content-Type: multipart/form-data

[Form data with image file]
```

**Response:**

```json
{
  "url": "/api/images/profile-photos/user-123.jpg",
  "message": "Profile photo uploaded successfully"
}
```

### **10.3 Get Profile Photo**

```plaintext
GET http://localhost:8080/api/images/profile-photos/user-123.jpg
```

**Response:** Image file

---

## 👑 **11. ADMIN ROUTES** (Require ADMIN role)

### **11.1 Admin Dashboard**

```plaintext
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer admin_jwt_token_here
```

**Response:**

```json
{
  "totalUsers": 1250,
  "activeUsers": 890,
  "verifiedStudents": 750,
  "bannedUsers": 15,
  "totalProducts": 3500,
  "approvedProducts": 3200,
  "pendingProducts": 250,
  "rejectedProducts": 50,
  "featuredProducts": 100,
  "totalOrders": 2800,
  "completedOrders": 2400,
  "pendingOrders": 300,
  "cancelledOrders": 100,
  "totalReviews": 1800,
  "averageRating": 4.3,
  "recentActivity": [
    {
      "type": "USER_REGISTERED",
      "description": "New user John Doe registered",
      "timestamp": "2024-01-20T22:00:00Z"
    }
  ]
}
```

### **11.2 Get All Products (Admin)**

```plaintext
GET http://localhost:8080/api/admin/products?status=PENDING&page=0&size=20&sortBy=date_desc
Authorization: Bearer admin_jwt_token_here
```

**Response:** Same structure as regular products, but includes all statuses

### **11.3 Admin Update Product**

```plaintext
PUT http://localhost:8080/api/admin/products/product-123
Authorization: Bearer admin_jwt_token_here
Content-Type: application/json

{
  "status": "APPROVED",
  "isFeatured": true,
  "reason": "Product meets quality standards and guidelines",
  "adminNotes": "Approved after manual review. Featured due to high quality."
}
```

**Response:** Updated product object

### **11.4 Search Products (Admin)**

```plaintext
GET http://localhost:8080/api/admin/products/search?keyword=iPhone&page=0&size=20
Authorization: Bearer admin_jwt_token_here
```

**Response:** Paginated product results

### **11.5 Get All Users (Admin)**

```plaintext
GET http://localhost:8080/api/admin/users?filter=banned&page=0&size=20
Authorization: Bearer admin_jwt_token_here
```

**Response:**

```json
{
  "content": [
    {
      "id": "user-123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@vitstudent.ac.in",
      "role": "USER",
      "status": "ACTIVE",
      "isStudentVerified": true,
      "isBanned": false,
      "bannedUntil": null,
      "bannedReason": null,
      "createdAt": "2024-01-15T10:00:00Z",
      "lastLoginAt": "2024-01-20T14:30:00Z",
      "productCount": 5,
      "orderCount": 8,
      "reviewCount": 12
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### **11.6 Admin Update User**

```plaintext
PUT http://localhost:8080/api/admin/users/user-123
Authorization: Bearer admin_jwt_token_here
Content-Type: application/json

{
  "action": "BAN",
  "reason": "Spam account detected - posting fake products",
  "bannedUntil": "2024-12-31T23:59:59Z"
}
```

**Available actions:** `BAN`, `UNBAN`, `VERIFY_STUDENT`, `MARK_SPAM`, `DELETE_ACCOUNT`

**Response:** Updated user object

### **11.7 Search Users (Admin)**

```plaintext
GET http://localhost:8080/api/admin/users/search?keyword=john&page=0&size=20
Authorization: Bearer admin_jwt_token_here
```

**Response:** Paginated user results

---

## 🏠 **12. SYSTEM ROUTES**

### **12.1 Root Endpoint**

```plaintext
GET http://localhost:8080/
```

**Response:**

```json
{
  "message": "Welcome to DealHarbor API",
  "version": "1.0.0",
  "status": "running"
}
```

### **12.2 Health Check**

```plaintext
GET http://localhost:8080/health
```

**Response:**

```json
{
  "status": "UP",
  "timestamp": "2024-01-20T23:00:00Z"
}
```

### **12.3 Test Endpoints**

```plaintext
GET http://localhost:8080/api/test/public
```

**Response:**

```json
{
  "message": "Public test endpoint working!"
}
```

---

## 📋 **PREDEFINED CATEGORIES REFERENCE**

### **Main Categories & Subcategories:**

1. **Electronics** (`electronics`)

1. Smartphones (`smartphones`)
2. Laptops & Computers (`laptops-computers`)
3. Audio & Video (`audio-video`)
4. Gaming (`gaming`)
5. Tablets & E-readers (`tablets-ereaders`)
6. Electronics Accessories (`electronics-accessories`)



2. **Books & Study** (`books-study`)

1. Computer Science (`computer-science`)
2. Engineering (`engineering`)
3. Business & Management (`business-management`)
4. Literature & Languages (`literature-languages`)
5. Science & Math (`science-math`)
6. Notes & Guides (`notes-guides`)



3. **Fashion** (`fashion`)

1. Men's Clothing (`mens-clothing`)
2. Women's Clothing (`womens-clothing`)
3. Shoes (`shoes`)
4. Bags & Accessories (`bags-accessories`)
5. Watches & Jewelry (`watches-jewelry`)



4. **Furniture** (`furniture`)

1. Study Furniture (`study-furniture`)
2. Bedroom Furniture (`bedroom-furniture`)
3. Storage & Organization (`storage-organization`)
4. Home Decor (`home-decor`)



5. **Sports** (`sports`)

1. Fitness Equipment (`fitness-equipment`)
2. Outdoor Sports (`outdoor-sports`)
3. Indoor Games (`indoor-games`)
4. Sports Apparel (`sports-apparel`)



6. **Vehicles** (`vehicles`)

1. Bicycles (`bicycles`)
2. Scooters & Motorcycles (`scooters-motorcycles`)
3. Cars (`cars`)
4. Vehicle Accessories (`vehicle-accessories`)



7. **Services** (`services`)

1. Tutoring (`tutoring`)
2. Repair Services (`repair-services`)
3. Freelance Services (`freelance-services`)
4. Event Services (`event-services`)



8. **Others** (`others`)

1. Miscellaneous (`miscellaneous`)





---

## 🔑 **AUTHENTICATION FLOW**

1. **Register** → **Verify Email** → **Login** → **Get JWT Token**
2. **Use JWT Token** in `Authorization: Bearer <token>` header for protected routes
3. **Refresh Token** when JWT expires
4. **Student Verification** (optional) for verified badge


## 📊 **RESPONSE STATUS CODES**

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `500` - Internal Server Error