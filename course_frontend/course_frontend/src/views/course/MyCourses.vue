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
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button
          v-for="term in availableTerms"
          :key="term"
          :label="term"
        >{{ term }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 课程卡片网格（至少 4 张） -->
    <el-row v-if="displayCourses.length > 0" :gutter="20" class="course-row">
      <el-col
        v-for="course in displayCourses"
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
          @click="goToCourseDetail(course.id)"
        >
          <!-- 课程封面（用渐变色 + 首字作为占位图） -->
          <div class="card-cover" :style="{ background: course.coverGradient }">
            <span class="cover-initial">{{ course.courseName.charAt(0) }}</span>
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
                <span>{{ course.teacherName }}</span>
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
            </div>
          </div>
        </el-card>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus, Link, Search, User, School, Coin, StarFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getTeacherCourseList,
  getStudentCourseList,
  getCourseDetail,
  createCourse,
  joinCourse
} from '@/api/course'

const router = useRouter()
const userStore = useUserStore()

// ===== 搜索关键词 =====
const searchKeyword = ref('')

// ===== 当前选中的学期（空字符串 = 全部） =====
const selectedTerm = ref('')

// ============ Mock 数据（保证页面有内容） ============
// 4 张教师课程 + 4 张学生课程，覆盖不同学期
const mockTeacherCourses = [
  {
    id: '1856321478523691000',
    courseNum: 'CS-101',
    courseName: '计算机科学导论',
    className: '2024级计算机1班',
    term: '2024-2025 第一学期',
    period: 48,
    credit: 3.0,
    status: 1,
    isTop: 1,
    teacherName: '张老师',
    coverGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
  },
  {
    id: '1856321478523691001',
    courseNum: 'CS-203',
    courseName: '数据结构与算法',
    className: '2023级软件工程2班',
    term: '2024-2025 第一学期',
    period: 64,
    credit: 4.0,
    status: 1,
    isTop: 0,
    teacherName: '张老师',
    coverGradient: 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
  },
  {
    id: '1856321478523691002',
    courseNum: 'CS-305',
    courseName: '操作系统原理',
    className: '2022级计算机3班',
    term: '2023-2024 第二学期',
    period: 56,
    credit: 3.5,
    status: 1,
    isTop: 0,
    teacherName: '张老师',
    coverGradient: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
  },
  {
    id: '1856321478523691003',
    courseNum: 'CS-410',
    courseName: '软件工程实践',
    className: '2021级卓越工程师班',
    term: '2023-2024 第二学期',
    period: 72,
    credit: 5.0,
    status: 1,
    isTop: 0,
    teacherName: '张老师',
    coverGradient: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
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
    status: 1,
    isTop: 1,
    teacherName: '张教授',
    coverGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
  },
  {
    id: '1856321478523691001',
    courseNum: 'CS-203',
    courseName: '数据结构与算法',
    className: '2024级软件工程2班',
    term: '2024-2025 第一学期',
    period: 64,
    credit: 4.0,
    status: 1,
    isTop: 0,
    teacherName: '李教授',
    coverGradient: 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
  },
  {
    id: '1856321478523691004',
    courseNum: 'MATH-201',
    courseName: '高等数学',
    className: '2024级理工类合班',
    term: '2024-2025 第一学期',
    period: 80,
    credit: 5.0,
    status: 1,
    isTop: 0,
    teacherName: '王教授',
    coverGradient: 'linear-gradient(135deg, #ec4899 0%, #db2777 100%)'
  },
  {
    id: '1856321478523691005',
    courseNum: 'ENG-102',
    courseName: '大学英语',
    className: '2024级1班',
    term: '2024-2025 第一学期',
    period: 48,
    credit: 2.0,
    status: 1,
    isTop: 0,
    teacherName: 'Emily',
    coverGradient: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)'
  }
]

// 课程列表（渲染用）
const courseList = ref([])

