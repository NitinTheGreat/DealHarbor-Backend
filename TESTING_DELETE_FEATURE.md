# 🚀 Quick Start - Testing Delete Feature

## Prerequisites
- Backend running on `http://localhost:8080`
- Admin account created and logged in
- At least one test product in database

---

## Step-by-Step Testing Guide

### 1️⃣ Access Admin Portal
```
1. Open browser
2. Navigate to: http://localhost:8080/admin.html
3. Login with admin credentials
4. You should see the dashboard
```

### 2️⃣ Test Delete from Products Table

#### Option A: Delete PENDING Product
```
1. Click "Products" in sidebar (if not already there)
2. Click "Pending" filter button
3. Find a PENDING product in the table
4. You should see these buttons in Actions column:
   - [View] [Approve] [Reject] [Delete]
5. Click "Delete" button
6. Follow confirmation prompts:
   - First dialog: Read warning, click OK
   - Second dialog: Click OK again
   - Third prompt: Type "DELETE" exactly
7. Success message should appear
8. Product should disappear from table
9. Dashboard stats should update
```

#### Option B: Delete APPROVED Product
```
1. Click "Approved" filter button
2. Find an APPROVED product
3. You should see: [View] [Reject] [Delete]
4. Click "Delete" and follow same confirmation flow
5. Product should be removed
```

#### Option C: Try to Delete SOLD Product (Should Fail)
```
1. Click "Sold" filter button
2. Find a SOLD product
3. You should NOT see a Delete button
4. This is correct - SOLD products are protected
```

### 3️⃣ Test Delete from Product Modal

```
1. Click "View" button on any non-SOLD product
2. Product detail modal should open
3. Look at bottom of modal:
   - Left side: "🗑️ Delete Product" button (gray)
   - Right side: Close, Approve, Reject buttons
4. Click "🗑️ Delete Product"
5. Follow same confirmation flow
6. Modal should close after successful deletion
7. Table should refresh automatically
```

### 4️⃣ Test Confirmation Cancellation

#### Cancel at First Step
```
1. Click any Delete button
2. In first warning dialog, click "Cancel"
3. Nothing should happen - product still there
```

#### Cancel at Second Step
```
1. Click Delete
2. Click OK on first warning
3. Click Cancel on second confirmation
4. Product should remain
```

#### Type Wrong Text
```
1. Click Delete
2. Click OK twice
3. When prompted for "DELETE", type something else
4. Should show: "Deletion cancelled - confirmation text did not match"
5. Product should remain
```

### 5️⃣ Test Cleanup Endpoints (Optional)

#### Get Cleanup Statistics
```bash
# Open PowerShell and run:
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$cookie = New-Object System.Net.Cookie
$cookie.Name = "JSESSIONID"
$cookie.Value = "YOUR_SESSION_ID"
$cookie.Domain = "localhost"
$session.Cookies.Add($cookie)

Invoke-RestMethod -Uri "http://localhost:8080/api/admin/cleanup/stats" `
  -Method GET `
  -WebSession $session
```

#### Manual Cleanup Trigger
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/cleanup/expired-products" `
  -Method POST `
  -WebSession $session
```

---

## Expected Behavior Summary

### ✅ Should Work
| Action | Expected Result |
|--------|-----------------|
| Delete PENDING product | Deleted successfully |
| Delete APPROVED product | Deleted successfully |
| Delete REJECTED product | Deleted successfully |
| Delete from table | Refreshes table, updates stats |
| Delete from modal | Closes modal, refreshes table |
| Cancel at any step | Product remains, no changes |
| Type wrong text | Cancellation message, product remains |

### ❌ Should NOT Work
| Action | Expected Result |
|--------|-----------------|
| Delete SOLD product | No delete button shown |
| Skip confirmations | Not possible - all required |
| Delete without login | Redirect to login page |

---

## Visual Verification Checklist

### Products Table
- [ ] Delete button has gray background
- [ ] Delete button appears for PENDING products
- [ ] Delete button appears for APPROVED products
- [ ] Delete button appears for REJECTED products
- [ ] Delete button DOES NOT appear for SOLD products
- [ ] Buttons wrap properly on narrow screens
- [ ] All buttons have hover effects

### Product Modal
- [ ] Delete button in bottom-left corner
- [ ] Delete button has trash emoji (🗑️)
- [ ] Delete button separated from other actions
- [ ] Button shows/hides based on product status
- [ ] Button styled consistently with table version

### Confirmation Dialogs
- [ ] First dialog shows ⚠️ warning emoji
- [ ] Product title and status displayed
- [ ] Bullet list of what will be deleted
- [ ] "CANNOT be undone" message visible
- [ ] Second dialog asks for confirmation
- [ ] Third prompt requires "DELETE" text
- [ ] Success message shows ✅ emoji
- [ ] Error messages show ❌ emoji

