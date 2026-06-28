<template>
  <div class="course-detail">
    <!-- ===== 顶部 Header（课程信息 + 返回） ===== -->
    <div class="detail-header">
      <el-button text @click="handleBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span style="margin-left: 4px;">返回课程列表</span>
      </el-button>

      <div class="header-content" :style="{ background: courseInfo.coverGradient }">
        <div class="header-overlay">
          <div class="course-badge">{{ courseInfo.courseNum }}</div>
          <h1 class="course-title">{{ courseInfo.courseName }}</h1>
          <div class="course-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ courseInfo.teacherName }}
            </span>
            <span class="meta-item">
              <el-icon><School /></el-icon>
              {{ courseInfo.className }}
            </span>
            <span class="meta-item">
              <el-icon><Coin /></el-icon>
              {{ courseInfo.credit }} 学分 · {{ courseInfo.period }} 学时
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ courseInfo.term }}
            </span>
          </div>
          <div class="header-stats">
            <div class="stat-block">
              <div class="stat-value">{{ stats.homeworkCount }}</div>
              <div class="stat-label">作业总数</div>
            </div>
            <div class="stat-block">
              <div class="stat-value">{{ stats.studentCount }}</div>
              <div class="stat-label">学生人数</div>
            </div>
            <div class="stat-block">
              <div class="stat-value">{{ stats.submitRate }}%</div>
              <div class="stat-label">提交率</div>
            </div>
            <div v-if="userStore.role === 'teacher'" class="stat-block">
              <div class="stat-value">{{ courseInfo.joinCode }}</div>
              <div class="stat-label">加课码</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 主体 Tabs ===== -->
    <div class="detail-body">
      <el-tabs v-model="activeTab" class="detail-tabs">
        <!-- Tab 1：作业大厅 -->
        <el-tab-pane label="作业大厅" name="homework">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              作业大厅
            </span>
          </template>

          <!-- 操作栏 -->
          <div class="tab-toolbar">
            <el-input
              v-model="homeworkSearch"
              placeholder="搜索作业标题"
              clearable
              style="width: 280px;"
              :prefix-icon="Search"
            />
            <el-radio-group v-model="homeworkStatusFilter" size="default">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="ongoing">进行中</el-radio-button>
              <el-radio-button label="ended">已截止</el-radio-button>
            </el-radio-group>
            <div style="flex: 1;" />
            <!-- 教师端：发布新作业按钮（弹窗化） -->
            <el-button
              v-if="userStore.role === 'teacher'"
              type="primary"
              @click="openCreateHomeworkDialog"
            >
              <el-icon><Plus /></el-icon>
              <span>发布新作业</span>
            </el-button>
          </div>

          <!-- 作业卡片列表 -->
          <div v-if="displayHomeworks.length > 0" class="homework-grid">
            <el-card
              v-for="hw in displayHomeworks"
              :key="hw.id"
              class="homework-card"
              shadow="hover"
            >
              <div class="hw-header">
                <h4 class="hw-title">{{ hw.title }}</h4>
                <el-tag :type="getHomeworkTagType(hw)" size="small">
                  {{ getHomeworkTagText(hw) }}
                </el-tag>
              </div>
              <p class="hw-desc">{{ hw.description }}</p>
              <div class="hw-meta">
                <span class="meta-chip">
                  <el-icon><Clock /></el-icon>
                  截止 {{ hw.deadline }}
                </span>
                <span class="meta-chip">
                  <el-icon><Star /></el-icon>
                  满分 {{ hw.totalScore }}
                </span>
                <span class="meta-chip">
                  <el-icon><Collection /></el-icon>
                  {{ hw.activityTag }}
                </span>
              </div>

              <!-- 教师视角：展示批改进度 -->
              <div v-if="userStore.role === 'teacher'" class="hw-stats">
                <div class="progress-stat">
                  <span class="stat-name">已批</span>
                  <span class="stat-num success">{{ hw.gradedCount }}</span>
                </div>
                <div class="progress-stat">
                  <span class="stat-name">待批</span>
                  <span class="stat-num warning">{{ hw.ungradedCount }}</span>
                </div>
                <div class="progress-stat">
                  <span class="stat-name">未交</span>
                  <span class="stat-num danger">{{ hw.unsubmittedCount }}</span>
                </div>
                <div style="flex: 1;" />
                <el-button text type="primary" @click="handleGrade(hw)">去批阅</el-button>
              </div>

              <!-- 学生视角：展示提交状态 + 按钮 -->
              <div v-else class="hw-stats">
                <div class="progress-stat">
                  <el-tag :type="getStudentStatusTagType(hw.status)" size="small">
                    {{ getStudentStatusText(hw.status) }}
                  </el-tag>
                </div>
                <div v-if="hw.score !== null" class="progress-stat">
                  <span class="score-label">得分</span>
                  <span class="score-value">{{ hw.score }}</span>
                </div>
                <div style="flex: 1;" />
                <el-button
                  type="primary"
                  size="small"
                  :disabled="hw.isOver && hw.forbidLate === 1"
                  @click="openSubmitDialog(hw)"
                >
                  {{ getSubmitButtonText(hw) }}
                </el-button>
              </div>
            </el-card>
          </div>

          <el-empty v-else description="该课程暂无作业">
            <el-button
              v-if="userStore.role === 'teacher'"
              type="primary"
              @click="openCreateHomeworkDialog"
            >发布第一份作业</el-button>
          </el-empty>
        </el-tab-pane>

        <!-- Tab 2：课程信息 -->
        <el-tab-pane name="info">
          <template #label>
            <span class="tab-label">
              <el-icon><InfoFilled /></el-icon>
              课程信息
            </span>
          </template>

          <div class="info-content">
            <!-- 基本信息卡片 -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="info-card-header">
                  <el-icon><Notebook /></el-icon>
                  <span>基本信息</span>
                </div>
              </template>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="课程编号">{{ courseInfo.courseNum }}</el-descriptions-item>
                <el-descriptions-item label="课程名称">{{ courseInfo.courseName }}</el-descriptions-item>
                <el-descriptions-item label="教学班级">{{ courseInfo.className }}</el-descriptions-item>
                <el-descriptions-item label="学年学期">{{ courseInfo.term }}</el-descriptions-item>
                <el-descriptions-item label="学时">{{ courseInfo.period }}</el-descriptions-item>
                <el-descriptions-item label="学分">{{ courseInfo.credit }}</el-descriptions-item>
                <el-descriptions-item label="任课教师">{{ courseInfo.teacherName }}</el-descriptions-item>
                <el-descriptions-item v-if="userStore.role === 'teacher'" label="加课码">
                  <el-tag type="warning">{{ courseInfo.joinCode }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- 课程大纲 -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="info-card-header">
                  <el-icon><List /></el-icon>
                  <span>课程大纲</span>
                </div>
              </template>
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in courseSyllabus"
                  :key="index"
                  :timestamp="item.week"
                  placement="top"
                  :type="item.type"
                >
                  <h4 class="syllabus-title">{{ item.title }}</h4>
                  <p class="syllabus-content">{{ item.content }}</p>
                </el-timeline-item>
              </el-timeline>
            </el-card>

            <!-- 课程介绍 -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="info-card-header">
                  <el-icon><Reading /></el-icon>
                  <span>课程介绍</span>
                </div>
              </template>
              <p class="course-intro">{{ courseIntro }}</p>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ===== 发布作业弹窗（教师） ===== -->
    <el-dialog
      v-model="createHomeworkVisible"
      title="发布新作业"
      width="640px"
      :close-on-click-modal="false"
      align-center
    >
      <el-form
        ref="homeworkFormRef"
        :model="homeworkForm"
        :rules="homeworkRules"
        label-width="100px"
      >
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="homeworkForm.title" placeholder="例如：第一章 课后练习" />
        </el-form-item>
        <el-form-item label="作业要求" prop="description">
          <el-input
            v-model="homeworkForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述作业要求、提交方式等"
          />
        </el-form-item>
        <el-form-item label="作业模式" prop="type">
          <el-radio-group v-model="homeworkForm.type">
            <el-radio :label="1">个人作业</el-radio>
            <el-radio :label="2">小组作业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="应用环节" prop="activityTag">
          <el-select v-model="homeworkForm.activityTag" placeholder="请选择" style="width: 100%;">
            <el-option label="课前" value="课前" />
            <el-option label="课中" value="课中" />
            <el-option label="课后" value="课后" />
            <el-option label="期末" value="期末" />
          </el-select>
        </el-form-item>
        <el-form-item label="满分值" prop="totalScore">
          <el-input-number v-model="homeworkForm.totalScore" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="超时提交" prop="forbidLate">
          <el-radio-group v-model="homeworkForm.forbidLate">
            <el-radio :label="0">允许超时提交</el-radio>
            <el-radio :label="1">超时禁止提交</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="homeworkForm.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createHomeworkVisible = false">取 消</el-button>
        <el-button type="primary" :loading="creatingHomework" @click="handleCreateHomeworkSubmit">
          立即发布
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 提交作业弹窗（学生） ===== -->
    <el-dialog
      v-model="submitHomeworkVisible"
      :title="`提交作业：${currentSubmitHomework?.title || ''}`"
      width="560px"
      align-center
    >
      <el-form
        ref="submitFormRef"
        :model="submitForm"
        :rules="submitRules"
        label-width="100px"
      >
        <el-form-item label="作业标题">
          <span class="readonly-field">{{ currentSubmitHomework?.title }}</span>
        </el-form-item>
        <el-form-item label="作业要求">
          <span class="readonly-field">{{ currentSubmitHomework?.description }}</span>
        </el-form-item>
        <el-form-item label="截止时间">
          <span class="readonly-field">{{ currentSubmitHomework?.deadline }}</span>
        </el-form-item>
        <el-form-item label="作答内容" prop="content">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入你的作答内容"
          />
        </el-form-item>
        <el-form-item label="上传附件">
          <el-upload
            v-model:file-list="submitForm.attachments"
            action="#"
            :auto-upload="false"
            multiple
          >
            <el-button>
              <el-icon><Upload /></el-icon>
              <span style="margin-left: 4px;">选择文件</span>
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传多个文件，单个文件不超过 20MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitHomeworkVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitHomework">
          提 交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, User, School, Coin, Calendar,
  Document, Plus, Search, Clock, Star, Collection,
  InfoFilled, Notebook, List, Reading, Upload
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { createHomework, submitHomework, getTeacherHomeworkList, getStudentHomeworkList } from '@/api/homework'
import { getCourseDetail } from '@/api/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前路由参数里的 courseId
const courseId = route.params.id

// ===== 顶部课程信息（mock） =====
const courseInfo = ref({
  id: courseId,
  courseNum: 'CS-101',
  courseName: '计算机科学导论',
  className: '2024级计算机1班',
  term: '2024-2025 第一学期',
  period: 48,
  credit: 3.0,
  teacherName: '张教授',
  joinCode: '9B3299',
  coverGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
})

// ===== 顶部统计数字（mock） =====
const stats = ref({
  homeworkCount: 6,
  studentCount: 42,
  submitRate: 88
})

// ===== 课程大纲（mock） =====
const courseSyllabus = [
  { week: '第 1 周', title: '计算机系统概述', content: '了解计算机发展史、分类与应用领域', type: 'primary' },
  { week: '第 2 周', title: '数制与编码', content: '二进制、八进制、十六进制，原码/反码/补码', type: 'success' },
  { week: '第 3 周', title: '数据结构基础', content: '数组、链表、栈、队列的概念与基本操作', type: 'warning' },
  { week: '第 4 周', title: '算法分析初步', content: '时间复杂度、空间复杂度的度量方法', type: 'danger' },
  { week: '第 5-8 周', title: '程序设计与实践', content: '通过 Python 实现常见数据结构与算法', type: 'primary' },
  { week: '第 9 周', title: '期中测验', content: '闭卷笔试 + 上机测试', type: 'info' },
  { week: '第 10-16 周', title: '专题与项目实践', content: '小组项目开发，覆盖真实业务场景', type: 'success' }
]

// ===== 课程介绍（mock） =====
const courseIntro = '本课程是计算机专业的入门课程，旨在帮助学生建立对计算机科学的整体认知。通过理论讲解与编程实践相结合的方式，使学生掌握计算机系统的基本组成、数据结构与算法的基础知识，培养计算思维与问题解决能力。课程强调动手实践，每周配有实验环节，鼓励学生通过实际编程加深理解。'

// ===== Tab 状态 =====
const activeTab = ref('homework')

// ===== 作业搜索与筛选 =====
const homeworkSearch = ref('')
const homeworkStatusFilter = ref('')

// ===== 作业列表 mock 数据（教师视角 + 学生视角合一） =====
const homeworkList = ref([
  {
    id: 'HW-001',
    title: '第一章 课后练习',
    description: '请完成课本第 1 章课后习题 1-10，拍照上传',
    type: 1,
    activityTag: '课后',
    totalScore: 100,
    forbidLate: 1,
    deadline: '2026-07-15 23:59:59',
    isOver: false,
    // 教师视角
    gradedCount: 18,
    ungradedCount: 4,
    unsubmittedCount: 20,
    // 学生视角
    status: 1,
    score: null
  },
  {
    id: 'HW-002',
    title: '实验报告 1：链表实现',
    description: '用 Python 实现单链表、循环链表，并完成测试用例',
    type: 1,
    activityTag: '课中',
    totalScore: 50,
    forbidLate: 0,
    deadline: '2026-07-08 23:59:59',
    isOver: false,
    gradedCount: 10,
    ungradedCount: 12,
    unsubmittedCount: 20,
    status: 0,
    score: null
  },
  {
    id: 'HW-003',
    title: '小组项目：图书管理系统',
    description: '5 人一组，完成一个完整的图书管理系统（CLI 版）',
    type: 2,
    activityTag: '课后',
    totalScore: 200,
    forbidLate: 1,
    deadline: '2026-06-30 23:59:59',
    isOver: true,
    gradedCount: 42,
    ungradedCount: 0,
    unsubmittedCount: 0,
    status: 2,
    score: 185
  },
  {
    id: 'HW-004',
    title: '第二章 数制转换练习',
    description: '完成进制转换相关题目，提交答题照片',
    type: 1,
    activityTag: '课后',
    totalScore: 100,
    forbidLate: 0,
    deadline: '2026-06-25 23:59:59',
    isOver: true,
    gradedCount: 42,
    ungradedCount: 0,
    unsubmittedCount: 0,
    status: 2,
    score: 92
  },
  {
    id: 'HW-005',
    title: '课前预习：算法复杂度',
    description: '阅读教材第 4 章，整理思维导图并提交',
    type: 1,
    activityTag: '课前',
    totalScore: 30,
    forbidLate: 1,
    deadline: '2026-07-20 23:59:59',
    isOver: false,
    gradedCount: 5,
    ungradedCount: 2,
    unsubmittedCount: 35,
    status: 0,
    score: null
  },
  {
    id: 'HW-006',
    title: '期末综合项目',
    description: '独立完成一个小型信息管理系统（任选题材）',
    type: 1,
    activityTag: '期末',
    totalScore: 300,
    forbidLate: 1,
    deadline: '2026-08-10 23:59:59',
    isOver: false,
    gradedCount: 0,
    ungradedCount: 0,
    unsubmittedCount: 42,
    status: 0,
    score: null
  }
])

// ===== 计算属性：过滤后的作业列表 =====
const displayHomeworks = computed(() => {
  let list = homeworkList.value
  // 状态筛选
  if (homeworkStatusFilter.value === 'ongoing') {
    list = list.filter((hw) => !hw.isOver)
  } else if (homeworkStatusFilter.value === 'ended') {
    list = list.filter((hw) => hw.isOver)
  }
  // 关键词筛选
  const kw = homeworkSearch.value.trim()
  if (kw) {
    list = list.filter((hw) => hw.title.includes(kw))
  }
  return list
})

// ===== 教师视角标签 =====
const getHomeworkTagType = (hw) => {
  if (hw.isOver) return 'info'
  if (hw.ungradedCount > 0) return 'warning'
  return 'success'
}
const getHomeworkTagText = (hw) => {
  if (hw.isOver) return '已截止'
  if (hw.ungradedCount > 0) return `${hw.ungradedCount} 份待批`
  return '进行中'
}

// ===== 学生视角标签 =====
const getStudentStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}
const getStudentStatusText = (status) => {
  const map = { 0: '未提交', 1: '已提交', 2: '已批改', 3: '打回重做' }
  return map[status] || '未知'
}
const getSubmitButtonText = (hw) => {
  if (hw.status === 0) return '去提交'
  if (hw.status === 1) return '重新提交'
  if (hw.status === 2) return '查看详情'
  if (hw.status === 3) return '重新提交'
  if (hw.isOver && hw.forbidLate === 1) return '已截止'
  return '查看详情'
}

