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
  createCollaborationSession,
  createYjsBindingTransaction,
  COLLABORATION_INITIAL_SYNC_TIMEOUT_MS,
  COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
  COLLABORATION_TICKET_RECONNECT_MAX_AGE_MS,
  collaborationPhaseAfterDisconnect,
  isCollaborationEditable,
  shouldRefreshCollaborationTicket,
  type CollaborationEvent,
  type CollaborationPhase,
  type CollaborationSession
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
import { RefreshRight } from '@element-plus/icons-vue'

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
const collaborationPhase = ref<CollaborationPhase>('initializing')
const collaborationRecovering = ref(false)
const collaborationEditable = computed(() =>
  isCollaborationEditable(collaborationReady.value, sessionExpired.value)
)
let disposed = false
let collaborationGeneration = 0
let collaborationSession: CollaborationSession | undefined
let collaborationTicketIssuedAt = 0
let ticketRefreshPromise: Promise<string> | undefined
let initializationPromise: Promise<void> | undefined
let recoveryPromise: Promise<void> | undefined
let recoveryRequested = false
let recoveryForceTicketRequested = false
let recoveryRestartRequested = false
let ticketRefreshTask: number | undefined
let initialSyncTimeoutTask: number | undefined
let initializationRetryTask: number | undefined
let initializationRetryCount = 0
let readyEmitted = false
let lastConnectionNotificationAt = 0

const issueCollaborationTicket = () => {
  const url = blogId
    ? buildQueryUrl(API_ENDPOINTS.COLLABORATION.TICKET, { blogId })
    : API_ENDPOINTS.COLLABORATION.TICKET
  return POST<string>(url, {})
}

