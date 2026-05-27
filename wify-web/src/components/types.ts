import type { TableColumnCtx } from 'element-plus'

export interface WifyTableColumn<RowData = any> {
  label: string
  prop?: Extract<keyof RowData, string> | string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  fixed?: boolean | 'left' | 'right'
  slot?: string
}

export interface WifyTableSlotProps<RowData = any> {
  row: RowData
  column: TableColumnCtx<Record<string, any>>
  $index: number
}

export interface WifyFormDialogSubmitPayload<FormData = any> {
  model: FormData
  isEditMode: boolean
  close: () => void
}
