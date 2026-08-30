<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { AuthStatus, ButtonAuth, Status } from '@/type/entity'
import { checkButtonAuth, getButtonTitle, getButtonType } from '@/utils/permissions'
import { useI18n } from 'vue-i18n'

type AuthorityForm = {
  id?: number
  code: string
  remark: string
  prototype: string
  methodType: string
  routePattern: string
  serviceHost: string
  servicePort: number
  status: Status
  type: AuthStatus
}

const props = defineProps<{
  visible: boolean
  form: AuthorityForm
  rules: FormRules<AuthorityForm>
}>()
const emit = defineEmits<{
  'update:visible': [visible: boolean]
  close: []
  save: [form: FormInstance]
}>()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const formModel = reactive(props.form)
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="t('common.addEdit')"
    width="600px"
    :before-close="() => emit('close')"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-form ref="formRef" :model="formModel" :rules="rules">
      <el-form-item :label="t('admin.permissionCode')" label-width="100px" prop="code">
        <el-input v-model="formModel.code" maxlength="50" />
      </el-form-item>
      <el-form-item :label="t('admin.remark')" label-width="100px" prop="remark">
        <el-input v-model="formModel.remark" maxlength="50" />
      </el-form-item>
      <el-form-item :label="t('admin.protocol')" label-width="100px" prop="prototype">
        <el-select v-model="formModel.prototype" style="width: 100%">
          <el-option label="http" value="http" />
          <el-option label="ws" value="ws" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('admin.methodType')" label-width="100px" prop="methodType">
        <el-select v-model="formModel.methodType" style="width: 100%">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('admin.routePattern')" label-width="100px" prop="routePattern">
        <el-input v-model="formModel.routePattern" maxlength="50" />
      </el-form-item>
      <el-form-item :label="t('admin.service')" label-width="100px" prop="serviceHost">
        <el-select v-model="formModel.serviceHost" style="width: 100%">
          <el-option
            v-for="service in ['blog', 'user', 'auth', 'search', 'exhibit']"
            :key="service"
            :label="`micro-${service}`"
            :value="`micro-${service}`"
          />
          <el-option label="micro-sync-rs" value="micro-sync-rs" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('admin.port')" label-width="100px" prop="servicePort">
        <el-input v-model="formModel.servicePort" maxlength="50" />
      </el-form-item>
      <el-form-item :label="t('common.status')" label-width="100px" prop="status">
        <el-radio-group v-model="formModel.status">
          <el-radio :value="Status.NORMAL">{{ t('common.enabled') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('common.type')" label-width="100px" prop="type">
        <el-radio-group v-model="formModel.type">
          <el-radio :value="AuthStatus.WHITE_LIST">{{ t('admin.whiteList') }}</el-radio>
          <el-radio :value="AuthStatus.NEED_AUTH">{{ t('admin.authRequired') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_AUTHORITY_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_AUTHORITY_SAVE)"
          @click="emit('save', formRef!)"
        >
          {{ getButtonTitle(ButtonAuth.SYS_AUTHORITY_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>
