-- Migration script: Thêm cột image_path vào bảng vocabulary
-- Chạy script này trên database TiengTrungDB

USE TiengTrungDB;

-- Thêm cột image_path để lưu đường dẫn hình ảnh
ALTER TABLE vocabulary 
ADD COLUMN image_path VARCHAR(500) NULL 
COMMENT 'Đường dẫn hình ảnh minh họa từ vựng (ví dụ: /images/vocab/apple_123.jpg)';

-- Kiểm tra kết quả
DESCRIBE vocabulary;
