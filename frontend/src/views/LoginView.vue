<template>
  <div class="auth-shell">
    <div class="auth-card panel">
      <section class="auth-hero">
        <div class="brand-mark">LT</div>
        <h1>LearnTrace</h1>
        <p>把音视频求职计划、错误记录、间隔复习和笔记沉淀连成一个真正可执行的闭环。</p>
        <div class="auth-strip">
          <span>客户端播放器 / SDK</span>
          <span>流媒体服务端</span>
          <span>RTC 扩展</span>
        </div>
      </section>
      <section class="auth-form">
        <h2>登录</h2>
        <el-form :model="form" label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名或邮箱">
            <el-input v-model="form.usernameOrEmail" placeholder="demo" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="learntrace123" />
          </el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="submit">进入学习驾驶舱</el-button>
        </el-form>
        <p class="muted">没有账号？<RouterLink class="accent" to="/register">创建一个</RouterLink></p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ usernameOrEmail: 'demo', password: 'learntrace123' })

const submit = async () => {
  loading.value = true
  try {
    await auth.login(form.usernameOrEmail, form.password)
    router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-shell {
  display: grid;
  place-items: center;
  padding: 28px;
}
.auth-card {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1.15fr .85fr;
  overflow: hidden;
}
.auth-hero {
  padding: 46px;
  background: var(--surface-2);
}
.auth-hero h1 {
  font-size: 54px;
  line-height: 1;
  margin: 28px 0 18px;
}
.auth-hero p {
  color: var(--muted);
  max-width: 54ch;
}
.auth-strip {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 34px;
}
.auth-strip span {
  border: 1px solid var(--border);
  padding: 8px 10px;
  border-radius: 8px;
  color: var(--muted);
}
.auth-form {
  padding: 46px;
  background: var(--surface);
}
.auth-form h2 {
  margin: 0 0 28px;
  font-size: 28px;
}
@media (max-width: 760px) {
  .auth-card { grid-template-columns: 1fr; }
  .auth-hero h1 { font-size: 38px; }
}
</style>
