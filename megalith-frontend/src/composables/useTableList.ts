import { reactive, ref, toRefs } from 'vue'
import type { PageAdapter } from '@/type/entity'
import { POST } from '@/http/http'
import { downloadSQLData } from '@/utils/tools'
import { displayState } from '@/utils/position'

/**
 * Options for table list composable
 */
export interface TableListOptions<T> {
  /**
   * Query function that fetches data
   */
  queryFn: (params: { pageNumber: number; pageSize: number }) => Promise<PageAdapter<T>>
  /**
   * Delete endpoint URL
   */
  deleteEndpoint: string
  /**
   * Download endpoint URL
   */
  downloadEndpoint: string
  /**
   * Download file name
   */
  downloadFileName: string
  /**
   * Default page size (optional)
   */
  pageSize?: number
}

/**
 * Composable for table list operations
 * Handles pagination, selection, batch delete, download
 */
export const useTableList = <T extends { id: number }>(options: TableListOptions<T>) => {
  const { moreItems, fixSelection, fix } = displayState()

  // State
  const loading = ref(false)
  const multipleSelection = ref<T[]>([])
  const delBtlStatus = ref(true)
  const uploadPercentage = ref(0)
  const showPercentage = ref(false)

  // Pagination
  const defaultPageSize = options.pageSize ?? (moreItems.value ? 20 : 5)
  const page: PageAdapter<T> = reactive({
    content: [],
    totalElements: 0,
    pageSize: defaultPageSize,
    pageNumber: 1
  })
  const { content, totalElements, pageSize, pageNumber } = toRefs(page)

  /**
   * Handle selection change
   */
  const handleSelectionChange = (val: T[]) => {
    multipleSelection.value = val
    delBtlStatus.value = val.length === 0
  }

  /**
   * Handle page size change
   */
  const handleSizeChange = async (val: number) => {
    pageSize.value = val
    pageNumber.value = 1
    await queryData()
  }

  /**
   * Handle current page change
   */
  const handleCurrentChange = async (val: number) => {
    pageNumber.value = val
    await queryData()
  }

  /**
   * Query data from server
   */
  const queryData = async () => {
    loading.value = true
    try {
      const data = await options.queryFn({
        pageNumber: pageNumber.value,
        pageSize: pageSize.value
      })
      content.value = data.content
      totalElements.value = data.totalElements
    } finally {
      loading.value = false
    }
  }

  /**
   * Batch delete selected items
   */
  const delBatch = async () => {
    const ids = multipleSelection.value.map((item) => item.id)
    await POST<null>(options.deleteEndpoint, ids)
    ElNotification({
      title: '操作成功',
      message: '批量删除成功',
      type: 'success'
    })
    multipleSelection.value = []
    delBtlStatus.value = true
    await queryData()
  }

  /**
   * Delete single item
   */
  const handleDelete = async (row: T) => {
    await POST<null>(options.deleteEndpoint, [row.id])
    ElNotification({
      title: '操作成功',
      message: '删除成功',
      type: 'success'
    })
    await queryData()
  }

  /**
   * Download data as SQL file
   */
  const download = async () => {
    await downloadSQLData(
      options.downloadEndpoint,
      options.downloadFileName,
      uploadPercentage,
      showPercentage
    )
  }

  return {
    // State
    loading,
    multipleSelection,
    delBtlStatus,
    uploadPercentage,
    showPercentage,
    content,
    totalElements,
    pageSize,
    pageNumber,
    // Display helpers
    moreItems,
    fixSelection,
    fix,
    // Methods
    handleSelectionChange,
    handleSizeChange,
    handleCurrentChange,
    queryData,
    delBatch,
    handleDelete,
    download
  }
}