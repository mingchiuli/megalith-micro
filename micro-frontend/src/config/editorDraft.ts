import type { EditForm, SensitiveItem, Status } from '@/type/entity'

export const EDITOR_DRAFT_VERSION = 1
export const EDITOR_DRAFT_DATABASE = 'megalith-editor-drafts-v1'
const EDITOR_DRAFT_STORE = 'metadata'

export type EditorMetadataDraft = {
  key: string
  title: string
  description: string
  status: Status
  link: string
  sensitiveContentList: SensitiveItem[]
  updatedAt: number
}

const hasIndexedDb = () => typeof indexedDB !== 'undefined'

let databasePromise: Promise<IDBDatabase> | undefined

const openDatabase = (): Promise<IDBDatabase> => {
  if (!hasIndexedDb()) return Promise.reject(new Error('IndexedDB is unavailable'))
  if (databasePromise) return databasePromise

  const pending = new Promise<IDBDatabase>((resolve, reject) => {
    const request = indexedDB.open(EDITOR_DRAFT_DATABASE, EDITOR_DRAFT_VERSION)
    request.onupgradeneeded = () => {
      if (!request.result.objectStoreNames.contains(EDITOR_DRAFT_STORE)) {
        request.result.createObjectStore(EDITOR_DRAFT_STORE, { keyPath: 'key' })
      }
    }
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error ?? new Error('Failed to open IndexedDB'))
    request.onblocked = () => reject(new Error('IndexedDB is blocked'))
  })
  pending.catch(() => {
    databasePromise = undefined
  })
  databasePromise = pending

  return pending
}

const draftTransaction = async <T>(
  mode: IDBTransactionMode,
  operation: (store: IDBObjectStore) => IDBRequest<T>
) => {
  const database = await openDatabase()
  const transaction = database.transaction(EDITOR_DRAFT_STORE, mode)
  return new Promise<T>((resolve, reject) => {
    let result: T
    const request = operation(transaction.objectStore(EDITOR_DRAFT_STORE))
    request.onsuccess = () => {
      result = request.result
    }
    request.onerror = () => reject(request.error ?? new Error('IndexedDB request failed'))
    transaction.oncomplete = () => resolve(result)
    transaction.onerror = () =>
      reject(transaction.error ?? new Error('IndexedDB transaction failed'))
    transaction.onabort = () =>
      reject(transaction.error ?? new Error('IndexedDB transaction aborted'))
  })
}

export const createEditorDraftId = (userId: number, blogId?: string) =>
  `${userId}:${blogId || 'new'}`

export const createEditorYjsPersistenceKey = (userId: number, blogId?: string) =>
  `megalith:editor:yjs:v${EDITOR_DRAFT_VERSION}:${createEditorDraftId(userId, blogId)}`

export const createEditorMetadataDraftKey = (userId: number, blogId?: string) =>
  `megalith:editor:metadata:v${EDITOR_DRAFT_VERSION}:${createEditorDraftId(userId, blogId)}`

export const toEditorMetadataDraft = (
  key: string,
  form: Pick<EditForm, 'title' | 'description' | 'status' | 'link' | 'sensitiveContentList'>
) => ({
  key,
  title: form.title,
  description: form.description,
  status: form.status,
  link: form.link,
  sensitiveContentList: form.sensitiveContentList.map((item) => ({ ...item })),
  updatedAt: Date.now()
})

export const loadEditorMetadataDraft = (key: string) =>
  draftTransaction<EditorMetadataDraft | undefined>('readonly', (store) => store.get(key))

export const saveEditorMetadataDraft = (draft: EditorMetadataDraft) =>
  draftTransaction('readwrite', (store) => store.put(draft))

export const clearEditorMetadataDraft = (key: string) =>
  draftTransaction('readwrite', (store) => store.delete(key))
