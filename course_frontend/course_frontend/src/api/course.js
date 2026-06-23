import { request } from './index'

/**
 * 创建全新教学班级（教师端）
 * @param {object} data - 课程信息，格式 { courseNum, courseName, className, term, period, credit, cover }
 * @returns Promise 响应结果
 */
const createCourse = (data) => {
  return request('post', '/course/teacher/create', data)
}

/**
 * 获取教师所教的课程大厅列表（支持学期分组）
 * @returns Promise 响应结果
 */
const getTeacherCourseList = () => {
  return request('get', '/course/teacher/list')
}

/**
 * 课程卡片拖拽排序权重保存（教师端）
 * @param {object} data - 格式 { sortedCourseIds: number[] }
 * @returns Promise 响应结果
 */
const saveCourseSort = (data) => {
  return request('put', '/course/teacher/sort', data)
}

/**
 * 单门课程切换置顶状态（教师端）
 * @param {object} data - 格式 { courseId: number, isTop: number }
 * @returns Promise 响应结果
 */
const toggleCourseTop = (data) => {
  return request('put', '/course/teacher/top', data)
}

/**
 * 获取课程详情与顶部面包屑面板数据（教师端）
 * @param {number} courseId - 课程ID
 * @returns Promise 响应结果
 */
const getTeacherCourseDetail = (courseId) => {
  return request('get', `/course/teacher/detail/${courseId}`)
}

/**
 * 凭唯一加课码申请加入课堂（学生端）
 * @param {object} data - 格式 { joinCode: string }
 * @returns Promise 响应结果
 */
const joinCourse = (data) => {
  return request('post', '/course/student/join', data)
}

/**
 * 获取学生加入学习的课程卡片列表（学生端）
 * @returns Promise 响应结果
 */
const getStudentCourseList = () => {
  return request('get', '/course/student/list')
}

export {
  createCourse,
  getTeacherCourseList,
  saveCourseSort,
  toggleCourseTop,
  getTeacherCourseDetail,
  joinCourse,
  getStudentCourseList
}
