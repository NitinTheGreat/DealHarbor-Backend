# DealHarbor Backend API Test Script
# Run this after starting the backend server

$baseUrl = "http://localhost:8080"
$cookieFile = "test-cookies.txt"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "DealHarbor Backend API Test Script" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if server is running
Write-Host "Test 1: Checking if server is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/test/public" -Method GET -UseBasicParsing
    Write-Host "✅ Server is running! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Server is not running or not responding" -ForegroundColor Red
    Write-Host "Please start the server with: ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Test 2: Login
Write-Host "Test 2: Testing login endpoint..." -ForegroundColor Yellow
$loginBody = @{
    email = "test@dealharbor.com"
    password = "password123"
} | ConvertTo-Json

try {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -WebSession $session `
        -UseBasicParsing
    
    Write-Host "✅ Login successful! Status: $($response.StatusCode)" -ForegroundColor Green
    $loginData = $response.Content | ConvertFrom-Json
    Write-Host "User: $($loginData.name) ($($loginData.email))" -ForegroundColor Gray
    Write-Host "Session cookie received: $($session.Cookies.Count) cookies" -ForegroundColor Gray
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure you have a test user or register first" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Test 3: Get Categories
Write-Host "Test 3: Fetching categories (no auth needed)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/categories" -Method GET -UseBasicParsing
    $categories = $response.Content | ConvertFrom-Json
    Write-Host "✅ Categories fetched! Total: $($categories.Count)" -ForegroundColor Green
    Write-Host "Sample categories:" -ForegroundColor Gray
    $categories | Select-Object -First 3 | ForEach-Object {
        Write-Host "  - $($_.name) (ID: $($_.id))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Failed to fetch categories: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 4: Get Products
Write-Host "Test 4: Fetching products (no auth needed)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/products?page=0&size=5" -Method GET -UseBasicParsing
    $productsData = $response.Content | ConvertFrom-Json
    Write-Host "✅ Products fetched! Total: $($productsData.totalElements)" -ForegroundColor Green
    Write-Host "Current page: $($productsData.currentPage + 1) of $($productsData.totalPages)" -ForegroundColor Gray
    
    if ($productsData.content.Count -gt 0) {
        Write-Host "Sample products:" -ForegroundColor Gray
        $productsData.content | Select-Object -First 3 | ForEach-Object {
            Write-Host "  - $($_.title) - ₹$($_.price) [$($_.status)]" -ForegroundColor Gray
        }
    } else {
        Write-Host "⚠️  No products found. Products need admin approval to show up." -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Failed to fetch products: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Upload Product Image
Write-Host "Test 5: Testing product image upload (auth required)..." -ForegroundColor Yellow
Write-Host "Creating a test image..." -ForegroundColor Gray

# Create a simple test image file
$testImagePath = "test-image.jpg"
if (-not (Test-Path $testImagePath)) {
    # Create a 1x1 pixel red JPEG (minimal valid JPEG)
    $jpegBytes = [byte[]]@(
        0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
        0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43,
        0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09,
        0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12,
        0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20,
        0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29,
        0x2C, 0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32,
        0x3C, 0x2E, 0x33, 0x34, 0x32, 0xFF, 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01,
        0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF, 0xC4, 0x00, 0x14, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00,
        0x3F, 0x00, 0x7F, 0xFF, 0xD9
    )
    [System.IO.File]::WriteAllBytes($testImagePath, $jpegBytes)
}

try {
    $boundary = [System.Guid]::NewGuid().ToString()
    $fileBytes = [System.IO.File]::ReadAllBytes($testImagePath)
    $fileContent = [System.Text.Encoding]::GetEncoding('iso-8859-1').GetString($fileBytes)
    
    $bodyLines = @(
        "--$boundary",
        'Content-Disposition: form-data; name="file"; filename="test-image.jpg"',
        'Content-Type: image/jpeg',
        '',
        $fileContent,
        "--$boundary--"
    ) -join "`r`n"
    
    $response = Invoke-WebRequest -Uri "$baseUrl/api/images/upload-product" `
        -Method POST `
        -ContentType "multipart/form-data; boundary=$boundary" `
        -Body $bodyLines `
        -WebSession $session `
        -UseBasicParsing
    
    $imageUrl = $response.Content
    Write-Host "✅ Image uploaded successfully!" -ForegroundColor Green
    Write-Host "Image URL: $imageUrl" -ForegroundColor Gray
    
    # Save image URL for next test
    $global:uploadedImageUrl = $imageUrl
} catch {
    Write-Host "❌ Image upload failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Gray
}
Write-Host ""

