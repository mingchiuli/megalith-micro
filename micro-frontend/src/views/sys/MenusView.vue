<script lang="ts" setup>
import type { TableInstance } from 'element-plus'
import { useHttp } from '@/http/http'
import type { MenuSys } from '@/type/entity'
import { type FormInstance, type FormRules, type TreeNodeData } from 'element-plus'
import { ButtonAuth, RoutesStatus, RoutesEnum } from '@/type/entity'
import { downloadSQLData } from '@/utils/download'
import { checkButtonAuth, getButtonType, getButtonTitle } from '@/utils/permissions'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'
import { useLatestRequest, useUniversalData } from '@/composables'

const { t } = useI18n()
const { GET, POST, DOWNLOAD } = useHttp()

const tableRef = useTemplateRef<TableInstance>('tableRef')
const { fix } = displayState(tableRef)
const dialogVisible = ref(false)
const loading = ref(true)
const { runLatest } = useLatestRequest(loading)
const content = ref<MenuSys[]>([])
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const authorityDialogVisible = ref(false)
const authorityData = ref<AuthorityForm[]>([])
const menuId = ref<number>()

const props = {
  label: 'title',
  //这个value代表根据这个值找节点，和v-model="value"的value不是一个概念
  value: 'id',
  disabled: (data: TreeNodeData) => {
    const menuData = data as MenuSys
    return menuData.status !== RoutesStatus.NORMAL || menuData.type === RoutesEnum.BUTTON
  }
}

type Form = {
  id?: number
  parentId: number
  title: string
  name: string
  url: string
  component: string
  type: RoutesEnum
  icon: string
  orderNum: number
  status: RoutesStatus
}

const form: Form = reactive({
  id: undefined,
  parentId: 0,
  title: '',
  name: '',
  url: '',
  component: '',
  type: 0,
  icon: '',
  orderNum: 0,
  status: 0
})

type AuthorityForm = {
  authorityId: number
  code: string
  check: boolean
}

const editFormRules = computed<FormRules<Form>>(() => ({
  parentId: [
    {
      required: true,
      message: t('validation.enter', { field: t('admin.parentId') }),
      trigger: 'blur'
    }
  ],
  name: [
    {
      required: true,
      message: t('validation.enter', { field: t('admin.uniqueName') }),
      trigger: 'blur'
    }
  ],
  title: [
    {
      required: true,
      message: t('validation.enter', { field: t('common.title') }),
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
  orderNum: [
    { required: true, message: t('validation.enter', { field: t('admin.order') }), trigger: 'blur' }
  ],
  status: [
    {
      required: true,
      message: t('validation.select', { field: t('common.status') }),
      trigger: 'blur'
    }
  ]
}))

const handleEdit = async (row: MenuSys) => {
  const data = await GET<MenuSys>(API_ENDPOINTS.MENU_ADMIN.GET_MENU_INFO(row.id))
  Object.assign(form, data)
  dialogVisible.value = true
}

const handleDelete = async (row: MenuSys) => {
  await POST<null>(API_ENDPOINTS.MENU_ADMIN.DELETE_MENU(row.id), null)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.deleteSuccess'),
    type: 'success'
  })
  await queryMenus()
}

const handleClose = () => {
  dialogVisible.value = false
  clearForm()
}

const download = async () => {
  await downloadSQLData(
    DOWNLOAD,
    API_ENDPOINTS.MENU_ADMIN.DOWNLOAD_MENUS,
    'menus',
    uploadPercentage,
    showPercentage
  )
}

const clearForm = () => {
  form.id = undefined
  form.parentId = 0
  form.title = ''
  form.name = ''
  form.url = ''
  form.component = ''
  form.type = 0
  form.icon = ''
  form.orderNum = 0
  form.status = 0
}

const fetchMenus = () => GET<Array<MenuSys>>(API_ENDPOINTS.MENU_ADMIN.GET_MENUS)
const applyMenus = (data: MenuSys[]) => {
  content.value = data
}

const queryMenus = async () => {
  await runLatest(fetchMenus, applyMenus)
}

const submitAuthorityFormHandle = async () => {
  const ids = authorityData.value.filter((item) => item.check).map((item) => item.authorityId)
  await POST<null>(API_ENDPOINTS.MENU_ADMIN.SET_MENU_AUTHORITY(menuId.value!), ids)
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('common.editSuccess'),
    type: 'success'
  })
  authorityData.value = []
  authorityDialogVisible.value = false
}

const handleAuthority = async (row: MenuSys) => {
  authorityData.value = await GET<AuthorityForm[]>(
    API_ENDPOINTS.MENU_ADMIN.GET_MENU_AUTHORITY(row.id)
  )
  menuId.value = row.id
  authorityDialogVisible.value = true
}

