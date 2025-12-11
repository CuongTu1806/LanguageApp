-- Thêm cột vocabulary_count vào bảng custom_lesson
ALTER TABLE custom_lesson 
ADD COLUMN vocabulary_count INT DEFAULT 0 NOT NULL;

-- Cập nhật vocabulary_count cho các bài đã tồn tại
UPDATE custom_lesson cl
SET vocabulary_count = (
    SELECT COUNT(*)
    FROM custom_lesson_vocabulary clv
    WHERE clv.custom_lesson_id = cl.id
);
