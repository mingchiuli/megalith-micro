<script lang="ts" setup>
import {computed, defineAsyncComponent, reactive, ref, useTemplateRef} from 'vue'
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
import {useRoute} from 'vue-router'
import router from '@/router'
import EditorLoadingItem from '@/components/sys/EditorLoadingItem.vue'
import {checkButtonAuth, getButtonTitle, getButtonType} from '@/utils/tools'
import {aiHttpClient} from '@/http/axios'
import {API_CONFIG, API_ENDPOINTS} from '@/config/apiConfig'

const aiModels = ref<AiModel[]>([])
const aiModel = ref('')
const imageModel = 'x/flux2-klein:9b-bf16'
const aiLoading = ref(false)
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
  await GET<null>(`${API_ENDPOINTS.BLOG_ADMIN.OSS_DELETE}?url=${form.link}`)
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
  owner.value = data.owner
  loadContent.value = false
}

const loadAiModel = async () => {
  try {
    const response = await aiHttpClient.get(API_ENDPOINTS.AI.GET_MODELS)
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

    aiLoading.value = true

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

    // 使用 Fetch API 实现真正的流式处理
    const response = await fetch(API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: aiModel.value,
        prompt,
        stream: true,
        options: {
          echo: false
        }
      })
    })

    if (!response.ok) {
      return
    }

    // 获取流式读取器
    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullResponse = ''

    if (reader) {
      while (true) {
        // 读取数据块（不 await 整个响应，而是逐块读取）
        const { done, value } = await reader.read()

        if (done) break

        // 解码当前数据块
        buffer += decoder.decode(value, { stream: true })

        // 按行分割处理 NDJSON
        const lines = buffer.split('\n')
        // 保留最后一个可能不完整的行
        buffer = lines.pop() || ''

        // 处理完整的行
        for (const line of lines) {
          if (line.trim()) {
            const json = JSON.parse(line)

            // 累积响应内容
            if (json.response) {
              fullResponse += json.response

              // 尝试提取 title 和 description（即使 JSON 不完整）
              // 使用正则表达式提取部分内容
              const descMatch = fullResponse.match(/"description"\s*:\s*"([^"]*)"?/)

              if (descMatch && descMatch[1]) {
                // 逐字显示 description
                form.description = descMatch[1]
              } else {
                // description 还未出现时，才更新 title
                const titleMatch = fullResponse.match(/"title"\s*:\s*"([^"]*)"?/)
                if (titleMatch && titleMatch[1]) {
                  form.title = titleMatch[1]
                }
              }
            }

            // 检查是否完成
            if (json.done) {
              break
            }
          }
        }
      }
    }

    // 如果链接为空且存在图片生成模型，生成封面图片
    if (!form.link && aiModels.value.some(m => m.name === imageModel)) {
      await generateImagePrompt()
    }
  } finally {
    // 无论成功与否，都关闭加载状态
    aiLoading.value = false
  }
}

// 生成封面图片提示词并生成图片
const generateImagePrompt = async () => {
  if (!form.content || !aiModel.value) {
    return
  }

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

    const response = await fetch(API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: aiModel.value,
        prompt,
        stream: false
      })
    })

    if (!response.ok) {
      return
    }

    const json = await response.json()

    // 非 stream 格式: response 在 json.response 中
    if (json.done && json.response) {
      const result = JSON.parse(json.response)
      if (result.imagePrompt) {
        await generateImage(result.imagePrompt)
      }
    }
  } catch (e) {
    console.warn('图片提示词生成失败:', e)
  }
}



const generateImage = async (prompt: string) => {
  let base64Image = ''
  imageGenerating.value = true
  imageProgress.value = 0
  try {
    const response = await fetch(API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: imageModel,
        prompt,
        stream: true
      })
    })

    if (!response.ok) {
      return
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    if (reader) {
      while (true) {
        const { done, value } = await reader.read()

        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.trim()) {
            const json = JSON.parse(line)

            // 更新进度
            if (json.completed !== undefined && json.total) {
              imageProgress.value = Math.round((json.completed / json.total) * 100)
            }

            // 收集 base64 图片数据
            if (json.image) {
              base64Image = json.image
            }
          }
        }
      }
    }
  } finally {
    imageGenerating.value = false
    imageProgress.value = 0
  }

  // 展示图片预览 dialog，不自动上传
  if (base64Image) {
    generatedImageUrl.value = `data:image/png;base64,${base64Image}`
    generatedImageBase64.value = base64Image
    generatedImageDialogVisible.value = true
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
    console.warn('图片上传失败:', e)
  } finally {
    uploadLoading.value = false
  }
}

const handleRegenerateImage = async () => {
  generatedImageDialogVisible.value = false
  if (form.content && aiModels.value.some(item => item.model === imageModel)) {
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
            <span class="progress-label">生成进度</span>
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

      <el-form-item label="生成进度" class="progress" v-if="imageGenerating">
        <el-progress type="line" :percentage="imageProgress" :color="Colors" />
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
  align-items: center;
  gap: 12px;
}

.progress-label {
  white-space: nowrap;
}
</style>
