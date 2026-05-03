<template>
  <div class="grid">
    <section class="panel panel-pad">
      <div class="toolbar">
        <h2 style="margin: 0">{{ id ? '编辑错误记录' : '新增错误记录' }}</h2>
        <el-button type="primary" @click="save">保存错误</el-button>
      </div>
      <el-form :model="form" label-position="top">
        <el-form-item label="错误标题"><el-input v-model="form.title" /></el-form-item>
        <div class="grid grid-3">
          <el-form-item label="类型"><el-select v-model="form.errorType"><el-option v-for="type in errorTypes" :key="type" :label="type" :value="type" /></el-select></el-form-item>
          <el-form-item label="严重程度"><el-select v-model="form.severity"><el-option label="LOW" value="LOW" /><el-option label="MEDIUM" value="MEDIUM" /><el-option label="HIGH" value="HIGH" /></el-select></el-form-item>
          <el-form-item label="状态"><el-select v-model="form.status"><el-option label="UNRESOLVED" value="UNRESOLVED" /><el-option label="RESOLVED" value="RESOLVED" /><el-option label="NEED_REVIEW" value="NEED_REVIEW" /></el-select></el-form-item>
        </div>
        <el-form-item label="标签">
          <el-select v-model="form.tagNames" multiple filterable allow-create default-first-option placeholder="输入或选择标签">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="错误描述（Markdown）">
          <MdEditor v-model="form.description" language="zh-CN" :toolbars-exclude="['github']" @on-upload-img="uploadImage" />
        </el-form-item>
      </el-form>
    </section>

    <div class="grid grid-2">
      <section class="panel panel-pad">
        <h2 class="section-title">错误代码</h2>
        <el-input v-model="form.wrongCode" type="textarea" :rows="12" class="code" />
      </section>
      <section class="panel panel-pad">
        <h2 class="section-title">修正代码</h2>
        <el-input v-model="form.correctCode" type="textarea" :rows="12" class="code" />
      </section>
    </div>

    <section class="panel panel-pad">
      <div class="grid grid-3">
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="解决方案"><el-input v-model="form.solution" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="一句话总结"><el-input v-model="form.summary" type="textarea" :rows="4" /></el-form-item>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import { api, rawApi } from '@/api/client'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string | undefined
const tags = ref<any[]>([])
const errorTypes = ['CPP', 'LINUX', 'NETWORK', 'FFMPEG', 'DATABASE', 'OS', 'COMPUTER_ORGANIZATION', 'FRONTEND', 'BACKEND', 'OTHER']
const form = reactive<any>({
  title: '',
  errorType: 'CPP',
  severity: 'MEDIUM',
  status: 'UNRESOLVED',
  description: '',
  wrongCode: '',
  correctCode: '',
  reason: '',
  solution: '',
  summary: '',
  tagNames: []
})

const load = async () => {
  tags.value = await api.get('/tags')
  if (id) {
    const data = await api.get(`/errors/${id}`)
    Object.assign(form, data.error)
    form.tagNames = (data.tags || []).map((t: any) => t.name)
  }
}
const save = async () => {
  if (id) await api.put(`/errors/${id}`, form)
  else await api.post('/errors', form)
  ElMessage.success('错误记录已保存')
  router.push('/errors')
}
const uploadImage = async (files: File[], callback: (urls: string[]) => void) => {
  const urls: string[] = []
  for (const file of files) {
    const data = new FormData()
    data.append('file', file)
    const res: any = await rawApi.post('/files/upload', data, { headers: { 'Content-Type': 'multipart/form-data' } })
    urls.push(`/api/files/public/${res.id}`)
  }
  callback(urls)
}
onMounted(load)
</script>
