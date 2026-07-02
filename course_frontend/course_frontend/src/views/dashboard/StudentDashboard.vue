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
              :key="hw.homeworkId"
              class="homework-item"
              @click="$router.push(`/student/homework/${hw.homeworkId}`)"
            >
              <div class="hw-main">
                <div class="hw-title">{{ hw.title }}</div>
                <div class="hw-meta">
                  <span class="hw-course">{{ hw.courseName }}</span>
                  <span class="hw-deadline" :class="deadlineClass(hw.deadline)">
                    <el-icon><Clock /></el-icon>
                    {{ hw.deadline || '无截止时间' }}
                  </span>
                </div>
              </div>
              <div class="hw-status">
                <el-tag :type="statusTagType(hw.status)" size="small">
                  {{ statusText(hw.status) }}
                </el-tag>
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
              <div class="course-cover" :style="{ background: course.coverColor || '#2563eb' }">
                <span class="course-initial">{{ course.courseName?.charAt(0) || '课' }}</span>
              </div>
              <div class="course-info">
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-teacher">{{ course.teacherName }}</div>
              </div>
              <el-tag size="small" :type="course.status === 1 ? 'success' : 'info'">
                {{ course.status === 1 ? '进行中' : '已结束' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="还没有加入课程" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Reading, Notebook, Document, Trophy, Calendar, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getStudentPendingHomeworks, getStudentTotalScoredScore } from '@/api/homework'
import { getStudentCourseList } from '@/api/course'

const userStore = useUserStore()

onMounted(() => {
  loadPendingHomeworks()
  loadRecentCourses()
  loadTotalScore()
})

// 根据当前小时返回问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 顶部统计卡片：在修课程 / 待完成作业 / 累计获取得分 / 已获学分
const enrolledCourseCount = ref(0)
const pendingHomeworkCount = ref(0)
const totalScore = ref(0)
const totalCredit = ref(0)

const statCards = computed(() => [
  { label: '在修课程', value: enrolledCourseCount.value, icon: Notebook, color: '#2563eb' },
  { label: '待完成作业', value: pendingHomeworkCount.value, icon: Document, color: '#f59e0b' },
  { label: '累计得分', value: totalScore.value, icon: Trophy, color: '#10b981' },
  { label: '已获学分', value: totalCredit.value, icon: Calendar, color: '#8b5cf6' }
])

// 待办作业
const pendingHomeworks = ref([])

// 加载待办作业
const loadPendingHomeworks = async () => {
  try {
    const res = await getStudentPendingHomeworks()
    if (res.data.code === 200) {
      pendingHomeworks.value = res.data.data || []
      // 顶部统计：直接取列表长度
      pendingHomeworkCount.value = pendingHomeworks.value.length
    }
  } catch (error) {
    console.error('加载待办作业异常:', error)
  }
}

// 我的课程（按学期分组的所有课，最近学期优先展示）
const recentCourses = ref([])

// 课程封面色板（与 MyCourse.vue 一致）
const gradientPalette = [
  'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
  'linear-gradient(135deg, #10b981 0%, #059669 100%)',
  'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
  'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
  'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
  'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
  'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
  'linear-gradient(135deg, #14b8a6 0%, #0d9488 100%)'
]

// 根据课程 ID 稳定取一个渐变色
const getCoverGradient = (courseId) => {
  if (!courseId) return gradientPalette[0]
  const idStr = String(courseId)
  let sum = 0
  for (let i = 0; i < idStr.length; i++) {
    sum = sum + idStr.charCodeAt(i)
  }
  return gradientPalette[sum % gradientPalette.length]
}

// 加载我的课程列表（取所有学期下前几门）
const loadRecentCourses = async () => {
  const studentId = userStore.userId
  if (!studentId) return
  try {
    const res = await getStudentCourseList(studentId)
    if (res && res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
      // 接口返回 [{ term, courses: [...] }]，按学期降序后取最近的 5 门课程铺到面板
      const groups = res.data.data.slice().sort((a, b) => {
        const ta = (a.term || '').toString()
        const tb = (b.term || '').toString()
        return tb.localeCompare(ta)
      })
      const flatList = []
      for (const group of groups) {
        const courseList = (group && Array.isArray(group.courses)) ? group.courses : []
        for (const c of courseList) {
          // 给课程补上渐变色（API 没返回这个字段，前端自己算）
          if (!c.coverColor) {
            c.coverColor = getCoverGradient(c.id)
          }
          flatList.push(c)
        }
      }
      recentCourses.value = flatList.slice(0, 5)

      // 顶部统计：在修课程数 + 累计学分
      enrolledCourseCount.value = flatList.length
      let creditSum = 0
      for (const c of flatList) {
        const num = Number(c.credit)
        if (!Number.isNaN(num)) creditSum += num
      }
      totalCredit.value = creditSum
    }
  } catch (error) {
    console.error('加载我的课程异常:', error)
  }
}

// 累计得分：直接走后端 /homework/student/stats/score 接口，一次 SUM 拿总分
const loadTotalScore = async () => {
  try {
    const res = await getStudentTotalScoredScore()
    if (res && res.data && res.data.code === 200) {
      const scoreValue = Number(res.data.data)
      totalScore.value = Number.isFinite(scoreValue) ? scoreValue : 0
    }
  } catch (error) {
    console.error('加载累计得分异常:', error)
    totalScore.value = 0
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

// 截止时间样式：已截止的显示红色
const deadlineClass = (deadline) => {
  if (!deadline) return ''
  return new Date() > new Date(deadline) ? 'deadline-over' : ''
}
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

.hw-deadline.deadline-over {
  color: #f56c6c;
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
