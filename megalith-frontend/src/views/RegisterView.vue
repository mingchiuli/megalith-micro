<script lang="ts" setup>
import { computed, reactive, ref, useTemplateRef } from 'vue'
import { type FormInstance, type FormRules, type UploadFile, type UploadProps, type UploadRawFile, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { GET, POST, UPLOAD } from '@/http/http'
import router from '@/router'
import { useRoute } from 'vue-router'
import { clearLoginState, submitLogin } from '@/utils/tools'
import { Colors } from '@/type/entity'


type Form = {
  id?: number
  username: string
  nickname: string
  password: string
  confirmPassword: string
  avatar: string
  email: string
  phone?: string
}

const form: Form = reactive({
  id: undefined,
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  avatar: '',
  email: '',
  phone: ''
})

const route = useRoute()
const token = ref<string | string[]>()
token.value = route.params.token
const username = route.query.username as string
if (username) {
  form.username = username
}

const uploadPercentage = ref(0)
const showPercentage = ref(false)

GET<boolean>(`/sys/user/register/check?token=${token.value}`).then(res => {
  if (!res) {
    router.push('/blogs')
  }
})

const fileList = computed(() => {
  const arr: UploadUserFile[] = []
  if (form.avatar) {
    arr.push({
      name: 'Cover',
      url: form.avatar
    })
  }
  return arr
})

const dialogVisible = ref(false)
const dialogImageUrl = ref('')

const validatePassword = (_rule: any, value: string, callback: Function) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const formRules = reactive<FormRules<Form>>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  avatar: [
    { required: false, message: '请输入头像链接', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' }
  ],
  phone: [
    { required: false, message: '请输入手机号', trigger: 'blur' }
  ]
})


const formRef = useTemplateRef<FormInstance>('form')

const upload = async (image: UploadRequestOptions) => {
  await uploadFile(image.file)
}

const uploadFile = async (file: UploadRawFile) => {
  const formdata = new FormData()
  formdata.append('image', file)
  formdata.append('token', token.value as string)
  const url = await UPLOAD('sys/user/register/image/upload', formdata, uploadPercentage, showPercentage)
  fileList.value.push({
    name: file.name,
    url: url
  })
  form.avatar = url
  ElNotification({
    title: '操作成功',
    message: '图片上传成功',
    type: 'success',
  })
}

const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('Avatar picture must be JPG/PNG format!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('Avatar picture size can not exceed 5MB!')
    return false
  }
  return true
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>(`/sys/user/register/save?token=${token.value}`, form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
      })
      clearLoginState()
      await submitLogin(form.username, form.password)
    }
  })
}

const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

const handleRemove = async (_file: UploadFile) => {
  if (form.avatar) return
  await GET<null>(`/sys/user/register/image/delete?url=${form.avatar}&token=${token.value}`)
  form.avatar = ''
}

</script>

<template>
  <div class="front">

    <el-form :model="form" :rules="formRules" ref="form" class="father">

      <el-form-item label="用户名" label-width="80" prop="username" class="username">
        <el-input v-model="form.username" maxlength="30" :disabled="username !== undefined && username !== ''" />
      </el-form-item>

      <el-form-item label="昵称" label-width="80" prop="nickname" class="nickname">
        <el-input v-model="form.nickname" maxlength="30" />
      </el-form-item>

      <el-form-item label="密码" label-width="80" prop="password" class="password">
        <el-input v-model="form.password" type="password" maxlength="30" />
      </el-form-item>
      <el-form-item label="确认密码" label-width="80" prop="confirmPassword" class="password">
        <el-input v-model="form.confirmPassword" type="password" maxlength="30" />
      </el-form-item>

      <el-form-item class="avatar" label-width="40">
        <span style="margin-right: 10px;">头像</span>
        <el-upload v-model:file-list="fileList" action="#" list-type="picture-card" :before-upload="beforeUpload" :limit="1" :http-request="upload"
          :on-remove="handleRemove" :on-preview="handlePictureCardPreview">
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>

        <el-dialog v-model="dialogVisible">
          <img style="width: 100%;" :src="dialogImageUrl" alt="" />
        </el-dialog>
      </el-form-item>

      <el-form-item v-if="showPercentage" label="进度" label-width="80" class="progress">
        <el-progress type="line" :percentage="uploadPercentage" :color="Colors" />
      </el-form-item>

      <el-form-item label="邮箱" label-width="80" prop="email" class="email">
        <el-input v-model="form.email" maxlength="30" />
      </el-form-item>

      <el-form-item label="手机号" label-width="80" prop="phone" class="phone">
        <el-input v-model="form.phone" maxlength="30" />
      </el-form-item>

      <div class="submit-button">
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.father {
  max-width: 30rem;
  margin: 50px auto
}

.submit-button {
  text-align: center
}

.username {
  width: 300px
}

.nickname {
  width: 300px
}

.password {
  width: 300px
}

.email {
  width: 300px
}

.progress {
  width: 300px
}

.phone {
  width: 300px
}

.el-progress {
  width: 150px;
}
</style>