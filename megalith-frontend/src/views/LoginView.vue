<script lang="ts" setup>
import type { LoginStruct } from '@/type/entity'
import { GET } from '@/http/http'
import { submitLogin } from '@/utils/tools'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { locale, t } = useI18n()
const mailButtonDisable = ref(false)
const smsButtonDisable = ref(false)
const buttonText = ref(t('auth.sendCode'))
const buttonMiles = ref(120)
const radioSelect = ref('Password')
const radioSMS = ref(false)
const radioEmail = ref(false)
const loginLoading = ref(false)
const loginInfo = reactive<LoginStruct>({
  username: '',
  password: ''
})

const triggerSubmitLogin = async (username: string, password: string) => {
  try {
    loginLoading.value = true
    await submitLogin(username, password)
  } finally {
    loginLoading.value = false
  }
}

const loginType = () => {
  switch (radioSelect.value) {
    case 'SMS':
      radioSMS.value = true
      radioEmail.value = false
      buttonText.value = t('auth.sendSms')
      break
    case 'Email':
      radioEmail.value = true
      radioSMS.value = false
      buttonText.value = t('auth.sendEmail')
      break
    default:
      radioSMS.value = false
      radioEmail.value = false
      break
  }
  loginInfo.password = ''
}

watch(locale, () => {
  if (buttonMiles.value < 120) {
    buttonText.value = t('auth.waitSeconds', { seconds: buttonMiles.value })
  } else if (radioSelect.value === 'Email') {
    buttonText.value = t('auth.sendEmail')
  } else if (radioSelect.value === 'SMS') {
    buttonText.value = t('auth.sendSms')
  } else {
    buttonText.value = t('auth.sendCode')
  }
})

let interval: NodeJS.Timeout | undefined
const sendCode = (via: string) => {
  if (!loginInfo.username) return
  smsButtonDisable.value = true
  mailButtonDisable.value = true
  GET(buildQueryUrl(API_ENDPOINTS.AUTH.SEND_CODE(via), { loginName: loginInfo.username }))
    .then(() => {
      ElMessage.success(t('auth.sent'))
      interval = setInterval(() => {
        buttonText.value = t('auth.waitSeconds', { seconds: buttonMiles.value })
        buttonMiles.value--
        if (buttonMiles.value <= -1) {
          clearInterval(interval)
          buttonText.value = t('auth.sendCode')
          mailButtonDisable.value = false
          smsButtonDisable.value = false
          buttonMiles.value = 120
        }
      }, 1000)
    })
    .catch(() => {
      mailButtonDisable.value = false
      smsButtonDisable.value = false
    })
}

onBeforeUnmount(() => {
  if (interval) {
    clearInterval(interval)
  }
})

const credentialPlaceholder = computed(() => {
  if (radioSelect.value === 'Password') return t('auth.password')
  return radioSelect.value === 'Email' ? t('auth.emailCode') : t('auth.smsCode')
})
</script>

<template>
  <div class="front">
    <el-radio-group v-model="radioSelect" class="dialog-select" size="small">
      <el-radio-button @change="loginType" :label="t('auth.password')" value="Password" />
      <el-radio-button @change="loginType" :label="t('auth.email')" value="Email" />
      <el-radio-button @change="loginType" :label="t('auth.sms')" value="SMS" />
    </el-radio-group>
    <div>
      <div>
        <el-input v-model="loginInfo.username" :placeholder="t('auth.loginName')" clearable />
      </div>
      <div>
        <el-input
          v-model="loginInfo.password"
          type="text"
          :placeholder="credentialPlaceholder"
          @keyup.enter="triggerSubmitLogin(loginInfo.username, loginInfo.password)"
          clearable
          :show-password="radioSelect === 'Password' ? true : false"
        />
      </div>
      <div class="dialog-footer">
        <el-button
          type="primary"
          :loading="loginLoading"
          :disabled="loginLoading"
          @click="triggerSubmitLogin(loginInfo.username, loginInfo.password)"
          >{{ t('auth.login') }}</el-button
        >
        <el-button
          type="primary"
          v-show="radioEmail"
          @click="sendCode('email')"
          :disabled="mailButtonDisable"
          >{{ buttonText }}</el-button
        >
        <el-button
          type="primary"
          v-show="radioSMS"
          @click="sendCode('sms')"
          :disabled="smsButtonDisable"
          >{{ buttonText }}</el-button
        >
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '@/assets/front.css';

.el-input {
  width: 180px;
  margin-left: 50%;
  transform: translate(-50%);
  margin-top: 10px;
}

.dialog-footer {
  margin-left: 50%;
  width: max-content;
  margin-top: 20px;
  transform: translate(-50%);
}

.dialog-select {
  margin-top: 30px;
  margin-left: 50%;
  width: max-content;
  transform: translate(-50%);
}
</style>
