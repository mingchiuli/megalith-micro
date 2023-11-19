<script lang="ts" setup>
import { onBeforeUnmount, onUnmounted, reactive, ref, watch } from 'vue'
import { type UploadFile, type UploadInstance, type UploadProps, type UploadRawFile, type UploadRequestOptions, type UploadUserFile, genFileId, type FormRules, type FormInstance } from 'element-plus'
import { GET, POST } from '@/http/http'
import { OperateTypeCode, Status } from '@/type/entity'
import { useRoute } from 'vue-router'
import type { BlogEdit } from '@/type/entity'
import router from '@/router'
import { tabStore } from '@/stores/store'
import { Client } from '@stomp/stompjs'

let timer: NodeJS.Timeout

const client = new Client({
  brokerURL: `${import.meta.env.VITE_BASE_WS_URL}/edit`,
  connectHeaders: { "Authorization": localStorage.getItem('accessToken')!, "Type": "EDIT" },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
})

const connect = () => {
  client.onConnect = _frame => {
    client.subscribe('/edits/push/all', _res => pushAllData())
  }

  client.activate()

  client.onStompError = frame => {
    ElNotification.error({
      title: 'Broker reported error: ' + frame.headers['message'],
      message: 'Additional details: ' + frame.body,
      showClose: true
    })
  }
}

type Form = {
  id?: number
  title?: string
  description?: string
  content?: string
  status?: number
  link?: string
}

const form: Form = reactive({
  id: undefined,
  title: undefined,
  description: undefined,
  content: undefined,
  status: undefined,
  link: undefined
})

type PushActionForm = {
  id?: number
  contentChange?: string
  operateTypeCode?: number
  version?: number
}

const pushActionForm: PushActionForm = {
  id: undefined,
  contentChange: undefined,
  operateTypeCode: undefined,
  version: undefined
}

let version = 0

const pushAllData = () => {
  client.publish({
    destination: '/app/edit/push/all',
    body: JSON.stringify(form)
  })
  version = 0
  if (transType.value !== 'warning') {
    transType.value = 'warning'
  }
}

const pushActionData = (pushActionForm: PushActionForm) => {
  client.publish({
    destination: '/app/edit/push/action',
    body: JSON.stringify(pushActionForm)
  })
  version++
  if (transType.value !== 'success') {
    transType.value = 'success'
  }
}

watch(() => form.content, (n, o) => {
  if (!client.connected || !n || !o) return

  pushActionForm.id = form.id

  let nLen = n.length
  let oLen = o.length
  let pushAll = false
  for (let i = 0; i < Math.min(nLen, oLen); i++) {
    if (n.charAt(i) != o.charAt(i)) {
      pushAll = true
    }
  }

  if (pushAll) {
    pushAllData()
    return
  }

  if (n.length > o.length) {
    pushActionForm.operateTypeCode = OperateTypeCode.APPEND
    pushActionForm.contentChange = n.substring(oLen)
  } else {
    pushActionForm.operateTypeCode = OperateTypeCode.SUBSTRACT
    pushActionForm.contentChange = o.substring(nLen)
  }
  pushActionForm.version = version
  pushActionData(pushActionForm)
})

const transType = ref('success')
const fileList = ref<UploadUserFile[]>([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const disabled = ref(false)
const md = ref<any>()
const route = useRoute()
let blogId = route.query.id

const uploadInstance = ref<UploadInstance>()
const formRef = ref<FormInstance>()
const formRules = reactive<FormRules<Form>>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ]
})

const loadEditContent = async () => {
  let data
  if (!blogId) {
    data = await GET<BlogEdit>('/sys/blog/echo')
  } else {
    data = await GET<BlogEdit>(`/sys/blog/echo?blogId=${blogId}`)
  }
  form.title = data.title
  form.description = data.description
  form.content = data.content
  form.link = data.link
  form.status = data.status
  form.id = data.id
  if (data.link) {
    fileList.value.push({
      name: 'Cover',
      url: data.link
    })
  }
}

const imgAdd = async (idx: number, file: File) => {
  const formdata = new FormData()
  formdata.append('image', file)
  formdata.append('nickname', JSON.parse(localStorage.getItem('userinfo')!).nickname)
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  setTimeout(() => md.value.$img2Url(idx, url), 2000)
}

