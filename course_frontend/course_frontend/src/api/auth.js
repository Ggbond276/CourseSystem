import { request } from './index'

/**
 * 账号密码登录
 * @param {object} data - 登录参数，格式 { account: string, password: string }
 * @returns Promise 响应结果
 */
const login = (data) => {
  return request('post', '/auth/login', data)
}

export { login }
