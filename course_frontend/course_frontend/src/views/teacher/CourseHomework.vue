<template>
  <div class="course-homework-container">
    <!-- 顶部面包屑 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/teacher/course-manage' }">
          我的课程
        </el-breadcrumb-item>
        <el-breadcrumb-item>作业管理</el-breadcrumb-item>
      </el-breadcrumb>
      <h2 class="course-name">{{ courseInfo.courseName }}</h2>
      <p class="course-info">班级：{{ courseInfo.className }} | 加课码：{{ courseInfo.joinCode }} | {{ courseInfo.memberCount }} 人</p>
    </div>

    <!-- 操作栏 -->
    <div class="top-bar">
      <el-button type="primary" @click="handleCreateHomework">
        发布作业
      </el-button>
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
          <el-tag :type="homework.isOver ? 'info' : 'success'" size="small">
            {{ homework.isOver ? '已截止' : '进行中' }}
          </el-tag>
        </div>
        <div class="homework-meta">
          <span class="activity-tag">{{ homework.activityTag }}</span>
          <span class="deadline">截止时间：{{ homework.deadline }}</span>
        </div>
        <div class="homework-stats">
          <span class="stat-item">已批 {{ homework.gradedCount }}</span>
          <span class="stat-item">待批 {{ homework.ungradedCount }}</span>
          <span class="stat-item">未交 {{ homework.unsubmittedCount }}</span>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="该课程暂无作业，点击上方按钮发布作业吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getTeacherHomeworkList } from '@/api/homework'

const route = useRoute()
const router = useRouter()

const courseId = route.params.courseId

// 课程基本信息
const courseInfo = ref({
  courseName: '',
  className: '',
  joinCode: '',
  memberCount: 0
})

// 作业列表
const homeworkList = ref([])

onMounted(() => {
  loadHomeworkList()
})

// 加载作业列表
const loadHomeworkList = async () => {
  try {
    const res = await getTeacherHomeworkList({ courseId })
    if (res.data.code === 200) {
      homeworkList.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载作业列表失败')
    }
  } catch (error) {
    console.error('加载作业列表异常:', error)
  }
}

// 跳转到作业详情/批阅页
const goToHomeworkDetail = (homeworkId) => {
  router.push(`/teacher/homework/${homeworkId}/submissions`)
}

// 发布新作业
const handleCreateHomework = () => {
  router.push(`/teacher/homework/create?courseId=${courseId}`)
}
</script>

<style scoped>
.course-homework-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.course-name {
  margin: 10px 0 5px;
  font-size: 20px;
  color: #333;
}

.course-info {
  font-size: 13px;
  color: #999;
}

.top-bar {
  margin-bottom: 20px;
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
  margin-bottom: 10px;
}

.homework-stats {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
