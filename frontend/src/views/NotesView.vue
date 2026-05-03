<template>
  <div class="grid grid-2 notes-layout">
    <section class="panel panel-pad">
      <div class="toolbar">
        <h2 style="margin: 0">笔记库</h2>
        <el-upload :auto-upload="true" :show-file-list="false" :http-request="importFile">
          <el-button type="primary">导入 PDF / Markdown / Word</el-button>
        </el-upload>
      </div>
      <el-input v-model="keyword" placeholder="搜索笔记" clearable @keyup.enter="load" style="margin-bottom: 14px" />
      <div v-for="note in notes" :key="note.id" class="note-item" :class="{ active: active?.id === note.id }" @click="select(note)">
        <strong>{{ note.title }}</strong>
        <span class="status-pill">{{ note.noteType }}</span>
        <p class="muted">{{ note.updatedAt }}</p>
      </div>
      <el-button style="width: 100%; margin-top: 12px" @click="newNote">新建 Markdown 笔记</el-button>
    </section>

    <section class="panel panel-pad">
      <template v-if="active">
        <div class="toolbar">
          <el-input v-model="active.title" style="max-width: 360px" />
          <div class="toolbar-right">
            <el-button v-if="active.sourceFileId && active.noteType === 'WORD'" @click="openWord">原格式编辑</el-button>
            <el-button v-if="active.sourceFileId && active.noteType === 'PDF'" @click="showPdf = !showPdf">PDF 预览/批注</el-button>
            <el-button @click="exportMarkdown">导出 MD</el-button>
            <el-button @click="exportPdf">导出 PDF</el-button>
            <el-button type="primary" @click="save">保存</el-button>
          </div>
        </div>
        <MdEditor v-model="active.contentMarkdown" language="zh-CN" style="height: 520px" />
        <div v-if="showPdf && active.sourceFileId" class="pdf-pane">
          <iframe :src="`/api/files/public/${active.sourceFileId}`"></iframe>
          <div class="panel-pad">
            <h3>PDF 批注 / 摘录</h3>
            <el-input v-model="annotation.selectedText" type="textarea" :rows="4" placeholder="摘录或粘贴 PDF 中的关键文字" />
            <el-input v-model="annotation.note" type="textarea" :rows="3" placeholder="批注" style="margin-top: 10px" />
            <el-button style="margin-top: 10px" @click="saveAnnotation">保存批注</el-button>
            <el-button style="margin-top: 10px" type="primary" @click="excerpt">摘录成新笔记</el-button>
          </div>
        </div>
        <div v-if="wordVisible" id="onlyoffice-editor" class="word-editor"></div>
      </template>
      <el-empty v-else description="选择或导入一篇笔记" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import { api, rawApi } from '@/api/client'

const notes = ref<any[]>([])
const active = ref<any>()
const keyword = ref('')
const showPdf = ref(false)
const wordVisible = ref(false)
const annotation = reactive({ pageNo: 1, selectedText: '', note: '', annotationType: 'HIGHLIGHT' })

const load = async () => {
  const res = await api.get('/notes', { params: { keyword: keyword.value, size: 100 } })
  notes.value = res.records
  if (!active.value && notes.value.length) active.value = { ...notes.value[0] }
}
const select = (note: any) => {
  active.value = { ...note }
  showPdf.value = false
  wordVisible.value = false
}
const newNote = () => {
  active.value = { title: '新的学习笔记', contentMarkdown: '# 新的学习笔记\n\n', noteType: 'MARKDOWN' }
}
const save = async () => {
  if (active.value.id) active.value = await api.put(`/notes/${active.value.id}`, active.value)
  else active.value = await api.post('/notes', active.value)
  ElMessage.success('笔记已保存')
  load()
}
const importFile = async (option: any) => {
  const data = new FormData()
  data.append('file', option.file)
  const note = await rawApi.post('/notes/import', data, { headers: { 'Content-Type': 'multipart/form-data' } })
  active.value = note
  ElMessage.success('笔记已导入')
  load()
}
const download = async (url: string, filename: string) => {
  const blob = await rawApi.get(url, { responseType: 'blob' }) as unknown as Blob
  const href = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = href
  a.download = filename
  a.click()
  URL.revokeObjectURL(href)
}
const exportMarkdown = () => download(`/notes/${active.value.id}/export/markdown`, `${active.value.title}.md`)
const exportPdf = () => download(`/notes/${active.value.id}/export/pdf`, `${active.value.title}.pdf`)
const saveAnnotation = async () => {
  await api.post(`/pdfs/${active.value.sourceFileId}/annotations`, annotation)
  ElMessage.success('PDF 批注已保存')
}
const excerpt = async () => {
  await api.post(`/pdfs/${active.value.sourceFileId}/excerpt`, { title: `摘录：${active.value.title}`, selectedText: annotation.selectedText })
  ElMessage.success('摘录已保存为新笔记')
  load()
}
const openWord = async () => {
  const data = await api.get(`/documents/${active.value.sourceFileId}/editor-config`)
  wordVisible.value = true
  const scriptId = 'onlyoffice-api-script'
  if (!document.getElementById(scriptId)) {
    const script = document.createElement('script')
    script.id = scriptId
    script.src = `${data.documentServerUrl}/web-apps/apps/api/documents/api.js`
    document.body.appendChild(script)
    await new Promise(resolve => { script.onload = resolve })
  }
  document.getElementById('onlyoffice-editor')!.innerHTML = ''
  if ((window as any).DocsAPI) new (window as any).DocsAPI.DocEditor('onlyoffice-editor', data.config)
  else ElMessage.warning('ONLYOFFICE 服务未就绪')
}
onMounted(load)
</script>

<style scoped>
.notes-layout { align-items: start; }
.note-item {
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  background: var(--surface-2);
}
.note-item.active {
  border-color: var(--primary);
}
.pdf-pane {
  display: grid;
  grid-template-columns: 1.3fr .7fr;
  gap: 14px;
  margin-top: 14px;
}
.pdf-pane iframe {
  width: 100%;
  min-height: 560px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: white;
}
.word-editor {
  height: 620px;
  margin-top: 14px;
  border: 1px solid var(--border);
}
@media (max-width: 1080px) {
  .pdf-pane { grid-template-columns: 1fr; }
}
</style>
