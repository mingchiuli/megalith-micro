<script lang="ts" setup>
import { reactive, ref, toRefs } from 'vue'
import { GET, POST } from '@/http/http'
import type { BlogsSys, PageAdapter } from '@/type/entity'
import editor from 'mavon-editor'
import router from '@/router'
import { Timer } from '@element-plus/icons-vue'

const mavonEditor: any = editor.mavonEditor
const md = mavonEditor.getMarkdownIt()
const input = ref('')
const multipleSelection = ref<BlogsSys[]>([])
const delBtlStatus = ref(false)
const loading = ref(false)

let page: PageAdapter<BlogsSys> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  "pageNumber": 1
})

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
  queryBLogs()
}

const handleEdit = (row: BlogsSys) => {
  console.log(row)
}
const handleDelete = async (row: BlogsSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>('/sys/blog/delete', id)
  ElNotification({
    title: '操作成功',
    message: '删除成功',
    type: 'success',
  })
  queryBLogs()
}

const handleCheck = (row: BlogsSys) => {
  router.push({
    name: 'blog',
    params: {
      id: row.id
    }
  })
}

const handleSelectionChange = (val: BlogsSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const queryBLogs = async () => {
  loading.value = true
  const data = await GET<PageAdapter<BlogsSys>>(`/sys/blog/blogs?currentPage=${pageNumber.value}&size=${pageSize.value}`)
  content.value = data.content
  totalElements.value = data.totalElements
  loading.value = false
}

const searchBlogs = async () => {
  loading.value = true
  const data = await GET<PageAdapter<BlogsSys>>(`search/sys/blogs?currentPage=${pageNumber.value}&size=${pageSize.value}&keywords=${input.value}`)
  page.content = data.content
  page.totalElements = data.totalElements
  loading.value = false
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
};

(async () => {
  await queryBLogs()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button">
    <el-form-item>
      <el-input v-model="input" placeholder="Please input" clearable maxlength="20" size="large" class="search-input"
        @clear="queryBLogs" @keyup.enter="searchBlogs" />
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
    :loading="loading">
    <el-table-column type="selection" width="55" />
    <el-table-column label="id" width="80" align="center">
      <template #default="scope">
        <span>{{ scope.row.id }}</span>
      </template>
    </el-table-column>

    <el-table-column label="标题" width="150" align="center">
      <template #default="scope">
        <span>{{ scope.row.title }}</span>
      </template>
    </el-table-column>

    <el-table-column label="摘要" width="200" align="center">
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="top" width="auto">
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

    <el-table-column label="内容" width="300" align="center">
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="bottom" width="auto" :show-after="1000">
          <template #default>
            <span v-html=md.render(scope.row.content)></span>
          </template>
          <template #reference>
            <span>{{ scope.row.content.length > 30 ? scope.row.content.substring(0, 30) + '...' : scope.row.content
            }}</span>
          </template>
        </el-popover>
      </template>
    </el-table-column>

    <el-table-column label="创建时间" width="180" align="center">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.created.replace('T', ' ') }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="阅读统计" width="150" align="center">
      <template #default="scope">
        <div>总阅读数: {{ scope.row.readCount }}</div>
        <div>本周阅读数: {{ scope.row.recentReadCount }}</div>
      </template>
    </el-table-column>

    <el-table-column label="状态" width="70" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === 0" type="success">公开</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === 1" type="danger">隐藏</el-tag>
      </template>
    </el-table-column>

    <el-table-column label="操作" fixed="right" width="200" align="center">
      <template #default="scope">
        <el-button size="small" type="primary" @click="handleCheck(scope.row)">查看</el-button>
        <el-button size="small" type="success" @click="handleEdit(scope.row)">编辑</el-button>
        <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>
  </el-table>

  <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
    layout="total, sizes, prev, pager, next, jumper" :page-sizes="[5, 10, 20, 50]" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements">
  </el-pagination>
</template>

<style scoped>
.search-input {
  width: 200px;
}

.button .el-form-item {
  margin-right: 10px;
}

.el-pagination {
  float: right;
  margin-top: 10px;
}
</style>