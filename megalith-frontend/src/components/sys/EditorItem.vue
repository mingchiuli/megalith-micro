<script lang="ts" setup>
import { useHttp } from '@/http/http'
import {
  SensitiveType,
  Status,
  type SensitiveTrans,
  type SensitiveContentItem,
  Colors
} from '@/type/entity'
import {
  createYjsExtension,
  createYjsBindingTransaction,
  cleanupYjs,
  updateProviderToken,
  type CollaborationEvent
} from '@/config/editorConfig'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import type { Footers, ToolbarNames, ExposeParam } from 'md-editor-v3'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { ExportPDF, Emoji } from '@vavt/v3-extension'
import { themeStore } from '@/stores'
import { loginStateStore } from '@/stores'
import { sanitizeHtml } from '@/utils/sanitize'
import { logger } from '@/utils/logger'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { POST, UPLOAD } = useHttp()

const route = useRoute()
const user = loginStateStore().user || { nickname: 'Anonymous', avatar: '', id: 0 }
const blogId = route.query.id as string | undefined
const roomId = blogId ? `${blogId}` : `init:${user.id}`

// 主题管理
const theme = themeStore()
const { isDark } = storeToRefs(theme)
const editorTheme = computed(() => (isDark.value ? 'dark' : 'light'))

const toolbars: ToolbarNames[] = [
  'revoke',
  'next',
  'bold',
  1,
  'underline',
  'italic',
  '-',
  'title',
  'strikeThrough',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  'katex',
  '-',
  0,
  'pageFullscreen',
  'fullscreen',
  'preview',
  'htmlPreview',
  'catalog',
  'github'
]
const footers: Footers[] = ['markdownTotal', 0, '=', 1, 'scrollSwitch']

const emit = defineEmits<{
  sensitive: [payload: SensitiveTrans]
}>()

const { formStatus, manageAssets } = defineProps<{
  formStatus: number
  manageAssets: boolean
}>()

const text = defineModel<string>('content')
const content = ref(text.value ?? '')
watch(content, (newValue: string) => {
  if (text.value !== newValue) {
    text.value = newValue
  }
})
watch(text, (newValue) => {
  if (newValue !== undefined && newValue !== content.value) {
    content.value = newValue
  }
})

const uploadPercentage = ref(0)
const showPercentage = ref(false)
const showSensitiveListDialog = ref(false)

const selectSensitiveData = ref<SensitiveContentItem[]>([])

const selectWord = (row: SensitiveContentItem) => {
  const sensitive: SensitiveTrans = {
    startIndex: row.startIndex,
    endIndex: row.endIndex,
    type: SensitiveType.CONTENT
  }
  emit('sensitive', sensitive)
  selectSensitiveData.value = []
  showSensitiveListDialog.value = false
}

const handleClose = () => {
  selectSensitiveData.value = []
  showSensitiveListDialog.value = false
}

const findAllOccurrences = (text: string, pattern: string) => {
  const escapedPattern = pattern.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(escapedPattern, 'g')
  let match
  const occurrences: SensitiveContentItem[] = []

  while ((match = regex.exec(text))) {
    const idx = match.index
    const frontIdx = Math.max(0, idx - 5)
    const behindIdx = Math.min(content.value!.length, idx + match[0].length + 5)

    occurrences.push({
      startIndex: idx,
      endIndex: idx + match[0].length,
      content: match[0],
      startContent: content.value!.substring(frontIdx, idx),
      endContent: content.value!.substring(idx + match[0].length, behindIdx)
    })
  }

  if (occurrences.length === 1) {
    selectWord(occurrences[0]!)
    return
  }

  return occurrences
}

const editorRef = useTemplateRef<ExposeParam>('editorRef')
const collaborationReady = ref(false)
let disposed = false

const notifyCollaborationEvent = (event: CollaborationEvent) => {
  const notifications = {
    initialized: {
      title: t('collaboration.initializedTitle'),
      message: t('collaboration.initializedMessage'),
      type: 'success' as const,
      duration: 2000
    },
    connected: {
      title: t('collaboration.connectedTitle'),
      message: t('collaboration.connectedMessage'),
      type: 'success' as const,
      duration: 2000
    },
    disconnected: {
      title: t('collaboration.disconnectedTitle'),
      message: t('collaboration.disconnectedMessage'),
      type: 'warning' as const,
      duration: 2000
    },
    'connection-error': {
      title: t('collaboration.errorTitle'),
      message: t('collaboration.errorMessage'),
      type: 'error' as const,
      duration: 3000
    }
  }

  if (event.type === 'connection-closed') {
    ElNotification({
      title: t('collaboration.disconnectedTitle'),
      message: t('collaboration.closedMessage', {
        code: event.code,
        reason: event.reason || t('common.unknown')
      }),
      type: 'warning',
      duration: 3000
    })
    return
  }
  ElNotification(notifications[event.type])
}

