<template>
  <div class="course-hall">
    <!-- 顶部标题栏 -->
    <div class="hall-header">
      <div>
        <h2 class="hall-title">我的课程</h2>
        <p class="hall-subtitle">{{ headerSubtitle }}</p>
      </div>
      <div class="hall-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程名称 / 班级 / 教师"
          clearable
          style="width: 280px;"
          :prefix-icon="Search"
        />
        <!-- 教师端：创建课程按钮 -->
        <el-button
          v-if="userStore.role === 'teacher'"
          type="primary"
          size="large"
          @click="openCreateDialog"
        >
          <el-icon><Plus /></el-icon>
          <span>创建课程</span>
        </el-button>
        <!-- 学生端：加入课程按钮 -->
        <el-button
          v-else
          type="primary"
          size="large"
          @click="openJoinDialog"
        >
          <el-icon><Link /></el-icon>
          <span>加入课程</span>
        </el-button>
      </div>
    </div>

    <!-- 学期筛选标签 -->
    <div class="term-filter">
      <span class="filter-label">学期：</span>
      <el-radio-group v-model="selectedTerm" size="default">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button
          v-for="term in availableTerms"
          :key="term"
          :value="term"
        >{{ term }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-block">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载课程...</span>
    </div>

    <!-- 课程卡片网格 -->
    <el-row v-else-if="displayCourses.length > 0" :gutter="20" class="course-row">
      <el-col
        v-for="course in displayCourses"
        :key="course.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        :xl="6"
      >
        <div
          class="course-card-wrapper"
          :class="{ 'is-dragging': dragSourceId === course.id }"
          draggable="true"
          @dragstart="handleDragStart($event, course)"
          @dragover.prevent="handleDragOver($event, course)"
          @dragend="handleDragEnd"
          @drop="handleDrop($event, course)"
        >
          <el-card
            class="course-card"
            shadow="hover"
            @click="goToCourseDetail(course.id)"
          >
            <!-- 课程封面（用渐变色 + 首字作为占位图） -->
            <div class="card-cover" :style="{ background: course.coverGradient }">
              <span class="cover-initial">{{ getInitialChar(course.courseName) }}</span>
              <div v-if="course.isTop" class="top-flag">
                <el-icon><StarFilled /></el-icon>
                <span>置顶</span>
              </div>
            </div>

            <!-- 课程信息 -->
            <div class="card-body">
              <h4 class="course-name" :title="course.courseName">{{ course.courseName }}</h4>
              <div class="course-meta">
                <div class="meta-row">
                  <el-icon><User /></el-icon>
                  <span>{{ course.teacherName || '任课教师' }}</span>
                </div>
                <div class="meta-row">
                  <el-icon><School /></el-icon>
                  <span>{{ course.className }}</span>
                </div>
                <div class="meta-row">
                  <el-icon><Coin /></el-icon>
                  <span>{{ course.credit }} 学分 · {{ course.period }} 学时</span>
                </div>
              </div>
              <div class="card-footer">
                <el-tag size="small" :type="course.status === 1 ? 'success' : 'info'">
                  {{ course.status === 1 ? '进行中' : '已结束' }}
                </el-tag>
                <span class="course-code">{{ course.courseNum }}</span>
                <!-- 教师端：置顶 / 取消置顶按钮 -->
                <el-button
                  v-if="userStore.role === 'teacher'"
                  text
                  size="small"
                  :type="course.isTop ? 'warning' : 'default'"
                  @click.stop="handleToggleTop(course)"
                >
                  <el-icon><Star v-if="!course.isTop" /><StarFilled v-else /></el-icon>
                  <span>{{ course.isTop ? '取消置顶' : '置顶' }}</span>
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-else :description="emptyDescription">
      <el-button
        v-if="userStore.role === 'teacher'"
        type="primary"
        @click="openCreateDialog"
      >创建第一门课程</el-button>
      <el-button v-else type="primary" @click="openJoinDialog">输入加课码加入</el-button>
    </el-empty>

    <!-- ===== 创建课程弹窗（教师） ===== -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建新课程"
      width="560px"
      :close-on-click-modal="false"
      align-center
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item label="课程编号" prop="courseNum">
          <el-input v-model="createForm.courseNum" placeholder="例如 CS-302" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="createForm.courseName" placeholder="例如 操作系统原理" />
        </el-form-item>
        <el-form-item label="教学班级" prop="className">
          <el-input v-model="createForm.className" placeholder="例如 2024级软件工程1班" />
        </el-form-item>
        <el-form-item label="学年学期" prop="term">
          <el-input v-model="createForm.term" placeholder="例如 2024-2025 第一学期" />
        </el-form-item>
        <el-form-item label="学时" prop="period">
          <el-input-number v-model="createForm.period" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="createForm.credit" :min="0" :max="20" :precision="1" :step="0.5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateSubmit">立即创建</el-button>
      </template>
    </el-dialog>

    <!-- ===== 加入课程弹窗（学生） ===== -->
    <el-dialog
      v-model="joinDialogVisible"
      title="使用加课码加入课程"
      width="420px"
      align-center
    >
      <el-form ref="joinFormRef" :model="joinForm" :rules="joinRules" label-width="80px">
        <el-form-item label="加课码" prop="joinCode">
          <el-input
            v-model="joinForm.joinCode"
            placeholder="请输入 6 位加课码"
            maxlength="6"
            show-word-limit
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="joining" @click="handleJoinSubmit">加 入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus, Link, Search, User, School, Coin, Star, StarFilled, Loading
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getTeacherCourseList,
  getStudentCourseList,
  createCourse,
  joinCourse,
  saveCourseSort,
  toggleCourseTop
} from '@/api/course'

