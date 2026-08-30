<script lang="ts" setup>
import { render } from '@/utils/markdown'

const props = withDefaults(
  defineProps<{
    text: string
    maxLength: number
    markdown?: boolean
  }>(),
  { markdown: false }
)

const preview = computed(() =>
  props.text.length > props.maxLength
    ? `${props.text.substring(0, props.maxLength)}...`
    : props.text
)
</script>

<template>
  <el-popover
    effect="light"
    trigger="hover"
    :placement="markdown ? 'bottom' : 'top'"
    :width="markdown ? 500 : 200"
    :show-after="markdown ? 1000 : 0"
    :popper-style="markdown ? 'height: 300px; overflow: auto' : undefined"
  >
    <template #default>
      <span v-if="markdown" v-html="render(text)" />
      <span v-else>{{ text }}</span>
    </template>
    <template #reference
      ><span>{{ preview }}</span></template
    >
  </el-popover>
</template>
