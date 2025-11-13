-- Migration: Add Product Pending Review Queue System
-- Date: November 7, 2025
-- Description: Creates table to track products pending for 14+ days moved to review queue

-- Create product_pending_reviews table
CREATE TABLE IF NOT EXISTS product_pending_reviews (
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
    
    CONSTRAINT fk_pending_review_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pending_review_admin FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_pending_reviews_product_id ON product_pending_reviews(product_id);
CREATE INDEX idx_pending_reviews_is_resolved ON product_pending_reviews(is_resolved);
CREATE INDEX idx_pending_reviews_moved_at ON product_pending_reviews(moved_to_review_at);
CREATE INDEX idx_pending_reviews_reviewed_at ON product_pending_reviews(reviewed_at);

-- Create composite index for common queries
CREATE INDEX idx_pending_reviews_product_unresolved ON product_pending_reviews(product_id, is_resolved);

-- Add comments for documentation
COMMENT ON TABLE product_pending_reviews IS 'Tracks products that have been pending for 14+ days and moved to extended review queue';
COMMENT ON COLUMN product_pending_reviews.product_id IS 'Reference to the product in review';
COMMENT ON COLUMN product_pending_reviews.original_created_at IS 'When the product was originally created';
COMMENT ON COLUMN product_pending_reviews.moved_to_review_at IS 'When the product was moved to review queue (after 14 days)';
COMMENT ON COLUMN product_pending_reviews.days_pending IS 'Number of days product was pending before being moved to review';
COMMENT ON COLUMN product_pending_reviews.user_notified IS 'Whether the user was sent an email notification';
COMMENT ON COLUMN product_pending_reviews.is_resolved IS 'Whether admin has taken action (approved or rejected)';
COMMENT ON COLUMN product_pending_reviews.review_decision IS 'Admin decision: APPROVED or REJECTED';