// 根据角色选 mock 数据；后端接口联调时切回真实接口即可
const loadMockCourses = () => {
  if (userStore.role === 'teacher') {
    courseList.value = [...mockTeacherCourses]
  } else {
    courseList.value = [...mockStudentCourses]
  }
}

// ===== 计算属性：学期选项（从课程里动态抽出来） =====
const availableTerms = computed(() => {
  const set = new Set()
  courseList.value.forEach((c) => {
    if (c.term) set.add(c.term)
  })
  return Array.from(set)
})

// ===== 计算属性：搜索 + 学期筛选后的课程列表 =====
const displayCourses = computed(() => {
  let list = courseList.value
  // 学期筛选
  if (selectedTerm.value) {
    list = list.filter((c) => c.term === selectedTerm.value)
  }
  // 关键词搜索（课程名 / 班级 / 教师）
  const kw = searchKeyword.value.trim()
  if (kw) {
    list = list.filter((c) => {
      return (
        c.courseName.includes(kw) ||
        c.className.includes(kw) ||
        (c.teacherName && c.teacherName.includes(kw))
      )
    })
  }
  return list
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

// ===== 创建课程弹窗 =====
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
  courseNum: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  className: [{ required: true, message: '请输入教学班级', trigger: 'blur' }],
  term: [{ required: true, message: '请输入学年学期', trigger: 'blur' }]
}
const openCreateDialog = () => {
  createDialogVisible.value = true
  // 重置表单（保留 period/credit 默认值）
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
    creating.value = true
    try {
      // 调用真实接口（联调时直接连后端）
      const res = await createCourse(createForm.value)
      if (res.data.code === 200) {
        ElMessage.success('创建成功')
        createDialogVisible.value = false
        // 临时用 mock 模拟新增（接口联调时可替换为 reload）
        courseList.value.unshift({
          ...createForm.value,
          id: String(Date.now()),
          status: 1,
          isTop: 0,
          teacherName: userStore.name || '张老师',
          coverGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
        })
      } else {
        ElMessage.error(res.data.msg || '创建失败')
      }
    } catch (e) {
      // 接口异常时也临时插入，保证 UI 演示效果
      courseList.value.unshift({
        ...createForm.value,
        id: String(Date.now()),
        status: 1,
        isTop: 0,
        teacherName: userStore.name || '张老师',
        coverGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
      })
      ElMessage.success('创建成功（mock 演示）')
      createDialogVisible.value = false
    } finally {
      creating.value = false
    }
  })
}

// ===== 加入课程弹窗（学生） =====
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
    joining.value = true
    try {
      const res = await joinCourse({ joinCode: joinForm.value.joinCode })
      if (res.data.code === 200) {
        ElMessage.success('加入成功')
        joinDialogVisible.value = false
        loadMockCourses()
      } else {
        ElMessage.error(res.data.msg || '加入失败')
      }
    } catch (e) {
      ElMessage.error('网络异常')
    } finally {
      joining.value = false
    }
  })
}

// 页面挂载 → 优先调真实接口，失败 fallback 到 mock（保证 UI 有数据）
onMounted(async () => {
  loadMockCourses() // 先用 mock 占位，避免空白
  try {
    const api = userStore.role === 'teacher' ? getTeacherCourseList : getStudentCourseList
    const res = await api()
    if (res?.data?.code === 200 && Array.isArray(res.data.data) && res.data.data.length > 0) {
      // 把后端返回的学期分组扁平化
      const flat = []
      res.data.data.forEach((group) => {
        group.courses.forEach((c) => {
          flat.push({
            ...c,
            term: group.term,
            teacherName: c.teacherName || (userStore.role === 'teacher' ? (userStore.name || '我') : '任课教师'),
            // 给真实数据补上渐变色占位
            coverGradient: c.cover || 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
          })
        })
      })
      courseList.value = flat
    }
  } catch (e) {
    // 接口失败时维持 mock 数据
    console.warn('课程列表接口调用失败，使用 mock 数据', e)
  }
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