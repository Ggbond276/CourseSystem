import { request } from './index'

/**
 * 上传单个文件到后端（本地磁盘版本，后续可平滑替换为 OSS）
 *
 * 接入说明：
 *   - 后端接口：POST /upload/file  form-data: file=<二进制>
 *   - 响应：{ code, msg, data: { fileName, originalName, fileUrl, size } }
 *   - 注意：必须用 FormData 提交，axios 会自动设置 multipart/form-data 头 + boundary
 *
 * @param {File} file - el-upload :http-request 回调里的 file 参数，或 input[type=file].files[0]
 * @returns Promise<{fileName,originalName,fileUrl,size}> - 上传成功后返回的文件元信息（关键字段 fileUrl）
 */
const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)

  // 必须手动指定 Content-Type，否则 axios 默认发 JSON 头会丢 boundary
  // 这里不显式设置 axios，会自动检测 FormData 并加 boundary
  return request('post', '/upload/file', formData)
}

export { uploadFile }
