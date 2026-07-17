import axios from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 120_000,
  headers: { 'Content-Type': 'application/json' },
})

export async function apiPost<T>(url: string, data: unknown = {}): Promise<T> {
  try {
    const response = await http.post<ApiResult<T>>(url, data)
    if (response.data.code !== 0) {
      throw new Error(response.data.message || '请求失败')
    }
    return response.data.data
  } catch (error) {
    const message = axios.isAxiosError(error)
      ? (error.response?.data as Partial<ApiResult<unknown>> | undefined)?.message || error.message
      : error instanceof Error ? error.message : '请求失败'
    ElMessage.error(message)
    throw error
  }
}
