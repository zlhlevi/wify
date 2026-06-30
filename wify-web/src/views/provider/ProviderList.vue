<script setup lang="ts">
import { Connection, Plus } from '@element-plus/icons-vue'
import { ElMessage, type FormRules } from 'element-plus'
import { computed, ref, watch } from 'vue'
import {
  createProvider,
  deleteProvider,
  getProviderDetail,
  getProviderList,
  testConnection,
  updateProvider,
  type ModelConfigResp,
  type ProviderDetailResp,
  type ProviderHealthResp,
  type ProviderResp,
  type ProviderType,
} from '@/api/provider'
import WifyFormDialog from '@/components/WifyFormDialog.vue'
import WifyTable from '@/components/WifyTable.vue'
import type {
  WifyFormDialogSubmitPayload,
  WifyTableColumn,
  WifyTableInstance,
} from '@/components/types'
import { useConfirm } from '@/composables/useConfirm'
import { useViewportWidth } from '@/composables/useViewportWidth'
import { notifySuccess } from '@/utils/notify'

const TABLE_COMPACT_BREAKPOINT = 1200

type ProviderItem = ProviderResp
type ProviderHealthStatus = 'UP' | 'DOWN' | 'DEGRADED' | 'UNKNOWN'

interface ProviderFormModel {
  name?: string
  type?: ProviderType | ''
  apiKey?: string
  baseUrl?: string
  enabled?: number
}

interface ProviderDialogInstance {
  open: (data?: Partial<ProviderFormModel>) => Promise<void>
  formData?: ProviderFormModel
}

const tableRef = ref<WifyTableInstance<ProviderItem> | null>(null)
const dialogRef = ref<ProviderDialogInstance | null>(null)
const dialogVisible = ref(false)
const editingProviderId = ref<number | null>(null)
const providerDetails = ref<Record<number, ProviderDetailResp>>({})
const detailLoadingMap = ref<Record<number, boolean>>({})
const testingProviderId = ref<number | null>(null)
const expandedRowKeys = ref<number[]>([])
const detailRequests = new Map<number, Promise<ProviderDetailResp>>()
const { width: viewportWidth } = useViewportWidth()

const providerTypeOptions: Array<{ label: string; value: ProviderType }> = [
  { label: 'OpenAI', value: 'OPENAI' },
  { label: 'Anthropic', value: 'ANTHROPIC' },
  { label: 'Ollama', value: 'OLLAMA' },
  { label: 'OpenAI Compatible', value: 'OPENAI_COMPATIBLE' },
]

const currentFormType = computed<ProviderType | ''>(() => (
  dialogRef.value?.formData?.type || ''
))

const isCompactTable = computed(() => viewportWidth.value < TABLE_COMPACT_BREAKPOINT)

const columns = computed<WifyTableColumn<ProviderItem>[]>(() => {
  const baseColumns: WifyTableColumn<ProviderItem>[] = [
    {
      label: '名称',
      prop: 'name',
      minWidth: 220,
      slot: 'name',
    },
    {
      label: '类型',
      width: 160,
      slot: 'type',
    },
    {
      label: '状态',
      width: 110,
      align: 'center',
      slot: 'status',
    },
    {
      label: '健康状态',
      minWidth: 180,
      slot: 'health',
    },
    {
      label: '模型数',
      width: 130,
      align: 'center',
      slot: 'modelCount',
    },
    {
      label: '操作',
      width: 260,
      fixed: 'right',
      align: 'right',
      slot: 'actions',
    },
  ]

  if (isCompactTable.value) {
    return baseColumns
  }

  return [
    baseColumns[0],
    baseColumns[1],
    {
      label: 'Base URL',
      prop: 'baseUrl',
      minWidth: 280,
    },
    baseColumns[2],
    baseColumns[3],
    baseColumns[4],
    {
      label: '创建时间',
      prop: 'createdAt',
      width: 180,
    },
    baseColumns[5],
  ]
})

const formRules: FormRules<ProviderFormModel> = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' },
  ],
  apiKey: [
    {
      validator: (_rule, value: string | undefined, callback) => {
        if (currentFormType.value !== 'OLLAMA' && !(value || '').trim()) {
          callback(new Error('请输入 API Key'))
          return
        }

        callback()
      },
      trigger: 'blur',
    },
  ],
  baseUrl: [
    { required: true, message: '请输入 Base URL', trigger: 'blur' },
  ],
}

