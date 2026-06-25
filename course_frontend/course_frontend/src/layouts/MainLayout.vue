<template>
  <div class="main-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar :collapsed="sidebarCollapsed" />

    <!-- 右侧主区域 -->
    <div class="main-container" :class="{ 'is-collapsed': sidebarCollapsed }">
      <!-- 顶部导航栏 -->
      <HeaderBar
        :collapsed="sidebarCollapsed"
        @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
      />

      <!-- 页面主体（子路由出口） -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import HeaderBar from './components/HeaderBar.vue'

// 侧边栏是否折叠（默认展开）
const sidebarCollapsed = ref(false)
</script>

<style scoped>
.main-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background-color: #f8fafc;
  transition: margin-left 0.25s ease;
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f8fafc;
}

/* ========== 路由切换动画 ========== */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
