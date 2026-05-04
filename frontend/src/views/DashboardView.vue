<template>
  <div class="dashboard-grid">
    <section class="panel panel-pad glass-panel mission-hero">
      <div class="mission-copy">
        <span class="status-pill primary">Learning Loop</span>
        <h2>{{ greeting }}，今天从最关键的一步开始</h2>
        <p>目标、任务、错误、复习和笔记在这里汇成一条线。先看今日队列，再处理高频薄弱点。</p>
        <div class="mission-actions">
          <el-button :icon="Aim" type="primary" @click="router.push('/goals')">调整目标</el-button>
          <el-button :icon="Warning" @click="router.push('/errors/new')">记录错误</el-button>
          <el-button :icon="Collection" @click="router.push('/notes')">沉淀笔记</el-button>
        </div>
      </div>
      <div class="mission-radar">
        <div class="radar-ring">
          <strong>{{ completionRate }}%</strong>
          <span>任务完成率</span>
        </div>
        <div class="radar-meta">
          <span>{{ todayText }}</span>
          <span>{{ overview.totalTasks || 0 }} 个任务 · {{ overview.totalErrors || 0 }} 条错误</span>
        </div>
      </div>
    </section>

    <div class="grid grid-4">
      <MetricCard label="进行中目标" :value="overview.activeGoals || 0" note="主线推进中的目标" :icon="Aim" tone="primary" />
      <MetricCard label="今日待完成" :value="overview.todayTasks || 0" note="到期且未完成任务" :icon="Clock" tone="blue" />
      <MetricCard label="今日待复习" :value="overview.todayReviews || 0" note="需要回炉的错误" :icon="Refresh" tone="warning" />
      <MetricCard label="本周新增错误" :value="overview.weekNewErrors || 0" note="最近 7 天记录" :icon="Warning" tone="danger" />
    </div>

    <div class="grid grid-3">
      <section class="panel panel-pad chart-panel wide-panel">
        <div class="section-title">
          <span>最近 7 天趋势</span>
          <span class="section-eyebrow">FLOW</span>
        </div>
        <div ref="trendEl" class="chart-box"></div>
      </section>
      <section class="panel panel-pad current-goal">
        <div class="section-title">
          <span>当前主线目标</span>
          <span class="section-eyebrow">MAIN</span>
        </div>
        <template v-if="overview.currentGoal">
          <span class="status-pill primary">{{ overview.currentGoal.category || '自定义目标' }}</span>
          <h3>{{ overview.currentGoal.title }}</h3>
          <p class="muted">{{ overview.currentGoal.description }}</p>
          <div class="goal-progress">
            <span>完成进度</span>
            <strong>{{ overview.currentGoal.progress || 0 }}%</strong>
          </div>
          <el-progress :percentage="overview.currentGoal.progress || 0" :stroke-width="12" />
          <el-button type="primary" style="margin-top: 16px" @click="router.push(`/goals/${overview.currentGoal.id}`)">查看详情</el-button>
        </template>
        <el-empty v-else description="还没有进行中的目标" />
      </section>
    </div>

    <div class="grid grid-3">
      <section class="panel panel-pad">
        <div class="section-title">
          <span>今天该做什么</span>
          <span class="section-eyebrow">TODAY</span>
        </div>
        <div class="data-list" v-if="(overview.todayTaskItems || []).length">
          <div v-for="task in overview.todayTaskItems || []" :key="task.id" class="data-row">
            <div class="row-head">
              <strong>{{ task.title }}</strong>
              <span class="status-pill">{{ task.priority }}</span>
            </div>
            <span class="muted">截止 {{ task.dueDate || '未设截止' }}</span>
          </div>
        </div>
        <el-empty v-else description="今天没有逾期任务" />
      </section>

      <section class="panel panel-pad">
        <div class="section-title">
          <span>该复习的错误</span>
          <span class="section-eyebrow">REVIEW</span>
        </div>
        <div class="data-list" v-if="(overview.reviewItems || []).length">
          <div v-for="item in overview.reviewItems || []" :key="item.error.id" class="data-row">
            <div class="row-head">
              <strong>{{ item.error.title }}</strong>
              <span class="status-pill danger">{{ item.error.severity }}</span>
            </div>
            <span class="muted">{{ item.error.errorType }} · 第 {{ item.error.reviewCount || 0 }} 次复习</span>
          </div>
        </div>
        <el-empty v-else description="没有待复习错误" />
      </section>

      <section class="panel panel-pad chart-panel">
        <div class="section-title">
          <span>反复出现的标签</span>
          <span class="section-eyebrow">TAGS</span>
        </div>
        <div ref="tagEl" class="chart-box small"></div>
      </section>
    </div>

    <section class="panel panel-pad">
      <div class="section-title">
        <span>最近新增错误</span>
        <span class="section-eyebrow">ERROR FEED</span>
      </div>
      <div class="recent-error-grid" v-if="(overview.recentErrors || []).length">
        <article v-for="error in overview.recentErrors || []" :key="error.id" class="recent-error">
          <div class="row-head">
            <span class="status-pill danger">{{ error.errorType }}</span>
            <span class="muted">{{ error.severity }}</span>
          </div>
          <h3>{{ error.title }}</h3>
          <p class="muted">{{ error.summary || error.description }}</p>
        </article>
      </div>
      <el-empty v-else description="还没有错误记录" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Aim, Clock, Collection, Refresh, Warning } from '@element-plus/icons-vue'
