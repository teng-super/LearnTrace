<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">LT</div>
        <div>
          <div class="brand-title">LearnTrace</div>
          <div class="brand-subtitle">目标 · 错误 · 复习 · 笔记</div>
        </div>
      </div>
      <div class="sidebar-section">学习闭环</div>
      <nav class="nav">
        <RouterLink v-for="item in nav" :key="item.to" class="nav-link" :to="item.to">
          <component :is="item.icon" style="width: 18px" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-status">
        <div class="status-line">
          <span>本地服务</span>
          <span class="live-dot"></span>
        </div>
        <div class="status-line" style="margin-top: 10px">
          <span>MySQL · Spring · Vite</span>
          <span>在线</span>
        </div>
      </div>
    </aside>
    <main class="main">
      <header class="topbar">
        <div>
          <div class="page-kicker">
            <span class="live-dot"></span>
            <span>{{ kicker }}</span>
          </div>
          <h1 class="page-title">{{ title }}</h1>
          <p class="page-subtitle">{{ subtitle }}</p>
        </div>
        <div class="toolbar-right">
          <div class="command-pill code">D:\LearnTrace</div>
          <ThemeSwitcher />
          <el-dropdown>
            <el-button>
              {{ auth.user?.nickname || auth.user?.username }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/settings')">个人设置</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Aim,
  Calendar,
  Collection,
  DataAnalysis,
  EditPen,
  Finished,
  HomeFilled,
  List,
  Setting,
  Warning,
  ArrowDown
} from '@element-plus/icons-vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const nav = [
  { to: '/dashboard', label: '驾驶舱', icon: HomeFilled },
  { to: '/plan', label: '八周计划', icon: Calendar },
  { to: '/goals', label: '学习目标', icon: Aim },
  { to: '/tasks', label: '学习任务', icon: List },
  { to: '/errors', label: '错误记录', icon: Warning },
  { to: '/reviews', label: '今日复习', icon: Finished },
  { to: '/statistics', label: '数据统计', icon: DataAnalysis },
  { to: '/notes', label: '学习笔记', icon: Collection },
  { to: '/settings', label: '个人设置', icon: Setting }
]

const copy: Record<string, [string, string, string]> = {
  '/dashboard': ['Mission Control', '学习驾驶舱', '今天要做什么、哪些错误该复习、哪里正在反复薄弱。'],
  '/plan': ['Eight Week Route', '八周作战图', '从原 HTML 迁移来的主线计划，现在可以导入、拆分和打卡。'],
  '/goals': ['Goal System', '学习目标', '从模板生成，也可以完全自定义阶段、任务和截止时间。'],
  '/tasks': ['Task Board', '学习任务', '把目标拆成今天能完成的一小步。'],
  '/errors': ['Error Vault', '错误记录', '用 Markdown、代码块、标签和间隔复习把坑变成资产。'],
  '/reviews': ['Review Queue', '今日复习', '按掌握程度自动推迟下次复习时间。'],
  '/statistics': ['Data Lens', '数据统计', '用任务趋势、错误类型和标签分布定位薄弱点。'],
  '/notes': ['Knowledge Base', '学习笔记', '导入 Markdown、PDF、Word，沉淀成可导出的复盘材料。'],
  '/settings': ['Workspace', '个人设置', '账号、主题和学习工作台偏好。']
}

const currentCopy = computed(() => {
  if (copy[route.path]) return copy[route.path]
  if (route.path.startsWith('/goals/')) return copy['/goals']
  if (route.path.startsWith('/errors/')) return copy['/errors']
  return ['LearnTrace', 'LearnTrace', '学习闭环系统']
})
const kicker = computed(() => currentCopy.value[0])
const title = computed(() => currentCopy.value[1])
const subtitle = computed(() => currentCopy.value[2])

const logout = () => {
  auth.logout()
  router.push('/login')
}
</script>
