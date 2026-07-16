<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import { Status, type BlogSys, type PageAdapter, ButtonAuth } from '@/type/entity'
import router from '@/router'
import { Timer } from '@element-plus/icons-vue'
import {
  render,
  checkButtonAuth,
  getButtonType,
  downloadSQLData,
  getButtonTitle
} from '@/utils/tools'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildCommonUrls } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { fixSelection, fix, moreItems } = displayState()
const input = ref('')
const multipleSelection = ref<BlogSys[]>([])
const delBtlStatus = ref(true)
const loading = ref(false)
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const dateTimeScope = ref(['', ''])
const status = ref<string | number>('')
const statusOptions = computed(() => [
  {
    value: 0,
    label: t('common.public')
  },
  {
    value: 1,
    label: t('common.hidden')
  },
  {
    value: 2,
    label: t('common.masked')
  },
  {
    value: 3,
    label: t('common.draft')
  }
])

const page: PageAdapter<BlogSys> = reactive({
  content: [],
  totalElements: 0,
  pageSize: moreItems.value ? 20 : 5,
  pageNumber: 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const delBatch = async () => {
  const args: number[] = []
  multipleSelection.value.forEach((item) => {
    args.push(item.id)
  })
  await POST<null>(API_ENDPOINTS.BLOG_ADMIN.DELETE_BLOGS, args)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.batchDeleteSuccess'),
    type: 'success'
  })
  multipleSelection.value = []
  await searchBlogs()
}

const handleDelete = async (row: BlogSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>(API_ENDPOINTS.BLOG_ADMIN.DELETE_BLOGS, id)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.deleteSuccess'),
    type: 'success'
  })
  await searchBlogs()
}

const handleEdit = (row: BlogSys) => {
  router.push({
    name: 'system-edit',
    query: {
      id: row.id
    }
  })
}

const handlePassword = async (row: BlogSys) => {
  const token = await GET<string>(API_ENDPOINTS.BLOG_ADMIN.LOCK_BLOG(row.id))
  ElNotification({
    title: t('common.operationSuccess'),
    message: token,
    type: 'success'
  })
}

const download = async () => {
  const url = buildCommonUrls.blogDownload({
    keywords: input.value,
    createStart: dateTimeScope.value[0],
    createEnd: dateTimeScope.value[1]
  })
  await downloadSQLData(url, 'blogs', uploadPercentage, showPercentage)
}

const handleCheck = (row: BlogSys) => {
  router.push({
    name: 'blog',
    params: {
      id: row.id
    }
  })
}

