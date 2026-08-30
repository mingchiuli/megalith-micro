<script lang="ts" setup>
import { useHttp } from '@/http/http'
import type { PageAdapter, RoleSys } from '@/type/entity'
import type { ElTree, FormInstance, TableInstance } from 'element-plus'
import { ButtonAuth, DataPermission } from '@/type/entity'
import { downloadSQLData } from '@/utils/download'
import { checkButtonAuth, getButtonType, getButtonTitle } from '@/utils/permissions'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildCommonUrls } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'
import { useLatestRequest, useUniversalData } from '@/composables'
import { useRoleEditor, type RoleMenuForm } from '@/composables/admin/useRoleEditor'

const { t } = useI18n()
const { GET, POST, DOWNLOAD } = useHttp()

const tableRef = useTemplateRef<TableInstance>('tableRef')
const { moreItems, fixSelection, fix } = displayState(tableRef)
const dialogVisible = ref(false)
const delBtlStatus = ref(true)
const loading = ref(true)
const { runLatest } = useLatestRequest(loading)
const multipleSelection = ref<RoleSys[]>([])
const menuDialogVisible = ref(false)
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const menuTreeData = ref<RoleMenuForm[]>([])
const roleId = ref<number>()
const dataPermissionDialogVisible = ref(false)
const dataPermissionRoleId = ref<number>()
const selectedDataPermissions = ref<DataPermission[]>([])

const page: PageAdapter<RoleSys> = reactive({
  content: [],
  totalElements: 0,
  pageSize: moreItems.value ? 15 : 5,
  pageNumber: 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const { form, formRules, dataPermissionOptions, dataPermissionLabel, clearForm } = useRoleEditor()

const download = async () => {
  await downloadSQLData(
    DOWNLOAD,
    API_ENDPOINTS.ROLE_ADMIN.DOWNLOAD_ROLES,
    'roles',
    uploadPercentage,
    showPercentage
  )
}

const submitmenuFormHandle = async (ref: InstanceType<typeof ElTree>) => {
  //全选和半选都要包含
  const ids = ref.getCheckedKeys()
  const halfCheckedIds = ref.getHalfCheckedKeys()
  await POST<null>(
    API_ENDPOINTS.ROLE_ADMIN.SET_ROLE_MENUS(roleId.value!),
    ids.concat(halfCheckedIds)
  )
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.editSuccess'),
    type: 'success'
  })
  menuTreeData.value = []
  menuDialogVisible.value = false
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid) => {
    if (valid) {
      await POST<null>(API_ENDPOINTS.ROLE_ADMIN.SAVE_ROLE, form)
      ElNotification({
        title: t('common.operationSuccess'),
        message: t('common.editSuccess'),
        type: 'success'
      })
      clearForm()
      dialogVisible.value = false
      pageNumber.value = 1
      await queryRoles()
    }
  })
}

const handleClose = () => {
  dialogVisible.value = false
  clearForm()
}

const menuHandleClose = () => {
  menuTreeData.value = []
  menuDialogVisible.value = false
}

const dataPermissionHandleClose = () => {
  selectedDataPermissions.value = []
  dataPermissionRoleId.value = undefined
  dataPermissionDialogVisible.value = false
}

const handleEdit = async (row: RoleSys) => {
  const data = await GET<RoleSys>(API_ENDPOINTS.ROLE_ADMIN.GET_ROLE_INFO(row.id))
  form.id = data.id
  form.name = data.name
  form.code = data.code
  form.remark = data.remark
  form.status = data.status
  dialogVisible.value = true
}

const handleMenu = async (row: RoleSys) => {
  const data = await GET<RoleMenuForm[]>(API_ENDPOINTS.ROLE_ADMIN.GET_ROLE_MENUS(row.id))
  menuTreeData.value = data
  roleId.value = row.id
  menuDialogVisible.value = true
}

const handleDataPermission = async (row: RoleSys) => {
  selectedDataPermissions.value = await GET<DataPermission[]>(
    API_ENDPOINTS.ROLE_ADMIN.GET_ROLE_DATA_PERMISSIONS(row.id)
  )
  dataPermissionRoleId.value = row.id
  dataPermissionDialogVisible.value = true
}

const submitDataPermission = async () => {
  await POST<null>(
    API_ENDPOINTS.ROLE_ADMIN.SET_ROLE_DATA_PERMISSIONS(dataPermissionRoleId.value!),
    selectedDataPermissions.value
  )
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.editSuccess'),
    type: 'success'
  })
  dataPermissionHandleClose()
  await queryRoles()
}

const delBatch = async () => {
  const args: number[] = []
  multipleSelection.value.forEach((item) => {
    args.push(item.id)
  })
  await POST<null>(API_ENDPOINTS.ROLE_ADMIN.DELETE_ROLES, args)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.batchDeleteSuccess'),
    type: 'success'
  })
  multipleSelection.value.splice(0)
  await queryRoles()
}

