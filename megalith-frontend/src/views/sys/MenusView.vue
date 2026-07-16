<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { MenuSys } from '@/type/entity'
import { type FormInstance, type FormRules, type TreeNodeData } from 'element-plus'
import { Status, ButtonAuth, RoutesStatus, RoutesEnum } from '@/type/entity'
import { checkButtonAuth, getButtonType, downloadSQLData, getButtonTitle } from '@/utils/tools'
import { displayState } from '@/utils/position'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { fix } = displayState()
const dialogVisible = ref(false)
const loading = ref(false)
const content = ref<MenuSys[]>([])
const formRef = ref<FormInstance>()
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

const queryMenus = async () => {
  loading.value = true
  try {
    content.value = await GET<Array<MenuSys>>(API_ENDPOINTS.MENU_ADMIN.GET_MENUS)
  } finally {
    loading.value = false
  }
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

;(async () => {
  await queryMenus()
})()
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

  <el-table v-loading="loading" :data="content" row-key="id" border stripe default-expand-all>
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
        <div class="time-icon">
          <el-icon>
            <timer v-if="scope.row.parentId !== -1" />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.created }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column :label="t('common.updatedAt')" min-width="180" align="center">
      <template #default="scope">
        <div class="time-icon">
          <el-icon>
            <timer v-if="scope.row.parentId !== -1" />
          </el-icon>
          <span style="margin-left: 10px">{{ scope.row.updated }}</span>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="status" :label="t('common.status')" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">{{
          t('common.active')
        }}</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">{{
          t('common.disabled')
        }}</el-tag>
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

  <el-dialog
    :title="t('common.addEdit')"
    v-model="dialogVisible"
    width="600px"
    :before-close="handleClose"
  >
    <el-form :model="form" :rules="editFormRules" ref="formRef" label-width="100px">
      <el-form-item :label="t('admin.parentMenu')" prop="parentId">
        <el-tree-select v-model="form.parentId" :props="props" :data="content" check-strictly />
      </el-form-item>

      <el-form-item :label="t('common.title')" prop="title" label-width="100px">
        <el-input v-model="form.title" autocomplete="off" />
      </el-form-item>

      <el-form-item :label="t('admin.icon')" prop="icon" label-width="100px">
        <el-input v-model="form.icon" autocomplete="off" />
      </el-form-item>
      <el-form-item :label="t('common.url')" prop="url" label-width="100px">
        <el-input v-model="form.url" autocomplete="off" />
      </el-form-item>

      <el-form-item :label="t('admin.componentName')" prop="name" label-width="100px">
        <el-input v-model="form.name" autocomplete="off" />
      </el-form-item>

      <el-form-item :label="t('admin.componentUri')" prop="component" label-width="100px">
        <el-input v-model="form.component" autocomplete="off" />
      </el-form-item>

      <el-form-item :label="t('common.type')" prop="type" label-width="100px">
        <el-radio-group v-model="form.type">
          <el-radio :value="RoutesEnum.CATALOGUE">{{ t('admin.category') }}</el-radio>
          <el-radio :value="RoutesEnum.MENU">{{ t('admin.menu') }}</el-radio>
          <el-radio :value="RoutesEnum.BUTTON">{{ t('admin.button') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="t('common.status')" prop="status" label-width="100px">
        <el-radio-group v-model="form.status">
          <el-radio :value="Status.NORMAL">{{ t('common.active') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="t('admin.order')" prop="orderNum" label-width="100px">
        <el-input-number v-model="form.orderNum" :min="1" :label="t('admin.order')"
          >1</el-input-number
        >
      </el-form-item>

      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_MENU_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_MENU_SAVE)"
          @click="submitForm(formRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_MENU_SAVE) }}</el-button
        >
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog
    :title="t('admin.apiPermission')"
    v-model="authorityDialogVisible"
    width="600px"
    :before-close="authorityHandleClose"
  >
    <el-form>
      <span class="authority-display" v-for="item in authorityData" :key="item.authorityId">
        <el-checkbox v-model="item.check" :label="item.code" size="large" />
      </span>

      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          @click="submitAuthorityFormHandle"
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

.authority-display {
  padding: 10px;
  width: 200px;
  height: 20px;
  display: inline-block;
}
</style>
