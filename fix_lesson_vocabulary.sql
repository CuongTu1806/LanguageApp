-- Script để populate bảng lesson_vocabulary từ lessons hiện tại
-- Chạy script này trong MySQL để fix data

-- Option 1: Xóa toàn bộ lessons cũ và tạo lại
-- TRUNCATE TABLE lesson_vocabulary;
-- DELETE FROM lesson;
-- (Sau đó vào app tạo lại lessons bằng nút "Tạo Bài Mới")

-- Option 2: Nếu bạn muốn giữ lại lessons hiện tại, cần insert manual vào lesson_vocabulary
-- Ví dụ: Nếu lesson 2 (id=14330) có 15 từ vựng với ids từ 1-15:
/*
INSERT INTO lesson_vocabulary (lesson_id, vocab_id) VALUES
(14330, 1),
(14330, 2),
(14330, 3),
...
(14330, 15);
*/

-- Hoặc nếu bạn biết logic cũ dựa trên lesson_no trong vocabulary:
-- INSERT INTO lesson_vocabulary (lesson_id, vocab_id)
-- SELECT l.id, v.id
-- FROM lesson l
-- JOIN vocabulary v ON v.lesson_no = l.lesson_index 
--     AND v.language_code = l.language_code 
--     AND v.level = l.level
-- WHERE NOT EXISTS (
--     SELECT 1 FROM lesson_vocabulary lv 
--     WHERE lv.lesson_id = l.id AND lv.vocab_id = v.id
-- );

-- Kiểm tra sau khi chạy:
SELECT l.id, l.lesson_index, l.vocabulary_count, COUNT(lv.vocab_id) as actual_count
FROM lesson l
LEFT JOIN lesson_vocabulary lv ON l.id = lv.lesson_id
GROUP BY l.id
ORDER BY l.lesson_index;