import MetricCard from '@/components/MetricCard.vue'
import { api } from '@/api/client'

const router = useRouter()
const overview = ref<any>({})
const trends = ref<any>({})
const tagStats = ref<any[]>([])
const trendEl = ref<HTMLDivElement>()
const tagEl = ref<HTMLDivElement>()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
const todayText = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
const completionRate = computed(() => {
  const total = Number(overview.value.totalTasks || 0)
  if (!total) return 0
  return Math.round((Number(overview.value.doneTasks || 0) / total) * 100)
})

const load = async () => {
  overview.value = await api.get('/statistics/overview')
  trends.value = await api.get('/statistics/trends')
  tagStats.value = await api.get('/statistics/error-tags')
  await nextTick()
  renderCharts()
}

const token = (name: string) => getComputedStyle(document.documentElement).getPropertyValue(name).trim()

const renderCharts = () => {
  const text = token('--muted')
  const border = token('--border')
  const primary = token('--primary')
  const blue = token('--primary-2')
  const danger = token('--danger')
  const good = token('--good')

  if (trendEl.value) {
    const chart = echarts.init(trendEl.value)
    chart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      legend: { textStyle: { color: text } },
      grid: { left: 32, right: 16, top: 40, bottom: 28 },
      xAxis: { type: 'category', data: trends.value.days || [], axisLine: { lineStyle: { color: border } }, axisLabel: { color: text } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: border } }, axisLabel: { color: text } },
      series: [
        { name: '完成任务', type: 'line', smooth: true, symbolSize: 7, data: trends.value.doneTasks || [], color: good, areaStyle: { color: `${good}20` } },
        { name: '新增错误', type: 'line', smooth: true, symbolSize: 7, data: trends.value.newErrors || [], color: danger, areaStyle: { color: `${danger}1c` } }
      ]
    })
  }
  if (tagEl.value) {
    const chart = echarts.init(tagEl.value)
    chart.setOption({
      tooltip: {},
      color: [primary, blue, good, danger],
      series: [{
        type: 'pie',
        radius: ['48%', '72%'],
        data: tagStats.value,
        label: { color: text }
      }]
    })
  }
}

onMounted(load)
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  gap: 18px;
}

.mission-hero {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 24px;
  min-height: 250px;
}

.mission-copy {
  display: grid;
  align-content: center;
  gap: 14px;
  max-width: 740px;
}

.mission-copy h2 {
  margin: 0;
  font-size: 36px;
  line-height: 1.14;
}

.mission-copy p {
  margin: 0;
  color: var(--muted);
  line-height: 1.75;
}

.mission-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.mission-radar {
  min-width: 270px;
  display: grid;
  place-items: center;
  gap: 14px;
}

.radar-ring {
  width: 190px;
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  border-radius: 50%;
  border: 1px solid color-mix(in srgb, var(--primary), transparent 50%);
  background:
    repeating-conic-gradient(from 0deg, color-mix(in srgb, var(--primary), transparent 72%) 0deg 6deg, transparent 6deg 18deg),
    radial-gradient(circle, var(--surface) 0 52%, transparent 53%);
}

.radar-ring strong {
  display: block;
  font-size: 42px;
}

.radar-ring span {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
}

.radar-meta {
  display: grid;
  gap: 6px;
  text-align: center;
  color: var(--muted);
  font-size: 13px;
}

.wide-panel {
  grid-column: span 2;
}

.chart-box {
  height: 318px;
}

.chart-box.small {
  height: 272px;
}

.current-goal h3 {
  margin: 14px 0 8px;
  line-height: 1.35;
}

.current-goal p {
  min-height: 78px;
  line-height: 1.65;
}

.goal-progress {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 18px 0 8px;
  color: var(--muted);
}

.recent-error-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.recent-error {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: color-mix(in srgb, var(--surface-2), transparent 35%);
}

.recent-error h3 {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
}

.recent-error p {
  margin: 0;
  line-height: 1.6;
}

@media (max-width: 1180px) {
  .wide-panel { grid-column: auto; }
  .mission-hero { flex-direction: column; }
  .mission-radar {
    min-width: 0;
    place-items: start;
  }
}

@media (max-width: 820px) {
  .mission-copy h2 { font-size: 28px; }
  .recent-error-grid { grid-template-columns: 1fr; }
}
</style>
