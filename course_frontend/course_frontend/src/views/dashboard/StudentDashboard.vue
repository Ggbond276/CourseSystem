<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.name || '同学' }} 👋</h2>
        <p class="welcome-subtitle">继续保持学习的热情，每一份努力都会被看见。</p>
      </div>
      <div class="welcome-right">
        <el-button type="primary" size="large" @click="$router.push('/student/my-course')">
          <el-icon><Reading /></el-icon>
          <span>进入我的课程</span>
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

    <!-- 主内容区：最近课程 + 待办作业 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :md="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">最近课程</span>
              <el-button text type="primary" @click="$router.push('/student/my-course')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-empty description="还没有加入课程，去加课码加入吧" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">待办作业</span>
              <el-button text type="primary" @click="$router.push('/student/my-course')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-empty description="暂无待办作业，好好休息一下" />
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
  Notebook,
  Document,
  Trophy,
  Calendar
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
  { label: '在修课程', value: '--', icon: Notebook, color: '#2563eb' },
  { label: '待完成作业', value: '--', icon: Document, color: '#f59e0b' },
  { label: '已获学分', value: '--', icon: Trophy, color: '#10b981' },
  { label: '本周学习', value: '--', icon: Calendar, color: '#8b5cf6' }
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

/* ========== 开发提示 ========== */
.dev-tip {
  margin-top: 8px;
}
</style>
