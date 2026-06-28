<template>
  <div class="student-dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.name || '同学' }}</h2>
        <p class="welcome-subtitle">继续保持学习的热情，每一份努力都会被看见。</p>
      </div>
      <div class="welcome-right">
        <el-button type="primary" size="large" @click="$router.push('/student/courses')">
          <el-icon><Reading /></el-icon>
          <span>进入我的课程</span>
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="24" color="#ffffff"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主内容：待办作业 + 最近课程 -->
    <el-row :gutter="16" class="content-row">
      <!-- 左侧：待办作业 -->
      <el-col :xs="24" :md="12">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">待办作业</span>
              <el-button text type="primary" @click="$router.push('/student/courses')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-if="pendingHomeworks.length > 0" class="homework-list">
            <div
              v-for="hw in pendingHomeworks"
              :key="hw.id"
              class="homework-item"
              @click="$router.push(`/student/homework/${hw.homeworkId}`)"
            >
              <div class="hw-main">
                <div class="hw-title">{{ hw.title }}</div>
                <div class="hw-meta">
                  <span class="hw-course">{{ hw.courseName }}</span>
                  <span class="hw-deadline">
                    <el-icon><Clock /></el-icon>
                    {{ hw.deadline }}
                  </span>
                </div>
              </div>
              <div class="hw-status">
                <el-tag type="warning" size="small">待提交</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待办作业" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 右侧：最近课程 -->
      <el-col :xs="24" :md="12">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">我的课程</span>
              <el-button text type="primary" @click="$router.push('/student/courses')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-if="recentCourses.length > 0" class="course-list">
            <div
              v-for="course in recentCourses"
              :key="course.id"
              class="course-item"
              @click="$router.push(`/student/course/detail/${course.id}`)"
            >
              <div class="course-cover" :style="{ backgroundColor: course.coverColor || '#2563eb' }">
                <span class="course-initial">{{ course.courseName?.charAt(0) || '课' }}</span>
              </div>
              <div class="course-info">
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-teacher">{{ course.teacherName }}</div>
              </div>
              <el-tag size="small" type="success">{{ course.homeworkCount }} 作业</el-tag>
            </div>
          </div>
          <el-empty v-else description="还没有加入课程" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Reading, Notebook, Document, Trophy, Calendar, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 根据当前小时返回问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 顶部统计卡片（占位）
const statCards = ref([
  { label: '在修课程', value: '--', icon: Notebook, color: '#2563eb' },
  { label: '待完成作业', value: '--', icon: Document, color: '#f59e0b' },
  { label: '已获学分', value: '--', icon: Trophy, color: '#10b981' },
  { label: '本周学习', value: '--', icon: Calendar, color: '#8b5cf6' }
])

// 待办作业（占位）
const pendingHomeworks = ref([])

// 最近课程（占位）
const recentCourses = ref([])
</script>

<style scoped>
.student-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* ========== 欢迎横幅 ========== */
.welcome-card {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  color: #ffffff;
  border-radius: 10px;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

/* ========== 统计卡片 ========== */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

/* ========== 面板卡片 ========== */
.content-row {
  margin-bottom: 20px;
}

.panel-card {
  border: 1px solid #e5e7eb;
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

/* ========== 作业列表 ========== */
.homework-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.homework-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.homework-item:hover {
  border-color: #2563eb;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1);
}

.hw-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.hw-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.hw-deadline {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 课程列表 ========== */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.course-item:hover {
  background: #eff6ff;
}

.course-cover {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.course-initial {
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
}

.course-info {
  flex: 1;
}

.course-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.course-teacher {
  font-size: 12px;
  color: #9ca3af;
}
</style>
