import { onBeforeUnmount, onMounted, ref } from 'vue'

export function useViewportWidth(initialWidth = 1440) {
  const width = ref(typeof window !== 'undefined' ? window.innerWidth : initialWidth)

  const syncWidth = () => {
    width.value = window.innerWidth
  }

  onMounted(() => {
    syncWidth()
    window.addEventListener('resize', syncWidth, { passive: true })
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', syncWidth)
  })

  return {
    width,
  }
}
