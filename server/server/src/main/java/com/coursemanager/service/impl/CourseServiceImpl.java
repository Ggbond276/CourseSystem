package com.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coursemanager.dto.CourseAppendDto;
import com.coursemanager.dto.CourseChangeDto;
import com.coursemanager.lang.CourseCondition;
import com.coursemanager.lang.PageQuery;
import com.coursemanager.mapper.CourseMapper;
import com.coursemanager.mapper.UserCourseMapper;
import com.coursemanager.pojo.Course;
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

    @Override
    public PageInfo<Course> getPage(PageQuery query, CourseCondition condition) {
        // 启动分页插件，按 pageNum 和 pageSize 分页查询
        // selectCondition 方法由 CourseMapper.xml 中的动态 SQL 实现条件过滤
        return PageHelper.startPage(query.getPageNum(), query.getPageSize())
                .doSelectPageInfo(() -> this.baseMapper.selectCondition(condition));
    }

    @Override
    public boolean appendCourse(CourseAppendDto dto) {
        // 使用 toModelCreate 方法将 DTO 转换为 Course 实体
        // 若转换结果为空，则新增失败
        Course course = dto.toModelCreate();
        if (course == null) {
            return false;
        }
        // 执行数据库插入操作，返回影响行数
        int count = this.baseMapper.insert(course);
        return count != 0;
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
    public boolean createCourse(Course course, Long creatorId) {
        // 1. 生成6位唯一加课码
        String joinCode = generateJoinCode();
        course.setJoinCode(joinCode);
        // 2. 插入课程记录
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
        return userCourseMapper.insert(uc) > 0;
    }

    @Override
    public List<Map<String, Object>> getTeacherCourseGroupedList(Long teacherId) {
        // TODO(teacher-dev): 实现教师课程分组列表查询
        // 提示：从 user_course 表查询 role=1 的记录，关联 course 表获取详情
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void saveCourseSort(Long teacherId, List<Long> sortedCourseIds) {
        // TODO(teacher-dev): 实现课程排序权重保存
        // 提示：遍历 sortedCourseIds，更新每门课程在 user_course 中的 sort_weight 字段
    }

    @Override
    @Transactional
    public void toggleCourseTop(Long teacherId, Long courseId, Integer isTop) {
        // TODO(teacher-dev): 实现置顶状态切换
        // 提示：更新 user_course 表中对应记录的 is_top 字段
    }

    @Override
    public Map<String, Object> getTeacherCourseDetail(Long courseId, Long teacherId) {
        // TODO(teacher-dev): 实现教师端课程详情查询
        // 提示：查询 course 表和统计 user_course 表的成员数量
        return new HashMap<>();
    }

    /**
     * 生成6位不重复的加课码
     * 循环生成随机数直到找到数据库中不存在的加课码
     */
    private String generateJoinCode() {
        Random random = new Random();
        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (this.exists(new LambdaQueryWrapper<Course>()
                .eq(Course::getJoinCode, code)));
        return code;
    }
}
