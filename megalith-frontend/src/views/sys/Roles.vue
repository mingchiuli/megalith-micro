<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { PageAdapter, RoleSys } from '@/type/entity'
import type { FormInstance, FormRules } from 'element-plus';
import { reactive, ref, toRefs } from 'vue'

const dialogVisible = ref(false)
const delBtlStatus = ref(false)
const loading = ref(false)
const multipleSelection = ref<RoleSys[]>([])
const page: PageAdapter<RoleSys> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  "pageNumber": 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const formRules = reactive<FormRules<Form>>({
  name: [
    { required: true, message: '请输入名字', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入唯一编码', trigger: 'blur' }
  ],
  remark: [
    { required: true, message: '请输入描述', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ]
})

const formRef = ref<FormInstance>()

type Form = {
  id?: number
  name: string
  code: string
  remark: string
  status: number
}

const form: Form = reactive({
  id: undefined,
  name: '',
  code: '',
  remark: '',
  status: 0
})

const resetForm = (ref: FormInstance) => ref.resetFields()

const clearForm = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.remark = ''
  form.status = 0
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>('/sys/role/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
      })
      clearForm()
      dialogVisible.value = false
      pageNumber.value = 1
      await queryRoles()
    }
  })
}

const infoHandleClose = () => {
  clearForm()
  dialogVisible.value = false
}

const handleEdit = async (row: RoleSys) => {
  const data = await GET<RoleSys>(`/sys/role/info/${row.id}`)
  form.id = data.id
  form.name = data.name
  form.code = data.code
  form.remark = data.remark
  form.status = data.status
  dialogVisible.value = true
}

const handlePermission = (row: RoleSys) => {

}

const delBatch = async () => {
  const args: number[] = []
  multipleSelection.value.forEach(item => {
    args.push(item.id)
  })
  await POST<null>('/sys/role/delete', args)
  ElNotification({
    title: '操作成功',
    message: '批量删除成功',
    type: 'success',
  })
  multipleSelection.value = []
  await queryRoles()
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

const handleDelete = async (row: RoleSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>('/sys/role/delete', id)
  ElNotification({
    title: '操作成功',
    message: '删除成功',
    type: 'success',
  })
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
        <el-button size="small" type="warning" @click="handlePermission(scope.row)">路由权限</el-button>
        <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>
  </el-table>

  <el-pagination @current-change="handleCurrentChange" layout="->, prev, pager, next" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements" />

  <el-dialog title="新增/编辑" v-model="dialogVisible" width="600px" :before-close="infoHandleClose">
    <el-form :model="form" :rules="formRules" label-width="100px" ref="formRef">
      <el-form-item label="名字" prop="name">
        <el-input v-model="form.name"></el-input>
      </el-form-item>

      <el-form-item label="唯一编码" prop="code">
        <el-input v-model="form.code"></el-input>
      </el-form-item>

      <el-form-item label="描述" prop="remark">
        <el-input v-model="form.remark" placeholder="以https/http开头"></el-input>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label=0>启用</el-radio>
          <el-radio :label=1>禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="400px">
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
        <el-button @click="resetForm(formRef!)">Reset</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<style scoped>
.button-form .el-form-item {
  margin-right: 10px;
}
</style>