import { ref } from 'vue'

type AsyncMethod<DataType, Args extends unknown[]> = (...args: Args) => Promise<DataType>

function normalizeError(error: unknown): Error {
  if (error instanceof Error) {
    return error
  }

  return new Error(typeof error === 'string' ? error : 'Request failed')
}

export function useRequest<DataType, Args extends unknown[]>(
  api: AsyncMethod<DataType, Args>,
) {
  const data = ref<DataType | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const execute = async (...args: Args) => {
    loading.value = true
    error.value = null

    try {
      const response = await api(...args)
      data.value = response
      return response
    } catch (requestError) {
      const normalizedError = normalizeError(requestError)
      error.value = normalizedError
      throw normalizedError
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    loading,
    error,
    execute,
  }
}
