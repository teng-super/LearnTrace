<template>
  <div class="grid">
    <div class="grid grid-4">
      <MetricCard label="总目标数" :value="overview.totalGoals || 0" />
      <MetricCard label="总任务数" :value="overview.totalTasks || 0" />
      <MetricCard label="总错误数" :value="overview.totalErrors || 0" />
      <MetricCard label="已解决错误" :value="overview.resolvedErrors || 0" />
    </div>
    <div class="grid grid-2">
      <section class="panel panel-pad"><h2 class="section-title">错误类型分布</h2><div ref="typeEl" style="height: 340px"></div></section>
      <section class="panel panel-pad"><h2 class="section-title">错误标签统计</h2><div ref="tagEl" style="height: 340px"></div></section>
    </div>
    <section class="panel panel-pad"><h2 class="section-title">最近 7 天趋势</h2><div ref="trendEl" style="height: 320px"></div></section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import MetricCard from '@/components/MetricCard.vue'
import { api } from '@/api/client'

const overview = ref<any>({})
const typeStats = ref<any[]>([])
const tagStats = ref<any[]>([])
const trends = ref<any>({})
const typeEl = ref<HTMLDivElement>()
const tagEl = ref<HTMLDivElement>()
const trendEl = ref<HTMLDivElement>()

const load = async () => {
  overview.value = await api.get('/statistics/overview')
  typeStats.value = await api.get('/statistics/error-types')
  tagStats.value = await api.get('/statistics/error-tags')
  trends.value = await api.get('/statistics/trends')
  await nextTick()
  draw()
}
const draw = () => {
  if (typeEl.value) echarts.init(typeEl.value).setOption({ tooltip: {}, xAxis: { type: 'category', data: typeStats.value.map(i => i.name) }, yAxis: { type: 'value' }, series: [{ type: 'bar', data: typeStats.value.map(i => i.value), color: '#77b7ff' }] })
  if (tagEl.value) echarts.init(tagEl.value).setOption({ tooltip: {}, series: [{ type: 'pie', radius: '70%', data: tagStats.value }] })
  if (trendEl.value) echarts.init(trendEl.value).setOption({ tooltip: { trigger: 'axis' }, legend: {}, xAxis: { type: 'category', data: trends.value.days || [] }, yAxis: { type: 'value' }, series: [{ name: '完成任务', type: 'line', data: trends.value.doneTasks || [] }, { name: '新增错误', type: 'line', data: trends.value.newErrors || [] }] })
}
onMounted(load)
</script>