const router = useRouter()
const userStore = useUserStore()

// ===== 搜索关键词 =====
const searchKeyword = ref('')

// ===== 当前选中的学期（空字符串 = 全部） =====
const selectedTerm = ref('')

// ===== 加载态 =====
const loading = ref(false)

// ===== 课程列表（渲染用） =====
const courseList = ref([])

// 【DEBUG 日志】监控 courseList 变化：打印每次课程列表的条目数和具体内容
watch(courseList, (newVal) => {
  console.log('[courseList.watch] 课程列表变化，当前条目数:', newVal.length, '具体内容:', JSON.stringify(newVal))
}, { deep: true })

// ===== 课程封面色板（8 种循环渐变色） =====
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

// 根据课程 ID 稳定取一个渐变色（避免每次刷新都变色）
const getCoverGradient = (course) => {
  if (course.coverGradient) return course.coverGradient
  if (!course.id) return gradientPalette[0]
  let sum = 0
  const idStr = String(course.id)
  for (let i = 0; i < idStr.length; i++) {
    sum = sum + idStr.charCodeAt(i)
  }
  const index = sum % gradientPalette.length
  return gradientPalette[index]
}

// 取课程名首字（中文取第一个字，英文取第一个字母）
const getInitialChar = (courseName) => {
  if (!courseName) return '?'
  const firstChar = courseName.trim().charAt(0)
  return firstChar || '?'
}

// ===== 计算属性：学期选项（从课程里动态抽出来） =====
const availableTerms = computed(() => {
  const set = new Set()
  courseList.value.forEach((c) => {
    if (c.term) set.add(c.term)
  })
  // 学期按时间倒序（字符串比较即可）
  return Array.from(set).sort((a, b) => b.localeCompare(a))
})

// ===== 计算属性：置顶课程排最前，其余按 sortWeight 降序 =====
const sortedAllCourses = computed(() => {
  return [...courseList.value].sort((a, b) => {
    if (a.isTop === 1 && b.isTop !== 1) return -1
    if (a.isTop !== 1 && b.isTop === 1) return 1
    return (b.sortWeight || 0) - (a.sortWeight || 0)
  })
})

// 【DEBUG 日志】监控 sortedAllCourses（排序后原始列表）
watch(sortedAllCourses, (newVal) => {
  console.log('[sortedAllCourses.watch] 排序后列表，条目数:', newVal.length, 'isTop/ids:', newVal.map((c) => ({ id: c.id, isTop: c.isTop })))
})

