# 📦 Product Listing Upload Guide

## Complete Step-by-Step Guide for Creating Product Listings

---

## 🎯 Overview

Creating a product listing involves **TWO main steps**:
1. **Upload product images** (if you have any)
2. **Create the product listing** with all details

---

## 📋 Required Information

### ✅ **Required Fields** (MUST provide)
- `title` - Product name (e.g., "iPhone 13 Pro 128GB")
- `description` - Detailed description of the product
- `price` - Selling price in INR (e.g., 65000.00)
- `condition` - Product condition (see options below)
- `categoryId` - Category ID (get from /api/categories)

### 📝 **Optional Fields** (Recommended)
- `originalPrice` - Original/MRP price for showing discount
- `isNegotiable` - Whether price is negotiable (default: false)
- `brand` - Product brand (e.g., "Apple", "Samsung")
- `model` - Product model (e.g., "iPhone 13 Pro")
- `tags` - Array of tags for search (e.g., ["iphone", "smartphone", "128gb"])
- `pickupLocation` - Where buyer can pick up (e.g., "VIT Main Campus, Block A")
- `deliveryAvailable` - Whether you offer delivery (default: false)
- `imageUrls` - Array of uploaded image URLs

---

## 🏷️ Product Condition Options

Choose ONE from these options:

| Condition | Code | Description |
|-----------|------|-------------|
| **New** | `NEW` | Brand new, never used |
| **Like New** | `LIKE_NEW` | Barely used, excellent condition |
| **Good** | `GOOD` | Used but in good working condition |
| **Fair** | `FAIR` | Shows wear but still functional |
| **Poor** | `POOR` | Heavy wear, may need repairs |
| **Used** | `USED` | Previously owned, normal wear |

---

## 📸 Step 1: Upload Product Images (Optional but Recommended)

### Why Upload Images First?
You need the image URLs before creating the product listing.

### Endpoint: `POST /api/images/upload-product`
(Note: Check if this endpoint exists, or you might need to use profile photo endpoint temporarily)

### Next.js 15 Example:

