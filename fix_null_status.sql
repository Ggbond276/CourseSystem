-- 修复 homework_submit 表中 status 为 null 的脏数据
-- 语义：有 submit_time 且 score 为 null 的记录 → 已提交待批阅（status=1）
--       有 score 的记录 → 已批改（status=2）
UPDATE homework_submit
SET status = CASE
    WHEN score IS NOT NULL THEN 2
    ELSE 1
END
WHERE status IS NULL;

SELECT id, homework_id, student_id, status, score, submit_time
FROM homework_submit
WHERE status IS NULL;
