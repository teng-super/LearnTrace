<template>
  <div class="grid">
    <section class="panel panel-pad glass-panel plan-hero">
      <div>
        <span class="status-pill primary">study_plan.html 已迁移为数据库模板</span>
        <h2>八周系统进阶作战图</h2>
        <p class="muted">Linux、C++ 编译链、并发、网络、FFmpeg、学校三科，现在不再是本地勾选，而是可导入、可拆分、可统计的目标计划。</p>
      </div>
      <el-button :icon="Download" type="primary" size="large" @click="importPlan">一键导入默认八周计划</el-button>
    </section>
    <section class="panel panel-pad">
      <div class="week-tabs">
        <button v-for="week in 8" :key="week" :class="{ active: activeWeek === week }" @click="activeWeek = week">W{{ week }}</button>
      </div>
      <el-table :data="weekTasks" style="width: 100%">
        <el-table-column prop="sectionTitle" label="模块" width="160" />
        <el-table-column prop="title" label="任务" />
        <el-table-column prop="taskType" label="类型" width="130" />
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { api } from '@/api/client'

const templates = ref<any[]>([])
const tasks = ref<any[]>([])
const activeWeek = ref(1)
const weekTasks = computed(() => tasks.value.filter(t => t.weekNo === activeWeek.value))

const load = async () => {
  templates.value = await api.get('/plan-templates')
}

const importPlan = async () => {
  try {
    await api.post('/tasks/import-default-plan')
    ElMessage.success('八周计划已导入到你的目标和任务中')
  } catch (e: any) {
    ElMessage.warning(e.message || '导入失败')
  }
}

onMounted(async () => {
  await load()
  const plan = templates.value[0]
  if (plan) {
    tasks.value = await api.get(`/plan-templates/${plan.id}/tasks`)
  }
})
</script>

<style scoped>
.plan-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  min-height: 210px;
}
.plan-hero h2 {
  margin: 12px 0 8px;
  font-size: 32px;
}
.week-tabs {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}
.week-tabs button {
  border: 1px solid var(--border);
  background: var(--surface-2);
  color: var(--muted);
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
}
.week-tabs button.active {
  background: var(--primary);
  color: color-mix(in srgb, var(--bg), #000 25%);
  font-weight: 800;
}
@media (max-width: 760px) {
  .plan-hero { align-items: stretch; flex-direction: column; }
  .week-tabs { grid-template-columns: repeat(4, 1fr); }
}
</style>
