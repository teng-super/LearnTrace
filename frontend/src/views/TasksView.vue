<template>
  <div class="grid">
    <section class="panel view-hero glass-panel">
      <div class="view-hero-main">
        <span class="status-pill primary">Execution Board</span>
        <h2>把目标拆到今天能推进的一小步</h2>
        <p>任务列表保持高密度，但每条任务都有周次、类型、优先级和完成状态，适合每天打卡复盘。</p>
      </div>
      <div class="hero-actions">
        <el-button :icon="Plus" type="primary" @click="openCreate">新增任务</el-button>
      </div>
    </section>

    <div class="grid grid-4">
      <MetricCard label="任务总数" :value="tasks.length" note="当前筛选结果" :icon="List" tone="primary" />
      <MetricCard label="待处理" :value="tasks.filter(t => t.status === 'TODO').length" note="还没有开始" :icon="Clock" tone="warning" />
      <MetricCard label="进行中" :value="tasks.filter(t => t.status === 'DOING').length" note="正在推进" :icon="TrendCharts" tone="blue" />
      <MetricCard label="已完成" :value="tasks.filter(t => t.status === 'DONE').length" note="已经打卡" :icon="CircleCheck" tone="green" />
    </div>

    <section class="panel panel-pad">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索任务" clearable style="width: 240px" @keyup.enter="load" />
          <el-select v-model="status" placeholder="状态" clearable style="width: 150px" @change="load">
            <el-option label="TODO" value="TODO" /><el-option label="DOING" value="DOING" /><el-option label="DONE" value="DONE" />
          </el-select>
          <el-input-number v-model="weekNo" :min="1" :max="8" placeholder="周" @change="load" />
        </div>
        <el-button :icon="Plus" type="primary" @click="openCreate">新增任务</el-button>
      </div>
      <el-table :data="tasks" style="width: 100%">
        <el-table-column label="任务" min-width="280">
          <template #default="{ row }">
            <strong>{{ row.title }}</strong>
            <div class="muted">{{ row.description || '无描述' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="weekNo" label="周" width="80" />
        <el-table-column prop="taskType" label="类型" width="140" />
        <el-table-column label="优先级" width="110">
          <template #default="{ row }"><span class="status-pill" :class="{ primary: row.priority === 'HIGH' }">{{ row.priority }}</span></template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止" width="130" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><span class="status-pill" :class="{ good: row.status === 'DONE', primary: row.status === 'DOING' }">{{ row.status }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="toggle(row)">{{ row.status === 'DONE' ? '取消完成' : '完成' }}</el-button>
            <el-button link @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="visible" title="任务编辑" width="640px">
      <el-form :model="form" label-position="top">
        <el-form-item label="任务标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <div class="grid grid-3">
          <el-form-item label="周次"><el-input-number v-model="form.weekNo" :min="1" :max="52" /></el-form-item>
          <el-form-item label="优先级"><el-select v-model="form.priority"><el-option label="LOW" value="LOW" /><el-option label="MEDIUM" value="MEDIUM" /><el-option label="HIGH" value="HIGH" /></el-select></el-form-item>
          <el-form-item label="状态"><el-select v-model="form.status"><el-option label="TODO" value="TODO" /><el-option label="DOING" value="DOING" /><el-option label="DONE" value="DONE" /></el-select></el-form-item>
        </div>
        <el-form-item label="截止日期"><el-date-picker v-model="form.dueDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Clock, List, Plus, TrendCharts } from '@element-plus/icons-vue'
import MetricCard from '@/components/MetricCard.vue'
import { api } from '@/api/client'

const tasks = ref<any[]>([])
const keyword = ref('')
const status = ref('')
const weekNo = ref<number>()
const visible = ref(false)
const form = reactive<any>({})

const load = async () => {
  const res = await api.get('/tasks', { params: { keyword: keyword.value, status: status.value, weekNo: weekNo.value, size: 100 } })
  tasks.value = res.records
}
const openCreate = () => {
  Object.assign(form, { id: null, title: '', description: '', taskType: 'CUSTOM', status: 'TODO', priority: 'MEDIUM', weekNo: 1, dueDate: '' })
  visible.value = true
}
const edit = (row: any) => {
  Object.assign(form, row)
  visible.value = true
}
const save = async () => {
  if (form.id) await api.put(`/tasks/${form.id}`, form)
  else await api.post('/tasks', form)
  ElMessage.success('任务已保存')
  visible.value = false
  load()
}
const toggle = async (row: any) => {
  await api.patch(`/tasks/${row.id}/done`, null, { params: { done: row.status !== 'DONE' } })
  load()
}
const remove = async (id: number) => {
  await api.delete(`/tasks/${id}`)
  load()
}
onMounted(load)
</script>
