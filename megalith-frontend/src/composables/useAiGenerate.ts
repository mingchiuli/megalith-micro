import { aiHttpClient } from '@/http/axios'
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'
import { cleanJsonResponse } from '@/utils/tools'
import { logger } from '@/utils/logger'
import { ollamaStreamRequest, type StreamChunk, type ThinkOption } from '@/utils/ollamaStream'
import type { AiModel, AiModelsResp } from '@/type/entity'
import { i18n, type AppLocale } from '@/i18n'

type AiGenerateForm = {
  content: string
  title: string
  description: string
  link: string
}

type WorkflowStep = 0 | 1 | 2 | 3 | 4
type FailedStep = 1 | 2 | 3 | null
type GenerationContext = {
  content: string
  model: string
  think?: ThinkOption
  locale: AppLocale
}

const AI_URL = API_CONFIG.AI_BASE_URL + API_ENDPOINTS.AI.GENERATE_CONTENT
const STEP_LABEL_KEYS: Record<Exclude<FailedStep, null>, string> = {
  1: 'ai.titleSummarySubject',
  2: 'ai.imagePromptSubject',
  3: 'ai.coverImageSubject'
}

const titleSummaryPrompt = (content: string, locale: AppLocale) =>
  locale === 'zh-CN'
    ? `请仔细阅读以下文章：\n${content}，根据文章内容生成标题和摘要：

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
    : `Read the following article carefully:\n${content}\nGenerate an English title and summary.

Output requirements:
- Return a strict JSON object only
- title: no more than 10 words
- description: no more than 50 words
- Do not include Markdown, code fences, or any surrounding text

Example:
{"title":"Title","description":"Article summary"}`

const imagePrompt = (content: string, locale: AppLocale) =>
  locale === 'zh-CN'
    ? `请仔细阅读以下文章：\n${content}，根据文章内容生成一张图片的英文提示词。

输出要求：
- 格式：严格的JSON字符串
- imagePrompt: 适合Flux模型生成图片的英文提示词，不超过100字，描述文章的核心场景或意象
- 不含任何额外字符和格式标记

示例输出：
{"imagePrompt": "A beautiful sunny day with blue sky and white clouds, person walking in a park with smile"}

注意事项：
- 返回内容必须是可直接解析的JSON
- 不要包含markdown、代码块等任何格式标记
- JSON前后不能有空格或其他字符`
    : `Read the following article carefully:\n${content}\nGenerate an English prompt for a cover image.

Output requirements:
- Return a strict JSON object only
- imagePrompt: no more than 100 words and focused on the article's central scene or idea
- Do not include Markdown, code fences, or any surrounding text

