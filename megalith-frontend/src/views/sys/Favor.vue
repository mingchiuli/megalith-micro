<script lang="ts" setup>
import { reactive, ref, toRefs } from 'vue'
import type { PageAdapter, SearchFavors } from '@/type/entity'
import { GET, POST } from '@/http/http'
import type { FormInstance, FormRules } from 'element-plus'

const input = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
let page: PageAdapter<SearchFavors> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 9,
  "pageNumber": 1
})
const { content, totalElements, pageSize, pageNumber } = toRefs(page)

const formRef = ref<FormInstance>()

const formRules = reactive<FormRules<Form>>({
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' }
  ],
  link: [
    { required: true, message: '请输入链接', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ]
})

const form: Form = reactive({
  id: undefined,
  title: '',
  description: '',
  link: '',
  status: 0
})

type Form = {
  id?: string
  title: string
  description: string
  link: string
  status: number
}

const searchFavors = async () => {
  loading.value = true
  const data = await GET<PageAdapter<SearchFavors>>(`/search/website/${pageNumber.value}?keyword=${input.value}`)
  if (data.content.length === 0) {
    clearSearchFavors()
    searchFavors()
    return
  }
  page.content = data.content
  page.totalElements = data.totalElements
  loading.value = false
}

const clearSearchFavors = async () => {
  pageNumber.value = 1
  input.value = ''
  await searchFavors()
}

const infoHandleClose = () => {
  clearForm()
  dialogVisible.value = false
}

const to = (url: string) => {
  window.location.href = url
}

const handleEdit = async (id: string) => {
  const data = await GET<SearchFavors>(`/search/website/info/${id}`)
  form.id = data.id
  form.title = data.title
  form.description = data.description
  form.link = data.link
  form.status = data.status
  dialogVisible.value = true
}

const handleDelete = async (id: string) => {
  await GET<null>(`/search/website/delete/${id}`)
  ElNotification({
    title: '操作成功',
    message: '删除成功',
    type: 'success',
  })
  setTimeout(() => {
    searchFavors()
  }, 1000)
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>('/search/website/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
      })
      clearForm()
      dialogVisible.value = false
      pageNumber.value = 1
      input.value = ''
      setTimeout(() => {
        searchFavors()
      }, 1000)
    }
  })
}

const clearForm = () => {
  form.id = undefined
  form.title = ''
  form.description = ''
  form.link = ''
  form.status = 0
}

const resetForm = (ref: FormInstance) => ref.resetFields()

const handleCurrentChange = async (pageNo: number) => {
  pageNumber.value = pageNo
  await searchFavors()
}

(async () => {
  searchFavors()
})()
</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item>
      <el-input v-model="input" placeholder="Please input" clearable maxlength="20" size="large" class="search-input"
        @clear="clearSearchFavors" @keyup.enter="searchFavors" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" @click="searchFavors">搜索</el-button>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" @click="dialogVisible = true">新增</el-button>
    </el-form-item>
  </el-form>

  <div class="wrapper" v-loading="loading">
    <el-card shadow="never" class="wrapper-content" v-for="favor in content">
      <template #header>
        <el-link @click="to(favor.link)">{{ favor.title }}</el-link>
        <el-button class="icon-button" link @click="handleEdit(favor.id)">编辑</el-button>
        <el-popconfirm title="确认删除?" @confirm="handleDelete(favor.id)">
          <template #reference>
            <el-button class="icon-button" link>删除</el-button>
          </template>
        </el-popconfirm>
      </template>

      <div>
        <div class="text item">
          {{ favor.description }}
        </div>
        <div class="text item">创建时间: {{ favor.created }}</div>
        <div class="text item">
          <div v-if="favor.score !== 'NaN'">{{ "Search Scores: " + favor.score }}</div>
          <p v-if="favor.highlight?.title" v-for="title in favor.highlight.title" v-html="'标题: ' + title"></p>
          <p v-if="favor.highlight?.description" v-for="description in favor.highlight.description"
            v-html="'摘要: ' + description"></p>
        </div>
      </div>
    </el-card>
  </div>
  <el-pagination @current-change="handleCurrentChange" layout="prev, pager, next" :current-page="pageNumber"
    :page-size="pageSize" :total="totalElements" />


  <el-dialog title="新增/编辑" v-model="dialogVisible" width="600px" :before-close="infoHandleClose">
    <el-form :model="form" :rules="formRules" label-width="100px" ref="formRef">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title"></el-input>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description"></el-input>
      </el-form-item>

      <el-form-item label="链接" prop="link">
        <el-input v-model="form.link" placeholder="以https/http开头"></el-input>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label=0>公开</el-radio>
          <el-radio :label=1>隐藏</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label-width="400px">
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
        <el-button @click="resetForm(formRef!)">Reset</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<style scoped>
.search-input {
  width: 200px;
}

.button-form .el-form-item {
  margin-right: 10px;
}

.icon-button {
  float: right;
  padding: 3px 0;
}

.wrapper {
  width: 100%;
  margin-top: 0;
  display: flex;
  justify-content: space-around;
  flex-direction: row;
  flex-wrap: wrap;
}

.wrapper-content {
  width: 350px;
  margin: 20px 0;
}

.el-pagination {
  width: fit-content;
  margin: 0 auto;
}

.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}
</style>