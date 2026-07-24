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
import { GET, POST, UPLOAD } from '@/http/http'
import {
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
import { render } from '@/utils/markdown'
import { checkButtonAuth, getButtonTitle, getButtonType } from '@/utils/permissions'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import { AI_MODELS } from '@/config/aiConfig'
import { logger } from '@/utils/logger'
import { useAiGenerate } from '@/composables'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()
const imageModel = AI_MODELS.IMAGE_MODEL
const aiThinkingCollapse = ref<string[]>(['thinking'])
const thinkingRef = useTemplateRef<HTMLDivElement>('thinkingRef')
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

const {
  aiModels,
  aiModel,
  aiLoading,
  aiStep,
  failedStep,
  aiError,
  aiThinking,
  imageSkipReason,
  thinkingSupported,
  aiPanelVisible,
  imageGenerating,
  imageProgress,
  generatedImageUrl,
  generatedImageBase64,
  generatedImageDialogVisible,
  loadAiModels,
  aiGenerate,
  regenerateImage
} = useAiGenerate(form, imageModel)

watch(aiThinking, () => {
  nextTick(() => {
    if (thinkingRef.value) thinkingRef.value.scrollTop = thinkingRef.value.scrollHeight
  })
})

const aiThinkingContent = computed(() => {
  if (aiThinking.value) return aiThinking.value
  if (aiError.value) return t('ai.noThinking')
  return thinkingSupported.value ? t('ai.waitingThinking') : t('ai.unsupportedThinking')
})
const aiThinkingHtml = computed(() => render(aiThinkingContent.value))

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

const formRef = ref<FormInstance>()
const formRules = computed<FormRules<EditForm>>(() => ({
  title: [
    {
      required: true,
      message: t('validation.enter', { field: t('common.title') }),
      trigger: 'blur'
    }
  ],
  description: [
    {
      required: true,
      message: t('validation.enter', { field: t('common.description') }),
      trigger: 'blur'
    }
  ],
  content: [
    {
      required: true,
      message: t('validation.enter', { field: t('common.content') }),
      trigger: 'blur'
    }
  ],
  status: [
    {
      required: true,
      message: t('validation.select', { field: t('common.status') }),
      trigger: 'blur'
    }
  ]
}))

const handleAiGenerate = async () => {
  formRef.value?.clearValidate(['title', 'description'])
  await aiGenerate()
}

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
          title: t('common.operationSuccess'),
          message: t('common.editSuccess'),
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
    ElMessage.error(t('common.imageFormatError'))
    return false
  } else if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error(t('common.imageSizeError'))
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
    const file: UploadRawFile = new File([blob], 'cover.png', {
      type: 'image/png'
    }) as UploadRawFile
    ;(file as UploadRawFile & { uid: number }).uid = Date.now()

    await uploadFile(file)
    generatedImageDialogVisible.value = false
  } catch (e) {
    logger.warn('图片上传失败:', e)
  } finally {
    uploadLoading.value = false
  }
}

