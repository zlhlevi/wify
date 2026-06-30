import { del, get, post, put } from '@/utils/request'
import type { PageQuery, PageResult } from '@/types/app'

export type ProviderType = 'OPENAI' | 'ANTHROPIC' | 'OLLAMA' | 'OPENAI_COMPATIBLE'

export type ProviderHealthStatus = 'UP' | 'DOWN' | 'DEGRADED' | 'UNKNOWN'

export interface ProviderHealthResp {
  providerId: number
  status: ProviderHealthStatus | ''
  lastCheckAt: string | null
  lastSuccessAt: string | null
  failCount: number | null
  latencyMs: number | null
  errorMessage: string
  updatedAt: string | null
}

export interface ModelConfigResp {
  id: number
  providerId: number
  name: string
  modelId: string
  contextSize: number | null
  extraParams?: Record<string, unknown> | null
  enabled: number
  createdAt: string | null
  updatedAt: string | null
}

export interface ProviderResp {
  id: number
  name: string
  type: ProviderType
  baseUrl: string
  enabled: number
  authConfigured: boolean
  enabledModelCount: number
  health: ProviderHealthResp | null
  createdAt: string | null
  updatedAt: string | null
}

export interface ProviderDetailResp extends ProviderResp {
  authConfig?: Record<string, unknown>
  modelConfigs: ModelConfigResp[]
}

export interface ProviderListQuery extends PageQuery {
  type?: ProviderType
  enabled?: 0 | 1
}

export interface ProviderUpsertPayload {
  name: string
  type: ProviderType
  baseUrl: string
  authConfig?: Record<string, unknown>
  enabled?: number
}

export interface ConnectionTestResult {
  success: boolean
  latencyMs: number
  modelCount: number
  errorMessage: string
}

export function getProviderList(params: ProviderListQuery) {
  return get<PageResult<ProviderResp>>('/v1/providers', {
    params,
  })
}

export function getProviderDetail(id: number) {
  return get<ProviderDetailResp>(`/v1/providers/${id}`)
}

export function createProvider(data: ProviderUpsertPayload) {
  return post<number>('/v1/providers', data)
}

export function updateProvider(id: number, data: ProviderUpsertPayload) {
  return put<number>(`/v1/providers/${id}`, data)
}

export function deleteProvider(id: number) {
  return del<void>(`/v1/providers/${id}`)
}

export function testConnection(id: number) {
  return post<ConnectionTestResult>(`/v1/providers/${id}/test-connection`)
}
