import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/http/axios', () => ({
  aiHttpClient: { get: vi.fn() }
}))

vi.mock('@/utils/logger', () => ({
  logger: { warn: vi.fn() }
}))

vi.mock('@/utils/ollamaStream', () => ({
  ollamaStreamRequest: vi.fn(),
  ollamaRequest: vi.fn()
}))

import { useAiGenerate } from '@/composables/useAiGenerate'
import { ollamaRequest, ollamaStreamRequest } from '@/utils/ollamaStream'

const createWorkflow = () => {
  const form = {
    content: '文章正文',
    title: '',
    description: '',
    link: ''
  }
  const workflow = useAiGenerate(form, 'image-model')
  workflow.aiModels.value = [
    {
      name: 'text-model',
      model: 'text-model',
      capabilities: ['completion', 'thinking']
    },
    { name: 'image-model', model: 'image-model', capabilities: ['image'] }
  ]
  workflow.aiModel.value = 'text-model'
  return { form, workflow }
}

const mockTitleSummary = () => {
  vi.mocked(ollamaStreamRequest).mockImplementationOnce(async (options) => {
    options.onChunk({ model: 'text-model', thinking: '分析正文', done: false })
    options.onChunk({
      model: 'text-model',
      response: '{"title":"新标题","description":"新摘要"}',
      done: true
    })
  })
}

describe('useAiGenerate', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('runs title, prompt and image generation in order', async () => {
    const { form, workflow } = createWorkflow()
    mockTitleSummary()
    vi.mocked(ollamaRequest).mockResolvedValueOnce('{"imagePrompt":"cover prompt"}')
    vi.mocked(ollamaStreamRequest).mockImplementationOnce(async (options) => {
      options.onChunk({ model: 'image-model', image: 'base64-image', done: true })
    })

    await workflow.aiGenerate()

    expect(form.title).toBe('新标题')
    expect(form.description).toBe('新摘要')
    expect(workflow.aiThinking.value).toBe('分析正文')
    expect(workflow.aiStep.value).toBe(4)
    expect(workflow.failedStep.value).toBeNull()
    expect(workflow.generatedImageUrl.value).toBe('data:image/png;base64,base64-image')
    expect(workflow.generatedImageDialogVisible.value).toBe(true)
    expect(vi.mocked(ollamaStreamRequest).mock.calls[0]?.[0]).toMatchObject({
      model: 'text-model',
      think: true,
      format: 'json'
    })
    expect(vi.mocked(ollamaRequest).mock.calls[0]?.[3]).toMatchObject({
      think: true,
      format: 'json'
    })
  })

  it('stops on the failed step and exposes an error', async () => {
    const { workflow } = createWorkflow()
    mockTitleSummary()
    vi.mocked(ollamaRequest).mockRejectedValueOnce(new Error('prompt failed'))

    await workflow.aiGenerate()

    expect(workflow.aiStep.value).toBe(2)
    expect(workflow.failedStep.value).toBe(2)
    expect(workflow.aiError.value).toBe('图片提示词生成失败，请重试')
    expect(workflow.aiLoading.value).toBe(false)
    expect(ollamaStreamRequest).toHaveBeenCalledTimes(1)
  })

  it('always resets image loading after an image error', async () => {
    const { workflow } = createWorkflow()
    mockTitleSummary()
    vi.mocked(ollamaRequest).mockResolvedValueOnce('{"imagePrompt":"cover prompt"}')
    vi.mocked(ollamaStreamRequest).mockRejectedValueOnce(new Error('image failed'))

    await workflow.aiGenerate()

    expect(workflow.aiStep.value).toBe(3)
    expect(workflow.failedStep.value).toBe(3)
    expect(workflow.imageGenerating.value).toBe(false)
    expect(workflow.imageProgress.value).toBe(0)
  })

  it('marks image steps as skipped when a cover already exists', async () => {
    const { form, workflow } = createWorkflow()
    form.link = 'https://example.com/cover.png'
    mockTitleSummary()

    await workflow.aiGenerate()

    expect(workflow.aiStep.value).toBe(4)
    expect(workflow.imageSkipReason.value).toBe('已有封面，已跳过')
    expect(ollamaRequest).not.toHaveBeenCalled()
    expect(ollamaStreamRequest).toHaveBeenCalledTimes(1)
  })

  it('does not partially update the form when structured output is incomplete', async () => {
    const { form, workflow } = createWorkflow()
    form.title = '原标题'
    form.description = '原摘要'
    vi.mocked(ollamaStreamRequest).mockImplementationOnce(async (options) => {
      options.onChunk({
        model: 'text-model',
        response: '{"title":"新标题"}',
        done: true
      })
    })

    await workflow.aiGenerate()

    expect(form.title).toBe('原标题')
    expect(form.description).toBe('原摘要')
    expect(workflow.failedStep.value).toBe(1)
  })
})
