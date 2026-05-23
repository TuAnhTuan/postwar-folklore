import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/ve-hoai-tu',
      name: 've-hoai-tu',
      component: () => import('@/views/AboutView.vue')
    },
    {
      path: '/ly-thuyet',
      name: 'theory',
      component: () => import('@/views/TheoryView.vue')
    },
    {
      path: '/truyen-thuyet',
      name: 'legend',
      component: () => import('@/views/LegendView.vue')
    },
    {
      path: '/bai-viet/:id',
      name: 'post-detail',
      component: () => import('@/views/PostDetailView.vue')
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/admin/AdminLoginView.vue')
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/AdminDashboardView.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0, behavior: 'smooth' }
  }
})

// Navigation guard
router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  // Chờ Firebase xác định auth state xong trước khi check quyền
  if (authStore.loading) {
    await new Promise(resolve => {
      const stop = authStore.$subscribe(() => {
        if (!authStore.loading) { stop(); resolve() }
      })
      // Timeout fallback 5s phòng trường hợp subscribe không fire
      setTimeout(resolve, 5000)
    })
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'admin-login' }
  }

  // Nếu đã là admin mà vào trang login → redirect vào dashboard
  if (to.name === 'admin-login' && authStore.isAdmin) {
    return { name: 'admin-dashboard' }
  }
})

export default router
