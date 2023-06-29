<script lang="ts" setup>
import { onMounted, ref } from 'vue'
const state = ref('')

const outerVisible = ref(false)
const innerVisible = ref(false)

interface LinkItem {
  value: string
  link: string
}

const links = ref<LinkItem[]>([])

const loadAll = () => {
  return [
    { value: 'vue', link: 'https://github.com/vuejs/vue' },
    { value: 'element', link: 'https://github.com/ElemeFE/element' },
    { value: 'cooking', link: 'https://github.com/ElemeFE/cooking' },
    { value: 'mint-ui', link: 'https://github.com/ElemeFE/mint-ui' },
    { value: 'vuex', link: 'https://github.com/vuejs/vuex' },
    { value: 'vue-router', link: 'https://github.com/vuejs/vue-router' },
    { value: 'babel', link: 'https://github.com/babel/babel' },
  ]
}

let timeout: NodeJS.Timeout
const querySearchAsync = (queryString: string, cb: (arg: any) => void) => {
  const results = queryString
    ? links.value.filter(createFilter(queryString))
    : links.value

  clearTimeout(timeout)
  timeout = setTimeout(() => {
    cb(results)
  }, 1000 * Math.random())
}
const createFilter = (queryString: string) => {
  return (restaurant: LinkItem) => {
    return (
      restaurant.value.toLowerCase().startsWith(queryString.toLowerCase())
    )
  }
}

const handleSelect = (item: LinkItem) => {
  console.log(item)
}

onMounted(() => {
  links.value = loadAll()
})

</script>
<template>
  <el-button class="search-button" @click="outerVisible = true">Search</el-button>
  <el-dialog v-model="outerVisible" fullscreen center align-center>
    <template #default>
      <div class="dialog-content">
        <el-autocomplete v-model="state" :fetch-suggestions="querySearchAsync" placeholder="Please input" @select="handleSelect" />
      </div>
      <el-dialog v-model="innerVisible" width="50%" append-to-body title="Archieve">aaa</el-dialog>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary">Confirm</el-button>
        <el-button type="primary" @click="innerVisible = true">Archieve</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-content {
  margin: 0 auto;
  max-width: max-content;
}

</style>