;(async () => {
  await loadEditContent(form, blogId)
  await loadAiModels()
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
          :placeholder="t('common.title')"
          maxlength="20"
          :disabled="!owner"
        />
      </el-form-item>

      <div class="desc-input-group">
        <el-form-item class="desc" prop="description">
          <el-input
            ref="descRef"
            @select="handleDescSelect"
            autosize
            type="textarea"
            v-model="form.description"
            :placeholder="t('common.description')"
            maxlength="60"
            :disabled="!owner"
          />
        </el-form-item>

        <div class="ai-actions">
          <el-select
            v-model="aiModel"
            :placeholder="t('ai.model')"
            style="width: 140px"
            :disabled="aiLoading"
          >
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
            @click="handleAiGenerate"
            :loading="aiLoading"
            :disabled="!owner || aiLoading || !form.content || !aiModel"
            >✨AI</el-button
          >
        </div>
      </div>

      <!-- AI 生成面板 -->
      <div v-if="aiPanelVisible" class="ai-panel">
        <el-steps :active="aiStep - 1" align-center finish-status="success">
          <el-step :title="t('ai.titleSummary')" :status="failedStep === 1 ? 'error' : undefined" />
          <el-step
            :title="t('ai.imagePrompt')"
            :description="imageSkipReason"
            :status="failedStep === 2 ? 'error' : imageSkipReason ? 'wait' : undefined"
          />
          <el-step
            :title="t('ai.coverImage')"
            :status="failedStep === 3 ? 'error' : imageSkipReason ? 'wait' : undefined"
          />
        </el-steps>

        <el-alert
          v-if="aiError"
          :title="aiError"
          type="error"
          show-icon
          :closable="false"
          class="ai-error"
        />

        <el-collapse v-model="aiThinkingCollapse" class="thinking-collapse">
          <el-collapse-item :title="`💭 ${t('ai.thinking')}`" name="thinking">
            <div class="thinking-content" ref="thinkingRef" v-html="aiThinkingHtml"></div>
          </el-collapse-item>
        </el-collapse>

        <el-form-item v-if="imageGenerating" :label="t('ai.imageProgress')" class="progress">
          <el-progress type="line" :percentage="imageProgress" :color="Colors" />
        </el-form-item>
      </div>

      <el-form-item class="status" prop="status">
        <el-radio-group v-model="form.status" :disabled="!owner">
          <el-radio :value="Status.NORMAL">{{ t('common.public') }}</el-radio>
          <el-radio :value="Status.BLOCK">{{ t('common.hidden') }}</el-radio>
          <el-radio :value="Status.SENSITIVE_FILTER">{{ t('common.masked') }}</el-radio>
          <el-radio :value="Status.DRAFT">{{ t('common.draft') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.status === Status.SENSITIVE_FILTER" :label="t('common.masked')">
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
          :title="t('ai.coverPreview')"
          width="500px"
          :close-on-click-modal="true"
        >
          <div class="image-preview-container">
            <img
              v-if="generatedImageUrl"
              :src="generatedImageUrl"
              class="preview-image"
              :alt="t('ai.previewAlt')"
            />
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
            <el-button
              v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_AI)"
              @click="regenerateImage"
              :loading="imageGenerating"
              >{{ t('ai.regenerate') }}</el-button
            >
            <el-button
              v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_UPLOAD)"
              type="primary"
              @click="handleConfirmUpload"
              :loading="uploadLoading"
              >{{ t('ai.confirmUpload') }}</el-button
            >
          </template>
        </el-dialog>
      </el-form-item>

      <el-form-item
        :label="t('common.uploadProgress')"
        class="progress"
        v-if="showPercentage && !generatedImageDialogVisible"
      >
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
  display: flex;
  width: 100%;
  max-width: 200px;
  min-width: 0;
  margin-top: 15px;
}

.desc {
  flex: 1;
  min-width: 0;
  margin: 0;
}

.desc-input-group {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  max-width: 800px;
  margin-top: 25px;
}

.desc-input-group .el-input {
  width: 100%;
}

.ai-actions {
  display: flex;
  flex: none;
  gap: 12px;
  align-items: center;
}

.progress {
  margin-top: 25px;
  width: 300px;
}

.status {
  display: flex;
  width: 100%;
  max-width: 300px;
  min-width: 0;
  margin-top: 25px;
}

.title :deep(.el-form-item__content),
.status :deep(.el-form-item__content) {
  flex: 1;
  min-width: 0;
}

.status :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
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
  width: min(100%, 36rem);
  box-sizing: border-box;
  margin-top: 25px;
  padding: 18px 16px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.ai-panel :deep(.el-step__title) {
  font-size: 13px;
  font-weight: 500;
  line-height: 20px;
}

.ai-panel :deep(.el-step__main) {
  margin-top: 5px;
}

.ai-panel :deep(.el-step__description) {
  padding-right: 0;
  font-size: 12px;
}

.ai-panel .progress {
  width: 100%;
  margin: 14px 0 0;
}

.thinking-collapse {
  margin-top: 14px;
}

.thinking-collapse :deep(.el-collapse-item__header) {
  height: 44px;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 500;
}

.thinking-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

.ai-error {
  margin-top: 12px;
}

.thinking-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
  color: var(--el-text-color-secondary);
}

.thinking-content :deep(p) {
  margin: 0 0 8px;
}

.thinking-content :deep(p:last-child),
.thinking-content :deep(ul:last-child),
.thinking-content :deep(ol:last-child),
.thinking-content :deep(pre:last-child),
.thinking-content :deep(blockquote:last-child) {
  margin-bottom: 0;
}

.thinking-content :deep(ul),
.thinking-content :deep(ol) {
  margin: 4px 0 8px;
  padding-left: 20px;
}

.thinking-content :deep(li) {
  margin: 2px 0;
}

.thinking-content :deep(li > p) {
  margin: 0;
}

.thinking-content :deep(pre) {
  overflow-x: auto;
  margin: 8px 0;
  padding: 10px 12px;
  border-radius: 4px;
  background: var(--el-fill-color-lighter);
}

.thinking-content :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.thinking-content :deep(:not(pre) > code) {
  padding: 1px 4px;
  border-radius: 3px;
  background: var(--el-fill-color-lighter);
}

.thinking-content :deep(blockquote) {
  margin: 8px 0;
  padding-left: 10px;
  border-left: 2px solid var(--el-border-color);
  color: var(--el-text-color-secondary);
}

.thinking-content :deep(h1),
.thinking-content :deep(h2),
.thinking-content :deep(h3) {
  margin: 10px 0 6px;
  font-size: 14px;
  line-height: 1.5;
}
</style>
