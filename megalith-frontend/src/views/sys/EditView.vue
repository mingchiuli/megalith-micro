<script lang="ts" setup>
import type {
  ElInput,
  FormInstance,
  FormRules,
  TagProps,
  UploadFile,
  UploadProps,
  UploadRawFile,
  UploadRequestOptions,
  UploadUserFile
} from 'element-plus'
import {GET, POST, UPLOAD} from '@/http/http'
import {
  type AiModel,
  type AiModelsResp,
  type BlogEdit,
  ButtonAuth,
  Colors,
  type EditForm,
  type SensitiveExhibit,
  type SensitiveItem,
  type SensitiveTrans,
  SensitiveType,
  Status
} from '@/type/entity'
import router from '@/router'
import EditorLoadingItem from '@/components/sys/EditorLoadingItem.vue'
import {checkButtonAuth, getButtonTitle, getButtonType} from '@/utils/tools'
import {aiHttpClient} from '@/http/axios'
import {API_CONFIG, API_ENDPOINTS, buildQueryUrl} from '@/config/apiConfig'
import {AI_MODELS} from '@/config/aiConfig'
import {logger} from '@/utils/logger'
import {ollamaStreamRequest, ollamaRequest, type StreamChunk} from '@/utils/ollamaStream'

const aiModels = ref<AiModel[]>([])
const aiModel = ref('')
const imageModel = AI_MODELS.IMAGE_MODEL
const aiLoading = ref(false)
const aiStep = ref(0) // 0=隐藏, 1=文本生成, 2=图片提示词, 3=封面图片
const aiThinking = ref('')
const aiThinkingCollapse = ref<string[]>(['thinking'])
const aiPanelVisible = ref(false)
const thinkingRef = useTemplateRef<HTMLDivElement>('thinkingRef')
const imageGenerating = ref(false)
const imageProgress = ref(0)
const submitLoading = ref(false)
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
  sensitiveContentList: []
})

const owner = ref(false)

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

//中文输入法的问题
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const uploadLoading = ref(false)

const dialogVisible = ref(false)
const dialogImageUrl = ref('')

// 图片生成预览 dialog
const generatedImageDialogVisible = ref(false)
const generatedImageUrl = ref('')
const generatedImageBase64 = ref('')

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
  const formData = new FormData()
  formData.append('image', file)
  form.link = await UPLOAD(
      API_ENDPOINTS.BLOG_ADMIN.OSS_UPLOAD,
      formData,
      uploadPercentage,
      showPercentage
  )
}

