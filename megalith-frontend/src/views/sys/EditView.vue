<script lang="ts" setup>
import { defineAsyncComponent, onUnmounted, reactive, ref, watch } from 'vue'
import { type TagProps, type UploadFile, type UploadInstance, type UploadProps, type UploadRawFile, type UploadRequestOptions, type UploadUserFile, genFileId, type FormRules, type FormInstance, ElInput } from 'element-plus'
import { GET, POST } from '@/http/http'
import { FieldName, FieldType, OperaColor, OperateTypeCode, ParaInfo, Status, ButtonAuth, ActionType, SensitiveType } from '@/type/entity'
import { useRoute } from 'vue-router'
import type { BlogEdit, EditForm, PushActionForm, SensitiveItem, SensitiveTrans } from '@/type/entity'
import router from '@/router'
import { blogsStore } from '@/stores/store'
import { Client, StompSocketState, type StompSubscription } from '@stomp/stompjs'
import EditorLoadingItem from '@/components/sys/EditorLoadingItem.vue'
import { checkAccessToken, checkButtonAuth, getButtonType, getButtonTitle, dealAction } from '@/utils/tools'

let timer: NodeJS.Timeout
const route = useRoute()
const blogId = route.query.id

let client = new Client({
  brokerURL: `${import.meta.env.VITE_BASE_WS_URL}/edit/ws`,
  connectHeaders: { "Authorization": localStorage.getItem('accessToken')!, "Type": "EDIT", "Identity": String(blogId) },
  reconnectDelay: 2000,
  heartbeatIncoming: 2000,
  heartbeatOutgoing: 2000,
  connectionTimeout: 2000
})

let pushAllSubscribe: StompSubscription
let pullAllSubscribe: StompSubscription

const connect = async () => {
  const key = form.id ? `${form.userId}/${form.id}` : form.userId!.toString()
  client.onConnect = _frame => {
    pushAllSubscribe = client.subscribe(`/edits/push/${key}`, async _res => {
      await pushAllData()
    })

    pullAllSubscribe = client.subscribe(`/edits/pull/${key}`, async _res => {
      await pullAllData()
    })
  }
  client.activate()
}

type SensitiveTagsItem = {
  element: SensitiveItem
  type: TagProps['type']
}

const sensitiveTags = ref<SensitiveTagsItem[]>([])

const titleRef = ref<InstanceType<typeof ElInput>>()
const descRef = ref<InstanceType<typeof ElInput>>()


const form: EditForm = reactive({
  id: undefined,
  userId: undefined,
  title: undefined,
  description: undefined,
  content: undefined,
  status: undefined,
  link: undefined,
  version: undefined,
  sensitiveContentList: []
})

const pushActionForm: PushActionForm = {
  id: undefined,
  contentChange: undefined,
  operateTypeCode: undefined,
  version: undefined,
  indexStart: undefined,
  indexEnd: undefined,
  field: undefined,
  paraNo: undefined,
}

let version = -1
//中文输入法的问题
let composing = false
let fieldType: FieldType
const readOnly = ref(false)
const netErrorEdited = ref(false)
let pulling = false

const pullAllData = async () => {
  pulling = true
  await loadEditContent()
  pulling = false
}

const pushAllData = async () => {
  form.version = version
  await POST<null>('/sys/blog/edit/push/all', form)
  if (transColor.value !== OperaColor.WARNING) {
    transColor.value = OperaColor.WARNING
  }
}

const pushActionData = (pushActionForm: PushActionForm) => {
  client.publish({
    destination: '/app/edit/ws/push/action',
    body: JSON.stringify(pushActionForm)
  })
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
  pushActionForm.field = undefined
  pushActionForm.paraNo = undefined
}

const preCheck = (n: string | undefined, o: string | undefined): boolean => {

  if (client.webSocket?.readyState !== StompSocketState.OPEN) {
    if (!netErrorEdited.value) {
      netErrorEdited.value = true
    }
    return false
  }

  if (!n && !o) {
    return false
  }

  if (composing) {
    return false
  }

  if (reconnecting) {
    return false
  }

  if (pulling) {
    return false
  }

  return true
}

watch(() => form.description, (n, o) => {
  if (!preCheck(n, o)) return
  commonPreDeal(FieldType.NON_PARA, FieldName.DESCRIPTION)
  deal(n, o)
})

