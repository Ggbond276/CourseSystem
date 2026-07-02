<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.name || '老师' }}</h2>
        <p class="welcome-subtitle">今天是个适合教学的好日子。</p>
      </div>
    </div>

    <!-- 顶部 4 个数据统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="24" color="#ffffff"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 底部主区：2:1 黄金分割（左侧看板 / 右侧动态） -->
    <el-row :gutter="20" class="bottom-row">
      <!-- ========== 左侧：作业处理看板 ========== -->
      <el-col :xs="24" :md="16">
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">作业处理看板</span>
              <!-- 右侧：课程筛选下拉 + 查看全部按钮 -->
              <div class="header-right">
                <el-select
                  v-model="selectedCourseId"
                  size="small"
                  placeholder="筛选课程"
                  clearable
                  :loading="courseLoading"
                  style="width: 160px;"
                  @change="onCourseChange"
                >
                  <el-option label="全部课程" value="all" />
                  <el-option
                    v-for="c in courseList"
                    :key="c.id"
                    :label="c.courseName || c.name"
                    :value="c.id"
                  />
                </el-select>
                <el-button type="primary" link @click="handleViewAll">
                  查看全部
                </el-button>
              </div>
            </div>
          </template>

          <!-- v-loading：作业列表请求中显示骨架 -->
          <el-tabs v-model="activeBoardTab" class="board-tabs" v-loading="homeworkLoading">
            <!-- Tab 1：待批阅作业（isOver=false 且 ungradedCount>0） -->
            <el-tab-pane label="待批阅作业" name="todo">
              <div class="board-list">
                <div
                  v-for="item in todoList"
                  :key="item.homeworkId"
                  class="board-item"
                  @click="$router.push(`/teacher/homework/${item.homeworkId}/submissions`)"
                >
                  <div class="board-item-left">
                    <div class="board-item-title">{{ item.title || '(无标题)' }}</div>
                    <div class="board-item-meta">
                      <span class="meta-chip">
                        <el-icon><Clock /></el-icon>
                        截止 {{ item.deadline || '未设置' }}
                      </span>
                      <span class="meta-chip">
                        <el-icon><User /></el-icon>
                        已提交 {{ item.submittedCount ?? 0 }} / {{ item.totalCount ?? 0 }}
                      </span>
                    </div>
                  </div>
                  <div class="board-item-right">
                    <el-tag :type="getUrgentTagType(item)" size="small">
                      {{ getUrgentText(item) }}
                    </el-tag>
                    <el-button
                      type="primary"
                      size="small"
                      @click.stop="$router.push(`/teacher/homework/${item.homeworkId}/submissions`)"
                    >
                      去处理
                    </el-button>
                  </div>
                </div>
                <el-empty
                  v-if="!homeworkLoading && todoList.length === 0"
                  :description="selectedCourseId && selectedCourseId !== 'all'
                    ? '该课程暂无待批作业'
                    : '暂无待批作业'"
                  :image-size="60"
                />
              </div>
            </el-tab-pane>

            <!-- Tab 2：全部作业（该课程下所有作业） -->
            <el-tab-pane label="全部作业" name="recent">
              <div class="board-list">
                <div
                  v-for="item in recentList"
                  :key="item.homeworkId"
                  class="board-item"
                  @click="$router.push(`/teacher/homework/${item.homeworkId}/submissions`)"
                >
                  <div class="board-item-left">
                    <div class="board-item-title">{{ item.title || '(无标题)' }}</div>
                    <div class="board-item-meta">
                      <span class="meta-chip">
                        <el-icon><Clock /></el-icon>
                        截止 {{ item.deadline || '未设置' }}
                      </span>
                      <span class="meta-chip">
                        <el-icon><User /></el-icon>
                        已提交 {{ item.submittedCount ?? 0 }} / {{ item.totalCount ?? 0 }}
                      </span>
                    </div>
                  </div>
                  <div class="board-item-right">
                    <el-tag :type="getStatusTagType(item)" size="small">
                      {{ getStatusText(item) }}
                    </el-tag>
                    <el-button
                      type="primary"
                      size="small"
                      plain
                      @click.stop="$router.push(`/teacher/course/detail/${item.courseId}`)"
                    >
                      查看详情
                    </el-button>
                  </div>
                </div>
                <el-empty
                  v-if="!homeworkLoading && recentList.length === 0"
                  :description="selectedCourseId && selectedCourseId !== 'all'
                    ? '该课程暂无作业'
                    : '暂无作业'"
                  :image-size="60"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- ========== 右侧：近期动态 ========== -->
      <el-col :xs="24" :md="8">
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">近期动态</span>
            </div>
          </template>
          <!-- 有待批作业时提示，无动态时显示空态 -->
          <template v-if="totalUngradedCount > 0">
            <el-alert
              :title="`您有 ${totalUngradedCount} 份作业待批阅`"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 16px; border-radius: 8px;"
            />
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in activityList"
              :key="index"
              :timestamp="item.time"
              :type="item.timelineType"
              placement="top"
            >
              <div class="activity-title">{{ item.title }}</div>
              <div class="activity-desc">{{ item.desc }}</div>
            </el-timeline-item>
            <el-timeline-item
              v-if="activityList.length === 0 && !homeworkLoading"
              timestamp="" type="info" placement="top"
            >
              <el-empty description="暂无动态" :image-size="50" />
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Reading,
  User, Clock, Calendar,
  UserFilled, Notebook, TrendCharts, Trophy
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getTeacherCourseList } from '@/api/course'
import { getTeacherHomeworkList } from '@/api/homework'