const handleRemove = async () => {
  if (!form.link) return
  await GET<null>(buildQueryUrl(API_ENDPOINTS.BLOG_ADMIN.OSS_DELETE, { url: form.link }))
  form.link = ''
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid) => {
    if (valid) {
      try {
        submitLoading.value = true
        await POST<null>(API_ENDPOINTS.BLOG_ADMIN.SAVE_BLOG, form)
        ElNotification({
          title: '操作成功',
          message: '编辑成功',
          type: 'success',
          duration: 1000
        })
        router.push({
          name: 'system-blogs'
        })
      } catch {
        submitLoading.value = false
      }
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

const loadContent = ref(true)
const loadEditContent = async (form: EditForm, blogId: string | undefined) => {
  let url = API_ENDPOINTS.BLOG_ADMIN.EDIT_PULL_ECHO
  if (blogId) {
    url = buildQueryUrl(url, { blogId })
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
  owner.value = data.owner
  loadContent.value = false
}

const loadAiModel = async () => {
  try {
    const response = await aiHttpClient.get(API_ENDPOINTS.AI.GET_MODELS)
    const result: AiModelsResp = response.data
    aiModels.value = result.models
  } catch (e) {
    logger.warn(e)
  }
}

const aiGenerate = async () => {
    if (!form.content || !aiModel.value) return

    aiPanelVisible.value = true
    aiStep.value = 1
    aiThinking.value = ''
    aiLoading.value = true

    try {
      let fullResponse = ''
      const prompt = `请仔细阅读以下文章：\n${form.content}，根据文章内容生成标题和摘要：

    输出要求：
    - 格式：严格的JSON字符串
    - title: 不超过10字的标题
    - description: 不超过50字的摘要
    - 不含任何额外字符和格式标记

    示例输出：
    {"title": "标题", "description": "文章摘要"}

    注意事项：
    - 返回内容必须是可直接解析的JSON
    - 不要包含markdown、代码块等任何格式标记
    - JSON前后不能有空格或其他字符`

      await ollamaStreamRequest({
        url: API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT,
        model: aiModel.value,
        prompt,
        onChunk: (chunk: StreamChunk) => {
          if (chunk.thinking) {
            aiThinking.value += chunk.thinking
            // 自动滚动到底部
            nextTick(() => {
              if (thinkingRef.value) {
                thinkingRef.value.scrollTop = thinkingRef.value.scrollHeight
              }
            })
          }
          if (chunk.response) {
            fullResponse += chunk.response
            const descMatch = fullResponse.match(/"description"\s*:\s*"([^"]*)"?/)
            if (descMatch?.[1]) {
              form.description = descMatch[1]
            } else {
              const titleMatch = fullResponse.match(/"title"\s*:\s*"([^"]*)"?/)
              if (titleMatch?.[1]) form.title = titleMatch[1]
            }
          }
        }
      })

      // 步骤1完成 → 步骤2：生成图片提示词（条件执行）
      if (!form.link && aiModels.value.some((m) => m.model === imageModel || m.name === imageModel)) {
        aiStep.value = 2
        await generateImagePrompt()
      } else {
        aiStep.value = 2 // 无图片生成，标记步骤1完成
      }
    } finally {
      aiLoading.value = false
    }
  }

// 生成封面图片提示词并生成图片
const generateImagePrompt = async () => {
    if (!form.content || !aiModel.value) return

    try {
      const prompt = `请仔细阅读以下文章：\n${form.content}，根据文章内容生成一张图片的英文提示词。

    输出要求：
    - 格式：严格的JSON字符串
    - imagePrompt: 适合Flux模型生成图片的英文提示词，不超过100字，描述文章的核心场景或意象
    - 不含任何额外字符和格式标记

    示例输出：
    {"imagePrompt": "A beautiful sunny day with blue sky and white clouds, person walking in a park with smile"}

    注意事项：
    - 返回内容必须是可直接解析的JSON
    - 不要包含markdown、代码块等任何格式标记
    - JSON前后不能有空格或其他字符`

      const text = await ollamaRequest(
        API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT,
        aiModel.value,
        prompt
      )
      if (!text) return

      const result = JSON.parse(text)
      if (result.imagePrompt) {
        aiStep.value = 3
        await generateImage(result.imagePrompt)
      }
    } catch (e) {
      logger.warn('图片提示词生成失败:', e)
    }
  }



const generateImage = async (prompt: string) => {
    let base64Image = ''
    imageGenerating.value = true
    imageProgress.value = 0

    await ollamaStreamRequest({
      url: API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT,
      model: imageModel,
      prompt,
      onChunk: (chunk: StreamChunk) => {
        if (chunk.completed !== undefined && chunk.total) {
          imageProgress.value = Math.round((chunk.completed / chunk.total) * 100)
        }
        if (chunk.image) base64Image = chunk.image
      }
    })

    imageGenerating.value = false
    imageProgress.value = 0

    if (base64Image) {
      generatedImageUrl.value = `data:image/png;base64,${base64Image}`
      generatedImageBase64.value = base64Image
      generatedImageDialogVisible.value = true
      aiStep.value = 4 // 全部完成，所有步骤变绿 ✓
    }
  }

const handleConfirmUpload = async () => {
  if (!generatedImageBase64.value) return

  uploadLoading.value = true
  try {
    // 将 base64 转换为 File 对象
    const base64Data = generatedImageBase64.value.replace(/^data:image\/\w+;base64,/, '')
    const byteCharacters = atob(base64Data)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: 'image/png' })
    const file: UploadRawFile = new File([blob], 'cover.png', { type: 'image/png' }) as UploadRawFile
    ;(file as UploadRawFile & { uid: number }).uid = Date.now()

    await uploadFile(file)
    generatedImageDialogVisible.value = false
  } catch (e) {
    logger.warn('图片上传失败:', e)
  } finally {
    uploadLoading.value = false
  }
}