const handleSelectionChange = (val: BlogSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const clearQueryBlogs = async () => {
  pageNumber.value = 1
  await searchBlogs()
}

const clearSelect = async () => {
  status.value = ''
  await searchBlogs()
}

const searchBlogsAction = () => {
  pageNumber.value = 1
  searchBlogs()
}

const searchBlogs = async () => {
  loading.value = true
  try {
    const url = buildCommonUrls.blogQuery({
      currentPage: pageNumber.value,
      size: pageSize.value,
      keywords: input.value,
      createStart: dateTimeScope.value[0],
      createEnd: dateTimeScope.value[1],
      status: status.value
    })
    const data = await GET<PageAdapter<BlogSys>>(url)
    page.content = data.content
    page.totalElements = data.totalElements
  } finally {
    loading.value = false
  }
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  pageNumber.value = 1
  await searchBlogs()
}

const handleCurrentChange = async (val: number) => {
  pageNumber.value = val
  await searchBlogs()
}

const clearDatePicker = async () => {
  dateTimeScope.value = ['', '']
  await searchBlogs()
}

;(async () => {
  await searchBlogs()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item>
      <el-input
        v-model="input"
        :placeholder="t('common.input')"
        clearable
        maxlength="20"
        size="large"
        class="search-input"
        @clear="clearQueryBlogs"
        @keyup.enter="searchBlogsAction"
      />
    </el-form-item>
    <el-form-item>
      <el-date-picker
        v-model="dateTimeScope"
        value-format="YYYY-MM-DDTHH:mm:ss"
        size="large"
        type="datetimerange"
        @clear="clearDatePicker"
        :range-separator="t('admin.dateSeparator')"
        :start-placeholder="t('admin.startDate')"
        :end-placeholder="t('admin.endDate')"
      />
    </el-form-item>
    <el-form-item>
      <el-select
        v-model="status"
        clearable
        :placeholder="t('common.status')"
        size="large"
        @clear="clearSelect"
        style="width: 100px"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_SEARCH)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_BLOG_SEARCH)"
        size="large"
        @click="searchBlogsAction"
        >{{ getButtonTitle(ButtonAuth.SYS_BLOG_SEARCH) }}</el-button
      >
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_BATCH_DEL)">
      <el-popconfirm :title="t('common.batchDeleteConfirm')" @confirm="delBatch">
        <template #reference>
          <el-button
            :type="getButtonType(ButtonAuth.SYS_BLOG_BATCH_DEL)"
            size="large"
            :disabled="delBtlStatus"
            >{{ getButtonTitle(ButtonAuth.SYS_BLOG_BATCH_DEL) }}</el-button
          >
        </template>
      </el-popconfirm>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_DOWNLOAD)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_BLOG_DOWNLOAD)"
        size="large"
        @click="download"
        >{{ getButtonTitle(ButtonAuth.SYS_BLOG_DOWNLOAD) }}</el-button
      >
    </el-form-item>
    <el-form-item>
      <el-progress v-if="showPercentage" type="circle" :width="40" :percentage="uploadPercentage" />
    </el-form-item>
  </el-form>

  <el-table
    :data="content"
    :style="{ width: '100%' }"
    border
    stripe
    @selection-change="handleSelectionChange"
    v-loading="loading"
  >
    <el-table-column type="selection" :fixed="fixSelection" />

    <el-table-column :label="t('common.title')" align="center" prop="title" min-width="180" />
    <el-table-column :label="t('common.description')" align="center" min-width="200">
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="top" min-width="200">
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
            <span v-html="render(scope.row.content)" />
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
        <div class="time-icon">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.created }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.updatedAt')" min-width="180" align="center">
      <template #default="scope">
        <div class="time-icon">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.updated }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column :label="t('admin.readStats')" align="center" min-width="180">
      <template #default="scope">
        <div>{{ t('blog.totalReadCount', { count: scope.row.readCount }) }}</div>
        <div>{{ t('blog.weeklyReadCount', { count: scope.row.recentReadCount }) }}</div>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.cover')" align="center">
      <template #default="scope">
        <el-avatar shape="square" size="default" :src="scope.row.link" />
      </template>
    </el-table-column>

    <el-table-column :label="t('common.status')" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">{{
          t('common.public')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">{{
          t('common.hidden')
        }}</el-tag>
        <el-tag
          size="small"
          v-else-if="scope.row.status === Status.SENSITIVE_FILTER"
          type="warning"
          >{{ t('common.masked') }}</el-tag
        >
        <el-tag size="small" v-else-if="scope.row.status === Status.DRAFT" type="info">{{
          t('common.draft')
        }}</el-tag>
      </template>
    </el-table-column>

    <!-- @vue-generic {BlogSys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" min-width="300" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_CHECK)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_BLOG_CHECK)"
            @click="handleCheck(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_BLOG_CHECK) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_EDIT)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_BLOG_EDIT)"
            @click="handleEdit(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_BLOG_EDIT) }}</el-button
          >
        </template>

        <template
          v-if="scope.row.status === Status.BLOCK && checkButtonAuth(ButtonAuth.SYS_BLOG_PASSWORD)"
        >
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_BLOG_PASSWORD)"
            @click="handlePassword(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_BLOG_PASSWORD) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_DELETE)">
          <el-popconfirm :title="t('common.deleteConfirm')" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" :type="getButtonType(ButtonAuth.SYS_BLOG_DELETE)">{{
                getButtonTitle(ButtonAuth.SYS_BLOG_DELETE)
              }}</el-button>
            </template>
          </el-popconfirm>
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

.search-input {
  width: 200px;
}

.button-form .el-form-item {
  margin-right: 10px;
}

.el-pagination {
  margin-top: 10px;
}
</style>
