<script setup lang="ts">
import { onMounted, reactive, ref, useAttrs } from 'vue'
import type { PageQuery, PageResult } from '@/types/app'
import type { WifyTableColumn, WifyTableSlotProps } from '@/components/types'

defineOptions({
  name: 'WifyTable',
  inheritAttrs: false,
})

const props = withDefaults(defineProps<{
  columns: WifyTableColumn[]
  api: (query: PageQuery) => Promise<PageResult<any>>
  showPagination?: boolean
  pageSize?: number
}>(), {
  showPagination: true,
  pageSize: 20,
})

const attrs = useAttrs()
const tableData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive<PageQuery & { total: number }>({
  page: 1,
  pageSize: props.pageSize,
  total: 0,
})

const loadData = async () => {
  loading.value = true

  try {
    const result = await props.api({
      page: pagination.page,
      pageSize: pagination.pageSize,
    })

    tableData.value = result.list || []
    pagination.total = result.total || 0
    pagination.page = result.page || pagination.page
    pagination.pageSize = result.pageSize || pagination.pageSize
  } finally {
    loading.value = false
  }
}

const refresh = async () => {
  await loadData()
}

const handleCurrentChange = async (page: number) => {
  pagination.page = page
  await loadData()
}

const handleSizeChange = async (pageSize: number) => {
  pagination.page = 1
  pagination.pageSize = pageSize
  await loadData()
}

defineExpose({
  refresh,
})

onMounted(async () => {
  await loadData()
})
</script>

<template>
  <div class="wify-table">
    <el-table
      v-bind="attrs"
      v-loading="loading"
      :data="tableData"
      class="wify-table__inner"
    >
      <el-table-column
        v-for="column in columns"
        :key="`${String(column.prop || column.label)}-${column.slot || 'default'}`"
        :label="column.label"
        :prop="column.prop ? String(column.prop) : undefined"
        :width="column.width"
        :min-width="column.minWidth"
        :align="column.align"
        :fixed="column.fixed"
      >
        <template v-if="column.slot" #default="scope">
          <slot :name="column.slot" v-bind="scope as WifyTableSlotProps" />
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="暂无数据" :image-size="72" />
      </template>
    </el-table>

    <div v-if="showPagination" class="wify-table__pagination">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.wify-table {
  overflow: hidden;
  border: 1px solid var(--wf-border-subtle);
  border-radius: var(--wf-radius-lg);
  background: #ffffff;
  box-shadow: var(--wf-shadow-sm);
}

.wify-table__inner {
  border-radius: inherit;
}

.wify-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px 20px;
  border-top: 1px solid var(--wf-border-subtle);
  background: rgba(248, 250, 255, 0.88);
}
</style>
