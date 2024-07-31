<script lang="ts" setup>
import { POST } from '@/http/http'
import { MdEditor, type Footers, type ToolbarNames, type ExposeParam } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { ExportPDF, Emoji } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'
import { onMounted, ref } from 'vue'
import { SensitiveType, Status, type SensitiveTrans } from '@/type/entity'

const emit = defineEmits<{
  composing: [payload: boolean]
  sensitive: [payload: SensitiveTrans]
}>()

const props = defineProps<{
  transColor: string
  formStatus: number | undefined
}>()

const content = defineModel<string | undefined>('content')
const editorRef = ref<ExposeParam>()

const toolbars: ToolbarNames[] = [
  'revoke', 'next', 'bold', 1, 'underline', 'italic', '-',
  'title', 'strikeThrough', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', 'task', '-',
  'codeRow', 'code', 'link', 'image', 'table', 'mermaid', 'katex', '-',
  0, 'pageFullscreen', 'fullscreen', 'preview', 'htmlPreview', 'catalog', 'github'
]
const footers: Footers[] = ['markdownTotal', '=', 0, 'scrollSwitch']

onMounted(() => {
  editorRef.value?.domEventHandlers({
    compositionstart: () => {
      emit('composing', true)
    },
    compositionend: () => {
      emit('composing', false)
    }
  });
  document.getElementById("md-editor")!.onmouseup = () => {
    if (props.formStatus !== Status.SENSITIVE_FILTER) {
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

      if (text.parentNode?.nodeName === 'SPAN') {
        text = text.parentNode
      }
      const eleSiblings: Node[] = []

      for(let i = 0; i < label!.childNodes?.length!; i++) {
        const item = label!.childNodes[i]!
        if (item !== text) {
          eleSiblings.push(item)
        } else {
          break;
        }
      }

      eleSiblings.forEach(item => {
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

    previousSiblings.forEach(item => {
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
    
    const sensitive : SensitiveTrans = {
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
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  callback([url])
}
</script>

<template>
  <md-editor id="md-editor" v-model="content" :preview="false" :toolbars="toolbars" :toolbarsExclude="['github']"
    @on-upload-img="onUploadImg" :footers="footers" ref="editorRef">
    <template #defToolbars>
      <Export-PDF v-model="content" />
      <emoji>
        <template #trigger> Emoji </template>
      </emoji>
    </template>
    <template #defFooters>
      <span class="trans-radius" :style="{ 'background-color': transColor }" />
    </template>
  </md-editor>
</template>

<style scoped>
.md-editor:deep(.md-editor-footer) {
  height: 30px
}

.trans-radius {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%
}
</style>
