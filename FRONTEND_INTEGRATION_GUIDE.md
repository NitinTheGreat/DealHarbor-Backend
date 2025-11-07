# 🎨 Frontend Integration Guide - Next.js 15

## Complete Guide for Integrating DealHarbor Backend with Next.js Frontend

---

## 🔧 Backend Fixes Applied

### ✅ **Fixed Issues:**
1. **Added Product Image Upload Endpoint** - `POST /api/images/upload-product`
2. **Updated CORS Configuration** - All controllers now properly allow credentials from localhost:3000
3. **Product Fetching Verified** - getAllProducts works correctly (shows only APPROVED products)

### 🔄 **Updated Controllers:**
- ✅ ImageController - Added product image upload/retrieval
- ✅ ProductController - Fixed CORS for session auth
- ✅ CategoryController - Fixed CORS for session auth
- ✅ AuthController - Already configured (from previous fix)

---

## 📸 Image Upload Integration

### Endpoint: `POST /api/images/upload-product`

**Features:**
- Max file size: 5MB
- Allowed types: All image formats (jpg, png, webp, etc.)
- Returns: Image URL path
- Authentication: Required (session cookie)

### Next.js 15 Example - Multiple Images:

```tsx
'use client';
import { useState } from 'react';

interface ImageUploaderProps {
  onImagesUploaded: (urls: string[]) => void;
  maxImages?: number;
}

export function ProductImageUploader({ 
  onImagesUploaded, 
  maxImages = 5 
}: ImageUploaderProps) {
  const [uploading, setUploading] = useState(false);
  const [uploadedUrls, setUploadedUrls] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function handleFileUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    // Check if adding these files exceeds max limit
    if (uploadedUrls.length + files.length > maxImages) {
      setError(`Maximum ${maxImages} images allowed`);
      return;
    }

    setUploading(true);
    setError(null);
    const newUrls: string[] = [];

    try {
      // Upload each file one by one
      for (const file of Array.from(files)) {
        // Validate file size (5MB max)
        if (file.size > 5 * 1024 * 1024) {
          throw new Error(`${file.name} exceeds 5MB limit`);
        }

        // Validate file type
        if (!file.type.startsWith('image/')) {
          throw new Error(`${file.name} is not an image file`);
        }

        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(
          'http://localhost:8080/api/images/upload-product',
          {
            method: 'POST',
            credentials: 'include', // IMPORTANT: Send session cookie
            body: formData, // Don't set Content-Type header - browser handles it
          }
        );

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || 'Upload failed');
        }

        const imageUrl = await response.text(); // Backend returns URL as plain text
        newUrls.push(imageUrl);
      }

      const allUrls = [...uploadedUrls, ...newUrls];
      setUploadedUrls(allUrls);
      onImagesUploaded(allUrls);
      
    } catch (error: any) {
      setError(error.message);
    } finally {
      setUploading(false);
      // Reset input
      e.target.value = '';
    }
  }

  function removeImage(index: number) {
    const newUrls = uploadedUrls.filter((_, i) => i !== index);
    setUploadedUrls(newUrls);
    onImagesUploaded(newUrls);
  }

  function moveImage(fromIndex: number, toIndex: number) {
    const newUrls = [...uploadedUrls];
    const [removed] = newUrls.splice(fromIndex, 1);
    newUrls.splice(toIndex, 0, removed);
    setUploadedUrls(newUrls);
    onImagesUploaded(newUrls);
  }

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium mb-2">
          Product Images ({uploadedUrls.length}/{maxImages})
          <span className="text-gray-500 ml-2">First image will be the main thumbnail</span>
        </label>

        <input
          type="file"
          accept="image/*"
          multiple
          onChange={handleFileUpload}
          disabled={uploading || uploadedUrls.length >= maxImages}
          className="block w-full text-sm text-gray-500
            file:mr-4 file:py-2 file:px-4
            file:rounded-md file:border-0
            file:text-sm file:font-semibold
            file:bg-blue-50 file:text-blue-700
            hover:file:bg-blue-100
            disabled:opacity-50 disabled:cursor-not-allowed"
        />
        
        <p className="text-xs text-gray-500 mt-1">
          Max 5MB per image. Supported formats: JPG, PNG, WebP, etc.
        </p>
      </div>

      {uploading && (
        <div className="text-blue-600 text-sm">
          Uploading images... Please wait.
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {uploadedUrls.length > 0 && (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
          {uploadedUrls.map((url, index) => (
            <div key={index} className="relative group">
              <img
                src={`http://localhost:8080${url}`}
                alt={`Product ${index + 1}`}
                className="w-full h-32 object-cover rounded-lg border-2 border-gray-200"
              />
              
              {/* Primary badge */}
              {index === 0 && (
                <div className="absolute top-2 left-2 bg-blue-600 text-white text-xs px-2 py-1 rounded">
                  Main
                </div>
              )}

              {/* Action buttons */}
              <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-40 transition-all rounded-lg flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100">
                <button
                  onClick={() => removeImage(index)}
                  className="bg-red-500 text-white p-2 rounded-full hover:bg-red-600"
                  title="Remove"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
                
                {index > 0 && (
                  <button
                    onClick={() => moveImage(index, 0)}
                    className="bg-blue-500 text-white p-2 rounded-full hover:bg-blue-600"
                    title="Set as main"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

---

## 🛍️ Product Listing Integration

### Endpoint: `GET /api/products`

**Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Items per page
- `sortBy` (default: "date_desc") - Sort option

**Sort Options:**
- `date_desc` - Newest first
- `date_asc` - Oldest first
- `price_asc` - Lowest price first
- `price_desc` - Highest price first
- `popularity` - Most viewed first

### Next.js 15 Example - Server Component (Recommended):

```tsx
// app/products/page.tsx
import { ProductCard } from '@/components/ProductCard';

interface Product {
  id: string;
  title: string;
  description: string;
  price: number;
  originalPrice?: number;
  condition: string;
  primaryImageUrl?: string;
  categoryName: string;
  createdAt: string;
  sellerName: string;
  sellerRating: number;
}

interface PagedResponse {
  content: Product[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  isFirst: boolean;
  isLast: boolean;
}

async function getProducts(page = 0, sortBy = 'date_desc'): Promise<PagedResponse> {
  const response = await fetch(
    `http://localhost:8080/api/products?page=${page}&size=20&sortBy=${sortBy}`,
    {
      cache: 'no-store', // Or use revalidate: 60 for ISR
    }
  );

  if (!response.ok) {
    throw new Error('Failed to fetch products');
  }

  return response.json();
}