const userStore = useUserStore()
const router = useRouter()

/* ==================== 欢迎语 ==================== */
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

/* ==================== 顶部 4 个统计卡片 ==================== */
const statCards = ref([
  { label: '授课班级', value: '--', icon: UserFilled, color: '#2563eb' },
  { label: '在带课程', value: '--', icon: Notebook, color: '#10b981' },
  { label: '待批作业', value: '--', icon: Reading, color: '#f59e0b' },
  { label: '本周活跃', value: '--', icon: TrendCharts, color: '#8b5cf6' }
])

/* ==================== 课程列表 ==================== */
// courseList 存放拉平后的课程数组 [{ id, courseName }]
const courseList = ref([])
const courseLoading = ref(false)

/* ==================== 作业看板状态 ==================== */
const activeBoardTab = ref('todo')
// allHomeworkList：当前选中课程的完整作业列表（由 API 返回）
const allHomeworkList = ref([])
const homeworkLoading = ref(false)

// selectedCourseId：null/''/'all' = 全部，数字字符串 = 具体课程 ID
const selectedCourseId = ref('all')

/* ==================== 计算属性：Tab 过滤 ==================== */
// 待批阅：isOver=false 且 ungradedCount>0
const todoList = computed(() => {
  if (!allHomeworkList.value || allHomeworkList.value.length === 0) return []
  return allHomeworkList.value.filter(item => {
    const isOver = item.isOver === true || item.isOver === 1
    const ungraded = Number(item.ungradedCount ?? 0)
    return !isOver && ungraded > 0
  })
})

// 全部作业：直接展示完整列表
const recentList = computed(() => {
  return allHomeworkList.value || []
})

// 所有待批总数（用于右侧动态卡片 Alert）
const totalUngradedCount = computed(() => {
  if (!allHomeworkList.value) return 0
  return allHomeworkList.value.reduce((sum, item) => {
    return sum + Number(item.ungradedCount ?? 0)
  }, 0)
})

/* ==================== 右侧近期动态 ==================== */
const activityList = ref([])

