<script lang="ts" setup>
import { UPLOAD } from '@/http/http'
import { onMounted, ref, onBeforeUnmount, useTemplateRef, watch } from 'vue'
import {
  SensitiveType,
  Status,
  type SensitiveTrans,
  type SensitiveContentItem,
  type UserInfo,
  Colors
} from '@/type/entity'
import { useRoute } from 'vue-router'
import { checkAccessToken } from '@/utils/tools'
import {
  createYjsExtension,
  yjsCompartment,
  cleanupYjs,
  updateProviderToken
} from '@/config/editorConfig'
import type { Footers, ToolbarNames, ExposeParam } from 'md-editor-v3'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const route = useRoute()
const userStr = localStorage.getItem('userinfo')!
const user: UserInfo = JSON.parse(userStr)
const blogId = route.query.id as string | undefined
const roomId = blogId ? `${blogId}` : `init:${user.id}`

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

const { formStatus } = defineProps<{
  formStatus: number
}>()

const text = defineModel<string>('content')
const content = ref('')
watch(content, (newValue: string) => {
  text.value = newValue
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

const editorRef = useTemplateRef<ExposeParam>('editorRef')

const updateEditorExtension = async () => {
  const view = editorRef.value?.getEditorView()
  if (view) {
    const { config, provider } = await createYjsExtension(roomId, text.value!)
    view.dispatch({
      effects: yjsCompartment.reconfigure(config)
    })
    provider.connect()
  }
}

const onUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  const formdata = new FormData()
  formdata.append('image', files[0])
  const url = await UPLOAD('sys/blog/oss/upload', formdata, uploadPercentage, showPercentage)
  callback([url])
}

onMounted(async () => {
  document.getElementById('md-editor')!.onmouseup = () => {
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
  updateEditorExtension()
})

const checkTokenTask = setInterval(async () => {
  const changed = await checkAccessToken()
  if (changed) {
    updateProviderToken()
  }
}, 1000)

onBeforeUnmount(() => {
  cleanupYjs()
  if (checkTokenTask) {
    clearInterval(checkTokenTask)
  }
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

  <MdEditor
    v-model="content"
    :preview="false"
    :toolbars="toolbars"
    :toolbarsExclude="['github']"
    @on-upload-img="onUploadImg"
    :footers="footers"
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
