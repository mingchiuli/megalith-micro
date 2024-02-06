<script lang="ts" setup>
import { POST } from '@/http/http';
import { computed } from 'vue'
import { MdEditor, type Footers, type ToolbarNames } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '@vavt/v3-extension/lib/asset/ExportPDF.css'
import { ExportPDF, Emoji } from '@vavt/v3-extension'
import '@vavt/v3-extension/lib/asset/Emoji.css'


const props = defineProps<{
  content: string | undefined
  isComposing: boolean
  skip: boolean
  input: string
  transColor: string
}>()

const emit = defineEmits<{
  (event: 'update:isComposing', payload: boolean): void
  (event: 'update:content', payload: string | undefined): void
  (event: 'update:skip', payload: boolean): void
  (event: 'update:input', payload: string): void
  (event: 'update:transColor', payload: string): void
}>()

let isComposing = computed({
  get() {
    return props.isComposing
  },
  set(value) {
    emit('update:isComposing', value)
  }
})

let content = computed({
  get() {
    return props.content
  },
  set(value) {
    emit('update:content', value)
  }
})

let skip = computed({
  get() {
    return props.skip
  },
  set(value) {
    emit('update:skip', value)
  }
})

let input = computed({
  get() {
    return props.input
  },
  set(value) {
    emit('update:input', value)
  }
})

let transColor = computed({
  get() {
    return props.transColor
  },
  set(value) {
    emit('update:transColor', value)
  }
})

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