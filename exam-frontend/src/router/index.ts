import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      // 管理员
      {
        path: 'admin/user',
        name: 'AdminUser',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'admin/role',
        name: 'AdminRole',
        component: () => import('@/views/admin/RoleManage.vue'),
        meta: { title: '角色管理', icon: 'Avatar', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'admin/config',
        name: 'SysConfig',
        component: () => import('@/views/admin/SysConfig.vue'),
        meta: { title: '系统配置', icon: 'Tools', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'admin/log',
        name: 'SysLog',
        component: () => import('@/views/admin/LogManage.vue'),
        meta: { title: '操作日志', icon: 'Document', roles: ['ROLE_ADMIN'] }
      },
      // 教师
      {
        path: 'teacher/question',
        name: 'QuestionManage',
        component: () => import('@/views/teacher/QuestionManage.vue'),
        meta: { title: '题库管理', icon: 'Edit', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] }
      },
      {
        path: 'teacher/subject',
        name: 'SubjectManage',
        component: () => import('@/views/teacher/SubjectManage.vue'),
        meta: { title: '科目管理', icon: 'Collection', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] }
      },
      {
        path: 'teacher/exam',
        name: 'ExamManage',
        component: () => import('@/views/teacher/ExamManage.vue'),
        meta: { title: '考试管理', icon: 'Tickets', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] }
      },
      {
        path: 'teacher/marking',
        name: 'MarkingManage',
        component: () => import('@/views/teacher/MarkingManage.vue'),
        meta: { title: '阅卷管理', icon: 'Finished', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] }
      },
      {
        path: 'teacher/statistics',
        name: 'Statistics',
        component: () => import('@/views/teacher/Statistics.vue'),
        meta: { title: '成绩统计', icon: 'DataAnalysis', roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] }
      },
      // 学生
      {
        path: 'student/exam',
        name: 'ExamList',
        component: () => import('@/views/student/ExamList.vue'),
        meta: { title: '考试列表', icon: 'Tickets' }
      },
      {
        path: 'student/exam/:id',
        name: 'ExamAnswer',
        component: () => import('@/views/student/ExamAnswer.vue'),
        meta: { title: '在线答题', hidden: true }
      },
      {
        path: 'student/score',
        name: 'MyScore',
        component: () => import('@/views/student/MyScore.vue'),
        meta: { title: '我的成绩', icon: 'DataBoard' }
      },
      {
        path: 'student/wrongbook',
        name: 'WrongBook',
        component: () => import('@/views/student/WrongBook.vue'),
        meta: { title: '错题本', icon: 'Notebook' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  document.title = (to.meta.title as string) || '计算机考试系统'

  const userStore = useUserStore()

  if (to.meta.noAuth) {
    next()
    return
  }

  if (!userStore.token) {
    next('/login')
    return
  }

  // 如果没有用户信息（刷新页面后丢失），先请求
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      userStore.logout()
      next('/login')
      return
    }
  }

  // 角色权限检查
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    if (!requiredRoles.includes(userStore.roleCode)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