Example:
{"imagePrompt":"A clear visual scene representing the article's central idea"}`

const parseJsonResponse = (response: string): Record<string, unknown> =>
  JSON.parse(cleanJsonResponse(response)) as Record<string, unknown>

const requiredString = (value: unknown, field: string) => {
  if (typeof value !== 'string' || !value.trim()) {
    throw new Error(`AI response is missing ${field}`)
  }
  return value.trim()
}

export const useAiGenerate = (form: AiGenerateForm, imageModel: string) => {
  const aiModels = ref<AiModel[]>([])
  const aiModel = ref('')
  const aiLoading = ref(false)
  const aiStep = ref<WorkflowStep>(0)
  const failedStep = ref<FailedStep>(null)
  const aiError = ref('')
  const aiThinking = ref('')
  const imageSkipReason = ref('')
  const aiPanelVisible = ref(false)
  const imageGenerating = ref(false)
  const imageProgress = ref(0)
  const generatedImageUrl = ref('')
  const generatedImageBase64 = ref('')
  const generatedImageDialogVisible = ref(false)

  const selectedModel = computed(() => aiModels.value.find((item) => item.model === aiModel.value))
  const imageModelAvailable = computed(() =>
    aiModels.value.some((item) => item.model === imageModel || item.name === imageModel)
  )
  const thinkingSupported = computed(() =>
    Boolean(selectedModel.value?.capabilities?.includes('thinking'))
  )

  const getThinkingOption = (): ThinkOption | undefined => {
    const model = selectedModel.value
    if (!model?.capabilities?.includes('thinking')) return undefined
    return model.model.toLowerCase().startsWith('gpt-oss') ? 'medium' : true
  }

  const createGenerationContext = (): GenerationContext => ({
    content: form.content,
    model: aiModel.value,
    think: getThinkingOption(),
    locale: i18n.global.locale.value as AppLocale
  })

  const loadAiModels = async () => {
    try {
      const response = await aiHttpClient.get<AiModelsResp>(API_ENDPOINTS.AI.GET_MODELS)
      aiModels.value = response.data.models
    } catch (error) {
      logger.warn('AI 模型列表加载失败:', error)
    }
  }

  const generateTitleSummary = async (context: GenerationContext) => {
    let fullResponse = ''

    await ollamaStreamRequest({
      url: AI_URL,
      model: context.model,
      prompt: titleSummaryPrompt(context.content, context.locale),
      think: context.think,
      format: 'json',
      onChunk: (chunk: StreamChunk) => {
        if (chunk.thinking) aiThinking.value += chunk.thinking
        if (chunk.response) fullResponse += chunk.response
      }
    })

    const result = parseJsonResponse(fullResponse)
    const title = requiredString(result.title, 'title')
    const description = requiredString(result.description, 'description')
    form.title = title
    form.description = description
  }

  const generateImagePrompt = async (context: GenerationContext) => {
    let fullResponse = ''
    let thinkingStarted = false

    await ollamaStreamRequest({
      url: AI_URL,
      model: context.model,
      prompt: imagePrompt(context.content, context.locale),
      think: context.think,
      format: 'json',
      onChunk: (chunk: StreamChunk) => {
        if (chunk.thinking) {
          if (!thinkingStarted && aiThinking.value) aiThinking.value += '\n\n'
          thinkingStarted = true
          aiThinking.value += chunk.thinking
        }
        if (chunk.response) fullResponse += chunk.response
      }
    })

    const result = parseJsonResponse(fullResponse)
    return requiredString(result.imagePrompt, 'imagePrompt')
  }

  const generateImage = async (prompt: string) => {
    let base64Image = ''
    imageGenerating.value = true
    imageProgress.value = 0

    try {
      await ollamaStreamRequest({
        url: AI_URL,
        model: imageModel,
        prompt,
        onChunk: (chunk: StreamChunk) => {
          if (chunk.completed !== undefined && chunk.total) {
            imageProgress.value = Math.round((chunk.completed / chunk.total) * 100)
          }
          if (chunk.image) base64Image = chunk.image
        }
      })

      if (!base64Image) throw new Error('AI response is missing an image')
      generatedImageUrl.value = `data:image/png;base64,${base64Image}`
      generatedImageBase64.value = base64Image
      generatedImageDialogVisible.value = true
    } finally {
      imageGenerating.value = false
      imageProgress.value = 0
    }
  }

  const generateImageWorkflow = async (context: GenerationContext) => {
    aiStep.value = 2
    const prompt = await generateImagePrompt(context)
    aiStep.value = 3
    await generateImage(prompt)
    aiStep.value = 4
  }

  const handleWorkflowError = (error: unknown) => {
    failedStep.value =
      aiStep.value === 1 || aiStep.value === 2 || aiStep.value === 3 ? aiStep.value : null
    const label = failedStep.value
      ? i18n.global.t(STEP_LABEL_KEYS[failedStep.value])
      : i18n.global.t('ai.contentGeneration')
    aiError.value = i18n.global.t('ai.generationFailed', { step: label })
    logger.warn('AI 生成流程失败:', error)
  }

  const aiGenerate = async () => {
    if (!form.content || !aiModel.value || aiLoading.value) return

    aiPanelVisible.value = true
    aiStep.value = 1
    failedStep.value = null
    aiError.value = ''
    aiThinking.value = ''
    imageSkipReason.value = ''
    aiLoading.value = true
    const context = createGenerationContext()

    try {
      await generateTitleSummary(context)
      if (form.link || !imageModelAvailable.value) {
        imageSkipReason.value = form.link
          ? i18n.global.t('ai.skippedExistingCover')
          : i18n.global.t('ai.skippedUnavailableModel')
        aiStep.value = 4
        return
      }
      await generateImageWorkflow(context)
    } catch (error) {
      handleWorkflowError(error)
    } finally {
      aiLoading.value = false
    }
  }

  const regenerateImage = async () => {
    if (!form.content || !aiModel.value || !imageModelAvailable.value || aiLoading.value) return

    failedStep.value = null
    aiError.value = ''
    imageSkipReason.value = ''
    generatedImageDialogVisible.value = false
    aiLoading.value = true
    const context = createGenerationContext()

    try {
      await generateImageWorkflow(context)
    } catch (error) {
      handleWorkflowError(error)
    } finally {
      aiLoading.value = false
    }
  }

  return {
    aiModels,
    aiModel,
    aiLoading,
    aiStep,
    failedStep,
    aiError,
    aiThinking,
    imageSkipReason,
    thinkingSupported,
    aiPanelVisible,
    imageGenerating,
    imageProgress,
    generatedImageUrl,
    generatedImageBase64,
    generatedImageDialogVisible,
    loadAiModels,
    aiGenerate,
    regenerateImage
  }
}
