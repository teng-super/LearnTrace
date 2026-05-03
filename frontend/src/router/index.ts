import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/RegisterView.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('@/views/DashboardView.vue') },
      { path: 'plan', component: () => import('@/views/PlanView.vue') },
      { path: 'goals', component: () => import('@/views/GoalsView.vue') },
      { path: 'goals/:id', component: () => import('@/views/GoalDetailView.vue') },
      { path: 'tasks', component: () => import('@/views/TasksView.vue') },
      { path: 'errors', component: () => import('@/views/ErrorsView.vue') },
      { path: 'errors/new', component: () => import('@/views/ErrorEditView.vue') },
      { path: 'errors/:id/edit', component: () => import('@/views/ErrorEditView.vue') },
      { path: 'reviews', component: () => import('@/views/ReviewsView.vue') },
      { path: 'statistics', component: () => import('@/views/StatisticsView.vue') },
      { path: 'notes', component: () => import('@/views/NotesView.vue') },
      { path: 'settings', component: () => import('@/views/SettingsView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async to => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) return '/login'
  if (!to.meta.public && auth.token && !auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      return '/login'
    }
  }
  if (to.meta.public && auth.token) return '/dashboard'
})

export default router
