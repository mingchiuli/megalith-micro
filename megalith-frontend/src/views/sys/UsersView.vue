<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { PageAdapter, RoleSys, UserSys } from '@/type/entity'
import type { FormInstance, FormRules } from 'element-plus'
import { Status, ButtonAuth } from '@/type/entity'
import { checkButtonAuth, getButtonType, downloadSQLData, getButtonTitle } from '@/utils/tools'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildCommonUrls, buildQueryUrl } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { moreItems, fixSelection, fix } = displayState()
const multipleSelection = ref<UserSys[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const delBtlStatus = ref(true)
const roleList = ref<RoleSys[]>([])
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const page: PageAdapter<UserSys> = reactive({
  content: [],
  totalElements: 0,
  pageSize: moreItems.value ? 20 : 5,
  pageNumber: 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const formRules = computed<FormRules<Form>>(() => ({
  username: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.username') }),
      trigger: 'blur'
    }
  ],
  nickname: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.nickname') }),
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: false,
      message: t('validation.enter', { field: t('auth.password') }),
      trigger: 'blur'
    }
  ],
  avatar: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.avatarUrl') }),
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: t('validation.enter', { field: t('auth.email') }), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('validation.enter', { field: t('auth.phone') }), trigger: 'blur' }
  ],
  roles: [
    { required: true, message: t('validation.select', { field: t('auth.role') }), trigger: 'blur' }
  ],
  status: [
    {
      required: true,
      message: t('validation.select', { field: t('common.status') }),
      trigger: 'blur'
    }
  ]
}))
const formRef = ref<FormInstance>()
type Form = {
  id?: number
  username: string
  nickname: string
  password: string
  avatar: string
  email: string
  phone: string
  status: Status
  roles: string[]
}
const form: Form = reactive({
  id: undefined,
  username: '',
  nickname: '',
  password: '',
  avatar: '',
  email: '',
  phone: '',
  status: 0,
  roles: []
})

const download = async () => {
  await downloadSQLData(
    API_ENDPOINTS.USER_ADMIN.DOWNLOAD_USERS,
    'users',
    uploadPercentage,
    showPercentage
  )
}

const delBatch = async () => {
  const args: number[] = []
  multipleSelection.value.forEach((item) => {
    args.push(item.id)
  })
  await POST<null>(API_ENDPOINTS.USER_ADMIN.DELETE_USERS, args)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.batchDeleteSuccess'),
    type: 'success'
  })
  multipleSelection.value = []
  await queryUsers()
}

const handleDelete = async (row: UserSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>(API_ENDPOINTS.USER_ADMIN.DELETE_USERS, id)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.deleteSuccess'),
    type: 'success'
  })
  queryUsers()
}

const handleEdit = async (row: UserSys) => {
  const data = await GET<UserSys>(API_ENDPOINTS.USER_ADMIN.GET_USER_INFO(row.id))
  Object.assign(form, data)
  dialogVisible.value = true
}

const handleSelectionChange = (val: UserSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const queryUsers = async () => {
  loading.value = true
  try {
    const url = buildCommonUrls.userQuery(pageNumber.value, { size: pageSize.value })
    const data = await GET<PageAdapter<UserSys>>(url)
    content.value = data.content
    totalElements.value = data.totalElements
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
      await POST<null>(API_ENDPOINTS.USER_ADMIN.SAVE_USER, form)
      ElNotification({
        title: t('common.operationSuccess'),
        message: t('common.editSuccess'),
        type: 'success'
      })
      clearForm()
      dialogVisible.value = false
      pageNumber.value = 1
      await queryUsers()
    }
  })
}

const clearForm = () => {
  form.id = undefined
  form.username = ''
  form.nickname = ''
  form.password = ''
  form.avatar = ''
  form.email = ''
  form.phone = ''
  form.roles = []
  form.status = 0
}

const getRoleName = (item: string) => {
  return roleList.value.find((role) => role.code === item)?.name
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  pageNumber.value = 1
  await queryUsers()
}

const handleCurrentChange = async (val: number) => {
  pageNumber.value = val
  await queryUsers()
}

const getRegisterLink = async (username: string) => {
  const link = await GET<string>(
    buildQueryUrl(API_ENDPOINTS.USER_ADMIN.GET_REGISTER_LINK, {
      username
    })
  )
  ElNotification({
    title: t('common.operationSuccess'),
    message: link,
    type: 'success'
  })
}

