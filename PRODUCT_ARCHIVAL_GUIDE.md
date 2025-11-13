# 📦 Product Archival System - Frontend Integration Guide

## Overview

The Product Archival System automatically manages product lifecycle:
- **Sold Products**: User manually marks products as sold → moved to `sold_products` table
- **Unsold Products**: Products older than 6 months → automatically archived to `unsold_products` table
- **Daily Cron Job**: Runs at 2 AM daily to archive expired products

---

## 🔑 Key Features

1. ✅ **Manual Sold Marking** - Sellers can mark their products as sold
2. ⏰ **Auto-Archival** - Products older than 6 months are automatically archived
3. 📊 **Statistics** - Track sold/unsold products and revenue
4. 📜 **History** - View all archived products with details
5. 🔒 **Secure** - Only product owners can mark products as sold

---

## 📡 API Endpoints

### Base URL: `/api/products/archived`

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/mark-sold/{productId}` | POST | ✅ | Mark product as sold |
| `/sold` | GET | ✅ | Get sold products (paginated) |
| `/sold/all` | GET | ✅ | Get all sold products |
| `/unsold` | GET | ✅ | Get unsold products (paginated) |
| `/unsold/all` | GET | ✅ | Get all unsold products |
| `/stats` | GET | ✅ | Get archival statistics |
| `/sold/{productId}` | GET | ✅ | Get specific sold product |
| `/unsold/{productId}` | GET | ✅ | Get specific unsold product |

---

## 🎯 Use Cases

### 1️⃣ Mark Product as Sold

**Endpoint:** `POST /api/products/archived/mark-sold/{productId}`

**Request Body (Optional):**
```json
{
  "buyerId": "buyer-user-id-123",
  "soldPrice": 45000.00
}
```

**Response:**
```json
{
  "message": "Product marked as sold successfully",
  "soldProduct": {
    "id": "product-123",
    "title": "iPhone 13 Pro",
    "price": 50000.00,
    "soldPrice": 45000.00,
    "buyerId": "buyer-user-id-123",
    "buyerName": "John Buyer",
    "soldAt": "2025-11-13T10:30:00Z",
    "archivedAt": "2025-11-13T10:30:00Z"
  }
}
```

**Next.js Example:**
```tsx
'use client';
import { useState } from 'react';

interface MarkSoldRequest {
  buyerId?: string;
  soldPrice?: number;
}

