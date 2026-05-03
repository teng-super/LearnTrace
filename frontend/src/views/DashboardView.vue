<template>
  <div class="grid" style="gap: 18px">
    <div class="grid grid-4">
      <MetricCard label="进行中目标" :value="overview.activeGoals || 0" note="主线推进中的目标" />
      <MetricCard label="今日待完成" :value="overview.todayTasks || 0" note="到期且未完成任务" />
      <MetricCard label="今日待复习" :value="overview.todayReviews || 0" note="需要回炉的错误" />
      <MetricCard label="本周新增错误" :value="overview.weekNewErrors || 0" note="最近 7 天记录" />
    </div>

    <div class="grid grid-3">
      <section class="panel panel-pad" style="grid-column: span 2">
        <h2 class="section-title">最近 7 天趋势</h2>
        <div ref="trendEl" style="height: 300px"></div>
      </section>
      <section class="panel panel-pad">
        <h2 class="section-title">当前主线目标</h2>
        <template v-if="overview.currentGoal">
          <h3>{{ overview.currentGoal.title }}</h3>
          <p class="muted">{{ overview.currentGoal.description }}</p>
          <el-progress :percentage="overview.currentGoal.progress || 0" :stroke-width="10" />
          <el-button type="primary" style="margin-top: 16px" @click="router.push(`/goals/${overview.currentGoal.id}`)">查看详情</el-button>
        </template>
        <el-empty v-else description="还没有进行中的目标" />
      </section>
    </div>

    <div class="grid grid-3">
      <section class="panel panel-pad">
        <h2 class="section-title">今天该做什么</h2>
        <el-timeline>
          <el-timeline-item v-for="task in overview.todayTaskItems || []" :key="task.id" :timestamp="task.dueDate || '未设截止'">
            <div>{{ task.title }}</div>
            <span class="status-pill">{{ task.priority }}</span>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!(overview.todayTaskItems || []).length" description="今天没有逾期任务" />
      </section>
      <section class="panel panel-pad">
        <h2 class="section-title">该复习的错误</h2>
        <div v-for="item in overview.reviewItems || []" :key="item.error.id" class="todo-row">
          <strong>{{ item.error.title }}</strong>
          <span class="muted">{{ item.error.errorType }} · {{ item.error.severity }}</span>
        </div>
        <el-empty v-if="!(overview.reviewItems || []).length" description="没有待复习错误" />
      </section>
      <section class="panel panel-pad">
        <h2 class="section-title">反复出现的标签</h2>
        <div ref="tagEl" style="height: 260px"></div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import MetricCard from '@/components/MetricCard.vue'
import { api } from '@/api/client'

const router = useRouter()
const overview = ref<any>({})
const trends = ref<any>({})
const tagStats = ref<any[]>([])
const trendEl = ref<HTMLDivElement>()
const tagEl = ref<HTMLDivElement>()

const load = async () => {
  overview.value = await api.get('/statistics/overview')
  trends.value = await api.get('/statistics/trends')
  tagStats.value = await api.get('/statistics/error-tags')
  await nextTick()
  renderCharts()
}

const renderCharts = () => {
  if (trendEl.value) {
    const chart = echarts.init(trendEl.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { textStyle: { color: 'var(--muted)' } },
      grid: { left: 32, right: 16, top: 40, bottom: 28 },
      xAxis: { type: 'category', data: trends.value.days || [], axisLabel: { color: 'var(--muted)' } },
      yAxis: { type: 'value', axisLabel: { color: 'var(--muted)' } },
      series: [
        { name: '完成任务', type: 'line', smooth: true, data: trends.value.doneTasks || [], color: '#7ee787' },
        { name: '新增错误', type: 'line', smooth: true, data: trends.value.newErrors || [], color: '#ff7b72' }
      ]
    })
  }
  if (tagEl.value) {
    const chart = echarts.init(tagEl.value)
    chart.setOption({
      tooltip: {},
      series: [{
        type: 'pie',
        radius: ['42%', '70%'],
        data: tagStats.value,
        label: { color: 'var(--text)' }
      }]
    })
  }
}

onMounted(load)
</script>

<style scoped>
.todo-row {
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  display: grid;
  gap: 6px;
}
.todo-row:last-child { border-bottom: 0; }
@media (max-width: 1080px) {
  section[style*="grid-column"] { grid-column: auto !important; }
}
</style>
