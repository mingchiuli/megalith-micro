<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { AuthoritySys } from '@/type/entity'
import type { FormInstance, FormRules } from 'element-plus'
import { Status, ButtonAuth, AuthStatus } from '@/type/entity'
import { checkButtonAuth, getButtonType, downloadSQLData, getButtonTitle } from '@/utils/tools'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { fixSelection, fix } = displayState()
const multipleSelection = ref<AuthoritySys[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const delBtlStatus = ref(true)
const uploadPercentage = ref(0)
const showPercentage = ref(false)

const content = ref<AuthoritySys[]>([])

const formRules = computed<FormRules<Form>>(() => ({
  code: [
    {
      required: true,
      message: t('validation.enter', { field: t('admin.permissionCode') }),
      trigger: 'blur'
    }
  ],
  remark: [
    {
      required: true,
      message: t('validation.enter', { field: t('admin.remark') }),
      trigger: 'blur'
    }
  ],
  status: [
    {
      required: true,
      message: t('validation.select', { field: t('common.status') }),
      trigger: 'blur'
    }
  ],
  type: [
    {
      required: true,
      message: t('validation.select', { field: t('common.type') }),
      trigger: 'blur'
    }
  ],
  prototype: [
    {
      required: true,
      message: t('validation.select', { field: t('admin.protocol') }),
      trigger: 'blur'
    }
  ],
  methodType: [
    {
      required: true,
      message: t('validation.select', { field: t('admin.methodType') }),
      trigger: 'blur'
    }
  ],
  routePattern: [
    {
      required: true,
      message: t('validation.enter', { field: t('admin.routePattern') }),
      trigger: 'blur'
    }
  ],
  serviceHost: [
    {
      required: true,
      message: t('validation.select', { field: t('admin.service') }),
      trigger: 'blur'
    }
  ],
  servicePort: [
    { required: true, message: t('validation.enter', { field: t('admin.port') }), trigger: 'blur' }
  ]
}))
const formRef = ref<FormInstance>()
type Form = {
  id?: number
  code: string
  remark: string
  status: Status
  type: AuthStatus
  prototype: string
  methodType: string
  routePattern: string
  serviceHost: string
  servicePort: number
}

const form: Form = reactive({
  id: undefined,
  code: '',
  remark: '',
  status: 0,
  type: 0,
  prototype: '',
  methodType: '',
  routePattern: '',
  serviceHost: '',
  servicePort: 8081
})

const delBatch = async () => {
  const args: number[] = []
  multipleSelection.value.forEach((item) => {
    args.push(item.id)
  })
  await POST<null>(API_ENDPOINTS.AUTHORITY_ADMIN.DELETE_AUTHORITIES, args)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.batchDeleteSuccess'),
    type: 'success'
  })
  multipleSelection.value = []
  await queryAuthorities()
}

const download = async () => {
  await downloadSQLData(
    API_ENDPOINTS.AUTHORITY_ADMIN.DOWNLOAD_AUTHORITIES,
    'authorities',
    uploadPercentage,
    showPercentage
  )
}

const handleDelete = async (row: AuthoritySys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>(API_ENDPOINTS.AUTHORITY_ADMIN.DELETE_AUTHORITIES, id)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.deleteSuccess'),
    type: 'success'
  })
  await queryAuthorities()
}

const handleEdit = async (row: AuthoritySys) => {
  const data = await GET<AuthoritySys>(API_ENDPOINTS.AUTHORITY_ADMIN.GET_AUTHORITY_INFO(row.id))
  Object.assign(form, data)
  dialogVisible.value = true
}

const handleSelectionChange = (val: AuthoritySys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const queryAuthorities = async () => {
  loading.value = true
  try {
    content.value = await GET<AuthoritySys[]>(API_ENDPOINTS.AUTHORITY_ADMIN.GET_AUTHORITIES)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  clearForm()
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid) => {
    if (valid) {
      await POST<null>(API_ENDPOINTS.AUTHORITY_ADMIN.SAVE_AUTHORITY, form)
      ElNotification({
        title: t('common.operationSuccess'),
        message: t('common.editSuccess'),
        type: 'success'
      })
      clearForm()
      dialogVisible.value = false
      await queryAuthorities()
    }
  })
}

const clearForm = () => {
  form.id = undefined
  form.code = ''
  form.remark = ''
  form.status = 0
  form.type = 0
  form.prototype = ''
  form.methodType = ''
  form.serviceHost = ''
  form.servicePort = 8081
  form.routePattern = ''
}

