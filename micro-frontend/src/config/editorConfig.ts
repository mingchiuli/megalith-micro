import { config } from 'md-editor-v3'
import { Compartment, type Extension } from '@codemirror/state'
import * as Y from 'yjs'
import { yCollab } from 'y-codemirror.next'
import { WebsocketProvider } from 'y-websocket'
import { clearDocument, IndexeddbPersistence } from 'y-indexeddb'
import * as random from 'lib0/random'
import type { UserInfo } from '@/type/entity'
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'
import { logger } from '@/utils/logger'
import { createEditorYjsPersistenceKey } from '@/config/editorDraft'

const usercolors = [
  { color: '#30bced', light: '#30bced33' },
  { color: '#6eeb83', light: '#6eeb8333' },
  { color: '#ffbc42', light: '#ffbc4233' },
  { color: '#ecd444', light: '#ecd44433' },
  { color: '#ee6352', light: '#ee635233' },
  { color: '#9ac2c9', light: '#9ac2c933' },
  { color: '#8acb88', light: '#8acb8833' },
  { color: '#1be7ff', light: '#1be7ff33' }
]

export const yjsCompartment = new Compartment()
export const COLLABORATION_TICKET_REFRESH_INTERVAL_MS = 240_000
export const COLLABORATION_TICKET_RECONNECT_MAX_AGE_MS = 30_000
export const COLLABORATION_INITIAL_SYNC_TIMEOUT_MS = 20_000
const INDEXEDDB_SYNC_TIMEOUT_MS = 5000
const activePersistences = new Map<string, Set<IndexeddbPersistence>>()

export type CollaborationPhase =
  'initializing' | 'connecting' | 'syncing' | 'ready' | 'reconnecting' | 'failed' | 'expired'

export type CollaborationEvent =
  | {
      type: 'initialized' | 'connected' | 'disconnected' | 'connection-error' | 'persistence-error'
    }
  | { type: 'synced'; content: string }
  | { type: 'connection-closed'; code: number; reason: string }

export type CollaborationSession = {
  config: Extension
  persistenceKey: string
  connect: () => void
  disconnect: () => void
  updateToken: (token: string) => void
  restart: (token?: string) => void
  clearPersistence: () => Promise<void>
  destroy: () => void
}

export const hasYjsDocumentState = (doc: Y.Doc) =>
  Y.decodeStateVector(Y.encodeStateVector(doc)).size > 0

export const shouldInitializeYjsDocument = (doc: Y.Doc, text: Y.Text, initialContent: string) =>
  initialContent.length > 0 && text.length === 0 && !hasYjsDocumentState(doc)

export const shouldRefreshCollaborationTicket = (
  issuedAt: number,
  maxAge: number,
  now = Date.now()
) => issuedAt <= 0 || now - issuedAt >= maxAge

export const isCollaborationEditable = (hasSynced: boolean, sessionExpired: boolean) =>
  hasSynced && !sessionExpired

export const collaborationPhaseAfterDisconnect = (hasSynced: boolean): CollaborationPhase =>
  hasSynced ? 'reconnecting' : 'connecting'

export const createYjsBindingTransaction = (
  currentDocumentLength: number,
  syncedContent: string,
  extension: Extension
) => ({
  changes: {
    from: 0,
    to: currentDocumentLength,
    insert: syncedContent
  },
  effects: yjsCompartment.reconfigure(extension)
})

export const clearYjsDraft = async (persistenceKey?: string) => {
  if (!persistenceKey) return
  const persistences = [...(activePersistences.get(persistenceKey) ?? [])]
  if (persistences.length === 0) {
    await clearDocument(persistenceKey)
    return
  }
  activePersistences.delete(persistenceKey)
  await Promise.all(persistences.map((persistence) => persistence.clearData()))
}

const registerPersistence = (key: string, persistence: IndexeddbPersistence) => {
  const persistences = activePersistences.get(key) ?? new Set<IndexeddbPersistence>()
  persistences.add(persistence)
  activePersistences.set(key, persistences)
}

const unregisterPersistence = (key: string, persistence: IndexeddbPersistence | null) => {
  if (!persistence) return
  const persistences = activePersistences.get(key)
  persistences?.delete(persistence)
  if (persistences?.size === 0) activePersistences.delete(key)
}

const waitForIndexedDb = async (persistence: IndexeddbPersistence) => {
  let timeout: ReturnType<typeof setTimeout> | undefined
  try {
    return await Promise.race([
      persistence.whenSynced,
      new Promise<never>((_, reject) => {
        timeout = globalThis.setTimeout(
          () => reject(new Error('IndexedDB synchronization timed out')),
          INDEXEDDB_SYNC_TIMEOUT_MS
        )
      })
    ])
  } finally {
    if (timeout !== undefined) globalThis.clearTimeout(timeout)
  }
}

