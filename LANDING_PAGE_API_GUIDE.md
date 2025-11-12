# Landing Page API Guide

## Overview
This guide provides all the endpoints needed to create an immersive, Amazon-like landing page with animated sections, product discovery features, and engaging user experience.

---

## 🔥 How Trending Feature Works

### Trending Algorithm
The trending feature uses a **weighted engagement score** to rank products:

```
Trending Score = viewCount + (favoriteCount × 2)
```

**Why this works:**
- **View Count**: Tracks how many times users view a product (indicates interest)
- **Favorite Count (2x weight)**: Users favoriting shows stronger intent than just viewing
- **Time Filter**: Only considers products from last 7 days (keeps content fresh)
- **Sorted by**: Highest engagement score first, then by creation date

**Database Query:**
```sql
SELECT * FROM products 
WHERE status = 'APPROVED' AND created_at >= (NOW() - INTERVAL '7 days')
ORDER BY (view_count + favorite_count * 2) DESC, created_at DESC
```

---

## 📋 Landing Page Sections & Endpoints

### 1. Hero/Banner Section - Statistics
**Purpose:** Display impressive stats to build trust and showcase platform activity

**Endpoint:** `GET /api/products/homepage-stats`

**Response:**
```json
{
  "totalProducts": 1250,
  "totalActiveProducts": 1180,
  "totalUsers": 3420,
  "totalVerifiedStudents": 2890,
  "totalSellers": 456,
  "totalCategories": 12,
  "productsAddedToday": 23,
  "productsAddedThisWeek": 156,
  "mostPopularCategory": "Electronics",
  "mostPopularCategoryCount": 340
}
```

**UI Suggestions:**
- Animated counter effects for numbers
- Icons for each stat
- Grid layout (4 columns on desktop, 2 on tablet, 1 on mobile)
- Gradient background with glassmorphism effect

---

### 2. Trending Products Section
**Purpose:** Show hot/popular products with high engagement

**Endpoint:** `GET /api/products/trending?page=0&size=12`

**Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 12) - Items per page

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "iPhone 15 Pro Max",
      "price": 89999.00,
      "originalPrice": 134900.00,
      "condition": "LIKE_NEW",
      "viewCount": 1240,
      "favoriteCount": 89,
      "isFeatured": false,
      "primaryImage": {
        "imageUrl": "https://...",
        "altText": "iPhone 15"
      },
      "seller": {
        "id": "uuid",
        "name": "John Doe",
        "sellerRating": 4.8
      },
      "category": {
        "id": "uuid",
        "name": "Electronics"
      },
      "createdAt": "2025-11-10T10:30:00Z"
    }
  ],
  "totalPages": 5,
  "totalElements": 58,
  "size": 12,
  "number": 0
}
```

**UI Suggestions:**
- Horizontal carousel/slider
- Show "🔥 TRENDING" badge
- Display engagement metrics (views, favorites)
- Add "New" badge if created in last 3 days
- Smooth slide animations with dots navigation

---

### 3. Recent Arrivals Section
**Purpose:** Display newly added products

**Endpoint:** `GET /api/products/recent?page=0&size=12`

**Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 12) - Items per page

**Response:** Same structure as trending products

**UI Suggestions:**
- Grid layout (4 columns)
- "⏱️ JUST ADDED" badges
- Fade-in animation as you scroll
- Time ago indicator (e.g., "Added 2 hours ago")

---

### 4. Deals of the Day Section
**Purpose:** Highlight products with 20%+ discounts

**Endpoint:** `GET /api/products/deals?page=0&size=12`

**Calculation:**
```
Discount % = ((originalPrice - price) / originalPrice) × 100
Only shows products with discount >= 20%
```

**Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 12) - Items per page

**Response:** Same structure as trending products (includes `originalPrice` for discount calculation)

**UI Suggestions:**
- Red/orange color scheme
- Large discount percentage badges
- "⚡ LIMITED TIME" labels
- Countdown timer (optional)
- Pulse animation on discount badges
- Strike-through original price

---

### 5. Top Rated Products Section
**Purpose:** Show products from highly-rated sellers

**Endpoint:** `GET /api/products/top-rated?page=0&size=12`

**Sorting Logic:**
1. Seller rating (highest first)
2. Favorite count (most favorited)
3. View count (most viewed)

**Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 12) - Items per page

**UI Suggestions:**
- Display seller rating prominently (stars)
- "⭐ TOP SELLER" badges
- Trust indicators (verified badge, total sales)
- Professional card design

---

### 6. Shop by Category Section
**Purpose:** Category-based product discovery

**Endpoint:** `GET /api/products/by-category-preview?productsPerCategory=6`

**Parameters:**
- `productsPerCategory` (default: 6) - Number of products to show per category

**Response:**
```json
[
  {
    "categoryId": "uuid",
    "categoryName": "Electronics",
    "categoryIcon": "https://...",
    "categoryImage": "https://...",
    "totalProducts": 340,
    "products": [
      {
        "id": "uuid",
        "title": "MacBook Pro M3",
        "price": 159999.00,
        "primaryImage": {...},
        "seller": {...}
      }
      // ... 5 more products
    ]
  },
  {
    "categoryId": "uuid",
    "categoryName": "Fashion",
    "categoryIcon": "https://...",
    "categoryImage": "https://...",
    "totalProducts": 215,
    "products": [...]
  }
  // ... more categories
]
```

**UI Suggestions:**
- Each category has its own section
- Category header with icon/image and total count
- Horizontal scroll for products within category
- "View All" link to category page
- Different background colors per category

---

### 7. Advanced Search with Filters 🔍
**Purpose:** Powerful search and filtering system for product discovery

**Endpoint:** `POST /api/products/search`

**Method:** POST (for complex filter combinations)

**Request Body:**
```json
{
  "keyword": "iphone",
  "categoryId": "electronics-uuid",
  "minPrice": 10000,
  "maxPrice": 100000,
  "conditions": ["LIKE_NEW", "BRAND_NEW"],
  "location": "Mumbai",
  "deliveryAvailable": true,
  "isNegotiable": true,
  "verifiedStudentsOnly": false,
  "sortBy": "price_asc",
  "page": 0,
  "size": 20
}
```

**All Parameters (All Optional):**

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `keyword` | String | Search in title, description, brand | "macbook pro" |
| `categoryId` | String | Filter by category UUID | "electronics-uuid" |
| `minPrice` | Number | Minimum price filter | 10000 |
| `maxPrice` | Number | Maximum price filter | 100000 |
| `conditions` | Array | Product conditions | ["LIKE_NEW", "BRAND_NEW"] |
| `location` | String | Pickup location filter | "Mumbai" |
| `deliveryAvailable` | Boolean | Only products with delivery | true |
| `isNegotiable` | Boolean | Only negotiable products | true |
| `verifiedStudentsOnly` | Boolean | Only verified student sellers | true |
| `sortBy` | String | Sort order | "price_asc" |
| `page` | Number | Page number (0-indexed) | 0 |
| `size` | Number | Results per page | 20 |

**Sort Options:**
- `date_desc` - Newest first (default)
- `date_asc` - Oldest first
- `price_asc` - Lowest price first (for budget shoppers)
- `price_desc` - Highest price first
- `popular` - Most favorited products

**Product Conditions:**
- `BRAND_NEW` - Brand new, unused
- `LIKE_NEW` - Gently used, excellent condition
- `GOOD` - Normal wear, fully functional
- `FAIR` - Visible wear, works well
- `USED` - Well used but functional

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "iPhone 15 Pro Max 256GB",
      "description": "Barely used, pristine condition...",
      "price": 89999.00,
      "originalPrice": 134900.00,
      "isNegotiable": true,
      "condition": "LIKE_NEW",
      "brand": "Apple",
      "model": "iPhone 15 Pro Max",
      "viewCount": 245,
      "favoriteCount": 12,
      "pickupLocation": "Andheri, Mumbai",
      "deliveryAvailable": true,
      "primaryImage": {
        "imageUrl": "https://...",
        "altText": "iPhone 15"
      },
      "seller": {
        "id": "uuid",
        "name": "John Doe",
        "sellerRating": 4.8,
        "isVerifiedStudent": true,
        "totalListings": 15
      },
      "category": {
        "id": "uuid",
        "name": "Electronics"
      },
      "createdAt": "2025-11-10T10:30:00Z"
    }
  ],
  "totalPages": 8,
  "totalElements": 156,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false,
  "empty": false
}
```