export function MarkAsSoldButton({ productId }: { productId: string }) {
  const [loading, setLoading] = useState(false);

  async function markAsSold() {
    if (!confirm('Mark this product as sold?')) return;

    setLoading(true);
    
    try {
      const response = await fetch(
        `http://localhost:8080/api/products/archived/mark-sold/${productId}`,
        {
          method: 'POST',
          credentials: 'include', // Include session cookie
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            // Optional: include buyer ID if you track buyers
            // buyerId: 'buyer-123',
            // soldPrice: 45000.00
          }),
        }
      );

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Failed to mark as sold');
      }

      const data = await response.json();
      alert('✅ ' + data.message);
      
      // Redirect to sold products page or refresh
      window.location.href = '/profile/sold-products';
      
    } catch (error: any) {
      alert('❌ Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={markAsSold}
      disabled={loading}
      className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 disabled:bg-gray-400"
    >
      {loading ? 'Marking as Sold...' : '✓ Mark as Sold'}
    </button>
  );
}
```

---

### 2️⃣ Display Sold Products in Profile

**Endpoint:** `GET /api/products/archived/sold?page=0&size=20`

**Response:**
```json
{
  "content": [
    {
      "id": "product-123",
      "title": "iPhone 13 Pro",
      "description": "Excellent condition",
      "price": 50000.00,
      "soldPrice": 45000.00,
      "condition": "LIKE_NEW",
      "categoryName": "Smartphones",
      "sellerName": "John Seller",
      "buyerId": "buyer-123",
      "buyerName": "Jane Buyer",
      "primaryImageUrl": "/api/images/products/iphone.jpg",
      "viewCount": 125,
      "favoriteCount": 15,
      "createdAt": "2025-05-01T10:00:00Z",
      "soldAt": "2025-11-13T10:30:00Z",
      "archivedAt": "2025-11-13T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 5,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Next.js Example - Sold Products Page:**
```tsx
'use client';
import { useEffect, useState } from 'react';
import Image from 'next/image';

interface SoldProduct {
  id: string;
  title: string;
  price: number;
  soldPrice: number;
  condition: string;
  buyerName?: string;
  primaryImageUrl?: string;
  soldAt: string;
}

export default function SoldProductsPage() {
  const [soldProducts, setSoldProducts] = useState<SoldProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchSoldProducts();
  }, [page]);

  async function fetchSoldProducts() {
    try {
      const response = await fetch(
        `http://localhost:8080/api/products/archived/sold?page=${page}&size=20`,
        {
          credentials: 'include',
        }
      );

      if (!response.ok) throw new Error('Failed to fetch sold products');

      const data = await response.json();
      setSoldProducts(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Error fetching sold products:', error);
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return <div className="text-center py-12">Loading sold products...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">My Sold Products</h1>

      {soldProducts.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500">No sold products yet</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {soldProducts.map((product) => (
              <div key={product.id} className="border rounded-lg overflow-hidden">
                {/* Image */}
                <div className="relative h-48 bg-gray-100">
                  {product.primaryImageUrl ? (
                    <Image
                      src={`http://localhost:8080${product.primaryImageUrl}`}
                      alt={product.title}
                      fill
                      className="object-cover"
                    />
                  ) : (
                    <div className="h-full flex items-center justify-center">
                      No Image
                    </div>
                  )}
                  
                  <div className="absolute top-2 right-2 bg-green-500 text-white text-xs font-bold px-3 py-1 rounded-full">
                    SOLD
                  </div>
                </div>

                {/* Details */}
                <div className="p-4">
                  <h3 className="font-semibold text-lg mb-2 line-clamp-1">
                    {product.title}
                  </h3>

                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600">Listed Price:</span>
                      <span className="line-through text-gray-400">
                        ₹{product.price.toLocaleString('en-IN')}
                      </span>
                    </div>
                    
                    <div className="flex justify-between">
                      <span className="text-gray-600">Sold For:</span>
                      <span className="font-bold text-green-600">
                        ₹{product.soldPrice.toLocaleString('en-IN')}
                      </span>
                    </div>

                    {product.buyerName && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Buyer:</span>
                        <span>{product.buyerName}</span>
                      </div>
                    )}

                    <div className="flex justify-between">
                      <span className="text-gray-600">Sold On:</span>
                      <span>{new Date(product.soldAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
              >
                Previous
              </button>
              
              <span className="px-4 py-2">
                Page {page + 1} of {totalPages}
              </span>
              
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= totalPages - 1}
                className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
```

---

### 3️⃣ Display Unsold (Expired) Products

**Endpoint:** `GET /api/products/archived/unsold?page=0&size=20`

**Response:**
```json
{
  "content": [
    {
      "id": "product-456",
      "title": "Old Laptop",
      "price": 20000.00,
      "condition": "GOOD",
      "categoryName": "Laptops",
      "primaryImageUrl": "/api/images/products/laptop.jpg",
      "createdAt": "2025-04-01T10:00:00Z",
      "expiredAt": "2025-10-01T10:00:00Z",
      "archivedAt": "2025-10-01T02:00:00Z",
      "archivalReason": "Product expired after 6 months of inactivity"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

**Next.js Example:**
```tsx
'use client';
import { useEffect, useState } from 'react';

interface UnsoldProduct {
  id: string;
  title: string;
  price: number;
  expiredAt: string;
  archivalReason: string;
}

export function UnsoldProductsList() {
  const [unsoldProducts, setUnsoldProducts] = useState<UnsoldProduct[]>([]);

  useEffect(() => {
    fetchUnsoldProducts();
  }, []);

  async function fetchUnsoldProducts() {
    try {
      const response = await fetch(
        'http://localhost:8080/api/products/archived/unsold/all',
        { credentials: 'include' }
      );

      if (!response.ok) throw new Error('Failed to fetch');

      const data = await response.json();
      setUnsoldProducts(data);
    } catch (error) {
      console.error('Error:', error);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">Expired Products</h2>
      
      {unsoldProducts.length === 0 ? (
        <p className="text-gray-500">No expired products</p>
      ) : (
        <div className="space-y-4">
          {unsoldProducts.map((product) => (
            <div key={product.id} className="border rounded-lg p-4">
              <h3 className="font-semibold">{product.title}</h3>
              <p className="text-sm text-gray-600">
                Listed at: ₹{product.price.toLocaleString('en-IN')}
              </p>
              <p className="text-sm text-red-600">
                Expired: {new Date(product.expiredAt).toLocaleDateString()}
              </p>
              <p className="text-xs text-gray-500 mt-2">
                {product.archivalReason}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

---

### 4️⃣ Display Statistics

**Endpoint:** `GET /api/products/archived/stats`

**Response:**
```json
{
  "totalSold": 12,
  "totalUnsold": 3,
  "totalRevenue": 540000.00
}
```

**Next.js Example:**
```tsx
'use client';
import { useEffect, useState } from 'react';

interface Stats {
  totalSold: number;
  totalUnsold: number;
  totalRevenue: number;
}

export function ArchivalStatsCard() {
  const [stats, setStats] = useState<Stats | null>(null);

  useEffect(() => {
    fetchStats();
  }, []);

  async function fetchStats() {
    try {
      const response = await fetch(
        'http://localhost:8080/api/products/archived/stats',
        { credentials: 'include' }
      );

      if (!response.ok) throw new Error('Failed to fetch stats');

      const data = await response.json();
      setStats(data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  }

  if (!stats) return <div>Loading...</div>;

  return (
    <div className="grid grid-cols-3 gap-4">
      {/* Total Sold */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-6">
        <div className="text-3xl font-bold text-green-600">
          {stats.totalSold}
        </div>
        <div className="text-sm text-gray-600 mt-1">Products Sold</div>
      </div>

      {/* Total Revenue */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div className="text-3xl font-bold text-blue-600">
          ₹{stats.totalRevenue.toLocaleString('en-IN')}
        </div>
        <div className="text-sm text-gray-600 mt-1">Total Revenue</div>
      </div>

      {/* Expired Products */}
      <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
        <div className="text-3xl font-bold text-orange-600">
          {stats.totalUnsold}
        </div>
        <div className="text-sm text-gray-600 mt-1">Expired Products</div>
      </div>
    </div>
  );
}
```

---

### 5️⃣ Complete Profile Page with Tabs

**Next.js Example - Profile Dashboard:**
```tsx
'use client';
import { useState } from 'react';
import { ArchivalStatsCard } from '@/components/ArchivalStatsCard';
import SoldProductsPage from '@/components/SoldProductsPage';
import { UnsoldProductsList } from '@/components/UnsoldProductsList';

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState<'active' | 'sold' | 'expired'>('active');

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">My Products</h1>

      {/* Statistics */}
      <div className="mb-8">
        <ArchivalStatsCard />
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="flex space-x-8">
          <button
            onClick={() => setActiveTab('active')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'active'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Active Listings
          </button>
          
          <button
            onClick={() => setActiveTab('sold')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'sold'
                ? 'border-green-500 text-green-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Sold Products
          </button>
          
          <button
            onClick={() => setActiveTab('expired')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'expired'
                ? 'border-orange-500 text-orange-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Expired Products
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      <div>
        {activeTab === 'active' && (
          <div>
            {/* Your existing active products list */}
            <ActiveProductsList />
          </div>
        )}

        {activeTab === 'sold' && <SoldProductsPage />}

        {activeTab === 'expired' && <UnsoldProductsList />}
      </div>
    </div>
  );
}

function ActiveProductsList() {
  // Your existing implementation for active products
  return <div>Active products go here...</div>;
}
```

---

## 🔄 Product Lifecycle Flow

```
┌─────────────────┐
│  Create Product │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ PENDING (Admin  │
│   Approval)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    APPROVED     │ ◄─────────────────┐
│  (Active Sale)  │                   │
└────────┬────────┘                   │
         │                            │
         │                            │
    ┌────┴────────┐                  │
    │             │                   │
    ▼             ▼                   │
┌─────────┐  ┌──────────┐           │
│ Seller  │  │  6 Months│           │
│  Marks  │  │  Passes  │           │
│ As Sold │  │ (Cron Job│           │
└────┬────┘  └─────┬────┘           │
     │             │                 │
     ▼             ▼                 │
┌─────────┐  ┌──────────┐           │
│  SOLD   │  │  UNSOLD  │           │
│ Products│  │ Products │           │
│  Table  │  │  Table   │           │
└─────────┘  └──────────┘           │
                                     │
    Both permanently archived        │
    (cannot be restored)             │
```

---

## ⚙️ Backend Automation

### Automatic Archival (Cron Job)

The backend runs a scheduled task **daily at 2:00 AM** that:

1. Finds all products with `status = APPROVED` 
2. Created more than **180 days (6 months)** ago
3. Moves them to `unsold_products` table
4. Deletes from `products` table

**Configuration (Already Set Up):**
```java
@Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
public void archiveExpiredProducts() {
    // Automatically archives products older than 6 months
}
```

**To Test Manually (For Development):**

You can trigger archival manually by calling the service directly in a test endpoint, or wait for the scheduled time.

---

## 🧪 Testing the API

### 1. Mark Product as Sold
```bash
# Login first
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@dealharbor.com","password":"password123"}' \
  -c cookies.txt

# Mark product as sold
curl -X POST http://localhost:8080/api/products/archived/mark-sold/product-123 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "soldPrice": 45000.00
  }'
```

### 2. Get Sold Products
```bash
curl -b cookies.txt \
  "http://localhost:8080/api/products/archived/sold?page=0&size=10"
```

### 3. Get Statistics
```bash
curl -b cookies.txt \
  "http://localhost:8080/api/products/archived/stats"
```

### 4. Get Unsold Products
```bash
curl -b cookies.txt \
  "http://localhost:8080/api/products/archived/unsold"
```

---

## 📊 Database Schema

### `sold_products` Table
```sql
CREATE TABLE sold_products (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    sold_price DECIMAL(10,2),
    seller_id VARCHAR(255) NOT NULL,
    buyer_id VARCHAR(255),
    sold_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NOT NULL,
    -- ... other fields
);
```

### `unsold_products` Table
```sql
CREATE TABLE unsold_products (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    seller_id VARCHAR(255) NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NOT NULL,
    archival_reason TEXT,
    -- ... other fields
);
```

---

## ⚠️ Important Notes

1. **Irreversible**: Once a product is archived (sold/unsold), it **cannot be restored** to active listings
2. **Ownership**: Only the product owner can mark their product as sold
3. **Status Check**: Only APPROVED products can be marked as sold
4. **Images**: Images are stored as JSON strings in archived tables
5. **Auto-Archival**: Runs daily at 2 AM server time
6. **Session Required**: All endpoints require authentication

---

## 🎯 UI/UX Recommendations

### 1. Product Card with "Mark as Sold" Button
```tsx
<div className="product-card">
  <img src={product.image} alt={product.title} />
  <h3>{product.title}</h3>
  <p>₹{product.price}</p>
  
  {/* Only show for owner */}
  {isOwner && (
    <button onClick={() => markAsSold(product.id)}>
      ✓ Mark as Sold
    </button>
  )}
</div>
```

### 2. Confirmation Dialog
```tsx
if (!confirm('Are you sure you want to mark this product as sold? This action cannot be undone.')) {
  return;
}
```

### 3. Success Feedback
```tsx
toast.success('Product marked as sold! Check your "Sold Products" tab.');
```

### 4. Revenue Display
```tsx
<div className="revenue-card">
  <h3>Total Earnings</h3>
  <p className="text-3xl font-bold">
    ₹{stats.totalRevenue.toLocaleString('en-IN')}
  </p>
  <p className="text-sm text-gray-500">
    From {stats.totalSold} sold products
  </p>
</div>
```

---

## 🚀 Quick Start Checklist

- [ ] **Backend**: Already implemented ✅
- [ ] **Start Backend**: `./mvnw spring-boot:run`
- [ ] **Create Profile Page**: Add tabs for Active/Sold/Expired
- [ ] **Add Mark as Sold Button**: On each active product
- [ ] **Display Statistics**: Show sold count and revenue
- [ ] **Test Manually**: Mark a product as sold
- [ ] **Wait for Cron**: Products older than 6 months will auto-archive

---

## 📞 Summary

You now have:
1. ✅ Backend routes for marking products as sold
2. ✅ Automatic archival of products older than 6 months
3. ✅ Separate tables for sold and unsold products
4. ✅ Statistics API for revenue tracking
5. ✅ Complete REST API documentation
6. ✅ Frontend integration examples with Next.js

**Your archival system is fully functional! 🎉**
