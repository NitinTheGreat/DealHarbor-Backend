# Product Pending Review System - Complete Implementation Guide

## 🎯 Overview

Instead of automatically deleting products after 14 days of being pending, the system now:
1. **Moves them to a review queue** for extended admin review
2. **Sends email notification** to users asking them to edit/improve their listing
3. **Allows admin to approve or reject** from the review queue
4. **Deletes only rejected products** from the review queue
5. **Users can edit their pending products** at any time

---

## 📋 New Architecture

### Old System (Before)
```
Product Created (PENDING)
        ↓
    14 days pass
        ↓
❌ AUTO-DELETED (without admin review)
```

### New System (Current)
```
Product Created (PENDING)
        ↓
    14 days pass
        ↓
📋 MOVED TO REVIEW QUEUE
        ↓
    ✉️ Email sent to user
        ↓
    User can edit product
        ↓
    Admin reviews product
        ↓
    ├─→ APPROVED ✅ → Goes live on marketplace
    └─→ REJECTED ❌ → Product deleted
```

---

## 🗄️ Database Schema

### New Table: `product_pending_reviews`

```sql
CREATE TABLE product_pending_reviews (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    original_created_at TIMESTAMP NOT NULL,
    moved_to_review_at TIMESTAMP NOT NULL,
    days_pending INTEGER NOT NULL,
    review_notes TEXT,
    user_notified BOOLEAN NOT NULL DEFAULT FALSE,
    notification_sent_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    review_decision VARCHAR(50),
    review_reason TEXT,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);
```

**Fields Explanation**:
- `product_id` - Reference to the product in review
- `original_created_at` - When product was first created
- `moved_to_review_at` - When product was moved to review queue (after 14 days)
- `days_pending` - Number of days product was pending before review
- `user_notified` - Whether user was sent email notification
- `is_resolved` - Whether admin has taken action (approved/rejected)
- `review_decision` - Admin's decision (APPROVED or REJECTED)

---

## 🔄 Workflow Details

### 1. Automatic Review Queue Movement (Daily at 2 AM)

**Trigger**: Scheduled task runs daily at 2:00 AM

**Process**:
```java
// ProductService.autoDeleteExpiredProducts()

1. Find products PENDING for 14+ days
2. For each product:
   a. Check if already in review queue (skip if yes)
   b. Create ProductPendingReview record
   c. Send email to user
   d. Create in-app notification
   e. Log the action
```

**What Gets Moved**:
- Products with status = `PENDING`
- Created more than 14 days ago
- Not already in review queue

**What Gets Deleted**:
- Products with status = `REJECTED` (deleted immediately, not moved to queue)

### 2. Email Notification to User

**Email Template**: `sendProductMovedToReview()`

```
Subject: DealHarbor - Product Needs Your Attention

Hi [User Name],

Your product '[Product Title]' has been pending approval for [X] days.

⚠️ ACTION REQUIRED:

Your product has been moved to an extended review queue. This means:

1. Your product is still visible to admins for review
2. You can edit your product listing to improve its chances of approval
3. The admin team will review it soon

💡 RECOMMENDATIONS:
• Review your product description - make it clear and detailed
• Check your images - ensure they are clear and show the product well
• Verify all information is accurate (price, condition, etc.)
• Make sure you've followed our listing guidelines

You can edit your product by:
1. Logging into your account
2. Going to 'My Products'
3. Clicking 'Edit' on this product

After you make improvements, an admin will review it and make a decision.

⚠️ If your product is REJECTED after this review, it will be permanently deleted.
⚠️ If APPROVED, it will go live on the marketplace immediately!

Best regards,
DealHarbor Team
```

### 3. User Edits Product

**Users can edit their pending products at any time**

**Endpoint**: `PUT /api/products/{productId}`

**What users can update**:
- Title, description
- Price, condition
- Images
- Category
- All product details

**Effect on review**:
- Product remains in review queue
- Admin will see updated information
- Does NOT reset the review timer

### 4. Admin Reviews Product

**Admin can approve or reject from review queue**

**Endpoint**: `POST /api/product-reviews/admin/{reviewId}/action`

**Request Body**:
```json
{
  "decision": "APPROVED",  // or "REJECTED"
  "reason": "Product meets all guidelines",
  "adminNotes": "Approved after user improved description"
}
```

#### If Admin APPROVES:
1. Product status → `APPROVED`
2. Review marked as resolved
3. Product goes live on marketplace
4. Email sent to user: "Product Approved!"
5. In-app notification created
6. Admin action logged

