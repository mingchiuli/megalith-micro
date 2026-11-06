<script lang="ts" setup>
import type { FormInstance, FormRules, TagProps } from 'element-plus'
import { useHttp } from '@/http/http'
import {
  type BlogEdit,
  type BlogVersion,
  type BlogPermissions,
  ButtonAuth,
  Status,
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
  loadEditorRecoveryDrafts,
  type EditorMetadataDraft,
  saveEditorMetadataDraft,
  toEditorMetadataDraft
} from '@/config/editorDraft'
import { useEditorDraftIdentity } from '@/composables/useEditorDraftIdentity'
import { clearYjsDraft, readPersistedYjsDraft } from '@/config/editorConfig'
import { loginStateStore } from '@/stores'
import { logger } from '@/utils/logger'
import { getSensitiveTagType, getSensitiveText } from '@/utils/sensitive'
import { saveBlogVersion } from '@/utils/blogVersion'

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
const form: EditForm = reactive({
  id: 0,
  userId: 0,
  title: '',
  description: '',
  content: '',
  status: 0,
  draftRevision: undefined,
  link: '',
  sensitiveContentList: []
})

const metadataDraftKey = computed(() =>
  createEditorMetadataDraftKey(
    draftUserId,
    String(form.id || blogId || 'new'),
    form.collaborationEpoch
  )
)
const yjsPersistenceKey = computed(() =>
  createEditorYjsPersistenceKey(
    draftUserId,
    String(form.id || blogId || 'new'),
    form.collaborationEpoch
  )
)
const recoveryDrafts = ref<EditorMetadataDraft[]>([])
const { failed: draftCreationFailed, createDraft } = useEditorDraftIdentity(
  blogId,
  () =>
    POST<{ id: number; collaborationEpoch: string }>(
      API_ENDPOINTS.BLOG_ADMIN.CREATE_DRAFT_BLOG,
      {}
    ),
  (id) => router.replace({ path: route.path, query: { ...route.query, id: String(id) } })
)

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
    const str = getSensitiveText(item.type, form)
    const element: SensitiveExhibit = {
      content: str.substring(item.startIndex, item.endIndex),
      startIndex: item.startIndex,
      type: item.type
    }
    const type = getSensitiveTagType(item.type)
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

type DraftSaveResponse = {
  blogId: number
  draftRevision: number
}

const ensureBlogId = async () => {
  if (!form.id || !form.collaborationEpoch) throw new Error('Editor draft is not ready')
  return form.id
}

const saveDraft = async (): Promise<BlogVersion> => {
  if (serverDraftSaveTask !== undefined) {
    globalThis.clearTimeout(serverDraftSaveTask)
    serverDraftSaveTask = undefined
  }
  if (inflightServerDraftSave) {
    await inflightServerDraftSave.catch((error) => {
      logger.warn('The pending server draft save failed before a manual save:', error)
    })
  }
  const currentBlogId = await ensureBlogId()
  const saved = await saveBlogVersion(POST, currentBlogId, form)
  form.draftRevision = saved.draftRevision
  lastServerSnapshot = editableSnapshot()
  return saved.version
}

const clearLocalDraftAfterSave = async () => {
  metadataDraftReady = false
  if (metadataDraftSaveTask !== undefined) {
    globalThis.clearTimeout(metadataDraftSaveTask)
    metadataDraftSaveTask = undefined
  }
  if (serverDraftSaveTask !== undefined) {
    globalThis.clearTimeout(serverDraftSaveTask)
    serverDraftSaveTask = undefined
  }
  await Promise.all([
    clearEditorMetadataDraft(metadataDraftKey.value),
    clearYjsDraft(yjsPersistenceKey.value)
  ]).catch((error) => {
    logger.warn('Failed to clear the saved editor draft:', error)
  })
  metadataDraftReady = true
}

const saveVersion = async (ref: FormInstance) => {
  if (sessionExpired.value) return
  await ref.validate(async (valid) => {
    if (!valid) return
    try {
      submitLoading.value = true
      await saveDraft()
      await clearLocalDraftAfterSave()
      ElNotification({
        title: t('common.operationSuccess'),
        message: t('admin.saveDraftVersion'),
        type: 'success',
        duration: 1000
      })
    } catch {
      // The HTTP layer presents the failure; keep the editor usable.
    } finally {
      submitLoading.value = false
    }
  })
}

