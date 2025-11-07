# Supabase Storage Configuration Guide

## Overview
Product images are now stored in **Supabase Storage (S3-compatible)** instead of the local filesystem.

## Setup Instructions

### 1. Create Storage Bucket in Supabase

1. Go to your Supabase project dashboard: https://supabase.com/dashboard/project/owywyvyrhwydlrzxgfpv
2. Navigate to **Storage** in the left sidebar
3. Click **"New bucket"**
4. Create a bucket named: `product-images`
5. Set it as **Public** bucket (so images can be accessed directly via URL)

### 2. Get Supabase Credentials

You need two keys from Supabase:

1. **Project URL**: `https://owywyvyrhwydlrzxgfpv.supabase.co`
2. **Service Role Key** (acts as both access key and secret key for S3):
   - Go to **Project Settings** → **API**
   - Copy the `service_role` key (NOT the `anon` key)
   - This key has full permissions to manage storage

### 3. Configure application.properties

The configuration is already added to `application.properties`:

```properties
# Supabase Storage Configuration (S3-compatible)
supabase.storage.endpoint=https://owywyvyrhwydlrzxgfpv.supabase.co/storage/v1/s3
supabase.storage.region=ap-south-1
supabase.storage.bucket-name=product-images
supabase.storage.access-key=${SUPABASE_ACCESS_KEY:your-supabase-service-role-key}
supabase.storage.secret-key=${SUPABASE_SECRET_KEY:your-supabase-service-role-key}
supabase.storage.public-url=https://owywyvyrhwydlrzxgfpv.supabase.co/storage/v1/object/public/product-images
```

**IMPORTANT**: Replace the placeholder values:
- Set both `SUPABASE_ACCESS_KEY` and `SUPABASE_SECRET_KEY` to your **service_role** key
- Or set them as environment variables:
  ```bash
  # Windows PowerShell
  $env:SUPABASE_ACCESS_KEY="your-service-role-key-here"
  $env:SUPABASE_SECRET_KEY="your-service-role-key-here"
  
  # Linux/Mac
  export SUPABASE_ACCESS_KEY="your-service-role-key-here"
  export SUPABASE_SECRET_KEY="your-service-role-key-here"
  ```

### 4. Bucket Policy (Optional)

The `StorageService` will attempt to set a public read policy automatically. However, if you created the bucket as public, this is already handled by Supabase.

If you need to manually set the policy:
1. Go to **Storage** → **Policies**
2. Create a new policy for the `product-images` bucket
3. Allow public `SELECT` (read) access:
   ```sql
   -- Allow anyone to read images
   CREATE POLICY "Public Access"
   ON storage.objects FOR SELECT
   USING ( bucket_id = 'product-images' );
   
   -- Allow authenticated users to upload
   CREATE POLICY "Authenticated Upload"
   ON storage.objects FOR INSERT
   WITH CHECK ( bucket_id = 'product-images' AND auth.role() = 'authenticated' );
   ```

## How It Works

### Image Upload Flow

1. **Frontend** sends a multipart file to `/api/images/upload-product`
2. **ImageController** receives the file and calls `StorageService.uploadProductImage()`
3. **StorageService** uploads the file to Supabase Storage S3 bucket
4. Returns a **public URL** like:
   ```
   https://owywyvyrhwydlrzxgfpv.supabase.co/storage/v1/object/public/product-images/products/uuid_timestamp_filename.jpg
   ```
5. **Frontend** stores this URL in the product's `imageUrls` array
6. Images are displayed directly using this URL (no backend proxy needed)

### API Endpoints

#### Upload Product Image
```
POST /api/images/upload-product
Content-Type: multipart/form-data

Parameters:
- file: The image file (max 5MB, image types only)

Returns: Full Supabase Storage URL as plain text
Example: "https://owywyvyrhwydlrzxgfpv.supabase.co/storage/v1/object/public/product-images/products/abc123_1234567890_product.jpg"
```

#### Upload Profile Photo
```
POST /api/images/upload-profile-photo
Content-Type: multipart/form-data

Parameters:
- file: The image file (max 5MB, image types only)

Returns: Full Supabase Storage URL as plain text
```

#### Delete Image
```
DELETE /api/images/delete?url={imageUrl}

Parameters:
- url: Full Supabase Storage URL to delete

Returns: { "message": "Image deleted successfully" }
```

#### Check Image Exists
```
GET /api/images/check?url={imageUrl}

Parameters:
- url: Full Supabase Storage URL to check

Returns: { "exists": true/false }
```

## Migration Notes

### Breaking Changes
- Old local filesystem paths like `/api/images/products/{filename}` **no longer work**
- These endpoints now return HTTP 410 (Gone) with a migration message
- All new uploads return full Supabase URLs

### Frontend Changes Required
Your frontend should already be compatible if it:
1. Stores the full URL returned from the upload endpoint
2. Uses the URL directly in `<img src="">` tags
3. Doesn't try to construct URLs by concatenating paths

### Database Migration
If you have existing products with local filesystem paths in the database:
1. You'll need to re-upload those images
2. Update the database records with new Supabase URLs
3. Or run a migration script to copy local files to Supabase

## Benefits

✅ **Cloud Storage**: Images stored reliably in Supabase
✅ **CDN**: Supabase uses global CDN for fast image delivery
✅ **Scalability**: No local disk space limits
✅ **Backup**: Automatic backups by Supabase
✅ **Public URLs**: Direct access without backend proxy
✅ **Cost-Effective**: Supabase storage is very affordable

## Troubleshooting

### "Failed to upload file to Supabase Storage"
- Check that the `product-images` bucket exists in Supabase
- Verify your service role key is correct
- Ensure the bucket is set as public
- Check Supabase project is active (not paused)

### "Access Denied" errors
- Make sure you're using the **service_role** key, not the **anon** key
- The service role key has full permissions

### Images not displaying
- Verify the bucket is public
- Check the URL format is correct
- Ensure CORS is configured in Supabase (usually automatic)
- Open the URL directly in browser to test

### Build Errors
If you get AWS SDK errors:
```bash
# Windows PowerShell
./mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

## Testing

1. Start the application
2. Upload a test image using Postman or your frontend:
   ```bash
   curl -X POST http://localhost:8080/api/images/upload-product \
     -F "file=@test-image.jpg"
   ```
3. You should get back a Supabase URL
4. Open that URL in your browser - the image should display
5. Check your Supabase Storage dashboard - the file should be visible

## Security Notes

⚠️ **IMPORTANT**: 
- The **service_role** key has full admin access - keep it secret!
- Never commit it to Git or expose it in frontend code
- Use environment variables in production
- Consider using Supabase Auth tokens for user-uploaded images in production
- Current setup allows any authenticated user to upload - add additional validation as needed

## Production Recommendations

For production deployment:
1. Use environment variables for keys (not hardcoded values)
2. Implement rate limiting on upload endpoints
3. Add user-specific folders (e.g., `products/{userId}/{filename}`)
4. Set up image optimization/resizing
5. Monitor storage usage and costs
6. Implement cleanup for deleted products
7. Consider using signed URLs for sensitive images
