<template>
  <div class="grade-detail-page">
    <!-- 顶部面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/teacher/course-manage' }">
          我的课程
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/teacher/homework/${homeworkId}/submissions`">
          批阅大厅
        </el-breadcrumb-item>
        <el-breadcrumb-item>批改作业</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：学生提交内容 -->
      <el-col :xs="24" :md="14">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">学生提交内容</span>
              <el-tag :type="statusTagType(currentSubmission?.status)" size="small">
                {{ statusText(currentSubmission?.status) }}
              </el-tag>
            </div>
          </template>

          <div v-if="loading" class="loading-area">
            <el-icon class="is-loading" :size="24"><Loading /></el-icon>
            <span>加载中...</span>
          </div>

          <div v-else-if="currentSubmission" class="submission-content">
            <div class="student-info">
              <div class="student-name">{{ currentSubmission.studentName }}</div>
              <div class="student-account">学号：{{ currentSubmission.studentAccount }}</div>
              <div class="submit-time">提交时间：{{ currentSubmission.submitTime }}</div>
            </div>

            <div class="content-section">
              <h4 class="section-title">作答内容</h4>
              <div class="content-text">{{ currentSubmission.content || '（未填写文本内容）' }}</div>
            </div>

            <div v-if="currentSubmission.attachments?.length > 0" class="attachments-section">
              <h4 class="section-title">提交附件</h4>
              <div
                v-for="(att, index) in currentSubmission.attachments"
                :key="index"
                class="attachment-item"
              >
                <el-icon><Document /></el-icon>
                <el-link :href="att.url" target="_blank" type="primary">
                  {{ att.name }}
                </el-link>
              </div>
            </div>

            <div class="meta-section">
              <div class="meta-item">
                <span class="meta-label">字数：</span>
                <span class="meta-value">{{ currentSubmission.wordCount || 0 }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">相似度：</span>
                <span :class="similarityClass(currentSubmission.similarity)">
                  {{ currentSubmission.similarity }}%
                </span>
              </div>
            </div>
          </div>

          <el-empty v-else description="未找到该学生的提交记录" />
        </el-card>
      </el-col>

      <!-- 右侧：批改表单 -->
      <el-col :xs="24" :md="10">
        <el-card class="panel-card grade-card">
          <template #header>
            <span class="panel-title">批改打分</span>
          </template>

          <el-form
            ref="gradeFormRef"
            :model="gradeForm"
            :rules="gradeRules"
            label-width="80px"
          >
            <el-form-item label="得分" prop="score">
              <el-input-number
                v-model="gradeForm.score"
                :min="0"
                :max="gradeForm.maxScore"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
              <div class="score-hint">满分：{{ gradeForm.maxScore }} 分</div>
            </el-form-item>

            <el-form-item label="批阅状态" prop="status">
              <el-radio-group v-model="gradeForm.status">
                <el-radio :label="2">已批改</el-radio>
                <el-radio :label="3">打回重做</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="评语" prop="teacherComment">
              <el-input
                v-model="gradeForm.teacherComment"
                type="textarea"
                :rows="4"
                placeholder="请输入评语（可选）"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="submitting"
                class="submit-btn"
                @click="handleSubmitGrade"
              >
                确认提交
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Loading, Document } from '@element-plus/icons-vue'
import { getSubmissionList, gradeHomework } from '@/api/homework'

const route = useRoute()
const router = useRouter()

const homeworkId = route.params.homeworkId
const submitId = route.query.submitId

const loading = ref(false)
const submitting = ref(false)
const currentSubmission = ref(null)
const gradeFormRef = ref(null)

// 批改表单
const gradeForm = reactive({
  score: 0,
  maxScore: 100,
  status: 2,
  teacherComment: ''
})

// 表单验证规则
const gradeRules = {
  score: [
    { required: true, message: '请输入得分', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择批阅状态', trigger: 'change' }
  ]
}

onMounted(() => {
  loadSubmissionDetail()
})

// 加载提交详情
const loadSubmissionDetail = async () => {
  loading.value = true
  try {
    const res = await getSubmissionList({ homeworkId })
    if (res.data.code === 200) {
      // 找到当前 submitId 对应的记录
      const list = res.data.data || []
      currentSubmission.value = list.find(item => item.submitId == submitId) || list[0] || null
      if (currentSubmission.value) {
        gradeForm.maxScore = currentSubmission.value.totalScore || 100
        gradeForm.score = currentSubmission.value.score || 0
        gradeForm.status = currentSubmission.value.status === 3 ? 3 : 2
        gradeForm.teacherComment = currentSubmission.value.teacherComment || ''
      }
    }
  } catch (error) {
    console.error('加载异常:', error)
  } finally {
    loading.value = false
  }
}

// 状态文字
const statusText = (status) => {
  const map = { 0: '未交', 1: '待批阅', 2: '已批改', 3: '打回重做' }
  return map[status] || '未知'
}

// 状态标签颜色
const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

// 相似度样式
const similarityClass = (value) => {
  if (value > 50) return 'similarity-high'
  if (value > 20) return 'similarity-mid'
  return ''
}

// 提交批阅
const handleSubmitGrade = async () => {
  if (!gradeFormRef.value) return

  await gradeFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 后端 Controller 用 String.trim() 校验 submitId，必须是字符串
      const targetSubmitId = currentSubmission.value?.submitId || submitId
      const res = await gradeHomework({
        submitId: String(targetSubmitId),
        score: gradeForm.score,
        status: gradeForm.status,
        teacherComment: gradeForm.teacherComment
      })
      if (res.data.code === 200) {
        ElMessage.success('批改成功')
        router.push(`/teacher/homework/${homeworkId}/submissions`)
      } else {
        ElMessage.error(res.data.msg || '批改失败')
      }
    } catch (error) {
      // 展示后端返回的真实错误信息，而非吞成"网络异常"
      if (error.response && error.response.data && error.response.data.msg) {
        ElMessage.error(error.response.data.msg)
      } else {
        console.error('批改异常:', error)
        ElMessage.error('网络异常，请稍后重试')
      }
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.grade-detail-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.loading-area {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
  padding: 40px;
  justify-content: center;
}

.panel-card {
  border-radius: 10px;
  margin-bottom: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

/* 学生信息 */
.student-info {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.student-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.student-account, .submit-time {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

/* 内容区 */
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 10px;
}

.content-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.attachments-section {
  margin-bottom: 20px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f8fafc;
  border-radius: 6px;
  margin-bottom: 6px;
}

/* 元数据 */
.meta-section {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.meta-label {
  color: #9ca3af;
}

.meta-value {
  color: #374151;
  font-weight: 600;
}

.similarity-high { color: #ef4444; font-weight: 700; }
.similarity-mid { color: #f59e0b; font-weight: 700; }

/* 批改卡片 */
.grade-card {
  position: sticky;
  top: 20px;
}

.score-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.submit-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
}
</style>
