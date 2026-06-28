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
      <el-menu-item index="/role-prefix/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>工作台</template>
      </el-menu-item>
      <el-menu-item index="/role-prefix/courses">
        <el-icon><Reading /></el-icon>
        <template #title>我的课程</template>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Reading, Odometer } from '@element-plus/icons-vue'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 根据角色取路径前缀：教师 → /teacher，学生 → /student
const rolePrefix = computed(() => {
  return userStore.role === 'teacher' ? '/teacher' : '/student'
})

// 菜单项的真实路径（响应式计算）
const menuItems = computed(() => [
  {
    index: `${rolePrefix.value}/dashboard`,
    icon: 'Odometer',
    title: '工作台'
  },
  {
    index: `${rolePrefix.value}/courses`,
    icon: 'Reading',
    title: '我的课程'
  }
])

// 监听路径前缀变化，重定向到对应角色下的有效路径
// 防止菜单点击的是模板占位符 /role-prefix/courses 而非真实路径
const activeMenu = computed(() => {
  // 当前路径以 /teacher 或 /student 开头时，认为是高亮 /teacher/courses 或 /student/courses
  const path = route.path
  if (path.startsWith('/teacher')) return '/teacher/courses'
  if (path.startsWith('/student')) return '/student/courses'
  return path
})

// 点击菜单时，把模板字符串替换为真实路径再跳转
const handleMenuSelect = (indexPath) => {
  const realPath = indexPath.replace('role-prefix', rolePrefix.value.replace('/', ''))
  router.push(realPath)
}
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