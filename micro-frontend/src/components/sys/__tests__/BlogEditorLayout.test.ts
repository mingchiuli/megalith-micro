import { describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'
import { Status } from '@/type/entity'

vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: () => true,
  getButtonTitle: () => 'Cover'
}))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ DELETE: vi.fn(), UPLOAD: vi.fn() })
}))

import BlogAiActions from '@/components/sys/BlogAiActions.vue'
import BlogAiPanel from '@/components/sys/BlogAiPanel.vue'
import BlogCoverField from '@/components/sys/BlogCoverField.vue'
import BlogMetadataFields from '@/components/sys/BlogMetadataFields.vue'

describe('blog editor layout', () => {
  it('keeps model actions beside the description and the AI panel before status', () => {
    const Host = defineComponent({
      setup: () => () =>
        h('div', [
          h(
            BlogMetadataFields,
            {
              title: '',
              description: '',
              status: Status.NORMAL,
              manageMetadata: true,
              sensitiveTags: []
            },
            {
              'description-actions': () =>
                h(BlogAiActions, {
                  model: 'text-model',
                  models: [{ name: 'text-model', model: 'text-model' }],
                  loading: false,
                  contentReady: true,
                  manageMetadata: true
                }),
              'after-description': () =>
                h(BlogAiPanel, {
                  visible: true,
                  step: 1,
                  failedStep: null,
                  error: '',
                  thinking: '',
                  imageSkipReason: '',
                  thinkingSupported: true,
                  imageGenerating: false,
                  imageProgress: 0
                })
            }
          )
        ])
    })
    const wrapper = mount(Host)

    expect(wrapper.get('.desc-input-group').find('.ai-actions').exists()).toBe(true)
    expect(wrapper.get('.ai-actions').text()).toContain('✨AI')
    const description = wrapper.get('.desc-input-group').element
    const panel = wrapper.get('.ai-panel').element
    const status = wrapper.get('.status').element
    expect(
      description.compareDocumentPosition(panel) & Node.DOCUMENT_POSITION_FOLLOWING
    ).toBeTruthy()
    expect(panel.compareDocumentPosition(status) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
  })

  it('keeps the cover preview and upload progress wrappers', () => {
    const wrapper = mount(BlogCoverField, {
      props: {
        link: '',
        generatedDialogVisible: true,
        manageAssets: true,
        generatedImageUrl: 'data:image/png;base64,image',
        generatedImageBase64: 'image',
        imageGenerating: false
      },
      global: {
        stubs: {
          ElDialog: {
            template: '<div><slot /><slot name="footer" /></div>'
          }
        }
      }
    })

    expect(wrapper.find('.image-preview-container .preview-image').exists()).toBe(true)
    expect(wrapper.find('.upload-progress-wrapper').exists()).toBe(true)
  })
})