const fetchRoles = async () => {
  const url = buildCommonUrls.roleQuery({
    currentPage: pageNumber.value,
    size: pageSize.value
  })
  return GET<PageAdapter<RoleSys>>(url)
}

const applyRoles = (data: PageAdapter<RoleSys>) => {
  content.value = data.content
  totalElements.value = data.totalElements
}

const queryRoles = async () => {
  await runLatest(fetchRoles, applyRoles)
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  pageNumber.value = 1
  await queryRoles()
}

const handleSelectionChange = (val: RoleSys[]) => {
  multipleSelection.value = val
  delBtlStatus.value = val.length === 0
}

const handleCurrentChange = async (pageNo: number) => {
  pageNumber.value = pageNo
  await queryRoles()
}

const getCheckKeys = (menuForms: RoleMenuForm[]): Array<number> => {
  const ids: Array<number> = []
  getKeysIds(menuForms, ids)
  return ids
}

const getKeysIds = (menuForms: RoleMenuForm[], ids: Array<number>) => {
  menuForms.forEach((item) => {
    if (item.check) {
      ids.push(item.menuId)
    }
    if (item.children.length) {
      getKeysIds(item.children, ids)
    }
  })
}

const handleDelete = async (row: RoleSys) => {
  const id: number[] = []
  id.push(row.id)
  await POST<null>('/sys/role/delete', id)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.deleteSuccess'),
    type: 'success'
  })
  await queryRoles()
}

useUniversalData('admin:roles', fetchRoles, applyRoles, { loading })
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_CREATE)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_ROLE_CREATE)"
        size="large"
        @click="dialogVisible = true"
        >{{ getButtonTitle(ButtonAuth.SYS_ROLE_CREATE) }}</el-button
      >
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_BATCH_DEL)">
      <el-popconfirm :title="t('common.batchDeleteConfirm')" @confirm="delBatch">
        <template #reference>
          <el-button
            :type="getButtonType(ButtonAuth.SYS_ROLE_BATCH_DEL)"
            size="large"
            :disabled="delBtlStatus"
            >{{ getButtonTitle(ButtonAuth.SYS_ROLE_BATCH_DEL) }}</el-button
          >
        </template>
      </el-popconfirm>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_DOWNLOAD)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_ROLE_DOWNLOAD)"
        size="large"
        @click="download"
        >{{ getButtonTitle(ButtonAuth.SYS_ROLE_DOWNLOAD) }}</el-button
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
    <el-table-column :label="t('admin.name')" align="center" prop="name" min-width="120" />
    <el-table-column :label="t('admin.uniqueCode')" align="center" prop="code" min-width="120" />

    <el-table-column :label="t('admin.remark')" align="center" prop="remark" min-width="200" />

    <el-table-column :label="t('common.status')" align="center">
      <template #default="scope">
        <StatusTag :status="scope.row.status" type="user" />
      </template>
    </el-table-column>

    <el-table-column :label="t('admin.dataPermission')" min-width="240" align="center">
      <template #default="scope">
        <div class="permission-tags">
          <el-tag
            v-for="permission in scope.row.dataPermissions ?? []"
            :key="permission"
            size="small"
            type="info"
          >
            {{ dataPermissionLabel(permission) }}
          </el-tag>
          <span v-if="(scope.row.dataPermissions ?? []).length === 0">{{
            t('admin.ownDataOnly')
          }}</span>
        </div>
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

    <!-- @vue-generic {RoleSys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" min-width="330" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_EDIT)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_ROLE_EDIT)"
            @click="handleEdit(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_ROLE_EDIT) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_MENU_PERM)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_ROLE_MENU_PERM)"
            @click="handleMenu(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_ROLE_MENU_PERM) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_DATA_PERM)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_ROLE_DATA_PERM)"
            @click="handleDataPermission(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_ROLE_DATA_PERM) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_DELETE)">
          <el-popconfirm :title="t('common.deleteConfirm')" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" :type="getButtonType(ButtonAuth.SYS_ROLE_DELETE)">{{
                getButtonTitle(ButtonAuth.SYS_ROLE_DELETE)
              }}</el-button>
            </template>
          </el-popconfirm>
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

  <RoleDialogs
    v-model:editor-visible="dialogVisible"
    v-model:menu-visible="menuDialogVisible"
    v-model:permission-visible="dataPermissionDialogVisible"
    v-model:selected-permissions="selectedDataPermissions"
    :form="form"
    :rules="formRules"
    :menus="menuTreeData"
    :permission-options="dataPermissionOptions"
    :checked-menu-keys="getCheckKeys"
    @close-editor="handleClose"
    @close-menu="menuHandleClose"
    @close-permission="dataPermissionHandleClose"
    @save-role="submitForm"
    @save-menu="submitmenuFormHandle"
    @save-permission="submitDataPermission"
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

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
}
</style>
