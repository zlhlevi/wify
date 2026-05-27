<script setup lang="ts" generic="FormData extends Record<string, unknown>">
import { computed, getCurrentInstance, nextTick, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { WifyFormDialogSubmitPayload } from '@/components/types'

defineOptions({
  name: 'WifyFormDialog',
})

const props = withDefaults(defineProps<{
  title: string
  width?: number | string
  rules?: FormRules
  labelWidth?: number | string
  labelPosition?: 'left' | 'right' | 'top'
}>(), {
  width: 640,
  rules: () => ({}),
  labelWidth: undefined,
  labelPosition: 'top',
})

const dialogVisible = defineModel<boolean>({
  default: false,
})

const formRef = ref<FormInstance>()
const formData = ref<FormData>({} as FormData)
const submitLoading = ref(false)
const isEditMode = ref(false)
const instance = getCurrentInstance()

const dialogWidth = computed(() => (
  typeof props.width === 'number' ? `${props.width}px` : props.width
))

const formLabelWidth = computed(() => {
  if (props.labelWidth === undefined) {
    return undefined
  }

  return typeof props.labelWidth === 'number' ? `${props.labelWidth}px` : props.labelWidth
})

function cloneValue<ValueType>(value: ValueType): ValueType {
  if (typeof structuredClone === 'function') {
    return structuredClone(value)
  }

  return JSON.parse(JSON.stringify(value)) as ValueType
}

function close() {
  dialogVisible.value = false
}

function resetForm() {
  formRef.value?.clearValidate()
  formData.value = {} as FormData
  isEditMode.value = false
}

async function open(data?: Partial<FormData>) {
  formData.value = data ? cloneValue(data as FormData) : ({} as FormData)
  isEditMode.value = Boolean(data)
  dialogVisible.value = true

  await nextTick()
  formRef.value?.clearValidate()
}

async function invokeSubmitListener(payload: WifyFormDialogSubmitPayload<FormData>) {
  const listeners = instance?.vnode.props
  const listener = listeners?.onSubmit || listeners?.onSubmi

  if (!listener) {
    return
  }

  if (Array.isArray(listener)) {
    for (const item of listener) {
      if (typeof item === 'function') {
        await item(payload)
      }
    }

    return
  }

  if (typeof listener === 'function') {
    await listener(payload)
  }
}

async function handleSubmit() {
  if (submitLoading.value) {
    return
  }

  if (formRef.value) {
    try {
      await formRef.value.validate()
    } catch {
      return
    }
  }

  submitLoading.value = true

  try {
    await invokeSubmitListener({
      model: cloneValue(formData.value),
      isEditMode: isEditMode.value,
      close,
    })
    close()
  } finally {
    submitLoading.value = false
  }
}

defineExpose({
  open,
  close,
  formData,
  isEditMode,
  submitLoading,
  refreshValidate: () => formRef.value?.validate(),
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    :width="dialogWidth"
    destroy-on-close
    class="wify-form-dialog"
    @closed="resetForm"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-width="formLabelWidth"
      :label-position="labelPosition"
      class="wify-form-dialog__form"
      status-icon
    >
      <slot
        :form-data="formData"
        :is-edit-mode="isEditMode"
        :submit-loading="submitLoading"
      />
    </el-form>

    <template #footer>
      <div class="wify-form-dialog__footer">
        <el-button @click="close">
          取消
        </el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEditMode ? '保存' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.wify-form-dialog :deep(.el-dialog) {
  border-radius: var(--wf-radius-lg);
  box-shadow: var(--wf-shadow-lg);
}

.wify-form-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 20px 0;
}

.wify-form-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.wify-form-dialog :deep(.el-dialog__footer) {
  padding: 0 20px 20px;
}

.wify-form-dialog__form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wify-form-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