#### If Admin REJECTS:
1. Product status → `REJECTED`
2. Review marked as resolved
3. Product will be deleted next day at 2 AM
4. Email sent to user: "Product Rejected"
5. In-app notification created
6. Admin action logged
7. Seller stats updated (activeListings decreased)

---

## 📧 Email Templates

### 1. Product Moved to Review
**Method**: `sendProductMovedToReview()`  
**Sent When**: Product moved to review queue after 14 days  
**Purpose**: Inform user and encourage editing

### 2. Product Approved After Review
**Method**: `sendProductApprovedAfterReview()`  
**Sent When**: Admin approves from review queue  
**Email**:
```
Subject: DealHarbor - Great News! Your Product is Approved

Hi [User Name],

🎉 Excellent news! Your product '[Product Title]' has been approved!

Your product is now live on the DealHarbor marketplace and visible to all users.

Thank you for taking the time to improve your listing. Your attention to detail has paid off!

What happens next:
• Your product is now searchable by all users
• Buyers can contact you for inquiries
• You'll receive notifications when users show interest

Best of luck with your sale!

Best regards,
DealHarbor Team
```

### 3. Product Rejected After Review
**Method**: `sendProductRejectedAfterReview()`  
**Sent When**: Admin rejects from review queue  
**Email**:
```
Subject: DealHarbor - Product Rejected After Review

Hi [User Name],

Unfortunately, your product '[Product Title]' has been rejected after extended review.

Reason: [Admin's reason]

Your product has been permanently removed from our system.

What you can do next:
• Review our listing guidelines carefully
• Address the issues mentioned in the rejection reason
• Create a new listing with improved details and images

We appreciate your understanding and encourage you to list again with the necessary improvements.

Best regards,
DealHarbor Team
```

---

## 🛠️ API Endpoints

### For Users

#### 1. Get My Products
```http
GET /api/products/my-products?page=0&size=20
Authorization: Required
```

**Response**: All user's products (including PENDING ones in review)

#### 2. Get My Products in Review Queue
```http
GET /api/product-reviews/my-pending
Authorization: Required
```

**Response**:
```json
[
  {
    "id": "review_123",
    "productId": "prod_456",
    "productTitle": "MacBook Pro 2020",
    "productDescription": "...",
    "productPrice": 45000.0,
    "categoryName": "Electronics",
    "sellerName": "John Doe",
    "sellerEmail": "john@example.com",
    "originalCreatedAt": "2025-10-20T10:00:00Z",
    "movedToReviewAt": "2025-11-03T02:00:00Z",
    "daysPending": 14,
    "userNotified": true,
    "notificationSentAt": "2025-11-03T02:00:05Z",
    "isResolved": false
  }
]
```

#### 3. Edit Product
```http
PUT /api/products/{productId}
Authorization: Required
Content-Type: application/json

{
  "title": "Updated title",
  "description": "Better description with more details",
  "price": 42000.0,
  "imageUrls": ["url1", "url2"],
  // ... other fields
}
```

**Effect**: Product updated, remains in review queue

#### 4. Delete Product
```http
DELETE /api/products/{productId}
Authorization: Required
```

**Effect**: Product deleted (if not SOLD)

---

### For Admin

#### 1. Get All Products in Review Queue
```http
GET /api/product-reviews/admin/pending?page=0&size=20
Authorization: Required (Admin only)
```

**Response**: Paginated list of products in review queue

#### 2. Get Review Queue Count
```http
GET /api/product-reviews/admin/pending/count
Authorization: Required (Admin only)
```

**Response**: `42` (number of unresolved reviews)

#### 3. Take Action on Review
```http
POST /api/product-reviews/admin/{reviewId}/action
Authorization: Required (Admin only)
Content-Type: application/json

{
  "decision": "APPROVED",
  "reason": "Product meets all requirements",
  "adminNotes": "Good listing, approved"
}
```

**Response**: Updated product details

---

## 🔧 Configuration

### Scheduled Task
**Cron Expression**: `0 0 2 * * *` (Daily at 2 AM)

**To change timing**, edit `ProductService.java`:
```java
@Scheduled(cron = "0 0 2 * * *") // Change this
public void autoDeleteExpiredProducts() {
    // ...
}
```

### 14-Day Threshold
**To change the number of days**, edit `ProductService.java`:
```java
Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
// Change 14 to desired number of days
```

---

## 📊 Admin Dashboard Integration

### Recommended Dashboard Stats

Add to admin dashboard:
```
📋 Pending Reviews: 42
⏰ Oldest Review: 23 days
📈 Reviews Today: 5
✅ Approved Today: 12
❌ Rejected Today: 3
```

