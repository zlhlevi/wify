import { ElMessage } from 'element-plus'

const BASE_MESSAGE_OPTIONS = {
  duration: 2400,
  offset: 20,
  showClose: true,
  grouping: true,
}

export function notifySuccess(message: string) {
  return ElMessage({
    ...BASE_MESSAGE_OPTIONS,
    type: 'success',
    message,
    customClass: 'wify-notify wify-notify--success',
  })
}

export function notifyError(message: string) {
  return ElMessage({
    ...BASE_MESSAGE_OPTIONS,
    type: 'error',
    message,
    customClass: 'wify-notify wify-notify--error',
  })
}

export function notifyWarning(message: string) {
  return ElMessage({
    ...BASE_MESSAGE_OPTIONS,
    type: 'warning',
    message,
    customClass: 'wify-notify wify-notify--warning',
  })
}
