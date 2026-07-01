<template>
  <div class="my-course">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">我的课程</h2>
        <p class="page-subtitle">{{ headerSubtitle }}</p>
      </div>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程名称 / 班级 / 教师"
          clearable
          style="width: 280px;"
          :prefix-icon="Search"
        />
        <el-button
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
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button
          v-for="term in availableTerms"
          :key="term"
          :label="term"
        >{{ term }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载态 -->
    <div v-if="loading" class="loading-block">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载课程...</span>
    </div>

    <!-- 课程卡片网格（按学期分组） -->
    <template v-else>
      <template v-for="group in displayGroups" :key="group.term">
        <div class="term-section">
          <div class="term-title">
            <el-icon><Calendar /></el-icon>
            <span>{{ group.term || '未分类学期' }}</span>
            <span class="term-count">{{ group.courses.length }} 门课程</span>
          </div>

          <el-row v-if="group.courses.length > 0" :gutter="20">
            <el-col
              v-for="course in group.courses"
              :key="course.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              :xl="6"
            >
              <el-card
                class="course-card"
                shadow="hover"
                @click="handleViewDetail(course)"
              >
                <!-- 课程封面色块 -->
                <div class="card-cover" :style="{ background: course.coverGradient }">
                  <span class="cover-initial">{{ getInitialChar(course.courseName) }}</span>
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
                      <span>{{ course.className || '—' }}</span>
                    </div>
                    <div class="meta-row">
                      <el-icon><Coin /></el-icon>
                      <span>{{ course.credit || 0 }} 学分 · {{ course.period || 0 }} 学时</span>
                    </div>
                  </div>
                  <div class="card-footer">
                    <el-tag size="small" :type="course.status === 1 ? 'success' : 'info'">
                      {{ course.status === 1 ? '进行中' : '已结束' }}
                    </el-tag>
                    <span class="course-code">{{ course.courseNum || '—' }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty
        v-if="displayGroups.length === 0 || (displayGroups.length === 1 && displayGroups[0].courses.length === 0)"
        :description="emptyDescription"
      >
        <el-button type="primary" @click="openJoinDialog">输入加课码加入</el-button>
      </el-empty>
    </template>

    <!-- ===== 加入课程弹窗 ===== -->
    <el-dialog
      v-model="joinDialogVisible"
      title="使用加课码加入课程"
      width="440px"
      :close-on-click-modal="false"
      align-center
    >
      <el-form
        ref="joinFormRef"
        :model="joinForm"
        :rules="joinRules"
        label-width="80px"
      >
        <el-form-item label="加课码" prop="joinCode">
          <el-input
            v-model="joinForm.joinCode"
            placeholder="请输入 6 位加课码"
            maxlength="6"
            minlength="6"
            show-word-limit
            clearable
            style="text-transform: uppercase;"
            @keyup.enter="handleJoinSubmit"
          />
        </el-form-item>
      </el-form>
      <div class="join-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>加课码由任课教师提供，区分大小写</span>
      </div>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          :loading="joining"
          @click="handleJoinSubmit"
        >
          <el-icon><Check /></el-icon>
          <span>加 入</span>
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Link, Search, User, School, Coin, Calendar,
  InfoFilled, Check, Loading
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getStudentCourseList, joinCourse } from '@/api/course'

const router = useRouter()
const userStore = useUserStore()

// ===== 搜索关键词 =====
const searchKeyword = ref('')

// ===== 当前选中的学期 =====
const selectedTerm = ref('')

// ===== 加载态 =====
const loading = ref(false)

// ===== 分组后的课程数据（按学期） =====
const groupedCourseList = ref([])

// ===== 课程封面色板 =====
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

// 取课程名首字
const getInitialChar = (courseName) => {
  if (!courseName) return '?'
  const firstChar = courseName.trim().charAt(0)
  return firstChar || '?'
}

// 根据课程 ID 稳定取一个渐变色
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

// ===== Mock 数据（接口失败时使用） =====
const mockGroupedCourses = [
  {
    term: '2024-2025 第一学期',
    courses: [
      {
        id: '1856321478523691000',
        courseNum: 'CS-101',
        courseName: '计算机科学导论',
        className: '2024级计算机1班',
        period: 48,
        credit: 3.0,
        status: 1,
        teacherName: '张教授'
      },
      {
        id: '1856321478523691001',
        courseNum: 'CS-203',
        courseName: '数据结构与算法',
        className: '2024级软件工程2班',
        period: 64,
        credit: 4.0,
        status: 1,
        teacherName: '李教授'
      },
      {
        id: '1856321478523691004',
        courseNum: 'MATH-201',
        courseName: '高等数学',
        className: '2024级理工类合班',
        period: 80,
        credit: 5.0,
        status: 1,
        teacherName: '王教授'
      },
      {
        id: '1856321478523691005',
        courseNum: 'ENG-102',
        courseName: '大学英语',
        className: '2024级1班',
        period: 48,
        credit: 2.0,
        status: 1,
        teacherName: 'Emily'
      }
    ]
  },
  {
    term: '2023-2024 第二学期',
    courses: [
      {
        id: '1856321478523691006',
        courseNum: 'PHY-110',
        courseName: '大学物理',
        className: '2023级理工类合班',
        period: 64,
        credit: 4.0,
        status: 0,
        teacherName: '陈教授'
      }
    ]
  }
]

// ===== 计算属性：所有学期选项 =====
const availableTerms = computed(() => {
  const set = new Set()
  groupedCourseList.value.forEach((g) => {
    if (g.term) set.add(g.term)
  })
  return Array.from(set).sort((a, b) => b.localeCompare(a))
})

// ===== 计算属性：搜索 + 学期筛选后的课程分组 =====
const displayGroups = computed(() => {
  const kw = searchKeyword.value.trim()
  // 先学期筛选
  let groups = groupedCourseList.value
  if (selectedTerm.value) {
    groups = groups.filter((g) => g.term === selectedTerm.value)
  }
  // 再关键词搜索
  if (kw) {
    groups = groups.map((g) => {
      const filtered = g.courses.filter((c) => {
        const matchName = c.courseName && c.courseName.includes(kw)
        const matchClass = c.className && c.className.includes(kw)
        const matchTeacher = c.teacherName && c.teacherName.includes(kw)
        return matchName || matchClass || matchTeacher
      })
      return { term: g.term, courses: filtered }
    }).filter((g) => g.courses.length > 0)
  }
  return groups
})

// ===== 顶部副标题 =====
const headerSubtitle = computed(() => {
  let total = 0
  groupedCourseList.value.forEach((g) => {
    total = total + g.courses.length
  })
  return `欢迎回来，${userStore.name || '同学'}！您当前共加入 ${total} 门课程`
})

// ===== 空状态文案 =====
const emptyDescription = computed(() => {
  if (selectedTerm.value || searchKeyword.value) {
    return '没有符合条件的课程'
  }
  return '还没有加入任何课程'
})

// ===== 加载学生课程列表 =====
const loadCourses = async () => {
  loading.value = true

  try {
    const studentId = userStore.userId
    if (!studentId) {
      ElMessage.warning('未检测到登录信息')
      groupedCourseList.value = []
      return
    }
    const res = await getStudentCourseList(studentId)
    if (res && res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
      // 后端返回的是 [{ term, courses: [...] }] 结构
      const list = res.data.data
      // 把每条数据里的 id 转字符串，避免雪花 ID 丢精度
      list.forEach((g) => {
        if (g && Array.isArray(g.courses)) {
          g.courses.forEach((c) => {
            c.id = c.id !== undefined && c.id !== null ? String(c.id) : c.id
            c.coverGradient = getCoverGradient(c)
          })
        }
      })
      // 接口成功就完全采用接口数据（即使为空，也说明学生真的没加入任何课）
      groupedCourseList.value = list
    } else {
      // 接口返回格式异常：用 mock 占位
      console.warn('学生课程列表接口返回格式异常，使用 mock 数据')
      groupedCourseList.value = JSON.parse(JSON.stringify(mockGroupedCourses))
      groupedCourseList.value.forEach((g) => {
        g.courses.forEach((c) => {
          c.coverGradient = getCoverGradient(c)
        })
      })
    }
  } catch (e) {
    // 接口失败：用 mock 占位
    console.warn('学生课程列表接口调用失败，使用 mock 数据', e)
    groupedCourseList.value = JSON.parse(JSON.stringify(mockGroupedCourses))
    groupedCourseList.value.forEach((g) => {
      g.courses.forEach((c) => {
        c.coverGradient = getCoverGradient(c)
      })
    })
  } finally {
    loading.value = false
  }
}

// ===== 跳到课程详情 =====
const handleViewDetail = (course) => {
  if (!course || !course.id) return
  router.push(`/student/course/detail/${course.id}`)
}

// ===== 加入课程弹窗 =====
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

  // 关键：element-plus 的 validate 自身返回 Promise，校验失败会 reject
  // 不要用 callback 形式，否则 await 不会等待校验结果
  let isValid = false
  try {
    await joinFormRef.value.validate()
    isValid = true
  } catch (e) {
    // 校验未通过，直接返回（Element Plus 内部已经显示了错误提示）
    return
  }
  if (!isValid) return

  const studentId = userStore.userId
  if (!studentId) {
    ElMessage.warning('未检测到登录信息')
    return
  }

  joining.value = true
  try {
    // 关键 1：studentId 转 String，避免雪花 ID 丢精度
    // 关键 2：joinCode 转大写并 trim，避免前端展示为大写但实际传小写导致后端查不到
    const payload = {
      studentId: String(studentId),
      joinCode: joinForm.value.joinCode.trim().toUpperCase()
    }
    const res = await joinCourse(payload)
    if (res && res.data && res.data.code === 200) {
      ElMessage.success('加入成功')
      joinDialogVisible.value = false
      // 重新拉取列表
      loadCourses()
    } else {
      ElMessage.error((res && res.data && res.data.msg) || '加入失败')
    }
  } catch (e) {
    console.error('加入课程接口异常:', e)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    joining.value = false
  }
}

// ===== 页面挂载 =====
onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.my-course {
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

/* ===== 按学期分组的区块 ===== */
.term-section {
  margin-bottom: 28px;
}
.term-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e7eb;
}
.term-title .el-icon {
  color: #2563eb;
  font-size: 18px;
}
.term-count {
  margin-left: auto;
  font-size: 13px;
  font-weight: 400;
  color: #6b7280;
}

/* ===== 课程卡片 ===== */
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

/* ===== 加入弹窗内的提示 ===== */
.join-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
  margin-top: -8px;
  padding-left: 4px;
}
.join-tip .el-icon {
  color: #2563eb;
  font-size: 14px;
}
</style>
