<template>
  <aside class="sidebar" :class="{ 'is-collapsed': collapsed }">
    <!-- Logo 区域 -->
    <div class="sidebar-logo">
      <el-icon :size="26" color="#ffffff"><Reading /></el-icon>
      <span v-show="!collapsed" class="logo-text">课堂管理</span>
    </div>

    <!-- 侧边栏：仅保留两个核心入口（工作台 + 我的课程） -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      background-color="#1f2937"
      text-color="#d1d5db"
      active-text-color="#ffffff"
      router
      class="sidebar-menu"
    >
      <!-- 工作台：index 直接是真实路径（带角色前缀） -->
      <el-menu-item :index="dashboardPath">
        <el-icon><Odometer /></el-icon>
        <template #title>工作台</template>
      </el-menu-item>

      <!-- 我的课程：index 直接是真实路径（带角色前缀） -->
      <el-menu-item :index="coursesPath">
        <el-icon><Reading /></el-icon>
        <template #title>我的课程</template>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Reading, Odometer } from '@element-plus/icons-vue'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const route = useRoute()
const userStore = useUserStore()

// 根据角色取路径前缀：教师 → /teacher，学生 → /student
const rolePrefix = computed(() => {
  return userStore.role === 'teacher' ? '/teacher' : '/student'
})

// 工作台真实路径（响应式，随角色变化）
const dashboardPath = computed(() => `${rolePrefix.value}/dashboard`)

// 我的课程真实路径（响应式，随角色变化）
const coursesPath = computed(() => `${rolePrefix.value}/courses`)

// 当前激活的菜单项：
// 1. 当前在课程详情等子页面时，高亮「我的课程」
// 2. 当前在 dashboard 时，高亮「工作台」
const activeMenu = computed(() => {
  const path = route.path
  // 命中「工作台」
  if (path === dashboardPath.value) return dashboardPath.value
  // 命中「我的课程」或它的子路由（course/detail/...、homework/...）
  if (
    path === coursesPath.value ||
    path.startsWith(`${coursesPath.value}/`) ||
    path.startsWith(`${rolePrefix.value}/course/`) ||
    path.startsWith(`${rolePrefix.value}/homework/`)
  ) {
    return coursesPath.value
  }
  // 默认高亮工作台
  return dashboardPath.value
})
</script>

<style scoped>
.sidebar {
  width: 220px;
  background-color: #1f2937;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  flex-shrink: 0;
}
.sidebar.is-collapsed { width: 64px; }

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #ffffff;
  border-bottom: 1px solid #374151;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  flex-shrink: 0;
}
.logo-text { white-space: nowrap; overflow: hidden; }

.sidebar-menu { flex: 1; border-right: none !important; }

/* 激活态与 hover 样式 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #2563eb !important;
  color: #ffffff !important;
}
.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #374151 !important;
}
.sidebar-menu :deep(.el-menu) { border-right: none !important; }
</style>