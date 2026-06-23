<template>
  <div class="homework-detail-container">
    <!-- 顶部面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/student/my-course' }">
          我的课程
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/student/course/${courseId}`">
          作业列表
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
            <el-tag :type="statusTagType(homeworkDetail.mySubmission.status)" size="small">
              {{ statusText(homeworkDetail.mySubmission.status) }}
            </el-tag>
            <span class="submit-time">提交时间：{{ homeworkDetail.mySubmission.submitTime }}</span>
          </div>

          <div class="submission-content">
            <h5>提交内容</h5>
            <div class="content-text">{{ homeworkDetail.mySubmission.content }}</div>
          </div>

          <!-- 提交附件 -->
          <div v-if="homeworkDetail.mySubmission.submitAttachments && homeworkDetail.mySubmission.submitAttachments.length > 0" class="submit-attachments">
            <h5>提交附件</h5>
            <div v-for="(att, index) in homeworkDetail.mySubmission.submitAttachments" :key="index">
              <el-link :href="att.url" target="_blank" type="primary">
                {{ att.name }}
              </el-link>
            </div>
          </div>

          <!-- 批改信息 -->
          <div v-if="homeworkDetail.mySubmission.status === 2" class="grade-info">
            <h5>批改结果</h5>
            <div class="score-display">
              得分：<span class="score-number">{{ homeworkDetail.mySubmission.score }}</span>
              <span class="score-total"> / {{ homeworkDetail.totalScore }}</span>
            </div>
            <div class="comment-display">
              <h5>教师评语</h5>
              <div class="comment-text">{{ homeworkDetail.mySubmission.teacherComment || '暂无评语' }}</div>
            </div>
          </div>

          <!-- 打回重做提示 -->
          <div v-if="homeworkDetail.mySubmission.status === 3" class="retry-hint">
            <el-alert type="warning" :closable="false">
              老师打回此作业，请根据评语修改后重新提交。
            </el-alert>
            <el-button
              type="primary"
              style="margin-top: 10px"
              :disabled="isOver"
              @click="showSubmitDialog = true"
            >
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
    <el-dialog v-model="showSubmitDialog" title="提交作业" width="500px">
      <el-form ref="submitFormRef" :model="submitForm" label-width="80px">
        <el-form-item label="提交内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入你的作业内容或答案"
          />
        </el-form-item>
        <el-form-item label="上传附件">
          <!-- TODO: 接入 OSS 文件上传组件 -->
          <el-input v-model="submitForm.attachmentPlaceholder" placeholder="文件上传功能待接入" disabled />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { Clock, Loading } from '@element-plus/icons-vue'
import { getStudentHomeworkDetail } from '@/api/homework'
import { getCommentList, addComment } from '@/api/comment'

const route = useRoute()

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

const submitForm = ref({
  content: '',
  attachmentPlaceholder: ''
})

onMounted(() => {
  loadHomeworkDetail()
  loadCommentList()
})

// 加载作业详情
const loadHomeworkDetail = async () => {
  loading.value = true
  try {
    const res = await getStudentHomeworkDetail({ homeworkId })
    if (res.data.code === 200) {
      homeworkDetail.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载作业详情失败')
    }
  } catch (error) {
    console.error('加载作业详情异常:', error)
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
  submitting.value = true
  try {
    const res = await submitHomework({
      homeworkId,
      content: submitForm.value.content,
      attachments: []
    })
    if (res.data.code === 200) {
      ElMessage.success('作业提交成功')
      showSubmitDialog.value = false
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

// 状态文字
const statusText = (status) => {
  const map = { 0: '未交', 1: '已提交', 2: '已批改', 3: '打回重做' }
  return map[status] || '未知'
}

// 状态标签颜色
const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

// 单独引入 submitHomework 函数（这里需要从 api 导入）
import { submitHomework } from '@/api/homework'
</script>

<style scoped>
.homework-detail-container {
  padding: 20px;
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
