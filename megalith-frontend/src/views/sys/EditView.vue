<script lang="ts" setup>
import { computed, defineAsyncComponent, onUnmounted, reactive, ref, useTemplateRef, watch } from 'vue'
import {
  type TagProps,
  type UploadFile,
  type UploadProps,
  type UploadRawFile,
  type UploadRequestOptions,
  type FormRules,
  type FormInstance,
  ElInput,
  type UploadUserFile
} from 'element-plus'
import { GET, POST, UPLOAD } from '@/http/http'
import {
  type EditForm,
  type SensitiveItem,
  type SensitiveTrans,
  Status,
  ButtonAuth,
  SensitiveType,
  type SensitiveExhibit,
  Colors,
  type BlogEdit
} from '@/type/entity'
import { useRoute } from 'vue-router'
import router from '@/router'
import { blogsStore, syncStore } from '@/stores/store'
import EditorLoadingItem from '@/components/sys/EditorLoadingItem.vue'
import { checkButtonAuth, getButtonType, getButtonTitle } from '@/utils/tools'

import { ytext, wsProvider, createIndexedDBProvider } from '@/config/sync'
import type { IndexeddbPersistence } from 'y-indexeddb'
import type { UserInfo } from '@/type/entity'

const route = useRoute()
const blogId = route.query.id as string | undefined

if (blogId) {
  syncStore().room = blogId
} else {
  const userStr = localStorage.getItem('userinfo')!
  const user: UserInfo = JSON.parse(userStr)
  syncStore().room = `init:${user.id}`
}

const initialized = ref(false)
let indexeddbProvider: IndexeddbPersistence | null = null

const form: EditForm = reactive({
  id: undefined,
  userId: undefined,
  title: undefined,
  description: undefined,
  content: undefined,
  status: undefined,
  link: undefined,
  sensitiveContentList: []
})

type SensitiveTagsItem = {
  element: SensitiveExhibit
  type: TagProps['type']
}

const sensitiveTags = computed(() => {
  const arr: SensitiveTagsItem[] = []
  form.sensitiveContentList.forEach((item) => {
    const str = getExhibitWords(item.type, form)
    const element: SensitiveExhibit = {
      content: str.substring(item.startIndex, item.endIndex),
      startIndex: item.startIndex,
      type: item.type
    }
    const type = getSensitiveType(item.type)
    arr.push({ element: element, type: type })
  })
  return arr
})

const fileList = computed(() => {
  const arr: UploadUserFile[] = []
  if (form.link) {
    arr.push({
      name: 'Cover',
      url: form.link
    })
  }
  return arr
})

const titleRef = useTemplateRef<InstanceType<typeof ElInput>>('title')
const descRef = useTemplateRef<InstanceType<typeof ElInput>>('desc')

//中文输入法的问题
const uploadPercentage = ref(0)
const showPercentage = ref(false)

const dialogVisible = ref(false)
const dialogImageUrl = ref('')

const formRef = useTemplateRef<FormInstance>('form')
const formRules = reactive<FormRules<EditForm>>({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'blur' }]
})

const createWsPromise = () => {
  return new Promise((resolve) => {
    if (wsProvider!.wsconnected) {
      resolve(true)
    } else {
      wsProvider!.on('sync', (isSynced: boolean) => {
        if (isSynced) {
          resolve(true)
        }
      })
    }
  })
}

// Add a counter to track form changes
const operationCount = ref(0)
const MAX_OPERATIONS = 10

// Watch for changes in form elements
watch(
  () => ({
    title: form.title,
    description: form.description,
    content: form.content,
    status: form.status,
    link: form.link,
    sensitiveContentList: form.sensitiveContentList
  }),
  async () => {
    operationCount.value++
    
    // When we reach the threshold, push all data
    if (operationCount.value >= MAX_OPERATIONS) {
      try {
        await pushAllData(form)
        console.log('Auto-saved after 10 operations')
      } catch (error) {
        console.error('Auto-save failed:', error)
      } finally {
        // Reset counter regardless of success or failure
        operationCount.value = 0
      }
    }
  },
  { deep: true }
)

const pushAllData = async (form: EditForm) => {
  await POST<null>('/sys/blog/edit/push/all', form)
}

