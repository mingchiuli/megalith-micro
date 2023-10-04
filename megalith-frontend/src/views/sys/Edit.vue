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
    <el-form :model="form">
      <el-form-item class="title">
        <el-input v-model="form.title" placeholder="标题" />
      </el-form-item>

      <el-form-item class="desc">
        <el-input autosize type="textarea" v-model="form.description" placeholder="摘要" />
      </el-form-item>

      <el-form-item class="status">
        <el-radio-group v-model="form.status">
          <el-radio label="0">公开</el-radio>
          <el-radio label="1">隐藏</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item class="cover">
        <el-upload drag action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
          multiple>
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
             <em>上传封面</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              封面大小不得超过5MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item class="content" v-loading="loading">
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