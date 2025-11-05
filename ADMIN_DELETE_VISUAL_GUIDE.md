# Admin Portal Delete Feature - Visual Guide

## Delete Button Locations

### 1. Products Table View
```
┌─────────────────────────────────────────────────────────────────┐
│ DealHarbor Admin Portal                                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Filters: [All] [Pending] [Approved] [Rejected] [Sold]         │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ Image │ Product   │ Seller │ Price │ Status  │ Actions   │ │
│  ├───────┼───────────┼────────┼───────┼─────────┼───────────┤ │
│  │  📷   │ Laptop    │ John   │ ₹5000 │ PENDING │ [View]    │ │
│  │       │           │        │       │         │ [Approve] │ │
│  │       │           │        │       │         │ [Reject]  │ │
│  │       │           │        │       │         │ [Delete]  │ │ <-- HERE
│  ├───────┼───────────┼────────┼───────┼─────────┼───────────┤ │
│  │  📷   │ Textbook  │ Sarah  │ ₹300  │ APPROVED│ [View]    │ │
│  │       │           │        │       │         │ [Reject]  │ │
│  │       │           │        │       │         │ [Delete]  │ │ <-- HERE
│  ├───────┼───────────┼────────┼───────┼─────────┼───────────┤ │
│  │  📷   │ Phone     │ Mike   │ ₹8000 │ SOLD    │ [View]    │ │
│  │       │           │        │       │         │           │ │ <-- NO DELETE BUTTON
│  └───────┴───────────┴────────┴───────┴─────────┴───────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**Delete Button Properties:**
- Visible: PENDING, APPROVED, REJECTED products
- Hidden: SOLD products
- Style: Gray background (`bg-gray-800`)
- Position: Below other action buttons
- Layout: Flex wrap with gap-2 spacing

---

### 2. Product Detail Modal View
```
┌─────────────────────────────────────────────────────────────────┐
│                        Product Details                      ✕   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  [Product Image]                                                │
│                                                                  │
│  Laptop XPS 15                                                  │
│  Dell XPS 15 in excellent condition...                          │
│                                                                  │
│  Price: ₹45,000    Condition: Like New                          │
│  Seller: John Doe  Category: Electronics                        │
│  Status: PENDING   Views: 25                                    │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  [🗑️ Delete Product]        [Close] [Approve] [Reject]        │
│   ^                                                              │
│   |                                                              │
│   DELETE BUTTON HERE (left side)                                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Modal Delete Button Properties:**
- Position: Bottom-left corner of modal footer
- Visible: Non-SOLD products only
- Style: Gray background with emoji icon
- Text: "🗑️ Delete Product"
- Layout: Separated from other action buttons using `justify-between`

---

## Button State Matrix

| Product Status | Table Delete | Modal Delete | Notes                    |
|---------------|--------------|--------------|--------------------------|
| PENDING       | ✅ Visible   | ✅ Visible   | Full delete access       |
| APPROVED      | ✅ Visible   | ✅ Visible   | Can still delete         |
| REJECTED      | ✅ Visible   | ✅ Visible   | Manual cleanup option    |
| SOLD          | ❌ Hidden    | ❌ Hidden    | Protected from deletion  |

---

## Confirmation Flow Visualization

```
User Clicks "Delete"
        ↓
┌─────────────────────────────────────┐
│  ⚠️ PERMANENT DELETION WARNING      │
│                                     │
│  Product: Laptop XPS 15             │
│  Status: PENDING                    │
│                                     │
│  This will permanently delete:      │
│  • The product from database        │
│  • All product images from S3       │
│  • All associated records           │
│                                     │
│  This action CANNOT be undone!      │
│                                     │
│  [Cancel]  [OK]                     │
└─────────────────────────────────────┘
        ↓ (User clicks OK)
┌─────────────────────────────────────┐
│  FINAL CONFIRMATION                 │
│                                     │
│  Delete "Laptop XPS 15"?            │
│                                     │
│  Type OK in the next prompt         │
│                                     │
│  [Cancel]  [OK]                     │
└─────────────────────────────────────┘
        ↓ (User clicks OK)
┌─────────────────────────────────────┐
│  Type "DELETE" to confirm:          │
│                                     │
│  [ DELETE__________________ ]       │
│                                     │
│  [Cancel]  [OK]                     │
└─────────────────────────────────────┘
        ↓ (User types "DELETE")
    API Call to Backend
        ↓
┌─────────────────────────────────────┐
│  ✅ Product deleted successfully!   │
│                                     │
│  The product and all its images     │
│  have been permanently removed.     │
│                                     │
│  [OK]                               │
└─────────────────────────────────────┘
        ↓
  - Table refreshes
  - Dashboard stats update
  - Modal closes (if open)
```

---

## Color Coding & Styling

### Delete Buttons
```css
Background: bg-gray-800 (#1f2937)
Hover: bg-gray-900 (#111827)
Text: text-white
Size: text-xs (table), text-base (modal)
Padding: px-3 py-1 (table), px-6 py-2 (modal)
Border Radius: rounded / rounded-lg
```

### Other Action Buttons for Reference
```css
View:    bg-blue-600  → bg-blue-700  (Blue)
Approve: bg-green-600 → bg-green-700 (Green)
Reject:  bg-red-600   → bg-red-700   (Red)
Delete:  bg-gray-800  → bg-gray-900  (Dark Gray)
Close:   bg-gray-200  → bg-gray-300  (Light Gray)
```

---

## Responsive Behavior

### Desktop View (>768px)
```
Actions Column: [View] [Approve] [Reject] [Delete]
All buttons on same row, wrapped if needed
```

### Tablet View (768px - 1024px)
```
Actions Column: [View] [Approve]
                [Reject] [Delete]
Buttons wrap to multiple rows
```

### Mobile View (<768px)
```
Actions Column: [View]
                [Approve]
                [Reject]
                [Delete]
Each button on its own row
```

**CSS Used**: `flex flex-wrap gap-2`

---

## Quick Reference

### HTML IDs & Classes
- Modal Delete Button: `#modal-delete-btn`
- Approve Button: `.btn-approve`
- Reject Button: `.btn-reject`
- Modal Container: `#productModal`
- Modal Body: `#modalBody`

### JavaScript Functions
- `deleteProduct(productId, title, status)` - Main deletion logic
- `deleteProductFromModal()` - Modal wrapper
- `loadProducts()` - Refresh table
- `loadDashboardStats()` - Refresh stats
- `closeProductModal()` - Close modal

### API Endpoint
```
DELETE /api/products/{productId}
Authorization: Session cookie (credentials: include)
Response: 200 OK with success message
```

---

## Testing Scenarios

### ✅ Should Work
1. Delete PENDING product from table
2. Delete APPROVED product from modal
3. Delete REJECTED product from either location
4. Cancel deletion at any confirmation step
5. Type wrong text in final confirmation
6. Delete multiple products in sequence

### ❌ Should NOT Work
1. Delete SOLD product (button hidden)
2. Delete without proper authentication
3. Delete someone else's product (unless admin)
4. Bypass confirmation dialogs

---

## Success Indicators

After successful deletion:
- ✅ Product removed from table
- ✅ Dashboard statistics updated
- ✅ Modal closed (if open)
- ✅ Success message displayed
- ✅ Images deleted from S3
- ✅ Database records removed
- ✅ Admin action logged

---

**Layout Summary:**
- **Table**: Delete button in actions column (right side)
- **Modal**: Delete button in footer (left side)
- **Both**: Only visible for non-SOLD products
- **Styling**: Consistent dark gray theme
- **Safety**: Triple confirmation required