;(async () => {
  await queryAuthorities()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_CREATE)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_AUTHORITY_CREATE)"
        size="large"
        @click="dialogVisible = true"
        >{{ getButtonTitle(ButtonAuth.SYS_AUTHORITY_CREATE) }}</el-button
      >
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_BATCH_DEL)">
      <el-popconfirm :title="t('common.batchDeleteConfirm')" @confirm="delBatch">
        <template #reference>
          <el-button
            :type="getButtonType(ButtonAuth.SYS_AUTHORITY_BATCH_DEL)"
            size="large"
            :disabled="delBtlStatus"
            >{{ getButtonTitle(ButtonAuth.SYS_AUTHORITY_BATCH_DEL) }}</el-button
          >
        </template>
      </el-popconfirm>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_DOWNLOAD)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_AUTHORITY_DOWNLOAD)"
        size="large"
        @click="download"
        >{{ getButtonTitle(ButtonAuth.SYS_AUTHORITY_DOWNLOAD) }}</el-button
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
    <el-table-column
      :label="t('admin.permissionCode')"
      align="center"
      prop="code"
      min-width="300"
    />

    <el-table-column :label="t('admin.protocol')" align="center" prop="prototype" min-width="130" />
    <el-table-column
      :label="t('admin.methodType')"
      align="center"
      prop="methodType"
      min-width="130"
    />
    <el-table-column
      :label="t('admin.routePattern')"
      align="center"
      prop="routePattern"
      min-width="300"
    />
    <el-table-column
      :label="t('admin.service')"
      align="center"
      prop="serviceHost"
      min-width="250"
    />
    <el-table-column :label="t('admin.port')" align="center" prop="servicePort" min-width="100" />

    <el-table-column :label="t('admin.remark')" min-width="300" align="center" prop="remark" />

    <el-table-column :label="t('common.status')" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">{{
          t('common.enabled')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">{{
          t('common.inactive')
        }}</el-tag>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.type')" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.type === AuthStatus.WHITE_LIST" type="success">{{
          t('admin.whiteList')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.type === AuthStatus.NEED_AUTH" type="warning">{{
          t('admin.authRequired')
        }}</el-tag>
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

    <!-- @vue-generic {AuthoritySys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" min-width="180" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_EDIT)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_AUTHORITY_EDIT)"
            @click="handleEdit(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_AUTHORITY_EDIT) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_DELETE)">
          <el-popconfirm :title="t('common.deleteConfirm')" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" :type="getButtonType(ButtonAuth.SYS_AUTHORITY_DELETE)">{{
                getButtonTitle(ButtonAuth.SYS_AUTHORITY_DELETE)
              }}</el-button>
            </template>
          </el-popconfirm>
        </template>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog
    v-model="dialogVisible"
    :title="t('common.addEdit')"
    width="600px"
    :before-close="handleClose"
  >
    <el-form :model="form" :rules="formRules" ref="formRef">
      <el-form-item :label="t('admin.permissionCode')" label-width="100px" prop="code">
        <el-input v-model="form.code" maxlength="50" />
      </el-form-item>

      <el-form-item :label="t('admin.remark')" label-width="100px" prop="remark">
        <el-input v-model="form.remark" maxlength="50" />
      </el-form-item>

      <el-form-item :label="t('admin.protocol')" label-width="100px" prop="prototype">
        <el-select
          v-model="form.prototype"
          :placeholder="t('validation.select', { field: t('admin.protocol') })"
          style="width: 100%"
        >
          <el-option label="http" value="http" />
          <el-option label="ws" value="ws" />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('admin.methodType')" label-width="100px" prop="methodType">
        <el-select
          v-model="form.methodType"
          :placeholder="t('validation.select', { field: t('admin.methodType') })"
          style="width: 100%"
        >
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('admin.routePattern')" label-width="100px" prop="routePattern">
        <el-input v-model="form.routePattern" maxlength="50" />
      </el-form-item>

      <el-form-item :label="t('admin.service')" label-width="100px" prop="serviceHost">
        <el-select
          v-model="form.serviceHost"
          :placeholder="t('validation.select', { field: t('admin.service') })"
          style="width: 100%"
        >
          <el-option label="micro-blog" value="micro-blog" />
          <el-option label="micro-user" value="micro-user" />
          <el-option label="micro-auth" value="micro-auth" />
          <el-option label="micro-search" value="micro-search" />
          <el-option label="micro-sync-rs" value="micro-sync-rs" />
          <el-option label="micro-exhibit" value="micro-exhibit" />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('admin.port')" label-width="100px" prop="servicePort">
        <el-input v-model="form.servicePort" maxlength="50" />
      </el-form-item>

      <el-form-item :label="t('common.status')" label-width="100px" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="Status.NORMAL">{{ t('common.enabled') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="t('common.type')" label-width="100px" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio :value="AuthStatus.WHITE_LIST">{{ t('admin.whiteList') }}</el-radio>
          <el-radio :value="AuthStatus.NEED_AUTH">{{ t('admin.authRequired') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_AUTHORITY_SAVE)"
          @click="submitForm(formRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_AUTHORITY_SAVE) }}</el-button
        >
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
@import '@/assets/main.css';

.button-form .el-form-item {
  margin-right: 10px;
}
</style>
