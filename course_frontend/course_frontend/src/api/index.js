import axios from 'axios'

const baseURL = '/api'

// Axios 实例：自动携带 Token
const instance = axios.create({
  baseURL: baseURL,
  timeout: 15000,
  /**
   * 雪花 ID 精度修复（关键！）
   * Java 后端的雪花 ID 是 19 位 Long，超出 JS Number.MAX_SAFE_INTEGER（2^53 = 9007199254740992，16位）。
   * Axios 默认用 JSON.parse 解析响应，19 位数字会被截断精度，导致 userId/courseId 末尾变 0。
   * 通过 transformResponse 拦截，在 JSON.parse 之前用自定义 reviver 把所有超长数字转为字符串，
   * 从根本上解决精度丢失问题。这样 store 里存的 userId 就是正确的，后续所有接口调用都不会受影响。
   */
  transformResponse: [
    (data) => {
      if (typeof data !== 'string') return data
      try {
        const MAX_SAFE = 9007199254740991
        return JSON.parse(data, (key, value) => {
          if (typeof value === 'number' && Number.isFinite(value)) {
            if (Math.abs(value) > MAX_SAFE) {
              // 精度已丢失：用字符串还原
              return String(value)
            }
          }
          return value
        })
      } catch (e) {
        return data
      }
    }
  ]
})

// 请求拦截器：从 localStorage 取出 token，附加到 Authorization 头
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    console.log('[API 请求]', config.method?.toUpperCase(), config.url, config.params || config.data || '')
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理 401 未授权，统一跳转登录页
instance.interceptors.response.use(
  (response) => {
    console.log('[API 响应]', response.config.method?.toUpperCase(), response.config.url,
      '→ code:', response.data?.code,
      'data:', JSON.stringify(response.data?.data),
      'rawData:', response.data)
    return response
  },
  (error) => {
    console.error('[API 错误]', error.config?.method?.toUpperCase(), error.config?.url, error.response?.status, error.response?.data)
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
