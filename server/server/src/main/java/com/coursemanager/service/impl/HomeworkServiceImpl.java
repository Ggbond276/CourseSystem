package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coursemanager.mapper.HomeworkMapper;
import com.coursemanager.mapper.HomeworkSubmitMapper;
import com.coursemanager.pojo.Homework;
import com.coursemanager.pojo.HomeworkSubmit;
import com.coursemanager.service.IHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 作业业务层实现类（教师端）
 */
@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements IHomeworkService {

    @Autowired
    private HomeworkSubmitMapper homeworkSubmitMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 用于将附件 Map 列表序列化为 JSON 字符串存储 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 用于把 Date 格式化为 API 文档要求的 yyyy-MM-dd HH:mm:ss 字符串 */
    private static final SimpleDateFormat DEADLINE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Long createHomework(Homework homework, List<Map<String, String>> attachmentMapList) {
        // 1. 把附件列表序列化进 homework.attachments（null 时置空串，避免数据库 NULL 与后续业务判断冲突）
        if (attachmentMapList != null && attachmentMapList.size() > 0) {
            try {
                homework.setAttachments(OBJECT_MAPPER.writeValueAsString(attachmentMapList));
            } catch (Exception jsonError) {
                throw new RuntimeException("附件列表 JSON 序列化失败", jsonError);
            }
        } else {
            homework.setAttachments("");
        }

        // 2. 写入 homework 主表（雪花 ID 由 MyBatis-Plus 在 save 时生成）
        this.save(homework);
        Long newHomeworkId = homework.getId();
        if (newHomeworkId == null) {
            throw new RuntimeException("作业保存失败，未返回主键");
        }

        // 3. 查询这门课的所有学生（role=2），为每人创建一条 status=0 的待提交占位记录
        List<Long> studentIdList = homeworkSubmitMapper.selectStudentIdListByCourseId(homework.getCourseId());
        if (studentIdList != null && studentIdList.size() > 0) {
            // 4. 为每个学生构造 HomeworkSubmit 实体，逐条插入（幂等：先查是否存在同名 student+homework 的记录）
            for (Long studentId : studentIdList) {
                // 判断是否已存在该学生对此作业的占位记录
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeworkSubmit> qw =
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                qw.eq(HomeworkSubmit::getHomeworkId, newHomeworkId);
                qw.eq(HomeworkSubmit::getStudentId, studentId);
                HomeworkSubmit exist = homeworkSubmitMapper.selectOne(qw);
                if (exist != null) {
                    // 已存在则跳过（幂等保护）
                    continue;
                }
                // 构造并插入占位记录
                HomeworkSubmit pending = new HomeworkSubmit();
                pending.setHomeworkId(newHomeworkId);
                pending.setStudentId(studentId);
                pending.setStatus(0);
                pending.setSimilarity(java.math.BigDecimal.ZERO);
                pending.setWordCount(0);
                homeworkSubmitMapper.insert(pending);
            }
        }

        // 5. 把新作业 ID 返回给 Controller
        return newHomeworkId;
    }

    @Override
    public List<Map<String, Object>> listByTeacher(Long courseId) {
        // 1. 入参校验：courseId 为 null 直接返回空集合，不抛异常，避免前端传错参数导致 500
        if (courseId == null) {
            return new ArrayList<>();
        }

        // 2. 调用 Mapper 联表查询（统计数量交给 SQL 完成，性能更好）
        List<Map<String, Object>> rawRowList = this.baseMapper.selectTeacherHomeworkOverview(courseId);
        if (rawRowList == null || rawRowList.size() == 0) {
            return new ArrayList<>();
        }

        // 3. 当前时间锚点：在循环外取一次，保证全列表时间基准一致（避免同一次请求内不同行时间不一致）
        Date now = new Date();

        // 4. 把数据库原始行转换为 API 文档要求的字段格式
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> rawRow : rawRowList) {
            // 4.1 homeworkId 必须转字符串返回，符合 B1_API.md "雪花 ID 加引号" 约定
            Object homeworkIdObj = rawRow.get("homeworkId");
            String homeworkIdString = homeworkIdObj == null ? null : homeworkIdObj.toString();

            // 4.2 deadline 从 Date 转成 yyyy-MM-dd HH:mm:ss 字符串，符合 B1_API.md 文档示例
            Object deadlineObj = rawRow.get("deadline");
            String deadlineString = null;
            if (deadlineObj instanceof Date) {
                deadlineString = DEADLINE_FORMAT.format((Date) deadlineObj);
            } else if (deadlineObj != null) {
                deadlineString = deadlineObj.toString();
            }

            // 4.3 判断是否已截止：deadline < 当前时间 → isOver = true
            Boolean isOver = Boolean.FALSE;
            if (deadlineObj instanceof Date) {
                isOver = ((Date) deadlineObj).before(now);
            }

            // 4.4 数字字段安全取值（SQL 里 COALESCE 保证了非 NULL，这里防御性再写一层）
            Integer gradedCount = getIntegerValue(rawRow.get("gradedCount"));
            Integer ungradedCount = getIntegerValue(rawRow.get("ungradedCount"));
            // totalCount 由 SQL 子查询直接从 user_course 统计，非 NULL，防御性再取一次
            Integer totalCount = getIntegerValue(rawRow.get("totalCount"));

            // 4.5 组装返回项（用 LinkedHashMap 保证字段顺序与文档示例一致）
            java.util.LinkedHashMap<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("homeworkId", homeworkIdString);
            item.put("title", rawRow.get("title"));
            item.put("description", rawRow.get("description"));
            item.put("type", rawRow.get("type"));
            item.put("activityTag", rawRow.get("activityTag"));
            item.put("totalScore", rawRow.get("totalScore"));
            item.put("forbidLate", rawRow.get("forbidLate"));
            item.put("deadline", deadlineString);
            item.put("isOver", isOver);
            item.put("gradedCount", gradedCount);
            item.put("ungradedCount", ungradedCount);
            // submittedCount = 已批改 + 待批（status=2 + status=1，即所有已提交的人）
            item.put("submittedCount", gradedCount + ungradedCount);
            item.put("totalCount", totalCount);

            resultList.add(item);
        }

        return resultList;
    }

    /**
     * 辅助方法：把任意数值类型安全转换为 Integer
     * MyBatis 在 SUM 聚合时可能返回 Long、BigInteger、Integer 等不同数值类型，统一兜底
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception parseError) {
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> getSubmitList(Long homeworkId, Integer filterStatus, String studentName) {
        if (homeworkId == null) {
            return new ArrayList<>();
        }

        // 用 JdbcTemplate 原生 SQL 查询，绕过 MyBatis 的 resultType 列名映射问题
        // 注意：SELECT 列表里加了 hs.content 和 hs.attachments，
        //       这样教师点开"批阅抽屉"时能直接看到学生写的文字与提交的附件 URL
        StringBuilder sql = new StringBuilder(
            "SELECT hs.id AS submitId, u.name AS studentName, u.account AS studentAccount, " +
            "hs.status AS submitStatus, hs.submit_time AS submitTime, " +
            "hs.similarity, hs.word_count AS wordCount, hs.score, " +
            "hs.content, hs.attachments " +
            "FROM homework_submit hs " +
            "INNER JOIN `user` u ON u.id = hs.student_id " +
            "WHERE hs.homework_id = ?"
        );
        List<Object> args = new ArrayList<>();
        args.add(homeworkId);
        if (filterStatus != null) {
            sql.append(" AND hs.status = ?");
            args.add(filterStatus);
        }
        if (studentName != null && !studentName.trim().isEmpty()) {
            sql.append(" AND u.name LIKE ?");
            args.add("%" + studentName.trim() + "%");
        }
        sql.append(" ORDER BY hs.status ASC, hs.submit_time DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());

        // Jackson 单例复用：用于把 attachments JSON 字符串解析回 List
        ObjectMapper attachmentMapper = new ObjectMapper();

        SimpleDateFormat fmt = DEADLINE_FORMAT;
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            java.util.LinkedHashMap<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("submitId", String.valueOf(row.get("submitId")));
            item.put("studentName", row.get("studentName"));
            item.put("studentAccount", row.get("studentAccount"));
            Object statusRaw = row.get("submitStatus");
            Integer statusValue = null;
            if (statusRaw instanceof Boolean) {
                // 数据库 TINYINT(1) 被 JDBC 映射为 Boolean：true=1, false=0
                statusValue = ((Boolean) statusRaw) ? 1 : 0;
            } else if (statusRaw instanceof Number) {
                statusValue = ((Number) statusRaw).intValue();
            } else if (statusRaw != null) {
                try { statusValue = Integer.parseInt(statusRaw.toString()); } catch (Exception ignore) {}
            }
            item.put("status", statusValue);
            item.put("submitTime", row.get("submitTime") == null ? null : row.get("submitTime").toString());
            item.put("similarity", row.get("similarity"));
            item.put("wordCount", row.get("wordCount"));
            item.put("score", row.get("score"));

            // 学生提交的文本内容（status=0 未交的记录 content 为 null，前端按 null 处理）
            item.put("content", row.get("content"));

            // 学生提交的附件 JSON：解析成 [{name,url},...] 列表给前端直接渲染
            Object attachmentsRaw = row.get("attachments");
            List<Map<String, String>> attachmentList = new ArrayList<>();
            if (attachmentsRaw != null) {
                String attachmentsJson = String.valueOf(attachmentsRaw);
                if (!attachmentsJson.trim().isEmpty() && !"null".equals(attachmentsJson.trim())) {
                    try {
                        attachmentList = attachmentMapper.readValue(
                                attachmentsJson,
                                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {});
                    } catch (Exception parseError) {
                        // 解析失败时返回空列表，避免把脏数据抛到前端
                        attachmentList = new ArrayList<>();
                    }
                }
            }
            item.put("attachments", attachmentList);

            resultList.add(item);
        }
        return resultList;
    }

    @Override
    @Transactional
    public void gradeHomework(Long submitId, Integer score, String teacherComment, Integer status) {
        // 1. 入参兜底校验：submitId / score / status 任何一项为 null 都视为非法
        if (submitId == null) {
            throw new RuntimeException("提交记录ID不能为空");
        }
        if (score == null) {
            throw new RuntimeException("分数不能为空");
        }
        if (status == null) {
            throw new RuntimeException("批阅状态不能为空");
        }

        // 2. 状态值白名单：只允许 2=已批改、3=打回重做；其他值视为非法，防止前端写错数据脏库
        if (status != 2 && status != 3) {
            throw new RuntimeException("批阅状态非法，仅支持 2=已批改 或 3=打回重做");
        }

        // 3. 分数下限校验：不能为负数
        if (score < 0) {
            throw new RuntimeException("分数不能为负数");
        }

        // 4. 先按 submitId 查出原记录，做"存在性"校验 + "批改幂等"判断
        HomeworkSubmit existSubmit = homeworkSubmitMapper.selectById(submitId);
        if (existSubmit == null) {
            throw new RuntimeException("提交记录不存在");
        }

        // 5. 业务规则：仅允许批改"已提交待批阅(status=1)"或"打回重做(status=3)"的记录
        //    已批改(status=2) 不允许重复批改；未交(status=0) 老师看不见，也没必要批
        if (existSubmit.getStatus() != 1 && existSubmit.getStatus() != 3) {
            throw new RuntimeException("该提交记录当前状态不允许批阅（仅允许批改 待批阅 / 打回重做 的记录）");
        }

        // 6. 业务规则：分数不能超过本次作业的满分
        //    需要从 homework 表查 total_score，做上限校验，避免老师误填 999 分
        Homework homeworkEntity = this.baseMapper.selectById(existSubmit.getHomeworkId());
        Integer totalScore = null;
        if (homeworkEntity != null) {
            totalScore = homeworkEntity.getTotalScore();
        }
        if (totalScore != null && score > totalScore) {
            throw new RuntimeException("分数不能超过本次作业满分 " + totalScore);
        }

        // 7. 状态=3 打回重做时：清空 score（保持未打分状态），并把评语写回
        //    状态=2 已批改时：写入 score 和评语
        HomeworkSubmit updateEntity = new HomeworkSubmit();
        updateEntity.setId(submitId);
        if (status == 3) {
            updateEntity.setScore(null);
        } else {
            updateEntity.setScore(score);
        }
        updateEntity.setTeacherComment(teacherComment);
        updateEntity.setStatus(status);

        // 8. 调用 MyBatis-Plus 自带 updateById，按主键 ID 更新指定字段（NULL 字段也会写入，所以打回时 score 会被置空）
        int affectedRowCount = homeworkSubmitMapper.updateById(updateEntity);
        if (affectedRowCount <= 0) {
            throw new RuntimeException("批阅失败，记录可能已被其他教师修改");
        }
    }
}
