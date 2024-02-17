<script lang="ts" setup>
import { POST } from '@/http/http';
import { MdEditor, type Footers, type ToolbarNames } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { ExportPDF, Emoji } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'

const isComposing = defineModel<boolean>('isComposing')
const content = defineModel<string | undefined>('content')
const skip = defineModel<boolean>('skip')
const input = defineModel<string>('input')
const transColor = defineModel<string>('transColor')

const toolbars: ToolbarNames[] = [
  'revoke', 'next', 'bold', 1, 'underline', 'italic', '-',
  'title', 'strikeThrough', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', 'task', '-',
  'codeRow', 'code', 'link', 'image', 'table', 'mermaid', 'katex', '-',
  0, 'pageFullscreen', 'fullscreen', 'preview', 'htmlPreview', 'catalog', 'github'
]
const footers: Footers[] = ['markdownTotal', '=', 0, 'scrollSwitch']
const regChinese = /[\u4e00-\u9fa5]$/

const onInput = (event: InputEvent) => {
  isComposing.value = event.isComposing
  const content = event.data
  input.value = content ?? ''
  skip.value = event.isComposing && !regChinese.test(content!)
}

const onUploadImg = async (files: File[], callback: Function) => {
  const formdata = new FormData()
  formdata.append('image', files[0])
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  callback([url])
}
</script>

<template>
  <md-editor v-model="content" :preview="false" :toolbars="toolbars" :toolbarsExclude="['github']"
    @onUploadImg="onUploadImg" :footers="footers" @onInput="onInput">
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
#md-editor-v3:deep(.md-editor-footer) {
  height: 30px
}

.trans-radius {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%
}
</style>