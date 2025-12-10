-- Tạo bảng custom_lesson
CREATE TABLE IF NOT EXISTS custom_lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    language_code VARCHAR(10) NOT NULL DEFAULT 'cn',
    level VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_language_code (language_code),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo bảng join table cho quan hệ ManyToMany
CREATE TABLE IF NOT EXISTS custom_lesson_vocabulary (
    custom_lesson_id BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    
    PRIMARY KEY (custom_lesson_id, vocabulary_id),
    
    CONSTRAINT fk_custom_lesson 
        FOREIGN KEY (custom_lesson_id) 
        REFERENCES custom_lesson(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_vocabulary 
        FOREIGN KEY (vocabulary_id) 
        REFERENCES vocabulary(id) 
        ON DELETE CASCADE,
    
    INDEX idx_custom_lesson_id (custom_lesson_id),
    INDEX idx_vocabulary_id (vocabulary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert bài học mẫu cho testing
INSERT INTO custom_lesson (user_id, title, description, language_code, level)
VALUES (1, 'Bài Học Của Tôi', 'Bài học tự tạo đầu tiên', 'cn', 'A1');