const dialogTitle = computed(() => (
  editingProviderId.value === null ? '新增提供商' : '编辑提供商'
))

const apiKeyPlaceholder = computed(() => (
  currentFormType.value === 'OLLAMA'
    ? 'Ollama 无需鉴权时可留空'
    : '请输入 API Key'
))

watch(dialogVisible, (visible) => {
  if (!visible) {
    editingProviderId.value = null
  }
})

function providerTypeLabel(type: ProviderType) {
  return providerTypeOptions.find((item) => item.value === type)?.label || type
}

function providerStatusTagType(enabled: number) {
  return enabled === 1 ? 'success' : 'info'
}

function providerStatusLabel(enabled: number) {
  return enabled === 1 ? '启用' : '禁用'
}

function normalizeHealthStatus(status?: string | null): ProviderHealthStatus {
  if (status === 'UP' || status === 'DOWN' || status === 'DEGRADED' || status === 'UNKNOWN') {
    return status
  }

  return 'UNKNOWN'
}

function healthStatusTagType(status: ProviderHealthStatus) {
  if (status === 'UP') {
    return 'success'
  }

  if (status === 'DOWN') {
    return 'danger'
  }

  if (status === 'DEGRADED') {
    return 'warning'
  }

  return 'info'
}

function formatLatency(health?: ProviderHealthResp | null) {
  if (health?.latencyMs === null || health?.latencyMs === undefined) {
    return '--'
  }

  return `${health.latencyMs} ms`
}

function formatContextSize(contextSize?: number | null) {
  if (contextSize === null || contextSize === undefined) {
    return '上下文未配置'
  }

  return `上下文 ${contextSize} tokens`
}

function providerRowStyle() {
  return {
    height: '52px',
  }
}

function buildProviderPayload(model: ProviderFormModel) {
  const apiKey = (model.apiKey || '').trim()

  return {
    name: (model.name || '').trim(),
    type: model.type as ProviderType,
    baseUrl: (model.baseUrl || '').trim(),
    authConfig: apiKey ? { apiKey } : undefined,
    enabled: model.enabled ?? 1,
  }
}

function clearProviderDetail(providerId: number) {
  if (!providerDetails.value[providerId]) {
    return
  }

  const nextDetails = { ...providerDetails.value }
  delete nextDetails[providerId]
  providerDetails.value = nextDetails
}

async function openCreateDialog() {
  editingProviderId.value = null
  await dialogRef.value?.open()
}

async function openEditDialog(row: ProviderItem) {
  editingProviderId.value = row.id
  const detail = await ensureProviderDetail(row.id)
  const apiKey = typeof detail.authConfig?.apiKey === 'string'
    ? detail.authConfig.apiKey
    : ''

  await dialogRef.value?.open({
    name: row.name,
    type: row.type,
    apiKey,
    baseUrl: row.baseUrl,
    enabled: row.enabled,
  })
}

async function handleDelete(row: ProviderItem) {
  const confirmed = await useConfirm(
    `确认删除提供商「${row.name}」吗？`,
    async () => {
      await deleteProvider(row.id)
      return true
    },
    {
      title: '删除提供商',
      successMessage: '提供商已删除',
    },
  )

  if (confirmed) {
    clearProviderDetail(row.id)
    expandedRowKeys.value = expandedRowKeys.value.filter((item) => item !== row.id)
    await tableRef.value?.refresh()
  }
}

async function handleSubmit({
  model,
}: WifyFormDialogSubmitPayload<ProviderFormModel>) {
  const payload = buildProviderPayload(model)

  if (editingProviderId.value !== null) {
    await updateProvider(editingProviderId.value, payload)
    clearProviderDetail(editingProviderId.value)
    notifySuccess('提供商已更新')
  } else {
    await createProvider(payload)
    notifySuccess('提供商已创建')
  }

  await tableRef.value?.refresh()
}

function isDetailLoading(providerId: number) {
  return Boolean(detailLoadingMap.value[providerId])
}

