import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 路由懒加载的视图组件
const LoginIndex = () => import('@/views/login/LoginIndex.vue')
const CourseManage = () => import('@/views/teacher/CourseManage.vue')
const CreateCourse = () => import('@/views/teacher/CreateCourse.vue')
const CourseHomework = () => import('@/views/teacher/CourseHomework.vue')
const CreateHomework = () => import('@/views/teacher/CreateHomework.vue')
const HomeworkSubmissions = () => import('@/views/teacher/HomeworkSubmissions.vue')
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
  {
    path: '/teacher',
    redirect: '/teacher/course-manage'
  },
  {
    path: '/teacher/course-manage',
    name: 'CourseManage',
    component: CourseManage,
    meta: { title: '我的课程', requiresAuth: true }
  },
  {
    path: '/teacher/create-course',
    name: 'CreateCourse',
    component: CreateCourse,
    meta: { title: '创建课程', requiresAuth: true }
  },
  {
    path: '/teacher/course/:courseId',
    name: 'CourseHomework',
    component: CourseHomework,
    meta: { title: '作业管理', requiresAuth: true }
  },
  {
    path: '/teacher/homework/create',
    name: 'CreateHomework',
    component: CreateHomework,
    meta: { title: '发布作业', requiresAuth: true }
  },
  {
    path: '/teacher/homework/:homeworkId/submissions',
    name: 'HomeworkSubmissions',
    component: HomeworkSubmissions,
    meta: { title: '批阅大厅', requiresAuth: true }
  },
  {
    path: '/student',
    redirect: '/student/my-course'
  },
  {
    path: '/student/my-course',
    name: 'MyCourse',
    component: MyCourse,
    meta: { title: '我的课程', requiresAuth: true }
  },
  {
    path: '/student/course/:courseId',
    name: 'StudentHomework',
    component: StudentHomework,
    meta: { title: '作业列表', requiresAuth: true }
  },
  {
    path: '/student/homework/:homeworkId',
    name: 'StudentHomeworkDetail',
    component: StudentHomeworkDetail,
    meta: { title: '作业详情', requiresAuth: true }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫：登录拦截
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 课堂管理系统`
  }

  // 判断该路由是否需要登录权限
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    // 未登录则跳转到登录页
    next('/login')
  } else {
    next()
  }
})

export default router
