import { ref } from 'vue'
import { getHealth } from '@/api/health'
import type { HealthState } from '@/types/app'

export function useBackendHealth() {
  const state = ref<HealthState>('idle')
  const message = ref('')
  const lastCheckedAt = ref('')

  async function load() {
    state.value = 'loading'

    try {
      message.value = await getHealth()
      state.value = 'online'
      lastCheckedAt.value = new Date().toLocaleString('zh-CN')
    } catch (error) {
      state.value = 'offline'
      message.value = error instanceof Error ? error.message : 'Request failed'
      lastCheckedAt.value = new Date().toLocaleString('zh-CN')
    }
  }

  return {
    state,
    message,
    lastCheckedAt,
    load,
  }
}
