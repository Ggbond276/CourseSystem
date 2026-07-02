import { request } from './index'

/**
 * 创建全新教学班级（教师端）
 * 重要：所有雪花 ID 必须以字符串形式提交，否则 JS Number 会丢精度导致后端 Long 反序列化失败
 * @param {object} data - 课程信息，格式 { teacherId: string, courseNum, courseName, className, term, period, credit, cover }
 * @returns Promise 响应结果
 */
const createCourse = (data) => {
  return request('post', '/course/teacher/create', data)
}

/**
 * 获取教师所教的课程大厅列表（支持学期分组）
 * 重要：teacherId 必须转 String，避免 GET query 参数丢精度
 * @param {string|number} teacherId - 教师用户ID（推荐传 String）
 * @returns Promise 响应结果
 */
const getTeacherCourseList = (teacherId) => {
  return request('get', '/course/teacher/list', { teacherId: String(teacherId) })
}

/**
 * 课程卡片拖拽排序权重保存（教师端）
 * 重要：所有雪花 ID 必须以字符串形式提交
 * @param {object} data - 格式 { teacherId: string, sortedCourseIds: string[] }
 * @returns Promise 响应结果
 */
const saveCourseSort = (data) => {
  return request('put', '/course/teacher/sort', data)
}

/**
 * 单门课程切换置顶状态（教师端）
 * 重要：所有雪花 ID 必须以字符串形式提交
 * @param {object} data - 格式 { teacherId: string, courseId: string, isTop: 0|1 }
 * @returns Promise 响应结果
 */
const toggleCourseTop = (data) => {
  return request('put', '/course/teacher/top', data)
}

/**
 * 获取课程详情与顶部面包屑面板数据（教师端）
 * 重要：所有雪花 ID 必须以字符串形式提交
 * @param {string|number} courseId - 课程ID（推荐传 String）
 * @param {string|number} teacherId - 教师用户ID（推荐传 String）
 * @returns Promise 响应结果
 */
const getTeacherCourseDetail = (courseId, teacherId) => {
  return request('get', `/course/teacher/detail/${courseId}`, { teacherId: String(teacherId) })
}

/**
 * 凭唯一加课码申请加入课堂（学生端）
 * 重要：studentId 必须以字符串形式提交
 * @param {object} data - 格式 { studentId: string, joinCode: string }
 * @returns Promise 响应结果
 */
const joinCourse = (data) => {
  return request('post', '/course/student/join', data)
}

/**
 * 获取学生加入学习的课程卡片列表（学生端）
 * 重要：studentId 必须转 String，避免 GET query 参数丢精度
 * @param {string|number} studentId - 学生用户ID（推荐传 String）
 * @returns Promise 响应结果
 */
const getStudentCourseList = (studentId) => {
  return request('get', '/course/student/list', { studentId: String(studentId) })
}

/**
 * 获取课程详情（学生端）
 * 重要：courseId 必须转 String，避免路径参数丢精度
 * @param {string|number} courseId - 课程ID
 * @param {string|number} studentId - 学生用户ID（可选，不传则由后端从 token 解析）
 * @returns Promise 响应结果
 */
const getStudentCourseDetail = (courseId, studentId) => {
  const params = studentId ? { studentId: String(studentId) } : {}
  return request('get', `/course/student/detail/${String(courseId)}`, params)
}

/**
 * 更新课程的可编辑信息（教师端：课程大纲 + 课程介绍）
 * 重要：所有雪花 ID 必须以字符串形式提交
 * @param {object} data - 格式 { teacherId?: string, courseId: string, syllabus?: string, intro?: string }
 *   syllabus / intro 均为可选，null 表示不更新该字段
 * @returns Promise 响应结果
 */
const updateCourseInfo = (data) => {
  // 兜底：把 courseId / teacherId 全部转字符串，避免 JS Number 丢精度
  const payload = { ...data }
  if (payload.courseId !== undefined && payload.courseId !== null) {
    payload.courseId = String(payload.courseId)
  }
  if (payload.teacherId !== undefined && payload.teacherId !== null) {
    payload.teacherId = String(payload.teacherId)
  }
  return request('put', '/course/teacher/update-info', payload)
}

/**
 * 课程详情（通用入口：根据角色自动分派到教师端/学生端接口）
 *
 * 用法：传入当前用户角色和课程 ID，无需关心后端到底走哪条路径
 * @param {number|string} courseId - 课程ID
 * @param {string} role - 当前用户角色：'teacher' 或 'student'
 * @param {number|string} userId - 当前登录用户ID（教师/学生均可）
 * @returns Promise 响应结果
 *
 * 后端依赖：
 *   - 教师端：GET /course/teacher/detail/{courseId}?teacherId=xxx（已实现）
 *   - 学生端：当前未实现，本函数暂时返回 reject，调用方需 try/catch 兜底
 */
const getCourseDetail = (courseId, role, userId) => {
  // 1. 参数校验：课程 ID 必须有
  if (courseId === null || courseId === undefined || courseId === '') {
    return Promise.reject(new Error('课程ID不能为空'))
  }

  // 2. 按角色分派到对应后端接口
  if (role === 'teacher') {
    // 教师端：已实现
    return getTeacherCourseDetail(courseId, userId)
  }

  if (role === 'student') {
    // 学生端：后端尚未提供课程详情接口，
    // 这里直接 reject，让调用方走 mock 数据兜底。
    // 等后端补上 GET /course/student/detail/{courseId}?studentId=xxx 后，
    // 把下面这行换成 request('get', `/course/student/detail/${courseId}`, { studentId: userId }) 即可。
    return Promise.reject(new Error('学生端课程详情接口待后端实现'))
  }

  // 3. 角色不识别
  return Promise.reject(new Error('未知用户角色：' + role))
}

export {
  createCourse,
  getTeacherCourseList,
  saveCourseSort,
  toggleCourseTop,
  getTeacherCourseDetail,
  updateCourseInfo,
  joinCourse,
  getStudentCourseList,
  getStudentCourseDetail,
  getCourseDetail
}