// ===== 计算属性：搜索 + 学期筛选后的课程列表 =====
const displayCourses = computed(() => {
  let list = sortedAllCourses.value
  // 学期筛选
  if (selectedTerm.value) {
    list = list.filter((c) => c.term === selectedTerm.value)
  }
  // 关键词搜索（课程名 / 班级 / 教师）
  const kw = searchKeyword.value.trim()
  if (kw) {
    list = list.filter((c) => {
      const matchName = c.courseName && c.courseName.includes(kw)
      const matchClass = c.className && c.className.includes(kw)
      const matchTeacher = c.teacherName && c.teacherName.includes(kw)
      return matchName || matchClass || matchTeacher
    })
  }
  return list
})

// 【DEBUG 日志】监控 displayCourses（最终渲染列表）
watch(displayCourses, (newVal) => {
  console.log('[displayCourses.watch] 最终渲染列表，条目数:', newVal.length,
    '学期筛选:', selectedTerm.value, '搜索关键词:', searchKeyword.value,
    '课程名列表:', newVal.map((c) => c.courseName))
})

// ===== 顶部副标题 =====
const headerSubtitle = computed(() => {
  const role = userStore.role === 'teacher' ? '老师' : '同学'
  const total = courseList.value.length
  return `欢迎回来，${userStore.name || role}！您当前共有 ${total} 门课程。`
})

// ===== 空状态文案 =====
const emptyDescription = computed(() => {
  if (selectedTerm.value || searchKeyword.value) {
    return '没有符合条件的课程'
  }
  return userStore.role === 'teacher' ? '还没有创建任何课程' : '还没有加入任何课程'
})

// ===== 跳转到课程详情 =====
const goToCourseDetail = (courseId) => {
  const prefix = userStore.role === 'teacher' ? '/teacher' : '/student'
  router.push(`${prefix}/course/detail/${courseId}`)
}

// ============ 加载课程列表（教师 / 学生共用） ============
const loadCourses = async () => {
  loading.value = true
  try {
    let res = null

    // ===== 【DEBUG 日志】打印 userId 的原始类型和值 =====
    const rawUserId = userStore.userId
    console.log('[LoadCourses] userStore.userId 原始值:', rawUserId, '类型:', typeof rawUserId)

    if (userStore.role === 'teacher') {
      const teacherId = userStore.userId
      if (!teacherId) {
        ElMessage.warning('未检测到登录信息')
        loading.value = false
        return
      }
      console.log('[LoadCourses] 教师模式，调用 getTeacherCourseList，参数 teacherId:', teacherId, '类型:', typeof teacherId)
      res = await getTeacherCourseList(teacherId)
    } else {
      const studentId = userStore.userId
      if (!studentId) {
        ElMessage.warning('未检测到登录信息')
        loading.value = false
        return
      }
      console.log('[LoadCourses] 学生模式，调用 getStudentCourseList，参数 studentId:', studentId, '类型:', typeof studentId)
      res = await getStudentCourseList(studentId)
    }

    // ===== 【DEBUG 日志】打印后端原始响应 =====
    console.log('[LoadCourses] 后端原始响应 res:', JSON.stringify(res?.data))

    const resData = res.data
    const dataArray = resData?.data

    // 根据实际后端返回结构判断（可能是 { data: [...] } 或直接是数组）
    if (resData?.code === 200 && dataArray) {
      // 判断是分组结构 [{ term, courses: [...] }] 还是扁平数组 [...]
      const flatList = []
      if (Array.isArray(dataArray) && dataArray.length > 0) {
        const firstItem = dataArray[0]
        if (firstItem && firstItem.courses && Array.isArray(firstItem.courses)) {
          // 分组结构：[{ term, courses: [...] }]
          dataArray.forEach((group) => {
            if (group && Array.isArray(group.courses)) {
              group.courses.forEach((c) => {
                flatList.push({
                  ...c,
                  term: group.term || c.term
                })
              })
            }
          })
        } else {
          // 扁平数组：[...]
          dataArray.forEach((c) => flatList.push({ ...c }))
        }
      }

      // 给每条数据补上渐变色
      flatList.forEach((c) => {
        c.coverGradient = getCoverGradient(c)
      })

      courseList.value = flatList
    } else {
      const msg = resData?.msg || '加载失败'
      ElMessage.warning(msg)
    }
  } catch (e) {
    console.error('[课程列表] 加载失败:', e)
    ElMessage.error('网络异常，请稍后刷新重试')
  } finally {
    loading.value = false
  }
}

