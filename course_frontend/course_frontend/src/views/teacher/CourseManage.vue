<template>
  <div class="course-manage">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">课程管理</h2>
        <p class="page-subtitle">{{ headerSubtitle }}</p>
      </div>
      <div class="header-actions">
        <!-- 学期筛选 -->
        <el-select
          v-model="selectedTerm"
          placeholder="全部学期"
          clearable
          style="width: 200px;"
        >
          <el-option label="全部学期" value="" />
          <el-option
            v-for="term in availableTerms"
            :key="term"
            :label="term"
            :value="term"
          />
        </el-select>
        <!-- 创建课程按钮 -->
        <el-button
          v-if="userStore.role === 'teacher'"
          type="primary"
          size="large"
          @click="handleCreateClick"
        >
          <el-icon><Plus /></el-icon>
          <span>创建课程</span>
        </el-button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-block">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载课程数据...</span>
    </div>

    <!-- 课程列表（带拖拽排序） -->
    <div v-else-if="displayCourses.length > 0" class="course-list">
      <div
        v-for="course in displayCourses"
        :key="course.id"
        class="course-item"
        :class="{ 'is-top': course.isTop }"
        draggable="true"
        @dragstart="handleDragStart($event, course)"
        @dragover.prevent="handleDragOver($event, course)"
        @dragend="handleDragEnd"
        @drop="handleDrop($event, course)"
      >
        <!-- 拖拽手柄 -->
        <div class="drag-handle" title="按住拖动可调整顺序">
          <el-icon><Rank /></el-icon>
        </div>

        <!-- 课程封面色块 -->
        <div class="course-cover" :style="{ background: course.coverGradient }">
          <span class="cover-initial">{{ getInitialChar(course.courseName) }}</span>
        </div>

        <!-- 课程信息 -->
        <div class="course-info">
          <div class="info-top">
            <h4 class="course-name" :title="course.courseName">{{ course.courseName }}</h4>
            <el-tag
              v-if="course.isTop"
              type="warning"
              size="small"
              effect="dark"
            >
              <el-icon><StarFilled /></el-icon>
              <span>已置顶</span>
            </el-tag>
          </div>
          <div class="info-meta">
            <span class="meta-item">
              <el-icon><Postcard /></el-icon>
              {{ course.courseNum }}
            </span>
            <span class="meta-item">
              <el-icon><School /></el-icon>
              {{ course.className }}
            </span>
            <span class="meta-item">
              <el-icon><Coin /></el-icon>
              {{ course.credit }} 学分 · {{ course.period }} 学时
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ course.term }}
            </span>
          </div>
        </div>

        <!-- 加课码（教师专属） -->
        <div v-if="userStore.role === 'teacher'" class="course-code">
          <div class="code-label">加课码</div>
          <div class="code-value">{{ course.joinCode }}</div>
        </div>

        <!-- 操作按钮区 -->
        <div class="course-actions">
          <el-button
            v-if="userStore.role === 'teacher'"
            text
            :type="course.isTop ? 'warning' : 'primary'"
            @click="handleToggleTop(course)"
          >
            <el-icon><Star v-if="!course.isTop" /><StarFilled v-else /></el-icon>
            <span>{{ course.isTop ? '取消置顶' : '置顶' }}</span>
          </el-button>
          <el-button
            type="primary"
            text
            @click="handleViewDetail(course)"
          >
            <el-icon><View /></el-icon>
            <span>查看详情</span>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="还没有任何课程">
      <el-button
        v-if="userStore.role === 'teacher'"
        type="primary"
        @click="handleCreateClick"
      >
        创建第一门课程
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Rank, Star, StarFilled, Postcard, School, Coin, Calendar,
  View, Loading
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getTeacherCourseList,
  getStudentCourseList,
  saveCourseSort,
  toggleCourseTop
} from '@/api/course'

const router = useRouter()
const userStore = useUserStore()

// ===== 学期筛选 =====
const selectedTerm = ref('')

// ===== 加载态 =====
const loading = ref(false)

// ===== 拖拽状态 =====
const dragSourceId = ref('')

// ===== 课程列表（教师 + 学生都复用） =====
const courseList = ref([])

