<template>
  <div class="create-course-container">
    <h2 class="page-title">创建新课程</h2>

    <div class="form-box">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
        class="course-form"
      >
        <el-form-item label="课程编号" prop="courseNum">
          <el-input v-model="form.courseNum" placeholder="请输入课程编号，如 CS-302" />
        </el-form-item>

        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>

        <el-form-item label="教学班级" prop="className">
          <el-input v-model="form.className" placeholder="请输入教学班级，如 2023级软件工程201班" />
        </el-form-item>

        <el-form-item label="学年学期" prop="term">
          <el-input v-model="form.term" placeholder="请输入学年学期，如 2023-2024 第二学期" />
        </el-form-item>

        <el-form-item label="学时" prop="period">
          <el-input-number v-model="form.period" :min="1" :max="500" />
        </el-form-item>

        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="20" :precision="1" />
        </el-form-item>

        <el-form-item label="课程封面" prop="cover">
          <el-input v-model="form.cover" placeholder="请输入课程封面图片 URL（可选）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            提 交
          </el-button>
          <el-button @click="handleReset">重 置</el-button>
          <el-button @click="handleBack">返 回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createCourse } from '@/api/course'

const router = useRouter()

// 表单引用
const formRef = ref(null)

// 加载状态
const loading = ref(false)

// 表单数据
const form = reactive({
  courseNum: '',
  courseName: '',
  className: '',
  term: '',
  period: 64,
  credit: 4.0,
  cover: ''
})

// 表单验证规则
const formRules = {
  courseNum: [
    { required: true, message: '请输入课程编号', trigger: 'blur' }
  ],
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
    { required: true, message: '请输入学时', trigger: 'blur' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await createCourse(form)
      if (res.data.code === 200) {
        ElMessage.success('创建课程成功')
        router.push('/teacher/course-manage')
      } else {
        ElMessage.error(res.data.msg || '创建课程失败')
      }
    } catch (error) {
      console.error('创建课程异常:', error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
}

// 返回上一页
const handleBack = () => {
  router.back()
}
</script>

<style scoped>
.create-course-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.form-box {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  max-width: 600px;
}

.course-form {
  width: 100%;
}
</style>
