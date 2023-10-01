<script lang="ts" setup>
import { GET } from '@/http/http';
import router from '@/router';
import type { BlogsDesc, PageAdapter } from '@/type/entity'
import { computed, ref } from 'vue'

const emit = defineEmits<{
  (event: 'transSearchData', payload: PageAdapter<BlogsDesc>): void
  (event: 'clear'): void
  (event: 'update:year', payload: string): void
  (event: 'update:keywords', payload: string): void
  (event: 'update:loading', payload: boolean): void
}>()

const props = defineProps<{
  year: string
  keywords: string
  loading: boolean
}>()

let year = computed({
  get() {
    return props.year
  },
  set(value) {
    emit('update:year', value);
  }
})

let keywords = computed({
  get() {
    return props.keywords
  },
  set(value) {
    emit('update:keywords', value);
  }
})

let loading = computed({
  get() {
    return props.loading
  },
  set(value) {
    emit('update:loading', value);
  }
})

const outerVisible = ref(false)
const innerVisible = ref(false)

const query = async (queryString: string, currentPage: number, allInfo: boolean, year: string): Promise<PageAdapter<BlogsDesc>> => {
  loading.value = true
  const data = await GET<PageAdapter<BlogsDesc>>(`/search/public/blog?keywords=${queryString}&currentPage=${currentPage}&allInfo=${allInfo}&year=${year}`);
  return Promise.resolve(data);
};

let timeout: NodeJS.Timeout
const queryAbstractAsync = async (queryString: string, cb: Function) => {
  if (queryString.length > 0) {
    const page: PageAdapter<BlogsDesc> = await query(queryString, -1, false, year.value)
    page.content.forEach((blogsDesc: BlogsDesc) => {
      blogsDesc.value = blogsDesc.highlight
    })
    //节流
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      cb(page.content)
      if (page.content.length === 0) {
        ElMessage.error('No Records')
      }
    }, 1000 * Math.random())
  }
}

const handleSelect = (item: BlogsDesc) => router.push(
  {
    name: 'blog',
    params: {
      id: item.id
    }
  }
)

const queryAllInfo = async (queryString: string, currentPage = 1) => {
  outerVisible.value = false
  if (queryString.length > 0) {
    const page: PageAdapter<BlogsDesc> = await query(queryString, currentPage, true, year.value)
    keywords.value = queryString
    emit('transSearchData', page)
  } else {
    emit('clear')
  }
}

const searchBeforeClose = (close: Function) => {
  keywords.value = ''
  year.value = ''
  refAutocomplete.value.suggestions = []
  emit('clear')
  close()
}

const refAutocomplete = ref<any>()

const yearsCloseEvent = async () => {
  innerVisible.value = false
  if (keywords.value.length > 0) {
    setTimeout(async () => {
      const page = await query(keywords.value, -1, false, year.value)
      page.content.forEach((blogsDesc: BlogsDesc) => {
        blogsDesc.value = blogsDesc.highlight
      })
      refAutocomplete.value.activated = true
      refAutocomplete.value.suggestions = page.content
      if (page.content.length === 0) {
        ElMessage.error('No Records')
      }
    }, 100)
  }
}

defineExpose(
  { queryAllInfo }
)
</script>

<template>
  <el-button class="search-button" @click="outerVisible = true" type="success">Search</el-button>
  <el-dialog v-model="outerVisible" center close-on-press-escape fullscreen align-center
    :before-close="searchBeforeClose">
    <template #default>
      <div class="dialog-content">
        <Hot></Hot>
        <div v-if="year.length > 0">年份：{{ year }}</div>
        <el-autocomplete v-model="keywords" :fetch-suggestions="queryAbstractAsync" placeholder="Please input"
          @select="handleSelect" :trigger-on-focus="false" clearable @keyup.enter="queryAllInfo(keywords)"
          ref="refAutocomplete">
          <template #default="{ item }">
            <div class="value" v-if="item.value.title" v-for="title in item.value.title" v-html="'标题：' + title"></div>
            <div class="value" v-if="item.value.description" v-for="description in item.value.description"
              v-html="'摘要：' + description"></div>
            <div class="value" v-if="item.value.content" v-for="content in item.value.content" v-html="'内容：' + content">
            </div>
          </template>
        </el-autocomplete>
      </div>
      <el-dialog v-model="innerVisible" width="50%" append-to-body title="Archieve">
        <Years v-model:year="year" @close="yearsCloseEvent"></Years>
      </el-dialog>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="queryAllInfo(keywords)">Confirm</el-button>
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

.search-button {
  position: absolute;
  right: 0;
  z-index: 1;
  top: 15px;
}

.el-overlay-dialog .dialog-content {
  text-align: center;
  margin-top: 20px
}
</style>