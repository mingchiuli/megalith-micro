<script lang="ts" setup>
import type { FormInstance, FormRules, TreeNodeData } from 'element-plus'
import type { MenuSys } from '@/type/entity'
import { ButtonAuth, RoutesEnum, RoutesStatus, Status } from '@/type/entity'
import { checkButtonAuth, getButtonTitle, getButtonType } from '@/utils/permissions'
import { useI18n } from 'vue-i18n'

type MenuForm = {
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
type AuthorityForm = { authorityId: number; code: string; check: boolean }

const props = defineProps<{
  editorVisible: boolean
  authorityVisible: boolean
  form: MenuForm
  rules: FormRules<MenuForm>
  menus: MenuSys[]
  authorities: AuthorityForm[]
  treeProps: {
    label: string
    value: string
    disabled: (data: TreeNodeData) => boolean
  }
}>()
const emit = defineEmits<{
  'update:editorVisible': [visible: boolean]
  'update:authorityVisible': [visible: boolean]
  closeEditor: []
  closeAuthority: []
  saveMenu: [form: FormInstance]
  saveAuthority: []
}>()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const formModel = reactive(props.form)
const authorities = computed(() => props.authorities)
</script>

<template>
  <el-dialog
    :model-value="editorVisible"
    :title="t('common.addEdit')"
    width="600px"
    :before-close="() => emit('closeEditor')"
    @update:model-value="emit('update:editorVisible', $event)"
  >
    <el-form ref="formRef" :model="formModel" :rules="rules" label-width="100px">
      <el-form-item :label="t('admin.parentMenu')" prop="parentId">
        <el-tree-select
          v-model="formModel.parentId"
          :props="treeProps"
          :data="menus"
          check-strictly
        />
      </el-form-item>
      <el-form-item :label="t('common.title')" prop="title"
        ><el-input v-model="formModel.title"
      /></el-form-item>
      <el-form-item :label="t('admin.icon')" prop="icon"
        ><el-input v-model="formModel.icon"
      /></el-form-item>
      <el-form-item :label="t('common.url')" prop="url"
        ><el-input v-model="formModel.url"
      /></el-form-item>
      <el-form-item :label="t('admin.componentName')" prop="name"
        ><el-input v-model="formModel.name"
      /></el-form-item>
      <el-form-item :label="t('admin.componentUri')" prop="component"
        ><el-input v-model="formModel.component"
      /></el-form-item>
      <el-form-item :label="t('common.type')" prop="type">
        <el-radio-group v-model="formModel.type">
          <el-radio :value="RoutesEnum.CATALOGUE">{{ t('admin.category') }}</el-radio>
          <el-radio :value="RoutesEnum.MENU">{{ t('admin.menu') }}</el-radio>
          <el-radio :value="RoutesEnum.BUTTON">{{ t('admin.button') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-radio-group v-model="formModel.status">
          <el-radio :value="Status.NORMAL">{{ t('common.active') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('admin.order')" prop="orderNum">
        <el-input-number v-model="formModel.orderNum" :min="1" :label="t('admin.order')" />
      </el-form-item>
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_MENU_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_MENU_SAVE)"
          @click="emit('saveMenu', formRef!)"
        >
          {{ getButtonTitle(ButtonAuth.SYS_MENU_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog
    :model-value="authorityVisible"
    :title="t('admin.apiPermission')"
    width="600px"
    :before-close="() => emit('closeAuthority')"
    @update:model-value="emit('update:authorityVisible', $event)"
  >
    <el-form>
      <span v-for="item in authorities" :key="item.authorityId" class="authority-display">
        <el-checkbox v-model="item.check" :label="item.code" size="large" />
      </span>
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_MENU_AUTHORITY_SAVE)"
          @click="emit('saveAuthority')"
        >
          {{ getButtonTitle(ButtonAuth.SYS_MENU_AUTHORITY_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
.authority-display {
  display: inline-block;
  width: 200px;
  height: 20px;
  padding: 10px;
}
</style>
