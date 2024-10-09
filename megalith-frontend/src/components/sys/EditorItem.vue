<script lang="ts" setup>
import { UPLOAD } from '@/http/http'
import { MdEditor, type Footers, type ToolbarNames, type ExposeParam } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { Emoji, ExportPDF } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'
import { onMounted, ref, useTemplateRef } from 'vue'
import { SensitiveType, Status, type SensitiveTrans, Colors } from '@/type/entity'

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

const customHeight = ref(40)

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

    const selection = document.getSelection()
    const selectedText = selection!.toString() // 获取选中的文本

    if (!selectedText) {
      return
    }

    // 选中文本后要执行的操作
    //文本标签,外面套了一层span或div
    let text = selection!.getRangeAt(0).startContainer
    let idx = 0
    let label = text!.parentNode
    if (label!.nodeName !== 'DIV' || label!.childNodes.length !== 1) {
      //从span替换为div
      while (label!.nodeName !== 'DIV') {
        label = label!.parentNode
      }

      if (text.parentNode!.nodeName === 'SPAN') {
        text = text.parentNode!
      }
      const eleSiblings: Node[] = []

      for (let i = 0; i < label!.childNodes?.length!; i++) {
        const item = label!.childNodes[i]!
        if (item !== text) {
          eleSiblings.push(item)
        } else {
          break
        }
      }

      eleSiblings.forEach((item) => {
        //同级别的span文本长度
        idx += item.textContent?.length!
      })
    }

    const previousSiblings = []
    let currentElement = label!.previousSibling

    while (currentElement) {
      previousSiblings.push(currentElement)
      currentElement = currentElement.previousSibling
    }

    previousSiblings.forEach((item) => {
      //上移一行
      idx++
      if (item.textContent) {
        idx += item.textContent.length
      }
    })

    if (selection!.anchorOffset > selection!.focusOffset) {
      idx += selection!.focusOffset
    } else {
      idx += selection!.anchorOffset
    }

    const sensitive: SensitiveTrans = {
      startIndex: idx,
      endIndex: idx + selectedText.length,
      type: SensitiveType.CONTENT
    }

    emit('sensitive', sensitive)
  }
})

const onUploadImg = async (files: File[], callback: Function) => {
  const formdata = new FormData()
  formdata.append('image', files[0])
  const url = await UPLOAD('sys/blog/oss/upload', formdata, uploadPercentage, showPercentage)
  callback([url])
}
</script>

<template>
  <md-editor
    v-model="content"
    :preview="false"
    :toolbars="toolbars"
    :toolbarsExclude="['github']"
    @on-upload-img="onUploadImg"
    :footers="footers"
    ref="editor"
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
      <span class="custom-bottom-items">
        <span class="trans-radius" :style="{ 'background-color': transColor }" />
      </span>

    </template>
  </md-editor>
</template>

<style scoped>
.md-editor:deep(.md-editor-footer) {
  height: v-bind(customHeight + 'px');
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

.custom-bottom-items {
  line-height: v-bind(customHeight + 'px');
}
</style>
