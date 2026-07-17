import { apiPost } from './request'
import type {
  AiReview,
  ContentDetail,
  ContentListItem,
  Dashboard,
  PageResult,
  PerformanceAnalysis,
  PublishRecord,
  Topic,
} from '@/types'

export const topicApi = {
  generate: (data: { count: number; generationDate?: string }) => apiPost<Topic[]>('/topics/generate', data),
  page: (data: Record<string, unknown>) => apiPost<PageResult<Topic>>('/topics/page', data),
  detail: (id: string) => apiPost<Topic>('/topics/detail', { id }),
  create: (data: Record<string, unknown>) => apiPost<Topic>('/topics/create', data),
  update: (data: Record<string, unknown>) => apiPost<Topic>('/topics/update', data),
  remove: (id: string) => apiPost<void>('/topics/delete', { id }),
  status: (id: string, status: string) => apiPost<void>('/topics/status/update', { id, status }),
}

export const contentApi = {
  generate: (topicId: string) => apiPost<ContentDetail>('/contents/generate', { topicId }),
  page: (data: Record<string, unknown>) => apiPost<PageResult<ContentListItem>>('/contents/page', data),
  detail: (id: string) => apiPost<ContentDetail>('/contents/detail', { id }),
  create: (data: Record<string, unknown>) => apiPost<ContentDetail>('/contents/create', data),
  update: (data: Record<string, unknown>) => apiPost<ContentDetail>('/contents/update', data),
  remove: (id: string) => apiPost<void>('/contents/delete', { id }),
  status: (id: string, targetStatus: string) => apiPost<void>('/contents/status/update', { id, targetStatus }),
  copy: (id: string) => apiPost<{ contentId: string; title: string; bodyWithTags: string; interactionGuide: string }>('/contents/copy', { id }),
}

export const reviewApi = {
  execute: (contentId: string) => apiPost<AiReview>('/reviews/execute', { contentId }),
  history: (id: string) => apiPost<AiReview[]>('/reviews/history', { id }),
  confirm: (contentId: string, approved: boolean) => apiPost<void>('/reviews/confirm', { contentId, approved }),
}

export const publishApi = {
  save: (data: Record<string, unknown>) => apiPost<PublishRecord>('/publish-records/save', data),
  page: (data: Record<string, unknown>) => apiPost<PageResult<PublishRecord>>('/publish-records/page', data),
  detail: (id: string) => apiPost<PublishRecord>('/publish-records/detail', { id }),
}

export const analysisApi = {
  execute: (publishRecordId: string) => apiPost<PerformanceAnalysis>('/analyses/execute', { publishRecordId }),
  latest: (id: string) => apiPost<PerformanceAnalysis>('/analyses/latest', { id }),
  dashboard: (data: { startDate?: string; endDate?: string }) => apiPost<Dashboard>('/analyses/dashboard', data),
}
