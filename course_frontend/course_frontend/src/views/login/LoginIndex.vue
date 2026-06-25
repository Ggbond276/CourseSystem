<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="40" color="#ffffff"><Reading /></el-icon>
          <span class="brand-name">课堂管理系统</span>
        </div>
        <h1 class="brand-headline">让教学更高效</h1>
        <p class="brand-subheadline">一站式课程管理 · 作业发布 · 在线批阅 · 学习追踪</p>
        <ul class="brand-features">
          <li><el-icon><Check /></el-icon><span>教师轻松管理课程与作业</span></li>
          <li><el-icon><Check /></el-icon><span>学生随时查看作业并在线提交</span></li>
          <li><el-icon><Check /></el-icon><span>数据统计 · 进度追踪 · 班级对比</span></li>
        </ul>
      </div>
      <div class="brand-footer">© 2024 课堂管理系统 · Powered by Vue 3 + Spring Boot</div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="login-form-wrapper">
      <div class="login-form-box">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请使用学号/工号登录系统</p>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
        >
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="请输入学号/工号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-tips">
          <span>测试账号：</span>
          <el-tag size="small" type="info" effect="plain">T001</el-tag>
          <el-tag size="small" type="info" effect="plain">S001</el-tag>
          <span class="tips-password">密码：123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Reading, Check } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const loginForm = reactive({
  account: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

/**
 * 根据账号前缀判断角色
 * T 开头 -> 教师，跳 /teacher/dashboard
 * S 开头 -> 学生，跳 /student/dashboard
 * 默认 -> 教师工作台
 */
const resolveHomePathByRole = (account) => {
  if (!account) return '/teacher/dashboard'
  const firstChar = account.charAt(0).toUpperCase()
  if (firstChar === 'S') return '/student/dashboard'
  return '/teacher/dashboard'
}

// 登录处理函数
const handleLogin = async () => {
  if (!loginForm.account || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const res = await login(loginForm)
    if (res.data.code === 200) {
      // 保存 token 和用户信息到 Pinia
      userStore.setToken(res.data.data.token)
      userStore.setUserInfo({
        userId: res.data.data.userId,
        name: res.data.data.name,
        avatar: res.data.data.avatar,
        phone: res.data.data.phone
      })
      // 记录角色，便于侧边栏按身份显示菜单
      userStore.setRole(resolveHomePathByRole(loginForm.account) === '/teacher/dashboard' ? 'teacher' : 'student')
      ElMessage.success('登录成功')
      // 根据账号前缀跳转到对应工作台
      router.push(resolveHomePathByRole(loginForm.account))
    } else {
      ElMessage.error(res.data.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录异常:', error)
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========== 整体左右分栏布局 ========== */
.login-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  background-color: #ffffff;
}

/* ========== 左侧品牌区 ========== */
.login-brand {
  flex: 1;
  background-color: #1f2937;
  background-image:
    radial-gradient(circle at 20% 20%, rgba(37, 99, 235, 0.25), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(59, 130, 246, 0.18), transparent 55%);
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px 80px;
  box-sizing: border-box;
}

.brand-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 60px;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

.brand-headline {
  font-size: 40px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: 1px;
}

.brand-subheadline {
  font-size: 16px;
  color: #9ca3af;
  margin: 0 0 48px 0;
  line-height: 1.6;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.brand-features li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #d1d5db;
  margin-bottom: 18px;
}

.brand-features .el-icon {
  color: #60a5fa;
  font-size: 18px;
}

.brand-footer {
  font-size: 13px;
  color: #6b7280;
}

/* ========== 右侧表单区 ========== */
.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc;
  padding: 40px;
  box-sizing: border-box;
}

.login-form-box {
  width: 100%;
  max-width: 400px;
  background-color: #ffffff;
  padding: 48px 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.form-title {
  font-size: 26px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 32px 0;
}

.login-form {
  width: 100%;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background-color: #2563eb;
  border-color: #2563eb;
}

.login-button:hover {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.form-tips {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tips-password {
  margin-left: 4px;
  color: #9ca3af;
}

/* ========== 响应式：小屏隐藏左侧品牌区 ========== */
@media (max-width: 900px) {
  .login-brand {
    display: none;
  }
}
</style>