const updateEditorExtension = async () => {
  const view = editorRef.value?.getEditorView()
  if (view) {
    try {
      const collaborationToken = await issueCollaborationTicket()
      const { config, provider, initialSync } = await createYjsExtension(
        roomId,
        text.value ?? '',
        collaborationToken,
        user,
        notifyCollaborationEvent
      )
      provider.connect()
      const syncedContent = await initialSync
      if (disposed) return

      view.dispatch(createYjsBindingTransaction(view.state.doc.length, syncedContent, config))
      collaborationReady.value = true
    } catch (error) {
      cleanupYjs()
      collaborationReady.value = true
      logger.error('Failed to initialize collaborative editor:', error)
    }
  }
}

const onUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  if (!manageAssets) return
  const formdata = new FormData()
  formdata.append('image', files[0]!, files[0]!.name)
  const url = await UPLOAD(
    API_ENDPOINTS.BLOG_ADMIN.OSS_UPLOAD,
    formdata,
    uploadPercentage,
    showPercentage
  )
  callback([url])
}

const sensitiveListen = () => {
  if (!manageAssets) return

  const editorElement = document.getElementById('md-editor')
  if (!editorElement) return

  editorElement.onmouseup = () => {
    if (formStatus !== Status.SENSITIVE_FILTER) {
      return
    }

    const selection = document.getSelection()?.toString()

    if (!selection || !content.value) {
      return
    }
    const items = findAllOccurrences(content.value, selection)
    if (items) {
      selectSensitiveData.value = items
      showSensitiveListDialog.value = true
    }
  }
}

onMounted(async () => {
  sensitiveListen()
  await updateEditorExtension()
  ticketRefreshTask = window.setInterval(async () => {
    try {
      updateProviderToken(await issueCollaborationTicket())
    } catch (error) {
      logger.error('Failed to renew collaboration ticket:', error)
    }
  }, 240_000)
})

const issueCollaborationTicket = () => {
  const url = blogId
    ? buildQueryUrl(API_ENDPOINTS.COLLABORATION.TICKET, { blogId })
    : API_ENDPOINTS.COLLABORATION.TICKET
  return POST<string>(url, {})
}

let ticketRefreshTask: number | undefined

onBeforeUnmount(() => {
  disposed = true
  cleanupYjs()
  if (ticketRefreshTask) {
    clearInterval(ticketRefreshTask)
  }
})
</script>

<template>
  <el-dialog
    v-model="showSensitiveListDialog"
    :title="t('admin.selectWord')"
    width="500"
    :before-close="handleClose"
  >
    <el-table :data="selectSensitiveData" @row-click="selectWord" border stripe>
      <el-table-column
        property="startIndex"
        :label="t('admin.startPosition')"
        align="center"
        width="100"
      />
      <el-table-column
        property="endIndex"
        :label="t('admin.endPosition')"
        align="center"
        width="100"
      />
      <el-table-column property="content" :label="t('common.content')" align="center">
        <template #default="scope">
          <el-text>
            {{ scope.row.startContent }}
            <el-text tag="mark">{{ scope.row.content }}</el-text>
            {{ scope.row.endContent }}
          </el-text>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <MdEditor
    v-model="content"
    :preview="false"
    :toolbars="toolbars"
    :toolbarsExclude="['github']"
    @on-upload-img="onUploadImg"
    :footers="footers"
    :theme="editorTheme"
    :disabled="!collaborationReady"
    :sanitize="sanitizeHtml"
    ref="editorRef"
    id="md-editor"
  >
    <template #defToolbars>
      <ExportPDF v-model="content" />
      <Emoji />
    </template>
    <template #defFooters>
      <el-progress
        v-if="showPercentage"
        type="line"
        :percentage="uploadPercentage"
        :color="Colors"
        status="success"
      />
    </template>
  </MdEditor>
</template>

<style scoped>
/* 编辑器内容区域内边距 */
.md-editor:deep(.md-editor-footer) {
  height: 40px;
}

.el-progress {
  width: 100px;
  display: inline-flex;
}
</style>
