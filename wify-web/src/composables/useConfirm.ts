import { ElMessageBox } from 'element-plus'
import { notifySuccess } from '@/utils/notify'

interface ConfirmOptions {
  title?: string
  confirmButtonText?: string
  cancelButtonText?: string
  successMessage?: string
}

export async function useConfirm<ResultType>(
  message: string,
  api: () => Promise<ResultType>,
  options: ConfirmOptions = {},
) {
  try {
    await ElMessageBox.confirm(
      message,
      options.title || '确认操作',
      {
        confirmButtonText: options.confirmButtonText || '确认',
        cancelButtonText: options.cancelButtonText || '取消',
        type: 'warning',
        customClass: 'wify-confirm-box',
        autofocus: false,
      },
    )
  } catch {
    return undefined
  }

  const result = await api()
  notifySuccess(options.successMessage || '操作成功')
  return result
}
