<template>
  <div class="grid">
    <section class="panel view-hero glass-panel">
      <div class="view-hero-main">
        <span class="status-pill danger">Error Vault</span>
        <h2>把踩坑记录成可复习、可统计、可面试复盘的资产</h2>
        <p>错误记录支持关键词、类型、标签、严重程度和状态筛选，后续会进入复习队列和薄弱点统计。</p>
      </div>
      <div class="hero-actions">
        <el-button :icon="Plus" type="primary" @click="router.push('/errors/new')">新增错误记录</el-button>
      </div>
    </section>

    <section class="panel panel-pad">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="filters.keyword" placeholder="关键词搜索" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="filters.errorType" placeholder="类型" clearable style="width: 150px" @change="load">
            <el-option v-for="type in errorTypes" :key="type" :label="type" :value="type" />
          </el-select>
          <el-select v-model="filters.severity" placeholder="严重程度" clearable style="width: 130px" @change="load">
            <el-option label="LOW" value="LOW" /><el-option label="MEDIUM" value="MEDIUM" /><el-option label="HIGH" value="HIGH" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 150px" @change="load">
            <el-option label="未解决" value="UNRESOLVED" /><el-option label="已解决" value="RESOLVED" /><el-option label="需复习" value="NEED_REVIEW" />
          </el-select>
        </div>
        <el-button :icon="Plus" type="primary" @click="router.push('/errors/new')">新增错误记录</el-button>
      </div>

      <el-table :data="rows" style="width: 100%">
        <el-table-column label="错误" min-width="300">
          <template #default="{ row }">
            <strong>{{ row.error.title }}</strong>
            <div class="muted">{{ row.error.summary }}</div>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="220">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag.id" :color="tag.color" effect="dark" style="margin: 2px">{{ tag.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="error.errorType" width="120" />
        <el-table-column label="严重" width="100">
          <template #default="{ row }"><span class="status-pill" :class="{ danger: row.error.severity === 'HIGH', primary: row.error.severity === 'MEDIUM' }">{{ row.error.severity }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><span class="status-pill" :class="{ good: row.error.status === 'RESOLVED', danger: row.error.status === 'UNRESOLVED' }">{{ row.error.status }}</span></template>
        </el-table-column>
        <el-table-column label="下次复习" prop="error.nextReviewAt" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/errors/${row.error.id}/edit`)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.error.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { api } from '@/api/client'

const router = useRouter()
const rows = ref<any[]>([])
const filters = reactive<any>({})
const errorTypes = ['CPP', 'LINUX', 'NETWORK', 'FFMPEG', 'DATABASE', 'OS', 'COMPUTER_ORGANIZATION', 'FRONTEND', 'BACKEND', 'OTHER']

const load = async () => {
  const res = await api.get('/errors', { params: { ...filters, size: 100 } })
  rows.value = res.records
}
const remove = async (id: number) => {
  await api.delete(`/errors/${id}`)
  load()
}
onMounted(load)
</script>
