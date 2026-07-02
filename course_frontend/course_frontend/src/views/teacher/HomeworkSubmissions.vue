<template>
  <div class="homework-submissions-container">
    <!-- 顶部面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ name: 'TeacherCourses' }">
          我的课程
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="{ name: 'TeacherDashboard' }">
          作业管理
        </el-breadcrumb-item>
        <el-breadcrumb-item>批阅大厅</el-breadcrumb-item>
      </el-breadcrumb>
      <h2 class="page-title">{{ homeworkTitle }} - 批阅大厅</h2>
    </div>

    <!-- 筛选操作栏 -->
    <div class="filter-bar">
      <el-select
        v-model="filterStatus"
        placeholder="筛选状态"
        clearable
        style="width: 150px"
        @change="loadSubmissionList"
      >
        <el-option label="待批阅" :value="1" />
        <el-option label="已批改" :value="2" />
        <el-option label="打回重做" :value="3" />
      </el-select>
      <el-input
        v-model="filterStudentName"
        placeholder="按学生姓名搜索"
        clearable
        style="width: 200px"
        @change="loadSubmissionList"
      />
      <el-button type="primary" @click="loadSubmissionList">
        搜 索
      </el-button>
    </div>

    <!-- 提交列表 -->
    <div v-if="submissions.length > 0" class="submission-list">
      <el-table :data="submissions" border stripe>
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="studentAccount" label="学号" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" />
        <el-table-column label="相似度">
          <template #default="{ row }">
            <span :class="similarityClass(row.similarity)">
              {{ row.similarity }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="wordCount" label="字数" />
        <el-table-column label="得分">
          <template #default="{ row }">
            <span v-if="row.score !== null">{{ row.score }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              :disabled="row.status === 2 || row.status === 3"
              :class="{ 'btn-disabled-gray': row.status === 2 || row.status === 3 }"
              @click="handleGrade(row)"
            >
              批阅
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无提交记录" />

    <!--
      ★ 重构：作业批阅改为右侧抽屉（el-drawer）。
      抽屉宽度 600px，符合"至少 50% 或 600px"的产品设计要求。
      标题动态展示 "作业批阅 - [学生姓名]"。
      内部布局上下分层：
        上半区：只读地展示学生提交的文本内容 + 附件下载按钮
        下半区：原表单（得分 / 状态 / 评语）
    -->
    <el-drawer
      v-model="gradeDrawerVisible"
      :title="`作业批阅 - ${gradingStudent.studentName || '未命名学生'}`"
      direction="rtl"
      size="640px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <!-- 抽屉正文 -->
      <div class="grade-drawer-body" v-if="gradingStudent.submitId">

        <!-- 上半区：学生作答区（只读） -->
        <section class="student-answer-section">
          <h4 class="section-title">学生作答</h4>

          <!-- 提交状态 + 提交时间 -->
          <div class="meta-row">
            <el-tag :type="statusTagType(gradingStudent.status)" size="small">
              {{ statusText(gradingStudent.status) }}
            </el-tag>
            <span class="meta-time">
              {{ gradingStudent.submitTime || '尚未提交' }}
            </span>
            <span v-if="gradingStudent.wordCount !== null && gradingStudent.wordCount !== undefined" class="meta-word">
              字数：{{ gradingStudent.wordCount }}
            </span>
            <span v-if="gradingStudent.similarity !== null && gradingStudent.similarity !== undefined" class="meta-sim">
              相似度：{{ gradingStudent.similarity }}%
            </span>
          </div>

          <!-- 文本内容（只读卡片） -->
          <div class="readonly-card">
            <div class="readonly-card-label">提交内容</div>
            <div v-if="gradingStudent.content && gradingStudent.content.trim()" class="readonly-card-text">
              {{ gradingStudent.content }}
            </div>
            <div v-else class="readonly-card-empty">
              学生未填写任何文本内容
            </div>
          </div>

          <!-- 附件下载区（可点击的卡片按钮组） -->
          <div v-if="gradingStudent.attachments && gradingStudent.attachments.length > 0" class="readonly-card">
            <div class="readonly-card-label">提交的附件（{{ gradingStudent.attachments.length }}个）</div>
            <div class="attachment-btn-group">
              <a
                v-for="(att, idx) in gradingStudent.attachments"
                :key="idx"
                :href="att.url"
                target="_blank"
                class="attachment-card-btn"
              >
                <!-- 文件图标 -->
                <span class="att-icon">{{ fileTypeIcon(att.name) }}</span>
                <!-- 文件名（超长截断） -->
                <span class="att-name" :title="att.name">{{ att.name || ('附件' + (idx + 1)) }}</span>
                <!-- 预览/下载标签 -->
                <span class="att-action">{{ isPreviewable(att.name) ? '预览' : '下载' }}</span>
              </a>
            </div>
          </div>
          <div v-else class="readonly-card">
            <div class="readonly-card-label">提交的附件</div>
            <div class="readonly-card-empty">未上传任何附件</div>
          </div>
        </section>

        <!-- 上下分隔线 -->
        <el-divider />

        <!-- 下半区：教师打分栏 -->
        <section class="teacher-grade-section">
          <h4 class="section-title">教师批阅</h4>

          <el-form ref="gradeFormRef" :model="gradeForm" label-width="84px">
            <el-form-item label="得分">
              <el-input-number
                v-model="gradeForm.score"
                :min="0"
                :max="formMaxScore"
                :step="1"
              />
              <span class="form-hint">满分 {{ formMaxScore }} 分</span>
            </el-form-item>
            <el-form-item label="批阅状态">
              <el-radio-group v-model="gradeForm.status">
                <el-radio :label="2">已批改</el-radio>
                <el-radio :label="3">打回重做</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="评语">
              <el-input
                v-model="gradeForm.teacherComment"
                type="textarea"
                :rows="4"
                placeholder="请输入评语（打回时建议说明原因）"
              />
            </el-form-item>
          </el-form>

          <div class="drawer-footer">
            <el-button @click="gradeDrawerVisible = false">取 消</el-button>
            <el-button
              type="primary"
              :loading="grading"
              @click="submitGrade"
            >
              确认批阅
            </el-button>
          </div>
        </section>
      </div>
      <!-- 抽屉里没有任何选中提交时的占位（理论上不会走到这里，只是兜底） -->
      <div v-else style="padding: 40px; text-align: center; color: #999;">
        请在左侧表格选择一条提交记录进行批阅
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { getSubmissionList, gradeHomework } from '@/api/homework'

const route = useRoute()

const homeworkId = route.params.homeworkId

// 作业标题
const homeworkTitle = ref('')

// 筛选条件
const filterStatus = ref(null)
const filterStudentName = ref('')

// 提交列表
const submissions = ref([])

// 满分值
const formMaxScore = ref(100)

// 批阅抽屉（替代原来的 dialog）
const gradeDrawerVisible = ref(false)
const grading = ref(false)
const gradeFormRef = ref(null)

// 当前批阅的提交 ID
const currentSubmitId = ref(null)

// 当前批阅的学生信息（用于抽屉上半部分学生作答区展示）
// 包含后端 /homework/teacher/submit-list 接口返回的所有字段，
// 关键字段：studentName / status / submitTime / similarity / wordCount / content / attachments
const gradingStudent = ref({
  submitId: null,
  studentName: '',
  studentAccount: '',
  status: null,
  submitTime: '',
  similarity: null,
  wordCount: null,
  score: null,
  content: '',
  attachments: []
})

// 批阅表单
const gradeForm = ref({
  score: 0,
  status: 2,
  teacherComment: ''
})

onMounted(() => {
  loadSubmissionList()
})

// 加载提交列表
const loadSubmissionList = async () => {
  try {
    const params = {
      homeworkId,
      status: filterStatus.value,
      studentName: filterStudentName.value
    }
    const res = await getSubmissionList(params)
    if (res.data.code === 200) {
      submissions.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载提交列表失败')
    }
  } catch (error) {
    console.error('加载提交列表异常:', error)
  }
}

// 状态标签文字
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

/**
 * 根据文件后缀返回 Emoji 图标（让附件列表更直观）
 */
const fileTypeIcon = (fileName) => {
  if (!fileName) return '📄'
  const lower = fileName.toLowerCase()
  if (lower.endsWith('.pdf')) return '📕'
  if (lower.endsWith('.doc') || lower.endsWith('.docx')) return '📘'
  if (lower.endsWith('.xls') || lower.endsWith('.xlsx')) return '📊'
  if (lower.endsWith('.ppt') || lower.endsWith('.pptx')) return '📙'
  if (lower.endsWith('.zip') || lower.endsWith('.rar') || lower.endsWith('.7z')) return '📦'
  if (lower.endsWith('.png') || lower.endsWith('.jpg') || lower.endsWith('.jpeg') || lower.endsWith('.gif') || lower.endsWith('.webp')) return '🖼️'
  if (lower.endsWith('.txt')) return '📝'
  if (lower.endsWith('.mp4') || lower.endsWith('.avi') || lower.endsWith('.mov')) return '🎬'
  if (lower.endsWith('.mp3') || lower.endsWith('.wav') || lower.endsWith('.aac')) return '🎵'
  return '📎'
}

/**
 * 根据后缀判断浏览器是否支持直接在线预览（PDF / 图片 / 纯文本）
 * 其他格式 → 触发下载
 */
const isPreviewable = (fileName) => {
  if (!fileName) return false
  const lower = fileName.toLowerCase()
  return lower.endsWith('.pdf') ||
    lower.endsWith('.png') || lower.endsWith('.jpg') ||
    lower.endsWith('.jpeg') || lower.endsWith('.gif') ||
    lower.endsWith('.webp') || lower.endsWith('.txt')
}

// 打开批阅抽屉（替代原来的对话框）
// 关键改动：把表格行的整行数据存到 gradingStudent，抽屉上半部分直接渲染
const handleGrade = (row) => {
  currentSubmitId.value = row.submitId
  formMaxScore.value = row.totalScore || 100
  // 整行数据 = 学生信息 + 提交内容 + 附件。后端 submit-list 接口已经返回了这些字段
  gradingStudent.value = {
    submitId: row.submitId,
    studentName: row.studentName || '',
    studentAccount: row.studentAccount || '',
    status: row.status,
    submitTime: row.submitTime || '',
    similarity: row.similarity,
    wordCount: row.wordCount,
    score: row.score,
    content: row.content || '',
    attachments: Array.isArray(row.attachments) ? row.attachments : []
  }
  gradeForm.value = {
    score: row.score || 0,
    status: row.status === 3 ? 3 : 2,
    teacherComment: row.teacherComment || ''
  }
  gradeDrawerVisible.value = true
}

// 提交批阅
const submitGrade = async () => {
  grading.value = true
  try {
    // 后端 Controller 用 String.trim() 校验 submitId，必须是字符串
    const res = await gradeHomework({
      submitId: String(currentSubmitId.value),
      score: gradeForm.value.score,
      status: gradeForm.value.status,
      teacherComment: gradeForm.value.teacherComment
    })
    if (res.data.code === 200) {
      ElMessage.success('批阅成功')
      gradeDrawerVisible.value = false
      loadSubmissionList()
    } else {
      ElMessage.error(res.data.msg || '批阅失败')
    }
  } catch (error) {
    // 展示后端返回的真实错误信息，而非吞成"网络异常"
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      console.error('批阅异常:', error)
      ElMessage.error('网络异常，请稍后重试')
    }
  } finally {
    grading.value = false
  }
}
</script>

<style scoped>
.homework-submissions-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 10px 0 0;
  font-size: 20px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.similarity-high {
  color: #f56c6c;
  font-weight: 600;
}

.similarity-mid {
  color: #e6a23c;
}

/* 批阅按钮：已批改/打回重做时强制灰色，不管 Element Plus 的默认 disabled 样式 */
:deep(.btn-disabled-gray) {
  color: #c0c4cc !important;
  cursor: not-allowed !important;
  pointer-events: none;
}

/* ========== 抽屉内部样式 ========== */
.grade-drawer-body {
  padding: 0 4px;
}

/* 小节标题 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 14px 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

/* 上半区：学生作答 */
.student-answer-section {
  margin-bottom: 18px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.meta-time,
.meta-word,
.meta-sim {
  color: #909399;
}

/* 只读文本卡片 */
.readonly-card {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px 14px;
  margin-bottom: 12px;
}

.readonly-card-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.readonly-card-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
}

.readonly-card-empty {
  font-size: 13px;
  color: #c0c4cc;
  font-style: italic;
}

/* 附件卡片按钮组（每个附件一个独立按钮，cursor:pointer 明确告知可点击） */
.attachment-btn-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-card-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
  color: #303133;
}

.attachment-card-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  transform: translateX(2px);
}

.att-icon {
  font-size: 18px;
  flex-shrink: 0;
  line-height: 1;
}

.att-name {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-card-btn:hover .att-name {
  color: #409eff;
}

.att-action {
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 10px;
  background: #f0f2f5;
  color: #909399;
  flex-shrink: 0;
  border: 1px solid #e4e7ed;
}

.attachment-card-btn:hover .att-action {
  background: #409eff;
  color: #ffffff;
  border-color: #409eff;
}

/* 下半区：教师打分 */
.teacher-grade-section {
  padding-top: 4px;
}

.form-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #ebeef5;
}
</style>
