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
  disconnectYjs,
  reconnectYjs,
  updateProviderToken,
  COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
  COLLABORATION_TICKET_RECONNECT_MAX_AGE_MS,
  shouldRefreshCollaborationTicket,
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
const loginState = loginStateStore()
const { sessionExpired } = storeToRefs(loginState)
const user = loginState.user || { nickname: 'Anonymous', avatar: '', id: 0 }
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
  ready: []
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
let collaborationTicketIssuedAt = 0
let ticketRefreshPromise: Promise<void> | undefined
let ticketRefreshTask: number | undefined

const issueCollaborationTicket = () => {
  const url = blogId
    ? buildQueryUrl(API_ENDPOINTS.COLLABORATION.TICKET, { blogId })
    : API_ENDPOINTS.COLLABORATION.TICKET
  return POST<string>(url, {})
}

const refreshCollaborationTicket = (
  maxAge = COLLABORATION_TICKET_RECONNECT_MAX_AGE_MS
): Promise<void> => {
  if (disposed || !shouldRefreshCollaborationTicket(collaborationTicketIssuedAt, maxAge)) {
    return Promise.resolve()
  }

  if (!ticketRefreshPromise) {
    ticketRefreshPromise = issueCollaborationTicket()
      .then((token) => {
        if (disposed) return
        updateProviderToken(token)
        collaborationTicketIssuedAt = Date.now()
      })
      .finally(() => {
        ticketRefreshPromise = undefined
      })
  }
  return ticketRefreshPromise
}

const startTicketRefresh = () => {
  if (ticketRefreshTask !== undefined) return
  ticketRefreshTask = window.setInterval(() => {
    void refreshCollaborationTicket(COLLABORATION_TICKET_REFRESH_INTERVAL_MS).catch((error) => {
      logger.error('Failed to renew collaboration ticket:', error)
    })
  }, COLLABORATION_TICKET_REFRESH_INTERVAL_MS)
}

const stopTicketRefresh = () => {
  if (ticketRefreshTask === undefined) return
  window.clearInterval(ticketRefreshTask)
  ticketRefreshTask = undefined
}

const handleVisibilityChange = () => {
  if (document.visibilityState !== 'visible' || disposed || sessionExpired.value) return

  void refreshCollaborationTicket(0)
    .then(() => {
      if (!disposed && !sessionExpired.value) reconnectYjs()
    })
    .catch((error) => {
      logger.error('Failed to recover collaboration after tab became visible:', error)
    })
}

const notifyCollaborationEvent = (event: CollaborationEvent) => {
  if (sessionExpired.value) return

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
    },
    'persistence-error': {
      title: t('collaboration.persistenceErrorTitle'),
      message: t('collaboration.persistenceErrorMessage'),
      type: 'warning' as const,
      duration: 5000
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
  if (event.type === 'disconnected') {
    void refreshCollaborationTicket().catch((error) => {
      logger.error('Failed to renew collaboration ticket after disconnect:', error)
    })
  }
  ElNotification(notifications[event.type])
}

const updateEditorExtension = async () => {
  const view = editorRef.value?.getEditorView()
  if (view) {
    try {
      const collaborationToken = await issueCollaborationTicket()
      collaborationTicketIssuedAt = Date.now()
      const { config, provider, initialSync } = await createYjsExtension(
        roomId,
        text.value ?? '',
        collaborationToken,
        user,
        notifyCollaborationEvent
      )
      if (disposed || sessionExpired.value) {
        cleanupYjs()
        return
      }
      provider.connect()
      startTicketRefresh()
      const syncedContent = await initialSync
      if (disposed) return

      view.dispatch(createYjsBindingTransaction(view.state.doc.length, syncedContent, config))
      collaborationReady.value = true
      emit('ready')
    } catch (error) {
      stopTicketRefresh()
      cleanupYjs()
      collaborationReady.value = true
      emit('ready')
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

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  sensitiveListen()
  void updateEditorExtension()
})

watch(sessionExpired, (expired) => {
  if (!expired) return
  stopTicketRefresh()
  disconnectYjs()
})

onBeforeUnmount(() => {
  disposed = true
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  stopTicketRefresh()
  cleanupYjs()
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
    :disabled="!collaborationReady || sessionExpired"
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