watch(() => form.status, (n, o) => {
  if (!client.connected || (!n && !o) || composing) return
  commonPreDeal(FieldType.NON_PARA, FieldName.STATUS)
  pushActionForm.operateTypeCode = OperateTypeCode.STATUS
  pushActionForm.contentChange = String(form.status)
  pushActionData(pushActionForm)
})

watch(() => sensitiveTags, (n, o) => {
  if (!client.connected || (!n && !o) || composing) return
  commonPreDeal(FieldType.NON_PARA, FieldName.SENSITIVE_CONTENT_LIST)
  pushActionForm.operateTypeCode = OperateTypeCode.SENSITIVE_CONTENT_LIST
  form.sensitiveContentList = []
  sensitiveTags.value.forEach(item => form.sensitiveContentList.push(item.element))
  pushActionForm.contentChange = JSON.stringify(form.sensitiveContentList)
  pushActionData(pushActionForm)
}, { deep: true })

watch(() => form.link, (n, o) => {
  if (!preCheck(n, o)) return
  commonPreDeal(FieldType.NON_PARA, FieldName.LINK)
  deal(n, o)
})

watch(() => form.title, (n, o) => {
  if (!preCheck(n, o)) return
  commonPreDeal(FieldType.NON_PARA, FieldName.TITLE)
  deal(n, o)
})

watch(() => form.content, (n, o) => {
  if (!preCheck(n, o)) return
  commonPreDeal(FieldType.PARA, FieldName.CONTENT)

  const nArr = n?.split(ParaInfo.PARA_SPLIT)
  const oArr = o?.split(ParaInfo.PARA_SPLIT)

  //本段内操作
  if (nArr?.length === oArr?.length) {
    for (let i = 0; i < nArr?.length!; i++) {
      //理论上一个动作改不了很多段
      if (nArr![i] !== oArr![i]) {
        pushActionForm.paraNo = i + 1
        deal(nArr![i], oArr![i])
      }
    }
    return
  }
  //向后新增段
  const nLen = nArr?.length!
  const oLen = oArr?.length!
  if (nLen - 1 === oLen && nArr![oLen - 1] + '\n' === oArr![oLen - 1] && nArr![oLen] === '') {
    //每段必须相同，否则推全量
    for (let i = 0; i < oLen; i++) {
      if (i !== oLen - 1 && nArr![i] !== oArr![i]) {
        pushAllData()
        return
      }
      if (i === oLen - 1 && nArr![i] + '\n' !== oArr![i]) {
        pushAllData()
        return
      }
    }
    pushActionForm.paraNo = nLen
    pushActionForm.operateTypeCode = OperateTypeCode.PARA_SPLIT_APPEND
    pushActionData(pushActionForm)
    return
  }

  //向前减少段
  if (nLen + 1 === oLen && nArr![nLen - 1] === oArr![nLen - 1] + '\n' && oArr![nLen] === '') {
    for (let i = 0; i < nLen; i++) {
      if (i !== nLen - 1 && nArr![i] !== oArr![i]) {
        pushAllData()
        return
      }
      if (i === nLen - 1 && nArr![i] !== oArr![i] + '\n') {
        pushAllData()
        return
      }
    }
    pushActionForm.paraNo = oLen
    pushActionForm.operateTypeCode = OperateTypeCode.PARA_SPLIT_SUBTRACT
    pushActionData(pushActionForm)
    return
  }

  //推全量
  pushAllData()
})

const commonPreDeal = (fieldTypeParam: FieldType, opreateField: FieldName) => {
  clearPushActionForm()
  pushActionForm.field = opreateField
  pushActionForm.id = form.id
  pushActionForm.version = ++version
  fieldType = fieldTypeParam
}

const deal = (n: string | undefined, o: string | undefined) => {
  const type = dealAction(n, o, pushActionForm, fieldType)
  if (ActionType.PUSH_ACTION === type) {
    pushActionData(pushActionForm)
    return
  }

  if (ActionType.PULL_ALL === type) {
    pushAllData()
  }
}

