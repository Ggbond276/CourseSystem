<template>
  <div class="homework-detail-container">
    <!-- 顶部面包屑（点击项用编程式导航，避免路由不匹配时 catch-all 跳登录） -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>
          <span class="breadcrumb-link" @click="handleGoToMyCourses">我的课程</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <span
            class="breadcrumb-link"
            :class="{ 'is-disabled': !courseId }"
            @click="handleGoToCourseDetail"
          >
            作业列表
          </span>
        </el-breadcrumb-item>
        <el-breadcrumb-item>作业详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载作业详情...</span>
    </div>

    <!-- 作业详情内容 -->
    <div v-else-if="homeworkDetail" class="detail-content">
      <!-- 作业基本信息卡片 -->
      <el-card class="info-card">
        <h2 class="homework-title">{{ homeworkDetail.title }}</h2>
        <div class="homework-tags">
          <el-tag size="small">{{ homeworkDetail.activityTag }}</el-tag>
          <el-tag :type="isOver ? 'info' : 'success'" size="small">
            {{ isOver ? '已截止' : '进行中' }}
          </el-tag>
          <el-tag size="small">满分：{{ homeworkDetail.totalScore }}</el-tag>
        </div>
        <div class="deadline-info">
          <el-icon><Clock /></el-icon>
          <span>截止时间：{{ homeworkDetail.deadline }}</span>
        </div>
        <div class="description-section">
          <h4>作业要求</h4>
          <div class="description-text">{{ homeworkDetail.description }}</div>
        </div>
        <!-- 附件下载 -->
        <div v-if="homeworkDetail.attachments && homeworkDetail.attachments.length > 0" class="attachments-section">
          <h4>附件下载</h4>
          <div v-for="(att, index) in homeworkDetail.attachments" :key="index" class="attachment-item">
            <el-link :href="att.url" target="_blank" type="primary">
              {{ att.name }}
            </el-link>
          </div>
        </div>
      </el-card>

      <!-- 提交/批改记录卡片 -->
      <el-card class="submission-card">
        <h4 class="section-title">我的提交</h4>

        <!-- 未提交状态 -->
        <div v-if="!homeworkDetail.mySubmission" class="no-submission">
          <p>你还未提交此作业</p>
          <el-button type="primary" :disabled="isOver" @click="showSubmitDialog = true">
            提 交 作 业
          </el-button>
        </div>

        <!-- 已提交状态 -->
        <div v-else class="submission-info">
          <div class="submission-header">
            <!-- 打回重做：标签改为深红色+加粗，并且整个卡片边框变红，更醒目 -->
            <el-tag
              :type="statusTagType(homeworkDetail.mySubmission.status)"
              :class="{ 'tag-danger-prominent': homeworkDetail.mySubmission.status === 3 }"
              size="default"
            >
              {{ statusText(homeworkDetail.mySubmission.status) }}
            </el-tag>
            <span class="submit-time">提交时间：{{ homeworkDetail.mySubmission.submitTime }}</span>
          </div>

          <!-- 打回重做时顶部横幅：醒目的红色警告 -->
          <div v-if="homeworkDetail.mySubmission.status === 3" class="retry-banner">
            <el-alert type="error" :closable="false" show-icon>
              <template #title>
                <span>老师打回此作业，请在截止时间前根据评语修改并<strong>重新提交</strong>。</span>
              </template>
            </el-alert>
          </div>

          <div class="submission-content">
            <h5>提交内容</h5>
            <div class="content-text">{{ homeworkDetail.mySubmission.content }}</div>
          </div>

          <!-- 提交附件 -->
          <div v-if="homeworkDetail.mySubmission.attachments && homeworkDetail.mySubmission.attachments.length > 0" class="submit-attachments">
            <h5>提交附件</h5>
            <div v-for="(att, index) in homeworkDetail.mySubmission.attachments" :key="index">
              <el-link :href="att.url" target="_blank" type="primary">
                {{ att.name }}
              </el-link>
            </div>
          </div>

          <!-- 批改信息（只有已批改状态才显示） -->
          <div v-if="homeworkDetail.mySubmission.status === 2" class="grade-info">
            <h5>批改结果</h5>
            <div class="score-display">
              得分：<span class="score-number">{{ homeworkDetail.mySubmission.score ?? '--' }}</span>
              <span class="score-total"> / {{ homeworkDetail.totalScore }}</span>
            </div>
            <div class="comment-display">
              <h5>教师评语</h5>
              <div class="comment-text">{{ homeworkDetail.mySubmission.teacherComment || '暂无评语' }}</div>
            </div>
          </div>

          <!-- 打回重做：截止前允许重新提交 -->
          <div v-if="homeworkDetail.mySubmission.status === 3 && !isOver" class="retry-action">
            <el-button type="danger" @click="showSubmitDialog = true">
              重新提交
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 讨论区卡片 -->
      <el-card class="comment-card">
        <h4 class="section-title">讨论区</h4>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div
            v-for="comment in commentList"
            :key="comment.commentId"
            class="comment-item"
          >
            <div class="comment-avatar">
              <img :src="comment.userAvatar || defaultAvatar" alt="avatar" />
            </div>
            <div class="comment-body">
              <div class="comment-meta">
                <span class="comment-user">{{ comment.userName }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
            </div>
          </div>
          <el-empty v-if="commentList.length === 0" description="暂无讨论，快来发表第一条评论吧！" :image-size="60" />
        </div>

        <!-- 发布评论 -->
        <div class="comment-input-area">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="2"
            placeholder="请输入评论内容"
          />
          <div class="comment-actions">
            <el-checkbox v-model="isAnonymous">匿名发布</el-checkbox>
            <el-button type="primary" size="small" @click="handleAddComment">
              发 表
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 提交作业对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交作业" width="640px">
      <el-form ref="submitFormRef" :model="submitForm" label-width="90px">
        <el-form-item label="提交内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入你的作业内容或答案"
          />
        </el-form-item>
        <el-form-item label="上传附件">
          <!--
            el-upload 自定义上传：用 :http-request 拦截默认上传行为，
            内部走 axios 复用 token，方便后端鉴权（虽然现在 SecurityConfig 是全放行）
          -->
          <el-upload
            :file-list="fileList"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleUploadRemove"
            :before-upload="handleBeforeUpload"
            :http-request="customUploadRequest"
            :show-file-list="true"
            :drag="false"
            :limit="5"
            :on-exceed="handleExceed"
          >
            <el-button type="primary" :loading="uploading">
              <el-icon><Plus /></el-icon>
              <span>点击上传附件</span>
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF / Word / 图片 / 压缩包，单文件不超过 20MB，最多 5 个附件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitHomework">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Clock, Loading, Plus } from '@element-plus/icons-vue'
import { getStudentHomeworkDetail, submitHomework } from '@/api/homework'
import { getCommentList, addComment } from '@/api/comment'
import { uploadFile } from '@/api/upload'

const route = useRoute()
const router = useRouter()

const homeworkId = route.params.homeworkId
const courseId = route.params.courseId

// 加载状态
const loading = ref(false)

// 作业详情
const homeworkDetail = ref(null)

// 评论列表
const commentList = ref([])

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/40x40?text=User'

// 是否截止
const isOver = computed(() => {
  if (!homeworkDetail.value) return false
  return new Date() > new Date(homeworkDetail.value.deadline)
})

// 评论输入
const commentContent = ref('')
const isAnonymous = ref(false)

// 提交对话框
const showSubmitDialog = ref(false)
const submitting = ref(false)
const submitFormRef = ref(null)

// ★ 改造：提交表单新增附件数组（已上传成功的文件元数据列表）
//  格式：[{ name: 原文件名, url: 后端返回的可访问 URL }]
const submitForm = ref({
  content: '',
  attachments: []
})

// ★ 新增：上传中的文件列表（el-upload 内部用）
//  我们用它来显示文件名 + 上传进度 + 已上传成功的访问 URL
const fileList = ref([])

// ★ 新增：上传是否处理中（控制提交按钮的 loading）
const uploading = ref(false)

// 打开提交对话框时，把表单恢复到初始状态
watch(showSubmitDialog, (newVisible) => {
  if (newVisible) {
    // 只在"打开"瞬间清，避免与正在编辑中的内容冲突
    submitForm.value = { content: '', attachments: [] }
    fileList.value = []
  }
})

onMounted(() => {
  loadHomeworkDetail()
  loadCommentList()
})

/**
 * 面包屑 - 跳转到「我的课程」列表
 * 已校验 router/index.js 中的实际路径是 /student/courses（不是 /student/my-course）
 */
const handleGoToMyCourses = () => {
  router.push('/student/courses')
}

/**
 * 面包屑 - 跳转到「作业列表」(课程详情页)
 * 注意：当前路由只声明了 :homeworkId，courseId 可能是 undefined
 * 如果 courseId 缺失，则降级到「我的课程」，避免跳到不存在的路径触 catch-all
 */
const handleGoToCourseDetail = () => {
  if (courseId) {
    router.push(`/student/course/${courseId}`)
  } else {
    ElMessage.warning('未携带课程参数，已返回课程列表')
    router.push('/student/courses')
  }
}

// 加载作业详情
const loadHomeworkDetail = async () => {
  loading.value = true
  try {
    const res = await getStudentHomeworkDetail({ homeworkId })
    if (res.data.code === 200) {
      homeworkDetail.value = res.data.data
    } else {
      // 作业不存在：弹窗提示并跳回课程详情，避免用户卡在空白页
      ElMessage.error(res.data.msg || '作业不存在或已删除')
      setTimeout(() => {
        if (courseId) {
          router.replace(`/student/course/detail/${courseId}`)
        } else {
          router.replace('/student/courses')
        }
      }, 1200)
    }
  } catch (error) {
    console.error('加载作业详情异常:', error)
    ElMessage.error('网络异常，无法加载作业详情')
    setTimeout(() => {
      if (courseId) {
        router.replace(`/student/course/detail/${courseId}`)
      } else {
        router.replace('/student/courses')
      }
    }, 1200)
  } finally {
    loading.value = false
  }
}

// 加载评论列表
const loadCommentList = async () => {
  try {
    const res = await getCommentList({ homeworkId })
    if (res.data.code === 200) {
      commentList.value = res.data.data
    }
  } catch (error) {
    console.error('加载评论列表异常:', error)
  }
}

// 发表评论
const handleAddComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const res = await addComment({
      homeworkId,
      content: commentContent.value,
      isAnonymous: isAnonymous.value ? 1 : 0
    })
    if (res.data.code === 200) {
      ElMessage.success('发表评论成功')
      commentContent.value = ''
      loadCommentList()
    } else {
      ElMessage.error(res.data.msg || '发表评论失败')
    }
  } catch (error) {
    console.error('发表评论异常:', error)
    ElMessage.error('网络异常，请稍后重试')
  }
}

