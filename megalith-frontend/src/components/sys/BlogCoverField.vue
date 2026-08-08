<script lang="ts" setup>
import type {
  UploadFile,
  UploadProps,
  UploadRawFile,
  UploadRequestOptions,
  UploadUserFile
} from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ButtonAuth, Colors } from '@/type/entity'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useHttp } from '@/http/http'
import { checkButtonAuth, getButtonTitle } from '@/utils/permissions'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  manageAssets: boolean
  generatedImageUrl: string
  generatedImageBase64: string
  imageGenerating: boolean
}>()

const emit = defineEmits<{ regenerate: [] }>()
const link = defineModel<string>('link', { required: true })
const generatedDialogVisible = defineModel<boolean>('generatedDialogVisible', { required: true })
const { t } = useI18n()
const { DELETE, UPLOAD } = useHttp()
const fileList = ref<UploadUserFile[]>([])
const uploadPercentage = ref(0)
const showPercentage = ref(false)
const uploadLoading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

watch(
  link,
  (value) => {
    fileList.value = value ? [{ name: 'Cover', url: value }] : []
  },
  { immediate: true }
)

const uploadFile = async (file: UploadRawFile) => {
  const formData = new FormData()
  formData.append('image', file)
  link.value = await UPLOAD(
    API_ENDPOINTS.BLOG_ADMIN.OSS_UPLOAD,
    formData,
    uploadPercentage,
    showPercentage
  )
}

const upload = (image: UploadRequestOptions) => uploadFile(image.file)

const remove = async () => {
  if (!link.value) return
  await DELETE<null>(API_ENDPOINTS.BLOG_ADMIN.OSS_DELETE, { url: link.value })
  link.value = ''
}

const preview = (file: UploadFile) => {
  previewUrl.value = file.url ?? ''
  previewVisible.value = true
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
    ElMessage.error(t('common.imageFormatError'))
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error(t('common.imageSizeError'))
    return false
  }
  return true
}

const confirmGeneratedImage = async () => {
  if (!props.generatedImageBase64) return
  uploadLoading.value = true
  try {
    const base64 = props.generatedImageBase64.replace(/^data:image\/\w+;base64,/, '')
    const bytes = Uint8Array.from(atob(base64), (character) => character.charCodeAt(0))
    const file = new File([bytes], 'cover.png', { type: 'image/png' }) as UploadRawFile
    file.uid = Date.now()
    await uploadFile(file)
    generatedDialogVisible.value = false
  } finally {
    uploadLoading.value = false
  }
}
</script>

<template>
  <el-form-item class="cover" :label="getButtonTitle(ButtonAuth.SYS_BLOG_UPLOAD)">
    <el-upload
      v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_UPLOAD)"
      v-model:file-list="fileList"
      action="#"
      list-type="picture-card"
      :before-upload="beforeUpload"
      :limit="1"
      :http-request="upload"
      :on-remove="remove"
      :on-preview="preview"
      :disabled="!manageAssets"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <el-dialog v-model="previewVisible">
      <img class="preview-image" :src="previewUrl" :alt="t('ai.previewAlt')" />
    </el-dialog>

    <el-dialog
      v-model="generatedDialogVisible"
      :title="t('ai.coverPreview')"
      width="500px"
      :close-on-click-modal="true"
    >
      <div class="image-preview-container">
        <img
          v-if="generatedImageUrl"
          :src="generatedImageUrl"
          class="preview-image"
          :alt="t('ai.previewAlt')"
        />
      </div>
      <el-progress
        v-if="showPercentage"
        type="line"
        :percentage="uploadPercentage"
        :color="Colors"
      />
      <template #footer>
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_AI)"
          :loading="imageGenerating"
          @click="emit('regenerate')"
        >
          {{ t('ai.regenerate') }}
        </el-button>
        <el-button
          v-if="checkButtonAuth(ButtonAuth.SYS_BLOG_UPLOAD)"
          type="primary"
          :loading="uploadLoading"
          @click="confirmGeneratedImage"
        >
          {{ t('ai.confirmUpload') }}
        </el-button>
      </template>
    </el-dialog>
  </el-form-item>

  <el-form-item
    v-if="showPercentage && !generatedDialogVisible"
    :label="t('common.uploadProgress')"
  >
    <el-progress type="line" :percentage="uploadPercentage" :color="Colors" />
  </el-form-item>
</template>

<style scoped>
.preview-image {
  display: block;
  max-width: 100%;
  max-height: 400px;
  margin: 0 auto;
  object-fit: contain;
  border-radius: 6px;
}

.image-preview-container {
  padding: 20px;
}

.el-progress {
  width: 150px;
}
</style>
