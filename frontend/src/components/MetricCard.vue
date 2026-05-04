<template>
  <div class="panel metric" :style="toneStyle">
    <div class="metric-top">
      <div class="metric-label">{{ label }}</div>
      <div v-if="icon" class="metric-icon">
        <component :is="icon" style="width: 18px" />
      </div>
    </div>
    <div class="metric-value">{{ value }}</div>
    <div v-if="note" class="metric-note">{{ note }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'

const props = defineProps<{
  label: string
  value: string | number
  note?: string
  icon?: Component
  tone?: 'primary' | 'blue' | 'green' | 'danger' | 'warning'
}>()

const toneStyle = computed(() => {
  const map = {
    primary: 'var(--primary)',
    blue: 'var(--primary-2)',
    green: 'var(--good)',
    danger: 'var(--danger)',
    warning: 'var(--warning)'
  }
  return { '--metric-color': map[props.tone || 'primary'] }
})
</script>
