import axios from 'axios'

const baseURL = '/api'

/**
 * 通用请求方法（基于 Axios 封装）
 * @param {string} method - HTTP 方法，如 'get', 'post', 'put', 'delete'
 * @param {string} url - 请求路径（会拼接 baseURL 前缀）
 * @param {object|null} payload - 请求体数据（GET 请求放到 params，POST/PUT/DELETE 放到 data）
 * @returns Promise
 */
const request = (method, url, payload = null) => {
  const config = {
    method: method,
    url: `${baseURL}${url}`
  }

  if (!payload) {
    return axios(config)
  }

  if (method === 'get') {
    config.params = payload
  } else {
    config.data = payload
  }

  return axios(config)
}

export { request }
