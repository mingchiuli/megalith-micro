<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { PageAdapter, RoleSys } from '@/type/entity'
import { reactive, ref, toRefs } from 'vue'

const dialogVisible = ref(false)
const delBtlStatus = ref(false)
const loading = ref(false)
const multipleSelection = ref<RoleSys[]>([])
  const page: PageAdapter<RoleSys> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 10,
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
  queryRoles()
}

const queryRoles = async () => {
  loading.value = true
  const data = await GET<PageAdapter<RoleSys>>(`/sys/role/roles?currentPage=${pageNumber.value}&size=${pageSize.value}`)
  content.value = data.content
  totalElements.value = data.totalElements
  loading.value = false
}

const handleSelectionChange = (val: RoleSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const handleCurrentChange = async (pageNo: number) => {
  pageNumber.value = pageNo
  await queryRoles()
}

(async () => {
  await queryRoles()
})()
</script>

<template>
<el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item>
      <el-button type="primary" size="large" @click="dialogVisible = true">新增</el-button>
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
    <el-table-column label="名字" width="150" align="center" prop="name" />
    <el-table-column label="唯一编码" width="200" align="center" prop="code" />

    <el-table-column label="描述" width="350" align="center" prop="remark" />

    <el-table-column label="状态" width="70" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === 0" type="success">公开</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === 1" type="danger">隐藏</el-tag>
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

    <el-table-column label="更新时间" width="180" align="center">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.updated }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="操作" fixed="right" width="250" align="center">
      <template #default="scope">
        <el-button size="small" type="success" @click="handleEdit(scope.row)">编辑</el-button>
        <el-button size="small" type="warning" @click="handleEdit(scope.row)">路由权限</el-button>
        <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>
  </el-table>

  <el-pagination @current-change="handleCurrentChange" layout="->, prev, pager, next" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements">
  </el-pagination>
</template>

<style scoped>
.button-form .el-form-item {
  margin-right: 10px;
}
</style>