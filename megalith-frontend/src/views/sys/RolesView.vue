<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { PageAdapter, RoleSys } from '@/type/entity'
import type { ElTree, FormInstance, FormRules } from 'element-plus'
import { Status, ButtonAuth } from '@/type/entity'
import { downloadSQLData } from '@/utils/download'
import { checkButtonAuth, getButtonType, getButtonTitle } from '@/utils/permissions'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS, buildCommonUrls } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { moreItems, fixSelection, fix } = displayState()
const dialogVisible = ref(false)
const delBtlStatus = ref(true)
const loading = ref(false)
const multipleSelection = ref<RoleSys[]>([])
const defaultProps = { children: 'children', label: 'title' }
const formRef = ref<FormInstance>()
const menuDialogVisible = ref(false)
const menuTreeRef = useTemplateRef<InstanceType<typeof ElTree>>('menuTreeRef')
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const menuTreeData = ref<MenuForm[]>([])
const roleId = ref<number>()

const page: PageAdapter<RoleSys> = reactive({
  content: [],
  totalElements: 0,
  pageSize: moreItems.value ? 15 : 5,
  pageNumber: 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const formRules = computed<FormRules<Form>>(() => ({
  name: [
    { required: true, message: t('validation.enter', { field: t('admin.name') }), trigger: 'blur' }
  ],
  code: [
    {
      required: true,
      message: t('validation.enter', { field: t('admin.uniqueCode') }),
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
  ]
}))
type Form = {
  id?: number
  name: string
  code: string
  remark: string
  status: Status
}
const form: Form = reactive({
  id: undefined,
  name: '',
  code: '',
  remark: '',
  status: 0
})
type MenuForm = {
  menuId: number
  title: string
  check: boolean
  children: MenuForm[]
}

const download = async () => {
  await downloadSQLData(
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

const clearForm = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.remark = ''
  form.status = 0
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

const handleEdit = async (row: RoleSys) => {
  const data = await GET<RoleSys>(API_ENDPOINTS.ROLE_ADMIN.GET_ROLE_INFO(row.id))
  Object.assign(form, data)
  dialogVisible.value = true
}

const handleMenu = async (row: RoleSys) => {
  const data = await GET<MenuForm[]>(API_ENDPOINTS.ROLE_ADMIN.GET_ROLE_MENUS(row.id))
  menuTreeData.value = data
  roleId.value = row.id
  menuDialogVisible.value = true
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

const queryRoles = async () => {
  loading.value = true
  try {
    const url = buildCommonUrls.roleQuery({
      currentPage: pageNumber.value,
      size: pageSize.value
    })
    const data = await GET<PageAdapter<RoleSys>>(url)
    content.value = data.content
    totalElements.value = data.totalElements
  } finally {
    loading.value = false
  }
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

const getCheckKeys = (menuForms: MenuForm[]): Array<number> => {
  const ids: Array<number> = []
  getKeysIds(menuForms, ids)
  return ids
}

const getKeysIds = (menuForms: MenuForm[], ids: Array<number>) => {
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

;(async () => {
  await queryRoles()
})()
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
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">{{
          t('common.enabled')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">{{
          t('common.disabled')
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

    <!-- @vue-generic {RoleSys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" min-width="250" align="center">
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

  <el-dialog
    :title="t('common.addEdit')"
    v-model="dialogVisible"
    width="600px"
    :before-close="handleClose"
  >
    <el-form :model="form" :rules="formRules" label-width="100px" ref="formRef">
      <el-form-item :label="t('admin.name')" prop="name">
        <el-input v-model="form.name"></el-input>
      </el-form-item>

      <el-form-item :label="t('admin.uniqueCode')" prop="code">
        <el-input v-model="form.code"></el-input>
      </el-form-item>

      <el-form-item :label="t('admin.remark')" prop="remark">
        <el-input
          v-model="form.remark"
          :placeholder="t('validation.enter', { field: t('admin.remark') })"
        ></el-input>
      </el-form-item>

      <el-form-item :label="t('common.status')" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="Status.NORMAL">{{ t('common.enabled') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="400px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_ROLE_SAVE)"
          @click="submitForm(formRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_ROLE_SAVE) }}</el-button
        >
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog
    :title="t('admin.menuPermission')"
    v-model="menuDialogVisible"
    width="600px"
    :before-close="menuHandleClose"
  >
    <el-form>
      <el-tree
        :data="menuTreeData"
        show-checkbox
        :default-expand-all="true"
        node-key="menuId"
        :props="defaultProps"
        :default-checked-keys="getCheckKeys(menuTreeData)"
        ref="menuTreeRef"
        :check-strictly="true"
      />
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          @click="submitmenuFormHandle(menuTreeRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_MENU_AUTHORITY_SAVE) }}</el-button
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

.el-pagination {
  margin-top: 10px;
}
</style>
