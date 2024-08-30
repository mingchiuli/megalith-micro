<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { AuthoritySys } from '@/type/entity'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { Status, ButtonAuth } from '@/type/entity'
import { checkButtonAuth, getButtonType, downloadData, getButtonTitle } from '@/utils/tools'
import { displayState } from '@/position/position'

const { fixSelection, fix } = displayState()
const multipleSelection = ref<AuthoritySys[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const delBtlStatus = ref(true)
const uploadPercentage = ref(0)
const showPercentage = ref(false)

let content = reactive<AuthoritySys[]>([])

const formRules = reactive<FormRules<Form>>({
  name: [
    { required: true, message: '请输入名字', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入唯一编码(白名单需要以whitelist开头)', trigger: 'blur' }
  ],
  remark: [
    { required: true, message: '请输入描述', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ],
  prototype: [
    { required: true, message: '请输入协议:http/ws', trigger: 'blur' }
  ],
  methodType: [
    { required: true, message: '请输入方法类型', trigger: 'blur' }
  ],
  routePattern: [
    { required: true, message: '请输入路由匹配', trigger: 'blur' }
  ],
  serviceName: [
    { required: true, message: '请输入调用域名', trigger: 'blur' }
  ]
})
const formRef = ref<FormInstance>()
type Form = {
  id?: number
  name: string
  code: string
  remark: string
  status: number
  prototype: string
  methodType: string
  routePattern: string
  serviceName: string
}

const form: Form = reactive({
  id: undefined,
  name: '',
  code: '',
  remark: '',
  status: 0,
  prototype: '',
  methodType: '',
  routePattern: '',
  serviceName: '',
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
  await queryAuthorities()
}

const download = async () => {
  await downloadData('/sys/authority/download', 'authorities', uploadPercentage, showPercentage)
}

const handleDelete = async (row: AuthoritySys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>('/sys/authority/delete', id)
  ElNotification({
    title: '操作成功',
    message: '删除成功',
    type: 'success',
  })
  await queryAuthorities()
}

const handleEdit = async (row: AuthoritySys) => {
  const data = await GET<AuthoritySys>(`/sys/authority/info/${row.id}`)
  form.code = data.code
  form.id = data.id
  form.name = data.name
  form.remark = data.remark
  form.status = data.status
  form.prototype = data.prototype
  form.methodType = data.methodType
  form.serviceName = data.serviceName
  form.routePattern = data.routePattern
  dialogVisible.value = true
}

const handleSelectionChange = (val: AuthoritySys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const queryAuthorities = async () => {
  loading.value = true
  content = await GET<AuthoritySys[]>('/sys/authority/list')
  loading.value = false
}

const handleClose = () => {
  clearForm()
  dialogVisible.value = false
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>('/sys/authority/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
      })
      clearForm()
      dialogVisible.value = false
      await queryAuthorities()
    }
  })
}

const clearForm = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.remark = ''
  form.status = 0
  form.prototype = ''
  form.methodType = ''
  form.serviceName = ''
  form.routePattern = ''
}

(async () => {
  await queryAuthorities()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_CREATE)">
      <el-button :type="getButtonType(ButtonAuth.SYS_AUTHORITY_CREATE)" size="large" @click="dialogVisible = true">{{
        getButtonTitle(ButtonAuth.SYS_AUTHORITY_CREATE) }}</el-button>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_BATCH_DEL)">
      <el-popconfirm title="确定批量删除?" @confirm="delBatch">
        <template #reference>
          <el-button :type="getButtonType(ButtonAuth.SYS_AUTHORITY_BATCH_DEL)" size="large" :disabled="delBtlStatus">{{
            getButtonTitle(ButtonAuth.SYS_AUTHORITY_BATCH_DEL) }}</el-button>
        </template>
      </el-popconfirm>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_DOWNLOAD)">
      <el-button :type="getButtonType(ButtonAuth.SYS_AUTHORITY_DOWNLOAD)" size="large" @click="download">{{
        getButtonTitle(ButtonAuth.SYS_AUTHORITY_DOWNLOAD) }}</el-button>
    </el-form-item>
    <el-form-item>
      <el-progress v-if="showPercentage" type="circle" :width="40" :percentage="uploadPercentage" />
    </el-form-item>
  </el-form>

  <el-table :data="content" style="width: 100%" border stripe @selection-change="handleSelectionChange"
    v-loading="loading">
    <el-table-column type="selection" :fixed="fixSelection" />
    <el-table-column label="接口名字" align="center" prop="name" min-width="300" />
    <el-table-column label="唯一编码" align="center" prop="code" min-width="180" />

    <el-table-column label="协议" align="center" prop="prototype" min-width="130" />
    <el-table-column label="方法类型" align="center" prop="methodType" min-width="130" />
    <el-table-column label="路由匹配" align="center" prop="routePattern" min-width="300" />
    <el-table-column label="请求服务" align="center" prop="serviceName" min-width="250" />

    <el-table-column label="描述" min-width="300" align="center" prop="remark" />

    <el-table-column label="状态" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">启用</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">停用</el-tag>
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

    <el-table-column :fixed="fix" label="操作" min-width="180" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_EDIT)">
          <el-button size="small" :type="getButtonType(ButtonAuth.SYS_AUTHORITY_EDIT)" @click="handleEdit(scope.row)">{{
            getButtonTitle(ButtonAuth.SYS_AUTHORITY_EDIT) }}</el-button>
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_DELETE)">
          <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" :type="getButtonType(ButtonAuth.SYS_AUTHORITY_DELETE)">{{
                getButtonTitle(ButtonAuth.SYS_AUTHORITY_DELETE) }}</el-button>
            </template>
          </el-popconfirm>
        </template>

      </template>
    </el-table-column>

  </el-table>

  <el-dialog v-model="dialogVisible" title="新增/编辑" width="600px" :before-close="handleClose">
    <el-form :model="form" :rules="formRules" ref="formRef">

      <el-form-item label="接口名字" label-width="100px" prop="name">
        <el-input v-model="form.name" maxlength="50" />
      </el-form-item>

      <el-form-item label="唯一编码" label-width="100px" prop="code">
        <el-input v-model="form.code" maxlength="50" />
      </el-form-item>

      <el-form-item label="描述" label-width="100px" prop="remark">
        <el-input v-model="form.remark" maxlength="50" />
      </el-form-item>

      <el-form-item label="协议" label-width="100px" prop="remark">
        <el-input v-model="form.prototype" maxlength="50" />
      </el-form-item>

      <el-form-item label="方法类型" label-width="100px" prop="remark">
        <el-input v-model="form.methodType" maxlength="50" />
      </el-form-item>

      <el-form-item label="路由匹配" label-width="100px" prop="remark">
        <el-input v-model="form.routePattern" maxlength="50" />
      </el-form-item>

      <el-form-item label="请求服务" label-width="100px" prop="remark">
        <el-input v-model="form.serviceName" maxlength="50" />
      </el-form-item>

      <el-form-item label="状态" label-width="100px" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value=Status.NORMAL>启用</el-radio>
          <el-radio :value=Status.BLOCK>禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="450px">
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<style scoped>
@import '@/assets/main.css';

.button-form .el-form-item {
  margin-right: 10px
}
</style>
