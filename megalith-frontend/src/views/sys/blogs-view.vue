<script lang="ts" setup>
import { reactive, ref, toRefs } from 'vue'
import { GET, POST } from '@/http/http'
import { Status, type BlogSys, type PageAdapter } from '@/type/entity'
import router from '@/router'
import { Timer } from '@element-plus/icons-vue'
import { tabStore, displayStateStore } from '@/stores/store'
import { render } from '@/utils/tools'

const search = ref(false)
const input = ref('')
const multipleSelection = ref<BlogSys[]>([])
const delBtlStatus = ref(true)
const loading = ref(false)
const page: PageAdapter<BlogSys> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  "pageNumber": 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const delBatch = async () => {
  const args: number[] = []
  multipleSelection.value.forEach(item => {
    args.push(item.id)
  })
  await POST<null>('/sys/blog/delete', args)
  ElNotification({
    title: '操作成功',
    message: '批量删除成功',
    type: 'success',
  })
  multipleSelection.value = []
  await queryBLogs()
}

const handleDelete = async (row: BlogSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>('/sys/blog/delete', id)
  ElNotification({
    title: '操作成功',
    message: '删除成功',
    type: 'success',
  })
  await queryBLogs()
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
  const token = await GET<string>(`/sys/blog/lock/${row.id}`)
  ElNotification({
    title: '操作成功',
    message: token,
    type: 'success',
  })
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

const queryBLogs = async () => {
  if (search.value) {
    search.value = false
  }
  loading.value = true
  const data = await GET<PageAdapter<BlogSys>>(`/sys/blog/blogs?currentPage=${pageNumber.value}&size=${pageSize.value}`)
  content.value = data.content
  totalElements.value = data.totalElements
  loading.value = false
}

const clearQueryBLogs = async () => {
  pageNumber.value = 1
  await queryBLogs()
}

const searchBlogs = async () => {
  if (input.value) {
    search.value = true
    loading.value = true
    const data = await GET<PageAdapter<BlogSys>>(`search/sys/blogs?currentPage=${pageNumber.value}&size=${pageSize.value}&keywords=${input.value}`)
    page.content = data.content
    page.totalElements = data.totalElements
    loading.value = false
  }
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  pageNumber.value = 1
  if (input.value) {
    await searchBlogs()
  } else {
    await queryBLogs()
  }
}

const handleCurrentChange = async (val: number) => {
  pageNumber.value = val
  if (input.value) {
    await searchBlogs()
  } else {
    await queryBLogs()
  }
}

(async () => {
  tabStore().addTab({ title: '日志管理', name: 'system-blogs' })
  await queryBLogs()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item>
      <el-input v-model="input" placeholder="Please input" clearable maxlength="20" size="large" class="search-input"
        @clear="clearQueryBLogs" @keyup.enter="searchBlogs" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" @click="searchBlogs">搜索</el-button>
    </el-form-item>
    <el-form-item>
      <el-popconfirm title="确定批量删除?" @confirm="delBatch">
        <template #reference>
          <el-button type="danger" size="large" :disabled="delBtlStatus">批量删除</el-button>
        </template>
      </el-popconfirm>
    </el-form-item>
  </el-form>

  <el-table :data="content" style="width: 100%" border stripe @selection-change="handleSelectionChange"
    v-loading="loading">
    <el-table-column type="selection" />

    <el-table-column label="标题" align="center" prop="title" min-width="80" />
    <el-table-column label="摘要" align="center" min-width="200" >
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="top" min-width="200" >
          <template #default>
            <span> {{ scope.row.description }}</span>
          </template>
          <template #reference>
            <span>{{ scope.row.description.length > 20 ? scope.row.description.substring(0, 20) + '...' :
              scope.row.description }}</span>
          </template>
        </el-popover>
      </template>
    </el-table-column>

    <el-table-column label="内容" align="center" min-width="200" >
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="bottom" width="500px" :show-after="1000"
          popper-style="height: 300px;overflow: auto;">
          <template #default>
            <span v-html=render(scope.row.content) />
          </template>
          <template #reference>
            <span>{{ scope.row.content.length > 30 ? scope.row.content.substring(0, 30) + '...' : scope.row.content
            }}</span>
          </template>
        </el-popover>
      </template>
    </el-table-column>

    <el-table-column label="创建时间" min-width="180" align="center">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.created }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="更新时间" min-width="180" align="center">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.updated }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="阅读统计" align="center" min-width="180" v-if="!search">
      <template #default="scope">
        <div>总阅读数: {{ scope.row.readCount }}</div>
        <div>本周阅读数: {{ scope.row.recentReadCount }}</div>
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
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">隐藏</el-tag>
      </template>
    </el-table-column>

    <el-table-column :fixed="displayStateStore().fix" label="操作" min-width="300" align="center">
      <template #default="scope">
        <el-button size="small" type="primary" @click="handleCheck(scope.row)">查看</el-button>
        <el-button size="small" type="success" @click="handleEdit(scope.row)">编辑</el-button>
        <el-button size="small" type="warning" @click="handlePassword(scope.row)">密码</el-button>
        <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>
  </el-table>

  <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
    layout="->, total, sizes, prev, pager, next, jumper" :page-sizes="[5, 10, 20, 50]" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements" />
</template>

<style scoped>
.search-input {
  width: 200px
}

.button-form .el-form-item {
  margin-right: 10px
}

.el-pagination {
  margin-top: 10px
}
</style>