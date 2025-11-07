# 🧪 Manual Testing Guide

## Quick Testing Steps

### Prerequisites
- ✅ Backend server is running: `./mvnw spring-boot:run`
- ✅ Redis is running: `redis-server` (for sessions)
- ✅ Database is connected (PostgreSQL)

---

## Method 1: Using PowerShell Script (Automated)

Run the automated test script:

```powershell
.\test-api.ps1
```

This will test all endpoints automatically and show you results.

---

## Method 2: Using curl Commands (Manual)

### Step 1: Check Server is Running

```bash
curl http://localhost:8080/api/test/public
```

**Expected:** "Public endpoint is working!"

---

### Step 2: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@dealharbor.com\",\"password\":\"password123\"}" \
  -c cookies.txt -v
```

**Expected:** 
- Status: 200 OK
- Response: User details JSON
- Cookie: JSESSIONID saved to cookies.txt

**If you don't have a test user, register first:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"name\":\"Test User\",
    \"email\":\"test@dealharbor.com\",
    \"password\":\"password123\",
    \"phoneNumber\":\"1234567890\",
    \"institutionName\":\"VIT\"
  }"
```

---

### Step 3: Get Categories (No Auth Needed)

```bash
curl http://localhost:8080/api/categories
```

**Expected:** Array of category objects

---

### Step 4: Get Products (No Auth Needed)

```bash
curl "http://localhost:8080/api/products?page=0&size=10&sortBy=date_desc"
```

**Expected:** 
```json
{
  "content": [...],
  "totalElements": 0,
  "totalPages": 0,
  "currentPage": 0
}
```

**Note:** If empty, it means no APPROVED products exist yet.

---

### Step 5: Upload Product Image (Auth Required)

```bash
# Create or use an existing image file
curl -X POST http://localhost:8080/api/images/upload-product \
  -F "file=@/path/to/your/image.jpg" \
  -b cookies.txt
```

**Expected:** `/api/images/products/1234567890_image.jpg`

**Save this URL for the next step!**

---

### Step 6: Create Product (Auth Required)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "iPhone 13 Pro 128GB - Pacific Blue",
    "description": "Excellent condition iPhone with all accessories",
    "price": 65000.00,
    "originalPrice": 119900.00,
    "isNegotiable": true,
    "condition": "LIKE_NEW",
    "brand": "Apple",
    "model": "iPhone 13 Pro",
    "categoryId": "electronics",
    "tags": ["iphone", "smartphone", "128gb"],
    "pickupLocation": "VIT Campus",
    "deliveryAvailable": false,
    "imageUrls": ["/api/images/products/1234567890_image.jpg"]
  }'
```

**Expected:** 
- Status: 200 OK
- Response: Product object with `"status": "PENDING"`

---

### Step 7: Get Your Products (Auth Required)

```bash
curl http://localhost:8080/api/products/my-products \
  -b cookies.txt
```

**Expected:** Your products list (including PENDING ones)

---

### Step 8: Get Product by ID

```bash
curl http://localhost:8080/api/products/{PRODUCT_ID}
```

Replace `{PRODUCT_ID}` with actual ID from previous step.

---

## Method 3: Using Postman

### 1. Import Collection

Create a new Postman collection with these requests:

#### Request 1: Login
- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "email": "test@dealharbor.com",
    "password": "password123"
  }
  ```
- **Tests Tab:** Add this to auto-save cookies:
  ```javascript
  pm.test("Login successful", function () {
      pm.response.to.have.status(200);
  });
  ```

#### Request 2: Get Products
- **Method:** GET
- **URL:** `http://localhost:8080/api/products?page=0&size=10`
- **Headers:** None needed

#### Request 3: Upload Image
- **Method:** POST
- **URL:** `http://localhost:8080/api/images/upload-product`
- **Body:** form-data
  - Key: `file` (type: File)
  - Value: Select an image file
- **Note:** Postman automatically sends cookies from login