---

### 8. Simple Keyword Search (Alternative)
**Purpose:** Quick search without filters (for simple search bars)

**Use the same POST endpoint with just keyword:**

```json
{
  "keyword": "laptop",
  "page": 0,
  "size": 20
}
```

**Or use GET for all products:**
`GET /api/products?page=0&size=20&sortBy=date_desc`

**What gets searched:**
- ✅ Product title
- ✅ Product description
- ✅ Brand name
- ✅ Only APPROVED status products (not pending/sold)

---

### 9. Featured Products
**Purpose:** Admin-curated featured products

**Endpoint:** `GET /api/products/featured?page=0&size=8`

**UI Suggestions:**
- Premium placement (maybe after hero section)
- "⭐ FEATURED" badges
- Larger card size
- Special background/border

---

### 10. Category Listing
**Endpoint:** `GET /api/categories`

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Electronics",
    "description": "Gadgets and electronic devices",
    "iconUrl": "https://...",
    "parentId": null,
    "sortOrder": 1,
    "isActive": true
  }
  // ... more categories
]
```

**UI Suggestions:**
- Grid of category cards with icons
- Hover effects (scale, shadow)
- Click to navigate to category page

---

## 🎨 Landing Page Layout Recommendation

```
┌─────────────────────────────────────────────┐
│  🏠 HERO SECTION                            │
│  - Large banner with PROMINENT search bar   │
│  - Quick filter chips (Under ₹10k, New, etc)│
│  - Homepage stats (animated counters)       │
│  - CTA buttons                              │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  🔥 TRENDING PRODUCTS (Carousel)            │
│  [Product] [Product] [Product] [Product]    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  📂 SHOP BY CATEGORY (Grid)                 │
│  [Electronics] [Fashion] [Books] [Sports]   │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  ⚡ DEALS OF THE DAY (Grid)                 │
│  [Product] [Product] [Product] [Product]    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  ⏱️ RECENT ARRIVALS (Grid)                  │
│  [Product] [Product] [Product] [Product]    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  ⭐ TOP RATED PRODUCTS (Grid)               │
│  [Product] [Product] [Product] [Product]    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  📦 CATEGORY PREVIEWS                       │
│  Electronics (340 products)                 │
│    [Product] [Product] [Product] → View All │
│                                             │
│  Fashion (215 products)                     │
│    [Product] [Product] [Product] → View All │
└─────────────────────────────────────────────┘
```

---

## 🔍 Search Results Page Layout

```
┌──────────────────────────────────────────────────────────────┐
│  Header: [Logo] [Search Bar] [Cart] [Profile]               │
└──────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│  🔍 Showing results for "laptop" - 156 products found        │
└──────────────────────────────────────────────────────────────┘

