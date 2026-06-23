<template>
  <div class="student-homework-container">
    <!-- 顶部面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/student/my-course' }">
          我的课程
        </el-breadcrumb-item>
        <el-breadcrumb-item>作业列表</el-breadcrumb-item>
      </el-breadcrumb>
      <h2 class="course-name">{{ courseInfo.courseName }}</h2>
    </div>

    <!-- 作业列表 -->
    <div v-if="homeworkList.length > 0" class="homework-list">
      <el-card
        v-for="homework in homeworkList"
        :key="homework.homeworkId"
        class="homework-card"
        @click="goToHomeworkDetail(homework.homeworkId)"
      >
        <div class="homework-header">
          <h3 class="homework-title">{{ homework.title }}</h3>
          <el-tag :type="statusTagType(homework.status)" size="small">
            {{ statusText(homework.status) }}
          </el-tag>
        </div>
        <div class="homework-meta">
          <span class="activity-tag">{{ homework.activityTag }}</span>
          <span class="deadline">截止时间：{{ homework.deadline }}</span>
        </div>
        <div v-if="homework.score !== null" class="homework-score">
          得分：<span class="score-value">{{ homework.score }}</span>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="该课程暂无作业" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getStudentHomeworkList } from '@/api/homework'

const route = useRoute()
const router = useRouter()

const courseId = route.params.courseId

// 课程基本信息
const courseInfo = ref({
  courseName: ''
})

// 作业列表
const homeworkList = ref([])

onMounted(() => {
  loadHomeworkList()
})

// 加载作业列表
const loadHomeworkList = async () => {
  try {
    const res = await getStudentHomeworkList({ courseId })
    if (res.data.code === 200) {
      homeworkList.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载作业列表失败')
    }
  } catch (error) {
    console.error('加载作业列表异常:', error)
  }
}

// 状态文字
const statusText = (status) => {
  const map = { 0: '未提交', 1: '已提交', 2: '已批改', 3: '打回重做' }
  return map[status] || '未知'
}

// 状态标签颜色
const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

// 跳转到作业详情
const goToHomeworkDetail = (homeworkId) => {
  router.push(`/student/homework/${homeworkId}`)
}
</script>

<style scoped>
.student-homework-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.course-name {
  margin: 10px 0 0;
  font-size: 20px;
  color: #333;
}

.homework-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.homework-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.homework-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.homework-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.homework-title {
  font-size: 16px;
  font-weight: 600;
}

.homework-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.homework-score {
  font-size: 13px;
  color: #409eff;
}

.score-value {
  font-weight: 600;
  font-size: 16px;
}
</style>