// 提交作业
const handleSubmitHomework = async () => {
  // 1. 前端先做 trim 检查，与后端 StudentHomeworkServiceImpl.submitHomework 的 trim 防御保持一致
  if (!submitForm.value.content || !submitForm.value.content.trim()) {
    ElMessage.warning('请输入作业内容')
    return
  }
  // 防止用户在上传未完成时点击提交（极少发生，但避免脏数据）
  if (uploading.value) {
    ElMessage.warning('附件正在上传中，请稍候')
    return
  }
  submitting.value = true
  try {
    // 2. homeworkId 是 19 位雪花 ID 的字符串（路由参数）；前端不会精度丢失
    //    attachments 直接传 fileList 里我们手动维护的 {name,url} 数组
    const res = await submitHomework({
      homeworkId,
      content: submitForm.value.content.trim(),
      attachments: submitForm.value.attachments || []
    })
    if (res.data.code === 200) {
      ElMessage.success('作业提交成功')
      showSubmitDialog.value = false
      // 3. 重新拉详情，让 mySubmission 状态从 null → 已提交
      loadHomeworkDetail()
    } else {
      ElMessage.error(res.data.msg || '提交作业失败')
    }
  } catch (error) {
    console.error('提交作业异常:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// ==================== 文件上传相关回调 ====================

/**
 * el-upload :before-upload：在文件真正上传前做客户端校验
 * 不通过返回 false，el-upload 自动跳过这次上传
 */
const handleBeforeUpload = (file) => {
  // 单文件 20MB 上限（与后端 UploadController.MAX_FILE_SIZE 一致）
  const MAX = 20 * 1024 * 1024
  if (file.size > MAX) {
    ElMessage.error(`文件「${file.name}」超过 20MB，请压缩后再上传`)
    return false
  }
  uploading.value = true
  return true
}

/**
 * el-upload :http-request：用我们的 axios 封装走 token，方便后期接入鉴权
 */
const customUploadRequest = async (options) => {
  try {
    const res = await uploadFile(options.file)
    // 我们封装里的 res 即 axios 完整响应，结构是 { data: { code, msg, data: { fileUrl, ... } } }
    if (res?.data?.code === 200 && res.data.data) {
      // 把后端返回的文件元数据塞回 el-upload 的 onSuccess，让它接管 UI 状态
      options.onSuccess(res.data.data)
    } else {
      options.onError(new Error(res?.data?.msg || '上传失败'))
    }
  } catch (err) {
    options.onError(err)
  }
}

/**
 * el-upload :on-success：单个文件上传成功的回调
 * 我们在这里把 {name, url} 写到 submitForm.attachments，确认提交时一起发给后端
 */
const handleUploadSuccess = (response, uploadFileObject) => {
  uploading.value = false
  if (response && response.fileUrl) {
    submitForm.value.attachments.push({
      name: response.originalName || uploadFileObject.name,
      url: response.fileUrl
    })
    ElMessage.success(`附件「${uploadFileObject.name}」上传成功`)
  } else {
    ElMessage.error('附件上传失败：未返回文件地址')
  }
}

/**
 * el-upload :on-error：单个文件上传失败的回调
 */
const handleUploadError = (error) => {
  uploading.value = false
  console.error('附件上传失败:', error)
  ElMessage.error('附件上传失败，请稍后重试')
}

/**
 * el-upload :on-remove：用户在文件列表里点 X 删除已上传的附件
 * 我们同步把它从 submitForm.attachments 里清掉，否则会出现"附件被删但提交时还发"
 */
const handleUploadRemove = (removedFile) => {
  submitForm.value.attachments = submitForm.value.attachments.filter(
    (att) => att.url !== removedFile.url
  )
}

/**
 * el-upload :on-exceed：超过 max 数量限制的提示
 */
const handleExceed = () => {
  ElMessage.warning('最多只能上传 5 个附件')
}

// 状态文字
const statusText = (status) => {
  const map = { 0: '未交', 1: '待批阅', 2: '已批改', 3: '打回重做' }
  return map[status] || '未知'
}

// 状态标签颜色：0=灰(未交), 1=橙(待批), 2=绿(已批), 3=红(打回)
const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

</script>

<style scoped>
.homework-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

/* 面包屑可点击项：视觉上像超链 */
.breadcrumb-link {
  cursor: pointer;
  color: #409eff;
  transition: color 0.2s;
}
.breadcrumb-link:hover {
  text-decoration: underline;
}
.breadcrumb-link.is-disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}
.breadcrumb-link.is-disabled:hover {
  text-decoration: none;
}

.loading-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #999;
  padding: 40px;
  justify-content: center;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.homework-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 10px;
}

.homework-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.deadline-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
  margin-bottom: 15px;
}

