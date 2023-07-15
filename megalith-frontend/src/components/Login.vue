<script lang="ts" setup>
import { reactive, ref, type Ref } from 'vue';
import { loginStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia';
import router from '@/router'
import type { LoginStruct, Token, Data } from '@/type/entity';
import axios from '@/axios';
import type { AxiosResponse } from 'axios';

const { login, loginDialog } = storeToRefs(loginStateStore())

const loginInfo: LoginStruct = reactive({
  username: '',
  password: ''
})

const submitLogin = async () => {
  const form: FormData = new FormData()
  form.append('username', loginInfo.username)
  form.append('password', loginInfo.password)
  const resp: AxiosResponse<Data<Token>> = await axios.post('/login', form)
  const token: Token = resp.data.data
  localStorage.setItem('accessToken', token.accessToken)
  localStorage.setItem('refreshToken', token.refreshToken)
  login.value = true
  loginDialog.value = false
  router.push({
    name: 'blogs'
  })
}

const beforeClose = (close: Function) => {
  router.push({
    name: 'blogs'
  })
  close()
}

const radioSelect: Ref<string> = ref('Password')
const radioSMS: Ref<boolean> = ref(false)
const radioEmail: Ref<boolean> = ref(false)

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
}

const mailButtonDisable: Ref<boolean> = ref(false)
const mailButtonText: Ref<any> = ref('发送邮件')
const mailButtonMiles: Ref<any> = ref(120)

let interval: NodeJS.Timeout
const sendCode = async (via: string) => {
    const resp: AxiosResponse<Data<null>> = await axios.get(`/code/${via}?loginEmail=${loginInfo.username}`)
    if (resp.status === 200) {
      //@ts-ignore  
      ElMessage.success('发送成功')
      mailButtonDisable.value = true
      interval = setInterval(() => {
        mailButtonText.value = `等待${mailButtonMiles.value}秒`
        mailButtonMiles.value--;
        if (mailButtonMiles.value <= -1) {
          clearInterval(interval)
          mailButtonText.value = '发送邮件'
          mailButtonDisable.value = false
          mailButtonMiles.value = 120
        }
      }, 1000)
  }
}

</script>

<template>
  <el-dialog v-model="loginDialog" center close-on-press-escape fullscreen align-center :before-close="beforeClose">
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
          <el-input v-model="loginInfo.password" type="password" :placeholder="radioSelect === 'Password' ? radioSelect : radioSelect + ' Code'" show-password clearable />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitLogin">登录</el-button>
        <el-button type="primary" v-show="radioEmail" @click="sendCode('email')" :disabled="mailButtonDisable">{{ mailButtonText }}</el-button>
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