function getProviderModels(providerId: number): ModelConfigResp[] {
  return providerDetails.value[providerId]?.modelConfigs || []
}

function getModelCountLabel(row: ProviderItem) {
  return `${row.enabledModelCount ?? 0} 个`
}

function getModelCountHint(row: ProviderItem) {
  if (isDetailLoading(row.id) && !providerDetails.value[row.id]) {
    return '正在加载'
  }

  if (!providerDetails.value[row.id]) {
    return '点击展开'
  }

  const total = getProviderModels(row.id).length

  if (!total) {
    return '暂无模型'
  }

  return `共 ${total} 个模型`
}

async function ensureProviderDetail(providerId: number) {
  const cached = providerDetails.value[providerId]
  if (cached) {
    return cached
  }

  const pending = detailRequests.get(providerId)
  if (pending) {
    return pending
  }

  detailLoadingMap.value = {
    ...detailLoadingMap.value,
    [providerId]: true,
  }

  const request = getProviderDetail(providerId)
    .then((detail) => {
      providerDetails.value = {
        ...providerDetails.value,
        [providerId]: detail,
      }
      return detail
    })
    .finally(() => {
      detailRequests.delete(providerId)
      detailLoadingMap.value = {
        ...detailLoadingMap.value,
        [providerId]: false,
      }
    })

  detailRequests.set(providerId, request)
  return request
}

function handleExpandChange(row: ProviderItem, expandedRows: ProviderItem[]) {
  expandedRowKeys.value = expandedRows.map((item) => item.id)

  if (expandedRowKeys.value.includes(row.id)) {
    void ensureProviderDetail(row.id)
  }
}

async function handleToggleModels(row: ProviderItem) {
  await ensureProviderDetail(row.id)
  const expanded = expandedRowKeys.value.includes(row.id)
  tableRef.value?.toggleRowExpansion(row, !expanded)
}

async function handleTestConnection(row: ProviderItem) {
  testingProviderId.value = row.id

  try {
    const result = await testConnection(row.id)

    if (result.success) {
      ElMessage({
        type: 'success',
        message: `连通性测试成功，延迟 ${result.latencyMs} ms，模型数 ${result.modelCount}`,
      })
      return
    }

    ElMessage({
      type: 'error',
      message: result.errorMessage || '连通性测试失败',
    })
  } finally {
    if (testingProviderId.value === row.id) {
      testingProviderId.value = null
    }
  }
}
</script>

