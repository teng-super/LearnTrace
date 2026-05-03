<template>
  <section class="panel panel-pad">
    <div class="toolbar">
      <h2 style="margin: 0">今日待复习错误</h2>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table :data="items" style="width: 100%">
      <el-table-column label="错误" min-width="260">
        <template #default="{ row }">
          <strong>{{ row.error.title }}</strong>
          <div class="muted">{{ row.error.summary }}</div>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="220">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag.id" style="margin: 2px">{{ tag.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="复习操作" width="320">
        <template #default="{ row }">
          <el-button type="success" @click="review(row.error.id, 'MASTERED')">已掌握</el-button>
          <el-button @click="review(row.error.id, 'UNCERTAIN')">还不熟</el-button>
          <el-button type="danger" @click="review(row.error.id, 'FORGOT')">仍不会</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!items.length" description="今天没有待复习错误" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api/client'

const items = ref<any[]>([])
const load = async () => {
  items.value = await api.get('/reviews/today')
}
const review = async (id: number, result: string) => {
  await api.post(`/reviews/${id}`, { result })
  ElMessage.success('复习结果已记录')
  load()
}
onMounted(load)
</script>
