<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.name || '老师' }} 👋</h2>
        <p class="welcome-subtitle">欢迎进入课堂管理系统，今天是个适合教学的好日子。</p>
      </div>
      <div class="welcome-right">
        <el-button type="primary" size="large" @click="$router.push('/teacher/create-course')">
          <el-icon><Plus /></el-icon>
          <span>新建课程</span>
        </el-button>
      </div>
    </div>

    <!-- 数据卡片骨架（占位，组员接入 API 后填真实数字） -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="24" color="#ffffff"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">--</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主内容区：快捷入口 + 最近课程 -->
    <el-row :gutter="20" class="content-row">
      <!-- 快捷入口 -->
      <el-col :xs="24" :md="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">快捷入口</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="quick-item" @click="$router.push('/teacher/course-manage')">
              <el-icon :size="22" color="#2563eb"><Reading /></el-icon>
              <span>课程管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/teacher/create-course')">
              <el-icon :size="22" color="#10b981"><Plus /></el-icon>
              <span>创建课程</span>
            </div>
            <div class="quick-item" @click="$router.push('/teacher/course-manage')">
              <el-icon :size="22" color="#f59e0b"><Document /></el-icon>
              <span>作业批阅</span>
            </div>
            <div class="quick-item" @click="$router.push('/teacher/course-manage')">
              <el-icon :size="22" color="#8b5cf6"><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近课程 -->
      <el-col :xs="24" :md="16">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">最近课程</span>
              <el-button text type="primary" @click="$router.push('/teacher/course-manage')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-empty description="暂无课程，去创建你的第一门课程吧" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 提示条：告知组员这是占位骨架 -->
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="dev-tip"
      title="提示：此页面为占位骨架，由组长搭建。组员请在此基础上接入真实 API 数据。"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Reading,
  Plus,
  Document,
  DataAnalysis,
  UserFilled,
  Notebook,
  Trophy,
  TrendCharts
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

// 顶部 4 个统计卡片（占位）
const statCards = [
  { label: '授课班级', value: '--', icon: UserFilled, color: '#2563eb' },
  { label: '在带课程', value: '--', icon: Notebook, color: '#10b981' },
  { label: '待批作业', value: '--', icon: Document, color: '#f59e0b' },
  { label: '本周活跃', value: '--', icon: TrendCharts, color: '#8b5cf6' }
]
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
  border-radius: 8px;
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
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
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
  font-weight: 600;
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
  border-radius: 8px;
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
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background-color: #f8fafc;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #1f2937;
  transition: all 0.15s ease;
}

.quick-item:hover {
  background-color: #eff6ff;
  transform: translateY(-1px);
}

/* ========== 开发提示 ========== */
.dev-tip {
  margin-top: 8px;
}
</style>