// ===== 课程封面渐变色（8 种循环） =====
const gradientPalette = [
  'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
  'linear-gradient(135deg, #10b981 0%, #059669 100%)',
  'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
  'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
  'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
  'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
  'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
  'linear-gradient(135deg, #14b8a6 0%, #0d9488 100%)'
]

// 根据课程名取首字（中文取第一个字，英文取第一个字母）
const getInitialChar = (courseName) => {
  if (!courseName) return '?'
  const firstChar = courseName.trim().charAt(0)
  return firstChar || '?'
}

// 根据课程 ID 稳定取一个渐变色（避免每次刷新都变色）
const getCoverGradient = (course) => {
  if (course.coverGradient) return course.coverGradient
  if (!course.id) return gradientPalette[0]
  // 用 ID 字符串每个字符的 ASCII 累加取模
  let sum = 0
  const idStr = String(course.id)
  for (let i = 0; i < idStr.length; i++) {
    sum = sum + idStr.charCodeAt(i)
  }
  const index = sum % gradientPalette.length
  return gradientPalette[index]
}

// ===== Mock 数据（接口失败时使用，保证页面有内容） =====
// 重要：mock 与接口返回的 ID 都统一用字符串，避免雪花 ID 丢精度
const mockTeacherCourses = [
  {
    id: '1856321478523691000',
    courseNum: 'CS-101',
    courseName: '计算机科学导论',
    className: '2024级计算机1班',
    term: '2024-2025 第一学期',
    period: 48,
    credit: 3.0,
    isTop: 1,
    sortWeight: 999,
    joinCode: '9B3299'
  },
  {
    id: '1856321478523691001',
    courseNum: 'CS-203',
    courseName: '数据结构与算法',
    className: '2023级软件工程2班',
    term: '2024-2025 第一学期',
    period: 64,
    credit: 4.0,
    isTop: 0,
    sortWeight: 100,
    joinCode: '8A1234'
  },
  {
    id: '1856321478523691002',
    courseNum: 'CS-305',
    courseName: '操作系统原理',
    className: '2022级计算机3班',
    term: '2023-2024 第二学期',
    period: 56,
    credit: 3.5,
    isTop: 0,
    sortWeight: 50,
    joinCode: '7C5678'
  },
  {
    id: '1856321478523691003',
    courseNum: 'CS-410',
    courseName: '软件工程实践',
    className: '2021级卓越工程师班',
    term: '2023-2024 第二学期',
    period: 72,
    credit: 5.0,
    isTop: 0,
    sortWeight: 10,
    joinCode: '6D9101'
  }
]

const mockStudentCourses = [
  {
    id: '1856321478523691000',
    courseNum: 'CS-101',
    courseName: '计算机科学导论',
    className: '2024级计算机1班',
    term: '2024-2025 第一学期',
    period: 48,
    credit: 3.0,
    isTop: 1,
    teacherName: '张教授',
    sortWeight: 999
  },
  {
    id: '1856321478523691001',
    courseNum: 'CS-203',
    courseName: '数据结构与算法',
    className: '2024级软件工程2班',
    term: '2024-2025 第一学期',
    period: 64,
    credit: 4.0,
    isTop: 0,
    teacherName: '李教授',
    sortWeight: 100
  },
  {
    id: '1856321478523691004',
    courseNum: 'MATH-201',
    courseName: '高等数学',
    className: '2024级理工类合班',
    term: '2024-2025 第一学期',
    period: 80,
    credit: 5.0,
    isTop: 0,
    teacherName: '王教授',
    sortWeight: 50
  },
  {
    id: '1856321478523691005',
    courseNum: 'ENG-102',
    courseName: '大学英语',
    className: '2024级1班',
    term: '2024-2025 第一学期',
    period: 48,
    credit: 2.0,
    isTop: 0,
    teacherName: 'Emily',
    sortWeight: 10
  }
]

// ===== 计算属性：学期选项（从当前课程里抽） =====
const availableTerms = computed(() => {
  const set = new Set()
  courseList.value.forEach((c) => {
    if (c.term) set.add(c.term)
  })
  // 学期按时间倒序
  return Array.from(set).sort((a, b) => b.localeCompare(a))
})

