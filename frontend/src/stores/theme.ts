import { defineStore } from 'pinia'

export type ThemeName = 'battle-dark' | 'daylight-pro' | 'paper-care' | 'terminal'

export const themes: Array<{ name: ThemeName; label: string; hint: string }> = [
  { name: 'battle-dark', label: '作战夜航', hint: '深色驾驶舱' },
  { name: 'daylight-pro', label: '专业日光', hint: '清爽后台' },
  { name: 'paper-care', label: '护眼纸张', hint: '长文笔记' },
  { name: 'terminal', label: '代码终端', hint: '高专注' }
]

export const useThemeStore = defineStore('theme', {
  state: () => ({
    current: (localStorage.getItem('learntrace_theme') || 'battle-dark') as ThemeName
  }),
  actions: {
    setTheme(name: ThemeName) {
      this.current = name
      localStorage.setItem('learntrace_theme', name)
      this.apply()
    },
    apply() {
      document.documentElement.dataset.theme = this.current
    }
  }
})
