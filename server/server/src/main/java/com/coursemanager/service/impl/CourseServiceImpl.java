package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.dto.CourseAppendDto;
import com.coursemanager.dto.CourseChangeDto;
import com.coursemanager.lang.CourseCondition;
import com.coursemanager.lang.PageQuery;
import com.coursemanager.mapper.CourseMapper;
import com.coursemanager.mapper.DepartmentMapper;
import com.coursemanager.mapper.TermMapper;
import com.coursemanager.mapper.UserCourseMapper;
import com.coursemanager.mapper.UserMapper;
import com.coursemanager.pojo.Course;
import com.coursemanager.pojo.Department;
import com.coursemanager.pojo.Term;
import com.coursemanager.pojo.User;
import com.coursemanager.pojo.UserCourse;
import com.coursemanager.service.ICourseService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author hhl
 * @date 2024/06/06 18:02
 * @description 课程业务层实现类
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TermMapper termMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public PageInfo<Course> getPage(PageQuery query, CourseCondition condition) {
        // 启动分页插件，按 pageNum 和 pageSize 分页查询
        // selectCondition 方法由 CourseMapper.xml 中的动态 SQL 实现条件过滤
        return PageHelper.startPage(query.getPageNum(), query.getPageSize())
                .doSelectPageInfo(() -> this.baseMapper.selectCondition(condition));
    }

    @Override
    public boolean appendCourse(CourseAppendDto dto) {
        // 1. 把 DTO 转成 Course 实体（DTO 校验已在 Controller 完成）
        Course course = dto.toModelCreate();
        if (course == null) {
            return false;
        }
        // 2. 委托给 createCourse：它会生成加课码 + 建立 user_course 关联
        //    boolean 版本：满足 CourseController 的老接口
        return this.createCourse(course, dto.getTeacherId()) != null;
    }

    @Override
    public Course appendCourseAndReturn(CourseAppendDto dto) {
        // 1. 把 DTO 转成 Course 实体（DTO 校验已在 Controller 完成）
        Course course = dto.toModelCreate();
        if (course == null) {
            return null;
        }
        // 2. 委托给 createCourse，返回带 id 与 joinCode 的 Course
        return this.createCourse(course, dto.getTeacherId());
    }

    @Override
    public boolean changeCourse(CourseChangeDto dto) {
        // 使用 toModelUpdate 方法将 DTO 转换为 Course 实体
        // 若转换结果为空，则修改失败
        Course course = dto.toModelUpdate();
        if (course == null) {
            return false;
        }
        // 执行数据库更新操作，返回影响行数
        int count = this.baseMapper.updateById(course);
        return count != 0;
    }

    @Override
    public boolean deleteCourse(Long id) {
        // 根据 ID 执行物理删除，返回影响行数
        int count = this.baseMapper.deleteById(id);
        return count != 0;
    }

    @Override
    @Transactional
    public Course createCourse(Course course, Long creatorId) {
        // 1. 生成6位唯一加课码
        String joinCode = generateJoinCode();
        course.setJoinCode(joinCode);
        course.setStatus(1);
        // 1.5 快照授课教师姓名：建课时把 user.name 写入 course.teacherName，
        //     避免后续教师改名导致历史课程显示变化，也避免 list 接口每次 join user 表
        //     如果传入的 course 已带 teacherName（外部已查询过），保留外部值；否则查 user 表兜底
        if (course.getTeacherName() == null || course.getTeacherName().trim().isEmpty()) {
            User creator = userMapper.selectById(creatorId);
            if (creator != null && creator.getName() != null) {
                course.setTeacherName(creator.getName());
            }
        }
        // 1.6 兜底 term 字符串：如果前端只传了 termId 没传 term，根据 termId 查 displayName 回填
        //     这样老的展示（直接读 course.term 字符串）仍然能渲染历史数据
        if ((course.getTerm() == null || course.getTerm().trim().isEmpty())
                && course.getTermId() != null) {
            Term termEntity = termMapper.selectById(course.getTermId());
            if (termEntity != null && termEntity.getDisplayName() != null) {
                course.setTerm(termEntity.getDisplayName());
            }
        }
        // 2. 插入课程记录（MyBatis-Plus 会自动回填 id）
        boolean saved = this.save(course);
        if (!saved) {
            throw new RuntimeException("课程创建失败");
        }
        // 3. 在 user_course 中建立创建者与课程的关联（role=1 教师）
        UserCourse uc = new UserCourse();
        uc.setUserId(creatorId);
        uc.setCourseId(course.getId());
        uc.setRole(1);
        uc.setIsTop(0);
        uc.setSortWeight(0);
        userCourseMapper.insert(uc);
        // 4. 返回带 id 和 joinCode 的 Course 实体，供 Controller 构造响应
        return course;
    }

    @Override
    public List<Map<String, Object>> getTeacherCourseGroupedList(Long teacherId) {
        // 1. 查出该教师作为"任课教师(role=1)"的所有 user_course 关联记录
        List<UserCourse> userCourseList = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, teacherId)
                        .eq(UserCourse::getRole, 1));

        // 2. 若没有任何关联，直接返回空列表
        if (userCourseList == null || userCourseList.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 把所有课程ID收集起来，用于一次性查出课程详情
        List<Long> courseIdList = new ArrayList<>();
        for (UserCourse uc : userCourseList) {
            courseIdList.add(uc.getCourseId());
        }

        // 4. 一次性查出这些课程的基本信息
        List<Course> courseList = this.baseMapper.selectBatchIds(courseIdList);

        // 5. 把课程按 ID 放进 Map，便于按 user_course 顺序快速取详情
        Map<Long, Course> courseMap = new HashMap<>();
        for (Course course : courseList) {
            courseMap.put(course.getId(), course);
        }

        // 6. 准备一个"学期 -> 该学期的卡片列表"的中间容器（保持原始顺序）
        Map<String, List<Map<String, Object>>> groupedCourses = new LinkedHashMap<>();

        // 7. 按 user_course 的顺序遍历，组装每个课程的卡片数据
        for (UserCourse uc : userCourseList) {
            Course course = courseMap.get(uc.getCourseId());
            if (course == null) {
                continue;
            }

            // 组装单张课程卡片
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", course.getId() == null ? null : course.getId().toString());
            card.put("courseNum", course.getCourseNum());
            card.put("courseName", course.getCourseName());
            card.put("className", course.getClassName());
            card.put("term", course.getTerm());
            card.put("termId", course.getTermId() == null ? null : course.getTermId().toString());
            card.put("termDisplayName", getTermDisplayName(course.getTermId()));
            card.put("departmentId", course.getDepartmentId() == null ? null : course.getDepartmentId().toString());
            card.put("departmentName", getDepartmentName(course.getDepartmentId()));
            card.put("period", course.getPeriod());
            card.put("credit", course.getCredit());
            card.put("joinCode", course.getJoinCode());
            card.put("cover", course.getCover());
            card.put("status", course.getStatus());
            card.put("teacherName", course.getTeacherName());
            card.put("isTop", uc.getIsTop());
            card.put("sortWeight", uc.getSortWeight());

            // 用 term 作为分组键；若学期为空则统一归到"未分学期"
            String term = course.getTerm();
            if (term == null || term.trim().isEmpty()) {
                term = "未分学期";
            }

            // 把卡片塞进对应学期的列表里
            List<Map<String, Object>> termCourseList = groupedCourses.get(term);
            if (termCourseList == null) {
                termCourseList = new ArrayList<>();
                groupedCourses.put(term, termCourseList);
            }
            termCourseList.add(card);
        }

        // 8. 把分组结果转换为前端需要的格式：[{ term, courses: [...] }]
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedCourses.entrySet()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("term", entry.getKey());
            group.put("courses", entry.getValue());
            result.add(group);
        }
        return result;
    }

    @Override
    @Transactional
    public void saveCourseSort(Long teacherId, List<Long> sortedCourseIds) {
        // 1. 入参校验：列表为空则直接返回，避免无意义的数据库操作
        if (sortedCourseIds == null || sortedCourseIds.isEmpty()) {
            return;
        }

        // 2. 把这批课程的 user_course 关联一次性查出来，用于权限校验
        List<UserCourse> userCourseList = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, teacherId)
                        .eq(UserCourse::getRole, 1)
                        .in(UserCourse::getCourseId, sortedCourseIds));

        // 3. 把查询结果转成 Map，便于按 courseId 快速找到对应关联
        Map<Long, UserCourse> relationMap = new HashMap<>();
        for (UserCourse uc : userCourseList) {
            relationMap.put(uc.getCourseId(), uc);
        }

        // 4. 按 sortedCourseIds 的顺序遍历，第 1 个 = 最大权重，越往后越小
        int size = sortedCourseIds.size();
        for (int i = 0; i < size; i++) {
            Long courseId = sortedCourseIds.get(i);
            UserCourse relation = relationMap.get(courseId);
            if (relation == null) {
                continue;
            }
            // 直接更新权重
            relation.setSortWeight(size - i);
            userCourseMapper.updateById(relation);
        }
    }

    @Override
    @Transactional
    public void toggleCourseTop(Long teacherId, Long courseId, Integer isTop) {
        // 1. 找出该教师与该课程的关联记录（必须存在才能置顶）
        UserCourse relation = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, teacherId)
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getRole, 1));
        if (relation == null) {
            throw new RuntimeException("课程不存在或您无权操作该课程");
        }

        // 2. 同时更新置顶状态与排序权重
        //    设计原则：isTop=1 时把 sortWeight 拉到一个很大的值，
        //    确保置顶的课程在列表里稳定排到最前；
        //    isTop=0 时把 sortWeight 归零，让普通排序继续生效。
        relation.setIsTop(isTop);
        if (isTop != null && isTop == 1) {
            relation.setSortWeight(999999);
        } else {
            relation.setSortWeight(0);
        }
        int count = userCourseMapper.updateById(relation);
        if (count == 0) {
            throw new RuntimeException("置顶状态更新失败");
        }
    }

    @Override
    public Map<String, Object> getTeacherCourseDetail(Long courseId, Long teacherId) {
        // 1. 先校验该教师确实"任教"了这门课，否则不返回详情
        UserCourse relation = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, teacherId)
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getRole, 1));
        if (relation == null) {
            throw new RuntimeException("课程不存在或您无权查看该课程");
        }

        // 2. 查询课程基本信息
        Course course = this.baseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 统计该课程的成员数量（含教师、学生、助教，全部角色）
        Long memberCount = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId));

        // 4. 查询创建课程的教师真实姓名（用于顶部"授课教师"展示）
        //    课程可能换教师接手，但 creatorId 是创建者；先按 creatorId 查
        //    若查不到则按 teacherId 兜底（即当前访问者本人）
        String teacherName = null;
        if (course.getCreatorId() != null) {
            User creator = userMapper.selectById(course.getCreatorId());
            if (creator != null) {
                teacherName = creator.getName();
            }
        }
        if (teacherName == null) {
            User self = userMapper.selectById(teacherId);
            if (self != null) {
                teacherName = self.getName();
            }
        }

        // 5. 组装返回给前端的详情数据
        //    注意：creatorId 是雪花算法生成的 19 位 Long，JS 解析会丢精度，
        //    因此在响应层手动转成 String，让前端拿到完整 ID。
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", course.getId() == null ? null : course.getId().toString());
        detail.put("courseNum", course.getCourseNum());
        detail.put("courseName", course.getCourseName());
        detail.put("className", course.getClassName());
        detail.put("term", course.getTerm());
        detail.put("termId", course.getTermId() == null ? null : course.getTermId().toString());
        detail.put("termDisplayName", getTermDisplayName(course.getTermId()));
        detail.put("departmentId", course.getDepartmentId() == null ? null : course.getDepartmentId().toString());
        detail.put("departmentName", getDepartmentName(course.getDepartmentId()));
        detail.put("period", course.getPeriod());
        detail.put("credit", course.getCredit());
        detail.put("joinCode", course.getJoinCode());
        detail.put("cover", course.getCover());
        detail.put("syllabus", course.getSyllabus());
        detail.put("intro", course.getIntro());
        detail.put("status", course.getStatus());
        detail.put("creatorId", course.getCreatorId() == null ? null : course.getCreatorId().toString());
        detail.put("teacherName", teacherName);
        detail.put("memberCount", memberCount == null ? 0L : memberCount);
        return detail;
    }

    @Override
    public boolean updateCourseInfo(Long courseId, Long teacherId, String syllabus, String intro) {
        // 1. 校验任教关系（防止教师改他人课程）
        UserCourse relation = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, teacherId)
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getRole, 1));
        if (relation == null) {
            throw new RuntimeException("课程不存在或您无权修改该课程");
        }
        // 2. 只更新非 null 字段（null 表示前端未传，保留数据库原值）
        Course updateEntity = new Course();
        updateEntity.setId(courseId);
        if (syllabus != null) {
            updateEntity.setSyllabus(syllabus);
        }
        if (intro != null) {
            updateEntity.setIntro(intro);
        }
        int count = this.baseMapper.updateById(updateEntity);
        return count > 0;
    }

    /**
     * 生成6位不重复的加课码
     * 循环生成随机数直到找到数据库中不存在的加课码
     *
     * 防御措施：
     * - 设置最大尝试次数 100 次，避免极端情况（如数据库已有海量课程码）
     *   导致循环不收敛。
     * - 仍失败则抛异常，由 Controller 转换成对前端的错误提示。
     */
    private String generateJoinCode() {
        Random random = new Random();
        int maxAttempts = 100;
        int attempt = 0;
        String code;
        do {
            // %06d 保证输出是 6 位字符串（不足补 0）
            code = String.format("%06d", random.nextInt(1000000));
            attempt++;
            if (attempt > maxAttempts) {
                throw new RuntimeException("生成唯一加课码失败，请稍后重试");
            }
        } while (this.exists(new LambdaQueryWrapper<Course>()
                .eq(Course::getJoinCode, code)));
        return code;
    }

    /**
     * 根据 termId 查学期显示名（用于详情/列表接口拼装给前端）
     * @param termId 学期ID（可为 null）
     * @return displayName（如 "2024-2025 第一学期"），查不到返回 null
     */
    private String getTermDisplayName(Long termId) {
        if (termId == null) {
            return null;
        }
        Term term = termMapper.selectById(termId);
        return term == null ? null : term.getDisplayName();
    }

    /**
     * 根据 departmentId 查学院名（用于详情/列表接口拼装给前端）
     * @param departmentId 学院ID（可为 null）
     * @return name（如 "计算机学院"），查不到返回 null
     */
    private String getDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        Department dept = departmentMapper.selectById(departmentId);
        return dept == null ? null : dept.getName();
    }
}