// ===== 计算属性：学期筛选后的课程列表 =====
const displayCourses = computed(() => {
  let list = courseList.value
  if (selectedTerm.value) {
    list = list.filter((c) => c.term === selectedTerm.value)
  }
  // 置顶在最前 + 按 sortWeight 降序
  return [...list].sort((a, b) => {
    if (a.isTop === 1 && b.isTop !== 1) return -1
    if (a.isTop !== 1 && b.isTop === 1) return 1
    return (b.sortWeight || 0) - (a.sortWeight || 0)
  })
})

// ===== 顶部副标题 =====
const headerSubtitle = computed(() => {
  const total = courseList.value.length
  if (userStore.role === 'teacher') {
    return `您当前共创建了 ${total} 门课程，可拖拽卡片调整顺序`
  }
  return `您当前共加入 ${total} 门课程`
})

// ===== 加载课程列表 =====
const loadCourses = async () => {
  loading.value = true

  try {
    let res = null
    if (userStore.role === 'teacher') {
      // 教师端：必须传 teacherId（白名单注释里说明，UserContext 还没就绪）
      const teacherId = userStore.userId
      if (!teacherId) {
        ElMessage.warning('未检测到登录信息')
        courseList.value = []
        return
      }
      res = await getTeacherCourseList(teacherId)
    } else {
      // 学生端：必须传 studentId
      const studentId = userStore.userId
      if (!studentId) {
        ElMessage.warning('未检测到登录信息')
        courseList.value = []
        return
      }
      res = await getStudentCourseList(studentId)
    }

    // 后端返回的是学期分组的二维结构：[{ term, courses: [...] }]
    if (res && res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
      const flatList = []
      res.data.data.forEach((group) => {
        if (group && Array.isArray(group.courses)) {
          group.courses.forEach((c) => {
            // 关键：把 ID 统一转成字符串，避免雪花 ID 在前端比对时丢精度
            const courseItem = {
              ...c,
              id: c.id !== undefined && c.id !== null ? String(c.id) : c.id,
              term: group.term || c.term
            }
            flatList.push(courseItem)
          })
        }
      })
      // 给每条数据补上渐变色（用于卡片封面色块）
      flatList.forEach((c) => {
        c.coverGradient = getCoverGradient(c)
      })
      // 接口成功就完全采用接口数据（即使是空数组，也说明真的没有课程，不要保留 mock）
      courseList.value = flatList
    } else {
      // 接口返回格式异常：用 mock 占位
      console.warn('课程列表接口返回格式异常，使用 mock 数据')
      if (userStore.role === 'teacher') {
        courseList.value = mockTeacherCourses.map((c) => ({ ...c }))
      } else {
        courseList.value = mockStudentCourses.map((c) => ({ ...c }))
      }
    }
  } catch (e) {
    // 接口失败：用 mock 占位
    console.warn('课程列表接口调用失败，使用 mock 数据', e)
    if (userStore.role === 'teacher') {
      courseList.value = mockTeacherCourses.map((c) => ({ ...c }))
    } else {
      courseList.value = mockStudentCourses.map((c) => ({ ...c }))
    }
  } finally {
    loading.value = false
  }
}

// ===== 跳到创建课程页 =====
const handleCreateClick = () => {
  router.push('/teacher/create-course')
}

// ===== 跳到课程详情 =====
const handleViewDetail = (course) => {
  if (!course || !course.id) return
  const prefix = userStore.role === 'teacher' ? '/teacher' : '/student'
  router.push(`${prefix}/course/detail/${course.id}`)
}

// ===== 切换置顶 =====
const handleToggleTop = async (course) => {
  if (!course || !course.id) return
  const newIsTop = course.isTop === 1 ? 0 : 1
  // 教师端置顶需要 teacherId
  const teacherId = userStore.userId
  if (!teacherId) {
    ElMessage.warning('未检测到登录信息')
    return
  }
  try {
    // 关键：雪花 ID 一律转 String，避免后端 Long 反序列化失败
    const res = await toggleCourseTop({
      teacherId: String(teacherId),
      courseId: String(course.id),
      isTop: newIsTop
    })
    if (res && res.data && res.data.code === 200) {
      // 更新本地状态
      course.isTop = newIsTop
      ElMessage.success(newIsTop === 1 ? '已置顶' : '已取消置顶')
    } else {
      ElMessage.error((res && res.data && res.data.msg) || '操作失败')
    }
  } catch (e) {
    // 接口失败时本地状态先回滚友好提示
    ElMessage.error('网络异常，请稍后重试')
    console.error('切换置顶异常:', e)
  }
}