const authorityHandleClose = () => {
  authorityData.value = []
  authorityDialogVisible.value = false
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid) => {
    if (valid) {
      await POST<null>(API_ENDPOINTS.MENU_ADMIN.SAVE_MENU, form)
      ElNotification({
        title: t('common.operationSuccess'),
        message: t('common.editSuccess'),
        type: 'success'
      })
      clearForm()
      dialogVisible.value = false
      await queryMenus()
    }
  })
}

useUniversalData('admin:menus', fetchMenus, applyMenus, { loading })
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_MENU_CREATE)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_MENU_CREATE)"
        size="large"
        @click="dialogVisible = true"
        >{{ getButtonTitle(ButtonAuth.SYS_MENU_CREATE) }}</el-button
      >
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_MENU_DOWNLOAD)">
      <el-button
        :type="getButtonType(ButtonAuth.SYS_MENU_DOWNLOAD)"
        size="large"
        @click="download"
        >{{ getButtonTitle(ButtonAuth.SYS_MENU_DOWNLOAD) }}</el-button
      >
    </el-form-item>
    <el-form-item>
      <el-progress v-if="showPercentage" type="circle" :width="40" :percentage="uploadPercentage" />
    </el-form-item>
  </el-form>

  <el-table
    ref="tableRef"
    v-loading="loading"
    :data="content"
    row-key="id"
    border
    stripe
    default-expand-all
  >
    <el-table-column
      prop="title"
      :label="t('common.title')"
      sortable
      min-width="150"
      align="center"
    />
    <el-table-column prop="icon" :label="t('admin.icon')" align="center" min-width="150" />

    <el-table-column prop="type" :label="t('common.type')" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.type === RoutesEnum.CATALOGUE">{{
          t('admin.category')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.type === RoutesEnum.MENU" type="success">{{
          t('admin.menu')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.type === RoutesEnum.BUTTON" type="info">{{
          t('admin.button')
        }}</el-tag>
      </template>
    </el-table-column>

    <el-table-column prop="url" :label="t('common.url')" align="center" min-width="180" />
    <el-table-column
      prop="component"
      :label="t('admin.componentUri')"
      align="center"
      min-width="180"
    />
    <el-table-column prop="name" :label="t('admin.componentName')" align="center" min-width="250" />
    <el-table-column prop="orderNum" :label="t('admin.order')" align="center" />

    <el-table-column :label="t('common.createdAt')" min-width="180" align="center">
      <template #default="scope">
        <TimeColumn v-if="scope.row.parentId !== -1" :time="scope.row.created" />
      </template>
    </el-table-column>

    <el-table-column :label="t('common.updatedAt')" min-width="180" align="center">
      <template #default="scope">
        <TimeColumn v-if="scope.row.parentId !== -1" :time="scope.row.updated" />
      </template>
    </el-table-column>

    <el-table-column prop="status" :label="t('common.status')" align="center">
      <template #default="scope">
        <StatusTag :status="scope.row.status" type="route" />
      </template>
    </el-table-column>
    <!-- @vue-generic {MenuSys} -->
    <el-table-column :fixed="fix" :label="t('common.operations')" align="center" min-width="250">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_MENU_EDIT)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_MENU_EDIT)"
            @click="handleEdit(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_MENU_EDIT) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_MENUS_AUTHORITY_PERM)">
          <el-button
            size="small"
            :type="getButtonType(ButtonAuth.SYS_MENUS_AUTHORITY_PERM)"
            @click="handleAuthority(scope.row)"
            >{{ getButtonTitle(ButtonAuth.SYS_MENUS_AUTHORITY_PERM) }}</el-button
          >
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_MENU_DELETE)">
          <el-popconfirm :title="t('common.deleteConfirm')" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button :type="getButtonType(ButtonAuth.SYS_MENU_DELETE)" size="small">{{
                getButtonTitle(ButtonAuth.SYS_MENU_DELETE)
              }}</el-button>
            </template>
          </el-popconfirm>
        </template>
      </template>
    </el-table-column>
  </el-table>

  <MenuDialogs
    v-model:editor-visible="dialogVisible"
    v-model:authority-visible="authorityDialogVisible"
    :form="form"
    :rules="editFormRules"
    :menus="content"
    :authorities="authorityData"
    :tree-props="props"
    @close-editor="handleClose"
    @close-authority="authorityHandleClose"
    @save-menu="submitForm"
    @save-authority="submitAuthorityFormHandle"
  />
</template>

<style scoped>
@import '@/assets/main.css';

.button-form .el-form-item {
  margin-right: 10px;
}
</style>
