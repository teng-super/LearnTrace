<template>
  <el-dropdown trigger="click" @command="select">
    <el-tooltip content="切换主题" placement="bottom">
      <el-button :icon="Brush" circle />
    </el-tooltip>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item v-for="themeItem in themes" :key="themeItem.name" :command="themeItem.name">
          <div class="theme-menu-item" :style="swatch(themeItem.name)">
            <span class="theme-swatch"></span>
            <span>
              <span style="font-weight: 800">{{ themeItem.label }}</span>
              <span v-if="theme.current === themeItem.name" class="status-pill primary" style="margin-left: 8px">当前</span>
              <br />
              <span class="muted">{{ themeItem.hint }}</span>
            </span>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { Brush } from '@element-plus/icons-vue'
import { themes, useThemeStore, type ThemeName } from '@/stores/theme'

const theme = useThemeStore()
const select = (name: ThemeName) => theme.setTheme(name)

const swatch = (name: ThemeName) => {
  const values: Record<ThemeName, Record<string, string>> = {
    'battle-dark': { '--swatch-a': '#090c11', '--swatch-b': '#f7c75d', '--swatch-c': '#6bb8ff' },
    'daylight-pro': { '--swatch-a': '#ffffff', '--swatch-b': '#2563eb', '--swatch-c': '#0f9f7a' },
    'paper-care': { '--swatch-a': '#fffaf1', '--swatch-b': '#2f6f5e', '--swatch-c': '#9a5a2c' },
    terminal: { '--swatch-a': '#050907', '--swatch-b': '#56f39a', '--swatch-c': '#f0c55c' }
  }
  return values[name]
}
</script>
