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
</style>