<template>
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
      <el-button type="primary" @click="router.push('/errors/new')">新增错误记录</el-button>
    </div>

    <el-table :data="rows" style="width: 100%">
      <el-table-column label="错误" min-width="260">
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
      <el-table-column label="严重" prop="error.severity" width="100" />
      <el-table-column label="状态" prop="error.status" width="120" />
      <el-table-column label="下次复习" prop="error.nextReviewAt" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/errors/${row.error.id}/edit`)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.error.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
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