;(async () => {
  const roles = await GET<RoleSys[]>(API_ENDPOINTS.ROLE_ADMIN.GET_VALID_ROLES)
  await queryUsers()
  roleList.value = roles
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_USER_CREATE)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_USER_CREATE)"
        size="large"
        @click="dialogVisible = true"
        >{{ getButtonTitle(ButtonAuth.SYS_USER_CREATE) }}</el-button
      >
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_USER_BATCH_DEL)">
      <el-popconfirm :title="t('common.batchDeleteConfirm')" @confirm="delBatch">
        <template #reference>
          <el-button
            :type="getButtonType(ButtonAuth.SYS_USER_BATCH_DEL)"
            size="large"
            :disabled="delBtlStatus"
            >{{ getButtonTitle(ButtonAuth.SYS_USER_BATCH_DEL) }}</el-button
          >
        </template>
      </el-popconfirm>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_USER_REGISTER)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_USER_REGISTER)"
        size="large"
        @click="getRegisterLink('')"
        >{{ getButtonTitle(ButtonAuth.SYS_USER_REGISTER) }}</el-button
      >
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_USER_DOWNLOAD)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_USER_DOWNLOAD)"
        size="large"
        @click="download"
        >{{ getButtonTitle(ButtonAuth.SYS_USER_DOWNLOAD) }}</el-button
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
    <el-table-column :label="t('auth.username')" align="center" prop="username" min-width="180" />
    <el-table-column :label="t('auth.nickname')" align="center" prop="nickname" min-width="180" />

    <el-table-column :label="t('auth.avatar')" align="center">
      <template #default="scope">
        <el-avatar size="default" :src="scope.row.avatar" />
      </template>
    </el-table-column>

    <el-table-column :label="t('auth.email')" min-width="200" align="center" prop="email" />
    <el-table-column :label="t('auth.phone')" min-width="200" align="center" prop="phone" />

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

    <el-table-column :label="t('auth.role')" min-width="200" align="center">
      <template #default="scope">
        <el-tag size="small" v-for="item in scope.row.roles" v-bind:key="item.code" type="info">{{
          getRoleName(item)
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

    <el-table-column :label="t('admin.lastLogin')" min-width="180" align="center">
      <template #default="scope">
        <div class="time-icon">
          <el-icon>
            <timer />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.lastLogin }}</span>
        </div>
      </template>
    </el-table-column>

    <!-- @vue-generic {UserSys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" min-width="280" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_USER_EDIT)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_USER_EDIT)"
            @click="handleEdit(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_USER_EDIT) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_USER_DELETE)">
          <el-popconfirm :title="t('common.deleteConfirm')" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" :type="getButtonType(ButtonAuth.SYS_USER_DELETE)">{{
                getButtonTitle(ButtonAuth.SYS_USER_DELETE)
              }}</el-button>
            </template>
          </el-popconfirm>
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_USER_MODIFY_REGISTER)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_USER_MODIFY_REGISTER)"
            @click="getRegisterLink(scope.row.username)"
            >{{ getButtonTitle(ButtonAuth.SYS_USER_MODIFY_REGISTER) }}</el-button
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

  <el-dialog
    v-model="dialogVisible"
    :title="t('common.addEdit')"
    width="600px"
    :before-close="handleClose"
  >
    <el-form :model="form" :rules="formRules" ref="formRef">
      <el-form-item
        :label="t('auth.username')"
        label-width="100px"
        prop="username"
        class="username"
      >
        <el-input v-model="form.username" maxlength="30" />
      </el-form-item>

      <el-form-item
        :label="t('auth.nickname')"
        label-width="100px"
        prop="nickname"
        class="nickname"
      >
        <el-input v-model="form.nickname" maxlength="30" />
      </el-form-item>

      <el-form-item
        :label="t('auth.password')"
        label-width="100px"
        prop="password"
        class="password"
      >
        <el-input v-model="form.password" type="password" maxlength="30" />
      </el-form-item>

      <el-form-item :label="t('auth.avatarUrl')" label-width="100px" prop="avatar" class="avatar">
        <el-input v-model="form.avatar" />
      </el-form-item>

      <el-form-item :label="t('auth.email')" label-width="100px" prop="email" class="email">
        <el-input v-model="form.email" maxlength="30" />
      </el-form-item>

      <el-form-item :label="t('auth.phone')" label-width="100px" prop="phone" class="phone">
        <el-input v-model="form.phone" maxlength="30" />
      </el-form-item>

      <el-form-item :label="t('auth.role')" label-width="100px" prop="roles" class="role">
        <el-select
          multiple
          class="role-option"
          v-model="form.roles"
          :placeholder="t('common.select')"
        >
          <el-option
            v-for="item in roleList"
            :key="item.code"
            :label="item.name"
            :value="item.code"
          >
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="t('common.status')" label-width="100px" prop="status" class="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="Status.NORMAL">{{ t('common.enabled') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_USER_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_USER_SAVE)"
          @click="submitForm(formRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_USER_SAVE) }}</el-button
        >
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
@import '@/assets/main.css';

.role-option {
  width: 150px;
}

.button-form .el-form-item {
  margin-right: 10px;
}

.el-pagination {
  margin-top: 10px;
}

.el-tag {
  margin: 5px;
}

.username {
  width: 300px;
}

.nickname {
  width: 300px;
}

.password {
  width: 400px;
}

.avatar {
  width: 500px;
}

.email {
  width: 400px;
}

.phone {
  width: 400px;
}
</style>
