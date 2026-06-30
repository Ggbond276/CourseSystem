package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coursemanager.mapper.HomeworkMapper;
import com.coursemanager.mapper.HomeworkSubmitMapper;
import com.coursemanager.pojo.Homework;
import com.coursemanager.pojo.HomeworkSubmit;
import com.coursemanager.service.IHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
            homeworkSubmitMapper.batchInsertPendingSubmit(newHomeworkId, studentIdList);
        }

        // 4. 把新作业 ID 返回给 Controller
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
            Integer unsubmittedCount = getIntegerValue(rawRow.get("unsubmittedCount"));

            // 4.5 组装返回项（用 LinkedHashMap 保证字段顺序与文档示例一致）
            java.util.LinkedHashMap<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("homeworkId", homeworkIdString);
            item.put("title", rawRow.get("title"));
            item.put("activityTag", rawRow.get("activityTag"));
            item.put("deadline", deadlineString);
            item.put("isOver", isOver);
            item.put("gradedCount", gradedCount);
            item.put("ungradedCount", ungradedCount);
            item.put("unsubmittedCount", unsubmittedCount);

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
    public List<Map<String, Object>> getSubmitList(Long homeworkId, Integer status, String studentName) {
        // 1. 入参校验：homeworkId 为 null 直接返回空集合（不抛异常，避免前端传错导致 500）
        if (homeworkId == null) {
            return new ArrayList<>();
        }

        // 2. 把 studentName 的 null 标准化为 null（XML 里已经处理 ""，这里保持原值即可）
        //    保留这个说明是为了让其他组员改代码时知道：null = 不筛，"xxx" = 模糊搜 xxx
        String safeStudentName = studentName;
        if (safeStudentName != null) {
            safeStudentName = safeStudentName.trim();
            if (safeStudentName.length() == 0) {
                safeStudentName = null;
            }
        }

        // 3. 调用 Mapper 联表查询（user 表关联取学生姓名、学号）
        List<Map<String, Object>> rawRowList = homeworkSubmitMapper.selectSubmitListForTeacher(
                homeworkId, status, safeStudentName);
        if (rawRowList == null || rawRowList.size() == 0) {
            return new ArrayList<>();
        }

        // 4. 把数据库原始行转换为 API 文档要求的字段格式
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> rawRow : rawRowList) {
            // 4.1 submitId 转字符串返回，符合 B1_API.md "雪花 ID 加引号" 约定
            Object submitIdObj = rawRow.get("submitId");
            String submitIdString = submitIdObj == null ? null : submitIdObj.toString();

            // 4.2 submitTime 从 Date 转成 yyyy-MM-dd HH:mm:ss 字符串
            Object submitTimeObj = rawRow.get("submitTime");
            String submitTimeString = null;
            if (submitTimeObj instanceof Date) {
                submitTimeString = DEADLINE_FORMAT.format((Date) submitTimeObj);
            } else if (submitTimeObj != null) {
                submitTimeString = submitTimeObj.toString();
            }

            // 4.3 status 是 Integer 数值，直接取出（可能为 null 表示没有此字段）
            Object statusObj = rawRow.get("status");
            Integer statusValue = null;
            if (statusObj instanceof Number) {
                statusValue = ((Number) statusObj).intValue();
            } else if (statusObj != null) {
                try { statusValue = Integer.parseInt(statusObj.toString()); } catch (Exception ignoreError) { statusValue = null; }
            }

            // 4.4 wordCount / score 是 Integer，可为 null（未批改时 score=null）
            Integer wordCountValue = null;
            Object wordCountObj = rawRow.get("wordCount");
            if (wordCountObj instanceof Number) {
                wordCountValue = ((Number) wordCountObj).intValue();
            } else if (wordCountObj != null) {
                try { wordCountValue = Integer.parseInt(wordCountObj.toString()); } catch (Exception ignoreError) { wordCountValue = null; }
            }

            Integer scoreValue = null;
            Object scoreObj = rawRow.get("score");
            if (scoreObj instanceof Number) {
                scoreValue = ((Number) scoreObj).intValue();
            } else if (scoreObj != null) {
                try { scoreValue = Integer.parseInt(scoreObj.toString()); } catch (Exception ignoreError) { scoreValue = null; }
            }

            // 4.5 similarity 是 BigDecimal，保留原样返回（前端可能做百分比展示）
            //    不用 toString 转换，让 Jackson 自动序列化
            Object similarityValue = rawRow.get("similarity");

            // 4.6 组装返回项（用 LinkedHashMap 保证字段顺序与文档示例一致）
            java.util.LinkedHashMap<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("submitId", submitIdString);
            item.put("studentName", rawRow.get("studentName"));
            item.put("studentAccount", rawRow.get("studentAccount"));
            item.put("status", statusValue);
            item.put("submitTime", submitTimeString);
            item.put("similarity", similarityValue);
            item.put("wordCount", wordCountValue);
            item.put("score", scoreValue);

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
