import sanitize from 'sanitize-html'

const allowedTags = [...sanitize.defaults.allowedTags, 'details', 'img', 'summary']

const sanitizeConfig: sanitize.IOptions = {
  allowedTags,
  allowedAttributes: {
    '*': ['aria-*', 'class', 'data-line', 'id', 'role', 'title'],
    a: ['href', 'name', 'rel', 'target'],
    col: ['span'],
    colgroup: ['span'],
    img: ['alt', 'height', 'loading', 'src', 'srcset', 'title', 'width'],
    li: ['value'],
    ol: ['start', 'type'],
    td: ['colspan', 'rowspan'],
    th: ['colspan', 'rowspan', 'scope'],
    time: ['datetime']
  },
  allowedSchemes: ['http', 'https', 'mailto', 'tel'],
  allowProtocolRelative: false,
  disallowedTagsMode: 'completelyDiscard',
  nestingLimit: 100,
  parseStyleAttributes: false
}

export const sanitizeHtml = (html: string): string => sanitize(html, sanitizeConfig)

export const sanitizeHighlight = (html: string): string => sanitizeHtml(html)