const handleRegenerateImage = async () => {
  generatedImageDialogVisible.value = false
  if (form.content && aiModels.value.some((item) => item.model === imageModel || item.name === imageModel)) {
    await generateImagePrompt()
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
          :disabled="!owner"
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
            :disabled="!owner"
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
            :disabled="!owner || aiLoading || !form.content || !aiModel"
            >✨AI</el-button
          >
        </div>
      </el-form-item>

      <!-- AI 生成面板 -->
      <div v-if="aiPanelVisible" class="ai-panel">
        <el-steps :active="aiStep - 1" align-center finish-status="success">
          <el-step title="生成标题摘要" />
          <el-step title="生成图片提示词" />
          <el-step title="生成封面图片" />
        </el-steps>

        <el-collapse v-model="aiThinkingCollapse" class="thinking-collapse">
          <el-collapse-item title="💭 模型思考过程" name="thinking">
            <div class="thinking-content" ref="thinkingRef">
              {{ aiThinking || '等待模型思考...' }}
            </div>
          </el-collapse-item>
        </el-collapse>

        <el-form-item v-if="imageGenerating" label="图片生成进度" class="progress">
          <el-progress type="line" :percentage="imageProgress" :color="Colors" />
        </el-form-item>
      </div>

      <el-form-item class="status" prop="status">
        <el-radio-group v-model="form.status" :disabled="!owner">
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
          :disabled="!owner"
        >
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>

        <el-dialog v-model="dialogVisible">
          <img style="width: 100%" :src="dialogImageUrl" alt="" />
        </el-dialog>

        <!-- 图片生成预览 dialog -->
        <el-dialog
          v-model="generatedImageDialogVisible"
          title="封面图片预览"
          width="500px"
          :close-on-click-modal="true"
        >
          <div class="image-preview-container">
            <img v-if="generatedImageUrl" :src="generatedImageUrl" class="preview-image" alt="预览图片" />
          </div>
          <div class="upload-progress-wrapper">
            <el-progress
              v-if="generatedImageDialogVisible && showPercentage"
              type="line"
              :percentage="uploadPercentage"
              :color="Colors"
              class="dialog-progress"
            />
          </div>
          <template #footer>
            <el-button v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_AI)" @click="handleRegenerateImage" :loading="imageGenerating">重新生成</el-button>
            <el-button v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_UPLOAD)" type="primary" @click="handleConfirmUpload" :loading="uploadLoading">确认上传</el-button>
          </template>
        </el-dialog>
      </el-form-item>

      <el-form-item label="上传进度" class="progress" v-if="showPercentage && !generatedImageDialogVisible">
        <el-progress type="line" :percentage="uploadPercentage" :color="Colors" />
      </el-form-item>

      <el-form-item class="content" prop="content">
        <CustomEditorItem
          v-if="!loadContent"
          v-model:content="form.content"
          @sensitive="dealSensitive"
          :form-status="form.status"
          :owner="owner"
        />
      </el-form-item>

      <div class="submit-button">
        <el-button
          :disabled="submitLoading || !owner"
          :loading="submitLoading"
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

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dialog-progress {
  margin-top: 16px;
}

.upload-progress-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.ai-panel {
  margin-top: 16px;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.thinking-collapse {
  margin-top: 16px;
}

.thinking-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 12px;
  background: var(--el-bg-color);
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--el-text-color-regular);
}
</style>