.description-section h4,
.attachments-section h4,
.section-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.description-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.attachment-item {
  margin-bottom: 4px;
}

.submission-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

/* 打回重做的标签：字体加粗，比默认 danger 更醒目 */
.tag-danger-prominent {
  font-weight: 700;
  font-size: 14px;
}

/* 打回重做的红色警告横幅 */
.retry-banner {
  margin-bottom: 15px;
}

/* 打回重做的重新提交按钮 */
.retry-action {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #fca5a5;
}

.submit-time {
  font-size: 13px;
  color: #999;
}

.submission-content h5,
.submit-attachments h5,
.grade-info h5,
.comment-display h5 {
  font-size: 13px;
  color: #333;
  margin: 10px 0 6px;
}

.content-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.score-display {
  font-size: 16px;
  color: #409eff;
  margin-bottom: 10px;
}

.score-number {
  font-size: 24px;
  font-weight: 700;
}

.score-total {
  font-size: 14px;
  color: #999;
}

.comment-display {
  margin-top: 10px;
}

.comment-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.comment-list {
  margin-bottom: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.comment-body {
  flex: 1;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.comment-user {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.no-submission {
  text-align: center;
  padding: 20px;
  color: #999;
}

.no-submission p {
  margin-bottom: 15px;
}
</style>
