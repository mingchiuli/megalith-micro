<script lang="ts" setup>
import { GET } from '@/http/http'
import type { BlogDelSys, PageAdapter } from '@/type/entity'
import { Status, ButtonAuth } from '@/type/entity'
import { reactive, ref, toRefs } from 'vue'
import { checkButtonAuth, getButtonType, render, getButtonTitle } from '@/utils/tools'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'

const { moreItems, fix } = displayState()
const loading = ref(false)
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

const queryDelBLogs = async () => {
  loading.value = true
  const url = buildQueryUrl(API_ENDPOINTS.BLOG_ADMIN.GET_DELETED_BLOGS, {
    currentPage: pageNumber.value,
    size: pageSize.value
  })
  const data = await GET<PageAdapter<BlogDelSys>>(url)
  content.value = data.content
  totalElements.value = data.totalElements
  loading.value = false
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
  await GET<PageAdapter<BlogDelSys>>(API_ENDPOINTS.BLOG_ADMIN.RECOVER_BLOG(row.idx))
  loading.value = false
  ElNotification({
    title: '操作成功',
    message: '恢复成功',
    type: 'success'
  })
  await queryDelBLogs()
}

;(async () => {
  await queryDelBLogs()
})()
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
    <el-table-column label="标题" align="center" prop="title" min-width="180" />
    <el-table-column label="摘要" align="center" min-width="200">
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

    <el-table-column label="内容" align="center" min-width="200">
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

    <el-table-column label="创建时间" min-width="180" align="center">
      <template #default="scope">
        <div class="time-icon">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.created }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="更新时间" min-width="180" align="center">
      <template #default="scope">
        <div class="time-icon">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.updated }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="阅读统计" align="center" min-width="180">
      <template #default="scope">
        <div>总阅读数: {{ scope.row.readCount }}</div>
      </template>
    </el-table-column>

    <el-table-column label="封面" align="center">
      <template #default="scope">
        <el-avatar shape="square" size="default" :src="scope.row.link" />
      </template>
    </el-table-column>

    <el-table-column label="状态" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">公开</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger"
          >隐藏</el-tag
        >
        <el-tag size="small" v-else-if="scope.row.status === Status.SENSITIVE_FILTER" type="warning"
          >打码</el-tag
        >
      </template>
    </el-table-column>

    <el-table-column :fixed="fix" label="操作" min-width="120" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_DELETE_RESUME)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_DELETE_RESUME)"
            @click="handleResume(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_DELETE_RESUME) }}</el-button
          >
        </template>
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
