import axios, { type AxiosRequestConfig } from 'axios'
import router from '@/router'

const instance = axios.create({
  baseURL: '/api',
  timeout: 20000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('learntrace_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

instance.interceptors.response.use(
  res => {
    if (res.config.responseType === 'blob') return res.data
    return res.data.data
  },
  err => {
    if (err.response?.status === 401 || err.response?.data?.code === 401) {
      localStorage.removeItem('learntrace_token')
      router.push('/login')
    }
    return Promise.reject(err.response?.data || err)
  }
)

export const api = {
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return instance.get<T, T>(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return instance.post<T, T>(url, data, config)
  },
  put<T = any>(url: string, data?: any) {
    return instance.put<T, T>(url, data)
  },
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return instance.patch<T, T>(url, data, config)
  },
  delete<T = any>(url: string) {
    return instance.delete<T, T>(url)
  }
}

export const rawApi = instance
