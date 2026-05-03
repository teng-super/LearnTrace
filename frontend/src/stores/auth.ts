import { defineStore } from 'pinia'
import { api } from '@/api/client'

export interface UserInfo {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl?: string
  theme?: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('learntrace_token') || '',
    user: null as UserInfo | null
  }),
  getters: {
    isLoggedIn: state => Boolean(state.token)
  },
  actions: {
    async login(usernameOrEmail: string, password: string) {
      const data = await api.post('/auth/login', { usernameOrEmail, password })
      this.setSession(data.token, data.user)
    },
    async register(payload: { username: string; email: string; password: string; nickname?: string }) {
      const data = await api.post('/auth/register', payload)
      this.setSession(data.token, data.user)
    },
    async fetchMe() {
      if (!this.token) return
      this.user = await api.get('/auth/me')
    },
    setSession(token: string, user: UserInfo) {
      this.token = token
      this.user = user
      localStorage.setItem('learntrace_token', token)
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('learntrace_token')
    }
  }
})
