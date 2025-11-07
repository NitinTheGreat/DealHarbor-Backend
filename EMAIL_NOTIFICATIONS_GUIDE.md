# Email Notification System - Complete Guide

## Overview
DealHarbor sends automated email notifications to users for important events, including product status updates, auto-deletions, and reminders.

---

## ✅ Confirmation: 14-Day Auto-Deletion Policy

### **YES, the 14-day time limit is ACTIVE and WORKING!**

**Location**: `ProductService.java` → `autoDeleteExpiredProducts()` method

**How it works**:
```java
@Scheduled(cron = "0 0 2 * * *") // Runs daily at 2:00 AM
public void autoDeleteExpiredProducts() {
    Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
    
    // Find products pending for more than 14 days
    List<Product> oldPendingProducts = productRepository.findByStatusAndCreatedAtBefore(
        ProductStatus.PENDING, fourteenDaysAgo
    );
    
    // Delete each old pending product
    // Also deletes all REJECTED products immediately
}
```

**Summary**:
- ✅ **Runs daily** at 2:00 AM server time
- ✅ **Deletes PENDING products** older than 14 days
- ✅ **Deletes ALL REJECTED products** (immediately)
- ✅ **Removes images** from Supabase S3
- ✅ **Updates seller statistics**
- ✅ **NOW SENDS EMAIL NOTIFICATIONS** to users

---

## 📧 Email Notifications Implemented

### 1. Product Rejection Email (Manual)
**Trigger**: Admin manually rejects a product  
**When**: Immediately when admin clicks "Reject"  
**File**: `AdminService.java` → `adminUpdateProduct()`

**Email Content**:
```
Subject: DealHarbor - Product Status Update

Hi [User Name],

Your product '[Product Title]' has been rejected.

Reason: [Admin's rejection reason]

Please review our guidelines and make necessary changes before resubmitting.

Best regards,
DealHarbor Team
```

**Code Location**:
```java
// AdminService.java line 133
if (request.getStatus() == ProductStatus.APPROVED || 
    request.getStatus() == ProductStatus.REJECTED) {
    emailService.sendProductStatusUpdate(
        product.getSeller().getEmail(),
        product.getSeller().getName(),
        product.getTitle(),
        request.getStatus().getDisplayName(),
        request.getReason()
    );
}
```

---

### 2. Product Auto-Deletion Email (NEW - Just Added)
**Trigger**: Product automatically deleted by scheduled task  
**When**: Daily at 2:00 AM (after deletion)  
**File**: `ProductService.java` → `autoDeleteExpiredProducts()`

#### For REJECTED Products:
```
Subject: DealHarbor - Product Automatically Removed

Hi [User Name],

Your product '[Product Title]' has been automatically removed from DealHarbor.

Reason: Your product was rejected by admin and has been automatically removed from the system.

Products that have been rejected by our admin team are automatically removed from the system.

What you can do:
• Review the rejection reason in your notifications
• Make necessary corrections
• Submit a new listing that follows our guidelines

Thank you for using DealHarbor!

Best regards,
DealHarbor Team
```

#### For OLD PENDING Products (14+ days):
```
Subject: DealHarbor - Product Automatically Removed

Hi [User Name],

Your product '[Product Title]' has been automatically removed from DealHarbor.

Reason: Your product has been pending approval for more than 14 days and has been automatically removed.

Products that remain pending for more than 14 days without admin approval are automatically deleted.

Listed on: [Creation Date]

What you can do:
• List your product again with better details
• Ensure all information is accurate and complete
• Add clear, high-quality images
• Follow our listing guidelines

Thank you for using DealHarbor!

Best regards,
DealHarbor Team
```

**Code Location**:
```java
// ProductService.java in autoDeleteExpiredProducts()
emailService.sendProductAutoDeletedNotification(
    seller.getEmail(),
    seller.getName(),
    productTitle,
    "Your product has been pending approval for more than 14 days...",
    product.getCreatedAt()
);
```

---

### 3. Product Pending Reminder Email (OPTIONAL - Available)
**Trigger**: Can be called to remind users about pending products  
**When**: Configurable (e.g., 11 days after listing)  
**File**: `EmailService.java` → `sendProductPendingReminder()`

**Email Content**:
```
Subject: DealHarbor - Product Pending Approval Reminder

Hi [User Name],

Your product '[Product Title]' has been pending approval for [X] days.

⚠️ IMPORTANT: Products pending for more than 14 days will be automatically deleted.

Days remaining: [Y] days

What you can do:
• Wait for admin approval (usually within 24-48 hours)
• Ensure your product listing is complete and accurate
• Check that all images are clear and relevant

If your product is not approved within [Y] days, it will be automatically removed 
and you'll need to list it again.

Best regards,
DealHarbor Team
```