/* ==================== 辅助函数：Tag 类型映射 ==================== */
const getUrgentTagType = (item) => {
  if (!item) return 'info'
  // 已截止但仍有未批：danger
  if (item.isOver === true || item.isOver === 1) return 'danger'
  // 未截止但临近 48h：warning
  return 'success'
}

const getUrgentText = (item) => {
  if (!item) return '未知'
  if (item.isOver === true || item.isOver === 1) return '已截止'
  const ungraded = Number(item.ungradedCount ?? 0)
  return `${ungraded} 待批`
}

const getStatusTagType = (item) => {
  if (!item) return 'info'
  if (item.isOver === true || item.isOver === 1) return 'info'
  return 'success'
}

const getStatusText = (item) => {
  if (!item) return '未知'
  if (item.isOver === true || item.isOver === 1) return '已截止'
  return '进行中'
}

/* ==================== API 请求函数 ==================== */

/**
 * 加载教师课程列表（按学期分组 → 扁平化）
 * 接口：GET /course/teacher/list?teacherId=xxx
 * 响应：[{ term: '2024春季', courses: [{ id, courseName, ... }] }]
 */
const loadCourseList = async () => {
  courseLoading.value = true
  try {
    const res = await getTeacherCourseList(userStore.userId)
    // 响应结构：{ code, msg, data: [{ term, courses: [...] }] }
    if (res.data && res.data.code === 200) {
      const rawList = res.data.data || []
      // 把嵌套数组扁平化，提取每个课程的 id 和 courseName
      const flat = []
      for (const group of rawList) {
        const courses = group.courses || []
        for (const c of courses) {
          flat.push({
            id: c.id,
            courseName: c.courseName || c.name || ''
          })
        }
      }
      courseList.value = flat

      // 自动选中第一门课程（如果有），并加载其作业
      if (flat.length > 0 && !selectedCourseId.value) {
        selectedCourseId.value = flat[0].id
      }
    } else {
      ElMessage.error('获取课程列表失败：' + (res.data?.msg || '未知错误'))
    }
  } catch (err) {
    console.error('[TeacherDashboard] loadCourseList error:', err)
    ElMessage.error('获取课程列表失败，请检查网络')
  } finally {
    courseLoading.value = false
  }
}

/**
 * 加载某门课程下的作业列表
 * 接口：GET /homework/teacher/list?courseId=xxx
 * 响应：[{ homeworkId, title, deadline, isOver, gradedCount, ungradedCount, unsubmittedCount }]
 * 当 courseId 为空时调用此函数会直接返回（不应该出现）
 */
const loadHomeworkList = async (courseId) => {
  if (!courseId || courseId === 'all') {
    allHomeworkList.value = []
    activityList.value = []
    return
  }

  homeworkLoading.value = true
  try {
    const res = await getTeacherHomeworkList({ courseId })
    // 响应结构：{ code, msg, data: [{ homeworkId, title, deadline, ... }] }
    if (res.data && res.data.code === 200) {
      const list = res.data.data || []
      allHomeworkList.value = list

      // 生成右侧动态时间轴
      buildActivityList(list)

      // 更新"在带课程"和"待批作业"统计
      updateStatCards(list)
    } else {
      ElMessage.error('获取作业列表失败：' + (res.data?.msg || '未知错误'))
    }
  } catch (err) {
    console.error('[TeacherDashboard] loadHomeworkList error:', err)
    ElMessage.error('获取作业列表失败，请检查网络')
    allHomeworkList.value = []
  } finally {
    homeworkLoading.value = false
  }
}

/**
 * 构建右侧"近期动态"时间轴（从当前课程的作业数据生成）
 */