// ============ 创建课程弹窗（教师） ============
const createDialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = ref({
  courseNum: '',
  courseName: '',
  className: '',
  term: '',
  period: 48,
  credit: 3.0
})
const createRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入教学班级', trigger: 'blur' }
  ],
  term: [
    { required: true, message: '请输入学年学期', trigger: 'blur' }
  ]
}
const openCreateDialog = () => {
  createDialogVisible.value = true
  // 重置表单（保留 period / credit 默认值）
  createForm.value = {
    courseNum: '',
    courseName: '',
    className: '',
    term: '',
    period: 48,
    credit: 3.0
  }
}
const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return

    const teacherId = userStore.userId
    if (!teacherId) {
      ElMessage.warning('未检测到登录信息')
      return
    }

    creating.value = true
    try {
      // 关键：雪花 ID 一律保持字符串形式，不做任何 Number() 转换
      const payload = {
        teacherId: userStore.userId,
        courseNum: createForm.value.courseNum || null,
        courseName: createForm.value.courseName,
        className: createForm.value.className,
        term: createForm.value.term,
        period: createForm.value.period,
        credit: createForm.value.credit,
        cover: null
      }
      const res = await createCourse(payload)
      if (res && res.data && res.data.code === 200) {
        ElMessage.success('创建成功')
        createDialogVisible.value = false
        // 重新拉取课程列表
        loadCourses()
      } else {
        ElMessage.error((res && res.data && res.data.msg) || '创建失败')
      }
    } catch (e) {
      console.error('创建课程接口异常:', e)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      creating.value = false
    }
  })
}

// ============ 加入课程弹窗（学生） ============
const joinDialogVisible = ref(false)
const joining = ref(false)
const joinFormRef = ref(null)
const joinForm = ref({ joinCode: '' })
const joinRules = {
  joinCode: [
    { required: true, message: '请输入加课码', trigger: 'blur' },
    { len: 6, message: '加课码必须为 6 位', trigger: 'blur' }
  ]
}
const openJoinDialog = () => {
  joinDialogVisible.value = true
  joinForm.value = { joinCode: '' }
}
const handleJoinSubmit = async () => {
  if (!joinFormRef.value) return
  await joinFormRef.value.validate(async (valid) => {
    if (!valid) return

    const studentId = userStore.userId
    if (!studentId) {
      ElMessage.warning('未检测到登录信息')
      return
    }

    joining.value = true
    try {
      // 关键 1：雪花 ID 一律保持字符串形式，不做任何 Number() 转换
      // 关键 2：加课码转大写，避免用户输入小写导致后端查询失败
      const payload = {
        studentId: userStore.userId,
        joinCode: joinForm.value.joinCode.trim().toUpperCase()
      }
      console.log('[加入课程] 请求 payload:', payload)
      const res = await joinCourse(payload)
      if (res && res.data && res.data.code === 200) {
        ElMessage.success('加入成功')
        joinDialogVisible.value = false

        // 后端返回 { courseId, courseName, alreadyJoined }
        // 直接构造课程卡片追加到本地列表，避免 loadCourses 时 userId 不一致导致查不到
        const data = res.data.data || {}
        const newCourse = {
          id: String(data.courseId || ''),
          courseNum: '',
          courseName: data.courseName || '新加入的课程',
          className: '',
          term: '',
          period: 0,
          credit: 0,
          status: 1,
          isTop: 0,
          sortWeight: 0,
          teacherName: '任课教师',
          coverGradient: gradientPalette[courseList.value.length % gradientPalette.length]
        }
        // 追加到列表（学期归为"未分学期"，让学期筛选重新计算即可见）
        courseList.value.push(newCourse)
      } else {
        ElMessage.error((res && res.data && res.data.msg) || '加入失败')
      }
    } catch (e) {
      console.error('加入课程接口异常:', e)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      joining.value = false
    }
  })
}

