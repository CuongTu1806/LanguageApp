-- Thêm cột vocabulary_count vào bảng lesson
ALTER TABLE lesson 
ADD COLUMN vocabulary_count INT DEFAULT 0 NOT NULL;

-- Cập nhật vocabulary_count cho các bài đã tồn tại
UPDATE lesson l
SET vocabulary_count = (
    SELECT COUNT(*)
    FROM lesson_vocabulary lv
    WHERE lv.lesson_id = l.id
);