const initializeEditor = async () => {
  try {
    // 0. 首先初始化 IndexedDB (提前初始化)
    indexeddbProvider = createIndexedDBProvider()
    const indexedDbSyncPromise = indexeddbProvider.whenSynced

    // 1. 等待 WebSocket
    const timeoutPromise = new Promise((_, reject) =>
      setTimeout(() => reject(new Error('WebSocket timeout')), 5000)
    )

    try {
      await Promise.race([createWsPromise(), timeoutPromise])
      // WebSocket 连接成功
      const wsText = ytext.toString()
      if (wsText) {
        form.content = wsText
        initialized.value = true
        await indexedDbSyncPromise // 确保 IndexedDB 同步完成
        return
      }
    } catch (error) {
      console.log('WebSocket 连接超时或失败', error)
    }

    await loadEditContent(form, blogId)

    // 2. 检查 serverText
    if (form.content) {
      wsProvider!.doc.transact(() => {
        ytext.delete(0, ytext.toString().length)
        ytext.insert(0, form.content!)
      })
      initialized.value = true
      await indexedDbSyncPromise // 确保 IndexedDB 同步完成
      return
    }

    // 3. 尝试使用 IndexedDB 数据
    await indexedDbSyncPromise
    const indexedDbText = ytext.toString()
    if (indexedDbText) {
      console.log('使用IndexedDB数据:', indexedDbText)
      form.content = indexedDbText
      initialized.value = true
      return
    }

    // 4. 如果都没有数据，使用默认值
    console.log('使用默认初始化文本')
    wsProvider!.doc.transact(() => {
      form.content = ''
      ytext.insert(0, '')
    })
    initialized.value = true
  } catch (error) {
    console.error('初始化过程出错:', error)
    initialized.value = true
  }
}

const upload = async (image: UploadRequestOptions) => {
  await uploadFile(image.file)
}

const uploadFile = async (file: UploadRawFile) => {
  const formdata = new FormData()
  formdata.append('image', file)
  const url = await UPLOAD('sys/blog/oss/upload', formdata, uploadPercentage, showPercentage)
  form.link = url
}

