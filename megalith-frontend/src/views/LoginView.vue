<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { loginStateStore } from '@/stores/store'
import type { LoginStruct, Token, UserInfo } from '@/type/entity'
import { GET, POST } from '@/http/http'
import router from '@/router'
import http from '@/http/axios'

const mailButtonDisable = ref(false)
const smsButtonDisable = ref(false)
const buttonText = ref('')
const buttonMiles = ref(120)
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
  router.push({ name: 'blogs' })
}

const loginType = () => {
  switch (radioSelect.value) {
    case 'SMS':
      radioSMS.value = true
      radioEmail.value = false
      buttonText.value = '发送短信'
      break
    case 'Email':
      radioEmail.value = true
      radioSMS.value = false
      buttonText.value = '发送邮件'
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
  smsButtonDisable.value = true
  mailButtonDisable.value = true

  http.get(`/code/${via}?loginName=${loginInfo.username}`).then(_res => {
    ElMessage.success('发送成功')
    interval = setInterval(() => {
      buttonText.value = `等待${buttonMiles.value}秒`
      buttonMiles.value--
      if (buttonMiles.value <= -1) {
        clearInterval(interval)
        buttonText.value = '发送验证码'
        mailButtonDisable.value = false
        smsButtonDisable.value = false
        buttonMiles.value = 120
      }
    }, 1000)
  }).catch(_e => {
    mailButtonDisable.value = false
    smsButtonDisable.value = false
  })
}

</script>

<template>
  <div class="front">
    <el-radio-group v-model="radioSelect" class="dialog-select" size="small">
      <el-radio-button @change="loginType" label="Password" value="Password" />
      <el-radio-button @change="loginType" label="Email" value="Email" />
      <el-radio-button @change="loginType" label="SMS" value="SMS" />
    </el-radio-group>
    <div>
      <div>
        <el-input v-model="loginInfo.username" placeholder="Login Name" clearable />
      </div>
      <div>
        <el-input v-model="loginInfo.password" type="text"
          :placeholder="radioSelect === 'Password' ? radioSelect : radioSelect + ' Code'" @keyup.enter="submitLogin"
          clearable :show-password="radioSelect === 'Password' ? true : false" />
      </div>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitLogin">登录</el-button>
        <el-button type="primary" v-show="radioEmail" @click="sendCode('email')" :disabled="mailButtonDisable">{{
      buttonText }}</el-button>
        <el-button type="primary" v-show="radioSMS" @click="sendCode('sms')" :disabled="smsButtonDisable">{{ buttonText
          }}</el-button>
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
  margin-top: 10px
}

.dialog-footer {
  margin-left: 50%;
  width: max-content;
  margin-top: 20px;
  transform: translate(-50%)
}

.dialog-select {
  margin-top: 30px;
  margin-left: 50%;
  width: max-content;
  transform: translate(-50%)
}
</style>