# Test 6: Create Product
Write-Host "Test 6: Creating a test product (auth required)..." -ForegroundColor Yellow

$productBody = @{
    title = "Test Product - iPhone 13 Pro"
    description = "This is a test product created via API. Excellent condition iPhone 13 Pro with all accessories."
    price = 65000.00
    originalPrice = 119900.00
    isNegotiable = $true
    condition = "LIKE_NEW"
    brand = "Apple"
    model = "iPhone 13 Pro"
    categoryId = "electronics"
    tags = @("iphone", "smartphone", "test")
    pickupLocation = "VIT Campus"
    deliveryAvailable = $false
    imageUrls = if ($global:uploadedImageUrl) { @($global:uploadedImageUrl) } else { @() }
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/products" `
        -Method POST `
        -ContentType "application/json" `
        -Body $productBody `
        -WebSession $session `
        -UseBasicParsing
    
    $product = $response.Content | ConvertFrom-Json
    Write-Host "✅ Product created successfully!" -ForegroundColor Green
    Write-Host "Product ID: $($product.id)" -ForegroundColor Gray
    Write-Host "Title: $($product.title)" -ForegroundColor Gray
    Write-Host "Status: $($product.status) (Awaiting admin approval)" -ForegroundColor Gray
    Write-Host "Price: ₹$($product.price)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Product creation failed: $($_.Exception.Message)" -ForegroundColor Red
    $errorResponse = $_.Exception.Response
    if ($errorResponse) {
        $reader = New-Object System.IO.StreamReader($errorResponse.GetResponseStream())
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error details: $errorContent" -ForegroundColor Gray
    }
}
Write-Host ""

# Test 7: Get User's Products
Write-Host "Test 7: Fetching user's products (auth required)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/products/my-products?page=0&size=10" `
        -Method GET `
        -WebSession $session `
        -UseBasicParsing
    
    $myProducts = $response.Content | ConvertFrom-Json
    Write-Host "✅ Your products fetched! Total: $($myProducts.totalElements)" -ForegroundColor Green
    
    if ($myProducts.content.Count -gt 0) {
        Write-Host "Your products:" -ForegroundColor Gray
        $myProducts.content | ForEach-Object {
            Write-Host "  - $($_.title) - ₹$($_.price) [$($_.status)]" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "❌ Failed to fetch user products: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Cleanup
if (Test-Path $testImagePath) {
    Remove-Item $testImagePath -Force
}

# Summary
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Backend is working!" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Check FRONTEND_INTEGRATION_GUIDE.md for Next.js integration" -ForegroundColor White
Write-Host "2. Products need admin approval to appear in public listings" -ForegroundColor White
Write-Host "3. Use the provided React components for your frontend" -ForegroundColor White
Write-Host ""
Write-Host "Important URLs:" -ForegroundColor Yellow
Write-Host "- API Base: http://localhost:8080/api" -ForegroundColor White
Write-Host "- Products: http://localhost:8080/api/products" -ForegroundColor White
Write-Host "- Categories: http://localhost:8080/api/categories" -ForegroundColor White
Write-Host "- Docs: See endpoints.txt for all available endpoints" -ForegroundColor White
Write-Host ""