const handleRemove = async (_file: UploadFile) => {
  if (!form.link) return
  await GET<null>(`/sys/blog/oss/delete?url=${form.link}`)
  form.link = ''
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>('/sys/blog/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
        duration: 1000
      })
      blogsStore().pageNum = 1
      router.push({
        name: 'system-blogs'
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
  form.sensitiveContentList = form.sensitiveContentList.filter(
    (item) =>
      item.type !== sensitiveItem.type ||
      (item.startIndex !== sensitiveItem.startIndex && item.type === sensitiveItem.type)
  )
}

const dealSensitive = (payload: SensitiveTrans) => {
  let flag = true
  form.sensitiveContentList.forEach((item) => {
    if (
      (item.endIndex === payload.endIndex || item.startIndex === payload.startIndex) &&
      item.type === payload.type
    ) {
      flag = false
    }
  })
  if (flag) {
    const element: SensitiveItem = {
      endIndex: payload.endIndex,
      startIndex: payload.startIndex,
      type: payload.type
    }
    form.sensitiveContentList.push(element)
  }
}

const getSensitiveType = (type: SensitiveType) => {
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

const getExhibitWords = (type: SensitiveType, form: EditForm) => {
  let words: string
  if (SensitiveType.TITLE === type) {
    words = form.title!
  } else if (SensitiveType.DESCRIPTION === type) {
    words = form.description!
  } else {
    words = form.content!
  }
  return words
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

const CustomEditorItem = defineAsyncComponent({
  loader: () => import('@/components/sys/EditorItem.vue'),
  loadingComponent: EditorLoadingItem,
  delay: 200,
  errorComponent: EditorLoadingItem,
  timeout: 5000
})

const handleTitleSelect = () => {
  if (form.status !== Status.SENSITIVE_FILTER) {
    return
  }

  const title = titleRef.value!.input!
  let start = title.selectionStart!
  let end = title.selectionEnd!

  if (start > end) {
    const tmp = start
    start = end
    end = tmp
  }

  const selectedText = form.title?.substring(start, end)
  if (selectedText) {
    const sensitive: SensitiveItem = {
      startIndex: start,
      endIndex: end,
      type: SensitiveType.TITLE
    }
    dealSensitive(sensitive)
  }
}

const handleDescSelect = () => {
  if (form.status !== Status.SENSITIVE_FILTER) {
    return
  }
  const desc = descRef.value!.textarea!

  let start = desc.selectionStart!
  let end = desc.selectionEnd!

  if (start > end) {
    const tmp = start
    start = end
    end = tmp
  }

  const selectedText = form.description?.substring(start, end)
  if (selectedText) {
    const sensitive: SensitiveItem = {
      startIndex: start,
      endIndex: end,
      type: SensitiveType.DESCRIPTION
    }
    dealSensitive(sensitive)
  }
}

onUnmounted(async () => {
  if (indexeddbProvider) {
    await indexeddbProvider.clearData()
    indexeddbProvider.destroy()
  }
  wsProvider!.destroy()
})

const loadEditContent = async (form: EditForm, blogId: string | undefined) => {
  let url = '/sys/blog/edit/pull/echo'
  if (blogId) {
    url += `?blogId=${blogId}`
  }
  const data = await GET<BlogEdit>(url)
  form.title = data.title
  form.description = data.description
  form.content = data.content
  form.link = data.link
  form.status = data.status
  form.id = data.id
  form.userId = data.userId
  form.sensitiveContentList = data.sensitiveContentList
}

const init = async () => {
  await initializeEditor()
}

;(async () => {
  await init()
})()
</script>

<template>
  <div class="father">
    <el-form :model="form" :rules="formRules" ref="form">
      <el-form-item class="title" prop="title">
        <el-input
          ref="title"
          @select="handleTitleSelect"
          v-model="form.title"
          placeholder="标题"
          maxlength="20"
        />
      </el-form-item>

      <el-form-item class="desc" prop="description">
        <el-input
          ref="desc"
          @select="handleDescSelect"
          autosize
          type="textarea"
          v-model="form.description"
          placeholder="摘要"
          maxlength="60"
        />
      </el-form-item>

      <el-form-item class="status" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="Status.NORMAL">公开</el-radio>
          <el-radio :value="Status.BLOCK">隐藏</el-radio>
          <el-radio :value="Status.SENSITIVE_FILTER">打码</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.status === Status.SENSITIVE_FILTER" label="打码">
        <el-popover
          v-for="tag in sensitiveTags"
          :key="`${tag.element.type}-${tag.element.startIndex}`"
          placement="top-start"
          trigger="hover"
          :content="`${tag.element.content}`"
        >
          <template #reference>
            <el-tag closable :type="tag.type" @close="handleTagClose(tag)">
              {{ tag.element.startIndex }}
            </el-tag>
          </template>
        </el-popover>
      </el-form-item>

      <el-form-item class="cover" :label="getButtonTitle(ButtonAuth.SYS_BLOG_UPLOAD)">
        <el-upload
          v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_UPLOAD)"
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

      <el-form-item label="进度" class="progress" v-if="showPercentage">
        <el-progress type="line" :percentage="uploadPercentage" :color="Colors" />
      </el-form-item>

      <el-form-item class="content" prop="content">
        <CustomEditorItem
          v-model:content="form.content"
          @sensitive="dealSensitive"
          :form-status="form.status"
        />
      </el-form-item>

      <div class="submit-button">
        <el-button
          :type="getButtonType(ButtonAuth.SYS_EDIT_COMMIT)"
          v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_COMMIT)"
          @click="submitForm(formRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_EDIT_COMMIT) }}</el-button
        >
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.father {
  max-width: 40rem;
  margin: 0 auto;
}

.title {
  margin-top: 5px;
  width: 200px;
}

.desc {
  margin-top: 5px;
  width: 300px;
}

.progress {
  margin-top: 5px;
  width: 300px;
}

.status {
  margin-top: 5px;
  width: 300px;
}

.el-tag {
  margin: 5px;
}

.el-upload__text {
  margin-top: 5px;
  width: 290px;
}

.submit-button {
  margin: 10px auto;
  text-align: center;
}

.content {
  max-width: 40rem;
  margin: 5px auto;
}

.el-progress {
  width: 150px;
}
</style>
