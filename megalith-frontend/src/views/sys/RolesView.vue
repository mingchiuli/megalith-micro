<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { PageAdapter, RoleSys } from '@/type/entity'
import type { ElTree, FormInstance, FormRules } from 'element-plus'
import { Status, ButtonAuth } from '@/type/entity'
import { reactive, ref, toRefs } from 'vue'
import { checkButtonAuth, getButtonType, downloadData, getButtonTitle } from '@/utils/tools'
import { displayState } from '@/position/position'

const { moreItems, fixSelection, fix } = displayState()
const dialogVisible = ref(false)
const delBtlStatus = ref(true)
const loading = ref(false)
const multipleSelection = ref<RoleSys[]>([])
const defaultProps = { children: 'children', label: 'title' }
const formRef = ref<FormInstance>()
const menuDialogVisible = ref(false)
const authorityDialogVisible = ref(false)
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const uploadPercentage = ref(0)
const showPercentage = ref(false)
let menuTreeData = ref<MenuForm[]>([])
let authorityData = ref<AuthorityForm[]>([])
let roleId = ref<number>()


const page: PageAdapter<RoleSys> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": moreItems.value ? 15 : 5,
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
type MenuForm = {
  roleId: number
  menuId: number
  title: string
  check: boolean
  children: MenuForm[]
}

type AuthorityForm = {
  authorityId: number
  code: string
  check: boolean
}

const download = async () => {
  await downloadData('/sys/role/download', 'roles', uploadPercentage, showPercentage)
}

const submitmenuFormHandle = async (ref: InstanceType<typeof ElTree>) => {
  //全选和半选都要包含
  const ids = ref.getCheckedKeys()
  const halfCheckedIds = ref.getHalfCheckedKeys()
  await POST<null>(`/sys/role/menu/${roleId.value}`, ids.concat(halfCheckedIds))
  ElNotification({
    title: '操作成功',
    message: '编辑成功',
    type: 'success',
  })
  menuTreeData.value = []
  menuDialogVisible.value = false
}

const submitAuthorityFormHandle = async () => {
  const ids = authorityData.value
    .filter(item => item.check)
    .map(item => item.authorityId)
  await POST<null>(`/sys/role/authority/${roleId.value}`, ids)
  ElNotification({
    title: '操作成功',
    message: '编辑成功',
    type: 'success',
  })
  authorityData.value = []
  authorityDialogVisible.value = false
}

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

const handleClose = () => {
  clearForm()
  dialogVisible.value = false
}

const authorityHandleClose = () => {
  authorityData.value = []
  authorityDialogVisible.value = false
}