**Usage** (optional - not currently scheduled):
```java
emailService.sendProductPendingReminder(
    seller.getEmail(),
    seller.getName(),
    product.getTitle(),
    3 // days remaining
);
```

---

## 🔔 Complete Email Flow Diagram

```
Product Created (PENDING)
        ↓
        ├─→ Admin Approves
        │   └─→ Email: "Product Approved" ✅
        │
        ├─→ Admin Rejects
        │   └─→ Email: "Product Rejected" ❌
        │   └─→ Next Day (2 AM): Auto-Delete
        │       └─→ Email: "Product Auto-Deleted (Rejected)" 🗑️
        │
        └─→ No Action for 14 Days
            └─→ Day 15 (2 AM): Auto-Delete
                └─→ Email: "Product Auto-Deleted (Pending Too Long)" ⏰
```

---

## 📝 Email Service Methods

### Available Email Methods in `EmailService.java`:

1. **`sendOtpEmail()`** - Registration OTP
2. **`sendForgotPasswordOtp()`** - Password reset OTP
3. **`sendEmailChangeOtp()`** - Email change verification
4. **`sendStudentVerificationOtp()`** - Student email verification
5. **`sendNotificationEmail()`** - General notifications
6. **`sendSecurityAlert()`** - Security alerts
7. **`sendLoginNotification()`** - Login alerts
8. **`sendAccountDeletionConfirmation()`** - Account deletion
9. **`sendStudentVerificationSuccess()`** - Student verification success
10. **`sendProductStatusUpdate()`** - Product approval/rejection ✅
11. **`sendAccountBanNotification()`** - Account suspension
12. **`sendProductAutoDeletedNotification()`** - Auto-deletion notice ✅ **NEW**
13. **`sendProductPendingReminder()`** - Pending reminder ✅ **NEW**

---

## 🛠️ Implementation Details

### Files Modified:

#### 1. `EmailService.java`
**Added Methods**:
- `sendProductAutoDeletedNotification()` - Notify users when products are auto-deleted
- `sendProductPendingReminder()` - Remind users about pending products

#### 2. `ProductService.java`
**Modified Method**: `autoDeleteExpiredProducts()`
- Added email notifications before deleting rejected products
- Added email notifications before deleting old pending products
- Added EmailService dependency injection

**Changes**:
```java
// Added dependency
private final EmailService emailService;

// In autoDeleteExpiredProducts()
try {
    emailService.sendProductAutoDeletedNotification(
        seller.getEmail(),
        seller.getName(),
        productTitle,
        "Appropriate reason message",
        product.getCreatedAt()
    );
    log.info("Sent auto-deletion email to {} for product: {}", 
             seller.getEmail(), productTitle);
} catch (Exception e) {
    log.error("Failed to send email notification: {}", e.getMessage());
}
```

---

## 🎯 Email Sending Logic

### When Product is Rejected by Admin:
1. Admin clicks "Reject" in admin portal
2. Product status changes to REJECTED
3. **Email sent immediately**: "Product Rejected" with reason
4. Notification created in database
5. Next day at 2 AM: Product auto-deleted
6. **Email sent again**: "Product Auto-Deleted (Rejected)"

### When Product Pending for 14+ Days:
1. Product created and status = PENDING
2. Admin doesn't approve/reject for 14 days
3. Day 15 at 2 AM: Scheduled task runs
4. **Email sent**: "Product Auto-Deleted (Pending Too Long)"
5. Product deleted from database
6. Images deleted from S3

---

## 🔍 Testing Email Notifications

### Test 1: Manual Rejection Email
```bash
1. Create a test product
2. Login as admin
3. Reject the product with a reason
4. Check seller's email inbox
5. Should receive "Product Status Update" email
```

### Test 2: Auto-Deletion Email (Rejected)
```bash
1. Create a test product
2. Login as admin and reject it
3. Wait for next day 2 AM OR trigger manually
4. Check seller's email inbox
5. Should receive "Product Automatically Removed" email
```

### Test 3: Auto-Deletion Email (14-Day Pending)
```bash
Option A: Change Date (Testing)
1. Create product in database with createdAt = 15 days ago
2. Run scheduled task manually OR wait for 2 AM
3. Check email

Option B: Trigger Manually
1. Call admin cleanup endpoint: POST /api/admin/cleanup/expired-products
2. Check logs and email
```

