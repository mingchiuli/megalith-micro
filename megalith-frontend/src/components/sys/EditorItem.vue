<script lang="ts" setup>
import "@milkdown/theme-nord/style.css";
import { emoji } from "@milkdown/plugin-emoji";
import { diagram } from "@milkdown/plugin-diagram";
import { clipboard } from "@milkdown/kit/plugin/clipboard";
import { history } from "@milkdown/kit/plugin/history";
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
import { imageBlockConfig, imageBlockComponent } from "@milkdown/kit/component/image-block";

import { Editor, rootCtx } from "@milkdown/kit/core"
import { nord } from "@milkdown/theme-nord";
import { Milkdown, useEditor } from '@milkdown/vue'
import { Crepe } from '@milkdown/crepe'
import { Doc } from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import { collab, collabServiceCtx } from '@milkdown/plugin-collab'
import * as random from 'lib0/random'
import { useRoute } from 'vue-router'
import { onBeforeUnmount } from 'vue'

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

useEditor((root) => {
  const editor = Editor.make()
    .config((ctx) => {
      ctx.set(rootCtx, root);
    })
    .config(nord)
    .config((ctx) => {
        ctx.update(imageBlockConfig.key, (prev) => ({
          ...prev,
          onUpload: onUploadImg,
        }));
      })
    .use(commonmark)
    .use(history)
    .use(diagram)
    .use(clipboard)
    .use(emoji)
    .use(imageBlockComponent)
    .use(collab);

  editor.create().then(() => {
    const doc = new Doc()
    // 创建 IndexedDB 持久化实例
    indexeddbProvider = new IndexeddbPersistence(roomId, doc)
    indexeddbProvider!.whenSynced.then(() => {
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
  
  return editor
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
    console.log('123')
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
:deep(.ProseMirror) {
  /* 第一个光标位置的修复 */
  & > .ProseMirror-yjs-cursor:first-child {
    margin-top: 16px;
  }

  /* 首个块级元素的间距 */
  & p:first-child,
  & h1:first-child,
  & h2:first-child,
  & h3:first-child,
  & h4:first-child,
  & h5:first-child,
  & h6:first-child {
    margin-top: 16px;
  }
}

/* 远程用户光标样式 */
:deep(.ProseMirror-yjs-cursor) {
  position: relative;
  margin-left: -1px;
  margin-right: -1px;
  border-left: 1px solid black;
  border-right: 1px solid black;
  border-color: orange;
  word-break: normal;
  pointer-events: none;

  /* 光标上方的用户名 */
  & > div {
    position: absolute;
    top: -1.05em;
    left: -1px;
    font-size: 13px;
    background-color: rgb(250, 129, 0);
    font-family: serif;
    font-style: normal;
    font-weight: normal;
    line-height: normal;
    user-select: none;
    color: white;
    padding-left: 2px;
    padding-right: 2px;
    white-space: nowrap;
  }
}

/* 编辑器内容区域内边距 */
:deep(.milkdown .ProseMirror) {
  padding: 5px 10px;
}

.el-progress {
  width: 100px;
  display: inline-flex;
}
</style>
