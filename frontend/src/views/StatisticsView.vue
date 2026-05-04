<template>
  <div class="grid">
    <section class="panel view-hero glass-panel">
      <div class="view-hero-main">
        <span class="status-pill primary">Data Lens</span>
        <h2>用数据定位学习阻塞点</h2>
        <p>目标完成率、错误类型、标签高频项和最近趋势会告诉你下一轮计划该收紧哪里。</p>
      </div>
    </section>
    <div class="grid grid-4">
      <MetricCard label="总目标数" :value="overview.totalGoals || 0" :icon="Aim" tone="primary" />
      <MetricCard label="总任务数" :value="overview.totalTasks || 0" :icon="List" tone="blue" />
      <MetricCard label="总错误数" :value="overview.totalErrors || 0" :icon="Warning" tone="danger" />
      <MetricCard label="已解决错误" :value="overview.resolvedErrors || 0" :icon="CircleCheck" tone="green" />
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
import { Aim, CircleCheck, List, Warning } from '@element-plus/icons-vue'
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
  const style = getComputedStyle(document.documentElement)
  const text = style.getPropertyValue('--muted').trim()
  const border = style.getPropertyValue('--border').trim()
  const primary = style.getPropertyValue('--primary').trim()
  const blue = style.getPropertyValue('--primary-2').trim()
  const danger = style.getPropertyValue('--danger').trim()
  const good = style.getPropertyValue('--good').trim()
  if (typeEl.value) echarts.init(typeEl.value).setOption({ tooltip: {}, grid: { left: 36, right: 14, top: 20, bottom: 58 }, xAxis: { type: 'category', data: typeStats.value.map(i => i.name), axisLabel: { color: text, rotate: 30 }, axisLine: { lineStyle: { color: border } } }, yAxis: { type: 'value', splitLine: { lineStyle: { color: border } }, axisLabel: { color: text } }, series: [{ type: 'bar', data: typeStats.value.map(i => i.value), color: blue, barWidth: 18, itemStyle: { borderRadius: [8, 8, 0, 0] } }] })
  if (tagEl.value) echarts.init(tagEl.value).setOption({ tooltip: {}, color: [primary, blue, good, danger], series: [{ type: 'pie', radius: ['42%', '72%'], data: tagStats.value, label: { color: text } }] })
  if (trendEl.value) echarts.init(trendEl.value).setOption({ tooltip: { trigger: 'axis' }, legend: { textStyle: { color: text } }, grid: { left: 36, right: 16, top: 44, bottom: 30 }, xAxis: { type: 'category', data: trends.value.days || [], axisLabel: { color: text }, axisLine: { lineStyle: { color: border } } }, yAxis: { type: 'value', splitLine: { lineStyle: { color: border } }, axisLabel: { color: text } }, series: [{ name: '完成任务', type: 'line', smooth: true, data: trends.value.doneTasks || [], color: good }, { name: '新增错误', type: 'line', smooth: true, data: trends.value.newErrors || [], color: danger }] })
}
onMounted(load)
</script>