export default async function ProductsPage({
  searchParams,
}: {
  searchParams: { page?: string; sort?: string };
}) {
  const page = parseInt(searchParams.page || '0');
  const sortBy = searchParams.sort || 'date_desc';
  
  const data = await getProducts(page, sortBy);

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">
          All Products ({data.totalElements})
        </h1>

        {/* Sort dropdown */}
        <SortDropdown currentSort={sortBy} />
      </div>

      {data.content.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500">No products found</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {data.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>

          {/* Pagination */}
          <Pagination
            currentPage={data.currentPage}
            totalPages={data.totalPages}
            isFirst={data.isFirst}
            isLast={data.isLast}
          />
        </>
      )}
    </div>
  );
}
```

### Next.js 15 Example - Client Component with React Query:

```tsx
'use client';
import { useQuery } from '@tanstack/react-query';
import { ProductCard } from '@/components/ProductCard';
import { useState } from 'react';

interface Product {
  id: string;
  title: string;
  price: number;
  primaryImageUrl?: string;
  condition: string;
  categoryName: string;
}

interface PagedResponse {
  content: Product[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export function ProductList() {
  const [page, setPage] = useState(0);
  const [sortBy, setSortBy] = useState('date_desc');

  const { data, isLoading, error } = useQuery({
    queryKey: ['products', page, sortBy],
    queryFn: async () => {
      const response = await fetch(
        `http://localhost:8080/api/products?page=${page}&size=20&sortBy=${sortBy}`,
        {
          credentials: 'include', // For authenticated requests
        }
      );

      if (!response.ok) {
        throw new Error('Failed to fetch products');
      }

      return response.json() as Promise<PagedResponse>;
    },
  });

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {[...Array(8)].map((_, i) => (
          <ProductCardSkeleton key={i} />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-red-500">Error loading products</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Sort Controls */}
      <div className="flex justify-between items-center">
        <p className="text-gray-600">
          {data?.totalElements || 0} products found
        </p>
        
        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="px-4 py-2 border rounded-lg"
        >
          <option value="date_desc">Newest First</option>
          <option value="date_asc">Oldest First</option>
          <option value="price_asc">Price: Low to High</option>
          <option value="price_desc">Price: High to Low</option>
          <option value="popularity">Most Popular</option>
        </select>
      </div>

      {/* Products Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {data?.content.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>

      {/* Pagination */}
      <div className="flex justify-center gap-2 mt-8">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
        >
          Previous
        </button>
        
        <span className="px-4 py-2">
          Page {page + 1} of {data?.totalPages || 1}
        </span>
        
        <button
          onClick={() => setPage((p) => p + 1)}
          disabled={page >= (data?.totalPages || 1) - 1}
          className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}
```

---

## 🎯 Product Card Component

```tsx
// components/ProductCard.tsx
import Link from 'next/link';
import Image from 'next/image';

interface Product {
  id: string;
  title: string;
  price: number;
  originalPrice?: number;
  condition: string;
  primaryImageUrl?: string;
  categoryName: string;
  sellerName: string;
  sellerRating?: number;
}

export function ProductCard({ product }: { product: Product }) {
  const imageUrl = product.primaryImageUrl 
    ? `http://localhost:8080${product.primaryImageUrl}`
    : '/placeholder-product.jpg';

  const discountPercentage = product.originalPrice
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : 0;

  return (
    <Link href={`/products/${product.id}`}>
      <div className="border rounded-lg overflow-hidden hover:shadow-lg transition-shadow">
        {/* Image */}
        <div className="relative h-48 bg-gray-100">
          <Image
            src={imageUrl}
            alt={product.title}
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 25vw"
          />
          
          {discountPercentage > 0 && (
            <div className="absolute top-2 right-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded">
              {discountPercentage}% OFF
            </div>
          )}
          
          <div className="absolute top-2 left-2 bg-blue-600 text-white text-xs px-2 py-1 rounded">
            {product.condition}
          </div>
        </div>

        {/* Content */}
        <div className="p-4">
          <h3 className="font-semibold text-lg mb-2 line-clamp-2">
            {product.title}
          </h3>
          
          <div className="flex items-baseline gap-2 mb-2">
            <span className="text-2xl font-bold text-green-600">
              ₹{product.price.toLocaleString('en-IN')}
            </span>
            {product.originalPrice && (
              <span className="text-sm text-gray-400 line-through">
                ₹{product.originalPrice.toLocaleString('en-IN')}
              </span>
            )}
          </div>

          <div className="flex items-center justify-between text-sm text-gray-600">
            <span className="truncate">{product.sellerName}</span>
            {product.sellerRating && (
              <span className="flex items-center gap-1">
                ⭐ {product.sellerRating.toFixed(1)}
              </span>
            )}
          </div>

          <p className="text-xs text-gray-500 mt-1">{product.categoryName}</p>
        </div>
      </div>
    </Link>
  );
}
```

---

## 📝 Complete Product Creation Flow

```tsx
'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ProductImageUploader } from '@/components/ProductImageUploader';

interface Category {
  id: string;
  name: string;
}

export function CreateProductPage() {
  const router = useRouter();
  const [categories, setCategories] = useState<Category[]>([]);
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  // Fetch categories on mount
  useEffect(() => {
    async function fetchCategories() {
      try {
        const response = await fetch('http://localhost:8080/api/categories', {
          credentials: 'include',
        });
        const data = await response.json();
        setCategories(data);
      } catch (error) {
        console.error('Failed to fetch categories:', error);
      }
    }
    fetchCategories();
  }, []);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData(e.currentTarget);

    const productData = {
      title: formData.get('title') as string,
      description: formData.get('description') as string,
      price: parseFloat(formData.get('price') as string),
      originalPrice: formData.get('originalPrice')
        ? parseFloat(formData.get('originalPrice') as string)
        : undefined,
      isNegotiable: formData.get('negotiable') === 'on',
      condition: formData.get('condition') as string,
      brand: formData.get('brand') as string || undefined,
      model: formData.get('model') as string || undefined,
      categoryId: formData.get('categoryId') as string,
      tags: (formData.get('tags') as string)
        ?.split(',')
        .map(tag => tag.trim())
        .filter(Boolean) || [],
      pickupLocation: formData.get('pickupLocation') as string || undefined,
      deliveryAvailable: formData.get('delivery') === 'on',
      imageUrls: imageUrls, // From image uploader
    };

    try {
      const response = await fetch('http://localhost:8080/api/products', {
        method: 'POST',
        credentials: 'include', // CRITICAL: Send session cookie
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(productData),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Failed to create product');
      }

      const product = await response.json();
      alert('✅ Product created successfully! Awaiting admin approval.');
      router.push(`/products/${product.id}`);
      
    } catch (error: any) {
      alert('❌ Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-3xl mx-auto p-6 space-y-6">
      <h1 className="text-3xl font-bold">Create Product Listing</h1>

      {/* Image Upload */}
      <ProductImageUploader onImagesUploaded={setImageUrls} />

      {/* Title */}
      <div>
        <label className="block font-medium mb-2">
          Title <span className="text-red-500">*</span>
        </label>
        <input
          name="title"
          type="text"
          required
          maxLength={100}
          placeholder="e.g., iPhone 13 Pro 128GB - Pacific Blue"
          className="w-full p-3 border rounded-lg"
        />
      </div>

      {/* Description */}
      <div>
        <label className="block font-medium mb-2">
          Description <span className="text-red-500">*</span>
        </label>
        <textarea
          name="description"
          required
          rows={6}
          placeholder="Describe your product in detail..."
          className="w-full p-3 border rounded-lg"
        />
      </div>

      {/* Price Fields */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block font-medium mb-2">
            Selling Price (₹) <span className="text-red-500">*</span>
          </label>
          <input
            name="price"
            type="number"
            step="0.01"
            min="1"
            required
            placeholder="65000"
            className="w-full p-3 border rounded-lg"
          />
        </div>
        <div>
          <label className="block font-medium mb-2">
            Original Price (₹)
          </label>
          <input
            name="originalPrice"
            type="number"
            step="0.01"
            min="1"
            placeholder="119900"
            className="w-full p-3 border rounded-lg"
          />
        </div>
      </div>

      {/* Condition */}
      <div>
        <label className="block font-medium mb-2">
          Condition <span className="text-red-500">*</span>
        </label>
        <select name="condition" required className="w-full p-3 border rounded-lg">
          <option value="">Select condition</option>
          <option value="NEW">New</option>
          <option value="LIKE_NEW">Like New</option>
          <option value="GOOD">Good</option>
          <option value="FAIR">Fair</option>
          <option value="POOR">Poor</option>
          <option value="USED">Used</option>
        </select>
      </div>

      {/* Brand & Model */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block font-medium mb-2">Brand</label>
          <input name="brand" type="text" placeholder="Apple" className="w-full p-3 border rounded-lg" />
        </div>
        <div>
          <label className="block font-medium mb-2">Model</label>
          <input name="model" type="text" placeholder="iPhone 13 Pro" className="w-full p-3 border rounded-lg" />
        </div>
      </div>

      {/* Category */}
      <div>
        <label className="block font-medium mb-2">
          Category <span className="text-red-500">*</span>
        </label>
        <select name="categoryId" required className="w-full p-3 border rounded-lg">
          <option value="">Select category</option>
          {categories.map(cat => (
            <option key={cat.id} value={cat.id}>{cat.name}</option>
          ))}
        </select>
      </div>

      {/* Tags */}
      <div>
        <label className="block font-medium mb-2">Tags</label>
        <input
          name="tags"
          type="text"
          placeholder="iphone, smartphone, 128gb (comma separated)"
          className="w-full p-3 border rounded-lg"
        />
      </div>

      {/* Pickup Location */}
      <div>
        <label className="block font-medium mb-2">Pickup Location</label>
        <input
          name="pickupLocation"
          type="text"
          placeholder="VIT Main Campus, Block A"
          className="w-full p-3 border rounded-lg"
        />
      </div>

      {/* Checkboxes */}
      <div className="space-y-2">
        <label className="flex items-center gap-2">
          <input name="negotiable" type="checkbox" />
          <span>Price is negotiable</span>
        </label>
        <label className="flex items-center gap-2">
          <input name="delivery" type="checkbox" />
          <span>Delivery available</span>
        </label>
      </div>

      {/* Submit */}
      <button
        type="submit"
        disabled={loading}
        className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400"
      >
        {loading ? 'Creating...' : 'Create Product Listing'}
      </button>

      <p className="text-sm text-gray-600 text-center">
        ⚠️ Your product will be reviewed by admin before going live
      </p>
    </form>
  );
}
```

---

## 🔐 Authentication Check Hook

```tsx
// hooks/useAuth.ts
'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

interface User {
  id: string;
  email: string;
  name: string;
  isVerifiedStudent: boolean;
}

export function useAuth(requireAuth = true) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    async function checkAuth() {
      try {
        const response = await fetch('http://localhost:8080/api/auth/profile', {
          credentials: 'include',
        });

        if (response.ok) {
          const userData = await response.json();
          setUser(userData);
        } else if (requireAuth) {
          router.push('/login');
        }
      } catch (error) {
        if (requireAuth) {
          router.push('/login');
        }
      } finally {
        setLoading(false);
      }
    }

    checkAuth();
  }, [requireAuth, router]);

  return { user, loading, isAuthenticated: !!user };
}
```

---

## 🧪 Testing the Integration

### 1. Test Image Upload:

```bash
# Login first
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@dealharbor.com","password":"password123"}' \
  -c cookies.txt

# Upload image
curl -X POST http://localhost:8080/api/images/upload-product \
  -b cookies.txt \
  -F "file=@/path/to/image.jpg"
```

### 2. Test Get Products:

```bash
# Get all products (no auth needed)
curl http://localhost:8080/api/products

# Get products with pagination
curl "http://localhost:8080/api/products?page=0&size=10&sortBy=price_asc"
```

### 3. Test Create Product:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Test Product",
    "description": "Test description",
    "price": 5000,
    "condition": "LIKE_NEW",
    "categoryId": "electronics",
    "imageUrls": ["/api/images/products/123_image.jpg"]
  }'
```

---

## ⚠️ Common Issues & Solutions

### Issue 1: CORS Error
**Error:** "CORS policy: No 'Access-Control-Allow-Origin' header"

**Solution:**
- ✅ Already fixed - All controllers now have proper CORS
- Ensure you're using `credentials: 'include'` in fetch calls
- Backend allows `http://localhost:3000` and `http://127.0.0.1:3000`

### Issue 2: 401 Unauthorized
**Error:** "Unauthorized" when creating products

**Solution:**
- Login first and ensure session cookie is sent
- Always use `credentials: 'include'` in authenticated requests
- Check if session is valid (call `/api/auth/profile`)

### Issue 3: No Products Returned
**Cause:** All products are in PENDING status

**Solution:**
- Products must be APPROVED by admin to appear in listings
- Admin can approve via: `POST /api/admin/products/{productId}/approve`
- Check product status: `GET /api/products/{productId}`

### Issue 4: Images Not Displaying
**Cause:** Incorrect image URL construction

**Solution:**
```tsx
// ✅ Correct
const imageUrl = `http://localhost:8080${product.primaryImageUrl}`;

// ❌ Wrong
const imageUrl = product.primaryImageUrl; // Missing base URL
```

### Issue 5: Image Upload 401 Error
**Cause:** Not logged in

**Solution:**
- Ensure user is authenticated before showing upload form
- Use `useAuth()` hook to check authentication
- Redirect to login if not authenticated

---

## 📊 Product Status Flow

```
CREATE → PENDING → (Admin Review) → APPROVED → SOLD
                                  ↓
                               REJECTED
```

1. **PENDING** - Just created, waiting for admin
2. **APPROVED** - Admin approved, visible to buyers
3. **REJECTED** - Admin rejected, not visible
4. **SOLD** - Marked as sold by seller
5. **DELETED** - Soft deleted by seller

---

## 🎯 Environment Variables

Create `.env.local` in your Next.js project:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_API_BASE=http://localhost:8080/api
```

Use in code:

```tsx
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

fetch(`${API_URL}/api/products`, {
  credentials: 'include',
});
```

---

## 🚀 Next Steps

1. **Start Backend:**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Ensure Redis is Running:**
   ```bash
   redis-server
   ```

3. **Test Endpoints:**
   - ✅ Image upload: `POST /api/images/upload-product`
   - ✅ Get products: `GET /api/products`
   - ✅ Create product: `POST /api/products`

4. **Integrate in Next.js:**
   - Copy components from this guide
   - Install React Query: `npm install @tanstack/react-query`
   - Set up authentication flow
   - Build product listing pages

---

## 📞 Quick Reference

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/images/upload-product` | POST | ✅ | Upload product image |
| `/api/images/products/{filename}` | GET | ❌ | Get product image |
| `/api/products` | GET | ❌ | Get all products |
| `/api/products` | POST | ✅ | Create product |
| `/api/products/{id}` | GET | ❌ | Get product details |
| `/api/categories` | GET | ❌ | Get all categories |
| `/api/auth/login` | POST | ❌ | Login |
| `/api/auth/profile` | GET | ✅ | Get current user |

---

**Your backend is now ready for frontend integration! 🎉**
