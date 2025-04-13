<script lang="ts" setup>
import { UPLOAD } from '@/http/http'
import { onMounted, ref } from 'vue'
import {
  SensitiveType,
  Status,
  type SensitiveTrans,
  type SensitiveContentItem,
  type UserInfo
} from '@/type/entity'
import { commonmark } from '@milkdown/kit/preset/commonmark'
import { Milkdown, useEditor } from '@milkdown/vue'
import { Crepe } from '@milkdown/crepe'
import { Doc } from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import { collab, collabServiceCtx } from '@milkdown/plugin-collab'
import * as random from 'lib0/random'
import { useRoute } from 'vue-router'
import { onBeforeUnmount } from 'vue'
import { onUnmounted } from 'vue'

const route = useRoute()
const userStr = localStorage.getItem('userinfo')!
const user: UserInfo = JSON.parse(userStr)
const blogId = route.query.id as string | undefined
const roomId = blogId ? blogId : `init:${user.id}`

const emit = defineEmits<{
  sensitive: [payload: SensitiveTrans]
}>()

const { formStatus } = defineProps<{
  formStatus: number | undefined
}>()

const content = defineModel<string | undefined>('content')

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
  const regex = new RegExp(pattern, 'g')
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
    selectWord(occurrences[0])
    return
  }

  return occurrences
}

const onUploadImg = async (file: File) => {
  const formdata = new FormData()
  formdata.append('image', file)
  const url = await UPLOAD('sys/blog/oss/upload', formdata, uploadPercentage, showPercentage)
  return url
}

let indexeddbProvider: IndexeddbPersistence | undefined
let websocketProvider: WebsocketProvider | undefined
let crepeInstance: Crepe | undefined
const clearIndexdbDate = () => {
  if (indexeddbProvider) {
    indexeddbProvider.clearData()
  }
}

const editor = useEditor((root) => {
  const crepe = new Crepe({
    root,
    featureConfigs: {
      [Crepe.Feature.ImageBlock]: {
        onUpload: async (file: File) => {
          const url = await onUploadImg(file)
          return url
        }
      }
    }
  })

  crepeInstance = crepe
  const editor = crepe.editor
  
  editor.use(collab)
  crepe.create().then(() => {
    const doc = new Doc()
    // 创建 IndexedDB 持久化实例
    indexeddbProvider = new IndexeddbPersistence(roomId, doc)
    websocketProvider = new WebsocketProvider(
      `${import.meta.env.VITE_BASE_WS_URL}/rooms`,
      roomId,
      doc,
      {
        params: {
          token: localStorage.getItem('accessToken')!
        }
      }
    )

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

    const userColor = usercolors[random.uint32() % usercolors.length]

    websocketProvider.awareness.setLocalStateField('user', {
      name: user.nickname,
      color: userColor.color,
      colorLight: userColor.light
    })

    editor.action((ctx) => {
      const collabService = ctx.get(collabServiceCtx)

      // 等待 IndexedDB 加载完成
      indexeddbProvider!.whenSynced.then(() => {
        collabService.bindDoc(doc).setAwareness(websocketProvider!.awareness)

        websocketProvider!.once('sync', async (isSynced: boolean) => {
          if (isSynced) {
            collabService
              .applyTemplate(content.value!, (remoteNode, templateNode) => {
                return (
                  !remoteNode.textContent || remoteNode.textContent === templateNode.textContent
                )
              })
              .connect()
          }
        })
      })
    })
  })
  return crepe
})

onMounted(() => {
  document.getElementById('milk')!.onmouseup = () => {
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
})

onBeforeUnmount(async () => {
  if (crepeInstance) {
    crepeInstance.destroy()
  }
})

defineExpose({
  clearIndexdbDate
})
</script>

<template>
  <el-dialog
    v-model="showSensitiveListDialog"
    title="选择一个词汇"
    width="500"
    :before-close="handleClose"
  >
    <el-table :data="selectSensitiveData" @row-click="selectWord" border stripe>
      <el-table-column property="startIndex" label="开始位置" align="center" width="100" />
      <el-table-column property="endIndex" label="结束位置" align="center" width="100" />
      <el-table-column property="content" label="内容" align="center">
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

  <Milkdown id="milk" />
</template>

<style scoped>
.md-editor:deep(.md-editor-footer) {
  height: 40px;
}

.trans-radius {
  display: inline-flex;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 5px;
}

.el-progress {
  width: 100px;
  display: inline-flex;
}
</style>
