<script lang="ts" setup>
import type { FormInstance, FormRules, TagProps } from 'element-plus'
import { useHttp } from '@/http/http'
import {
  type BlogEdit,
  type BlogPermissions,
  ButtonAuth,
  type EditForm,
  type SensitiveExhibit,
  type SensitiveItem,
  type SensitiveTrans,
  SensitiveType
} from '@/type/entity'
import EditorLoadingItem from '@/components/sys/EditorLoadingItem.vue'
import { checkButtonAuth, getButtonTitle, getButtonType } from '@/utils/permissions'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import { AI_MODELS } from '@/config/aiConfig'
import { useAiGenerate } from '@/composables'
import { useI18n } from 'vue-i18n'
import { useUniversalData } from '@/composables'
import {
  clearEditorMetadataDraft,
  createEditorMetadataDraftKey,
  createEditorYjsPersistenceKey,
  loadEditorMetadataDraft,
  saveEditorMetadataDraft,
  toEditorMetadataDraft
} from '@/config/editorDraft'
import { clearYjsDraft } from '@/config/editorConfig'
import { loginStateStore } from '@/stores'
import { logger } from '@/utils/logger'

const { t } = useI18n()
const { GET, POST } = useHttp()
const router = useRouter()
const imageModel = AI_MODELS.IMAGE_MODEL
const submitLoading = ref(false)
const route = useRoute()
const blogId = route.query.id as string | undefined
const loginState = loginStateStore()
const { sessionExpired } = storeToRefs(loginState)
const collaborationReady = ref(false)
const draftUserId = loginState.user?.id ?? 0
const metadataDraftKey = createEditorMetadataDraftKey(draftUserId, blogId)
const yjsPersistenceKey = createEditorYjsPersistenceKey(draftUserId, blogId)
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

