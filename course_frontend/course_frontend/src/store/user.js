import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理仓库
 * 负责管理登录用户的 token、用户ID、基本信息
 */
export const useUserStore = defineStore('user', () => {
  // 用户 token
  const token = ref(localStorage.getItem('token') || '')

  // 用户ID
  const userId = ref(localStorage.getItem('userId') || null)

  // 用户名称
  const name = ref(localStorage.getItem('name') || '')

  // 用户头像 URL
  const avatar = ref(localStorage.getItem('avatar') || '')

  // 用户手机号
  const phone = ref(localStorage.getItem('phone') || '')

  /**
   * 保存 token
   * @param {string} newToken - 登录成功后后端返回的 JWT token
   */
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 保存用户基本信息
   * @param {object} userInfo - 格式 { userId: number, name: string, avatar: string, phone: string }
   */
  const setUserInfo = (userInfo) => {
    userId.value = userInfo.userId
    name.value = userInfo.name
    avatar.value = userInfo.avatar || ''
    phone.value = userInfo.phone || ''

    // 同步写入 localStorage，页面刷新后状态不丢失
    localStorage.setItem('userId', userInfo.userId)
    localStorage.setItem('name', userInfo.name)
    localStorage.setItem('avatar', userInfo.avatar || '')
    localStorage.setItem('phone', userInfo.phone || '')
  }

  /**
   * 清除所有用户状态（退出登录时调用）
   */
  const clearUser = () => {
    token.value = ''
    userId.value = null
    name.value = ''
    avatar.value = ''
    phone.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('name')
    localStorage.removeItem('avatar')
    localStorage.removeItem('phone')
  }

  /**
   * 判断用户是否已登录
   * @returns {boolean}
   */
  const isLoggedIn = () => {
    return !!token.value
  }

  return {
    token,
    userId,
    name,
    avatar,
    phone,
    setToken,
    setUserInfo,
    clearUser,
    isLoggedIn
  }
})
