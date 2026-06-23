<template>
  <div class="homework-submissions-container">
    <!-- 顶部面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/teacher/course-manage' }">
          我的课程
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/teacher/homework/${homeworkId}/list`">
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

    <!-- 批阅对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="作业批阅"
      width="500px"
    >
      <el-form ref="gradeFormRef" :model="gradeForm" label-width="80px">
        <el-form-item label="得分">
          <el-input-number
            v-model="gradeForm.score"
            :min="0"
            :max="formMaxScore"
          />
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
            :rows="3"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="grading" @click="submitGrade">
          确认批阅
        </el-button>
      </template>
    </el-dialog>
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

// 批阅对话框
const gradeDialogVisible = ref(false)
const grading = ref(false)
const gradeFormRef = ref(null)

// 当前批阅的提交 ID
const currentSubmitId = ref(null)

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

// 打开批阅对话框
const handleGrade = (row) => {
  currentSubmitId.value = row.submitId
  formMaxScore.value = row.totalScore || 100
  gradeForm.value = {
    score: row.score || 0,
    status: row.status === 3 ? 3 : 2,
    teacherComment: row.teacherComment || ''
  }
  gradeDialogVisible.value = true
}

// 提交批阅
const submitGrade = async () => {
  grading.value = true
  try {
    const res = await gradeHomework({
      submitId: currentSubmitId.value,
      score: gradeForm.value.score,
      status: gradeForm.value.status,
      teacherComment: gradeForm.value.teacherComment
    })
    if (res.data.code === 200) {
      ElMessage.success('批阅成功')
      gradeDialogVisible.value = false
      loadSubmissionList()
    } else {
      ElMessage.error(res.data.msg || '批阅失败')
    }
  } catch (error) {
    console.error('批阅异常:', error)
    ElMessage.error('网络异常，请稍后重试')
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
</style>