// ===== 返回上一页 =====
const handleBack = () => {
  const prefix = userStore.role === 'teacher' ? '/teacher' : '/student'
  router.push(`${prefix}/courses`)
}

// ===== 教师：去批阅 =====
const handleGrade = (hw) => {
  const prefix = userStore.role === 'teacher' ? '/teacher' : '/student'
  router.push(`${prefix}/homework/${hw.id}/submissions`)
}

// ===== 教师：发布作业弹窗 =====
const createHomeworkVisible = ref(false)
const creatingHomework = ref(false)
const homeworkFormRef = ref(null)
const homeworkForm = ref({
  title: '',
  description: '',
  type: 1,
  activityTag: '课后',
  totalScore: 100,
  forbidLate: 0,
  deadline: ''
})
const homeworkRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}
const openCreateHomeworkDialog = () => {
  createHomeworkVisible.value = true
  homeworkForm.value = {
    title: '',
    description: '',
    type: 1,
    activityTag: '课后',
    totalScore: 100,
    forbidLate: 0,
    deadline: ''
  }
}
const handleCreateHomeworkSubmit = async () => {
  if (!homeworkFormRef.value) return
  await homeworkFormRef.value.validate(async (valid) => {
    if (!valid) return
    creatingHomework.value = true
    try {
      const payload = { ...homeworkForm.value, courseId }
      // 调用真实接口
      const res = await createHomework(payload)
      if (res.data.code === 200) {
        ElMessage.success('作业发布成功')
        createHomeworkVisible.value = false
        // 临时把新作业 push 到列表头部（保证 UI 演示）
        homeworkList.value.unshift({
          id: 'HW-' + String(Date.now()).slice(-3),
          title: homeworkForm.value.title,
          description: homeworkForm.value.description,
          type: homeworkForm.value.type,
          activityTag: homeworkForm.value.activityTag,
          totalScore: homeworkForm.value.totalScore,
          forbidLate: homeworkForm.value.forbidLate,
          deadline: homeworkForm.value.deadline,
          isOver: false,
          gradedCount: 0,
          ungradedCount: 0,
          unsubmittedCount: stats.value.studentCount,
          status: 0,
          score: null
        })
      } else {
        ElMessage.error(res.data.msg || '发布失败')
      }
    } catch (e) {
      ElMessage.error('网络异常')
    } finally {
      creatingHomework.value = false
    }
  })
}

