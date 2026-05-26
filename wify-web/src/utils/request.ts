import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types/app'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>

    if (result.code === 200) {
      return result.data as never
    }

    ElMessage.error(result.message || 'Request failed')
    return Promise.reject(new Error(result.message || 'Request failed'))
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      'Request failed'

    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export function get<T>(url: string, config?: AxiosRequestConfig) {
  return request.get<Result<T>, T>(url, config)
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<Result<T>, T>(url, data, config)
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<Result<T>, T>(url, data, config)
}

export function del<T>(url: string, config?: AxiosRequestConfig) {
  return request.delete<Result<T>, T>(url, config)
}
