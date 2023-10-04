<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'

const form = reactive({
  title: '',
  description: '',
  content: '',
  status: '0',
  url: ''
})

const loading = ref(false)

const onSubmit = () => {
  console.log('submit!')
}
</script>

<template>
  <div class="father">
    <el-form :model="form" label-width="50px">
      <el-form-item label="标题" class="title">
        <el-input v-model="form.title" />
      </el-form-item>

      <el-form-item label="摘要" class="desc" label-width="50px">
        <el-input autosize type="textarea" v-model="form.description" />
      </el-form-item>

      <el-form-item label-width="65px" label="可见性" class="status">
        <el-radio-group v-model="form.status">
          <el-radio label="0">公开</el-radio>
          <el-radio label="1">隐藏</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="封面" class="cover" label-width="50px">
        <el-upload class="upload-demo" drag action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
          multiple>
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              封面大小不得超过5MB
            </div>
          </template>
        </el-upload>
      </el-form-item>


      <el-form-item label="内容" class="content" label-width="50px" v-loading="loading">
        <mavon-editor style="height: 100%" v-model="form.content" :subfield="false" :ishljs="true" ref="md"
          code-style="androidstudio" @imgAdd="console.log()" @imgDel="console.log()" class="content"></mavon-editor>
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
  width: 300px;
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