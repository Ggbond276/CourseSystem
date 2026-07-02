import { request } from './index'

/**
 * 查询所有学期字典（前端"学年学期"下拉用）
 * GET /dict/term/list
 * 响应：{ code, msg, data: [ { id, schoolYear, semester, displayName, startDate, endDate, isCurrent } ] }
 * @returns Promise 响应结果
 */
const getTermList = () => {
  return request('get', '/dict/term/list')
}

/**
 * 查询当前学期（is_current=1）
 * GET /dict/term/current
 * @returns Promise 响应结果
 */
const getCurrentTerm = () => {
  return request('get', '/dict/term/current')
}

/**
 * 查询所有学院字典（前端"所属学院"下拉用）
 * GET /dict/department/list
 * 响应：{ code, msg, data: [ { id, schoolId, name, code } ] }
 * @returns Promise 响应结果
 */
const getDepartmentList = () => {
  return request('get', '/dict/department/list')
}

export {
  getTermList,
  getCurrentTerm,
  getDepartmentList
}