import type { Component } from 'vue'

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageQuery {
  page: number
  pageSize: number
}

export interface NavigationItem {
  index: string
  label: string
  icon: Component
}

export type HealthState = 'idle' | 'loading' | 'online' | 'offline'