const buildActivityList = (list) => {
  const activities = []

  for (const item of list) {
    const title = item.title || '(无标题)'
    const submitted = Number(item.submittedCount ?? 0)
    const total = Number(item.totalCount ?? 0)
    const ungraded = Number(item.ungradedCount ?? 0)
    const isOver = item.isOver === true || item.isOver === 1

    if (isOver && ungraded > 0) {
      activities.push({
        time: '已截止',
        timelineType: 'danger',
        title: `《${title}》已到期，尚有 ${ungraded} 份未批`,
        desc: `共 ${total} 人，已提交 ${submitted} 人`
      })
    } else if (!isOver && ungraded > 0) {
      activities.push({
        time: '进行中',
        timelineType: 'warning',
        title: `《${title}》有 ${ungraded} 份待批`,
        desc: `截止 ${item.deadline || '未设置'}`
      })
    } else if (!isOver && submitted > 0) {
      activities.push({
        time: '进行中',
        timelineType: 'success',
        title: `《${title}》已发布 ${submitted} 人已提交`,
        desc: `截止 ${item.deadline || '未设置'}`
      })
    } else {
      activities.push({
        time: '已发布',
        timelineType: 'info',
        title: `《${title}》`,
        desc: `截止 ${item.deadline || '未设置'}`
      })
    }
  }

  activityList.value = activities
}

/**
 * 根据作业列表更新顶部统计卡片
 */
const updateStatCards = (list) => {
  // 授课班级 = 课程列表长度
  statCards.value[0].value = courseList.value.length || '--'
  // 在带课程 = 当前选中课程数（1）
  statCards.value[1].value = selectedCourseId.value && selectedCourseId.value !== 'all' ? '1' : String(courseList.value.length || 0)
  // 待批作业 = 所有待批总数
  statCards.value[2].value = totalUngradedCount.value || '--'
  // 本周活跃：简化处理 = 已发布且进行中的作业数
  const activeCount = (list || []).filter(item => !(item.isOver === true || item.isOver === 1)).length
  statCards.value[3].value = activeCount || '--'
}

/* ==================== 事件处理 ==================== */

/**
 * 课程筛选下拉切换时，重新加载该课程的作业数据
 */
const onCourseChange = (val) => {
  selectedCourseId.value = val
  loadHomeworkList(val)
}

/**
 * "查看全部"按钮：未选课程时提示，已选课程时跳转
 */
const handleViewAll = () => {
  if (!selectedCourseId.value || selectedCourseId.value === 'all') {
    ElMessage.warning('请先在下拉框选择一门具体的课程，再查看该课程的全部作业')
    return
  }
  router.push(`/teacher/course/detail/${selectedCourseId.value}`)
}

/* ==================== 页面初始化 ==================== */
onMounted(async () => {
  // 1. 先拉课程列表（同时填充顶部统计卡片的"授课班级"数）
  await loadCourseList()

  // 2. 如果已有选中课程，立即加载其作业
  if (selectedCourseId.value && selectedCourseId.value !== 'all') {
    await loadHomeworkList(selectedCourseId.value)
  } else if (courseList.value.length > 0) {
    // 没有预选时自动选第一门并加载
    selectedCourseId.value = courseList.value[0].id
    await loadHomeworkList(selectedCourseId.value)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* ========== 欢迎横幅 ========== */
.welcome-card {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
  border-radius: 10px;
  padding: 28px 32px;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

/* ========== 顶部 4 个统计卡片 ========== */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

/* ========== 底部主区 ========== */
.bottom-row {
  margin-bottom: 20px;
}

.panel-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

/* header 右侧：筛选下拉 + 查看全部 */
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

/* ========== 作业处理看板（左侧 Span 16） ========== */
.board-tabs {
  margin-top: -8px;
}

.board-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 380px;
}

.board-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.board-item:hover {
  background: #eff6ff;
  border-color: #c7d2fe;
  transform: translateX(2px);
}

.board-item-left {
  flex: 1;
  min-width: 0;
}

.board-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.board-item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.meta-chip .el-icon {
  font-size: 12px;
}

.board-item-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-left: 16px;
}

/* ========== 近期动态（右侧 Span 8，el-timeline） ========== */
.activity-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
}

.activity-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
  line-height: 1.5;
}
</style>
