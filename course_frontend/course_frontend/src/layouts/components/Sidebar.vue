<template>
  <aside class="sidebar" :class="{ 'is-collapsed': collapsed }">
    <!-- Logo 区域 -->
    <div class="sidebar-logo">
      <el-icon :size="26" color="#ffffff"><Reading /></el-icon>
      <span v-show="!collapsed" class="logo-text">课堂管理</span>
    </div>

    <!-- 菜单区域（按角色显示不同菜单） -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :unique-opened="true"
      background-color="#1f2937"
      text-color="#d1d5db"
      active-text-color="#ffffff"
      router
      class="sidebar-menu"
    >
      <template v-for="item in menuList" :key="item.path">
        <!-- 子项含 hidden:true（点课程卡进去的二级页）的不在侧边栏显示 -->
        <el-menu-item v-if="!item.meta.hidden" :index="resolvePath(item.path)">
          <el-icon><component :is="iconMap[item.meta.icon] || Folder" /></el-icon>
          <template #title>{{ item.meta.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  Reading,
  Odometer,
  Plus,
  Folder
} from '@element-plus/icons-vue'

// 接收父组件传来的折叠状态
defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const userStore = useUserStore()

// Element Plus 的 icon 名称 -> 实际组件映射
const iconMap = {
  Odometer,
  Reading,
  Plus
}

/**
 * 根据当前用户角色选出对应的菜单（路由的 children）
 * 教师 -> /teacher 的 children
 * 学生 -> /student 的 children
 */
const menuList = computed(() => {
  // 通过 route.matched 找到当前所属的父路由（/teacher 或 /student）
  const parentRoute = route.matched.find((r) => r.path === '/teacher' || r.path === '/student')
  if (!parentRoute) return []

  // 兜底：如果拿不到当前父路由，但能根据 role 推断
  if (!parentRoute.children || parentRoute.children.length === 0) {
    if (userStore.role === 'student') {
      // 直接构造学生菜单（兜底场景：当前路由不在 /student 下）
      return [
        { path: '/student/dashboard', meta: { title: '工作台', icon: 'Odometer' } },
        { path: '/student/my-course', meta: { title: '我的课程', icon: 'Reading' } }
      ]
    }
    return [
      { path: '/teacher/dashboard', meta: { title: '工作台', icon: 'Odometer' } },
      { path: '/teacher/course-manage', meta: { title: '课程管理', icon: 'Reading' } },
      { path: '/teacher/create-course', meta: { title: '创建课程', icon: 'Plus' } },
      { path: '/teacher/homework-list', meta: { title: '作业管理', icon: 'Document' } }
    ]
  }
  return parentRoute.children
})

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 把子路由 path（如 "dashboard"）拼成完整路径（"/teacher/dashboard"）
const resolvePath = (childPath) => {
  if (childPath.startsWith('/')) return childPath
  const parent = route.matched.find((r) => r.path === '/teacher' || r.path === '/student')
  return parent ? `/${parent.path.split('/')[1]}/${childPath}` : `/${childPath}`
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

.sidebar.is-collapsed {
  width: 64px;
}

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

.logo-text {
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
}

/* 覆盖 Element Plus 菜单主题色，让激活态更明显 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #2563eb !important;
  color: #ffffff !important;
}
.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #374151 !important;
}
.sidebar-menu :deep(.el-menu) {
  border-right: none !important;
}
</style>
