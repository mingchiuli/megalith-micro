import { ref } from 'vue'
import { aiHttpClient } from '@/http/axios'
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'
import { cleanJsonResponse } from '@/utils/tools'
import { logger } from '@/utils/logger'
import type { AiModel, AiModelsResp } from '@/type/entity'

/**
 * Composable for AI content and image generation
 */
export const useAiGenerate = (form: { content: string; title: string; description: string }) => {
  // State
  const aiModels = ref<AiModel[]>([])
  const aiModel = ref('')
  const aiLoading = ref(false)
  const imageGenerating = ref(false)
  const imageProgress = ref(0)
  const generatedImageUrl = ref('')
  const generatedImageBase64 = ref('')
  const generatedImageDialogVisible = ref(false)

  /**
   * Load available AI models
   */
  const loadAiModels = async () => {
    try {
      const response = await aiHttpClient.get(API_ENDPOINTS.AI.GET_MODELS)
      const result: AiModelsResp = response.data
      aiModels.value = result.models
    } catch (e) {
      logger.warn(e)
    }
  }

  /**
   * Generate title and description from content using AI
   */
  const aiGenerateContent = async () => {
    if (!form.content || !aiModel.value) {
      return
    }

    aiLoading.value = true

    const prompt = `请仔细阅读以下文章：\n${form.content}，根据文章内容生成标题和摘要：

输出要求：
- 格式：严格的JSON字符串
- title: 不超过10字的标题
- description: 不超过50字的摘要
- 不含任何额外字符和格式标记

示例输出：
{"title": "标题", "description": "文章摘要"}

注意事项：
- 返回内容必须是可直接解析的JSON
- 不要包含markdown、代码块等任何格式标记
- JSON前后不能有空格或其他字符`

    try {
      const response = await fetch(API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          model: aiModel.value,
          prompt,
          stream: true
        })
      })

      if (!response.ok) {
        return
      }

      const reader = response.body?.getReader()
      if (!reader) {
        return
      }

      const decoder = new TextDecoder()
      let result = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        result += chunk
      }

      // Clean and parse JSON response
      const cleanedResult = cleanJsonResponse(result)
      const parsed = JSON.parse(cleanedResult)

      if (parsed.title) {
        form.title = parsed.title
      }
      if (parsed.description) {
        form.description = parsed.description
      }
    } catch (e) {
      logger.warn(e)
    } finally {
      aiLoading.value = false
    }
  }

  /**
   * Generate image from prompt using AI
   */
  const generateImage = async (prompt: string, imageModel: string) => {
    if (!prompt) {
      return
    }

    imageGenerating.value = true
    imageProgress.value = 0

    try {
      const response = await fetch(API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          model: imageModel,
          prompt,
          stream: false
        })
      })

      if (!response.ok) {
        return
      }

      const data = await response.json()

      if (data.images && data.images[0]) {
        generatedImageBase64.value = data.images[0]
        generatedImageUrl.value = `data:image/png;base64,${data.images[0]}`
        generatedImageDialogVisible.value = true
      }
    } catch (e) {
      logger.warn('图片生成失败:', e)
    } finally {
      imageGenerating.value = false
    }
  }

  return {
    // State
    aiModels,
    aiModel,
    aiLoading,
    imageGenerating,
    imageProgress,
    generatedImageUrl,
    generatedImageBase64,
    generatedImageDialogVisible,
    // Methods
    loadAiModels,
    aiGenerateContent,
    generateImage
  }
}