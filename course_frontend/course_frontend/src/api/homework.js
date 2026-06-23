import { request } from './index'

/**
 * 发布新作业（教师端）
 * @param {object} data - 作业信息，格式 { courseId, title, description, type, activityTag, totalScore, forbidLate, deadline, attachments }
 * @returns Promise 响应结果
 */
const createHomework = (data) => {
  return request('post', '/homework/teacher/create', data)
}

/**
 * 获取某门课下的所有作业总览列表（教师端）
 * @param {object} params - 格式 { courseId: number }
 * @returns Promise 响应结果
 */
const getTeacherHomeworkList = (params) => {
  return request('get', '/homework/teacher/list', params)
}

/**
 * 获取某次作业的所有学生提交明细（教师端批阅大厅）
 * @param {object} params - 格式 { homeworkId: number, status?: number, studentName?: string }
 * @returns Promise 响应结果
 */
const getSubmissionList = (params) => {
  return request('get', '/homework/teacher/submit-list', params)
}

/**
 * 教师执行作业单人打分与评语留存
 * @param {object} data - 格式 { submitId: number, score: number, teacherComment: string, status: number }
 * @returns Promise 响应结果
 */
const gradeHomework = (data) => {
  return request('post', '/homework/teacher/grade', data)
}

/**
 * 获取某门课下属于学生的作业任务列表（学生端）
 * @param {object} params - 格式 { courseId: number }
 * @returns Promise 响应结果
 */
const getStudentHomeworkList = (params) => {
  return request('get', '/homework/student/list', params)
}

/**
 * 查看特定作业要求及个人提交/批改记录详情（学生端）
 * @param {object} params - 格式 { homeworkId: number }
 * @returns Promise 响应结果
 */
const getStudentHomeworkDetail = (params) => {
  return request('get', '/homework/student/detail', params)
}

/**
 * 学生上传附件并提交作业（学生端）
 * @param {object} data - 格式 { homeworkId: number, content: string, attachments: Array }
 * @returns Promise 响应结果
 */
const submitHomework = (data) => {
  return request('post', '/homework/student/submit', data)
}

export {
  createHomework,
  getTeacherHomeworkList,
  getSubmissionList,
  gradeHomework,
  getStudentHomeworkList,
  getStudentHomeworkDetail,
  submitHomework
}