const acquireCollaborationTicket = (
  maxAge = COLLABORATION_TICKET_RECONNECT_MAX_AGE_MS
): Promise<string | undefined> => {
  if (disposed || !shouldRefreshCollaborationTicket(collaborationTicketIssuedAt, maxAge)) {
    return Promise.resolve(undefined)
  }

  if (!ticketRefreshPromise) {
    ticketRefreshPromise = issueCollaborationTicket()
      .then((token) => {
        if (!disposed) collaborationTicketIssuedAt = Date.now()
        return token
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
    void acquireCollaborationTicket(COLLABORATION_TICKET_REFRESH_INTERVAL_MS)
      .then((token) => {
        if (token) collaborationSession?.updateToken(token)
      })
      .catch((error) => {
        logger.error('Failed to renew collaboration ticket:', error)
      })
  }, COLLABORATION_TICKET_REFRESH_INTERVAL_MS)
}

const stopTicketRefresh = () => {
  if (ticketRefreshTask === undefined) return
  window.clearInterval(ticketRefreshTask)
  ticketRefreshTask = undefined
}

const clearInitialSyncTimeout = () => {
  if (initialSyncTimeoutTask === undefined) return
  window.clearTimeout(initialSyncTimeoutTask)
  initialSyncTimeoutTask = undefined
}

const clearInitializationRetry = () => {
  if (initializationRetryTask === undefined) return
  window.clearTimeout(initializationRetryTask)
  initializationRetryTask = undefined
}

const notifyCollaborationEvent = (event: CollaborationEvent) => {
  if (sessionExpired.value || event.type === 'synced') return

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
  ElNotification(notifications[event.type])
}

const notifyConnectionIssue = (event: CollaborationEvent) => {
  const now = Date.now()
  if (now - lastConnectionNotificationAt < 5000) return
  lastConnectionNotificationAt = now
  notifyCollaborationEvent(event)
}

const armInitialSyncTimeout = () => {
  clearInitialSyncTimeout()
  if (disposed || sessionExpired.value || collaborationReady.value) return

  initialSyncTimeoutTask = window.setTimeout(() => {
    initialSyncTimeoutTask = undefined
    if (disposed || sessionExpired.value || collaborationReady.value) return

    collaborationPhase.value = 'failed'
    notifyConnectionIssue({ type: 'connection-error' })
    void recoverCollaboration(true, true).finally(armInitialSyncTimeout)
  }, COLLABORATION_INITIAL_SYNC_TIMEOUT_MS)
}

const scheduleInitializationRetry = () => {
  if (disposed || sessionExpired.value || initializationRetryTask !== undefined) return
  const delay = Math.min(1000 * 2 ** initializationRetryCount, 10_000)
  initializationRetryCount += 1
  initializationRetryTask = window.setTimeout(() => {
    initializationRetryTask = undefined
    void initializeCollaboration()
  }, delay)
}

const handleCollaborationEvent = (event: CollaborationEvent, generation: number) => {
  if (disposed || generation !== collaborationGeneration || sessionExpired.value) return

  switch (event.type) {
    case 'initialized':
      notifyCollaborationEvent(event)
      return
    case 'connected':
      collaborationPhase.value = collaborationReady.value ? 'reconnecting' : 'syncing'
      return
    case 'synced': {
      if (!collaborationReady.value) {
        const view = editorRef.value?.getEditorView()
        const session = collaborationSession
        if (!view || !session) return
        view.dispatch(
          createYjsBindingTransaction(view.state.doc.length, event.content, session.config)
        )
        collaborationReady.value = true
        if (!readyEmitted) {
          readyEmitted = true
          emit('ready')
        }
        notifyCollaborationEvent({ type: 'connected' })
      }
      clearInitialSyncTimeout()
      clearInitializationRetry()
      initializationRetryCount = 0
      collaborationRecovering.value = false
      collaborationPhase.value = 'ready'
      return
    }
    case 'disconnected':
      collaborationPhase.value = collaborationPhaseAfterDisconnect(collaborationReady.value)
      notifyConnectionIssue(event)
      void recoverCollaboration(false, false)
      return
    case 'connection-error':
      collaborationPhase.value = collaborationPhaseAfterDisconnect(collaborationReady.value)
      notifyConnectionIssue(event)
      void recoverCollaboration(false, false)
      return
    case 'connection-closed': {
      const terminal = event.code >= 4400 && event.code < 4500
      collaborationPhase.value =
        terminal && !collaborationReady.value
          ? 'failed'
          : collaborationReady.value
            ? 'reconnecting'
            : 'connecting'
      notifyConnectionIssue(event)
      void recoverCollaboration(terminal, terminal)
      return
    }
    case 'persistence-error':
      notifyCollaborationEvent(event)
      return
  }
}

const initializeCollaboration = async () => {
  if (disposed || sessionExpired.value || collaborationSession) return
  if (initializationPromise) return initializationPromise

  const generation = ++collaborationGeneration
  collaborationPhase.value = 'initializing'
  const task = (async () => {
    const collaborationToken = await acquireCollaborationTicket(0)
    if (!collaborationToken) throw new Error('Collaboration ticket is unavailable')

    const session = await createCollaborationSession(
      roomId,
      text.value ?? '',
      collaborationToken,
      user,
      (event) => handleCollaborationEvent(event, generation)
    )
    if (disposed || sessionExpired.value || generation !== collaborationGeneration) {
      session.destroy()
      return
    }

    collaborationSession = session
    collaborationPhase.value = 'connecting'
    session.connect()
    startTicketRefresh()
    armInitialSyncTimeout()
  })()

  initializationPromise = task
  try {
    await task
  } catch (error) {
    if (!disposed && generation === collaborationGeneration) {
      collaborationPhase.value = 'failed'
      logger.error('Failed to initialize collaborative editor:', error)
      notifyConnectionIssue({ type: 'connection-error' })
      scheduleInitializationRetry()
    }
  } finally {
    if (initializationPromise === task) initializationPromise = undefined
  }
}

const recoverCollaboration = async (forceTicket: boolean, restart: boolean) => {
  if (disposed || sessionExpired.value) return
  if (!collaborationSession) {
    await initializeCollaboration()
    return
  }
  recoveryRequested = true
  recoveryForceTicketRequested ||= forceTicket
  recoveryRestartRequested ||= restart
  if (recoveryPromise) return recoveryPromise

  collaborationRecovering.value = true
  const task = (async () => {
    while (recoveryRequested) {
      const shouldForceTicket = recoveryForceTicketRequested
      const shouldRestart = recoveryRestartRequested
      recoveryRequested = false
      recoveryForceTicketRequested = false
      recoveryRestartRequested = false

      const token = await acquireCollaborationTicket(
        shouldForceTicket ? 0 : COLLABORATION_TICKET_RECONNECT_MAX_AGE_MS
      )
      if (disposed || sessionExpired.value || !collaborationSession) return
      if (token) collaborationSession.updateToken(token)
      if (shouldRestart) collaborationSession.restart(token)
    }
  })()
  recoveryPromise = task
  try {
    await task
  } catch (error) {
    logger.error('Failed to recover collaboration:', error)
    if (!collaborationReady.value) collaborationPhase.value = 'failed'
  } finally {
    if (recoveryPromise === task) recoveryPromise = undefined
    collaborationRecovering.value = false
  }
}

const retryCollaboration = () => {
  clearInitializationRetry()
  if (collaborationSession) {
    void recoverCollaboration(true, true).finally(armInitialSyncTimeout)
  } else {
    void initializeCollaboration()
  }
}

const handleVisibilityChange = () => {
  if (document.visibilityState !== 'visible' || disposed || sessionExpired.value) return
  void recoverCollaboration(true, true).finally(armInitialSyncTimeout)
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
  void initializeCollaboration()
})

watch(sessionExpired, (expired) => {
  if (!expired) return
  collaborationPhase.value = 'expired'
  clearInitialSyncTimeout()
  clearInitializationRetry()
  stopTicketRefresh()
  collaborationSession?.disconnect()
})

onBeforeUnmount(() => {
  disposed = true
  collaborationGeneration += 1
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  clearInitialSyncTimeout()
  clearInitializationRetry()
  stopTicketRefresh()
  collaborationSession?.destroy()
  collaborationSession = undefined
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

  <div
    v-if="collaborationPhase === 'failed' && !collaborationReady && !sessionExpired"
    class="collaboration-status"
    role="alert"
  >
    <el-text type="danger">{{ t('collaboration.syncFailedMessage') }}</el-text>
    <el-tooltip :content="t('collaboration.retry')">
      <el-button
        circle
        :icon="RefreshRight"
        :loading="collaborationRecovering"
        :aria-label="t('collaboration.retry')"
        @click="retryCollaboration"
      />
    </el-tooltip>
  </div>

  <MdEditor
    v-model="content"
    :preview="false"
    :toolbars="toolbars"
    :toolbarsExclude="['github']"
    @on-upload-img="onUploadImg"
    :footers="footers"
    :theme="editorTheme"
    :disabled="!collaborationEditable"
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

.collaboration-status {
  display: flex;
  min-height: 40px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 0;
}
</style>