```tsx
'use client';
import { useState } from 'react';

export function ImageUploader({ onImagesUploaded }: { 
  onImagesUploaded: (urls: string[]) => void 
}) {
  const [uploading, setUploading] = useState(false);
  const [uploadedUrls, setUploadedUrls] = useState<string[]>([]);

  async function handleFileUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setUploading(true);
    const urls: string[] = [];

    try {
      // Upload each file one by one
      for (const file of Array.from(files)) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(
          'http://localhost:8080/api/images/upload-product', // Adjust endpoint
          {
            method: 'POST',
            credentials: 'include', // Important for session auth
            body: formData, // Don't set Content-Type, browser sets it automatically
          }
        );

        if (!response.ok) throw new Error('Upload failed');
        
        const imageUrl = await response.text(); // Backend returns URL as text
        urls.push(imageUrl);
      }

      setUploadedUrls(urls);
      onImagesUploaded(urls);
      alert(`${urls.length} image(s) uploaded successfully!`);
    } catch (error) {
      alert('Image upload failed: ' + error.message);
    } finally {
      setUploading(false);
    }
  }

  return (
    <div>
      <label className="block mb-2 font-medium">
        Product Images (up to 5 recommended)
      </label>
      
      <input
        type="file"
        accept="image/png,image/jpeg,image/jpg"
        multiple
        onChange={handleFileUpload}
        disabled={uploading}
        className="block w-full"
      />
      
      {uploading && <p className="text-blue-600 mt-2">Uploading...</p>}
      
      {uploadedUrls.length > 0 && (
        <div className="mt-4 grid grid-cols-3 gap-2">
          {uploadedUrls.map((url, index) => (
            <div key={index} className="relative">
              <img
                src={url}
                alt={`Upload ${index + 1}`}
                className="w-full h-32 object-cover rounded border"
              />
              <button
                onClick={() => {
                  const newUrls = uploadedUrls.filter((_, i) => i !== index);
                  setUploadedUrls(newUrls);
                  onImagesUploaded(newUrls);
                }}
                className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

---

## 🛍️ Step 2: Create Product Listing

### Endpoint: `POST /api/products`

**Authentication:** Required (session cookie)  
**Content-Type:** application/json

### Request Body Format:

```json
{
  "title": "iPhone 13 Pro 128GB - Pacific Blue",
  "description": "Excellent condition iPhone 13 Pro with all original accessories. Battery health 95%. No scratches or dents. Used for 6 months only. Includes:\n- Original box\n- Charging cable\n- Earphones\n- Case and screen protector",
  "price": 65000.00,
  "originalPrice": 119900.00,
  "isNegotiable": true,
  "condition": "LIKE_NEW",
  "brand": "Apple",
  "model": "iPhone 13 Pro",
  "categoryId": "cat-electronics-smartphones",
  "tags": ["iphone", "apple", "smartphone", "128gb", "pacific blue"],
  "pickupLocation": "VIT Main Campus, Block A, Near Canteen",
  "deliveryAvailable": false,
  "imageUrls": [
    "/api/images/products/1698765432000_front.jpg",
    "/api/images/products/1698765432001_back.jpg",
    "/api/images/products/1698765432002_accessories.jpg"
  ]
}
```

### Field Breakdown:

#### **title** (Required)
- Keep it descriptive but concise
- Include key specs/features
- Max recommended: 100 characters
- Example: "MacBook Air M1 8GB 256GB - Space Grey"

#### **description** (Required)
- Detailed product information
- Mention condition clearly
- List what's included
- Mention any defects honestly
- Use line breaks (\n) for readability
- Example:
  ```
  "MacBook Air M1 chip with 8GB RAM and 256GB SSD in Space Grey color.
  
  Condition: Like new, barely used for 3 months.
  
  What's included:
  - MacBook Air M1
  - Original charger
  - Original box
  - Protective case
  
  Reason for selling: Upgrading to MacBook Pro
  
  No trades, serious buyers only."
  ```

#### **price** (Required)
- Your selling price in INR
- Use decimal format: 65000.00
- Don't use commas or currency symbols
- Example: 25000.00 (not "25,000" or "₹25000")

#### **originalPrice** (Optional)
- Original/MRP price
- Shows discount percentage to buyers
- Example: If bought for ₹80,000, selling for ₹60,000
  ```json
  "price": 60000.00,
  "originalPrice": 80000.00
  ```

#### **isNegotiable** (Optional, default: false)
- `true` - Price is negotiable
- `false` - Firm price, no negotiation
- Negotiable items get more inquiries

#### **condition** (Required)
Must be one of these EXACT values:
- `"NEW"` - Brand new, unopened
- `"LIKE_NEW"` - Barely used, looks new
- `"GOOD"` - Normal wear, fully functional
- `"FAIR"` - Visible wear, works fine
- `"POOR"` - Heavy wear, may need repairs
- `"USED"` - Generally used condition

#### **brand** (Optional)
- Product brand name
- Example: "Apple", "Samsung", "Sony", "HP"
- Helps with search and filtering

#### **model** (Optional)
- Specific model number/name
- Example: "iPhone 13 Pro", "Galaxy S21", "XPS 15"

#### **categoryId** (Required)
Get category ID from: `GET /api/categories`

Common categories:
```
Electronics > Smartphones: "cat-electronics-smartphones"
Electronics > Laptops: "cat-electronics-laptops"
Books: "cat-books"
Furniture: "cat-furniture"
Clothing: "cat-clothing"
```

To get all categories:
```bash
curl http://localhost:8080/api/categories
```

#### **tags** (Optional but recommended)
Array of search keywords
- Lowercase recommended
- Include brand, model, features, color
- Max 10 tags recommended
- Example:
  ```json
  "tags": ["laptop", "gaming", "rtx", "nvidia", "16gb", "hp", "omen"]
  ```

#### **pickupLocation** (Optional but recommended)
Where buyer can meet/pickup
- Be specific but safe (public places)
- Example: "VIT Main Campus Library"
- Don't share exact room/house number

#### **deliveryAvailable** (Optional, default: false)
- `true` - You can deliver to buyer
- `false` - Pickup only
- Delivery items attract more buyers

#### **imageUrls** (Optional but HIGHLY recommended)
Array of uploaded image URLs
- Get URLs from Step 1 (image upload)
- Recommended: 3-5 images
- First image is the main/thumbnail image
- Show product from different angles
- Example:
  ```json
  "imageUrls": [
    "/api/images/products/1698765432000_front.jpg",
    "/api/images/products/1698765432001_back.jpg",
    "/api/images/products/1698765432002_box.jpg"
  ]
  ```

---

## 💻 Complete Next.js 15 Example

### Full Product Creation Form:

```tsx
'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