// ===== 拖拽排序：开始拖动 =====
const handleDragStart = (event, course) => {
  dragSourceId.value = course.id
  // 让拖拽时浏览器显示半透明效果
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

// ===== 拖拽经过（必须 preventDefault 才能触发 drop） =====
const handleDragOver = (event) => {
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// ===== 拖拽结束 =====
const handleDragEnd = () => {
  dragSourceId.value = ''
}

// ===== 拖拽放下：把源课程移动到目标位置 =====
const handleDrop = async (event, targetCourse) => {
  event.preventDefault()
  if (!dragSourceId.value || dragSourceId.value === targetCourse.id) {
    dragSourceId.value = ''
    return
  }

  // 在 courseList 里找到源和目标
  const sourceIndex = courseList.value.findIndex((c) => c.id === dragSourceId.value)
  const targetIndex = courseList.value.findIndex((c) => c.id === targetCourse.id)
  if (sourceIndex < 0 || targetIndex < 0) {
    dragSourceId.value = ''
    return
  }

  // 把源课程从原位置删除，再插到目标位置
  const sourceCourse = courseList.value[sourceIndex]
  courseList.value.splice(sourceIndex, 1)
  courseList.value.splice(targetIndex, 0, sourceCourse)

  // 重新计算 sortWeight（越靠前越大，步长 100）
  const newOrder = courseList.value
  newOrder.forEach((c, index) => {
    c.sortWeight = (newOrder.length - index) * 100
  })

  // 调接口保存排序（只对教师端有意义）
  dragSourceId.value = ''
  if (userStore.role !== 'teacher') return

  const teacherId = userStore.userId
  if (!teacherId) {
    ElMessage.warning('未检测到登录信息')
    return
  }

  try {
    // 关键：雪花 ID 一律转 String，避免后端 Long[] 反序列化失败
    const sortedCourseIds = newOrder.map((c) => String(c.id))
    const res = await saveCourseSort({ teacherId: String(teacherId), sortedCourseIds })
    if (res && res.data && res.data.code === 200) {
      ElMessage.success('排序已保存')
    } else {
      ElMessage.error((res && res.data && res.data.msg) || '排序保存失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，排序可能未保存')
    console.error('保存排序异常:', e)
  }
}

// ===== 页面挂载 =====
onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.course-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 4px;
}

/* ===== 顶部栏 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}
.page-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ===== 加载态 ===== */
.loading-block {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 0;
  color: #6b7280;
  font-size: 14px;
}
.is-loading {
  animation: rotating 2s linear infinite;
  font-size: 18px;
  color: #2563eb;
}
@keyframes rotating {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

/* ===== 课程列表（列表式，便于拖拽） ===== */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px 16px;
  transition: all 0.2s;
  cursor: grab;
}
.course-item:hover {
  border-color: #2563eb;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.08);
}
.course-item:active {
  cursor: grabbing;
}
.course-item.is-top {
  background: linear-gradient(90deg, #fff7ed 0%, #ffffff 100%);
  border-color: #fdba74;
}

/* ===== 拖拽手柄 ===== */
.drag-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  color: #9ca3af;
  font-size: 18px;
  flex-shrink: 0;
}

/* ===== 课程封面（色块） ===== */
.course-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cover-initial {
  color: #ffffff;
  font-size: 30px;
  font-weight: 700;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* ===== 课程信息 ===== */
.course-info {
  flex: 1;
  min-width: 0;
}
.info-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.course-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.info-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 13px;
  color: #6b7280;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.meta-item .el-icon { color: #9ca3af; }

/* ===== 加课码（教师专属） ===== */
.course-code {
  text-align: center;
  background: #fffbeb;
  border: 1px dashed #f59e0b;
  border-radius: 6px;
  padding: 6px 10px;
  flex-shrink: 0;
}
.code-label {
  font-size: 11px;
  color: #92400e;
  margin-bottom: 2px;
}
.code-value {
  font-size: 16px;
  font-weight: 700;
  color: #b45309;
  font-family: ui-monospace, monospace;
  letter-spacing: 1px;
}

/* ===== 操作按钮区 ===== */
.course-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
</style>
