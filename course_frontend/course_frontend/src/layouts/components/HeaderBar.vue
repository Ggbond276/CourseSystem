<template>
  <header class="header-bar">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="header-left">
      <el-icon class="collapse-btn" :size="20" @click="$emit('toggle-sidebar')">
        <Fold v-if="!collapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: homePath }">{{ homeLabel }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title && route.path !== homePath">
          {{ route.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧：用户头像下拉 -->
    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-trigger">
          <el-avatar :size="34" :src="userStore.avatar || defaultAvatar">
            {{ (userStore.name || 'U').charAt(0) }}
          </el-avatar>
          <span class="user-name">{{ userStore.name || '未登录用户' }}</span>
          <el-icon :size="14"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold,
  Expand,
  ArrowDown,
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle-sidebar'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 兜底头像（用纯色块代替，避免 404）
const defaultAvatar = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="34" height="34"><rect width="34" height="34" fill="%23e5e7eb"/></svg>'

// 当前用户所属主页路径（用于面包屑首页跳转）
const homePath = computed(() => {
  return userStore.role === 'student' ? '/student/dashboard' : '/teacher/dashboard'
})

const homeLabel = computed(() => '工作台')

// 下拉菜单命令处理
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      })
      // 清空用户状态 + 跳登录页
      userStore.clearUser()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (e) {
      // 用户点了取消，不做任何事
    }
  } else if (command === 'profile') {
    ElMessage.info('个人中心页面待开发')
  }
}
</script>

<style scoped>
.header-bar {
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.collapse-btn {
  cursor: pointer;
  color: #6b7280;
  padding: 6px;
  border-radius: 4px;
  transition: background-color 0.15s ease;
}
.collapse-btn:hover {
  background-color: #f3f4f6;
  color: #2563eb;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background-color 0.15s ease;
}
.user-trigger:hover {
  background-color: #f3f4f6;
}

.user-name {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}
</style>