const permissions = ref<BlogPermissions>({
  collaborate: false,
  commit: false,
  manageMetadata: false,
  manageAssets: false
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

const submitForm = async (ref: FormInstance) => {
  if (sessionExpired.value) return
  await ref.validate(async (valid) => {
    if (valid) {
      try {
        submitLoading.value = true
        await POST<null>(API_ENDPOINTS.BLOG_ADMIN.SAVE_BLOG, form)
        metadataDraftReady = false
        if (metadataDraftSaveTask !== undefined) {
          globalThis.clearTimeout(metadataDraftSaveTask)
          metadataDraftSaveTask = undefined
        }
        await Promise.all([
          clearEditorMetadataDraft(metadataDraftKey),
          clearYjsDraft(yjsPersistenceKey)
        ]).catch((error) => {
          logger.warn('Failed to clear the saved editor draft:', error)
        })
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

const handleTagClose = (type: SensitiveType, startIndex: number) => {
  form.sensitiveContentList = form.sensitiveContentList.filter(
    (item) => item.type !== type || (item.startIndex !== startIndex && item.type === type)
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

const CustomEditorItem = defineAsyncComponent({
  loader: () => import('@/components/sys/EditorItem.vue'),
  loadingComponent: EditorLoadingItem,
  delay: 200,
  errorComponent: EditorLoadingItem,
  timeout: 15000
})

const loadContent = ref(true)
const editorReady = ref(false)
let metadataDraftReady = false
let metadataDraftRestored = false
let metadataDraftSaveTask: ReturnType<typeof setTimeout> | undefined

const restoreMetadataDraft = async () => {
  if (metadataDraftRestored) return
  metadataDraftRestored = true
  try {
    const draft = await loadEditorMetadataDraft(metadataDraftKey)
    if (draft) {
      form.title = draft.title
      form.description = draft.description
      form.status = draft.status
      form.link = draft.link
      form.sensitiveContentList = draft.sensitiveContentList.map((item) => ({ ...item }))
    }
  } catch (error) {
    logger.warn('Failed to restore the editor metadata draft:', error)
  } finally {
    metadataDraftReady = true
    editorReady.value = true
  }
}

const scheduleMetadataDraftSave = () => {
  if (!metadataDraftReady || sessionExpired.value) return
  if (metadataDraftSaveTask !== undefined) globalThis.clearTimeout(metadataDraftSaveTask)
  metadataDraftSaveTask = globalThis.setTimeout(() => {
    metadataDraftSaveTask = undefined
    void saveEditorMetadataDraft(toEditorMetadataDraft(metadataDraftKey, form)).catch((error) => {
      logger.warn('Failed to persist the editor metadata draft:', error)
    })
  }, 300)
}

watch(
  () => [form.title, form.description, form.status, form.link, form.sensitiveContentList],
  scheduleMetadataDraftSave,
  { deep: true }
)

const fetchEditContent = async (blogId: string | undefined) => {
  let url = API_ENDPOINTS.BLOG_ADMIN.EDIT_PULL_ECHO
  if (blogId) {
    url = buildQueryUrl(url, { blogId })
  }
  return GET<BlogEdit>(url)
}

const applyEditContent = (data: BlogEdit) => {
  form.title = data.title
  form.description = data.description
  form.content = data.content
  form.link = data.link
  form.status = data.status
  form.id = data.id
  form.userId = data.userId
  form.sensitiveContentList = data.sensitiveContentList
  permissions.value = data.permissions
}

useUniversalData(
  `admin:edit:${blogId ?? 'new'}`,
  () => fetchEditContent(blogId),
  applyEditContent,
  { loading: loadContent }
)

watch(
  loadContent,
  (loading) => {
    if (!loading) void restoreMetadataDraft()
  },
  { immediate: true }
)

onMounted(() => void loadAiModels())
</script>

<template>
  <div class="father">
    <el-form :model="form" :rules="formRules" ref="formRef">
      <BlogMetadataFields
        v-model:title="form.title"
        v-model:description="form.description"
        v-model:status="form.status"
        :manage-metadata="permissions.manageMetadata && !sessionExpired"
        :sensitive-tags="sensitiveTags"
        @sensitive="dealSensitive"
        @remove-sensitive="handleTagClose"
      >
        <template #description-actions>
          <BlogAiActions
            v-model:model="aiModel"
            :models="aiModels"
            :loading="aiLoading"
            :content-ready="Boolean(form.content)"
            :manage-metadata="permissions.manageMetadata && !sessionExpired"
            @generate="handleAiGenerate"
          />
        </template>

        <template #after-description>
          <BlogAiPanel
            :visible="aiPanelVisible"
            :step="aiStep"
            :failed-step="failedStep"
            :error="aiError"
            :thinking="aiThinking"
            :image-skip-reason="imageSkipReason"
            :thinking-supported="thinkingSupported"
            :image-generating="imageGenerating"
            :image-progress="imageProgress"
          />
        </template>
      </BlogMetadataFields>

      <BlogCoverField
        v-model:link="form.link"
        v-model:generated-dialog-visible="generatedImageDialogVisible"
        :manage-assets="permissions.manageAssets && !sessionExpired"
        :generated-image-url="generatedImageUrl"
        :generated-image-base64="generatedImageBase64"
        :image-generating="imageGenerating"
        :blog-id="form.id || undefined"
        @regenerate="regenerateImage"
      />

      <el-form-item class="content" prop="content">
        <ClientOnly>
          <CustomEditorItem
            v-if="!loadContent && editorReady"
            v-model:content="form.content"
            @ready="collaborationReady = true"
            @sensitive="dealSensitive"
            :form-status="form.status"
            :manage-assets="permissions.manageAssets && !sessionExpired"
          />
          <template #fallback><EditorLoadingItem /></template>
        </ClientOnly>
      </el-form-item>

      <div class="submit-button">
        <el-button
          :disabled="submitLoading || !permissions.commit || !collaborationReady || sessionExpired"
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

.submit-button {
  margin: 10px auto;
  text-align: center;
}

.content {
  margin: 25px auto;
}
</style>
