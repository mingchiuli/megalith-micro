<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import type { RoleSys } from '@/type/entity'
import { ButtonAuth, Status } from '@/type/entity'
import { checkButtonAuth, getButtonTitle, getButtonType } from '@/utils/permissions'
import { useI18n } from 'vue-i18n'

type UserForm = {
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

const props = defineProps<{
  visible: boolean
  form: UserForm
  rules: FormRules<UserForm>
  roles: RoleSys[]
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
      <el-form-item
        :label="t('auth.username')"
        label-width="100px"
        prop="username"
        class="username"
      >
        <el-input v-model="formModel.username" maxlength="30" />
      </el-form-item>
      <el-form-item
        :label="t('auth.nickname')"
        label-width="100px"
        prop="nickname"
        class="nickname"
      >
        <el-input v-model="formModel.nickname" maxlength="30" />
      </el-form-item>
      <el-form-item
        :label="t('auth.password')"
        label-width="100px"
        prop="password"
        class="password"
      >
        <el-input v-model="formModel.password" type="password" maxlength="30" />
      </el-form-item>
      <el-form-item :label="t('auth.avatarUrl')" label-width="100px" prop="avatar" class="avatar">
        <el-input v-model="formModel.avatar" />
      </el-form-item>
      <el-form-item :label="t('auth.email')" label-width="100px" prop="email" class="email">
        <el-input v-model="formModel.email" maxlength="30" />
      </el-form-item>
      <el-form-item :label="t('auth.phone')" label-width="100px" prop="phone" class="phone">
        <el-input v-model="formModel.phone" maxlength="30" />
      </el-form-item>
      <el-form-item :label="t('auth.role')" label-width="100px" prop="roles">
        <el-select
          v-model="formModel.roles"
          multiple
          class="role-option"
          :placeholder="t('common.select')"
        >
          <el-option v-for="role in roles" :key="role.code" :label="role.name" :value="role.code" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.status')" label-width="100px" prop="status">
        <el-radio-group v-model="formModel.status">
          <el-radio :value="Status.NORMAL">{{ t('common.enabled') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.disabled') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label-width="450px">
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_USER_SAVE)"
          :type="getButtonType(ButtonAuth.SYS_USER_SAVE)"
          @click="emit('save', formRef!)"
        >
          {{ getButtonTitle(ButtonAuth.SYS_USER_SAVE) }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
.role-option {
  width: 150px;
}
.username,
.nickname {
  width: 300px;
}
.password,
.email,
.phone {
  width: 400px;
}
.avatar {
  width: 500px;
}
</style>
