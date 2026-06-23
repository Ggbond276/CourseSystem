<template>
  <div class="create-homework-container">
    <h2 class="page-title">发布作业</h2>

    <div class="form-box">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
        class="homework-form"
      >
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入作业标题" />
        </el-form-item>

        <el-form-item label="作业要求" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入作业要求描述"
          />
        </el-form-item>

        <el-form-item label="作业模式" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">个人作业</el-radio>
            <el-radio :label="2">小组作业</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="应用环节" prop="activityTag">
          <el-select v-model="form.activityTag" placeholder="请选择作业所属环节">
            <el-option label="课前" value="课前" />
            <el-option label="课中" value="课中" />
            <el-option label="课后" value="课后" />
            <el-option label="期末" value="期末" />
          </el-select>
        </el-form-item>

        <el-form-item label="满分值" prop="totalScore">
          <el-input-number v-model="form.totalScore" :min="1" :max="1000" />
        </el-form-item>

        <el-form-item label="超时提交" prop="forbidLate">
          <el-radio-group v-model="form.forbidLate">
            <el-radio :label="0">允许超时提交</el-radio>
            <el-radio :label="1">超时禁止提交</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            发 布
          </el-button>
          <el-button @click="handleBack">返 回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { createHomework } from '@/api/homework'

const route = useRoute()
const router = useRouter()

const courseId = route.query.courseId

// 表单引用
const formRef = ref(null)

// 加载状态
const loading = ref(false)

// 表单数据
const form = reactive({
  courseId: courseId || null,
  title: '',
  description: '',
  type: 1,
  activityTag: '课后',
  totalScore: 100,
  forbidLate: 0,
  deadline: '',
  attachments: []
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' }
  ],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await createHomework(form)
      if (res.data.code === 200) {
        ElMessage.success('作业发布成功')
        router.back()
      } else {
        ElMessage.error(res.data.msg || '发布作业失败')
      }
    } catch (error) {
      console.error('发布作业异常:', error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 返回上一页
const handleBack = () => {
  router.back()
}
</script>

<style scoped>
.create-homework-container {
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
  max-width: 700px;
}

.homework-form {
  width: 100%;
}
</style>