<template>
  <div class="wf-page provider-page">
    <section class="wf-page__header">
      <div>
        <span class="wf-page__eyebrow">
          <el-icon><Connection /></el-icon>
          Provider Center
        </span>
        <h1 class="wf-page__title">模型提供商管理</h1>
        <p class="wf-page__description">
          使用统一的提供商列表管理名称、类型、鉴权信息、健康状态和模型配置，页面数据直接来自 Provider API。
        </p>
      </div>

      <div class="wf-page__actions">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新增提供商
        </el-button>
      </div>
    </section>

    <WifyTable
      ref="tableRef"
      :columns="columns"
      :api="getProviderList"
      :row-style="providerRowStyle"
      row-key="id"
      @expand-change="handleExpandChange"
    >
      <template #expand="{ row }">
        <div class="provider-page__expand-panel">
          <template v-if="isDetailLoading(row.id) && !providerDetails[row.id]">
            <el-skeleton animated>
              <template #template>
                <div class="provider-page__skeleton-row" />
                <div class="provider-page__skeleton-row provider-page__skeleton-row--short" />
              </template>
            </el-skeleton>
          </template>

          <template v-else-if="getProviderModels(row.id).length">
            <div class="provider-page__model-list">
              <div
                v-for="model in getProviderModels(row.id)"
                :key="model.id"
                class="provider-page__model-item"
              >
                <div class="provider-page__model-main">
                  <span class="provider-page__model-name">
                    {{ model.name || model.modelId }}
                  </span>
                  <span class="provider-page__model-id">{{ model.modelId }}</span>
                </div>

                <div class="provider-page__model-meta">
                  <span class="provider-page__model-context">
                    {{ formatContextSize(model.contextSize) }}
                  </span>
                  <el-tag
                    :type="providerStatusTagType(model.enabled)"
                    effect="light"
                    size="small"
                  >
                    {{ providerStatusLabel(model.enabled) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>

          <el-empty v-else description="暂无模型配置" :image-size="52" />
        </div>
      </template>

      <template #name="{ row }">
        <div class="provider-page__name-cell">
          <span class="provider-page__name">{{ row.name }}</span>
          <span class="provider-page__meta">ID #{{ row.id }}</span>
        </div>
      </template>

      <template #type="{ row }">
        <span>{{ providerTypeLabel(row.type) }}</span>
      </template>

      <template #status="{ row }">
        <el-tag :type="providerStatusTagType(row.enabled)" effect="light">
          {{ providerStatusLabel(row.enabled) }}
        </el-tag>
      </template>

      <template #health="{ row }">
        <div class="provider-page__health-cell">
          <el-tag
            :type="healthStatusTagType(normalizeHealthStatus(row.health?.status))"
            effect="light"
            size="small"
          >
            {{ normalizeHealthStatus(row.health?.status) }}
          </el-tag>
          <span class="provider-page__health-latency">
            {{ formatLatency(row.health) }}
          </span>
        </div>
      </template>

      <template #modelCount="{ row }">
        <div class="provider-page__model-count-cell">
          <el-button
            link
            type="primary"
            :loading="isDetailLoading(row.id) && !providerDetails[row.id]"
            @click="handleToggleModels(row)"
          >
            {{ getModelCountLabel(row) }}
          </el-button>
          <span class="provider-page__model-count-hint">
            {{ getModelCountHint(row) }}
          </span>
        </div>
      </template>

      <template #actions="{ row }">
        <div class="provider-page__actions-cell">
          <el-button
            link
            type="primary"
            :loading="testingProviderId === row.id"
            @click="handleTestConnection(row)"
          >
            连通性测试
          </el-button>
          <el-button link type="primary" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">
            删除
          </el-button>
        </div>
      </template>
    </WifyTable>

    <WifyFormDialog
      ref="dialogRef"
      v-model="dialogVisible"
      :title="dialogTitle"
      :rules="formRules"
      width="520px"
      label-width="100px"
      label-position="right"
      @submit="handleSubmit"
    >
      <template #default="{ formData }">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入提供商名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-select
            v-model="formData.type"
            placeholder="请选择提供商类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in providerTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            show-password
            :placeholder="apiKeyPlaceholder"
          />
        </el-form-item>

        <el-form-item label="Base URL" prop="baseUrl">
          <el-input
            v-model="formData.baseUrl"
            placeholder="请输入 API Base URL"
            clearable
          />
        </el-form-item>
      </template>
    </WifyFormDialog>
  </div>
</template>

<style scoped>
.provider-page {
  gap: 16px;
}

.provider-page__expand-panel {
  padding: 8px 0;
}

.provider-page__skeleton-row {
  height: 18px;
  margin-bottom: 12px;
  border-radius: 6px;
  background: rgba(218, 225, 242, 0.6);
}

.provider-page__skeleton-row--short {
  width: 62%;
  margin-bottom: 0;
}

.provider-page__model-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.provider-page__model-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border: 1px solid var(--wf-border-subtle);
  border-radius: 8px;
  background: rgba(248, 250, 255, 0.7);
}

.provider-page__model-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.provider-page__model-name {
  color: var(--wf-text-strong);
  font-weight: 700;
}

.provider-page__model-id {
  color: var(--wf-text-muted);
  font-size: 12px;
  word-break: break-all;
}

.provider-page__model-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.provider-page__model-context {
  color: var(--wf-text-secondary);
  font-size: 13px;
}

.provider-page__name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.provider-page__name {
  color: var(--wf-text-strong);
  font-weight: 700;
}

.provider-page__meta {
  color: var(--wf-text-muted);
  font-size: 12px;
}

.provider-page__health-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.provider-page__health-latency {
  color: var(--wf-text-secondary);
  font-size: 13px;
  white-space: nowrap;
}

.provider-page__model-count-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.provider-page__model-count-hint {
  color: var(--wf-text-muted);
  font-size: 12px;
  line-height: 1.2;
}

.provider-page__actions-cell {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .provider-page__model-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .provider-page__model-meta {
    justify-content: flex-start;
  }
}
</style>
