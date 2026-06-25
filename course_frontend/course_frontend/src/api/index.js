import axios from 'axios'

const baseURL = '/api'

// Axios 实例：自动携带 Token
const instance = axios.create({
  baseURL: baseURL,
  timeout: 15000
})

// 请求拦截器：从 localStorage 取出 token，附加到 Authorization 头
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理 401 未授权，统一跳转登录页
instance.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 通用请求方法（基于 Axios 封装，自动携带 Token）
 * @param {string} method - HTTP 方法，如 'get', 'post', 'put', 'delete'
 * @param {string} url - 请求路径（会拼接 baseURL 前缀）
 * @param {object|null} payload - 请求体数据（GET 请求放到 params，POST/PUT/DELETE 放到 data）
 * @returns Promise
 */
const request = (method, url, payload = null) => {
  const config = {
    method: method,
    url: url
  }

  if (!payload) {
    return instance(config)
  }

  if (method === 'get') {
    config.params = payload
  } else {
    config.data = payload
  }

  return instance(config)
}

export { request }