┌──────────────┬──────────────────────────────────────────────┐
│              │  Sort by: [Newest ▼]        [Grid] [List]    │
│   FILTERS    │                                               │
│              ├──────────────────────────────────────────────┤
│ 🗂️ Category  │  ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  □ All       │  │ Product │ │ Product │ │ Product │        │
│  ☑ Electronics│ │ Image   │ │ Image   │ │ Image   │        │
│  □ Fashion   │  │ ₹45,999 │ │ ₹52,000 │ │ ₹38,500 │        │
│  □ Books     │  └─────────┘ └─────────┘ └─────────┘        │
│              │                                               │
│ 💰 Price     │  ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  Min: [0]    │  │ Product │ │ Product │ │ Product │        │
│  Max: [100k] │  │ Image   │ │ Image   │ │ Image   │        │
│  [━━━━━━━━]  │  │ ₹29,999 │ │ ₹61,000 │ │ ₹44,500 │        │
│              │  └─────────┘ └─────────┘ └─────────┘        │
│ ✨ Condition │                                               │
│  ☑ Brand New │  ... more products ...                       │
│  ☑ Like New  │                                               │
│  □ Good      │  ┌─────────────────────────────────────┐     │
│  □ Fair      │  │  [1] [2] [3] ... [8] [Next →]      │     │
│  □ Used      │  └─────────────────────────────────────┘     │
│              │                                               │
│ 🚚 Delivery  │                                               │
│  ☑ Available │                                               │
│              │                                               │
│ 🎓 Seller    │                                               │
│  ☑ Verified  │                                               │
│    Students  │                                               │
│              │                                               │
│ [Clear All]  │                                               │
└──────────────┴──────────────────────────────────────────────┘
```

### Filter Panel UI Components

#### 1. **Category Filter**
```
🗂️ CATEGORY
─────────────
  All Categories
○ Electronics (245)
○ Fashion (180)
○ Books (95)
○ Sports (67)
○ Home & Living (120)
```

#### 2. **Price Range Slider**
```
💰 PRICE RANGE
──────────────
Min: ₹0          Max: ₹100,000

[━━━●━━━━━━━━━━━━━━━━━●━━]
₹10,000              ₹80,000
```

#### 3. **Condition Checkboxes**
```
✨ CONDITION
────────────
☑ Brand New (45)
☑ Like New (89)
□ Good (120)
□ Fair (78)
□ Used (156)
```

#### 4. **Quick Toggles**
```
🚚 DELIVERY
───────────
☑ Delivery Available

🎓 SELLER TYPE
──────────────
☑ Verified Students Only

