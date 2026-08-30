<script lang="ts" setup>
import type { ElTree, FormInstance, FormRules } from 'element-plus'
import { ButtonAuth, DataPermission, Status } from '@/type/entity'
import { checkButtonAuth, getButtonTitle, getButtonType } from '@/utils/permissions'
import { useI18n } from 'vue-i18n'

type RoleForm = {
  id?: number
  name: string
  code: string
  remark: string
  status: Status
}
type MenuForm = {
  menuId: number
  title: string
  check: boolean
  children: MenuForm[]
}
type PermissionOption = { value: DataPermission; label: string }

const props = defineProps<{
  editorVisible: boolean
  menuVisible: boolean
  permissionVisible: boolean
  form: RoleForm
  rules: FormRules<RoleForm>
  menus: MenuForm[]
  permissionOptions: PermissionOption[]
  checkedMenuKeys: (menus: MenuForm[]) => number[]
}>()
const emit = defineEmits<{
  'update:editorVisible': [visible: boolean]
  'update:menuVisible': [visible: boolean]
  'update:permissionVisible': [visible: boolean]
  closeEditor: []
  closeMenu: []
  closePermission: []
  saveRole: [form: FormInstance]
  saveMenu: [tree: InstanceType<typeof ElTree>]
  savePermission: []
}>()
const selectedPermissions = defineModel<DataPermission[]>('selectedPermissions', { required: true })
const { t } = useI18n()
const formRef = ref<FormInstance>()
const menuTreeRef = useTemplateRef<InstanceType<typeof ElTree>>('menuTreeRef')
const formModel = reactive(props.form)
const treeProps = { children: 'children', label: 'title' }
</script>

<template>
  <el-dialog
    :model-value="editorVisible"
    :title="t('common.addEdit')"
    width="600px"
    :before-close="() => emit('closeEditor')"
    @update:model-value="emit('update:editorVisible', $event)"
  >
    <el-form ref="formRef" :model="formModel" :rules="rules" label-width="130px">
      <el-form-item :label="t('admin.name')" prop="name"
        ><el-input v-model="formModel.name"
      /></el-form-item>
      <el-form-item :label="t('admin.uniqueCode')" prop="code"
        ><el-input v-model="formModel.code"
      /></el-form-item>
      <el-form-item :label="t('admin.remark')" prop="remark">
        <el-input
          v-model="formModel.remark"
          :placeholder="t('validation.enter', { field: t('admin.remark') })"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-radio-group v-model="formModel.status">
          <el-radio :value="Status.NORMAL">{{ t('common.enabled') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label-width="400px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_ROLE_SAVE)"
          @click="emit('saveRole', formRef!)"
        >
          {{ getButtonTitle(ButtonAuth.SYS_ROLE_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog
    :model-value="menuVisible"
    :title="t('admin.menuPermission')"
    width="600px"
    :before-close="() => emit('closeMenu')"
    @update:model-value="emit('update:menuVisible', $event)"
  >
    <el-form>
      <el-tree
        ref="menuTreeRef"
        :data="menus"
        :props="treeProps"
        :default-checked-keys="checkedMenuKeys(menus)"
        show-checkbox
        default-expand-all
        node-key="menuId"
        check-strictly
      />
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          @click="emit('saveMenu', menuTreeRef!)"
        >
          {{ getButtonTitle(ButtonAuth.SYS_MENU_AUTHORITY_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog
    :model-value="permissionVisible"
    :title="t('admin.dataPermission')"
    width="600px"
    :before-close="() => emit('closePermission')"
    @update:model-value="emit('update:permissionVisible', $event)"
  >
    <el-form>
      <el-form-item>
        <el-checkbox-group v-model="selectedPermissions" class="data-permission-options">
          <el-checkbox v-for="item in permissionOptions" :key="item.value" :value="item.value">{{
            item.label
          }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_ROLE_DATA_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_ROLE_DATA_SAVE)"
          @click="emit('savePermission')"
        >
          {{ getButtonTitle(ButtonAuth.SYS_ROLE_DATA_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
.data-permission-options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  width: 100%;
}
</style>
