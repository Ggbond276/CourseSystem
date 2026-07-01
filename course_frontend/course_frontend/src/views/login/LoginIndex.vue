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
      <div class="brand-footer">2024 课堂管理系统 · Powered by Vue 3 + Spring Boot</div>
    </div>

    <!-- 右侧登录/注册表单区 -->
    <div class="login-form-wrapper">
      <div class="login-form-box">
        <!-- 登录 / 注册 Tab 切换 -->
        <div class="form-tabs">
          <div
            class="tab-item"
            :class="{ active: activeTab === 'login' }"
            @click="activeTab = 'login'"
          >
            登录
          </div>
          <div
            class="tab-item"
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >
            注册
          </div>
        </div>

        <!-- ==================== 登录表单 ==================== -->
        <div v-show="activeTab === 'login'" class="tab-content">
          <p class="form-subtitle">请选择学校并登录</p>
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            size="large"
          >
            <!-- 学校下拉 -->
            <el-form-item prop="schoolId">
              <el-select
                v-model="loginForm.schoolId"
                placeholder="请选择学校"
                filterable
                class="full-width"
                :prefix-icon="School"
                :loading="schoolLoading"
              >
                <el-option
                  v-for="school in schoolList"
                  :key="school.id"
                  :label="school.name"
                  :value="school.id"
                />
              </el-select>
            </el-form-item>

            <!-- 账号 -->
            <el-form-item prop="account">
              <el-input
                v-model="loginForm.account"
                placeholder="请输入学号/工号"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <!-- 密码 -->
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

        <!-- ==================== 注册表单 ==================== -->
        <div v-show="activeTab === 'register'" class="tab-content">
          <p class="form-subtitle">注册新账号</p>
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="login-form"
            size="large"
          >
            <!-- 学校下拉 -->
            <el-form-item prop="schoolId">
              <el-select
                v-model="registerForm.schoolId"
                placeholder="请选择学校"
                filterable
                class="full-width"
                :prefix-icon="School"
                :loading="schoolLoading"
              >
                <el-option
                  v-for="school in schoolList"
                  :key="school.id"
                  :label="school.name"
                  :value="school.id"
                />
              </el-select>
            </el-form-item>

            <!-- 姓名 -->
            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="请输入真实姓名"
                :prefix-icon="UserFilled"
                clearable
              />
            </el-form-item>

            <!-- 账号 -->
            <el-form-item prop="account">
              <el-input
                v-model="registerForm.account"
                placeholder="请输入学号/工号"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <!-- 手机号 -->
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                clearable
              />
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（至少6位）"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                class="login-button"
                :loading="loading"
                @click="handleRegister"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-tips">
            <span>已有账号？</span>
            <el-link type="primary" @click="activeTab = 'login'">直接登录</el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, Lock, Reading, Check, School, UserFilled, Phone
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login, register, getSchoolList } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// Tab 切换
const activeTab = ref('login')

// 学校列表
const schoolList = ref([])
const schoolLoading = ref(false)

// 加载状态
const loading = ref(false)

// 表单引用
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录表单数据
// schoolId 存字符串（学校列表的 id 在后端被强制转 String 输出，避开 JS Number 丢精度陷阱）
const loginForm = reactive({
  schoolId: '',
  account: '',
  password: ''
})

// 注册表单数据
const registerForm = reactive({
  schoolId: '',
  name: '',
  account: '',
  phone: '',
  password: ''
})

// 登录表单验证规则
const loginRules = {
  schoolId: [
    { required: true, message: '请选择学校', trigger: 'change' }
  ],
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册表单验证规则
const registerRules = {
  schoolId: [
    { required: true, message: '请选择学校', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  account: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

// 挂载时加载学校列表
onMounted(() => {
  loadSchoolList()
})

// 加载学校列表
const loadSchoolList = async () => {
  schoolLoading.value = true
  try {
    const res = await getSchoolList()
    if (res.data.code === 200) {
      schoolList.value = res.data.data
    }
  } catch (error) {
    console.error('加载学校列表异常:', error)
  } finally {
    schoolLoading.value = false
  }
}

// 根据账号前缀判断角色
const resolveHomePathByRole = (account) => {
  if (!account) return '/teacher/dashboard'
  const firstChar = account.charAt(0).toUpperCase()
  if (firstChar === 'S') return '/student/dashboard'
  return '/teacher/dashboard'
}

// 登录处理函数
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(loginForm)
      if (res.data.code === 200) {
        userStore.setToken(res.data.data.token)
        userStore.setUserInfo({
          userId: res.data.data.userId,
          name: res.data.data.name,
          avatar: res.data.data.avatar,
          phone: res.data.data.phone,
          schoolId: res.data.data.schoolId
        })
        userStore.setRole(
          resolveHomePathByRole(loginForm.account) === '/teacher/dashboard'
            ? 'teacher'
            : 'student'
        )
        ElMessage.success('登录成功')
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
  })
}

// 注册处理函数
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await register(registerForm)
      if (res.data.code === 200) {
        ElMessage.success('注册成功，请登录')
        activeTab.value = 'login'
        loginForm.account = registerForm.account
        loginForm.schoolId = registerForm.schoolId
        registerFormRef.value.resetFields()
      } else {
        ElMessage.error(res.data.msg || '注册失败')
      }
    } catch (error) {
      console.error('注册异常:', error)
      ElMessage.error('网络异常，请稍后重试')
    } finally {
      loading.value = false
    }
  })
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
  padding: 40px 36px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* ========== Tab 切换 ========== */
.form-tabs {
  display: flex;
  border-bottom: 2px solid #e5e7eb;
  margin-bottom: 28px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
}

.tab-item.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}

.form-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 24px 0;
}

.login-form {
  width: 100%;
}

/* 下拉框占满宽度 */
.full-width {
  width: 100%;
}

/* 输入框前缀图标颜色微调 */
.login-form :deep(.el-input__prefix-inner .el-icon) {
  color: #9ca3af;
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
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
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
