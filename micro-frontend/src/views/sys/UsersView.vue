<script lang="ts" setup>
import { useHttp } from '@/http/http'
import type { PageAdapter, RoleSys, UserSys } from '@/type/entity'
import type { FormInstance, TableInstance } from 'element-plus'
import { ButtonAuth } from '@/type/entity'
import { downloadSQLData } from '@/utils/download'
import { checkButtonAuth, getButtonType, getButtonTitle } from '@/utils/permissions'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildCommonUrls, buildQueryUrl } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'
import { useLatestRequest, useUniversalData } from '@/composables'
import { useUserEditor } from '@/composables/admin/useUserEditor'

const { t } = useI18n()
const { GET, POST, DOWNLOAD } = useHttp()

const tableRef = useTemplateRef<TableInstance>('tableRef')
const { moreItems, fixSelection, fix } = displayState(tableRef)
const multipleSelection = ref<UserSys[]>([])
const dialogVisible = ref(false)
const loading = ref(true)
const { runLatest } = useLatestRequest(loading)
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

const { form, formRules, clearForm } = useUserEditor()

const download = async () => {
  await downloadSQLData(
    DOWNLOAD,
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

const fetchUsers = async () => {
  const url = buildCommonUrls.userQuery(pageNumber.value, { size: pageSize.value })
  return GET<PageAdapter<UserSys>>(url)
}

const applyUsers = (data: PageAdapter<UserSys>) => {
  content.value = data.content
  totalElements.value = data.totalElements
}

const queryUsers = async () => {
  await runLatest(fetchUsers, applyUsers)
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

type UsersInitialData = { users: PageAdapter<UserSys>; roles: RoleSys[] }
useUniversalData<UsersInitialData>(
  'admin:users',
  async () => ({
    users: await fetchUsers(),
    roles: await GET<RoleSys[]>(API_ENDPOINTS.ROLE_ADMIN.GET_VALID_ROLES)
  }),
  ({ users, roles }) => {
    applyUsers(users)
    roleList.value = roles
  },
  { loading }
)
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
    ref="tableRef"
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
        <StatusTag :status="scope.row.status" type="user" />
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
        <TimeColumn :time="scope.row.created" />
      </template>
    </el-table-column>

    <el-table-column :label="t('common.updatedAt')" min-width="180" align="center">
      <template #default="scope">
        <TimeColumn :time="scope.row.updated" />
      </template>
    </el-table-column>

    <el-table-column :label="t('admin.lastLogin')" min-width="180" align="center">
      <template #default="scope">
        <TimeColumn :time="scope.row.lastLogin" />
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

  <UserEditorDialog
    v-model:visible="dialogVisible"
    :form="form"
    :rules="formRules"
    :roles="roleList"
    @close="handleClose"
    @save="submitForm"
  />
</template>

<style scoped>
@import '@/assets/main.css';

.button-form .el-form-item {
  margin-right: 10px;
}

.el-pagination {
  margin-top: 10px;
}

.el-tag {
  margin: 5px;
}
</style>