// ===== 学生：提交作业弹窗 =====
const submitHomeworkVisible = ref(false)
const submitting = ref(false)
const submitFormRef = ref(null)
const currentSubmitHomework = ref(null)
const submitForm = ref({
  content: '',
  attachments: []
})
const submitRules = {
  content: [{ required: true, message: '请输入作答内容', trigger: 'blur' }]
}
const openSubmitDialog = (hw) => {
  // 已批改的查看详情走跳转，不开弹窗
  if (hw.status === 2) {
    router.push(`/student/homework/${hw.id}`)
    return
  }
  currentSubmitHomework.value = hw
  submitForm.value = { content: '', attachments: [] }
  submitHomeworkVisible.value = true
}
const handleSubmitHomework = async () => {
  if (!submitFormRef.value) return
  await submitFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        homeworkId: currentSubmitHomework.value.id,
        content: submitForm.value.content,
        attachments: submitForm.value.attachments.map((f) => ({
          name: f.name,
          url: f.url || '#'
        }))
      }
      const res = await submitHomework(payload)
      if (res.data.code === 200) {
        ElMessage.success('提交成功')
        submitHomeworkVisible.value = false
        // 临时把作业状态改为「已提交」
        const target = homeworkList.value.find((h) => h.id === currentSubmitHomework.value.id)
        if (target) target.status = 1
      } else {
        ElMessage.error(res.data.msg || '提交失败')
      }
    } catch (e) {
      ElMessage.error('网络异常')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(async () => {
  // 真实接口：getCourseDetail(courseId) → 回填 courseInfo
  // 失败时维持 mock，保证页面有数据
  try {
    const res = await getCourseDetail(courseId)
    if (res?.data?.code === 200 && res.data.data) {
      courseInfo.value = { ...courseInfo.value, ...res.data.data }
    }
  } catch (e) {
    console.warn('课程详情接口调用失败，使用 mock 数据', e)
  }
  // 同时尝试拉作业列表（按角色）
  try {
    const api = userStore.role === 'teacher' ? getTeacherHomeworkList : getStudentHomeworkList
    const res = await api({ courseId })
    if (res?.data?.code === 200 && Array.isArray(res.data.data)) {
      homeworkList.value = res.data.data
    }
  } catch (e) {
    console.warn('作业列表接口调用失败，使用 mock 数据', e)
  }
})
</script>

<style scoped>
.course-detail {
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== Header ===== */
.detail-header {
  margin-bottom: 16px;
}
.back-btn {
  margin-bottom: 12px;
  color: #6b7280;
}
.back-btn:hover { color: #2563eb; }

.header-content {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}
.header-overlay {
  padding: 28px 32px;
  color: #ffffff;
  position: relative;
}
.course-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-family: ui-monospace, monospace;
  margin-bottom: 12px;
}
.course-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 14px 0;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}
.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 14px;
  margin-bottom: 24px;
  color: rgba(255, 255, 255, 0.95);
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.header-stats {
  display: flex;
  gap: 32px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.25);
}
.stat-block {
  text-align: left;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 2px;
}

/* ===== Tabs ===== */
.detail-body {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.detail-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
}

/* ===== Tab 工具栏 ===== */
.tab-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

/* ===== 作业卡片网格 ===== */
.homework-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}
.homework-card {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}
.homework-card:hover {
  border-color: #2563eb;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.1);
}
.hw-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.hw-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}
.hw-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hw-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.hw-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}
.progress-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}
.stat-name { color: #6b7280; }
.stat-num { font-weight: 700; }
.stat-num.success { color: #10b981; }
.stat-num.warning { color: #f59e0b; }
.stat-num.danger { color: #ef4444; }

.score-label {
  color: #6b7280;
  font-size: 12px;
}
.score-value {
  font-size: 18px;
  font-weight: 700;
  color: #2563eb;
}

/* ===== 课程信息 Tab ===== */
.info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-card {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}
.info-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
.syllabus-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 6px 0;
}
.syllabus-content {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}
.course-intro {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.8;
  margin: 0;
}

.readonly-field {
  font-size: 13px;
  color: #6b7280;
}
</style>