// ============ 拖拽排序（教师端） ============
// 记录被拖动的课程 ID
const dragSourceId = ref('')

// 开始拖动：记录被拖动的课程 ID
const handleDragStart = (event, course) => {
  dragSourceId.value = course.id
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

// 拖动经过（必须 preventDefault 才能触发 drop）
const handleDragOver = (event) => {
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// 拖动结束（只清理状态，不做任何业务操作）
const handleDragEnd = () => {
  dragSourceId.value = ''
}

// 放下：把源课程移动到目标位置
const handleDrop = async (event, targetCourse) => {
  event.preventDefault()
  if (!dragSourceId.value || dragSourceId.value === targetCourse.id) {
    dragSourceId.value = ''
    return
  }

  // 只允许教师端操作排序
  if (userStore.role !== 'teacher') {
    dragSourceId.value = ''
    return
  }

  const teacherId = userStore.userId
  if (!teacherId) {
    dragSourceId.value = ''
    ElMessage.warning('未检测到登录信息')
    return
  }

  // 在 courseList 里找到源和目标
  const sourceIndex = courseList.value.findIndex((c) => c.id === dragSourceId.value)
  const targetIndex = courseList.value.findIndex((c) => c.id === targetCourse.id)
  if (sourceIndex < 0 || targetIndex < 0) {
    dragSourceId.value = ''
    return
  }

  // 把源课程从原位置删除，再插到目标位置（Vue 响应式）
  const sourceCourse = courseList.value[sourceIndex]
  courseList.value.splice(sourceIndex, 1)
  courseList.value.splice(targetIndex, 0, sourceCourse)

  // 重新计算 sortWeight（越靠前越大，步长 100）
  const newOrder = courseList.value
  newOrder.forEach((c, index) => {
    c.sortWeight = (newOrder.length - index) * 100
  })

  dragSourceId.value = ''

  // 调用后端接口持久化排序
  try {
    // 关键：雪花 ID 一律保持字符串形式，不做任何 Number() 转换
    const sortedCourseIds = newOrder.map((c) => String(c.id))
    const res = await saveCourseSort({ teacherId: String(teacherId), sortedCourseIds })
    if (res && res.data && res.data.code === 200) {
      ElMessage.success('排序已保存')
    } else {
      ElMessage.error((res && res.data && res.data.msg) || '排序保存失败')
    }
  } catch (e) {
    console.error('保存排序异常:', e)
    ElMessage.error('网络异常，排序可能未保存')
  }
}

// ============ 置顶 / 取消置顶（教师端） ============
const handleToggleTop = async (course) => {
  if (!course || !course.id) return
  if (userStore.role !== 'teacher') return

  const teacherId = userStore.userId
  if (!teacherId) {
    ElMessage.warning('未检测到登录信息')
    return
  }

  const newIsTop = course.isTop === 1 ? 0 : 1
  try {
    // 关键：雪花 ID 一律保持字符串形式，不做任何 Number() 转换
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
    console.error('切换置顶异常:', e)
    ElMessage.error('网络异常，请稍后重试')
  }
}

// 页面挂载：加载真实课程列表
onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.course-hall {
  max-width: 1400px;
  margin: 0 auto;
  padding: 4px;
}

/* ===== 顶部栏 ===== */
.hall-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}
.hall-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}
.hall-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}
.hall-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ===== 学期筛选 ===== */
.term-filter {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
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

/* ===== 卡片网格 ===== */
.course-row {
  margin-bottom: 24px;
}

.course-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}
.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  border-color: #2563eb;
}
.course-card-wrapper {
  cursor: grab;
  user-select: none;
}
.course-card-wrapper:active {
  cursor: grabbing;
}
.course-card-wrapper.is-dragging {
  opacity: 0.5;
}

.card-cover {
  height: 110px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-initial {
  color: #ffffff;
  font-size: 42px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.top-flag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  color: #f59e0b;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
}

.card-body { padding: 14px; }
.course-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.course-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}
.meta-row .el-icon { font-size: 13px; color: #9ca3af; }

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
}
.course-code {
  font-size: 12px;
  color: #9ca3af;
  font-family: ui-monospace, monospace;
}
</style>