interface Category {
  id: string;
  name: string;
}

export function CreateProductForm({ categories }: { categories: Category[] }) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [imageUrls, setImageUrls] = useState<string[]>([]);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData(e.currentTarget);

    // Build request body
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
      imageUrls: imageUrls, // From image upload
    };

    try {
      const response = await fetch('http://localhost:8080/api/products', {
        method: 'POST',
        credentials: 'include', // IMPORTANT: Send session cookie
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
      alert('Product created successfully! Awaiting admin approval.');
      router.push(`/products/${product.id}`);
    } catch (error) {
      alert('Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-3xl font-bold">Create Product Listing</h1>

      {/* Image Upload */}
      <ImageUploader onImagesUploaded={setImageUrls} />

      {/* Title */}
      <div>
        <label className="block mb-2 font-medium">
          Title <span className="text-red-500">*</span>
        </label>
        <input
          name="title"
          type="text"
          required
          maxLength={100}
          placeholder="e.g., iPhone 13 Pro 128GB - Pacific Blue"
          className="w-full p-3 border rounded"
        />
        <p className="text-sm text-gray-500 mt-1">
          Keep it descriptive and include key features
        </p>
      </div>

      {/* Description */}
      <div>
        <label className="block mb-2 font-medium">
          Description <span className="text-red-500">*</span>
        </label>
        <textarea
          name="description"
          required
          rows={6}
          placeholder="Describe your product in detail. Include condition, what's included, reason for selling, etc."
          className="w-full p-3 border rounded"
        />
      </div>

      {/* Price */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block mb-2 font-medium">
            Selling Price (₹) <span className="text-red-500">*</span>
          </label>
          <input
            name="price"
            type="number"
            step="0.01"
            min="1"
            required
            placeholder="65000.00"
            className="w-full p-3 border rounded"
          />
        </div>
        <div>
          <label className="block mb-2 font-medium">
            Original Price (₹) <span className="text-gray-400">(Optional)</span>
          </label>
          <input
            name="originalPrice"
            type="number"
            step="0.01"
            min="1"
            placeholder="119900.00"
            className="w-full p-3 border rounded"
          />
        </div>
      </div>

      {/* Condition */}
      <div>
        <label className="block mb-2 font-medium">
          Condition <span className="text-red-500">*</span>
        </label>
        <select name="condition" required className="w-full p-3 border rounded">
          <option value="">Select condition</option>
          <option value="NEW">New - Brand new, never used</option>
          <option value="LIKE_NEW">Like New - Barely used, excellent condition</option>
          <option value="GOOD">Good - Used but in good working condition</option>
          <option value="FAIR">Fair - Shows wear but still functional</option>
          <option value="POOR">Poor - Heavy wear, may need repairs</option>
          <option value="USED">Used - Previously owned, normal wear</option>
        </select>
      </div>

      {/* Brand & Model */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block mb-2 font-medium">Brand</label>
          <input
            name="brand"
            type="text"
            placeholder="e.g., Apple"
            className="w-full p-3 border rounded"
          />
        </div>
        <div>
          <label className="block mb-2 font-medium">Model</label>
          <input
            name="model"
            type="text"
            placeholder="e.g., iPhone 13 Pro"
            className="w-full p-3 border rounded"
          />
        </div>
      </div>

      {/* Category */}
      <div>
        <label className="block mb-2 font-medium">
          Category <span className="text-red-500">*</span>
        </label>
        <select name="categoryId" required className="w-full p-3 border rounded">
          <option value="">Select category</option>
          {categories.map(cat => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
      </div>

      {/* Tags */}
      <div>
        <label className="block mb-2 font-medium">Tags</label>
        <input
          name="tags"
          type="text"
          placeholder="laptop, gaming, rtx, 16gb (comma separated)"
          className="w-full p-3 border rounded"
        />
        <p className="text-sm text-gray-500 mt-1">
          Add keywords to help buyers find your product
        </p>
      </div>

      {/* Pickup Location */}
      <div>
        <label className="block mb-2 font-medium">Pickup Location</label>
        <input
          name="pickupLocation"
          type="text"
          placeholder="e.g., VIT Main Campus, Block A"
          className="w-full p-3 border rounded"
        />
      </div>

      {/* Checkboxes */}
      <div className="space-y-3">
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
        className="w-full bg-blue-600 text-white py-3 rounded font-medium hover:bg-blue-700 disabled:bg-gray-400"
      >
        {loading ? 'Creating...' : 'Create Product Listing'}
      </button>

      <p className="text-sm text-gray-600">
        * Your product will be reviewed by admin before going live
      </p>
    </form>
  );
}
```

---

## 🧪 Testing with curl

### 1. First, login to get session:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@dealharbor.com","password":"password123"}' \
  -c cookies.txt
```

### 2. Create product:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "iPhone 13 Pro 128GB",
    "description": "Excellent condition, barely used",
    "price": 65000.00,
    "condition": "LIKE_NEW",
    "categoryId": "cat-electronics-smartphones",
    "isNegotiable": true,
    "deliveryAvailable": false
  }'
```

---

## ⚠️ Common Mistakes to Avoid

1. **Missing Authentication**
   - ❌ Don't forget `credentials: 'include'`
   - ✅ Always send session cookie

2. **Wrong Price Format**
   - ❌ `"price": "65,000"` or `"price": "₹65000"`
   - ✅ `"price": 65000.00`

3. **Wrong Condition Value**
   - ❌ `"condition": "like new"` (lowercase)
   - ✅ `"condition": "LIKE_NEW"` (exact enum)

4. **Invalid Category ID**
   - ❌ Using category name instead of ID
   - ✅ Get ID from `/api/categories` first

5. **Image URLs Before Upload**
   - ❌ Using external URLs or local file paths
   - ✅ Upload images first, get URLs, then use them

6. **Empty Required Fields**
   - ❌ Missing title, description, price, condition, or categoryId
   - ✅ All required fields must have values

---

## 🔄 Product Status Flow

After creating a product:

1. **PENDING** - Your product is created
2. **Admin Reviews** - Admin checks if it meets guidelines
3. **APPROVED** - Product goes live, buyers can see it
4. **REJECTED** - Admin rejects (you'll get notification with reason)
5. **SOLD** - Mark as sold when transaction completes

---

## 📊 What Happens After Creation?

1. Product status is set to `PENDING`
2. Admin receives notification
3. Admin reviews and approves/rejects
4. You receive notification of approval/rejection
5. If approved, product appears in search and listings
6. Buyers can view, favorite, and contact you

---

## 🎯 Tips for Better Listings

1. **Use Multiple Images** - Products with 3+ images sell faster
2. **Be Honest** - Describe condition accurately
3. **Price Competitively** - Check similar products
4. **Detailed Description** - More details = more buyer confidence
5. **Good Tags** - Better search visibility
6. **Specific Location** - Easier for local buyers
7. **Quick Response** - Reply to inquiries promptly

---

## 🆘 Troubleshooting

### Error: "Unauthorized" (401)
→ You're not logged in. Login first and ensure cookies are sent.

### Error: "Cannot find category"
→ Invalid categoryId. Get valid IDs from `/api/categories`

### Error: "Invalid condition"
→ Use exact enum values: NEW, LIKE_NEW, GOOD, FAIR, POOR, USED

### Product stuck in PENDING
→ Wait for admin approval. Can take up to 24-48 hours.

### Images not showing
→ Make sure imageUrls array contains valid uploaded image URLs

---

## 📞 Need Help?

- Check API documentation: `endpoints.txt`
- Test endpoints with curl first
- Check browser console for errors
- Verify session cookie is being sent

---

**Good luck with your product listings! 🚀**
