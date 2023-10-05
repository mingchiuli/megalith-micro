<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { type UploadFile, type UploadProps, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { GET, POST } from '@/http/http'
import { useRoute } from 'vue-router'
import type { BlogsEdit } from '@/type/entity'
import router from '@/router'

const fileList = ref<UploadUserFile[]>([])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const disabled = ref(false)
const md = ref<any>()
const route = useRoute()
const blogId = route.query.id
const loading = ref(false)

type Form = {
  id?: number
  title: string
  description: string
  content: string
  status: string
  link: string
}

let form: Form = reactive({
  id: 0,
  title: '',
  description: '',
  content: '',
  status: '0',
  link: ''
})

const loadEditContent = async () => {
  if (blogId) {
    loading.value = true
    const data = await GET<BlogsEdit>(`/sys/blog/echo/${blogId}`)
    form.title = data.title
    form.description = data.description
    form.content = data.content
    form.link = data.link
    form.status = String(data.status)
    form.id = data.id
    if (data.link) {
      fileList.value.push({
        name: 'Cover',
        url: data.link
      })
    }
    loading.value = false
  }
}

const imgAdd = async (idx: number, file: File) => {
  const formdata = new FormData()
  formdata.append('image', file)
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  setTimeout(() => md.value.$img2Url(idx, url), 2000)
}

const imgDel = async (pos: Array<any>) => {
  await GET<null>(`sys/blog/oss/delete?url=${pos[0]}`)
}

const upload = async (image: UploadRequestOptions) => {
  const formdata = new FormData()
  formdata.append('image', image.file)
  const url = await POST<string>('sys/blog/oss/upload', formdata)
  setTimeout(() => {
    fileList.value.push({
      name: image.filename,
      url: url
    })
    form.link = url
  }, 2000)
}

const handleRemove = async (_file: UploadFile) => {
  await GET<null>(`sys/blog/oss/delete?url=${form.link}`)
  form.link = ''
  fileList.value = []
}

const onSubmit = async () => {
  if (form.id === 0) {
    delete form.id
  }
  await POST<null>('/sys/blog/save', form)
  ElNotification({
    title: '操作成功',
    message: '编辑成功',
    type: 'success',
  })
  router.push({
    name: "systemBlogs"
  })
}

const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
  ElMessage.warning(
    `The limit is 1, you selected ${files.length} files this time, add up to ${files.length + uploadFiles.length
    } totally`
  )
}

const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('Avatar picture must be JPG format!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('Avatar picture size can not exceed 5MB!')
    return false
  }
  return true
}

(async () => {
  await loadEditContent()
})()
</script>

<template>
  <div class="father">
    <el-form :model="form">
      <el-form-item class="title">
        <el-input v-model="form.title" placeholder="标题" maxlength="10" />
      </el-form-item>

      <el-form-item class="desc">
        <el-input autosize type="textarea" v-model="form.description" placeholder="摘要" maxlength="30" />
      </el-form-item>

      <el-form-item class="status">
        <el-radio-group v-model="form.status">
          <el-radio label="0">公开</el-radio>
          <el-radio label="1">隐藏</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item class="cover">
        <span style="margin-right: 10px;">封面</span>
        <el-upload action="#" list-type="picture-card" :before-upload="beforeAvatarUpload" :limit="1"
          :on-exceed="handleExceed" :http-request="upload" :on-remove="handleRemove" :file-list="fileList">
          <el-icon>
            <Plus />
          </el-icon>
          <template #file="{ file }">
            <div>
              <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
              <span class="el-upload-list__item-actions">
                <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                  <el-icon><zoom-in /></el-icon>
                </span>
                <span v-if="!disabled" class="el-upload-list__item-delete" @click="handleRemove(file)">
                  <el-icon>
                    <Delete />
                  </el-icon>
                </span>
              </span>
            </div>
          </template>
        </el-upload>

        <el-dialog v-model="dialogVisible">
          <img w-full :src="dialogImageUrl" alt="Preview Image" />
        </el-dialog>
      </el-form-item>

      <el-form-item class="content" v-loading="loading">
        <mavon-editor style="height: 100%" v-model="form.content" :subfield="false" :ishljs="true" ref="md"
          code-style="androidstudio" @imgAdd="imgAdd" @imgDel="imgDel" class="content"></mavon-editor>
      </el-form-item>
      <el-button type="primary" @click="onSubmit" class="submit-button">提交</el-button>
    </el-form>
  </div>
</template>

<style scoped>
.father {
  max-width: 40rem;
  margin: 0 auto;
}

.title {
  width: 200px;
}

.desc {
  width: 300px;
}

.status {
  width: 300px;
}

.el-upload__text {
  width: 290px;
}

.submit-button {
  display: block;
  margin: 10px 0 auto auto;
}

.content {
  max-width: 40rem;
  margin: 0 auto;
}
</style>