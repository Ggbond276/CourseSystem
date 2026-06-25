<template>
  <div class="student-workbench">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">我的课程</h2>
      <div class="header-right">
        <el-input
          v-model="joinCode"
          placeholder="输入加课码加入课程"
          style="width: 200px; margin-right: 10px"
          @keyup.enter="handleJoinCourse"
        >
          <template #prefix>
            <el-icon><Link /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleJoinCourse">加入课程</el-button>
      </div>
    </div>

    <!-- 课程卡片网格 -->
    <div v-if="loading" class="loading-area">
      <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="courseList.length > 0" class="course-grid">
      <div
        v-for="course in courseList"
        :key="course.id"
        class="course-card"
        @click="$router.push(`/student/course/${course.id}`)"
      >
        <!-- 封面区 -->
        <div class="card-cover" :style="{ backgroundColor: course.coverColor || '#2563eb' }">
          <div class="cover-initial">{{ course.courseName?.charAt(0) || '课' }}</div>
          <div class="cover-overlay">
            <el-button type="primary" size="small" plain>
              进入课程
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 信息区 -->
        <div class="card-body">
          <div class="course-name">{{ course.courseName }}</div>
          <div class="course-teacher">
            <el-icon><User /></el-icon>
            {{ course.teacherName }}
          </div>
          <div class="course-meta">
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              {{ course.homeworkCount || 0 }} 作业
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ course.term }}
            </span>
          </div>
        </div>
      </div>

      <!-- 添加课程卡片 -->
      <div class="course-card add-card" @click="showJoinDialog = true">
        <div class="add-icon">
          <el-icon :size="36"><Plus /></el-icon>
        </div>
        <div class="add-text">加入新课程</div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="还没有加入课程，请输入加课码加入吧！">
      <el-button type="primary" @click="showJoinDialog = true">输入加课码</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus, Loading, User, Document, Clock,
  ArrowRight, Link
} from '@element-plus/icons-vue'

const loading = ref(false)
const courseList = ref([])
const joinCode = ref('')
const showJoinDialog = ref(false)

const handleJoinCourse = () => {
  if (!joinCode.value.trim()) {
    ElMessage.warning('请输入加课码')
    return
  }
  // TODO(student-dev): 调用加入课程接口
  ElMessage.info('加入课程功能待后端实现')
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    // TODO(student-dev): 调用 /course/student/list 接口
    courseList.value = []
  } catch (error) {
    console.error('加载异常:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.student-workbench {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.loading-area {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
  padding: 40px;
  justify-content: center;
}

/* 课程卡片网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.course-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
}

.course-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 封面区 */
.card-cover {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.cover-initial {
  color: #ffffff;
  font-size: 40px;
  font-weight: 700;
  z-index: 1;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.course-card:hover .cover-overlay {
  opacity: 1;
}

/* 信息区 */
.card-body {
  padding: 16px;
}

.course-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  line-height: 1.4;
}

.course-teacher {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 10px;
}

.course-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

/* 添加卡片 */
.add-card {
  border: 2px dashed #d1d5db;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  gap: 8px;
}

.add-card:hover {
  border-color: #2563eb;
  background: #eff6ff;
  box-shadow: none;
  transform: none;
}

.add-icon {
  color: #9ca3af;
  transition: color 0.2s;
}

.add-card:hover .add-icon {
  color: #2563eb;
}

.add-text {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

.add-card:hover .add-text {
  color: #2563eb;
}
</style>
