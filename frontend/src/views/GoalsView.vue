<template>
  <div class="grid">
    <section class="panel panel-pad">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索目标" clearable style="width: 240px" @keyup.enter="load" />
          <el-select v-model="status" placeholder="状态" clearable style="width: 160px" @change="load">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已暂停" value="PAUSED" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button @click="openTemplate">从模板生成</el-button>
          <el-button type="primary" @click="openCreate">新建目标</el-button>
        </div>
      </div>
      <el-table :data="goals" style="width: 100%">
        <el-table-column prop="title" label="目标" min-width="240" />
        <el-table-column prop="category" label="分类" width="150" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="进度" width="180">
          <template #default="{ row }"><el-progress :percentage="row.progress || 0" /></template>
        </el-table-column>
        <el-table-column label="截止" prop="deadline" width="130" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/goals/${row.id}`)">详情</el-button>
            <el-button link @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="editorVisible" :title="form.id ? '编辑目标' : '新建目标'" width="760px">
      <el-form :model="form" label-position="top">
        <el-form-item label="目标名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="目标描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <div class="grid grid-3">
          <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="form.priority">
              <el-option label="LOW" value="LOW" /><el-option label="MEDIUM" value="MEDIUM" /><el-option label="HIGH" value="HIGH" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="未开始" value="NOT_STARTED" /><el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" /><el-option label="已暂停" value="PAUSED" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-2">
          <el-form-item label="开始时间"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          <el-form-item label="截止时间"><el-date-picker v-model="form.deadline" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        </div>
        <h3>阶段计划</h3>
        <div v-for="(stage, index) in form.stages" :key="index" class="stage-edit">
          <el-input v-model="stage.title" placeholder="阶段标题" />
          <el-input v-model="stage.description" placeholder="阶段描述" />
          <el-button @click="form.stages.splice(index, 1)">移除</el-button>
        </div>
        <el-button @click="form.stages.push({ title: '', description: '', status: 'NOT_STARTED', stageOrder: form.stages.length + 1 })">添加阶段</el-button>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateVisible" title="选择目标模板" width="860px">
      <div class="grid grid-3">
        <div v-for="tpl in templates" :key="tpl.id" class="panel panel-pad template-card">
          <span class="status-pill">{{ tpl.category }}</span>
          <h3>{{ tpl.title }}</h3>
          <p class="muted">{{ tpl.description }}</p>
          <el-button type="primary" @click="importTemplate(tpl.id)">导入并开始定制</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api/client'

const router = useRouter()
const goals = ref<any[]>([])
const templates = ref<any[]>([])
const keyword = ref('')
const status = ref('')
const editorVisible = ref(false)
const templateVisible = ref(false)
const form = reactive<any>({ stages: [] })

const load = async () => {
  const res = await api.get('/goals', { params: { keyword: keyword.value, status: status.value, size: 100 } })
  goals.value = res.records
}

const openCreate = () => {
  Object.assign(form, { id: null, title: '', description: '', category: '', priority: 'HIGH', status: 'NOT_STARTED', startDate: '', deadline: '', stages: [] })
  editorVisible.value = true
}

const openEdit = async (row: any) => {
  const detail = await api.get(`/goals/${row.id}`)
  Object.assign(form, detail.goal, { stages: detail.stages || [] })
  editorVisible.value = true
}

const save = async () => {
  if (form.id) await api.put(`/goals/${form.id}`, form)
  else await api.post('/goals', form)
  ElMessage.success('目标已保存')
  editorVisible.value = false
  load()
}

const remove = async (id: number) => {
  await ElMessageBox.confirm('删除目标会保留已关联错误和笔记的历史引用，确认删除？', '确认删除')
  await api.delete(`/goals/${id}`)
  load()
}

const openTemplate = async () => {
  templates.value = await api.get('/goal-templates')
  templateVisible.value = true
}

const importTemplate = async (templateId: number) => {
  const goal = await api.post('/goals/import-template', { templateId, startDate: new Date().toISOString().slice(0, 10) })
  ElMessage.success('模板已导入，可以继续编辑')
  templateVisible.value = false
  router.push(`/goals/${goal.id}`)
}

onMounted(load)
</script>

<style scoped>
.stage-edit {
  display: grid;
  grid-template-columns: 1fr 1.3fr auto;
  gap: 10px;
  margin-bottom: 10px;
}
.template-card {
  display: grid;
  gap: 10px;
  align-content: start;
  min-height: 260px;
}
</style>