💬 NEGOTIATION
──────────────
□ Negotiable Price Only
```

#### 5. **Active Filters Display**
```
Applied Filters:  [Electronics ✕] [₹10k-₹80k ✕] [Brand New ✕] [Clear All]
```

---

## 🎭 Animation Suggestions

### Page Load
1. **Hero Section**: Fade in from top (0.5s delay)
2. **Stats Counters**: Count up animation (1-2s)
3. **Sections**: Stagger fade-in as user scrolls

### Search & Filters
- **Search Input**: Expand width on focus
- **Filter Panel**: Slide in from left on mobile
- **Filter Toggle**: Smooth accordion expand/collapse
- **Active Filters**: Fade in with scale animation
- **Results Update**: Fade out old, fade in new (0.3s)
- **Price Slider**: Smooth thumb drag, debounced API call

### Product Cards
- **Hover**: Scale 1.05, lift shadow
- **Image**: Smooth zoom on hover
- **Badges**: Pulse animation for "New" and "Trending"
- **Favorite Button**: Heart pop animation on click

### Carousels
- **Transition**: Smooth slide with 0.3s ease
- **Auto-play**: Optional 5s interval
- **Infinite Loop**: Yes

### Loading States
- **Skeleton Loaders**: Show card outlines while loading
- **Progressive Loading**: Load above-fold content first

### Scroll Effects
- **Lazy Loading**: Load images as they enter viewport
- **Parallax**: Subtle parallax on hero section
- **Reveal**: Fade & slide up on scroll

---

## 🚀 Performance Optimization

### Caching
```javascript
// Cache homepage stats (5 minutes)
// Cache trending products (2 minutes)
// Cache category previews (10 minutes)
```

### Pagination
- Default page size: 12 products (mobile: 6)
- Infinite scroll or "Load More" button
- Prefetch next page

### Image Optimization
- Use CDN for product images
- Lazy load images below fold
- Responsive images (srcset)
- WebP format with fallback

### API Calls
```javascript
// Load in parallel on page load:
Promise.all([
  fetch('/api/products/homepage-stats'),
  fetch('/api/products/trending?size=12'),
  fetch('/api/products/deals?size=12'),
  fetch('/api/categories')
])
```

---

## 📱 Responsive Design

### Desktop (1200px+)
- 4 products per row
- Show all sections
- Carousels show 4-5 items

### Tablet (768px - 1199px)
- 3 products per row
- Carousels show 3 items

### Mobile (< 768px)
- 2 products per row (or 1 for large cards)
- Carousels show 1-2 items
- Stack sections vertically
- Sticky search bar

---

## 🔐 Authentication Context

Most endpoints are **public** (no auth required), but user-specific features require authentication:
- Favorites: Need to know which products user has favorited
- Personalized recommendations: Based on user history

**Authenticated Endpoints:**
- `GET /api/favorites` - User's favorite products
- `GET /api/products/{id}/is-favorite` - Check if user favorited

---

## 📊 Metrics to Track

### Product Engagement
- View count (tracked automatically when viewing product details)
- Favorite count (when user favorites a product)
- Click-through rate from each section

### Section Performance
- Which section gets most clicks?
- Time spent on each section
- Scroll depth

---

## 🔍 Implementing Search & Filters on Landing Page

### Search Bar Component

#### 1. **Hero Section Search Bar**
Place a prominent search bar in the hero section:

```jsx
// Example React Component
const HeroSearch = () => {
  const [keyword, setKeyword] = useState('');
  
  const handleSearch = async (e) => {
    e.preventDefault();
    
    const response = await fetch('/api/products/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        keyword: keyword,
        page: 0,
        size: 20,
        sortBy: 'date_desc'
      })
    });
    
    const data = await response.json();
    // Navigate to search results page or show modal
    router.push(`/search?q=${keyword}`);
  };
  
  return (
    <form onSubmit={handleSearch}>
      <input 
        type="text"
        placeholder="Search for products, brands, or categories..."
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
      />
      <button type="submit">🔍 Search</button>
    </form>
  );
};
```

#### 2. **Search Results Page with Filters**

```jsx
const SearchResultsPage = () => {
  const [filters, setFilters] = useState({
    keyword: '',
    categoryId: null,
    minPrice: null,
    maxPrice: null,
    conditions: [],
    deliveryAvailable: null,
    verifiedStudentsOnly: false,
    sortBy: 'date_desc',
    page: 0,
    size: 20
  });
  
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  
  const fetchResults = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/products/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(filters)
      });
      const data = await response.json();
      setResults(data);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchResults();
  }, [filters]);
  
  return (
    <div className="search-page">
      {/* Sidebar Filters */}
      <aside className="filters-sidebar">
        {/* Category Filter */}
        <FilterSection title="Category">
          <CategorySelect 
            value={filters.categoryId}
            onChange={(id) => setFilters({...filters, categoryId: id})}
          />
        </FilterSection>
        
        {/* Price Range Filter */}
        <FilterSection title="Price Range">
          <PriceRangeSlider
            min={filters.minPrice}
            max={filters.maxPrice}
            onChange={(min, max) => setFilters({...filters, minPrice: min, maxPrice: max})}
          />
        </FilterSection>
        
        {/* Condition Filter */}
        <FilterSection title="Condition">
          <CheckboxGroup
            options={['BRAND_NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'USED']}
            selected={filters.conditions}
            onChange={(conditions) => setFilters({...filters, conditions})}
          />
        </FilterSection>
        
        {/* Delivery Available */}
        <FilterSection title="Delivery">
          <Checkbox
            label="Delivery Available"
            checked={filters.deliveryAvailable}
            onChange={(checked) => setFilters({...filters, deliveryAvailable: checked})}
          />
        </FilterSection>
        
        {/* Verified Students Only */}
        <FilterSection title="Seller">
          <Checkbox
            label="Verified Students Only"
            checked={filters.verifiedStudentsOnly}
            onChange={(checked) => setFilters({...filters, verifiedStudentsOnly: checked})}
          />
        </FilterSection>
        
        {/* Clear Filters Button */}
        <button onClick={() => setFilters({...filters, categoryId: null, minPrice: null, maxPrice: null, conditions: [], deliveryAvailable: null})}>
          Clear All Filters
        </button>
      </aside>
      
      {/* Results Section */}
      <main className="results-section">
        {/* Sort Dropdown */}
        <div className="results-header">
          <p>{results?.totalElements || 0} products found</p>
          <select 
            value={filters.sortBy}
            onChange={(e) => setFilters({...filters, sortBy: e.target.value})}
          >
            <option value="date_desc">Newest First</option>
            <option value="date_asc">Oldest First</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
            <option value="popular">Most Popular</option>
          </select>
        </div>
        
        {/* Product Grid */}
        {loading ? (
          <SkeletonLoader count={20} />
        ) : (
          <div className="product-grid">
            {results?.content?.map(product => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
        
        {/* Pagination */}
        <Pagination
          currentPage={filters.page}
          totalPages={results?.totalPages || 0}
          onPageChange={(page) => setFilters({...filters, page})}
        />
      </main>
    </div>
  );
};
```

#### 3. **Quick Filters on Landing Page**

Add filter chips below the search bar:

```jsx
const QuickFilters = () => {
  const navigate = useNavigate();
  
  const applyFilter = (filterName, filterValue) => {
    navigate('/search', { 
      state: { 
        filters: { [filterName]: filterValue } 
      } 
    });
  };
  
  return (
    <div className="quick-filters">
      <button onClick={() => applyFilter('minPrice', 0) && applyFilter('maxPrice', 10000)}>
        🏷️ Under ₹10,000
      </button>
      <button onClick={() => applyFilter('conditions', ['BRAND_NEW'])}>
        ✨ Brand New Only
      </button>
      <button onClick={() => applyFilter('deliveryAvailable', true)}>
        🚚 Free Delivery
      </button>
      <button onClick={() => applyFilter('verifiedStudentsOnly', true)}>
        🎓 Verified Students
      </button>
      <button onClick={() => applyFilter('sortBy', 'popular')}>
        🔥 Popular
      </button>
    </div>
  );
};
```

#### 4. **Category-Based Search**

When user clicks a category, navigate with filters:

```jsx
const CategoryCard = ({ category }) => {
  const handleClick = () => {
    fetch('/api/products/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        categoryId: category.id,
        page: 0,
        size: 20
      })
    }).then(res => res.json())
      .then(data => {
        // Show results
      });
  };
  
  return (
    <div onClick={handleClick}>
      <img src={category.iconUrl} alt={category.name} />
      <h3>{category.name}</h3>
    </div>
  );
};
```

---

## 🎯 Quick Implementation Checklist

### Core Features
- [ ] Hero section with animated stats
- [ ] **Prominent search bar in hero section**
- [ ] **Quick filter chips below search**
- [ ] Trending products carousel
- [ ] Category grid with icons (clickable for filtered search)
- [ ] Deals of the day section
- [ ] Recent arrivals grid
- [ ] Top-rated products section
- [ ] Category preview sections

### Search & Filter Features
- [ ] **Advanced search page with sidebar filters**
- [ ] **Category filter dropdown**
- [ ] **Price range slider (min/max)**
- [ ] **Condition checkboxes**
- [ ] **Delivery available toggle**
- [ ] **Verified students toggle**
- [ ] **Sort dropdown (date, price, popularity)**
- [ ] **Clear all filters button**
- [ ] **Active filter chips display**
- [ ] **Results count display**

### UX Enhancements
- [ ] Responsive design
- [ ] Loading states & skeletons
- [ ] Error handling
- [ ] Infinite scroll / pagination
- [ ] Image lazy loading
- [ ] API response caching
- [ ] Performance monitoring
- [ ] **Search suggestions/autocomplete (optional)**
- [ ] **Recent searches (optional)**
- [ ] **Filter persistence in URL params**

---

## 🐛 Testing the Endpoints

### 1. Start the application
```bash
.\mvnw spring-boot:run
```

### 2. Test with curl or Postman

**Get Homepage Stats:**
```bash
curl http://localhost:8080/api/products/homepage-stats
```

**Get Trending Products:**
```bash
curl http://localhost:8080/api/products/trending?size=12
```

**Get Deals:**
```bash
curl http://localhost:8080/api/products/deals?size=12
```

**Get Category Previews:**
```bash
curl http://localhost:8080/api/products/by-category-preview?productsPerCategory=6
```

**Simple Keyword Search:**
```bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "iphone",
    "page": 0,
    "size": 20
  }'
```

**Advanced Search with Filters:**
```bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "laptop",
    "categoryId": "electronics-uuid",
    "minPrice": 30000,
    "maxPrice": 80000,
    "conditions": ["LIKE_NEW", "BRAND_NEW"],
    "deliveryAvailable": true,
    "sortBy": "price_asc",
    "page": 0,
    "size": 20
  }'
