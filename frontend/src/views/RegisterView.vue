<template>
  <div class="auth-shell">
    <div class="auth-card panel">
      <section class="auth-form">
        <h2>创建 LearnTrace 账号</h2>
        <el-form :model="form" label-position="top">
          <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="submit">注册并进入</el-button>
        </el-form>
        <p class="muted">已经有账号？<RouterLink class="accent" to="/login">返回登录</RouterLink></p>
      </section>
      <section class="auth-hero">
        <div class="brand-mark">LT</div>
        <h1>从今天开始，把每个坑都记成作品集的一部分。</h1>
        <p>目标可以从模板生成，也可以按你的计划自由改。错误记录支持 Markdown、代码块、标签和复习提醒。</p>
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
const form = reactive({ username: '', email: '', nickname: '', password: '' })

const submit = async () => {
  loading.value = true
  try {
    await auth.register(form)
    router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-shell { display: grid; place-items: center; padding: 28px; }
.auth-card {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: .9fr 1.1fr;
  overflow: hidden;
}
.auth-form { padding: 46px; background: var(--surface); }
.auth-form h2 { margin: 0 0 28px; font-size: 28px; }
.auth-hero { padding: 46px; background: var(--surface-2); }
.auth-hero h1 { margin: 28px 0 18px; font-size: 38px; line-height: 1.12; }
.auth-hero p { color: var(--muted); }
@media (max-width: 760px) { .auth-card { grid-template-columns: 1fr; } }
</style>
