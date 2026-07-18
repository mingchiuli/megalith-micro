<script lang="ts" setup>
import { ButtonAuth } from '@/type/entity'
import { checkButtonAuth, getButtonType, getButtonTitle } from '@/utils/permissions'
import type { ButtonProps } from 'element-plus'

const props = defineProps<{
  auth: ButtonAuth
  type?: ButtonProps['type']
  size?: ButtonProps['size']
  disabled?: boolean
  loading?: boolean
  icon?: ButtonProps['icon']
}>()

const emit = defineEmits<{
  click: []
}>()

const hasAuth = () => checkButtonAuth(props.auth)
const buttonType = () => props.type ?? getButtonType(props.auth)
const buttonTitle = () => getButtonTitle(props.auth)
</script>

<template>
  <el-button
    v-if="hasAuth()"
    :type="buttonType()"
    :size="size"
    :disabled="disabled"
    :loading="loading"
    :icon="icon"
    @click="emit('click')"
  >
    {{ buttonTitle() }}
  </el-button>
</template>
