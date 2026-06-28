import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 路由懒加载的视图组件
const LoginIndex = () => import('@/views/login/LoginIndex.vue')

// 需要登录后才能访问的业务页面，统一包在 MainLayout 外壳里
const MainLayout = () => import('@/layouts/MainLayout.vue')

const TeacherDashboard = () => import('@/views/dashboard/TeacherDashboard.vue')
const StudentDashboard = () => import('@/views/dashboard/StudentDashboard.vue')

// 课程大厅 + 课程详情（新重构的页面）
const MyCourses = () => import('@/views/course/MyCourses.vue')
const CourseDetail = () => import('@/views/course/CourseDetail.vue')

// 教师批阅、批改
const HomeworkSubmissions = () => import('@/views/teacher/HomeworkSubmissions.vue')
const GradeDetail = () => import('@/views/teacher/GradeDetail.vue')

// 学生作业详情
const StudentHomeworkDetail = () => import('@/views/student/StudentHomeworkDetail.vue')

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginIndex,
    meta: { title: '登录' }
  },

  // ========== 教师端 ==========
  {
    path: '/teacher',
    component: MainLayout,
    redirect: '/teacher/dashboard',
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: TeacherDashboard,
        meta: { title: '工作台' }
      },
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: MyCourses,
        meta: { title: '我的课程' }
      },
      {
        path: 'course/detail/:id',
        name: 'TeacherCourseDetail',
        component: CourseDetail,
        meta: { title: '课程详情', hidden: true }
      },
      {
        path: 'homework/:homeworkId/submissions',
        name: 'HomeworkSubmissions',
        component: HomeworkSubmissions,
        meta: { title: '批阅大厅', hidden: true }
      },
      {
        path: 'homework/:homeworkId/grade',
        name: 'GradeDetail',
        component: GradeDetail,
        meta: { title: '批改作业', hidden: true }
      }
    ]
  },

  // ========== 学生端 ==========
  {
    path: '/student',
    component: MainLayout,
    redirect: '/student/dashboard',
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: StudentDashboard,
        meta: { title: '工作台' }
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: MyCourses,
        meta: { title: '我的课程' }
      },
      {
        path: 'course/detail/:id',
        name: 'StudentCourseDetail',
        component: CourseDetail,
        meta: { title: '课程详情', hidden: true }
      },
      {
        path: 'homework/:homeworkId',
        name: 'StudentHomeworkDetail',
        component: StudentHomeworkDetail,
        meta: { title: '作业详情', hidden: true }
      }
    ]
  },

  // 兜底：任何未匹配路由都跳登录
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由守卫：
 * 1. 设置页面标题
 * 2. 登录拦截（未登录访问需要登录的页面 → 跳 /login）
 * 3. 角色越权拦截（学生访问教师页 → 跳 /student/dashboard；反之亦然）
 */
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 课堂管理系统`
  }

  // 该路由不需要登录，直接放行
  if (!to.matched.some((record) => record.meta.requiresAuth)) {
    return next()
  }

  // 需要登录但未登录 → 跳登录页
  if (!userStore.isLoggedIn()) {
    return next('/login')
  }

  // 角色越权检查：从路由的 meta.role 与用户实际角色比对
  const requiredRole = to.matched.find((r) => r.meta.role)?.meta.role
  if (requiredRole && userStore.role && userStore.role !== requiredRole) {
    const fallback = userStore.role === 'teacher' ? '/teacher/dashboard' : '/student/dashboard'
    return next(fallback)
  }

  next()
})

export default router