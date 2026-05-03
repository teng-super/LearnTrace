<template>
  <div class="grid grid-2">
    <section class="panel panel-pad">
      <h2 class="section-title">账号资料</h2>
      <el-form :model="profile" label-position="top">
        <el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item>
        <el-form-item label="头像 URL"><el-input v-model="profile.avatarUrl" /></el-form-item>
        <el-button type="primary" @click="saveProfile">保存资料</el-button>
      </el-form>
    </section>
    <section class="panel panel-pad">
      <h2 class="section-title">主题样式</h2>
      <div class="theme-grid">
        <button v-for="item in themes" :key="item.name" :class="{ active: theme.current === item.name }" @click="theme.setTheme(item.name)">
          <strong>{{ item.label }}</strong>
          <span>{{ item.hint }}</span>
        </button>
      </div>
    </section>
    <section class="panel panel-pad">
      <h2 class="section-title">修改密码</h2>
      <el-form :model="password" label-position="top">
        <el-form-item label="旧密码"><el-input v-model="password.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="password.newPassword" type="password" show-password /></el-form-item>
        <el-button @click="savePassword">更新密码</el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { themes, useThemeStore } from '@/stores/theme'

const auth = useAuthStore()
const theme = useThemeStore()
const profile = reactive<any>({})
const password = reactive({ oldPassword: '', newPassword: '' })

watchEffect(() => Object.assign(profile, auth.user || {}))

const saveProfile = async () => {
  const user = await api.put('/users/me', { ...profile, theme: theme.current })
  auth.user = user
  ElMessage.success('资料已保存')
}
const savePassword = async () => {
  await api.put('/users/me/password', password)
  ElMessage.success('密码已更新')
}
</script>

<style scoped>
.theme-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.theme-grid button {
  border: 1px solid var(--border);
  background: var(--surface-2);
  color: var(--text);
  border-radius: 8px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  display: grid;
  gap: 6px;
}
.theme-grid button.active {
  border-color: var(--primary);
  box-shadow: inset 0 0 0 2px var(--primary);
}
.theme-grid span { color: var(--muted); }
</style>
