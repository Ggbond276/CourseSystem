<template>
  <div class="my-course-container">
    <h2 class="page-title">我的课程</h2>

    <!-- 空状态引导页 -->
    <div v-if="courseList.length === 0" class="empty-join">
      <el-empty description="你还没有加入任何课程">
        <el-button type="primary" @click="showJoinDialog = true">
          使用加课码加入课程
        </el-button>
      </el-empty>
    </div>

    <!-- 按学期分组展示的课程列表 -->
    <div v-else class="course-groups">
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
            <div class="course-card" @click="goToCourse(course.id)">
              <div
                class="card-cover"
                :style="{ backgroundImage: `url(${course.cover || defaultCover})` }"
              />
              <div class="card-body">
                <h4 class="course-name">{{ course.courseName }}</h4>
                <p class="course-class">{{ course.className }}</p>
                <p class="teacher-name">{{ course.teacherName }}</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 加课码对话框 -->
    <el-dialog
      v-model="showJoinDialog"
      title="使用加课码加入课程"
      width="400px"
    >
      <el-form ref="joinFormRef" :model="joinForm" :rules="joinRules">
        <el-form-item label="加课码" prop="joinCode">
          <el-input
            v-model="joinForm.joinCode"
            placeholder="请输入6位加课码"
            maxlength="6"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinDialog = false">取 消</el-button>
        <el-button type="primary" :loading="joining" @click="handleJoin">
          加 入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getStudentCourseList, joinCourse } from '@/api/course'

const router = useRouter()

// 默认课程封面
const defaultCover = 'https://via.placeholder.com/300x150?text=Course'

// 课程数据列表
const courseList = ref([])

// 加课对话框
const showJoinDialog = ref(false)
const joining = ref(false)
const joinFormRef = ref(null)

// 加课表单
const joinForm = ref({
  joinCode: ''
})

// 加课表单验证规则
const joinRules = {
  joinCode: [
    { required: true, message: '请输入加课码', trigger: 'blur' },
    { len: 6, message: '加课码必须为6位', trigger: 'blur' }
  ]
}

// 按学期分组
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

onMounted(() => {
  loadCourseList()
})

// 加载学生课程列表
const loadCourseList = async () => {
  try {
    const res = await getStudentCourseList()
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

// 加入课程
const handleJoin = async () => {
  if (!joinFormRef.value) return

  await joinFormRef.value.validate(async (valid) => {
    if (!valid) return

    joining.value = true
    try {
      const res = await joinCourse({ joinCode: joinForm.value.joinCode })
      if (res.data.code === 200) {
        ElMessage.success('成功加入课程')
        showJoinDialog.value = false
        joinForm.value.joinCode = ''
        loadCourseList()
      } else {
        ElMessage.error(res.data.msg || '加入课程失败')
      }
    } catch (error) {
      console.error('加入课程异常:', error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      joining.value = false
    }
  })
}

// 跳转到课程详情
const goToCourse = (courseId) => {
  router.push(`/student/course/${courseId}`)
}
</script>

<style scoped>
.my-course-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.empty-join {
  background: #fff;
  padding: 60px;
  border-radius: 8px;
  text-align: center;
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
  margin-bottom: 2px;
}

.teacher-name {
  font-size: 12px;
  color: #409eff;
}
</style>