const transColor = ref(OperaColor.SUCCESS)
const fileList = ref<UploadUserFile[]>([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const disabled = ref(false)

const uploadInstance = ref<UploadInstance>()
const formRef = ref<FormInstance>()
const formRules = reactive<FormRules<EditForm>>({
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
    data = await GET<BlogEdit>('/sys/blog/edit/pull/echo')
  } else {
    data = await GET<BlogEdit>(`/sys/blog/edit/pull/echo?blogId=${blogId}`)
  }
  form.title = data.title
  form.description = data.description
  form.content = data.content
  form.link = data.link
  form.status = data.status
  form.id = data.id
  form.userId = data.userId
  //不清空会重复显示
  if (sensitiveTags.value.length !== 0) {
    sensitiveTags.value = []
  }
  data.sensitiveContentList.forEach(item => {
    let type = getSensitiveType(item.type)
    sensitiveTags.value.push({ element: item, type: type })
  })
  if (form.sensitiveContentList.length !== 0) {
    form.sensitiveContentList = []
  }
  sensitiveTags.value.forEach(item => form.sensitiveContentList.push(item.element))
  version = data.version
  if (fileList.value.length !== 0) {
    //移除全部
    fileList.value.splice(0, fileList.value.length)
  }
  if (data.link) {
    fileList.value.push({
      name: 'Cover',
      url: data.link
    })
  }
}

const upload = async (image: UploadRequestOptions) => {
  await uploadFile(image.file)
}

const uploadFile = async (file: UploadRawFile) => {
  const formdata = new FormData()
  formdata.append('image', file)
  const url = await POST<string>('sys/blog/oss/upload', formdata)
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
}

const handleRemove = async (_file: UploadFile) => {
  await GET<null>(`/sys/blog/oss/delete?url=${form.link}`)
  form.link = ''
  fileList.value = []
}

const handleExceed: UploadProps['onExceed'] = async (files, _uploadFiles) => {
  GET<null>(`/sys/blog/oss/delete?url=${form.link}`)
  uploadInstance.value!.clearFiles()
  const file = files[0] as UploadRawFile
  await uploadFile(file)
  file.uid = genFileId()
  uploadInstance.value!.handleStart(file)
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
      blogsStore().pageNum = 1
      router.push({
        name: "system-blogs"
      })
    }
  })
}

const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

const handleTagClose = (tag: SensitiveTagsItem) => {
  const sensitiveItem = tag.element
  sensitiveTags.value = sensitiveTags.value.filter(item => !(item.element.content === sensitiveItem.content && item.element.startIndex === sensitiveItem.startIndex && item.element.type === sensitiveItem.type))
}

const dealComposing = (payload: boolean) => composing = payload

const dealSensitive = (payload: SensitiveTrans) => {
  if (form.status !== Status.SENSITIVE_FILTER) {
    return
  }

  let flag = true
  sensitiveTags.value.forEach(item => {
    if (item.element.content === payload.content && item.element.startIndex === payload.startIndex && item.element.type === payload.type) {
      flag = false
    }
  })
  if (flag) {
    let type = getSensitiveType(payload.type)
    const element: SensitiveItem = {
      content: payload.content,
      startIndex: payload.startIndex,
      type: payload.type
    }
    sensitiveTags.value.push({ element: element, type: type })
  }
}

