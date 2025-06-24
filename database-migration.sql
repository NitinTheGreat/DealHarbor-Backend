-- Drop existing tables to start fresh
DROP TABLE IF EXISTS admin_actions CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS security_events CASCADE;
DROP TABLE IF EXISTS user_sessions CASCADE;
DROP TABLE IF EXISTS login_attempts CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS otp_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table with enhanced seller badge system
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Profile information
    profile_photo_url VARCHAR(500) DEFAULT '/api/images/default-avatar.png',
    bio VARCHAR(500),
    phone_number VARCHAR(20),
    
    -- University information
    university_id VARCHAR(100),
    graduation_year INTEGER,
    department VARCHAR(100),
    is_verified_student BOOLEAN DEFAULT FALSE,
    
    -- OAuth fields
    google_id VARCHAR(100),
    github_id VARCHAR(100),
    provider VARCHAR(20) DEFAULT 'LOCAL',
    
    -- Seller Performance & Badge System
    seller_rating DECIMAL(3,2) DEFAULT 0.00,
    buyer_rating DECIMAL(3,2) DEFAULT 0.00,
    total_sales INTEGER DEFAULT 0,
    total_purchases INTEGER DEFAULT 0,
    total_listings INTEGER DEFAULT 0,
    active_listings INTEGER DEFAULT 0,
    seller_badge VARCHAR(20) DEFAULT 'NEW_SELLER',
    first_sale_at TIMESTAMP,
    total_revenue DECIMAL(10,2) DEFAULT 0.00,
    response_rate DECIMAL(5,2) DEFAULT 0.00,
    positive_reviews INTEGER DEFAULT 0,
    negative_reviews INTEGER DEFAULT 0,
    
    -- Account status
    is_banned BOOLEAN DEFAULT FALSE,
    banned_until TIMESTAMP,
    ban_reason VARCHAR(500),
    
    -- Security fields
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    
    -- Account verification
    email_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Account status
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Create categories table
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id VARCHAR(36),
    icon_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Create products table
CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    is_negotiable BOOLEAN DEFAULT FALSE,
    
    -- Product details
    condition VARCHAR(20) DEFAULT 'USED',
    brand VARCHAR(100),
    model VARCHAR(100),
    
    -- Categorization
    category_id VARCHAR(36) NOT NULL,
    tags TEXT,
    
    -- Seller info
    seller_id VARCHAR(36) NOT NULL,
    
    -- Status management
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_notes TEXT,
    approved_by VARCHAR(36),
    approved_at TIMESTAMP,
    
    -- Visibility & metrics
    view_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    
    -- Location
    pickup_location VARCHAR(200),
    delivery_available BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Create product_images table
CREATE TABLE product_images (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    file_size INTEGER,
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create orders table
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE NOT NULL,
    
    -- Parties involved
    buyer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    
    -- Order details
    product_title VARCHAR(200) NOT NULL,
    agreed_price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2) NOT NULL,
    
    -- Status tracking
    status VARCHAR(20) DEFAULT 'PENDING',
    
    -- Communication
    buyer_notes TEXT,
    seller_notes TEXT,
    
    -- Delivery info
    pickup_location VARCHAR(200),
    delivery_method VARCHAR(50) DEFAULT 'PICKUP',
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create admin_actions table
CREATE TABLE admin_actions (
    id VARCHAR(36) PRIMARY KEY,
    admin_id VARCHAR(36) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    reason TEXT,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- Recreate existing tables
CREATE TABLE otp_tokens (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE login_attempts (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    successful BOOLEAN NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    user_agent TEXT
);

CREATE TABLE user_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    refresh_token VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    device_info VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE security_events (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    description TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted ON users(deleted);
CREATE INDEX idx_users_seller_badge ON users(seller_badge);
CREATE INDEX idx_users_total_sales ON users(total_sales);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(is_active);
CREATE INDEX idx_categories_sort ON categories(sort_order);

CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_created_at ON products(created_at DESC);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_status_category ON products(status, category_id);
CREATE INDEX idx_products_featured ON products(is_featured);

CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_images_primary ON product_images(is_primary);

CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_seller ON orders(seller_id);
CREATE INDEX idx_orders_product ON orders(product_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_number ON orders(order_number);

CREATE INDEX idx_admin_actions_admin ON admin_actions(admin_id);
CREATE INDEX idx_admin_actions_target ON admin_actions(target_type, target_id);
CREATE INDEX idx_admin_actions_created_at ON admin_actions(created_at DESC);

CREATE INDEX idx_login_attempts_email ON login_attempts(email);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_security_events_user_id ON security_events(user_id);

-- Insert sample categories
INSERT INTO categories (id, name, description, icon_url, sort_order, created_at, updated_at) VALUES
('cat-1', 'Books & Study Materials', 'Textbooks, notes, study guides', '/icons/books.png', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-2', 'Electronics', 'Laptops, phones, gadgets', '/icons/electronics.png', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-3', 'Furniture', 'Dorm furniture, study desks, chairs', '/icons/furniture.png', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-4', 'Clothing & Accessories', 'Clothes, bags, accessories', '/icons/clothing.png', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-5', 'Sports & Recreation', 'Sports equipment, games', '/icons/sports.png', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-6', 'Vehicles', 'Bikes, scooters, cars', '/icons/vehicles.png', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-7', 'Services', 'Tutoring, repairs, other services', '/icons/services.png', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-8', 'Miscellaneous', 'Everything else', '/icons/misc.png', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert subcategories for Books
INSERT INTO categories (id, name, description, parent_id, sort_order, created_at, updated_at) VALUES
('cat-1-1', 'Computer Science', 'CS textbooks and materials', 'cat-1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-1-2', 'Engineering', 'Engineering textbooks', 'cat-1', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-1-3', 'Business', 'Business and management books', 'cat-1', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-1-4', 'Literature', 'Literature and language books', 'cat-1', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert subcategories for Electronics
INSERT INTO categories (id, name, description, parent_id, sort_order, created_at, updated_at) VALUES
('cat-2-1', 'Laptops & Computers', 'Laptops, desktops, accessories', 'cat-2', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-2-2', 'Mobile Phones', 'Smartphones and accessories', 'cat-2', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-2-3', 'Audio & Video', 'Headphones, speakers, cameras', 'cat-2', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat-2-4', 'Gaming', 'Gaming consoles, games, accessories', 'cat-2', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create test user with enhanced seller profile
INSERT INTO users (
    id, email, password_hash, name, role, enabled, locked, email_verified,
    seller_rating, total_sales, seller_badge, profile_photo_url, provider,
    created_at, updated_at
) VALUES (
    'test-user-1', 'test@dealharbor.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test User', 'USER', TRUE, FALSE, TRUE,
    4.5, 25, 'TRUSTED_SELLER', '/api/images/default-avatar.png', 'LOCAL',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Create admin user
INSERT INTO users (
    id, email, password_hash, name, role, enabled, locked, email_verified,
    profile_photo_url, provider, created_at, updated_at
) VALUES (
    'admin-user-1', 'admin@dealharbor.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Admin User', 'ADMIN', TRUE, FALSE, TRUE,
    '/api/images/default-avatar.png', 'LOCAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

COMMIT;
