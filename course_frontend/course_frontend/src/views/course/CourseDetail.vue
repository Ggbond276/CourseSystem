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
            <span v-if="courseInfo.departmentName" class="meta-item">
              <el-icon><OfficeBuilding /></el-icon>
              {{ courseInfo.departmentName }}
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
              <div class="stat-value">{{ stats.submitRate }}</div>
              <div class="stat-label">提交率</div>
            </div>
            <div v-if="userStore.role === 'teacher'" class="stat-block" style="cursor: pointer;" @click="copyJoinCode">
              <div class="stat-value" :title="'点击复制'">{{ courseInfo.joinCode }}</div>
              <div class="stat-label">加课码（点复制）</div>
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
              :key="hw.homeworkId || hw.id"
              class="homework-card"
              shadow="hover"
            >
              <div class="hw-header">
                <h4 class="hw-title">
                  {{ hw.title }}
                  <!-- 双重验证：标题后立即跟真实 ID + 标题里的 index，确保 HMR 生效 -->
                  <span style="color:#e11d48; font-size:13px; margin-left:8px; font-family:monospace;">
                    [#{{ homeworkList.indexOf(hw) + 1 }} | {{ hw.homeworkId || hw.id || 'NO_ID' }}]
                  </span>
                </h4>
                <!-- 教师视角：显示批改进度；学生视角：显示个人提交状态 -->
                <el-tag
                  v-if="userStore.role === 'teacher'"
                  :type="getHomeworkTagType(hw)"
                  size="small"
                >
                  {{ getHomeworkTagText(hw) }}
                </el-tag>
                <el-tag
                  v-else
                  :type="getStudentStatusTagType(hw.status)"
                  size="small"
                >
                  {{ getStudentStatusText(hw.status) }}
                </el-tag>
              </div>
              <!-- 真实数据标识：每个卡片显示自己的数据库ID，方便学生核对是否跟后端一致 -->
              <div class="hw-db-id">ID: {{ hw.homeworkId || hw.id }}</div>
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
                  <div style="flex: 1;" />
                  <el-button
                    v-if="userStore.role === 'teacher'"
                    type="primary"
                    text
                    :icon="Plus"
                    @click="openEditSyllabusDialog"
                  >编辑大纲</el-button>
                </div>
              </template>
              <el-timeline v-if="courseSyllabus.length > 0">
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
              <el-empty v-else :description="userStore.role === 'teacher' ? '暂无大纲，点击右上角【编辑大纲】添加' : '教师暂未填写大纲'" :image-size="80" />
            </el-card>

            <!-- 课程介绍 -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="info-card-header">
                  <el-icon><Reading /></el-icon>
                  <span>课程介绍</span>
                  <div style="flex: 1;" />
                  <el-button
                    v-if="userStore.role === 'teacher'"
                    type="primary"
                    text
                    :icon="Plus"
                    @click="openEditIntroDialog"
                  >编辑介绍</el-button>
                </div>
              </template>
              <p v-if="courseIntro" class="course-intro">{{ courseIntro }}</p>
              <el-empty v-else :description="userStore.role === 'teacher' ? '暂无介绍，点击右上角【编辑介绍】添加' : '教师暂未填写介绍'" :image-size="80" />
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

    <!-- ===== 编辑课程大纲 对话框（教师） ===== -->
    <el-dialog
      v-model="editSyllabusVisible"
      title="编辑课程大纲"
      width="760px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 12px; color: #6b7280; font-size: 13px;">
        按周次填写课程大纲，每行包含周次、标题、内容、类型。保存后立即生效。
      </div>
      <el-form label-width="100px">
        <div
          v-for="(row, index) in editSyllabusRows"
          :key="index"
          class="syllabus-edit-row"
        >
          <el-form-item :label="`第 ${index + 1} 行`">
            <div class="syllabus-edit-fields">
              <el-input
                v-model="row.week"
                placeholder="周次，例如：第 1 周"
                style="width: 140px;"
                clearable
              />
              <el-select v-model="row.type" style="width: 140px;">
                <!-- 5 个类型：中文标签 + 颜色预览色块，下拉里只显示中文，让用户看见颜色（hover 选项时预览色块） -->
                <el-option label="重点" value="primary">
                  <div class="syllabus-type-option">
                    <span class="syllabus-type-dot syllabus-type-dot--primary"></span>
                    <span>重点</span>
                  </div>
                </el-option>
                <el-option label="掌握" value="success">
                  <div class="syllabus-type-option">
                    <span class="syllabus-type-dot syllabus-type-dot--success"></span>
                    <span>掌握</span>
                  </div>
                </el-option>
                <el-option label="挑战" value="warning">
                  <div class="syllabus-type-option">
                    <span class="syllabus-type-dot syllabus-type-dot--warning"></span>
                    <span>挑战</span>
                  </div>
                </el-option>
                <el-option label="测验" value="danger">
                  <div class="syllabus-type-option">
                    <span class="syllabus-type-dot syllabus-type-dot--danger"></span>
                    <span>测验</span>
                  </div>
                </el-option>
                <el-option label="信息" value="info">
                  <div class="syllabus-type-option">
                    <span class="syllabus-type-dot syllabus-type-dot--info"></span>
                    <span>信息</span>
                  </div>
                </el-option>
              </el-select>
              <el-input
                v-model="row.title"
                placeholder="标题"
                style="flex: 1;"
                clearable
              />
              <el-input
                v-model="row.content"
                placeholder="内容描述"
                style="flex: 2;"
                clearable
              />
              <el-button
                type="danger"
                text
                :icon="Plus"
                style="transform: rotate(45deg);"
                @click="removeSyllabusRow(index)"
              />
            </div>
          </el-form-item>
        </div>
        <el-button type="primary" plain :icon="Plus" @click="addSyllabusRow">
          新增一行
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="editSyllabusVisible = false">取 消</el-button>
        <el-button type="primary" :loading="savingInfo" @click="saveSyllabus">
          保 存
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 编辑课程介绍 对话框（教师） ===== -->
    <el-dialog
      v-model="editIntroVisible"
      title="编辑课程介绍"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="editIntroDraft"
        type="textarea"
        :rows="10"
        placeholder="请输入课程介绍，例如：本课程的目标、先修要求、教学方式等"
      />
      <template #footer>
        <el-button @click="editIntroVisible = false">取 消</el-button>
        <el-button type="primary" :loading="savingInfo" @click="saveIntro">
          保 存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, User, School, Coin, Calendar,
  Document, Plus, Search, Clock, Star, Collection,
  InfoFilled, Notebook, List, Reading, Upload
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { createHomework, submitHomework, getTeacherHomeworkList, getStudentHomeworkList } from '@/api/homework'
import { getTeacherCourseDetail, getStudentCourseDetail, updateCourseInfo } from '@/api/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前路由参数里的 courseId
const courseId = route.params.id

// ===== 顶部课程信息：初值先用空字符串挂载，后端返回后再覆盖 =====
const courseInfo = ref({
  id: courseId,
  courseNum: '',
  courseName: '',
  className: '',
  term: '',
  termDisplayName: '',
  departmentName: '',
  period: 0,
  credit: 0,
  teacherName: '',
  joinCode: '',
  syllabus: '',
  intro: '',
  coverGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
})

// ===== 顶部统计数字：所有数字由后端或前端计算得到，不再硬编码 =====
// homeworkCount 用作业列表的长度实时算（避免维护冗余字段）
// studentCount 用后端返回的 memberCount（教师+学生+助教总数，每 10s 轮询刷新）
// submitRate 后端暂未提供统计接口 → 占位为"-"，避免给用户错误预期
const stats = ref({
  homeworkCount: 0,
  studentCount: 0,
  submitRate: '-'
})

// ===== 课程大纲（真实数据）=====
// 后端把 syllabus 字段存为 JSON 字符串；解析失败时回退为空数组
const courseSyllabus = ref([])

// ===== 课程介绍（真实数据）=====
const courseIntro = ref('')

// ===== 编辑课程大纲 / 介绍 对话框状态（教师端）=====
const editSyllabusVisible = ref(false)
const editIntroVisible = ref(false)
const savingInfo = ref(false)
// 大纲编辑：用本地数组操作，最后 JSON.stringify 提交
const editSyllabusRows = ref([])
// 介绍编辑：直接绑字符串
const editIntroDraft = ref('')

// ===== Tab 状态 =====
const activeTab = ref('homework')

// ===== 作业搜索与筛选 =====
const homeworkSearch = ref('')
const homeworkStatusFilter = ref('')

// ===== 作业列表：初值空数组，由后端填充 =====
// 字段说明：homeworkId / title / description / type / activityTag / totalScore /
//          forbidLate / deadline / isOver / gradedCount / ungradedCount / unsubmittedCount
const homeworkList = ref([])

// ===== 10s 轮询计时器句柄 =====
let pollTimerId = null

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
  // 后端返回的作业 ID 字段是 homeworkId（字符串），而非 id
  const homeworkId = hw.homeworkId || hw.id
  const prefix = userStore.role === 'teacher' ? '/teacher' : '/student'
  router.push(`${prefix}/homework/${homeworkId}/submissions`)
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
      // 后端要求 courseId 为字符串雪花 ID，必须 toString() 转换，否则 trim() 会报错
      const payload = { ...homeworkForm.value, courseId: String(courseId) }
      // 调用真实接口
      const res = await createHomework(payload)
      if (res.data.code === 200) {
        ElMessage.success('作业发布成功')
        createHomeworkVisible.value = false
        // 临时把新作业 push 到列表头部（保证 UI 演示）
        // 注意：后端返回的 homeworkId 是字符串，mock 数据也统一用 homeworkId 字段
        const newHomeworkId = res.data.data?.homeworkId || 'HW-' + String(Date.now()).slice(-3)
        homeworkList.value.unshift({
          homeworkId: newHomeworkId,
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
    const hwId = hw.homeworkId || hw.id
    router.push(`/student/homework/${hwId}`)
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
      // 后端返回的作业 ID 字段是 homeworkId，统一取 homeworkId 兜底 id
      const hwId = currentSubmitHomework.value.homeworkId || currentSubmitHomework.value.id
      const payload = {
        homeworkId: hwId,
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
        const target = homeworkList.value.find(
          (h) => (h.homeworkId || h.id) === (currentSubmitHomework.value.homeworkId || currentSubmitHomework.value.id)
        )
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
  // 1. 进入页面：拉一次课程详情 + 作业列表
  await Promise.all([loadCourseDetail(), loadHomeworkList()])
  // 2. 启动 10s 轮询，实时刷新学生人数（memberCount）
  //    仅教师端轮询；学生端不轮询（学生加入课程后也只会变自己的视图，本页无变化）
  if (userStore.role === 'teacher') {
    pollTimerId = setInterval(() => {
      loadCourseDetail()
    }, 10000)
  }
})

// 组件销毁时清理轮询定时器，避免内存泄漏
onBeforeUnmount(() => {
  if (pollTimerId !== null) {
    clearInterval(pollTimerId)
    pollTimerId = null
  }
})

/**
 * 加载课程详情（教师端走 /course/teacher/detail/{id}，学生端后端暂未实现 → 走 mock 兜底）
 * 任何字段缺失都保留空字符串而非 undefined，避免模板渲染 [object Object]
 */
const loadCourseDetail = async () => {
  try {
    if (userStore.role === 'teacher') {
      // 教师端：走教师专用接口
      const teacherId = userStore.userId
      const res = await getTeacherCourseDetail(courseId, teacherId)
      if (res && res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        courseInfo.value = {
          ...courseInfo.value,
          courseNum: data.courseNum || courseInfo.value.courseNum,
          courseName: data.courseName || courseInfo.value.courseName,
          className: data.className || courseInfo.value.className,
          term: data.term || courseInfo.value.term,
          termDisplayName: data.termDisplayName || courseInfo.value.termDisplayName,
          departmentName: data.departmentName || courseInfo.value.departmentName,
          period: data.period || 0,
          credit: data.credit || 0,
          teacherName: data.teacherName || '未知教师',
          joinCode: data.joinCode || '',
          syllabus: data.syllabus || '',
          intro: data.intro || '',
          coverGradient: courseInfo.value.coverGradient
        }
        stats.value.studentCount = data.memberCount || 0
        if (courseInfo.value.syllabus) {
          try {
            const parsed = JSON.parse(courseInfo.value.syllabus)
            courseSyllabus.value = Array.isArray(parsed) ? parsed : []
          } catch (parseError) {
            courseSyllabus.value = []
          }
        } else {
          courseSyllabus.value = []
        }
        courseIntro.value = courseInfo.value.intro || ''
      }
    } else {
      // 学生端：走学生专用接口
      const res = await getStudentCourseDetail(courseId, userStore.userId)
      if (res && res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        courseInfo.value = {
          ...courseInfo.value,
          courseNum: data.courseNum || '',
          courseName: data.courseName || '',
          className: data.className || '',
          term: data.term || '',
          period: data.period || 0,
          credit: data.credit || 0,
          teacherName: data.teacherName || '暂无教师',
          studentCount: data.memberCount || 0,
          status: data.status || 1
        }
        // 学生人数
        stats.value.studentCount = data.memberCount || 0
      }
    }
  } catch (e) {
    console.warn('[课程详情] 接口调用失败:', e)
  }
}

/**
 * 加载作业列表（按角色分派到教师端 / 学生端接口）
 * 后端字段：homeworkId / title / description / type / activityTag / totalScore /
 *          forbidLate / deadline / isOver / gradedCount / ungradedCount / unsubmittedCount
 * 字段名为 homeworkId（非 id），前端展示和路由跳转时统一用 homeworkId
 */
const loadHomeworkList = async () => {
  try {
    const api = userStore.role === 'teacher' ? getTeacherHomeworkList : getStudentHomeworkList
    const res = await api({ courseId: String(courseId) })
    if (res?.data?.code === 200 && Array.isArray(res.data.data)) {
      homeworkList.value = res.data.data
      // 作业总数用列表长度实时算，避免后端冗余字段
      stats.value.homeworkCount = homeworkList.value.length
      // ★ 调试探针：把 homeworkList 的实际内容打到控制台，方便排查"所有卡片 ID 都一样"的诡异问题
      console.log('[CourseDetail] 作业列表实际数据:', JSON.stringify(homeworkList.value))
    } else {
      homeworkList.value = []
      stats.value.homeworkCount = 0
    }
  } catch (e) {
    console.warn('[作业列表] 接口调用失败:', e)
    homeworkList.value = []
    stats.value.homeworkCount = 0
  }
}

/**
 * 打开"编辑课程大纲"对话框
 * 把后端的 JSON 字符串解析回数组，让教师逐行编辑
 */
const openEditSyllabusDialog = () => {
  editSyllabusRows.value = courseSyllabus.value.length > 0
    ? JSON.parse(JSON.stringify(courseSyllabus.value))
    : [{ week: '', title: '', content: '', type: 'primary' }]
  editSyllabusVisible.value = true
}

/**
 * 大纲编辑：新增一行
 */
const addSyllabusRow = () => {
  editSyllabusRows.value.push({ week: '', title: '', content: '', type: 'primary' })
}

/**
 * 大纲编辑：删除一行
 */
const removeSyllabusRow = (index) => {
  editSyllabusRows.value.splice(index, 1)
}

/**
 * 大纲编辑：保存（提交到后端）
 */
const saveSyllabus = async () => {
  // 1. 校验：每行必须有 week + title
  for (let i = 0; i < editSyllabusRows.value.length; i++) {
    const row = editSyllabusRows.value[i]
    if (!row.week || !row.week.trim()) {
      ElMessage.warning(`第 ${i + 1} 行周次不能为空`)
      return
    }
    if (!row.title || !row.title.trim()) {
      ElMessage.warning(`第 ${i + 1} 行标题不能为空`)
      return
    }
  }
  savingInfo.value = true
  try {
    const json = JSON.stringify(editSyllabusRows.value)
    const res = await updateCourseInfo({
      teacherId: String(userStore.userId),
      courseId: String(courseId),
      syllabus: json
    })
    if (res?.data?.code === 200) {
      ElMessage.success('课程大纲已更新')
      editSyllabusVisible.value = false
      // 立即刷新本地数据，避免等 10s 轮询
      courseSyllabus.value = JSON.parse(json)
      courseInfo.value.syllabus = json
    } else {
      ElMessage.error(res?.data?.msg || '更新失败')
    }
  } catch (e) {
    console.error('[保存大纲] 异常:', e)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    savingInfo.value = false
  }
}

/**
 * 打开"编辑课程介绍"对话框
 */
const openEditIntroDialog = () => {
  editIntroDraft.value = courseIntro.value || ''
  editIntroVisible.value = true
}

/**
 * 介绍编辑：保存
 */
const saveIntro = async () => {
  savingInfo.value = true
  try {
    const res = await updateCourseInfo({
      teacherId: String(userStore.userId),
      courseId: String(courseId),
      intro: editIntroDraft.value
    })
    if (res?.data?.code === 200) {
      ElMessage.success('课程介绍已更新')
      editIntroVisible.value = false
      courseIntro.value = editIntroDraft.value
      courseInfo.value.intro = editIntroDraft.value
    } else {
      ElMessage.error(res?.data?.msg || '更新失败')
    }
  } catch (e) {
    console.error('[保存介绍] 异常:', e)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    savingInfo.value = false
  }
}

/**
 * 复制加课码到剪贴板
 */
const copyJoinCode = async () => {
  if (!courseInfo.value.joinCode) return
  try {
    await navigator.clipboard.writeText(courseInfo.value.joinCode)
    ElMessage.success('加课码已复制')
  } catch (e) {
    // 浏览器不支持 Clipboard API 时降级为选中文本
    const input = document.createElement('input')
    input.value = courseInfo.value.joinCode
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('加课码已复制')
  }
}
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
/* 真实数据库 ID 标识：每个卡片显示自己的 ID，与后端一一对应 */
.hw-db-id {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  word-break: break-all;
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

/* ===== 大纲编辑行样式 ===== */
.syllabus-edit-row {
  border: 1px dashed #e5e7eb;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
  background: #fafbfc;
}
.syllabus-edit-fields {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* 编辑大纲弹窗里"类型"下拉选项前的颜色预览块 */
.syllabus-type-option {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}
.syllabus-type-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: middle;
}
.syllabus-type-dot--primary { background-color: #409eff; }
.syllabus-type-dot--success { background-color: #67c23a; }
.syllabus-type-dot--warning { background-color: #e6a23c; }
.syllabus-type-dot--danger  { background-color: #f56c6c; }
.syllabus-type-dot--info    { background-color: #909399; }

.readonly-field {
  font-size: 13px;
  color: #6b7280;
}
</style>