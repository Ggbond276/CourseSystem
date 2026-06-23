import { request } from './index'

/**
 * 获取当前作业详情页下的所有评论交流列表
 * @param {object} params - 格式 { homeworkId: number }
 * @returns Promise 响应结果
 */
const getCommentList = (params) => {
  return request('get', '/homework/comment/list', params)
}

/**
 * 在作业详情页下发表留言/讨论
 * @param {object} data - 格式 { homeworkId: number, content: string, isAnonymous: number }
 * @returns Promise 响应结果
 */
const addComment = (data) => {
  return request('post', '/homework/comment/add', data)
}

export { getCommentList, addComment }
