<script lang="ts" setup>
import { onUnmounted, reactive, ref, watch } from 'vue'
import { type UploadFile, type UploadInstance, type UploadProps, type UploadRawFile, type UploadRequestOptions, type UploadUserFile, genFileId, type FormRules, type FormInstance } from 'element-plus'
import { GET, POST } from '@/http/http'
import { OperaColor, OperateTypeCode, Status } from '@/type/entity'
import { useRoute } from 'vue-router'
import type { BlogEdit } from '@/type/entity'
import router from '@/router'
import { tabStore } from '@/stores/store'
import { Client } from '@stomp/stompjs'
import { MdEditor, type Footers, type ToolbarNames } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { ExportPDF, Emoji } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'

const toolbars: ToolbarNames[] = [
  'bold', 1, 'underline', 'italic', '-',
  'title', 'strikeThrough', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', 'task', '-',
  'codeRow', 'code', 'link', 'image', 'table', 'mermaid', 'katex', '-',
  'revoke', 'next', 'save', '=',
  0, 'pageFullscreen', 'fullscreen', 'preview', 'htmlPreview', 'catalog', 'github'
]
const footers: Footers[] = ['markdownTotal', '=', 0, 'scrollSwitch']


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
  indexStart?: number
  indexEnd?: number
}

const pushActionForm: PushActionForm = {
  id: undefined,
  contentChange: undefined,
  operateTypeCode: undefined,
  version: undefined,
  indexStart: undefined,
  indexEnd: undefined
}

let version = 0

const pushAllData = () => {
  client.publish({
    destination: '/app/edit/push/all',
    body: JSON.stringify(form)
  })
  version = 0
  if (transColor.value !== OperaColor.WARNING) {
    transColor.value = OperaColor.WARNING
  }
}

const pushActionData = (pushActionForm: PushActionForm) => {
  client.publish({
    destination: '/app/edit/push/action',
    body: JSON.stringify(pushActionForm)
  })
  version++
  if (transColor.value !== OperaColor.SUCCESS) {
    transColor.value = OperaColor.SUCCESS
  }
}

const clearPushActionForm = () => {
  pushActionForm.contentChange = undefined
  pushActionForm.indexEnd = undefined
  pushActionForm.indexStart = undefined
  pushActionForm.operateTypeCode = undefined
  pushActionForm.version = undefined
}

watch(() => form.content, (n, o) => {
  console.log('老的：' + o)
  console.log('新的: ' + n)
  if (!client.connected || (!n && !o)) return

  clearPushActionForm()

  pushActionForm.id = form.id
  pushActionForm.version = version

  //全部删除
  if (!n) {
    pushActionForm.operateTypeCode = OperateTypeCode.REMOVE
    pushActionData(pushActionForm)
    return
  }

  //初始化新增
  if (!o) {
    pushActionForm.contentChange = n
    pushActionForm.operateTypeCode = OperateTypeCode.TAIL_APPEND
    pushActionData(pushActionForm)
    return
  }

  let nLen = n.length
  let oLen = o.length
  const minLen = Math.min(nLen, oLen)

  let indexStart = oLen
  for (let i = 0; i < minLen; i++) {
    if (n.charAt(i) !== o.charAt(i)) {
      indexStart = i
      break
    }
  }

  if (indexStart === oLen) {
    //向末尾添加
    if (oLen < nLen) {
      pushActionForm.contentChange = n.substring(indexStart)
      pushActionForm.operateTypeCode = OperateTypeCode.TAIL_APPEND
    } else {
      //从末尾删除
      pushActionForm.indexStart = nLen
      pushActionForm.operateTypeCode = OperateTypeCode.TAIL_SUBTRACT
    }
    pushActionData(pushActionForm)
    return
  }

  let oIndexEnd = -1
  let nIndexEnd = -1
  for (let i = oLen - 1, j = nLen - 1; i >= 0 && j >= 0; i--, j--) {
    if (o.charAt(i) !== n.charAt(j)) {
      oIndexEnd = i + 1
      nIndexEnd = j + 1
      break
    }
  }

  if (oIndexEnd === -1) {
    //从开头添加
    if (oLen < nLen) {
      pushActionForm.contentChange = n.substring(0, nLen - oLen)
      pushActionForm.operateTypeCode = OperateTypeCode.HEAD_APPEND

    } else {
      //从开头删除
      pushActionForm.indexStart = oLen - nLen
      pushActionForm.operateTypeCode = OperateTypeCode.HEAD_SUBTRACT
    }
    pushActionData(pushActionForm)
    return
  }

  //中间插入重复字符
  if (indexStart > oIndexEnd) {
    let len = indexStart - oIndexEnd + 1
    let contentChange

    //增
    if (nLen > oLen) {
      contentChange = n.substring(oIndexEnd, oIndexEnd + len)
      pushActionForm.indexStart = oIndexEnd
      pushActionForm.indexEnd = oLen - (nLen - (oIndexEnd + len))
      //删
    } else {
      contentChange = n.substring(oIndexEnd - 1, oIndexEnd - 1 + len)
      pushActionForm.indexStart = oIndexEnd - 1
      pushActionForm.indexEnd = oLen - (nLen - (len + oIndexEnd - 1))
    }
    pushActionForm.contentChange = contentChange
    pushActionForm.operateTypeCode = OperateTypeCode.REPLACE
    pushActionData(pushActionForm)
    return
  }

  //中间正常插入
  const contentChange = n.substring(indexStart, nIndexEnd)
  pushActionForm.contentChange = contentChange
  pushActionForm.operateTypeCode = OperateTypeCode.REPLACE
  pushActionForm.indexStart = indexStart
  pushActionForm.indexEnd = oIndexEnd
  pushActionData(pushActionForm)
})

const transColor = ref(OperaColor.SUCCESS)
const fileList = ref<UploadUserFile[]>([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const disabled = ref(false)
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

const onUploadImg = async (files: File[], callback: Function) => {
  const formdata = new FormData()
  formdata.append('image', files[0])
  formdata.append('nickname', JSON.parse(localStorage.getItem('userinfo')!).nickname)
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  callback([url])
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

onUnmounted(() => {
  clearInterval(timer)
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
    }
  }, 2000)
})()
</script>

<template>
  <div class="father">
    <el-form :model="form" :rules="formRules" ref="formRef">
      <el-form-item class="title" prop="title">
        <el-input v-model="form.title" placeholder="标题" maxlength="20" />
      </el-form-item>

      <el-form-item class="desc" prop="description">
        <el-input autosize type="textarea" v-model="form.description" placeholder="摘要" maxlength="60" />
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
        <md-editor v-model="form.content" :preview="false" :toolbars="toolbars" :toolbarsExclude="['github']"
          @onUploadImg="onUploadImg" :footers="footers">
          <template #defToolbars>
            <Export-PDF v-model="form.content" />
            <emoji>
              <template #trigger> Emoji </template>
            </emoji>
          </template>
          <template #defFooters>
            <span class="trans-radius" :style="{ 'background-color': transColor }" />
          </template>
        </md-editor>
      </el-form-item>

      <div class="submit-button">
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
      </div>
    </el-form>

  </div>
</template>

<style scoped>
.trans-radius {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
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

#md-editor-v3:deep(.md-editor-footer) {
  height: 30px;
}
</style>
