<script lang="ts" setup>
let str = '# ssss \n 白日依山尽 \n 黄河入海流'
</script>

<template>
  <mavon-editor :boxShadow="false" :editable="false" :subfield="false" v-model="str" :htmi="false" :toolbarsFlag="false"
    defaultOpen="preview" previewBackground="#ffffff" />
</template>

<style scoped></style>