const publish = async (ref: FormInstance) => {
  if (sessionExpired.value) return
  await ref.validate(async (valid) => {
    if (!valid) return
    try {
      submitLoading.value = true
      const version = await saveDraft()
      await POST<null>(API_ENDPOINTS.BLOG_ADMIN.PUBLISH(form.id!), {
        revisionId: version.id,
        accessMode: form.status === Status.BLOCK ? 1 : 0
      })
      await clearLocalDraftAfterSave()
      ElNotification({
        title: t('common.operationSuccess'),
        message: t('admin.publish'),
        type: 'success',
        duration: 1000
      })
      router.push({ name: 'system-blogs' })
    } catch {
      // The HTTP layer presents the failure; keep the editor usable.
    } finally {
      submitLoading.value = false
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
let serverDraftSaveTask: ReturnType<typeof setTimeout> | undefined
let inflightServerDraftSave: Promise<DraftSaveResponse> | undefined
let lastServerSnapshot = ''
let editorDisposed = false

const editableSnapshot = () =>
  JSON.stringify({
    title: form.title,
    description: form.description,
    content: form.content,
    status: form.status,
    link: form.link,
    sensitiveContentList: form.sensitiveContentList
  })

const restoreMetadataDraft = async () => {
  if (metadataDraftRestored) return
  metadataDraftRestored = true
  try {
    const draft = await loadEditorMetadataDraft(metadataDraftKey.value)
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
    void loadRecoveryDrafts().catch((error) =>
      logger.warn('Failed to load recovery copies:', error)
    )
  }
}

const scheduleMetadataDraftSave = () => {
  if (!metadataDraftReady || sessionExpired.value) return
  if (metadataDraftSaveTask !== undefined) globalThis.clearTimeout(metadataDraftSaveTask)
  metadataDraftSaveTask = globalThis.setTimeout(() => {
    metadataDraftSaveTask = undefined
    void saveEditorMetadataDraft(toEditorMetadataDraft(metadataDraftKey.value, form)).catch(
      (error) => {
        logger.warn('Failed to persist the editor metadata draft:', error)
      }
    )
  }, 300)
}

const scheduleServerDraftSave = () => {
  if (
    !form.id ||
    !form.collaborationEpoch ||
    !collaborationReady.value ||
    !permissions.value.commit ||
    loadContent.value ||
    sessionExpired.value ||
    !checkButtonAuth(ButtonAuth.SYS_EDIT_SAVE_DRAFT)
  ) {
    return
  }
  if (editableSnapshot() === lastServerSnapshot) return
  if (serverDraftSaveTask !== undefined) globalThis.clearTimeout(serverDraftSaveTask)
  serverDraftSaveTask = globalThis.setTimeout(() => {
    serverDraftSaveTask = undefined
    const epoch = form.collaborationEpoch
    const snapshot = editableSnapshot()
    const payload = {
      ...form,
      sensitiveContentList: form.sensitiveContentList.map((span) => ({ ...span }))
    }
    let request: Promise<DraftSaveResponse>
    request = POST<DraftSaveResponse>(API_ENDPOINTS.BLOG_ADMIN.SAVE_DRAFT(form.id!), payload)
      .then((draft) => {
        if (editorDisposed || form.collaborationEpoch !== epoch) return draft
        form.draftRevision = draft.draftRevision
        form.hasDraft = true
        lastServerSnapshot = snapshot
        return draft
      })
      .catch((error) => {
        logger.warn('Failed to autosave the server draft:', error)
        throw error
      })
      .finally(() => {
        if (inflightServerDraftSave === request) inflightServerDraftSave = undefined
      })
    inflightServerDraftSave = request
    void request.catch(() => undefined)
  }, 1200)
}

watch(
  () => [form.title, form.description, form.status, form.link, form.sensitiveContentList],
  scheduleMetadataDraftSave,
  { deep: true }
)

watch(
  () => [
    form.title,
    form.description,
    form.content,
    form.status,
    form.link,
    form.sensitiveContentList
  ],
  scheduleServerDraftSave,
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
  form.collaborationEpoch = data.collaborationEpoch
  form.draftRevision = data.draftRevision
  form.basePublishedRevisionId = data.basePublishedRevisionId
  form.publishedRevisionId = data.publishedRevisionId
  form.hasDraft = data.hasDraft
  form.draftNewer = data.draftNewer
  form.id = data.id
  form.userId = data.userId
  form.sensitiveContentList = data.sensitiveContentList
  permissions.value = data.permissions
  lastServerSnapshot = editableSnapshot()
}

const { refresh: refreshEditor } = useUniversalData(
  `admin:edit:${blogId ?? 'new'}`,
  () => fetchEditContent(blogId),
  applyEditContent,
  { loading: loadContent }
)

watch(
  loadContent,
  (loading) => {
    if (!loading && blogId && form.collaborationEpoch) void restoreMetadataDraft()
  },
  { immediate: true }
)

const loadRecoveryDrafts = async () => {
  if (!blogId) return
  const drafts = await loadEditorRecoveryDrafts(draftUserId, blogId, metadataDraftKey.value)
  for (const draft of drafts) {
    if (draft.content === undefined) {
      draft.content = await readPersistedYjsDraft(draft.key.replace(':metadata:', ':yjs:')).catch(
        () => ''
      )
    }
  }
  // Legacy new-article drafts cannot be attributed to any individual article.
  const legacyKey = `megalith:editor:metadata:v1:${draftUserId}:new`
  const legacy = await loadEditorMetadataDraft(legacyKey).catch(() => undefined)
  const legacyContent = await readPersistedYjsDraft(legacyKey.replace(':metadata:', ':yjs:')).catch(
    () => ''
  )
  if (legacy || legacyContent)
    drafts.push({ ...(legacy ?? toEditorMetadataDraft(legacyKey, form)), content: legacyContent })
  const legacyBlogKey = `megalith:editor:metadata:v1:${draftUserId}:${blogId}`
  if (!drafts.some((draft) => draft.key === legacyBlogKey)) {
    const content = await readPersistedYjsDraft(legacyBlogKey.replace(':metadata:', ':yjs:')).catch(
      () => ''
    )
    if (content) drafts.push({ ...toEditorMetadataDraft(legacyBlogKey, form), content })
  }
  recoveryDrafts.value = [
    ...new Map([...recoveryDrafts.value, ...drafts].map((draft) => [draft.key, draft])).values()
  ]
}

const handleEpochChanged = async () => {
  collaborationReady.value = false
  metadataDraftReady = false
  loadContent.value = true
  if (serverDraftSaveTask !== undefined) globalThis.clearTimeout(serverDraftSaveTask)
  if (metadataDraftSaveTask !== undefined) globalThis.clearTimeout(metadataDraftSaveTask)
  const recovery = {
    ...toEditorMetadataDraft(`${metadataDraftKey.value}:recovery:${Date.now()}`, form),
    content: form.content
  }
  recoveryDrafts.value.unshift(recovery)
  await saveEditorMetadataDraft(recovery).catch((error) =>
    logger.warn('Failed to retain recovery copy:', error)
  )
  try {
    await refreshEditor()
    metadataDraftRestored = false
    await restoreMetadataDraft()
    await loadRecoveryDrafts()
  } catch {
    loadContent.value = true
  }
}

onMounted(() => {
  void loadAiModels()
})

onUnmounted(() => {
  editorDisposed = true
  if (metadataDraftSaveTask !== undefined) globalThis.clearTimeout(metadataDraftSaveTask)
  if (serverDraftSaveTask !== undefined) globalThis.clearTimeout(serverDraftSaveTask)
})
</script>

<template>
  <div class="father">
    <el-alert
      v-if="draftCreationFailed"
      type="error"
      :closable="false"
      :title="t('admin.draftCreationFailed')"
    >
      <el-button @click="createDraft">{{ t('collaboration.retry') }}</el-button>
    </el-alert>
    <el-collapse v-if="recoveryDrafts.length">
      <el-collapse-item :title="t('admin.recoveryCopies')">
        <div v-for="draft in recoveryDrafts" :key="draft.key">
          <p>{{ draft.title }} — {{ new Date(draft.updatedAt).toLocaleString() }}</p>
          <el-input :model-value="draft.title" :aria-label="t('common.title')" readonly />
          <el-input
            :model-value="draft.description"
            :aria-label="t('common.description')"
            readonly
          />
          <el-input
            type="textarea"
            :rows="6"
            :model-value="draft.content"
            :aria-label="t('common.content')"
            readonly
          />
        </div>
      </el-collapse-item>
    </el-collapse>
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
            v-if="!loadContent && editorReady && form.id && form.collaborationEpoch"
            :key="`${form.id}:${form.collaborationEpoch}`"
            :blog-id="form.id"
            :collaboration-epoch="form.collaborationEpoch"
            @epoch-changed="handleEpochChanged"
            @authorization-paused="collaborationReady = false"
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
          @click="saveVersion(formRef!)"
          >{{ getButtonTitle(ButtonAuth.SYS_EDIT_COMMIT) }}</el-button
        >
        <el-button
          :disabled="submitLoading || !permissions.commit || !collaborationReady || sessionExpired"
          :loading="submitLoading"
          type="primary"
          v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_PUBLISH)"
          @click="publish(formRef!)"
          >{{ t('admin.publish') }}</el-button
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
