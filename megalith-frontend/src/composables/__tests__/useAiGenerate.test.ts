import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'

vi.mock('@/http/http', () => ({
  useHttp: () => ({ aiHttpClient: { get: vi.fn() } })
}))

vi.mock('@/utils/logger', () => ({
  logger: { warn: vi.fn() }
}))

vi.mock('@/utils/ollama', () => ({
  generate: vi.fn()
}))

import { useAiGenerate } from '@/composables/useAiGenerate'
import { generate, type GenerateChunk } from '@/utils/ollama'

const chunkStream = async function* (chunks: GenerateChunk[]) {
  yield* chunks
}

const createWorkflow = () => {
  const form = {
    content: '文章正文',
    title: '',
    description: '',
    link: ''
  }
  let workflow!: ReturnType<typeof useAiGenerate>
  const wrapper = mount({
    setup() {
      workflow = useAiGenerate(form, 'image-model')
      return () => null
    }
  })
  workflow.aiModels.value = [
    {
      name: 'text-model',
      model: 'text-model',
      capabilities: ['completion', 'thinking']
    },
    { name: 'image-model', model: 'image-model', capabilities: ['image'] }
  ]
  workflow.aiModel.value = 'text-model'
  return { form, workflow, wrapper }
}

const mockTitleSummary = () => {
  vi.mocked(generate).mockResolvedValueOnce(
    chunkStream([
      { model: 'text-model', thinking: '分析正文', done: false },
      {
        model: 'text-model',
        response: '{"title":"新标题","description":"新摘要"}',
        done: true
      }
    ])
  )
}

const mockImagePrompt = () => {
  vi.mocked(generate).mockResolvedValueOnce(
    chunkStream([
      { model: 'text-model', thinking: '构思封面', done: false },
      {
        model: 'text-model',
        response: '{"imagePrompt":"cover prompt"}',
        done: true
      }
    ])
  )
}

describe('useAiGenerate', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('runs title, prompt and image generation in order', async () => {
    const { form, workflow } = createWorkflow()
    mockTitleSummary()
    mockImagePrompt()
    vi.mocked(generate).mockResolvedValueOnce(
      chunkStream([{ model: 'image-model', image: 'base64-image', done: true }])
    )

    await workflow.aiGenerate()

    expect(form.title).toBe('新标题')
    expect(form.description).toBe('新摘要')
    expect(workflow.aiThinking.value).toBe('分析正文\n\n构思封面')
    expect(workflow.aiStep.value).toBe(4)
    expect(workflow.failedStep.value).toBeNull()
    expect(workflow.generatedImageUrl.value).toBe('data:image/png;base64,base64-image')
    expect(workflow.generatedImageDialogVisible.value).toBe(true)
    expect(vi.mocked(generate).mock.calls[0]?.[0]).toMatchObject({
      model: 'text-model',
      stream: true,
      think: true,
      format: 'json'
    })
    expect(vi.mocked(generate).mock.calls[1]?.[0]).toMatchObject({
      model: 'text-model',
      stream: true,
      think: true,
      format: 'json'
    })
  })

  it('stops on the failed step and exposes an error', async () => {
    const { workflow } = createWorkflow()
    mockTitleSummary()
    vi.mocked(generate).mockRejectedValueOnce(new Error('prompt failed'))

    await workflow.aiGenerate()

    expect(workflow.aiStep.value).toBe(2)
    expect(workflow.failedStep.value).toBe(2)
    expect(workflow.aiError.value).toBe('图片提示词生成失败，请重试')
    expect(workflow.aiLoading.value).toBe(false)
    expect(generate).toHaveBeenCalledTimes(2)
  })

  it('always resets image loading after an image error', async () => {
    const { workflow } = createWorkflow()
    mockTitleSummary()
    mockImagePrompt()
    vi.mocked(generate).mockRejectedValueOnce(new Error('image failed'))

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
    expect(generate).toHaveBeenCalledTimes(1)
  })

  it('does not partially update the form when structured output is incomplete', async () => {
    const { form, workflow } = createWorkflow()
    form.title = '原标题'
    form.description = '原摘要'
    vi.mocked(generate).mockResolvedValueOnce(
      chunkStream([
        {
          model: 'text-model',
          response: '{"title":"新标题"}',
          done: true
        }
      ])
    )

    await workflow.aiGenerate()

    expect(form.title).toBe('原标题')
    expect(form.description).toBe('原摘要')
    expect(workflow.failedStep.value).toBe(1)
  })
})
