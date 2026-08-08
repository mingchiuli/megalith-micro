<script lang="ts" setup>
import { useHttp } from '@/http/http'
import type { BlogDelSys, PageAdapter } from '@/type/entity'
import { ButtonAuth } from '@/type/entity'
import { render } from '@/utils/markdown'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'
import { useLatestRequest, useUniversalData } from '@/composables'

const { t } = useI18n()
const { GET, POST } = useHttp()

const { moreItems, fix } = displayState()
const loading = ref(true)
const { runLatest } = useLatestRequest(loading)
const multipleSelection = ref<BlogDelSys[]>([])
const delBtlStatus = ref(false)
const page: PageAdapter<BlogDelSys> = reactive({
  content: [],
  totalElements: 0,
  pageSize: moreItems.value ? 20 : 5,
  pageNumber: 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const handleSelectionChange = (val: BlogDelSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const fetchDeletedBlogs = async () => {
  const url = buildQueryUrl(API_ENDPOINTS.BLOG_ADMIN.GET_DELETED_BLOGS, {
    currentPage: pageNumber.value,
    size: pageSize.value
  })
  return GET<PageAdapter<BlogDelSys>>(url)
}

const applyDeletedBlogs = (data: PageAdapter<BlogDelSys>) => {
  content.value = data.content
  totalElements.value = data.totalElements
}

const queryDelBLogs = async () => {
  await runLatest(fetchDeletedBlogs, applyDeletedBlogs)
}

const handleCurrentChange = async (pageNo: number) => {
  pageNumber.value = pageNo
  await queryDelBLogs()
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  pageNumber.value = 1
  await queryDelBLogs()
}

const handleResume = async (row: BlogDelSys) => {
  loading.value = true
  try {
    await POST<null>(API_ENDPOINTS.BLOG_ADMIN.RECOVER_BLOG(row.idx), {})
  } finally {
    loading.value = false
  }
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.restoreSuccess'),
    type: 'success'
  })
  await queryDelBLogs()
}

useUniversalData('admin:deleted-blogs', fetchDeletedBlogs, applyDeletedBlogs, { loading })
</script>

<template>
  <el-table
    :data="content"
    :style="{ width: '100%' }"
    border
    stripe
    @selection-change="handleSelectionChange"
    v-loading="loading"
  >
    <el-table-column :label="t('common.title')" align="center" prop="title" min-width="180" />
    <el-table-column :label="t('common.description')" align="center" min-width="200">
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="top" width="auto">
          <template #default>
            <span> {{ scope.row.description }}</span>
          </template>
          <template #reference>
            <span>{{
              scope.row.description.length > 20
                ? scope.row.description.substring(0, 20) + '...'
                : scope.row.description
            }}</span>
          </template>
        </el-popover>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.content')" align="center" min-width="200">
      <template #default="scope">
        <el-popover
          effect="light"
          trigger="hover"
          placement="bottom"
          width="500px"
          :show-after="1000"
          popper-style="height: 300px;overflow: auto;"
        >
          <template #default>
            <span v-html="render(scope.row.content)"></span>
          </template>
          <template #reference>
            <span>{{
              scope.row.content.length > 30
                ? scope.row.content.substring(0, 30) + '...'
                : scope.row.content
            }}</span>
          </template>
        </el-popover>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.createdAt')" min-width="180" align="center">
      <template #default="scope">
        <TimeColumn :time="scope.row.created" />
      </template>
    </el-table-column>

    <el-table-column :label="t('common.updatedAt')" min-width="180" align="center">
      <template #default="scope">
        <TimeColumn :time="scope.row.updated" />
      </template>
    </el-table-column>

    <el-table-column :label="t('admin.readStats')" align="center" min-width="180">
      <template #default="scope">
        <div>{{ t('blog.totalReadCount', { count: scope.row.readCount }) }}</div>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.cover')" align="center">
      <template #default="scope">
        <el-avatar shape="square" size="default" :src="scope.row.link" />
      </template>
    </el-table-column>

    <el-table-column :label="t('common.status')" align="center">
      <template #default="scope">
        <StatusTag :status="scope.row.status" />
      </template>
    </el-table-column>

    <!-- @vue-generic {BlogDelSys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" min-width="120" align="center">
      <template #default="scope">
        <AuthButton
          :auth="ButtonAuth.SYS_DELETE_RESUME"
          size="small"
          @click="handleResume(scope.row)"
        />
      </template>
    </el-table-column>
  </el-table>

  <el-pagination
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
    layout="->, total, sizes, prev, pager, next, jumper"
    :page-sizes="[5, 10, 20, 50]"
    :current-page="pageNumber"
    :page-size="pageSize"
    :total="totalElements"
  />
</template>

<style scoped>
@import '@/assets/main.css';

.el-pagination {
  margin-top: 10px;
}
</style>