```

**Filter by Category Only:**
```bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "electronics-uuid",
    "sortBy": "date_desc",
    "page": 0,
    "size": 20
  }'
```

**Filter by Price Range:**
```bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 0,
    "maxPrice": 10000,
    "sortBy": "price_asc",
    "page": 0,
    "size": 20
  }'
```

**Verified Students Only:**
```bash
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "verifiedStudentsOnly": true,
    "sortBy": "date_desc",
    "page": 0,
    "size": 20
  }'
```

---

## 💡 Real-World Search Scenarios

### Scenario 1: Budget Shopper
**User wants:** Cheap electronics under ₹10,000 with delivery

```json
POST /api/products/search
{
  "categoryId": "electronics-uuid",
  "maxPrice": 10000,
  "deliveryAvailable": true,
  "sortBy": "price_asc",
  "page": 0,
  "size": 20
}
```

### Scenario 2: Quality Hunter
**User wants:** Brand new or like-new iPhones from verified students

```json
POST /api/products/search
{
  "keyword": "iphone",
  "conditions": ["BRAND_NEW", "LIKE_NEW"],
  "verifiedStudentsOnly": true,
  "sortBy": "date_desc",
  "page": 0,
  "size": 20
}
```

### Scenario 3: Deal Seeker
**User wants:** Discounted laptops under ₹50,000

```json
POST /api/products/search
{
  "keyword": "laptop",
  "maxPrice": 50000,
  "sortBy": "price_asc",
  "page": 0,
  "size": 20
}
```
Then cross-reference with `/api/products/deals` for best deals.

### Scenario 4: Local Pickup
**User wants:** Products in specific location (Mumbai)

```json
POST /api/products/search
{
  "location": "Mumbai",
  "sortBy": "date_desc",
  "page": 0,
  "size": 20
}
```

### Scenario 5: Negotiable Items
**User wants:** Items open to negotiation

```json
POST /api/products/search
{
  "isNegotiable": true,
  "sortBy": "date_desc",
  "page": 0,
  "size": 20
}
```

---

## 💡 Additional Features to Consider

1. **Personalized Recommendations** (if user logged in)
2. **Location-based Products** (nearby sellers)
3. **Price Drop Alerts**
4. **Wishlist Sharing**
5. **Recently Viewed Products**
6. **Similar Products**
7. **Seller of the Week**
8. **Student Deals** (special section for verified students)

---

## 🎉 Summary

### ✅ Core Landing Page Endpoints (6)
1. **Homepage Stats** (`GET /homepage-stats`) - Trust indicators with metrics
2. **Trending Products** (`GET /trending`) - High engagement products (viewCount + favoriteCount×2)
3. **Recent Arrivals** (`GET /recent`) - Fresh content, newest first
4. **Deals of the Day** (`GET /deals`) - Discounted items (20%+ off)
5. **Top Rated Products** (`GET /top-rated`) - Quality sellers, sorted by rating
6. **Category Previews** (`GET /by-category-preview`) - Products grouped by category

### ✅ Search & Discovery Endpoints (3)
7. **Advanced Search** (`POST /search`) - Comprehensive filtering system with:
   - ✅ Keyword search (title, description, brand)
   - ✅ Category filter
   - ✅ Price range (min/max)
   - ✅ Condition filter (5 types)
   - ✅ Location filter
   - ✅ Delivery available toggle
   - ✅ Verified students toggle
   - ✅ Negotiable items filter
   - ✅ Multiple sort options (date, price, popularity)
   
8. **Featured Products** (`GET /featured`) - Admin-curated products
9. **Category Listing** (`GET /categories`) - All categories with icons

### ✅ Product Detail Endpoint (1)
10. **Product Details** (`GET /{productId}`) - Full product information (auto-increments view count)

---

## 📊 Feature Comparison Table

| Feature | Endpoint | Filters | Sort | Pagination | Auth Required |
|---------|----------|---------|------|------------|---------------|
| Homepage Stats | GET /homepage-stats | ❌ | ❌ | ❌ | ❌ |
| Trending | GET /trending | ❌ | ❌ | ✅ | ❌ |
| Recent | GET /recent | ❌ | ❌ | ✅ | ❌ |
| Deals | GET /deals | ❌ | ❌ | ✅ | ❌ |
| Top Rated | GET /top-rated | ❌ | ❌ | ✅ | ❌ |
| Category Preview | GET /by-category-preview | ❌ | ❌ | ⚠️ Per-category | ❌ |
| **Advanced Search** | **POST /search** | **✅ 9 filters** | **✅ 5 options** | **✅** | **❌** |
| Featured | GET /featured | ❌ | ❌ | ✅ | ❌ |
| Categories | GET /categories | ❌ | ✅ | ❌ | ❌ |
| Product Details | GET /{id} | ❌ | ❌ | ❌ | ❌ |

---

## 🚀 What You Can Build

### 1. **Amazon-Style Homepage**
- Hero with stats and search
- Trending carousel
- Category grid
- Multiple product sections
- Personalized recommendations

### 2. **Advanced Search Page**
- Sidebar filters (collapsible on mobile)
- Real-time filter updates
- Sort dropdown
- Grid/List view toggle
- Infinite scroll or pagination

### 3. **Category Pages**
- Category banner
- Sub-category navigation
- Filtered product grid
- Category-specific deals

### 4. **Mobile-First Experience**
- Bottom sheet filters
- Sticky search bar
- Quick filter chips
- Touch-optimized sliders

---

## 🎯 Key Advantages

### Performance
- ✅ **Optimized queries** with proper indexing
- ✅ **Pagination** on all list endpoints
- ✅ **Filtered by APPROVED status** (no pending/deleted products)
- ✅ **Single POST endpoint** for all search/filter combinations

### User Experience
- ✅ **Multiple discovery paths** (search, browse, trending, deals)
- ✅ **Flexible filtering** (combine any filters)
- ✅ **Smart sorting** (date, price, popularity)
- ✅ **Student-focused** (verified student filter)
- ✅ **Location-aware** (pickup location filter)

### Developer Experience
- ✅ **RESTful API design**
- ✅ **Consistent response format** (PagedResponse)
- ✅ **Optional parameters** (all filters are optional)
- ✅ **No authentication required** for public endpoints
- ✅ **Clear documentation** with examples

---

All endpoints are **production-ready**, **optimized**, **paginated**, and ready for an **immersive, animated frontend**! 🚀

**Ready to build the next big student marketplace! 🎓🛒**
