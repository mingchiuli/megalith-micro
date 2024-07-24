<script lang="ts" setup>
import { POST } from '@/http/http'
import { MdEditor, type Footers, type ToolbarNames, type ExposeParam } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { ExportPDF, Emoji } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'
import { onMounted, ref } from 'vue'

const emit = defineEmits<{
  composing: [payload: boolean]
  sensitive: [payload: string]
}>()

defineProps<{
  transColor: string
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
    const selection = document.getSelection()
    const selectedText = selection!.toString() // 获取选中的文本

    if (!selectedText) {
      return
    }

    // 选中文本后要执行的操作
    let ele = selection!.getRangeAt(0).startContainer.parentNode
    let idx = 0

    if (ele!.nodeName !== 'DIV') {
      const eleSiblings = []
      let eleSibling = ele!.previousSibling

      while (eleSibling) {
        eleSiblings.push(eleSibling)
        eleSibling = eleSibling.previousSibling
      }

      eleSiblings.forEach(item => {
        //同级别的span文本长度
        idx += item.textContent?.length!
      })

      //从span替换为div
      while (ele!.nodeName !== 'DIV') {
        ele = ele?.parentNode!
      }
    }

    const previousSiblings = []
    let currentElement = ele!.previousSibling

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

    idx += selection!.anchorOffset
    console.log(idx)

    emit('sensitive', selectedText)
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