export const createCollaborationSession = async (
  roomId: string,
  initialContent: string,
  collaborationToken: string,
  user: UserInfo,
  onEvent: (event: CollaborationEvent) => void
): Promise<CollaborationSession> => {
  const userColor = usercolors[random.uint32() % usercolors.length]!
  const ydoc = new Y.Doc()
  const ytext = ydoc.getText()
  const persistenceKey = createEditorYjsPersistenceKey(
    user.id,
    roomId === `init:${user.id}` ? undefined : roomId
  )

  let persistence: IndexeddbPersistence | null = null
  try {
    if (typeof globalThis.indexedDB === 'undefined') {
      throw new Error('IndexedDB is unavailable')
    }
    persistence = new IndexeddbPersistence(persistenceKey, ydoc)
    persistence._db?.catch(() => undefined)
    await waitForIndexedDb(persistence)
    registerPersistence(persistenceKey, persistence)
  } catch (error) {
    persistence?.destroy().catch(() => undefined)
    persistence = null
    logger.warn('IndexedDB persistence is unavailable; continuing online:', error)
    onEvent({ type: 'persistence-error' })
  }

  const provider = new WebsocketProvider(
    `${API_CONFIG.BASE_WS_URL}${API_ENDPOINTS.COLLABORATION.WS_ROOMS}`,
    roomId,
    ydoc,
    {
      params: {
        token: collaborationToken
      },
      connect: false,
      resyncInterval: 3000,
      maxBackoffTime: 10000,
      disableBc: false
    }
  )
  let destroyed = false

  provider.on('sync', (isSynced: boolean) => {
    if (destroyed) return
    logger.log('Sync event fired, isSynced:', isSynced)
    logger.log('Document length after sync:', ytext.length)

    if (!isSynced) return

    if (shouldInitializeYjsDocument(ydoc, ytext, initialContent)) {
      logger.log('Inserting initial content:', initialContent.substring(0, 50))
      ytext.insert(0, initialContent)
      onEvent({ type: 'initialized' })
    }

    onEvent({ type: 'synced', content: ytext.toString() })
  })

  provider.on('status', (event: { status: 'connected' | 'disconnected' | 'connecting' }) => {
    if (destroyed) return
    logger.log('WebSocket status:', event.status)

    switch (event.status) {
      case 'connected':
        onEvent({ type: 'connected' })
        break
      case 'disconnected':
        onEvent({ type: 'disconnected' })
        break
      case 'connecting':
        break
    }
  })

  provider.on('connection-error', (event: Event) => {
    if (destroyed) return
    logger.error('WebSocket connection error:', event)
    onEvent({ type: 'connection-error' })
  })

  provider.on('connection-close', (event: CloseEvent | null) => {
    if (destroyed) return
    if (!event) {
      logger.warn('WebSocket 连接关闭: 事件为 null')
      return
    }

    logger.warn('WebSocket 连接关闭:', {
      code: event.code,
      reason: event.reason,
      wasClean: event.wasClean
    })

    if (event.code !== 1000) {
      onEvent({ type: 'connection-closed', code: event.code, reason: event.reason })
    }
  })

  const undoManager = new Y.UndoManager(ytext)

  provider.awareness.setLocalStateField('user', {
    name: user.nickname,
    color: userColor.color,
    colorLight: userColor.light
  })

  const config = yCollab(ytext, provider.awareness, { undoManager })

  return {
    config,
    persistenceKey,
    connect: () => {
      if (!destroyed) provider.connect()
    },
    disconnect: () => {
      if (!destroyed) provider.disconnect()
    },
    updateToken: (token) => {
      if (!destroyed) provider.params.token = token
    },
    restart: (token) => {
      if (destroyed) return
      if (token) provider.params.token = token
      provider.disconnect()
      provider.connect()
    },
    clearPersistence: () => clearYjsDraft(persistenceKey),
    destroy: () => {
      if (destroyed) return
      destroyed = true
      unregisterPersistence(persistenceKey, persistence)
      provider.destroy()
      persistence?.destroy().catch(() => undefined)
      ydoc.destroy()
    }
  }
}

config({
  codeMirrorExtensions(extensions) {
    return [
      ...extensions,
      {
        type: 'compartment',
        extension: yjsCompartment.of([])
      }
    ]
  }
})
