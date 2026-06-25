<template>
  <div class="homework-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">作业管理</h2>
      <el-button type="primary" @click="$router.push('/teacher/homework/create')">
        <el-icon><Plus /></el-icon>
        发布作业
      </el-button>
    </div>

    <!-- 按课程分组展示作业 -->
    <div v-if="loading" class="loading-area">
      <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="courseHomeworkList.length > 0" class="course-section-list">
      <div
        v-for="course in courseHomeworkList"
        :key="course.id"
        class="course-section"
      >
        <!-- 课程卡片头部 -->
        <div class="course-card" @click="toggleCourse(course.id)">
          <div class="course-cover" :style="{ backgroundColor: course.coverColor || '#2563eb' }">
            <span class="cover-initial">{{ course.courseName?.charAt(0) || '课' }}</span>
          </div>
          <div class="course-info">
            <div class="course-name">{{ course.courseName }}</div>
            <div class="course-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ course.className }}
              </span>
              <span class="meta-item">
                <el-icon><Document /></el-icon>
                {{ course.homeworkCount }} 个作业
              </span>
              <span class="meta-item">
                <el-icon><UserFilled /></el-icon>
                {{ course.studentCount }} 名学生
              </span>
            </div>
          </div>
          <div class="course-arrow">
            <el-icon :size="20"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 作业列表（展开显示） -->
        <div v-show="expandedCourses.includes(course.id)" class="homework-list">
          <div v-if="course.homeworks && course.homeworks.length > 0">
            <div
              v-for="hw in course.homeworks"
              :key="hw.id"
              class="homework-item"
              @click="$router.push(`/teacher/homework/${hw.id}/submissions`)"
            >
              <div class="hw-main">
                <div class="hw-top">
                  <span class="hw-title">{{ hw.title }}</span>
                  <el-tag :type="hw.isOver ? 'info' : 'success'" size="small">
                    {{ hw.isOver ? '已截止' : '进行中' }}
                  </el-tag>
                </div>
                <div class="hw-meta">
                  <span class="meta-tag">{{ hw.activityTag }}</span>
                  <span class="meta-deadline">
                    <el-icon><Clock /></el-icon>
                    {{ hw.deadline }}
                  </span>
                  <span class="meta-score">满分 {{ hw.totalScore }}</span>
                </div>
              </div>
              <div class="hw-stats">
                <div class="stat-item stat-done">
                  <span class="stat-num">{{ hw.gradedCount }}</span>
                  <span class="stat-label">已批</span>
                </div>
                <div class="stat-item stat-pending">
                  <span class="stat-num">{{ hw.ungradedCount }}</span>
                  <span class="stat-label">待批</span>
                </div>
                <div class="stat-item stat-not">
                  <span class="stat-num">{{ hw.unsubmittedCount }}</span>
                  <span class="stat-label">未交</span>
                </div>
              </div>
              <div class="hw-action">
                <el-button type="primary" size="small" link>
                  批阅
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="该课程暂无作业" :image-size="50" />
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无作业，点击上方按钮发布作业吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Loading, User, Document, UserFilled, ArrowRight, Clock } from '@element-plus/icons-vue'
import { getTeacherHomeworkList } from '@/api/homework'

const loading = ref(false)
const courseHomeworkList = ref([])

// 记录哪些课程已展开
const expandedCourses = ref([])

// 切换课程展开/收起
const toggleCourse = (courseId) => {
  const index = expandedCourses.value.indexOf(courseId)
  if (index >= 0) {
    expandedCourses.value.splice(index, 1)
  } else {
    expandedCourses.value.push(courseId)
  }
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    // TODO(student-dev): 改为调用获取教师所有课程下作业的接口
    // 目前占位：演示用空数据
    courseHomeworkList.value = []
  } catch (error) {
    console.error('加载异常:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.homework-list-page {
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

.loading-area {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
  padding: 40px;
  justify-content: center;
}

/* 课程区块 */
.course-section {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  background: #ffffff;
}

/* 课程卡片头部 */
.course-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  cursor: pointer;
  transition: background 0.15s;
}

.course-card:hover {
  background: #f8fafc;
}

.course-cover {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cover-initial {
  color: #ffffff;
  font-size: 22px;
  font-weight: 700;
}

.course-info {
  flex: 1;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.course-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.course-arrow {
  color: #d1d5db;
  transition: transform 0.2s;
}

/* 作业列表 */
.homework-list {
  border-top: 1px solid #f0f0f0;
  padding: 12px 20px 20px;
}

.homework-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  border: 1px solid transparent;
}

.homework-item:hover {
  background: #f8fafc;
  border-color: #e5e7eb;
}

.hw-main {
  flex: 1;
}

.hw-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.hw-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.hw-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.meta-tag {
  background: #eff6ff;
  color: #2563eb;
  padding: 1px 8px;
  border-radius: 4px;
}

.meta-deadline {
  display: flex;
  align-items: center;
  gap: 3px;
}

.meta-score {
  color: #6b7280;
}

/* 统计数据 */
.hw-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 40px;
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.stat-done .stat-num { color: #10b981; }
.stat-pending .stat-num { color: #f59e0b; }
.stat-not .stat-num { color: #ef4444; }

.hw-action {
  flex-shrink: 0;
}
</style>
