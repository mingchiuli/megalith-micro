<script lang="ts" setup>
import { reactive } from 'vue';
import { loginStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia';
import router from '@/router'
import type { LoginStruct, Token, Data } from '@/type/entity';
import axios from '@/axios';
import type { AxiosResponse } from 'axios';

const { login } = storeToRefs(loginStateStore())

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
  login.value = false
  router.push({
    name: 'blogs'
  })
}


const beforeClose = (close: Function) => {
  login.value = true
  router.push({
    name: 'blogs'
  })
  close()
}

</script>

<template>
  <el-dialog v-model="login" center close-on-press-escape fullscreen align-center :before-close="beforeClose">
    <template #default>
      <div class="input">
        <div>
          <el-input v-model="loginInfo.username" placeholder="username" clearable />
        </div>
        <div>
          <el-input v-model="loginInfo.password" type="password" placeholder="password" show-password clearable />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitLogin">登录</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.el-input {
  width: 200px;
  left: 50%;
  transform: translate(-50%);
  margin-top: 10px;
}

.dialog-footer {
  margin-top: 0;
}
</style>