### Manual Trigger for Testing:
```bash
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/cleanup/expired-products" `
  -Method POST `
  -WebSession $adminSession
```

---

## 📊 Email Sending Statistics

### Email Success Tracking:
All email sends are logged in backend logs:

**Success**:
```log
INFO  ProductService : Sent auto-deletion email to user@example.com for rejected product: Laptop
```

**Failure**:
```log
ERROR ProductService : Failed to send email notification for rejected product prod_123: Connection timeout
```

### Error Handling:
- Email failures **DO NOT** stop product deletion
- Product is deleted even if email fails
- Errors are logged for admin review
- User can still see notification in their account

---

## ⚙️ Email Configuration

### SMTP Settings (`application.properties`):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Email Sending Service:
- Uses Spring `JavaMailSender`
- Configured in `EmailService.java`
- All emails sent as plain text (SimpleMailMessage)
- Can be enhanced with HTML templates later

---

## 🚀 Future Enhancements

### Potential Improvements:
1. **HTML Email Templates** - Rich formatting with images
2. **Email Preferences** - Users can opt-out of certain emails
3. **Bulk Email Notifications** - Send weekly summaries
4. **Email Tracking** - Track open rates and clicks
5. **Scheduled Reminders** - Day 11 reminder for pending products
6. **Digest Emails** - Daily/weekly summary of activity
7. **Customizable Templates** - Admin can edit email content
8. **Multi-language Support** - Emails in user's preferred language

### Recommended Schedule for Reminders:
```java
// Add to ProductService.java (optional)
@Scheduled(cron = "0 0 10 * * *") // Daily at 10 AM
public void sendPendingProductReminders() {
    Instant elevenDaysAgo = Instant.now().minus(11, ChronoUnit.DAYS);
    Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
    
    List<Product> products = productRepository.findByStatusAndCreatedAtBetween(
        ProductStatus.PENDING, fourteenDaysAgo, elevenDaysAgo
    );
    
    for (Product product : products) {
        long daysOld = ChronoUnit.DAYS.between(product.getCreatedAt(), Instant.now());
        int daysRemaining = 14 - (int) daysOld;
        
        emailService.sendProductPendingReminder(
            product.getSeller().getEmail(),
            product.getSeller().getName(),
            product.getTitle(),
            daysRemaining
        );
    }
}
```

---

## 📋 Summary

### ✅ What's Working Now:

1. **Manual Rejection Emails** ✅
   - Admin rejects → User gets email with reason
   - Already implemented and working

2. **Auto-Deletion Emails** ✅ **NEW**
   - Rejected products deleted → User gets email
   - 14-day pending products deleted → User gets email
   - Just implemented with this update

3. **14-Day Auto-Deletion** ✅ **CONFIRMED**
   - Runs daily at 2:00 AM
   - Deletes products pending >14 days
   - Already implemented and working

### 📧 Email Sending Summary:

| Event | Email Sent? | When | Template |
|-------|-------------|------|----------|
| Admin Rejects Product | ✅ Yes | Immediately | `sendProductStatusUpdate()` |
| Admin Approves Product | ✅ Yes | Immediately | `sendProductStatusUpdate()` |
| Rejected Product Auto-Deleted | ✅ Yes | 2 AM next day | `sendProductAutoDeletedNotification()` |
| Pending 14+ Days Auto-Deleted | ✅ Yes | 2 AM on day 15 | `sendProductAutoDeletedNotification()` |
| Pending 11 Days Reminder | ⏸️ Optional | Not scheduled | `sendProductPendingReminder()` |

---

## 🔧 Troubleshooting

### Email Not Received?
1. Check spam/junk folder
2. Verify SMTP configuration in `application.properties`
3. Check backend logs for email errors
4. Verify seller's email address is correct
5. Test SMTP connection manually

### Email Sent but Wrong Content?
1. Check email template in `EmailService.java`
2. Verify parameters passed correctly
3. Review logs for any parameter errors

### Auto-Deletion Not Triggering?
1. Verify `@EnableScheduling` on main application class
2. Check server timezone (cron runs in server time)
3. Review logs for "Starting automatic cleanup..." message
4. Manually trigger via admin endpoint to test

---

**Status**: ✅ **FULLY IMPLEMENTED**  
**Last Updated**: November 7, 2025  
**Email Notifications**: ACTIVE for both manual and automatic deletions