#### Request 4: Create Product
- **Method:** POST
- **URL:** `http://localhost:8080/api/products`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "title": "Test Product",
    "description": "Test description",
    "price": 5000,
    "condition": "LIKE_NEW",
    "categoryId": "electronics",
    "imageUrls": ["/api/images/products/123_image.jpg"]
  }
  ```

---

## Method 4: Using Browser Developer Tools

### 1. Open Browser Console

Press F12 or Ctrl+Shift+I

### 2. Test Login

```javascript
// Login
fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  credentials: 'include',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'test@dealharbor.com',
    password: 'password123'
  })
})
.then(r => r.json())
.then(data => console.log('Login success:', data))
.catch(err => console.error('Login failed:', err));
```

### 3. Test Get Products

```javascript
// Get products
fetch('http://localhost:8080/api/products?page=0&size=10', {
  credentials: 'include'
})
.then(r => r.json())
.then(data => console.log('Products:', data))
.catch(err => console.error('Failed:', err));
```

### 4. Test Image Upload

```javascript
// Upload image (first select file using <input type="file">)
const input = document.createElement('input');
input.type = 'file';
input.accept = 'image/*';
input.onchange = async (e) => {
  const file = e.target.files[0];
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch('http://localhost:8080/api/images/upload-product', {
    method: 'POST',
    credentials: 'include',
    body: formData
  });
  
  const imageUrl = await response.text();
  console.log('Image uploaded:', imageUrl);
};
input.click();
```

### 5. Test Create Product

```javascript
// Create product
fetch('http://localhost:8080/api/products', {
  method: 'POST',
  credentials: 'include',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    title: 'Test Product',
    description: 'Test description',
    price: 5000,
    condition: 'LIKE_NEW',
    categoryId: 'electronics'
  })
})
.then(r => r.json())
.then(data => console.log('Product created:', data))
.catch(err => console.error('Failed:', err));
```

---

## ✅ What to Check

### 1. Server Running
- Terminal shows: "Started DealharborBackendApplication"
- No errors in console

### 2. Login Works
- Status: 200 OK
- Cookie received: JSESSIONID
- User details returned

### 3. Image Upload Works
- Status: 200 OK
- Returns: `/api/images/products/{timestamp}_{filename}`
- File saved in: `uploads/products/`

### 4. Product Creation Works
- Status: 200 OK
- Product status: PENDING
- Product ID returned

### 5. Product Fetching Works
- Status: 200 OK
- Returns: Empty or products array
- **Note:** Only APPROVED products show in public listing

---

## 🐛 Troubleshooting

### Issue: 401 Unauthorized
**Cause:** Not logged in or session expired

**Fix:**
1. Login first: `POST /api/auth/login`
2. Ensure cookies are sent: Use `-b cookies.txt` or `credentials: 'include'`

### Issue: No products returned
**Cause:** All products are PENDING (need admin approval)

**Fix:**
- Use admin account to approve products
- Or check your own products: `GET /api/products/my-products`

### Issue: CORS error in browser
**Cause:** Already fixed, but browser might cache

**Fix:**
- Clear browser cache
- Restart backend server
- Ensure frontend is on `http://localhost:3000`

### Issue: Image upload fails with 401
**Cause:** Not authenticated

**Fix:**
- Login first
- Make sure session cookie is sent with request

### Issue: Cannot change session ID error
**Cause:** Already fixed in AuthController

**Fix:**
- Already applied! Session is created before changeSessionId()

---

## 📊 Expected Test Results

### ✅ Successful Test Output

```
Test 1: Server Running ✅
Test 2: Login ✅ (User: Test User)
Test 3: Categories ✅ (Found 10 categories)
Test 4: Products ✅ (Total: 0 - No approved products yet)
Test 5: Image Upload ✅ (URL: /api/images/products/123_image.jpg)
Test 6: Create Product ✅ (Status: PENDING)
Test 7: My Products ✅ (Total: 1)
```

### 🎯 Ready for Frontend Integration!

Once all tests pass, you can:
1. Follow `FRONTEND_INTEGRATION_GUIDE.md` for Next.js integration
2. Use the React components provided
3. Start building your frontend UI

---

## 📞 Quick Commands Reference

```bash
# Start backend
./mvnw spring-boot:run

# Start Redis
redis-server

# Run automated tests
.\test-api.ps1

# Test specific endpoint
curl http://localhost:8080/api/products

# Login and save cookies
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@dealharbor.com","password":"password123"}' \
  -c cookies.txt

# Create product with cookies
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{...product data...}'
```

---

**Happy Testing! 🚀**
