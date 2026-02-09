-- Migration: Gộp custom_lesson vào lesson, cập nhật lesson_vocabulary
-- Bước 1: Thêm cột lesson_type vào bảng lesson
ALTER TABLE lesson ADD COLUMN lesson_type VARCHAR(20) DEFAULT 'system';

-- Bước 2: Thêm cột title và description vào bảng lesson (từ custom_lesson)
ALTER TABLE lesson ADD COLUMN title VARCHAR(200);
ALTER TABLE lesson ADD COLUMN description VARCHAR(500);
ALTER TABLE lesson ADD COLUMN updated_at TIMESTAMP;

-- Bước 3: Cho phép lesson_index và display_order NULL (cho personal lesson)
ALTER TABLE lesson MODIFY COLUMN lesson_index INT NULL;
ALTER TABLE lesson MODIFY COLUMN display_order INT NULL;

-- Bước 4: Migrate dữ liệu từ custom_lesson sang lesson
INSERT INTO lesson (user_id, language_code, level, lesson_index, display_order, created_at, vocabulary_count, review_stage, next_review_at, lesson_type, title, description, updated_at)
SELECT 
    user_id,
    language_code,
    level,
    NULL as lesson_index,  -- personal lesson không cần lesson_index
    NULL as display_order,
    created_at,
    vocabulary_count,
    review_stage,
    next_review_at,
    'personal' as lesson_type,
    title,
    description,
    updated_at
FROM custom_lesson;

-- Bước 5: Thêm cột user_vocab_id vào lesson_vocabulary và sửa primary key
-- Bước 5.1: Drop các foreign key constraints trước
ALTER TABLE lesson_vocabulary DROP FOREIGN KEY fk_lv_lesson;
ALTER TABLE lesson_vocabulary DROP FOREIGN KEY fk_lv_vocab;
ALTER TABLE lesson_vocabulary DROP FOREIGN KEY fk_lesson_vocabulary_user_vocab;

-- Bước 5.2: Drop primary key cũ (composite key của lesson_id và vocab_id)
ALTER TABLE lesson_vocabulary DROP PRIMARY KEY;

-- Bước 5.3: Thêm cột id làm primary key mới
ALTER TABLE lesson_vocabulary ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST;

-- Bước 5.4: Set vocab_id NULL
ALTER TABLE lesson_vocabulary MODIFY COLUMN vocab_id BIGINT NULL;

-- Bước 5.5: Recreate foreign key constraints
ALTER TABLE lesson_vocabulary ADD CONSTRAINT fk_lv_lesson FOREIGN KEY (lesson_id) REFERENCES lesson(id) ON DELETE CASCADE;
ALTER TABLE lesson_vocabulary ADD CONSTRAINT fk_lv_vocab FOREIGN KEY (vocab_id) REFERENCES vocabulary(id) ON DELETE CASCADE;
ALTER TABLE lesson_vocabulary ADD CONSTRAINT fk_lesson_vocabulary_user_vocab FOREIGN KEY (user_vocab_id) REFERENCES user_vocabulary(id) ON DELETE CASCADE;

-- Bước 6: Migrate dữ liệu từ custom_lesson_vocabulary sang lesson_vocabulary
-- Tạo map giữa custom_lesson.id cũ và lesson.id mới (dùng temp table)
DROP TEMPORARY TABLE IF EXISTS temp_lesson_mapping;
CREATE TEMPORARY TABLE temp_lesson_mapping AS
SELECT 
    cl.id as old_custom_lesson_id,
    l.id as new_lesson_id
FROM custom_lesson cl
INNER JOIN lesson l ON 
    l.user_id = cl.user_id AND
    l.language_code = cl.language_code AND
    l.level = cl.level AND
    l.title = cl.title AND
    l.lesson_type = 'personal';

-- Insert dữ liệu từ custom_lesson_vocabulary sang lesson_vocabulary
INSERT INTO lesson_vocabulary (lesson_id, vocab_id, user_vocab_id)
SELECT 
    tlm.new_lesson_id,
    clv.vocabulary_id,
    clv.user_vocabulary_id
FROM custom_lesson_vocabulary clv
INNER JOIN temp_lesson_mapping tlm ON tlm.old_custom_lesson_id = clv.custom_lesson_id;

-- Bước 7: Migrate quiz attempts từ custom_lesson_quiz_attempt sang lesson_quiz_attempt
INSERT INTO lesson_quiz_attempt (user_id, lesson_id, mode, score, total, created_at)
SELECT 
    clqa.user_id,
    tlm.new_lesson_id,
    clqa.mode,
    clqa.score,
    clqa.total,
    clqa.created_at
FROM custom_lesson_quiz_attempt clqa
INNER JOIN temp_lesson_mapping tlm ON tlm.old_custom_lesson_id = clqa.custom_lesson_id;

-- Bước 8: Migrate wrong vocab records (cần map attempt IDs)
-- Tạo temp table cho attempt mapping
DROP TEMPORARY TABLE IF EXISTS temp_attempt_mapping;
CREATE TEMPORARY TABLE temp_attempt_mapping AS
SELECT 
    clqa.id as old_attempt_id,
    lqa.id as new_attempt_id
FROM custom_lesson_quiz_attempt clqa
INNER JOIN temp_lesson_mapping tlm ON tlm.old_custom_lesson_id = clqa.custom_lesson_id
INNER JOIN lesson_quiz_attempt lqa ON 
    lqa.user_id = clqa.user_id AND
    lqa.lesson_id = tlm.new_lesson_id AND
    lqa.created_at = clqa.created_at;

-- Insert wrong vocab records
INSERT INTO lesson_quiz_wrong_vocab (attempt_id, vocab_id)
SELECT 
    tam.new_attempt_id,
    clqwv.vocab_id
FROM custom_lesson_quiz_wrong_vocab clqwv
INNER JOIN temp_attempt_mapping tam ON tam.old_attempt_id = clqwv.attempt_id;

-- Bước 9: Drop các bảng custom_lesson không còn dùng
DROP TABLE IF EXISTS custom_lesson_quiz_wrong_vocab;
DROP TABLE IF EXISTS custom_lesson_quiz_attempt;
DROP TABLE IF EXISTS custom_lesson_vocabulary;
DROP TABLE IF EXISTS custom_lesson;

-- Bước 10: Drop temp tables
DROP TEMPORARY TABLE IF EXISTS temp_attempt_mapping;
DROP TEMPORARY TABLE IF EXISTS temp_lesson_mapping;

-- Bước 11: Thêm constraint cho lesson_type
ALTER TABLE lesson ADD CONSTRAINT chk_lesson_type CHECK (lesson_type IN ('system', 'personal'));
