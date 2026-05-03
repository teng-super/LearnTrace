<template>
  <div v-if="detail.goal" class="grid">
    <section class="panel panel-pad">
      <div class="toolbar">
        <div>
          <h2 style="margin: 0">{{ detail.goal.title }}</h2>
          <p class="muted">{{ detail.goal.description }}</p>
        </div>
        <el-select v-model="detail.goal.status" style="width: 160px" @change="changeStatus">
          <el-option label="未开始" value="NOT_STARTED" /><el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" /><el-option label="已暂停" value="PAUSED" />
        </el-select>
      </div>
      <el-progress :percentage="detail.goal.progress || 0" :stroke-width="12" />
    </section>

    <section class="panel panel-pad">
      <h2 class="section-title">阶段计划</h2>
      <el-steps :active="activeStage" finish-status="success" align-center>
        <el-step v-for="stage in detail.stages" :key="stage.id" :title="stage.title" :description="stage.deadline" />
      </el-steps>
    </section>

    <div class="grid grid-2">
      <section class="panel panel-pad">
        <h2 class="section-title">关联任务</h2>
        <el-table :data="detail.tasks" max-height="360">
          <el-table-column prop="title" label="任务" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column width="80">
            <template #default="{ row }">
              <el-button link type="primary" @click="toggle(row)">{{ row.status === 'DONE' ? '取消' : '完成' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
      <section class="panel panel-pad">
        <h2 class="section-title">关联错误</h2>
        <div v-for="item in detail.errors" :key="item.id" class="line-item">
          <strong>{{ item.title }}</strong><span class="muted">{{ item.errorType }} · {{ item.status }}</span>
        </div>
      </section>
    </div>

    <section class="panel panel-pad">
      <h2 class="section-title">阶段复盘和笔记</h2>
      <div v-for="note in detail.notes" :key="note.id" class="line-item">
        <strong>{{ note.title }}</strong><span class="muted">{{ note.noteType }}</span>
      </div>
      <el-button type="primary" @click="router.push('/notes')">去写复盘笔记</el-button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api/client'

const route = useRoute()
const router = useRouter()
const detail = ref<any>({})
const id = computed(() => route.params.id)
const activeStage = computed(() => (detail.value.stages || []).findIndex((s: any) => s.status !== 'COMPLETED') + 1)

const load = async () => {
  detail.value = await api.get(`/goals/${id.value}`)
}
const changeStatus = async () => {
  await api.patch(`/goals/${id.value}/status`, { status: detail.value.goal.status })
}
const toggle = async (row: any) => {
  await api.patch(`/tasks/${row.id}/done`, null, { params: { done: row.status !== 'DONE' } })
  load()
}
onMounted(load)
</script>

<style scoped>
.line-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}
.line-item:last-child { border-bottom: 0; }
</style>