---

## Quick Test Script

Copy-paste this checklist and mark items as you test:

```
🧪 DELETE FEATURE TEST CHECKLIST

Table View Tests:
[ ] 1. Open admin portal - logged in successfully
[ ] 2. See products table with data
[ ] 3. PENDING product has Delete button
[ ] 4. APPROVED product has Delete button
[ ] 5. REJECTED product has Delete button
[ ] 6. SOLD product has NO Delete button
[ ] 7. Click Delete on PENDING product
[ ] 8. First warning dialog appears with ⚠️
[ ] 9. Second confirmation dialog appears
[ ] 10. Text input prompt requires "DELETE"
[ ] 11. Success message shows ✅
[ ] 12. Product removed from table
[ ] 13. Dashboard stats updated

Modal View Tests:
[ ] 14. Click View on any product
[ ] 15. Modal opens with product details
[ ] 16. Delete button visible bottom-left
[ ] 17. Button has 🗑️ emoji
[ ] 18. Click Delete button
[ ] 19. Same confirmation flow works
[ ] 20. Modal closes after deletion
[ ] 21. Table refreshes automatically

Cancellation Tests:
[ ] 22. Click Delete, cancel at step 1 - product remains
[ ] 23. Click Delete, cancel at step 2 - product remains
[ ] 24. Click Delete, type wrong text - product remains
[ ] 25. Cancellation message displayed correctly

Edge Cases:
[ ] 26. Try to delete without login - redirects
[ ] 27. Delete multiple products in sequence - works
[ ] 28. Check browser console - no errors
[ ] 29. Check backend logs - deletions logged
[ ] 30. Verify images deleted from Supabase

✅ ALL TESTS PASSED: Ready for production
❌ TESTS FAILED: Review errors and fix issues
```

---

## Troubleshooting Quick Fixes

### Delete Button Not Showing
```javascript
// Open browser console (F12) and check:
console.log(product.status); // Should NOT be "SOLD"
```

### Confirmation Not Working
```javascript
// Check browser console for errors
// Verify deleteProduct function exists:
console.log(typeof deleteProduct); // Should be "function"
```

### Backend Errors
```powershell
# Check backend logs
Get-Content logs\dealharbor-backend.log -Tail 50
```

### Images Not Deleted
```bash
# Check Supabase configuration in application.properties
supabase.storage.url=https://YOUR_PROJECT.supabase.co/storage/v1/object
supabase.storage.bucket=product-images
supabase.storage.api-key=YOUR_API_KEY
```

---

## Performance Testing

### Load Test (Optional)
```bash
# Test deleting 10 products rapidly
# All should complete successfully within 30 seconds
```

### Scheduled Cleanup Test
```bash
# Wait for next 2 AM or temporarily change cron expression
# Check logs for "Starting automatic cleanup..."
# Verify products deleted and logged
```

---

## Success Indicators

After testing, you should see:

✅ **UI Working**
- Delete buttons appear correctly
- Confirmations work smoothly
- Success/error messages display
- Table refreshes automatically
- Modal behavior correct

✅ **Backend Working**
- Products deleted from database
- Images deleted from Supabase S3
- Seller stats updated
- Deletions logged
- Admin actions recorded

✅ **Integration Working**
- Frontend calls correct API
- Authentication works
- Authorization enforced
- Error handling graceful

---

## Next Steps After Testing

1. **All Tests Pass** ✅
   - Mark feature as complete
   - Deploy to production
   - Monitor for issues
   - Collect user feedback

2. **Some Tests Fail** ��️
   - Document failing tests
   - Check error messages
   - Review implementation
   - Fix issues and retest

3. **Major Issues** ❌
   - Do not deploy
   - Create bug report
   - Review documentation
   - Debug with developer

---

## Quick Command Reference

### Start Backend
```powershell
cd DealHarbor-Backend
.\mvnw spring-boot:run
```

### Access Admin Portal
```
http://localhost:8080/admin.html
```

### Check Logs
```powershell
Get-Content logs\dealharbor-backend.log -Wait
```

### Test API Directly
```powershell
# Delete product via API
Invoke-RestMethod -Uri "http://localhost:8080/api/products/123" `
  -Method DELETE `
  -WebSession $session
```

---

## Support

If you encounter issues:
1. Check browser console (F12 → Console tab)
2. Check backend logs
3. Review PRODUCT_DELETION_GUIDE.md
4. Review ADMIN_PORTAL_DELETE_FEATURE.md
5. Check Supabase dashboard for images

---

**Happy Testing!** 🚀

*Estimated testing time: 15-20 minutes*
*Required role: Admin*
*Browser: Any modern browser (Chrome, Firefox, Edge)*
