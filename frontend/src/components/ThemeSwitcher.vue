<template>
  <el-dropdown trigger="click" @command="select">
    <el-button :icon="Brush" circle />
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item v-for="themeItem in themes" :key="themeItem.name" :command="themeItem.name">
          <span style="font-weight: 700">{{ themeItem.label }}</span>
          <span class="muted" style="margin-left: 8px">{{ themeItem.hint }}</span>
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
</script>
