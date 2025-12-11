-- Kiểm tra lesson 2 có từ vựng không
SELECT l.id, l.lesson_index, l.vocabulary_count, 
       COUNT(lv.vocab_id) as actual_vocab_count
FROM lesson l
LEFT JOIN lesson_vocabulary lv ON l.id = lv.lesson_id
WHERE l.lesson_index = 2 AND l.language_code = 'cn' AND l.level = 'A1'
GROUP BY l.id;

-- Xem chi tiết từ vựng của lesson 2
SELECT lv.lesson_id, lv.vocab_id, v.word, v.meaning
FROM lesson_vocabulary lv
JOIN vocabulary v ON lv.vocab_id = v.id
WHERE lv.lesson_id = (
    SELECT id FROM lesson 
    WHERE lesson_index = 2 AND language_code = 'cn' AND level = 'A1'
    LIMIT 1
);
