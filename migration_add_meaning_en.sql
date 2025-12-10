-- Migration: Add meaning_en column to vocabulary table
-- Date: 2025-12-09
-- Purpose: Store English translations for better image searches

-- Add meaning_en column (up to 500 characters)
ALTER TABLE vocabulary 
ADD COLUMN meaning_en VARCHAR(500) 
AFTER meaning;

-- Add index for better query performance
CREATE INDEX idx_meaning_en ON vocabulary(meaning_en);

-- Verify the column was added
DESCRIBE vocabulary;