const getSensitiveType = (type: number) => {

  let typeProp: TagProps['type']
  if (SensitiveType.TITLE === type) {
    typeProp = 'success'
  } else if (SensitiveType.DESCRIPTION === type) {
    typeProp = 'primary'
  } else {
    typeProp = 'warning'
  }
  return typeProp
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

const CustomEditorItem = defineAsyncComponent({
  loader: () => import('@/components/sys/EditorItem.vue'),
  loadingComponent: EditorLoadingItem,
  delay: 200,
  errorComponent: EditorLoadingItem,
  timeout: 5000
})

const handleTitleSelect = () => {
  const title = titleRef.value?.input!
  console.log(title)
  const start = title.selectionStart!
  const end = title.selectionEnd!
  console.log(start, end)

  const selectedText = title.textContent.substring(start, end)
  console.log(title.textContent)

  if (selectedText) {
    const sensitive: SensitiveItem = {
      content: selectedText,
      startIndex: getSelection()?.anchorOffset!,
      type: SensitiveType.TITLE
    }
    dealSensitive(sensitive)
  }
}

const handleDescSelect = () => {
  const desc = descRef.value?.input!
  console.log(desc)

  const start = desc.selectionStart!
  const end = desc.selectionEnd!
  const selectedText = desc.innerText.substring(start, end)
  if (selectedText) {
    const sensitive: SensitiveItem = {
      content: selectedText,
      startIndex: getSelection()?.anchorOffset!,
      type: SensitiveType.DESCRIPTION
    }
    dealSensitive(sensitive)
  }
}

onUnmounted(() => {
  clearInterval(timer)
  pushAllSubscribe.unsubscribe()
  pullAllSubscribe.unsubscribe()
  client.deactivate()
})

let reconnecting = false;
(async () => {
  await loadEditContent()
  await connect()
  timer = setInterval(async () => {
    if (client.webSocket?.readyState !== StompSocketState.OPEN) {
      transColor.value = OperaColor.FAILED
      readOnly.value = true
      const token = await checkAccessToken()
      client.connectHeaders = { "Authorization": token, "Type": "EDIT" }
      reconnecting = true
    }
    if (reconnecting && client.webSocket?.readyState === StompSocketState.OPEN) {
      if (netErrorEdited.value) {
        await pushAllData()
      } else {
        await pullAllData()
      }
      readOnly.value = false
      transColor.value = OperaColor.SUCCESS
      reconnecting = false
      netErrorEdited.value = false
    }
  }, 2000)
})()
</script>

<template>
  <div class="father">
    <el-form :model="form" :rules="formRules" ref="formRef">
      <el-form-item class="title" prop="title">
        <el-input ref="titleRef" @select="handleTitleSelect" v-model="form.title" placeholder="标题" maxlength="20"
          :disabled="readOnly" />
      </el-form-item>

      <el-form-item class="desc" prop="description">
        <el-input ref="descRef" @select="handleDescSelect" autosize type="textarea" v-model="form.description"
          placeholder="摘要" maxlength="60" :disabled="readOnly" />
      </el-form-item>

      <el-form-item class="status" prop="status">
        <el-radio-group v-model="form.status" :disabled="readOnly">
          <el-radio :value=Status.NORMAL>公开</el-radio>
          <el-radio :value=Status.BLOCK>隐藏</el-radio>
          <el-radio :value=Status.SENSITIVE_FILTER>打码</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.status === Status.SENSITIVE_FILTER">
        <span style="margin-right: 10px;">打码</span>
        <div>
          <el-tag v-for="tag in sensitiveTags" :key="tag.element" closable :type="tag.type"
            @close="handleTagClose(tag)">
            {{ tag.element.startIndex }} - {{ tag.element.content }}
          </el-tag>
        </div>
      </el-form-item>

      <el-form-item class="cover">
        <span style="margin-right: 10px;">封面</span>
        <el-upload action="#" list-type="picture-card" :before-upload="beforeAvatarUpload" :limit="1"
          :on-exceed="handleExceed" :http-request="upload" :on-remove="handleRemove" :file-list="fileList"
          ref="uploadInstance" :disabled="readOnly">
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
          <img style="width: 100%;" :src="dialogImageUrl" alt="" />
        </el-dialog>
      </el-form-item>

      <el-form-item class="content" prop="content">
        <CustomEditorItem v-model:content="form.content" @composing="dealComposing" @sensitive="dealSensitive"
          :trans-color="transColor" />
      </el-form-item>

      <div class="submit-button">
        <el-button :type="getButtonType(ButtonAuth.SYS_EDIT_COMMIT)" v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_COMMIT)"
          @click="submitForm(formRef!)" :disabled="readOnly">{{ getButtonTitle(ButtonAuth.SYS_EDIT_COMMIT)
          }}</el-button>
      </div>
    </el-form>

  </div>
</template>

<style scoped>
.father {
  max-width: 40rem;
  margin: 0 auto
}

.title {
  margin-top: 5px;
  width: 200px
}

.desc {
  margin-top: 5px;
  width: 300px
}

.status {
  margin-top: 5px;
  width: 300px
}

.el-tag {
  margin-right: 10px;
}

.el-upload__text {
  margin-top: 5px;
  width: 290px
}

.submit-button {
  margin: 10px auto;
  text-align: center
}

.content {
  max-width: 40rem;
  margin: 5px auto
}
</style>
