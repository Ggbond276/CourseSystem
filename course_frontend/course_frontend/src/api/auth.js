import { request } from './index'

/**
 * 账号密码登录
 * @param {object} data - 登录参数，格式 { schoolId: number, account: string, password: string }
 * @returns Promise 响应结果
 */
const login = (data) => {
  return request('post', '/auth/login', data)
}

/**
 * 用户注册
 * @param {object} data - 格式 { schoolId, account, password, name, phone }
 * @returns Promise 响应结果
 */
const register = (data) => {
  return request('post', '/auth/register', data)
}

/**
 * 获取所有学校列表
 * @returns Promise 响应结果
 */
const getSchoolList = () => {
  return request('get', '/auth/school-list')
}

export { login, register, getSchoolList }
