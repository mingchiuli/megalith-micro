<script lang="ts" setup>
import { UPLOAD } from '@/http/http'
import {
  MdEditor,
  type Footers,
  type ToolbarNames,
  type ExposeParam,
  NormalFooterToolbar
} from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { Emoji, ExportPDF } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'
import { onMounted, ref, useTemplateRef } from 'vue'
import { SensitiveType, Status, type SensitiveTrans, Colors, type SensitiveContentItem } from '@/type/entity'

const emit = defineEmits<{
  composing: [payload: boolean]
  sensitive: [payload: SensitiveTrans]
}>()

const { transColor, formStatus } = defineProps<{
  transColor: string
  formStatus: number | undefined
}>()

const content = defineModel<string | undefined>('content')
const editorRef = useTemplateRef<ExposeParam>('editor')

const uploadPercentage = ref(0)
const showPercentage = ref(false)
const showSensitiveListDialog = ref(false)

const selectSensitiveData = ref<SensitiveContentItem[]>([])

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

onMounted(() => {
  editorRef.value?.domEventHandlers({
    compositionstart: () => {
      emit('composing', true)
    },
    compositionend: () => {
      emit('composing', false)
    }
  })
  document.getElementById('md-editor')!.onmouseup = () => {
    if (formStatus !== Status.SENSITIVE_FILTER) {
      return
    }

    const selection = editorRef.value!.getSelectedText()

    if (!selection || !content.value) {
      return
    }

    selectSensitiveData.value = findAllOccurrences(content.value, selection)
    showSensitiveListDialog.value = true
  }
})

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
    let frontIdx
    if (idx > 5) {
      frontIdx = idx - 5
    } else {
      frontIdx = 0
    }
    let behindIdx
    if (idx + text.length + 5 < content.value!.length) {
      behindIdx = idx + text.length + 5
    } else {
      behindIdx = idx + text.length
    }

    occurrences.push({
      startIndex: match.index,
      endIndex: match.index + text.length,
      content: `${content.value!.substring(frontIdx, idx)}\u001f${text}\u001f${content.value!.substring(idx + text.length, behindIdx)}`
    })
  }
 
  return occurrences
}

const onUploadImg = async (files: File[], callback: Function) => {
  const formdata = new FormData()
  formdata.append('image', files[0])
  const url = await UPLOAD('sys/blog/oss/upload', formdata, uploadPercentage, showPercentage)
  callback([url])
}
</script>

<template>
  <el-dialog v-model="showSensitiveListDialog" title="选择一个词汇" width="500" :before-close="handleClose">
    <el-table :data="selectSensitiveData" @row-click="selectWord" border stripe>
      <el-table-column property="startIndex" label="开始位置" />
      <el-table-column property="endIndex" label="结束位置"  />
      <el-table-column property="content" label="内容" width="200">
        <template #default="scope">
          <el-text>
            {{ scope.row.content.split('\u001f')[0] }}
            <el-text tag="mark" size="small">{{ scope.row.content.split('\u001f')[1] }}</el-text>
            {{ scope.row.content.split('\u001f')[2] }}
          </el-text>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <md-editor
    v-model="content"
    :preview="false"
    :toolbars="toolbars"
    :toolbarsExclude="['github']"
    @on-upload-img="onUploadImg"
    :footers="footers"
    ref="editor"
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
      <NormalFooterToolbar>
        <span class="trans-radius" :style="{ 'background-color': transColor }" />
      </NormalFooterToolbar>
    </template>
  </md-editor>
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
