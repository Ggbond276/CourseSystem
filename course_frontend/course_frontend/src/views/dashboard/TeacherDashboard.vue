<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.name || '老师' }}</h2>
        <p class="welcome-subtitle">今天是个适合教学的好日子。</p>
      </div>
      <div class="welcome-right">
        <el-button
          type="primary"
          size="large"
          @click="$router.push('/teacher/courses')"
        >
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

    <!-- 主内容：快捷入口 + 最近作业 -->
    <el-row :gutter="16" class="content-row">
      <!-- 左侧：快捷入口 -->
      <el-col :xs="24" :md="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">快捷入口</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="quick-item" @click="$router.push('/teacher/courses')">
              <el-icon :size="22" color="#2563eb"><Reading /></el-icon>
              <div>
                <div class="quick-label">课程管理</div>
                <div class="quick-desc">查看与创建我的课程</div>
              </div>
            </div>
            <div class="quick-item" @click="$router.push('/teacher/courses')">
              <el-icon :size="22" color="#10b981"><Plus /></el-icon>
              <div>
                <div class="quick-label">创建课程</div>
                <div class="quick-desc">在我的课程页一键创建</div>
              </div>
            </div>
            <div class="quick-item" @click="$router.push('/teacher/courses')">
              <el-icon :size="22" color="#f59e0b"><Document /></el-icon>
              <div>
                <div class="quick-label">作业管理</div>
                <div class="quick-desc">在课程详情中发布与批改</div>
              </div>
            </div>
            <div class="quick-item" @click="$router.push('/teacher/courses')">
              <el-icon :size="22" color="#8b5cf6"><DataAnalysis /></el-icon>
              <div>
                <div class="quick-label">数据统计</div>
                <div class="quick-desc">班级学情分析</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：本周作业概览 -->
      <el-col :xs="24" :md="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">本周作业</span>
              <el-button text type="primary" @click="$router.push('/teacher/courses')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-if="recentHomeworks.length > 0" class="homework-mini-list">
            <div
              v-for="hw in recentHomeworks"
              :key="hw.id"
              class="homework-mini-item"
              @click="$router.push(`/teacher/homework/${hw.id}/submissions`)"
            >
              <div class="hw-left">
                <div class="hw-title">{{ hw.title }}</div>
                <div class="hw-course">{{ hw.courseName }}</div>
              </div>
              <div class="hw-right">
                <el-tag :type="hw.isOver ? 'info' : 'success'" size="small">
                  {{ hw.isOver ? '已截止' : '进行中' }}
                </el-tag>
                <div class="hw-stat">{{ hw.ungradedCount }} 待批</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="本周暂无作业" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 右侧：待办事项 -->
      <el-col :xs="24" :md="6">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">待批作业</span>
            </div>
          </template>
          <div class="todo-list">
            <div
              v-for="item in todoList"
              :key="item.id"
              class="todo-item"
              @click="$router.push(`/teacher/homework/${item.homeworkId}/submissions`)"
            >
              <div class="todo-course">{{ item.courseName }}</div>
              <div class="todo-title">{{ item.homeworkTitle }}</div>
              <div class="todo-count">{{ item.pendingCount }} 份待批</div>
            </div>
            <el-empty v-if="todoList.length === 0" description="暂无待批" :image-size="50" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Reading, Plus, Document, DataAnalysis,
  UserFilled, Notebook, TrendCharts, Trophy
} from '@element-plus/icons-vue'
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

// 顶部统计卡片
const statCards = ref([
  { label: '授课班级', value: '--', icon: UserFilled, color: '#2563eb' },
  { label: '在带课程', value: '--', icon: Notebook, color: '#10b981' },
  { label: '待批作业', value: '--', icon: Document, color: '#f59e0b' },
  { label: '本周活跃', value: '--', icon: TrendCharts, color: '#8b5cf6' }
])

// 最近作业（占位）
const recentHomeworks = ref([])

// 待批作业列表（占位）
const todoList = ref([])
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* ========== 欢迎横幅 ========== */
.welcome-card {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
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

/* ========== 快捷入口 ========== */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background-color: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.quick-item:hover {
  background-color: #eff6ff;
  transform: translateX(2px);
}

.quick-label {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.quick-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

/* ========== 作业迷你列表 ========== */
.homework-mini-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.homework-mini-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.homework-mini-item:hover {
  background: #eff6ff;
}

.hw-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.hw-course {
  font-size: 12px;
  color: #9ca3af;
}

.hw-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.hw-stat {
  font-size: 12px;
  color: #f59e0b;
  font-weight: 600;
}

/* ========== 待办列表 ========== */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item {
  padding: 14px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.todo-item:hover {
  background: #ffedd5;
}

.todo-course {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.todo-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.todo-count {
  font-size: 12px;
  color: #f59e0b;
  font-weight: 600;
}
</style>
