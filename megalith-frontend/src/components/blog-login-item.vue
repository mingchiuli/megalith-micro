<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { loginStateStore } from '@/stores/store'
import type { LoginStruct, Token, UserInfo } from '@/type/entity'
import { GET, POST } from '@/http/http'
import router from '@/router'
import http from '@/http/axios'

const props = defineProps<{
  loginDialogVisible: boolean
}>()

const emit = defineEmits<(event: 'update:loginDialogVisible', payload: boolean) => void>()

let loginDialogVisible = computed({
  get() {
    return props.loginDialogVisible
  },
  set(value) {
    emit('update:loginDialogVisible', value)
  },
})

const mailButtonDisable = ref(false)
const mailButtonText = ref('发送邮件')
const mailButtonMiles = ref(120)
const radioSelect = ref('Password')
const radioSMS = ref(false)
const radioEmail = ref(false)
const loginInfo = reactive<LoginStruct>({
  username: '',
  password: ''
})

const submitLogin = async () => {
  const form = new FormData()
  form.append('username', loginInfo.username)
  form.append('password', loginInfo.password)
  const token = await POST<Token>('/login', form)
  localStorage.setItem('accessToken', token.accessToken)
  localStorage.setItem('refreshToken', token.refreshToken)
  loginStateStore().login = true
  const info = await GET<UserInfo>('/token/userinfo')
  localStorage.setItem('userinfo', JSON.stringify(info))
  loginDialogVisible.value = false
  router.push({ name: 'blogs' })
}

const beforeClose = (close: Function) => {
  router.push({ name: 'blogs' })
  close()
}

const loginType = () => {
  switch (radioSelect.value) {
    case 'SMS':
      radioSMS.value = true
      radioEmail.value = false
      break
    case 'Email':
      radioEmail.value = true
      radioSMS.value = false
      break
    default:
      radioSMS.value = false
      radioEmail.value = false
      break;
  }
  loginInfo.username = ''
  loginInfo.password = ''
}

let interval: NodeJS.Timeout
const sendCode = (via: string) => {
  mailButtonDisable.value = true
  http.get(`/code/${via}?loginName=${loginInfo.username}`).then(_res => {
    ElMessage.success('发送成功')
    interval = setInterval(() => {
      mailButtonText.value = `等待${mailButtonMiles.value}秒`
      mailButtonMiles.value--
      if (mailButtonMiles.value <= -1) {
        clearInterval(interval)
        mailButtonText.value = '发送验证码'
        mailButtonDisable.value = false
        mailButtonMiles.value = 120
      }
    }, 1000)
  }).catch(_e => {
    mailButtonDisable.value = false
  })
}

</script>

<template>
  <el-dialog v-model="loginDialogVisible" center close-on-press-escape fullscreen align-center
    :before-close="beforeClose">
    <template #default>
      <el-radio-group v-model="radioSelect" class="dialog-select" size="small">
        <el-radio-button @change="loginType" label="Password" />
        <el-radio-button @change="loginType" label="Email" />
        <el-radio-button @change="loginType" label="SMS" />
      </el-radio-group>
      <div class="input">
        <div>
          <el-input v-model="loginInfo.username" placeholder="Login Name" clearable />
        </div>
        <div>
          <el-input v-model="loginInfo.password" type="password"
            :placeholder="radioSelect === 'Password' ? radioSelect : radioSelect + ' Code'" @keyup.enter="submitLogin"
            show-password clearable />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitLogin">登录</el-button>
        <el-button type="primary" v-show="radioEmail" @click="sendCode('email')" :disabled="mailButtonDisable">{{
          mailButtonText }}</el-button>
        <el-button type="primary" v-show="radioSMS" @click="sendCode('sms')" disabled>发送短信</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.el-input {
  width: 180px;
  left: 50%;
  transform: translate(-50%);
  margin-top: 10px;
}

.dialog-footer {
  margin-top: 0;
}

.dialog-select {
  margin-top: 30px;
  margin-left: 50%;
  width: max-content;
  transform: translate(-50%);
}
</style>