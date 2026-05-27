<script setup lang="ts">
import { Connection, Plus } from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue'
import type { FormRules } from 'element-plus'
import WifyFormDialog from '@/components/WifyFormDialog.vue'
import WifyTable from '@/components/WifyTable.vue'
import type {
  WifyFormDialogSubmitPayload,
  WifyTableColumn,
} from '@/components/types'
import { useConfirm } from '@/composables/useConfirm'
import { useViewportWidth } from '@/composables/useViewportWidth'
import { notifySuccess } from '@/utils/notify'
import type { PageQuery, PageResult } from '@/types/app'

type ProviderType = 'OpenAI' | 'Claude' | 'Gemini' | 'Ollama'
type ProviderStatus = 'enabled' | 'disabled'
const TABLE_COMPACT_BREAKPOINT = 1200

interface ProviderItem {
  id: number
  name: string
  type: ProviderType
  apiKey: string
  baseUrl: string
  status: ProviderStatus
  createdAt: string
}

interface ProviderFormModel {
  name: string
  type: ProviderType | ''
  apiKey: string
  baseUrl: string
}

const tableRef = ref<{ refresh: () => Promise<void> } | null>(null)
const dialogRef = ref<{ open: (data?: Partial<ProviderFormModel>) => Promise<void> } | null>(null)
const dialogVisible = ref(false)
const editingProviderId = ref<number | null>(null)
const { width: viewportWidth } = useViewportWidth()

const providerTypeOptions: ProviderType[] = ['OpenAI', 'Claude', 'Gemini', 'Ollama']

const providers = ref<ProviderItem[]>([
  {
    id: 1,
    name: 'OpenAI Production',
    type: 'OpenAI',
    apiKey: 'sk-openai-prod-001',
    baseUrl: 'https://api.openai.com/v1',
    status: 'enabled',
    createdAt: '2026-05-20 10:12',
  },
  {
    id: 2,
    name: 'Claude Internal',
    type: 'Claude',
    apiKey: 'sk-claude-int-002',
    baseUrl: 'https://api.anthropic.com',
    status: 'enabled',
    createdAt: '2026-05-18 14:35',
  },
  {
    id: 3,
    name: 'Gemini Sandbox',
    type: 'Gemini',
    apiKey: 'sk-gemini-sbx-003',
    baseUrl: 'https://generativelanguage.googleapis.com',
    status: 'disabled',
    createdAt: '2026-05-16 09:48',
  },
  {
    id: 4,
    name: 'Ollama GPU Node',
    type: 'Ollama',
    apiKey: 'sk-ollama-node-004',
    baseUrl: 'http://10.0.4.18:11434',
    status: 'enabled',
    createdAt: '2026-05-15 16:20',
  },
  {
    id: 5,
    name: 'OpenAI Backup',
    type: 'OpenAI',
    apiKey: 'sk-openai-bak-005',
    baseUrl: 'https://backup-openai.internal/v1',
    status: 'disabled',
    createdAt: '2026-05-12 11:05',
  },
])

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
      prop: 'type',
      width: 140,
    },
    {
      label: '状态',
      prop: 'status',
      width: 120,
      align: 'center',
      slot: 'status',
    },
    {
      label: '操作',
      width: 160,
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
    {
      label: '创建时间',
      prop: 'createdAt',
      width: 180,
    },
    baseColumns[3],
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
    { required: true, message: '请输入 API Key', trigger: 'blur' },
  ],
  baseUrl: [
    { required: true, message: '请输入 Base URL', trigger: 'blur' },
  ],
}

const dialogTitle = computed(() => (
  editingProviderId.value === null ? '新增提供商' : '编辑提供商'
))

watch(dialogVisible, (visible) => {
  if (!visible) {
    editingProviderId.value = null
  }
})

function createEmptyForm(): ProviderFormModel {
  return {
    name: '',
    type: '',
    apiKey: '',
    baseUrl: '',
  }
}

function formatNow() {
  const current = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')

  return [
    current.getFullYear(),
    pad(current.getMonth() + 1),
    pad(current.getDate()),
  ].join('-') + ` ${pad(current.getHours())}:${pad(current.getMinutes())}`
}

function sleep(duration = 180) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, duration)
  })
}

function statusTagType(status: ProviderStatus) {
  return status === 'enabled' ? 'success' : 'info'
}

function statusLabel(status: ProviderStatus) {
  return status === 'enabled' ? '启用' : '禁用'
}

function providerRowStyle() {
  return {
    height: '52px',
  }
}

async function fetchProviders(query: PageQuery): Promise<PageResult<ProviderItem>> {
  await sleep(220)

  const start = (query.page - 1) * query.pageSize
  const end = start + query.pageSize

  return {
    list: providers.value.slice(start, end),
    total: providers.value.length,
    page: query.page,
    pageSize: query.pageSize,
  }
}

async function openCreateDialog() {
  editingProviderId.value = null
  dialogVisible.value = true
  await dialogRef.value?.open(createEmptyForm())
}

async function openEditDialog(row: ProviderItem) {
  editingProviderId.value = row.id
  dialogVisible.value = true
  await dialogRef.value?.open({
    name: row.name,
    type: row.type,
    apiKey: row.apiKey,
    baseUrl: row.baseUrl,
  })
}

async function handleDelete(row: ProviderItem) {
  const confirmed = await useConfirm(
    `确认删除提供商「${row.name}」吗？`,
    async () => {
      await sleep(160)
      providers.value = providers.value.filter((item) => item.id !== row.id)
      return true
    },
    {
      title: '删除提供商',
      successMessage: '提供商已删除',
    },
  )

  if (confirmed) {
    await tableRef.value?.refresh()
  }
}

async function handleSubmit({
  model,
}: WifyFormDialogSubmitPayload<ProviderFormModel>) {
  await sleep(200)

  const payload = {
    name: model.name.trim(),
    type: model.type as ProviderType,
    apiKey: model.apiKey.trim(),
    baseUrl: model.baseUrl.trim(),
  }

  if (editingProviderId.value !== null) {
    providers.value = providers.value.map((item) => (
      item.id === editingProviderId.value
        ? { ...item, ...payload }
        : item
    ))
    notifySuccess('提供商已更新')
  } else {
    const nextId = Math.max(0, ...providers.value.map((item) => item.id)) + 1

    providers.value = [
      {
        id: nextId,
        ...payload,
        status: 'enabled',
        createdAt: formatNow(),
      },
      ...providers.value,
    ]
    notifySuccess('提供商已创建')
  }

  await tableRef.value?.refresh()
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
          使用统一的提供商列表管理名称、类型、鉴权信息和基础地址。当前页面基于通用表格与通用表单弹窗实现，
          方便后续直接替换为真实接口。
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
      :api="fetchProviders"
      :row-style="providerRowStyle"
      row-key="id"
    >
      <template #name="{ row }">
        <div class="provider-page__name-cell">
          <span class="provider-page__name">{{ row.name }}</span>
          <span class="provider-page__meta">ID #{{ row.id }}</span>
        </div>
      </template>

      <template #status="{ row }">
        <el-tag :type="statusTagType(row.status)" effect="light">
          {{ statusLabel(row.status) }}
        </el-tag>
      </template>

      <template #actions="{ row }">
        <div class="provider-page__actions-cell">
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
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            show-password
            placeholder="请输入 API Key"
          />
        </el-form-item>

        <el-form-item label="Base URL" prop="baseUrl">
          <el-input
            v-model="formData.baseUrl"
            placeholder="https://api.openai.com/v1"
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

.provider-page__actions-cell {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
}

.provider-page__actions-cell :deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>