const menuHandleClose = () => {
  menuTreeData.value = []
  menuDialogVisible.value = false
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

const handleMenu = async (row: RoleSys) => {
  const data = await GET<MenuForm[]>(`/sys/role/menu/${row.id}`)
  menuTreeData.value = data
  roleId.value = row.id
  menuDialogVisible.value = true
}

const handleAuthority = async (row: RoleSys) => {
  authorityData.value = await GET<AuthorityForm[]>(`/sys/role/authority/${row.id}`)
  roleId.value = row.id
  authorityDialogVisible.value = true
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
  multipleSelection.value.splice(0)
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

const getCheckKeys = (menuForms: MenuForm[]): Array<number> => {
  const ids: Array<number> = []
  getKeysIds(menuForms, ids)
  return ids
}

const getKeysIds = (menuForms: MenuForm[], ids: Array<number>) => {
  menuForms.forEach(item => {
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
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_CREATE)">
      <el-button :type="getButtonType(ButtonAuth.SYS_ROLE_CREATE)" size="large" @click="dialogVisible = true">{{
        getButtonTitle(ButtonAuth.SYS_ROLE_CREATE) }}</el-button>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_BATCH_DEL)">
      <el-popconfirm title="确定批量删除?" @confirm="delBatch">
        <template #reference>
          <el-button :type="getButtonType(ButtonAuth.SYS_ROLE_BATCH_DEL)" size="large" :disabled="delBtlStatus">{{
            getButtonTitle(ButtonAuth.SYS_ROLE_BATCH_DEL) }}</el-button>
        </template>
      </el-popconfirm>
    </el-form-item>
    <el-form-item v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_DOWNLOAD)">
      <el-button :type="getButtonType(ButtonAuth.SYS_ROLE_DOWNLOAD)" size="large" @click="download">{{
        getButtonTitle(ButtonAuth.SYS_ROLE_DOWNLOAD) }}</el-button>
    </el-form-item>
    <el-form-item>
      <el-progress v-if="showPercentage" type="circle" width="40" :percentage="uploadPercentage" />
    </el-form-item>
  </el-form>

  <el-table :data="content" style="width: 100%" border stripe @selection-change="handleSelectionChange"
    v-loading="loading">

    <el-table-column type="selection" :fixed="fixSelection" />
    <el-table-column label="名字" align="center" prop="name" />
    <el-table-column label="唯一编码" align="center" prop="code" min-width="120" />

    <el-table-column label="描述" align="center" prop="remark" min-width="200" />

    <el-table-column label="状态" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === Status.NORMAL" type="success">启用</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === Status.BLOCK" type="danger">禁用</el-tag>
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

    <el-table-column :fixed="fix" label="操作" min-width="350" align="center">
      <template #default="scope">
        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_EDIT)">
          <el-button size="small" :type="getButtonType(ButtonAuth.SYS_ROLE_EDIT)" @click="handleEdit(scope.row)">{{
            getButtonTitle(ButtonAuth.SYS_ROLE_EDIT) }}</el-button>
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_MENU_PERM)">
          <el-button size="small" :type="getButtonType(ButtonAuth.SYS_ROLE_MENU_PERM)" @click="handleMenu(scope.row)">{{
            getButtonTitle(ButtonAuth.SYS_ROLE_MENU_PERM) }}</el-button>
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_AUTHORITY_PERM)">
          <el-button size="small" :type="getButtonType(ButtonAuth.SYS_ROLE_AUTHORITY_PERM)"
            @click="handleAuthority(scope.row)">{{ getButtonTitle(ButtonAuth.SYS_ROLE_AUTHORITY_PERM) }}</el-button>
        </template>

        <template v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_DELETE)">
          <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" :type="getButtonType(ButtonAuth.SYS_ROLE_DELETE)">{{
                getButtonTitle(ButtonAuth.SYS_ROLE_DELETE) }}</el-button>
            </template>
          </el-popconfirm>
        </template>

      </template>
    </el-table-column>
  </el-table>

  <el-pagination @current-change="handleCurrentChange" layout="->, prev, pager, next" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements" />

  <el-dialog title="新增/编辑" v-model="dialogVisible" width="600px" :before-close="handleClose">
    <el-form :model="form" :rules="formRules" label-width="100px" ref="formRef">
      <el-form-item label="名字" prop="name">
        <el-input v-model="form.name"></el-input>
      </el-form-item>

      <el-form-item label="唯一编码" prop="code">
        <el-input v-model="form.code"></el-input>
      </el-form-item>

      <el-form-item label="描述" prop="remark">
        <el-input v-model="form.remark" placeholder="文字描述"></el-input>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value=Status.NORMAL>启用</el-radio>
          <el-radio :value=Status.BLOCK>禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="400px">
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog title="菜单权限" v-model="menuDialogVisible" width="600px" :before-close="menuHandleClose">
    <el-form>
      <el-tree :data="menuTreeData" show-checkbox :default-expand-all=true node-key="menuId" :props="defaultProps"
        :default-checked-keys="getCheckKeys(menuTreeData)" ref="menuTreeRef" :check-strictly="true" />
      <el-form-item label-width="450px">
        <el-button type="primary" @click="submitmenuFormHandle(menuTreeRef!)">Submit</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog title="接口权限" v-model="authorityDialogVisible" width="600px" :before-close="authorityHandleClose">
    <el-form>

      <span class="authority-display" v-for="item in authorityData" :key="item.authorityId">
        <el-checkbox v-model="item.check" :label="item.code" size="large" />
      </span>

      <el-form-item label-width="450px">
        <el-button type="primary" @click="submitAuthorityFormHandle">Submit</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
@import '@/assets/main.css';

.button-form .el-form-item {
  margin-right: 10px
}

.el-pagination {
  margin-top: 10px
}

.authority-display {
  padding: 10px;
  width: 200px;
  height: 20px;
  display: inline-block;
}
</style>