const imgDel = async (pos: Array<any>) => {
  await GET<null>(`sys/blog/oss/delete?url=${pos[0]}`)
}

const upload = async (image: UploadRequestOptions) => {
  await uploadFile(image.file)
}

const uploadFile = async (file: UploadRawFile) => {
  const formdata = new FormData()
  formdata.append('image', file)
  formdata.append('nickname', JSON.parse(localStorage.getItem('userinfo')!).nickname)
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  setTimeout(() => {
    fileList.value.push({
      name: file.name,
      url: url
    })
    form.link = url
    ElNotification({
      title: '操作成功',
      message: '图片上传成功',
      type: 'success',
    })
  }, 2000)
}

const handleRemove = async (_file: UploadFile) => {
  await GET<null>(`sys/blog/oss/delete?url=${form.link}`)
  form.link = ''
  fileList.value = []
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>('/sys/blog/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
      })
      router.push({
        name: "system-blogs"
      })
    }
  })
}

const handleExceed: UploadProps['onExceed'] = async (files, _uploadFiles) => {
  uploadInstance.value!.clearFiles()
  const file = files[0] as UploadRawFile
  await uploadFile(file)
  file.uid = genFileId()
  uploadInstance.value!.handleStart(file)
}

const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('Avatar picture must be JPG/PNG format!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('Avatar picture size can not exceed 5MB!')
    return false
  }
  return true
}

onBeforeUnmount(() => pushAllData())

onUnmounted(() => {
  clearInterval(timer)
  stop()
  client.deactivate()
});

(async () => {
  await loadEditContent()

  tabStore().addTab({ title: '编辑博客', name: 'system-edit' })
  connect()
  timer = setInterval(() => {
    if (!client.connected) {
      ElNotification.warning("websocket reconnection ...")
      connect()
      pushAllData()
    }
  }, 2000)
})()
</script>

<template>
  <div class="father">
    <el-form :model="form" :rules="formRules" ref="formRef">
      <el-form-item class="title" prop="title">
        <el-input v-model="form.title" placeholder="标题" maxlength="10" />
      </el-form-item>

      <el-form-item class="desc" prop="description">
        <el-input autosize type="textarea" v-model="form.description" placeholder="摘要" maxlength="30" />
      </el-form-item>

      <el-form-item class="status" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label=Status.NORMAL>公开</el-radio>
          <el-radio :label=Status.BLOCK>隐藏</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item class="cover">
        <span style="margin-right: 10px;">封面</span>
        <el-upload action="#" list-type="picture-card" :before-upload="beforeAvatarUpload" :limit="1"
          :on-exceed="handleExceed" :http-request="upload" :on-remove="handleRemove" :file-list="fileList"
          ref="uploadInstance">
          <el-icon>
            <Plus />
          </el-icon>
          <template #file="{ file }">
            <div>
              <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
              <span class="el-upload-list__item-actions">
                <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                  <el-icon><zoom-in /></el-icon>
                </span>
                <span v-if="!disabled" class="el-upload-list__item-delete" @click="handleRemove(file)">
                  <el-icon>
                    <Delete />
                  </el-icon>
                </span>
              </span>
            </div>
          </template>
        </el-upload>

        <el-dialog v-model="dialogVisible">
          <img w-full :src="dialogImageUrl" alt="Preview Image" />
        </el-dialog>
      </el-form-item>

      <el-form-item class="content" prop="content">
        <mavon-editor :boxShadow="false" style="height: 100%" v-model="form.content" :subfield="false" :ishljs="true"
          ref="md" code-style="androidstudio" @imgAdd="imgAdd" @imgDel="imgDel" class="content" />
      </el-form-item>

      <div class="submit-button">
        <div class="trans-radius" v-if="transType === 'success'" style="background-color: #67c23a;" />
        <div class="trans-radius" v-else style="background-color: #e6a23c;" />
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
      </div>
    </el-form>

  </div>
</template>

<style scoped>

.trans-radius {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin: 5px auto;
}

.father {
  max-width: 40rem;
  margin: 0 auto;
}

.title {
  width: 200px;
}

.desc {
  width: 300px;
}

.status {
  width: 300px;
}

.el-upload__text {
  width: 290px;
}

.submit-button {
  display: block;
  margin: 10px auto;
  text-align: center;
}

.content {
  max-width: 40rem;
  margin: 0 auto;
}
</style>