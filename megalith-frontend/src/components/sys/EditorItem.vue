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
  console.log(row)
  const sensitive: SensitiveTrans = {
    startIndex: row.startIndex,
    endIndex: row.endIndex,
    type: SensitiveType.CONTENT
  }
  emit('sensitive', sensitive)
  selectSensitiveData.value = []
  showSensitiveListDialog.value = false
}

const findAllOccurrences = (text: string, pattern: string) => {
  const regex = new RegExp(pattern, 'g')
  let match
  const occurrences: SensitiveContentItem[] = []
 
  while ((match = regex.exec(text))) {
    occurrences.push({
      startIndex: match.index,
      endIndex: match.index + match[0].length,
      type: SensitiveType.CONTENT,
      content: match[0]
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
  <el-dialog v-model="showSensitiveListDialog" title="Sensitive List" width="500">
    <el-table :data="selectSensitiveData" @row-click="selectWord" border stripe>
      <el-table-column property="startIndex" label="StartIndex" />
      <el-table-column property="endIndex" label="EndIndex"  />
      <el-table-column property="type" label="Type" />
      <el-table-column property="content" label="Content" width="200"/>
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
