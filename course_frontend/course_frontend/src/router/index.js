import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 路由懒加载的视图组件
const LoginIndex = () => import('@/views/login/LoginIndex.vue')

// 需要登录后才能访问的业务页面，统一包在 MainLayout 外壳里
const MainLayout = () => import('@/layouts/MainLayout.vue')

const TeacherDashboard = () => import('@/views/dashboard/TeacherDashboard.vue')
const StudentDashboard = () => import('@/views/dashboard/StudentDashboard.vue')

const CourseManage = () => import('@/views/teacher/CourseManage.vue')
const CreateCourse = () => import('@/views/teacher/CreateCourse.vue')
const CourseHomework = () => import('@/views/teacher/CourseHomework.vue')
const CreateHomework = () => import('@/views/teacher/CreateHomework.vue')
const HomeworkSubmissions = () => import('@/views/teacher/HomeworkSubmissions.vue')
const TeacherHomeworkList = () => import('@/views/teacher/TeacherHomeworkList.vue')
const GradeDetail = () => import('@/views/teacher/GradeDetail.vue')

const StudentWorkbench = () => import('@/views/student/StudentWorkbench.vue')
const MyCourse = () => import('@/views/student/MyCourse.vue')
const StudentHomework = () => import('@/views/student/StudentHomework.vue')
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
  // ========== 教师端：所有 /teacher/* 路由都包在 MainLayout 里 ==========
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
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'course-manage',
        name: 'CourseManage',
        component: CourseManage,
        meta: { title: '课程管理', icon: 'Reading' }
      },
      {
        path: 'create-course',
        name: 'CreateCourse',
        component: CreateCourse,
        meta: { title: '创建课程', icon: 'Plus' }
      },
      {
        path: 'homework-list',
        name: 'TeacherHomeworkList',
        component: TeacherHomeworkList,
        meta: { title: '作业管理', icon: 'Document' }
      },
      {
        path: 'course/:courseId',
        name: 'CourseHomework',
        component: CourseHomework,
        meta: { title: '作业管理', hidden: true }
      },
      {
        path: 'homework/create',
        name: 'CreateHomework',
        component: CreateHomework,
        meta: { title: '发布作业', hidden: true }
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
  // ========== 学生端：所有 /student/* 路由都包在 MainLayout 里 ==========
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
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'my-course',
        name: 'StudentWorkbench',
        component: StudentWorkbench,
        meta: { title: '我的课程', icon: 'Reading' }
      },
      {
        path: 'course/:courseId',
        name: 'StudentHomework',
        component: StudentHomework,
        meta: { title: '作业列表', hidden: true }
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
