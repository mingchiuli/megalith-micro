<script lang="ts" setup>
import {
  type FormInstance,
  type FormRules,
  type UploadFile,
  type UploadProps,
  type UploadRawFile,
  type UploadRequestOptions,
  type UploadUserFile
} from 'element-plus'
import { GET, POST, UPLOAD } from '@/http/http'
import router from '@/router'
import { clearLoginState, submitLogin } from '@/utils/auth'
import { Colors } from '@/type/entity'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const saveLoading = ref(false)

type Form = {
  username: string
  nickname: string
  password: string
  confirmPassword: string
  avatar: string
  email: string
  phone?: string
  token: string
}

const form: Form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  avatar: '',
  email: '',
  phone: '',
  token: ''
})

const route = useRoute()
const registerToken = route.params.token as string
if (registerToken) {
  form.token = registerToken
}
const username = route.query.username as string
if (username) {
  form.username = username
}

const uploadPercentage = ref(0)
const showPercentage = ref(false)

GET<boolean>(buildQueryUrl(API_ENDPOINTS.AUTH.REGISTER_CHECK, { token: registerToken })).then(
  (res) => {
    if (!res) {
      router.push('/blogs')
    }
  }
)

const fileList = computed(() => {
  const arr: UploadUserFile[] = []
  if (form.avatar) {
    arr.push({
      name: t('auth.avatar'),
      url: form.avatar
    })
  }
  return arr
})

const dialogVisible = ref(false)
const dialogImageUrl = ref('')

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error(t('validation.passwordMismatch')))
    return
  }
  callback()
}

const formRules = computed<FormRules<Form>>(() => ({
  username: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.username') }),
      trigger: 'blur'
    }
  ],
  nickname: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.nickname') }),
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.password') }),
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: t('validation.enter', { field: t('auth.password') }),
      trigger: 'blur'
    },
    { validator: validatePassword, trigger: 'blur' }
  ],
  avatar: [
    {
      required: false,
      message: t('validation.enter', { field: t('auth.avatarUrl') }),
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: t('validation.enter', { field: t('auth.email') }), trigger: 'blur' }
  ],
  phone: [
    { required: false, message: t('validation.enter', { field: t('auth.phone') }), trigger: 'blur' }
  ]
}))

const formRef = useTemplateRef<FormInstance>('form')

const upload = async (image: UploadRequestOptions) => {
  await uploadFile(image.file)
}

const uploadFile = async (file: UploadRawFile) => {
  const formdata = new FormData()
  formdata.append('image', file)
  formdata.append('token', registerToken)
  const url = await UPLOAD(
    API_ENDPOINTS.AUTH.REGISTER_IMAGE_UPLOAD,
    formdata,
    uploadPercentage,
    showPercentage
  )
  fileList.value.push({
    name: file.name,
    url: url
  })
  form.avatar = url
  ElNotification({
    title: t('common.operationSuccess'),
    message: t('auth.imageUploadSuccess'),
    type: 'success'
  })
}

const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error(t('common.imageFormatError'))
    return false
  } else if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error(t('common.imageSizeError'))
    return false
  }
  return true
}

const submitForm = async (ref: FormInstance) => {
  saveLoading.value = true
  await ref.validate(async (valid) => {
    if (!valid) {
      saveLoading.value = false
      return
    }
    if (valid) {
      try {
        await POST<null>(API_ENDPOINTS.AUTH.REGISTER_SAVE, form)
        ElNotification({
          title: t('common.operationSuccess'),
          message: t('common.editSuccess'),
          type: 'success'
        })
        clearLoginState()
        await submitLogin(form.username, form.password)
      } finally {
        saveLoading.value = false
      }
    }
  })
}

const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

const handleRemove = async () => {
  if (!form.avatar) return
  await GET<null>(
    buildQueryUrl(API_ENDPOINTS.AUTH.REGISTER_IMAGE_DELETE, {
      url: form.avatar,
      token: registerToken
    })
  )
  form.avatar = ''
}
</script>

<template>
  <div class="front">
    <el-form :model="form" :rules="formRules" ref="form" class="father">
      <el-form-item :label="t('auth.username')" label-width="80" prop="username" class="username">
        <el-input
          v-model="form.username"
          maxlength="30"
          :disabled="username !== undefined && username !== ''"
        />
      </el-form-item>

      <el-form-item :label="t('auth.nickname')" label-width="80" prop="nickname" class="nickname">
        <el-input v-model="form.nickname" maxlength="30" />
      </el-form-item>

      <el-form-item :label="t('auth.password')" label-width="80" prop="password" class="password">
        <el-input v-model="form.password" type="password" maxlength="30" />
      </el-form-item>
      <el-form-item
        :label="t('auth.confirmPassword')"
        label-width="80"
        prop="confirmPassword"
        class="password"
      >
        <el-input v-model="form.confirmPassword" type="password" maxlength="30" />
      </el-form-item>

      <el-form-item class="avatar" label-width="40">
        <span style="margin-right: 10px">{{ t('auth.avatar') }}</span>
        <el-upload
          v-model:file-list="fileList"
          action="#"
          list-type="picture-card"
          :before-upload="beforeUpload"
          :limit="1"
          :http-request="upload"
          :on-remove="handleRemove"
          :on-preview="handlePictureCardPreview"
        >
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>

        <el-dialog v-model="dialogVisible">
          <img style="width: 100%" :src="dialogImageUrl" alt="" />
        </el-dialog>
      </el-form-item>

      <el-form-item
        v-if="showPercentage"
        :label="t('common.uploadProgress')"
        label-width="80"
        class="progress"
      >
        <el-progress type="line" :percentage="uploadPercentage" :color="Colors" />
      </el-form-item>

      <el-form-item :label="t('auth.email')" label-width="80" prop="email" class="email">
        <el-input v-model="form.email" maxlength="30" />
      </el-form-item>

      <el-form-item :label="t('auth.phone')" label-width="80" prop="phone" class="phone">
        <el-input v-model="form.phone" maxlength="30" />
      </el-form-item>

      <div class="submit-button">
        <el-button
          type="primary"
          :loading="saveLoading"
          :disabled="saveLoading"
          @click="submitForm(formRef!)"
          >{{ t('common.submit') }}</el-button
        >
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.father {
  max-width: 30rem;
  margin: 50px auto;
}

.submit-button {
  text-align: center;
}

.username {
  width: 300px;
}

.nickname {
  width: 300px;
}

.password {
  width: 300px;
}

.email {
  width: 300px;
}

.progress {
  width: 300px;
}

.phone {
  width: 300px;
}

.el-progress {
  width: 150px;
}
</style>
