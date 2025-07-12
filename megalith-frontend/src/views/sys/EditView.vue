<script lang="ts" setup>
import { computed, defineAsyncComponent, reactive, ref, useTemplateRef } from 'vue'
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
  type BlogEdit,
  type AiContentResp,
  type AiModelsResp,
  type AiContent,
  type AiModel
} from '@/type/entity'
import { useRoute } from 'vue-router'
import router from '@/router'
import { blogsStore } from '@/stores/store'
import EditorLoadingItem from '@/components/sys/EditorLoadingItem.vue'
import { checkButtonAuth, getButtonType, getButtonTitle, cleanJsonResponse } from '@/utils/tools'
import { MilkdownProvider } from '@milkdown/vue'
import { aiHttpClient } from '@/http/axios'
import 'element-plus/es/components/input/style/css' //不明原因样式缺失

const aiModels = ref<AiModel[]>([])
const aiModel = ref('')
const aiLoading = ref(false)
const route = useRoute()
const blogId = route.query.id as string | undefined
const form: EditForm = reactive({
  id: 0,
  userId: 0,
  title: '',
  description: '',
  content: '',
  status: 0,
  link: '',
  sensitiveContentList: [],
  owner: false
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

const titleRef = useTemplateRef<InstanceType<typeof ElInput>>('titleRef')
const descRef = useTemplateRef<InstanceType<typeof ElInput>>('descRef')
const editorRef = useTemplateRef<InstanceType<typeof CustomEditorItem>>('editorRef')

//中文输入法的问题
const uploadPercentage = ref(0)
const showPercentage = ref(false)

const dialogVisible = ref(false)
const dialogImageUrl = ref('')

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules<EditForm>>({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'blur' }]
})

const upload = async (image: UploadRequestOptions) => {
  await uploadFile(image.file)
}

const uploadFile = async (file: UploadRawFile) => {
  const formdata = new FormData()
  formdata.append('image', file)
  const url = await UPLOAD('sys/blog/oss/upload', formdata, uploadPercentage, showPercentage)
  form.link = url
}

const handleRemove = async () => {
  if (!form.link) return
  await GET<null>(`/sys/blog/oss/delete?url=${form.link}`)
  form.link = ''
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid) => {
    if (valid) {
      await POST<null>('/sys/blog/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
        duration: 1000
      })
      editorRef.value!.clearLocalData()
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
  timeout: 15000
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
  form.owner = data.owner
}

const loadAiModel = async () => {
  try {
    const response = await aiHttpClient.get('/api/tags')
    const result: AiModelsResp = response.data
    aiModels.value = result.models
  } catch (e) {
    console.warn(e)
  }
}

const aiGenerate = async () => {
  try {
    // 检查是否有内容
    if (!form.content || !aiModel.value) {
      return
    }

    // 设置按钮加载状态
    const prePrompt = `请仔细阅读以下文章：\n${form.content}`

    aiLoading.value = true
    const preResponse = await aiHttpClient.post('/api/generate', {
      model: aiModel.value, // 可用的模型
      prompt: prePrompt,
      stream: false
    })

    // 解析 AI 返回的结果
    const preResult: AiContentResp = preResponse.data
    const context = preResult.context
    
    const prompt = `请根据文章内容生成标题和摘要：
    
    输出要求：
    - 格式：严格的JSON字符串
    - title: 不超过10字的标题
    - description: 不超过50字的摘要
    - 不含任何额外字符和格式标记
    
    示例输出：
    {"title": "美好的一天", "description": "今天天气晴朗，心情愉快。"}
    
    注意事项：
    - 返回内容必须是可直接解析的JSON
    - 不要包含markdown、代码块等任何格式标记
    - JSON前后不能有空格或其他字符`
    
    const response = await aiHttpClient.post('/api/generate', {
      model: aiModel.value, // 可用的模型
      prompt,
      stream: false,
      context,
      options: {
        echo: false
      }
    })
    
    const result: AiContentResp = response.data
    
    const aiContent: AiContent = JSON.parse(cleanJsonResponse(result.response))

    // 更新表单数据
    if (aiContent.title && aiContent.description) {
      form.title = aiContent.title
      form.description = aiContent.description
    }
  } finally {
    // 无论成功与否，都关闭加载状态
    aiLoading.value = false
  }
}

;(async () => {
  await loadEditContent(form, blogId)
  await loadAiModel()
})()
</script>

<template>
  <div class="father">
    <el-form :model="form" :rules="formRules" ref="formRef">
      <el-form-item class="title" prop="title">
        <el-input
          ref="titleRef"
          @select="handleTitleSelect"
          v-model="form.title"
          placeholder="标题"
          maxlength="20"
          :disabled="!form.owner"
        />
      </el-form-item>

      <el-form-item class="desc" prop="description">
        <div class="desc-input-group">
          <el-input
            ref="descRef"
            @select="handleDescSelect"
            autosize
            type="textarea"
            v-model="form.description"
            placeholder="摘要"
            maxlength="60"
            :disabled="!form.owner"
          />

          <el-select v-model="aiModel" placeholder="模型" style="width: 140px">
            <el-option
              v-for="item in aiModels"
              :key="item.name"
              :label="item.name"
              :value="item.model"
            />
          </el-select>

          <el-button
            v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_AI)"
            color="#626aef"
            size="small"
            @click="aiGenerate"
            :loading="aiLoading"
            :disabled="aiLoading || !form.content || !aiModel"
            >✨AI</el-button
          >
        </div>
      </el-form-item>

      <el-form-item class="status" prop="status">
        <el-radio-group v-model="form.status" :disabled="!form.owner">
          <el-radio :value="Status.NORMAL">公开</el-radio>
          <el-radio :value="Status.BLOCK">隐藏</el-radio>
          <el-radio :value="Status.SENSITIVE_FILTER">打码</el-radio>
          <el-radio :value="Status.DRAFT">草稿</el-radio>
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
          :disabled="!form.owner"
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
        <MilkdownProvider>
          <CustomEditorItem
            ref="editorRef"
            v-model:content="form.content"
            @sensitive="dealSensitive"
            :form-status="form.status"
          />
        </MilkdownProvider>
      </el-form-item>

      <div class="submit-button">
        <el-button
          :disabled="!form.owner"
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
  margin-top: 15px;
  width: 200px;
}

.desc {
  margin-top: 25px;
  width: 500px;
}

.desc-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
  max-width: 800px; /* 限制最大宽度 */
}

.desc-input-group .el-input {
  flex: 1;
  min-width: 350px; /* 设置最小宽度确保输入框足够大 */
}

.progress {
  margin-top: 25px;
  width: 300px;
}

.status {
  margin-top: 25px;
  width: 300px;
}

.el-tag {
  margin: 5px;
}

.el-upload__text {
  margin-top: 25px;
  width: 290px;
}

.submit-button {
  margin: 10px auto;
  text-align: center;
}

.content {
  margin: 25px auto;
}

.el-progress {
  width: 150px;
}
</style>
