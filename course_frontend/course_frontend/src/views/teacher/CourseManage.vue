<template>
  <div class="course-manage-container">
    <h2 class="page-title">我的课程</h2>

    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <el-button type="primary" @click="handleCreateCourse">
        创建课程
      </el-button>
    </div>

    <!-- 按学期分组展示的课程列表 -->
    <div v-if="groupedCourses.length > 0" class="course-groups">
      <div v-for="group in groupedCourses" :key="group.term" class="term-group">
        <h3 class="term-title">{{ group.term }}</h3>
        <el-row :gutter="20">
          <el-col
            v-for="course in group.courses"
            :key="course.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            :xl="4"
          >
            <div class="course-card" @click="goToCourseDetail(course.id)">
              <div
                class="card-cover"
                :style="{ backgroundImage: `url(${course.cover || defaultCover})` }"
              />
              <div class="card-body">
                <h4 class="course-name">{{ course.courseName }}</h4>
                <p class="course-class">{{ course.className }}</p>
                <div class="card-footer">
                  <span class="join-code">加课码：{{ course.joinCode }}</span>
                  <span class="member-count">{{ course.memberCount }} 人</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="您还没有创建任何课程，点击上方按钮创建第一门课程吧！" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getTeacherCourseList } from '@/api/course'

const router = useRouter()

// 默认课程封面
const defaultCover = 'https://via.placeholder.com/300x150?text=Course'

// 课程数据列表
const courseList = ref([])

// 按学期分组后的课程
const groupedCourses = computed(() => {
  const map = new Map()
  courseList.value.forEach(course => {
    const term = course.term || '未分组'
    if (!map.has(term)) {
      map.set(term, { term, courses: [] })
    }
    map.get(term).courses.push(course)
  })
  return Array.from(map.values())
})

// 页面加载时获取课程列表
onMounted(() => {
  loadCourseList()
})

// 加载教师课程列表
const loadCourseList = async () => {
  try {
    const res = await getTeacherCourseList()
    if (res.data.code === 200) {
      courseList.value = []
      res.data.data.forEach(group => {
        group.courses.forEach(c => {
          c.term = group.term
          courseList.value.push(c)
        })
      })
    } else {
      ElMessage.error(res.data.msg || '加载课程列表失败')
    }
  } catch (error) {
    console.error('加载课程列表异常:', error)
  }
}

// 创建新课程
const handleCreateCourse = () => {
  router.push('/teacher/create-course')
}

// 跳转到课程详情页
const goToCourseDetail = (courseId) => {
  router.push(`/teacher/course/${courseId}`)
}
</script>

<style scoped>
.course-manage-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.top-bar {
  margin-bottom: 20px;
}

.term-group {
  margin-bottom: 30px;
}

.term-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
  padding-left: 8px;
  border-left: 4px solid #409eff;
}

.course-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 20px;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-cover {
  height: 120px;
  background-size: cover;
  background-position: center;
  background-color: #f0f0f0;
}

.card-body {
  padding: 12px;
}

.course-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-class {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
}
</style>