### Query Examples
```java
// Get count
long count = pendingReviewRepository.countByIsResolvedFalse();

// Get oldest review
ProductPendingReview oldest = pendingReviewRepository
    .findByIsResolvedFalse(PageRequest.of(0, 1, Sort.by("movedToReviewAt").ascending()))
    .getContent().get(0);
```

---

## 🧪 Testing Guide

### Test 1: Product Moved to Review
```bash
1. Create a product
2. Manually update createdAt to 15 days ago:
   UPDATE products SET created_at = NOW() - INTERVAL '15 days' 
   WHERE id = 'prod_123';
3. Run scheduled task manually:
   POST /api/admin/cleanup/expired-products
4. Check email inbox
5. Verify product_pending_reviews table has new record
6. Verify user received email
```

### Test 2: User Edits Product in Review
```bash
1. Get product in review: GET /api/product-reviews/my-pending
2. Note the productId
3. Edit product: PUT /api/products/{productId}
4. Verify product updated
5. Verify still in review queue
```

### Test 3: Admin Approves from Review
```bash
1. Get reviews: GET /api/product-reviews/admin/pending
2. Pick a review, note reviewId
3. Approve: POST /api/product-reviews/admin/{reviewId}/action
   {
     "decision": "APPROVED",
     "reason": "Looks good"
   }
4. Check seller's email
5. Verify product status = APPROVED
6. Verify review.isResolved = true
7. Verify product visible on marketplace
```

### Test 4: Admin Rejects from Review
```bash
1. Get reviews: GET /api/product-reviews/admin/pending
2. Reject: POST /api/product-reviews/admin/{reviewId}/action
   {
     "decision": "REJECTED",
     "reason": "Doesn't meet guidelines"
   }
3. Check seller's email
4. Verify product status = REJECTED
5. Wait for next day 2 AM OR trigger cleanup
6. Verify product deleted
```

---

## 📈 Benefits of New System

### For Users
✅ **Second chance** - Can improve listing before deletion  
✅ **Clear guidance** - Email explains what to improve  
✅ **Transparency** - Knows product is in review  
✅ **Control** - Can edit product while in review  

### For Admins
✅ **Better organization** - Separate queue for old listings  
✅ **Informed decisions** - Can see how long product has been pending  
✅ **Tracking** - All review actions logged  
✅ **Flexibility** - Can approve products after 14 days  

### For Platform
✅ **Reduced waste** - Fewer false deletions  
✅ **Better quality** - Users improve listings  
✅ **User retention** - Don't frustrate users with auto-delete  
✅ **Audit trail** - Complete record of review process  

---

## 🚀 Migration Notes

### Database Migration
```sql
-- Create new table
CREATE TABLE product_pending_reviews (
    -- ... see schema above
);

-- Add indexes for performance
CREATE INDEX idx_pending_reviews_product ON product_pending_reviews(product_id);
CREATE INDEX idx_pending_reviews_resolved ON product_pending_reviews(is_resolved);
CREATE INDEX idx_pending_reviews_moved_at ON product_pending_reviews(moved_to_review_at);
```

### Deployment Steps
1. ✅ Create database table
2. ✅ Deploy new code
3. ✅ Test scheduled task
4. ✅ Verify emails sending
5. ✅ Test admin workflow
6. ✅ Monitor logs

---

## 📝 Summary

### What Changed
- ❌ **Old**: Products auto-deleted after 14 days
- ✅ **New**: Products moved to review queue after 14 days

### Key Features
1. **Review Queue** - Products stored for admin review
2. **Email Notifications** - Users notified when moved to review
3. **Edit Capability** - Users can edit products in review
4. **Admin Control** - Admins approve or reject from queue
5. **Audit Trail** - All actions logged

### Workflow
```
PENDING (14 days) → REVIEW QUEUE → Admin Action → APPROVED or DELETED
```

---

## 📞 Support

### Common Issues

**Q: Product not moved to review queue?**  
A: Check if already in queue with `existsByProductIdAndIsResolvedFalse()`

**Q: User didn't receive email?**  
A: Check logs for email errors, verify SMTP configuration

**Q: How to manually trigger review queue?**  
A: Call `POST /api/admin/cleanup/expired-products`

**Q: Can user delete product in review?**  
A: Yes, using `DELETE /api/products/{productId}`

---

**Status**: ✅ **FULLY IMPLEMENTED**  
**Last Updated**: November 7, 2025  
**Version**: 2.0 (Review Queue System)
