<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { PageAdapter, UserSys } from '@/type/entity'
import { reactive, ref, toRefs } from 'vue'

const multipleSelection = ref<UserSys[]>([])
const visible = ref(false)
const loading = ref(false)
const delBtlStatus = ref(false)
const page: PageAdapter<UserSys> = reactive({
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
  await POST<null>('/sys/user/delete', args)
  ElNotification({
    title: '操作成功',
    message: '批量删除成功',
    type: 'success',
  })
  multipleSelection.value = []
  queryUsers()
}

const handleDelete = async (row: UserSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>('/sys/user/delete', id)
  ElNotification({
    title: '操作成功',
    message: '删除成功',
    type: 'success',
  })
  queryUsers()
}

const handleEdit = (row: UserSys) => {
  
}

const handleSelectionChange = (val: UserSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const queryUsers = async () => {
  loading.value = true
  const data = await GET<PageAdapter<UserSys>>(`/sys/user/page/${pageNumber.value}?size=${pageSize.value}`)
  content.value = data.content
  totalElements.value = data.totalElements
  loading.value = false
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  pageNumber.value = 1
  queryUsers()
}

const handleCurrentChange = async (val: number) => {
  pageNumber.value = val
  queryUsers()
};

(async () => {
  await queryUsers()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item>
      <el-button type="primary" size="large" @click="visible = true">新增</el-button>
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
    <el-table-column type="selection" width="55" />
    <el-table-column label="用户名" width="150" align="center" prop="username" />
    <el-table-column label="昵称" width="150" align="center" prop="nickname" />
    
    <el-table-column label="头像" width="70" align="center">
      <template #default="scope">
        <el-avatar size="default" :src="scope.row.avatar" />
      </template>
    </el-table-column>

    <el-table-column label="邮箱" width="150" align="center" prop="email" />
    <el-table-column label="手机号" width="150" align="center" prop="phone" />

    <el-table-column label="状态" width="70" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === 0" type="success">启用</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === 1" type="danger">停用</el-tag>
      </template>
    </el-table-column>

    <el-table-column label="创建时间" width="180" align="center">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.created }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="最后登录时间" width="180" align="center">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.lastLogin }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="操作" fixed="right" width="200" align="center">
      <template #default="scope">
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
    layout="->, total, sizes, prev, pager, next, jumper" :page-sizes="[5, 10, 20, 50]" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements">
  </el-pagination>
</template>

<style scoped>
.button-form .el-form-item {
  margin-right: 10px;
}

.el-pagination {
  margin-top: 10px;
}
</style>