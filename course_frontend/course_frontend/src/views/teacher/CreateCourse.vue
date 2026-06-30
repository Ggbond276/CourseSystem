<template>
  <div class="create-course">
    <!-- 顶部 Header：返回 + 标题 -->
    <div class="page-header">
      <el-button text @click="handleBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span style="margin-left: 4px;">返回课程列表</span>
      </el-button>
      <h2 class="page-title">创建新课程</h2>
      <p class="page-subtitle">填写下面的信息，系统会自动生成 6 位加课码</p>
    </div>

    <!-- 表单卡片 -->
    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="课程编号" prop="courseNum">
          <el-input
            v-model="form.courseNum"
            placeholder="例如：CS-101"
            maxlength="50"
            show-word-limit
            clearable
          />
          <div class="form-tip">选填，作为课程的简短代号</div>
        </el-form-item>

        <el-form-item label="课程名称" prop="courseName">
          <el-input
            v-model="form.courseName"
            placeholder="例如：计算机科学导论"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="教学班级" prop="className">
          <el-input
            v-model="form.className"
            placeholder="例如：2024级软件工程1班"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="学年学期" prop="term">
          <el-input
            v-model="form.term"
            placeholder="例如：2024-2025 第一学期"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="学时" prop="period">
          <el-input-number
            v-model="form.period"
            :min="0"
            :max="500"
            :step="2"
            controls-position="right"
          />
          <div class="form-tip">课程总学时，0 表示不填</div>
        </el-form-item>

        <el-form-item label="学分" prop="credit">
          <el-input-number
            v-model="form.credit"
            :min="0"
            :max="20"
            :precision="1"
            :step="0.5"
            controls-position="right"
          />
          <div class="form-tip">课程学分，0 表示不填</div>
        </el-form-item>

        <el-form-item label="封面 URL" prop="cover">
          <el-input
            v-model="form.cover"
            placeholder="选填：课程封面的图片地址"
            clearable
          />
        </el-form-item>
      </el-form>

      <!-- 底部操作按钮 -->
      <div class="form-footer">
        <el-button @click="handleBack">取 消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>
          <span>立即创建</span>
        </el-button>
      </div>
    </el-card>

    <!-- ===== 创建成功对话框（显示加课码） ===== -->
    <el-dialog
      v-model="successDialogVisible"
      title="创建成功"
      width="460px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="success-content">
        <div class="success-icon">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <p class="success-text">课程已创建成功！</p>
        <p class="success-tip">请将以下加课码告知学生，让他们可以加入课程：</p>

        <div class="code-box">
          <div class="code-label">加课码</div>
          <div class="code-value">{{ createdJoinCode }}</div>
        </div>

        <el-alert
          type="warning"
          :closable="false"
          show-icon
        >
          <template #title>
            <span>请妥善保管此加课码，学生加入课程时需要用到</span>
          </template>
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="successDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="handleGoToCourseList"
        >
          返回课程列表
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Check, CircleCheckFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { createCourse } from '@/api/course'

const router = useRouter()
const userStore = useUserStore()

// ===== 表单引用 =====
const formRef = ref(null)

// ===== 提交态 =====
const submitting = ref(false)

// ===== 成功弹窗 =====
const successDialogVisible = ref(false)
const createdJoinCode = ref('')

// ===== 表单数据 =====
const form = reactive({
  courseNum: '',
  courseName: '',
  className: '',
  term: '',
  period: 48,
  credit: 3.0,
  cover: ''
})

// ===== 表单校验规则 =====
const formRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入教学班级', trigger: 'blur' }
  ],
  term: [
    { required: true, message: '请输入学年学期', trigger: 'blur' }
  ],
  period: [
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined) {
          callback(new Error('请输入学时'))
          return
        }
        if (value < 0) {
          callback(new Error('学时不能小于 0'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  credit: [
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined) {
          callback(new Error('请输入学分'))
          return
        }
        if (value < 0) {
          callback(new Error('学分不能小于 0'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

// ===== 返回上一页 =====
const handleBack = () => {
  router.push('/teacher/courses')
}

// ===== 提交表单 =====
const handleSubmit = async () => {
  if (!formRef.value) return

  // 先做表单校验
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 必须有 teacherId
    const teacherId = userStore.userId
    if (!teacherId) {
      ElMessage.warning('未检测到登录信息，请重新登录')
      return
    }

    submitting.value = true
    try {
      // 组装请求参数（必须带上 teacherId，等 UserContext 就绪后可去掉）
      const payload = {
        teacherId,
        courseNum: form.courseNum || null,
        courseName: form.courseName,
        className: form.className,
        term: form.term,
        period: form.period,
        credit: form.credit,
        cover: form.cover || null
      }

      const res = await createCourse(payload)
      if (res && res.data && res.data.code === 200) {
        // 取出后端返回的加课码
        const data = res.data.data || {}
        createdJoinCode.value = data.joinCode || ''
        successDialogVisible.value = true
        ElMessage.success('建课成功')
      } else {
        ElMessage.error((res && res.data && res.data.msg) || '建课失败')
      }
    } catch (e) {
      console.error('建课接口异常:', e)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// ===== 成功后跳转回课程列表 =====
const handleGoToCourseList = () => {
  successDialogVisible.value = false
  router.push('/teacher/courses')
}
</script>

<style scoped>
.create-course {
  max-width: 800px;
  margin: 0 auto;
}

/* ===== 顶部 ===== */
.page-header {
  margin-bottom: 20px;
}
.back-btn {
  margin-bottom: 12px;
  color: #6b7280;
}
.back-btn:hover { color: #2563eb; }

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

/* ===== 表单卡片 ===== */
.form-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 8px 0;
}
.form-card :deep(.el-form-item) {
  margin-bottom: 22px;
}
.form-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
  line-height: 1.4;
}

/* ===== 表单底部按钮 ===== */
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  margin-top: 8px;
  border-top: 1px dashed #e5e7eb;
}

/* ===== 成功弹窗内容 ===== */
.success-content {
  text-align: center;
  padding: 8px 0;
}
.success-icon {
  font-size: 56px;
  color: #10b981;
  margin-bottom: 12px;
}
.success-text {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}
.success-tip {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 16px 0;
}
.code-box {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border: 2px dashed #f59e0b;
  border-radius: 10px;
  padding: 18px;
  margin-bottom: 16px;
}
.code-label {
  font-size: 12px;
  color: #92400e;
  margin-bottom: 6px;
}
.code-value {
  font-size: 32px;
  font-weight: 700;
  color: #b45309;
  font-family: ui-monospace, monospace;
  letter-spacing: 4px;
}
</style>
