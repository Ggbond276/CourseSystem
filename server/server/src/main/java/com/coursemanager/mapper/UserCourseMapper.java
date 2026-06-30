package com.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursemanager.pojo.UserCourse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 架构师
 * @date 2024/06/23
 * @description 用户-课程关联持久层
 *
 * 设计原则：MyBatis-Plus 的 BaseMapper 已提供 selectList / selectOne /
 *           selectCount / insert / updateById / exists 等基础能力，
 *           这里只补充"业务语义清晰"的自定义方法，让 Service 层读起来像白话文。
 */
@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {

    /**
     * 查询某用户作为"任课教师(role=1)"关联的全部课程
     * @param teacherId 教师用户ID
     * @return 关联列表
     */
    List<UserCourse> selectTeacherRelations(Long teacherId);

    /**
     * 查询某用户作为"正式学生(role=2)"关联的全部课程
     * @param studentId 学生用户ID
     * @return 关联列表
     */
    List<UserCourse> selectStudentRelations(Long studentId);

    /**
     * 查询某用户与某门课程的关联记录（不限角色）
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 单条关联记录，没有则返回 null
     */
    UserCourse selectOneByUserAndCourse(Long userId, Long courseId);

    /**
     * 查询某用户与某门课程、且角色匹配的关联记录
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param role 课堂角色（1=教师，2=学生，3=助教）
     * @return 单条关联记录，没有则返回 null
     */
    UserCourse selectOneByUserCourseAndRole(Long userId, Long courseId, Integer role);

    /**
     * 查询某教师作为"任课教师(role=1)"关联的全部课程ID
     * @param teacherId 教师用户ID
     * @return 课程ID列表
     */
    List<Long> selectTeacherCourseIds(Long teacherId);
}